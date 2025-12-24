import { useState, useRef, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import axios from "axios"; // <--- added
import "../styles/App.css";
import "../styles/Document.css";

// --- CONFIGURATION ---
const WEBSOCKET_URL = "http://localhost:8080/document/ws";
const API_URL = "http://localhost:8080/document/documents/get";

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
  
  const lastSavedRef = useRef("");

  // --- 1. FETCH DOCUMENT (Load on Start) ---
  useEffect(() => {
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

    setStatus("Loading document...");
    
    fetch(`${API_URL}/${docId}`)
      .then(async (res) => {
        if (res.status === 404) throw new Error("404");
        if (!res.ok) throw new Error("Network Error");
        return res.json();
      })
      .then((data) => {
        setDoc(data);
        if (editorRef.current) {
          editorRef.current.innerHTML = data.content;
        }
        lastSavedRef.current = data.content;
        setStatus("Ready");
      })
      .catch((err) => {
        console.error("Fetch error:", err);
        if (err.message === "404") {
          alert("Document not found!");
          navigate("/"); 
        } else {
          setStatus("Error loading file");
        }
      });
  }, [docId, isNew, navigate]);

  // --- 2. WEBSOCKET CONNECTION ---
  useEffect(() => {
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
              if (payload.sender !== sessionId) {
                setDoc((prev) => ({ ...prev, content: payload.content }));
                if (editorRef.current) {
                   if (document.activeElement !== editorRef.current) {
                       editorRef.current.innerHTML = payload.content;
                   } else {
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
  }, [docId, isNew, sessionId, doc?.id]);

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

      if (wsClientRef.current && wsClientRef.current.connected) {
         const payload = { sender: sessionId, content: content };
         wsClientRef.current.publish({
            destination: `/app/doc/${docId}`,
            body: JSON.stringify(payload)
         });
      }
    }
  };

  const download = () => {
    // FIX: Ensure doc exists before accessing title
    if (!doc) return;
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

  const save = useCallback(() => {
    console.log("Saving...");
    // Implement API Save here
  }, [doc]);

  // new handler: create document if new before navigating back
  const handleBack = async () => {
    if (isNew && doc) {
      console.log("Creating new document...");
      const payload = {
        title: doc.title || "Untitled Document",
        author: doc.author || "You",
        content: editorRef.current?.innerHTML ?? doc.content ?? ""
      };

      try {
        setStatus("Creating document...");
        const token = localStorage.getItem("cy_token");
        const resp = await axios.post(
          "http://127.0.0.1:8080/api/documents/document/create", // a revoir cote backend
          
          { headers: { Authorization: `Bearer ${token}` } },
          payload
        );
        // update local state with created doc if server returns it
        if (resp && resp.data) {
          console.log("Document created with ID:", resp.data.id);
          setDoc(resp.data);
          lastSavedRef.current = resp.data.content ?? payload.content;
          setStatus("Created");
        } else {
          setStatus("Created (no body)");
        }
      } catch (err) {
        console.error("Create failed", err);
        setStatus("Create failed");
        // continue navigation even on failure (optional)
      }
    }
    navigate(-1);
  };

  // --- RENDER ---
  
  // 🛑 THIS IS THE CRITICAL CHECK YOU WERE MISSING 🛑
  if (!doc) {
    return <div className="app-root"><main className="site-main"><h2>Loading Document...</h2></main></div>;
  }

  return (
    <div className="app-root">
      <main className="site-main">
        <div className="doc-header">
          <div className="doc-header-left">
            <button className="btn btn-outline" onClick={handleBack}>Back</button>
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