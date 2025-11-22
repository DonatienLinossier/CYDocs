import { useState, useRef, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import "../styles/App.css";
import "../styles/Document.css";

// --- CONFIGURATION ---
const WEBSOCKET_URL = "http://localhost:8080/document/ws";
const API_URL = "http://localhost:8080/document/documents/get"; // Base URL for fetching

export default function Document() {
  const { id } = useParams();
  const isNew = id === "new";
  const docId = isNew ? null : parseInt(id, 10);
  const navigate = useNavigate();
  const editorRef = useRef(null);

  // --- STATE ---
  const wsClientRef = useRef(null);
  const [sessionId] = useState(() => "user-" + Math.random().toString(36).substr(2, 9));
  
  // Start with NULL so we know we are loading
  const [doc, setDoc] = useState(null); 
  const [status, setStatus] = useState("Loading...");
  
  // Track last saved content to detect changes
  const lastSavedRef = useRef("");

  // --- 1. FETCH DOCUMENT (Load on Start) ---
  useEffect(() => {
    // A. Handle New Document (No Fetch Needed)
    if (isNew) {
      const newDoc = {
        id: null,
        title: "Untitled Document",
        author: "You",
        content: ""
      };
      setDoc(newDoc);
      setStatus("New (Unsaved)");
      lastSavedRef.current = "";
      return;
    }

    // B. Handle Existing Document (Fetch from API)
    setStatus("Loading document...");
    
    fetch(`${API_URL}/${docId}`)
      .then(async (res) => {
        if (res.status === 404) throw new Error("404");
        if (!res.ok) throw new Error("Network Error");
        return res.json();
      })
      .then((data) => {
        // 1. Update React State
        setDoc(data);
        
        // 2. Update Editor Content immediately
        if (editorRef.current) {
          editorRef.current.innerHTML = data.content;
        }
        
        // 3. Sync Reference so we don't show "Unsaved Changes" immediately
        lastSavedRef.current = data.content;
        setStatus("Ready");
      })
      .catch((err) => {
        console.error("Fetch error:", err);
        if (err.message === "404") {
          alert("Document not found!");
          navigate("/"); // Go back to home
        } else {
          setStatus("Error loading file");
        }
      });
  }, [docId, isNew, navigate]);

  // --- 2. WEBSOCKET CONNECTION (Waits for doc to be loaded) ---
  useEffect(() => {
    // Stop if doc hasn't loaded yet
    if (isNew || !doc) return; 

    const client = new Client({
      webSocketFactory: () => new SockJS(WEBSOCKET_URL),
      reconnectDelay: 5000,
      debug: (str) => console.log(str),

      onConnect: (frame) => {
        setStatus("Connected (Live)");
        console.log("Connected: " + frame);

        client.subscribe(`/topic/doc/${doc.id}`, (msg) => {
          if (msg.body) {
            try {
              const payload = JSON.parse(msg.body);
              // Only update if it's from someone else
              if (payload.sender !== sessionId) {
                setDoc((prev) => ({ ...prev, content: payload.content }));
                
                if (editorRef.current) {
                   // Avoid resetting cursor if we are focused
                   if (document.activeElement !== editorRef.current) {
                       editorRef.current.innerHTML = payload.content;
                   } else {
                       // Force update (might jump cursor, but keeps sync)
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
        console.error("Broker error: " + frame.headers["message"]);
        setStatus("Connection Error");
      },
      onWebSocketClose: () => {
        setStatus("Disconnected");
      }
    });

    client.activate();
    wsClientRef.current = client;

    return () => {
      if (wsClientRef.current) wsClientRef.current.deactivate();
    };
  }, [docId, isNew, sessionId, doc?.id]); // Depend on doc.id so we connect only after fetch

  // --- ACTIONS ---
  const format = (cmd, value = null) => {
    document.execCommand(cmd, false, value);
    editorRef.current && editorRef.current.focus();
    handleInput();
  };

  const handleInput = () => {
    if (editorRef.current) {
      const content = editorRef.current.innerHTML;
      setStatus("Connected (Live)");

      // Send via WebSocket
      if (wsClientRef.current && wsClientRef.current.connected) {
         const payload = { sender: sessionId, content: content };
         wsClientRef.current.publish({
            destination: `/app/doc/${docId}`,
            body: JSON.stringify(payload)
         });
      }
    }
  };

  const save = useCallback(() => {
    // TODO: Implement API Save (POST/PUT) here
    console.log("Saving...");
    setStatus("Saving...");
    
    // Example:
    // fetch("http://localhost:8080/document/save", { method: "POST", body: ... })
  }, [doc]);

  // --- RENDER ---
  if (!doc) {
    return <div className="app-root"><main className="site-main"><h2>Loading Document...</h2></main></div>;
  }

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
          </div>
        </div>

        <div
          ref={editorRef}
          contentEditable
          suppressContentEditableWarning
          className="doc-editor"
          onInput={handleInput}
          // Only used for initial render. Subsequent updates are handled manually via refs to preserve cursor if possible
          dangerouslySetInnerHTML={{ __html: doc.content }}
        />
        <p>test</p>
      </main>
    </div>
    
  );
}