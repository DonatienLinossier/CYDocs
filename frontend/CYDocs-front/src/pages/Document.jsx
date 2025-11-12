import { useState, useRef, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import "../styles/App.css";
import "../styles/Document.css";

/**
 * Simple document editor page.
 * - Supports creating a new document via route /document/new
 * - Saves new document to localStorage and navigates to its id
 * - Existing behavior preserved
 */

export default function Document() {
  const { id } = useParams();
  const isNew = id === "new";
  const docId = isNew ? null : parseInt(id, 10);
  const navigate = useNavigate();
  const editorRef = useRef(null);

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

  // track last saved HTML to avoid unnecessary writes
  const lastSavedRef = useRef(doc ? doc.content : "");

  useEffect(() => {
    // refresh doc if docs change (for existing docs)
    if (!isNew) setDoc(docs.find((d) => d.id === docId) || null);
  }, [docs, docId, isNew]);

  useEffect(() => {
    // focus editor on mount
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

  const format = (cmd, value = null) => {
    document.execCommand(cmd, false, value);
    editorRef.current && editorRef.current.focus();
    if (editorRef.current && editorRef.current.innerHTML !== lastSavedRef.current) {
      setStatus("Unsaved changes");
    }
  };

  // replace existing save implementation with this more robust version
  const save = useCallback(() => {
    const html = editorRef.current ? editorRef.current.innerHTML : doc.content;
    // no change -> skip
    if (!isNew && html === lastSavedRef.current) return;

    try {
      // read latest docs from localStorage to avoid stale closures
      const raw = localStorage.getItem("cy_docs");
      const storedDocs = raw ? JSON.parse(raw) : docs;

      if (isNew) {
        const maxId = storedDocs.reduce((m, d) => Math.max(m, d.id || 0), 0);
        const newId = maxId + 1;
        const created = { ...doc, id: newId, content: html };
        const updated = [...storedDocs, created];
        localStorage.setItem("cy_docs", JSON.stringify(updated));

        // update state and editor immediately with the saved version
        setDocs(updated);
        setDoc(created);
        if (editorRef.current) editorRef.current.innerHTML = created.content;
        lastSavedRef.current = created.content;

        setStatus("Saved");
        setTimeout(() => setStatus(""), 1500);
        navigate(`/document/${newId}`, { replace: true });
        return;
      }

      // existing doc: update stored docs
      const updated = storedDocs.map((d) => (d.id === doc.id ? { ...d, content: html, title: doc.title } : d));
      localStorage.setItem("cy_docs", JSON.stringify(updated));

      // update state and editor immediately with the saved version
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

  // autosave every minute if content changed
  useEffect(() => {
    const interval = setInterval(() => {
      try {
        if (editorRef.current && editorRef.current.innerHTML !== lastSavedRef.current) {
          save();
        }
      } catch (e) {
        // ignore
      }
    }, 60_000);

    return () => {
      clearInterval(interval);
      try {
        if (editorRef.current && editorRef.current.innerHTML !== lastSavedRef.current) {
          save();
        }
      } catch (e) {
        // ignore
      }
    };
  }, [save]);

  const handleInput = () => {
    if (editorRef.current && editorRef.current.innerHTML !== lastSavedRef.current) {
      setStatus("Unsaved changes");
    } else {
      setStatus("");
    }
  };

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

        {/* centered toolbar placed above the editor */}
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