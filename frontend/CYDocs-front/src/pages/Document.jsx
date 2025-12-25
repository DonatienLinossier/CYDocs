import { useState, useRef, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import axios from "axios";
import "../styles/App.css";
import "../styles/Document.css";

const WEBSOCKET_URL = "http://localhost:8888/documents/documents/ws"; 
const API_URL = "http://localhost:8888/documents/documents/get";

export default function Document() {
  const { id } = useParams();
  const isNew = id === "new";
  const docId = isNew ? null : parseInt(id, 10);
  const navigate = useNavigate();
  const editorRef = useRef(null);
  const wsClientRef = useRef(null); // FIX: Correction du crash "not iterable"

  const [sessionId] = useState(() => "user-" + Math.random().toString(36).substr(2, 9));
  const [doc, setDoc] = useState(null); 
  const [status, setStatus] = useState("Loading...");
  const [canEdit, setCanEdit] = useState(false);

  const token = localStorage.getItem("cy_token");
  const currentUserId = localStorage.getItem("cy_user_id");

  // --- 1. FETCH DOCUMENT & PERMISSIONS ---
  useEffect(() => {
    if (isNew) {
      setDoc({ title: "Untitled Document", author: "You", content: "" });
      setCanEdit(true);
      setStatus("New (Unsaved)");
      return;
    }

    setStatus("Loading document...");
    
    axios.get(`${API_URL}/${docId}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    .then((res) => {
      const data = res.data;
      setDoc(data);
      
      const isOwner = data.ownerId?.toString() === currentUserId?.toString();
      setCanEdit(isOwner || data.currentPermission === "write"); 

      if (editorRef.current) {
        editorRef.current.innerHTML = data.content;
      }
      setStatus(isOwner || data.currentPermission === "write" ? "Ready" : "Read Only");
    })
    .catch((err) => {
      console.error("Fetch error:", err);
      alert("Document not found or Access Denied");
      navigate("/");
    });
  }, [docId, isNew, navigate, token, currentUserId]);

  // --- 2. WEBSOCKET ---
  useEffect(() => {
    if (isNew || !doc) return; 

    const client = new Client({
      webSocketFactory: () => new SockJS(WEBSOCKET_URL),
      reconnectDelay: 5000,
      onConnect: () => {
        setStatus(canEdit ? "Connected (Live)" : "Connected (View Only)");
        client.subscribe(`/topic/doc/${doc.id}`, (msg) => {
          if (msg.body) {
            const payload = JSON.parse(msg.body);
            if (payload.sender !== sessionId && editorRef.current) {
              editorRef.current.innerHTML = payload.content;
            }
          }
        });
      },
    });

    client.activate();
    wsClientRef.current = client;
    return () => client.deactivate();
  }, [docId, isNew, sessionId, doc?.id, canEdit]);

  // --- ACTIONS ---
  // Ajout de la fonction format manquante
  const format = (cmd, value = null) => {
    if (!canEdit) return;
    document.execCommand(cmd, false, value);
    editorRef.current && editorRef.current.focus();
    handleInput();
  };

  const handleInput = () => {
    if (!canEdit || !editorRef.current) return;

    const content = editorRef.current.innerHTML;
    if (wsClientRef.current?.connected) {
      wsClientRef.current.publish({
        destination: `/app/doc/${docId}`,
        body: JSON.stringify({ sender: sessionId, content: content })
      });
    }
  };

  const download = () => {
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
      setTimeout(() => setStatus(canEdit ? "Ready" : "Read Only"), 1500);
    } catch { setStatus("Copy failed"); }
  };

  const handleBack = async () => {
    if (!doc) return navigate("/");
    if (!canEdit) return navigate("/");

    try {
      setStatus("Saving...");
      const payload = {
        title: doc.title,
        content: editorRef.current?.innerHTML || ""
      };

      const url = isNew ? "http://localhost:8888/documents/documents/create" : `http://localhost:8888/documents/documents/update/${docId}`;
      const method = isNew ? "post" : "put";

      await axios[method](url, payload, {
        headers: { Authorization: `Bearer ${token}` }
      });

      navigate("/");
    } catch (err) { setStatus("Save failed"); }
  };

  if (!doc) return <div className="app-root"><h2>Loading...</h2></div>;

  return (
    <div className="app-root">
      <main className="site-main">
        <div className="doc-header">
          <div className="doc-header-left">
            <button className="btn btn-outline" onClick={handleBack}>Back</button>
            {isNew || (canEdit && doc.ownerId?.toString() === currentUserId?.toString()) ? (
              <input
                className="doc-title-input"
                value={doc.title}
                onChange={(e) => setDoc((s) => ({ ...s, title: e.target.value }))}
              />
            ) : (
              <strong className="doc-title">{doc.title}</strong>
            )}
            <span className="doc-meta"> by {doc.author}</span>
          </div>
          <div className={`doc-status ${!canEdit ? "status-readonly" : ""}`}>{status}</div>
        </div>

        <div className={`doc-toolbar-center ${!canEdit ? "toolbar-disabled" : ""}`}>
          <div className="doc-toolbar">
            <button type="button" className="btn" disabled={!canEdit} onClick={() => format("undo")}>↶</button>
            <button type="button" className="btn" disabled={!canEdit} onClick={() => format("redo")}>↷</button>
            <button type="button" className="btn" disabled={!canEdit} onClick={() => format("bold")}><b>B</b></button>
            <button type="button" className="btn" disabled={!canEdit} onClick={() => format("italic")}><i>I</i></button>
            <button type="button" className="btn" disabled={!canEdit} onClick={() => format("underline")}><u>U</u></button>
            <button type="button" className="btn" disabled={!canEdit} onClick={() => format("strikeThrough")}>S</button>
            <button type="button" className="btn" disabled={!canEdit} onClick={() => format("formatBlock", "<h2>")}>H2</button>
            <button type="button" className="btn" disabled={!canEdit} onClick={() => format("formatBlock", "<pre>")}>{"</>"}</button>
            <button type="button" className="btn" disabled={!canEdit} onClick={() => format("insertUnorderedList")}>•</button>
            <button type="button" className="btn" disabled={!canEdit} onClick={() => format("insertOrderedList")}>1.</button>
            <button type="button" className="btn btn-secondary" onClick={download}>Download</button>
            <button type="button" className="btn btn-outline" onClick={copyLink}>Share</button>
          </div>
        </div>

        <div
          ref={editorRef}
          contentEditable={canEdit} 
          className={`doc-editor ${!canEdit ? "editor-readonly" : ""}`}
          onInput={handleInput}
          dangerouslySetInnerHTML={{ __html: doc.content }}
        />
      </main>
    </div>
  );
}