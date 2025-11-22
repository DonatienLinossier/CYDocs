import { useState, useRef, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import "../styles/App.css";
import "../styles/Document.css";

// --- WEBSOCKET CONFIGURATION ---
const WEBSOCKET_URL = "http://localhost:8080/document/ws";

export default function Document() {
  const { id } = useParams();
  const isNew = id === "new";
  const docId = isNew ? null : parseInt(id, 10);
  const navigate = useNavigate();
  const editorRef = useRef(null);

  // --- WEBSOCKET STATE ---
  const wsClientRef = useRef(null);
  // Unique session ID to prevent overwriting our own changes
  const [sessionId] = useState(() => "user-" + Math.random().toString(36).substr(2, 9));

  // --- LOCAL STORAGE DATA (Existing Logic) ---
  const sampleDocs = [
    { id: 1, title: "Project Plan - Q4", author: "Alice", content: "<p>Project plan contents…</p>" },
    { id: 2, title: "Design Guidelines", author: "Bob", content: "<p>Design guidelines contents…</p>" },
    { id: 3, title: "User Onboarding", author: "Carol", content: "<p>User onboarding contents…</p>" },
  ];

  const [docs, setDocs] = useState(() => {
    try {
      const raw = localStorage.getItem("cy_docs");
      return raw ? JSON.parse(raw) : sampleDocs;
    } catch {
      return sampleDocs;
    }
  });

  const currentUser = (() => {
    try {
      const s = localStorage.getItem("cy_user");
      return s ? JSON.parse(s) : null;
    } catch { return null; }
  })();

  const [doc, setDoc] = useState(() => {
    if (isNew) {
      return {
        id: null,
        title: "Untitled document",
        author: currentUser?.name || "You",
        content: "<p></p>",
      };
    }
    return docs.find((d) => d.id === docId) || null;
  });

  const [status, setStatus] = useState("");
  const lastSavedRef = useRef(doc ? doc.content : "");

  // --- WEBSOCKET CONNECTION EFFECT (Modern V7 Syntax) ---
  useEffect(() => {
    // 1. Don't connect if it's a new unsaved doc or invalid
    if (isNew || !doc) return;

    // 2. Initialize Stomp Client
    const client = new Client({
      // The factory allows the client to recreate the socket on disconnect (Auto-Reconnect)
      webSocketFactory: () => new SockJS(WEBSOCKET_URL),
      
      // Reconnect every 5 seconds if connection is lost
      reconnectDelay: 5000, 
      
      // Debug logs (optional, help you see what's happening in console)
      debug: (str) => console.log(str),

      onConnect: (frame) => {
        setStatus("Connected");
        console.log("Connected: " + frame);

        // 3. Subscribe to the document topic
        client.subscribe(`/topic/doc/${doc.id}`, (msg) => {
          if (msg.body) {
            try {
              const payload = JSON.parse(msg.body);
              
              // 4. Only update if the sender is NOT us (to avoid cursor jumps)
              if (payload.sender !== sessionId) {
                // Update React State
                setDoc((prev) => ({ ...prev, content: payload.content }));
                
                // Update Editor DOM
                if (editorRef.current) {
                  // Note: Updating innerHTML while user is typing will reset cursor.
                  // For this simple implementation, we accept that risk or check focus.
                  if (document.activeElement !== editorRef.current) {
                      editorRef.current.innerHTML = payload.content;
                  } else {
                      // If focused, we still update to ensure sync, though it may feel jumpy.
                      editorRef.current.innerHTML = payload.content;
                  }
                  lastSavedRef.current = payload.content;
                }
              }
            } catch (e) {
              console.error("WS Parse error", e);
            }
          }
        });
      },

      onStompError: (frame) => {
        console.error("Broker reported error: " + frame.headers["message"]);
        console.error("Additional details: " + frame.body);
        setStatus("Connection Error");
      },

      onWebSocketClose: () => {
        setStatus("Disconnected");
      }
    });

    // Activate the client
    client.activate();
    wsClientRef.current = client;

    // Cleanup on unmount
    return () => {
      if (wsClientRef.current) {
        wsClientRef.current.deactivate();
      }
    };
  }, [docId, isNew, sessionId]);

  // --- STANDARD EFFECTS ---
  useEffect(() => {
    if (!isNew) setDoc(docs.find((d) => d.id === docId) || null);
  }, [docs, docId, isNew]);

  useEffect(() => {
    if (editorRef.current) editorRef.current.focus();
  }, []);

  useEffect(() => {
    if (doc) lastSavedRef.current = doc.content || "";
  }, [doc]);

  if (!doc) {
    return (
      <div className="app-root">
        <main className="site-main">
          <div className="doc-notfound">
            <button className="btn btn-outline" onClick={() => navigate(-1)}>Back</button>
            <h2>Document not found</h2>
            <p>The requested document does not exist.</p>
          </div>
        </main>
      </div>
    );
  }

  // --- ACTIONS ---
  const format = (cmd, value = null) => {
    document.execCommand(cmd, false, value);
    editorRef.current && editorRef.current.focus();
    handleInput(); // Trigger update immediately on formatting
  };

  const save = useCallback(() => {
    const html = editorRef.current ? editorRef.current.innerHTML : doc.content;
    if (!isNew && html === lastSavedRef.current) return;

    try {
      const raw = localStorage.getItem("cy_docs");
      const storedDocs = raw ? JSON.parse(raw) : docs;

      if (isNew) {
        const maxId = storedDocs.reduce((m, d) => Math.max(m, d.id || 0), 0);
        const newId = maxId + 1;
        const created = { ...doc, id: newId, content: html };
        const updated = [...storedDocs, created];
        localStorage.setItem("cy_docs", JSON.stringify(updated));

        setDocs(updated);
        setDoc(created);
        if (editorRef.current) editorRef.current.innerHTML = created.content;
        lastSavedRef.current = created.content;

        setStatus("Saved");
        setTimeout(() => setStatus(""), 1500);
        navigate(`/document/${newId}`, { replace: true });
        return;
      }

      const updated = storedDocs.map((d) => (d.id === doc.id ? { ...d, content: html, title: doc.title } : d));
      localStorage.setItem("cy_docs", JSON.stringify(updated));

      setDocs(updated);
      const updatedDoc = updated.find((d) => d.id === doc.id);
      if (updatedDoc) {
        setDoc(updatedDoc);
        if (editorRef.current) editorRef.current.innerHTML = updatedDoc.content;
        lastSavedRef.current = updatedDoc.content;
      }

      setStatus("Saved");
      setTimeout(() => setStatus(""), 1500);
    } catch (e) {
      console.error("Save failed", e);
      setStatus("Save failed");
      setTimeout(() => setStatus(""), 2000);
    }
  }, [doc, docs, isNew, navigate]);

  const download = () => {
    const blob = new Blob([editorRef.current?.innerText || ""], { type: "text/plain;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `${(doc.title || "document").replace(/\s+/g, "_")}.txt`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const copyLink = async () => {
    try {
      await navigator.clipboard.writeText(window.location.href);
      setStatus("Link copied");
      setTimeout(() => setStatus(""), 1500);
    } catch {
      setStatus("Copy failed");
    }
  };

  // Autosave
  useEffect(() => {
    const interval = setInterval(() => {
      try {
        if (editorRef.current && editorRef.current.innerHTML !== lastSavedRef.current) {
          save();
        }
      } catch (e) {}
    }, 60_000);

    return () => clearInterval(interval);
  }, [save]);

  // --- INPUT HANDLER (WebSocket Sending) ---
  const handleInput = () => {
    if (editorRef.current) {
      const content = editorRef.current.innerHTML;

      // 1. Check local unsaved status
      if (content !== lastSavedRef.current) {
        setStatus("Unsaved changes");
      } else {
        setStatus("");
      }

      // 2. Send via WebSocket
      // V7 Syntax: use 'publish' instead of 'send'
      if (wsClientRef.current && wsClientRef.current.connected) {
         const payload = {
            sender: sessionId,
            content: content
         };
         
         wsClientRef.current.publish({
            destination: `/app/doc/${docId}`,
            body: JSON.stringify(payload)
         });
      }
    }
  };

  // --- RENDER (Unchanged) ---
  return (
    <div className="app-root">
      <main className="site-main">
        <div className="doc-header">
          <div className="doc-header-left">
            <button className="btn btn-outline" onClick={() => navigate(-1)}>Back</button>
            {isNew ? (
              <input
                className="doc-title-input"
                value={doc.title}
                onChange={(e) => setDoc((s) => ({ ...s, title: e.target.value }))}
              />
            ) : (
              <strong className="doc-title">{doc.title}</strong>
            )}
            <span className="doc-meta"> created by {doc.author}</span>
          </div>
          <div className="doc-status">{status}</div>
        </div>

        <div className="doc-toolbar-center">
          <div className="doc-toolbar">
            <button type="button" className="btn" onClick={() => format("undo")} title="Undo">↶</button>
            <button type="button" className="btn" onClick={() => format("redo")} title="Redo">↷</button>
            <button type="button" className="btn" onClick={() => format("bold")} title="Bold"><b>B</b></button>
            <button type="button" className="btn" onClick={() => format("italic")} title="Italic"><i>I</i></button>
            <button type="button" className="btn" onClick={() => format("underline")} title="Underline"><u>U</u></button>
            <button type="button" className="btn" onClick={() => format("strikeThrough")} title="Strike">S</button>
            <button type="button" className="btn" onClick={() => format("formatBlock", "<H2>")} title="Heading">H2</button>
            <button type="button" className="btn" onClick={() => format("formatBlock", "<PRE>")} title="Code">{"</>"}</button>
            <button type="button" className="btn" onClick={() => format("insertUnorderedList")} title="Bullet list">•</button>
            <button type="button" className="btn" onClick={() => format("insertOrderedList")} title="Numbered list">1.</button>
            <button type="button" className="btn" onClick={() => format("justifyLeft")} title="Align left">⟵</button>
            <button type="button" className="btn" onClick={() => format("justifyCenter")} title="Align center">⤒</button>
            <button type="button" className="btn" onClick={() => format("justifyRight")} title="Align right">⟶</button>
            <button type="button" className="btn btn-outline" onClick={save} title="Save">Save</button>
            <button type="button" className="btn btn-secondary" onClick={download} title="Download">Download</button>
            <button type="button" className="btn btn-outline" onClick={copyLink} title="Copy link">Share</button>
          </div>
        </div>

        <div
          ref={editorRef}
          contentEditable
          suppressContentEditableWarning
          className="doc-editor"
          onInput={handleInput}
          dangerouslySetInnerHTML={{ __html: doc.content }}
        />
      </main>
    </div>
  );
}