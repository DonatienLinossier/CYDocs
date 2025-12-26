import React, { useState } from "react";
import axios from "axios";
import "../styles/Modal.css";

export default function ShareModal({ open, onClose, docId, docTitle, isOwner }) {
  const [email, setEmail] = useState("");
  const [accessType, setAccessType] = useState("read"); // "read" par défaut
  const token = localStorage.getItem("cy_token");
  React.useEffect(() => {
    if (!isOwner) {
      setAccessType("read");
    }
  }, [isOwner, open]);
  const handleShare = async (e) => {
    e.preventDefault();
    try {
      await axios.post("http://localhost:8888/documents/documents/share", {
        documentId: docId,
        targetEmail: email,
        accessType: accessType // Envoie "read" ou "write"
      }, {
        headers: { Authorization: `Bearer ${token}` }
      });
      alert(`Document "${docTitle}" partagé avec succès en mode ${accessType}`);
      setEmail("");
      onClose();
    } catch (err) {
      alert("Erreur : Utilisateur introuvable ou vous n'avez pas les droits.");
    }
  };

  if (!open) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <h2>Partager "{docTitle}"</h2>
        <form onSubmit={handleShare}>
          <div className="form-group">
            <label>Email du destinataire</label>
            <input 
              type="email" 
              className="search-input"
              value={email} 
              onChange={(e) => setEmail(e.target.value)} 
              required 
              placeholder="collaborateur@test.com"
            />
          </div>
          
          <div className="form-group">
            <label>Type d'accès</label>
            {isOwner ? (
              /* Si propriétaire : choix libre */
              <select 
                className="search-input"
                value={accessType} 
                onChange={(e) => setAccessType(e.target.value)}
              >
                <option value="read">Lecture seule (👀)</option>
                <option value="write">Lecture & Écriture (✏️)</option>
              </select>
            ) : (
              /* Si non-propriétaire : bloqué en lecture seule */
              <div className="search-input" style={{ background: "#f0f0f0", cursor: "not-allowed" }}>
                Lecture seule uniquement (Restriction invité)
              </div>
            )}
          </div>

          <div style={{ display: "flex", gap: "10px", marginTop: "20px" }}>
            <button type="submit" className="signin-button" style={{ flex: 2 }}>Partager</button>
            <button type="button" onClick={onClose} className="btn-outline" style={{ flex: 1, marginTop: 0 }}>Annuler</button>
          </div>
        </form>
      </div>
    </div>
  );
}