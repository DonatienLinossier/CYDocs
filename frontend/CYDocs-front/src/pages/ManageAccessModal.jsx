import React, { useEffect, useState } from "react";
import axios from "axios";
import "../styles/Modal.css";

export default function ManageAccessModal({ open, onClose, docId }) {
  const [collaborators, setCollaborators] = useState([]);
  const token = localStorage.getItem("cy_token");

  useEffect(() => {
    if (open && docId) fetchCollaborators();
  }, [open, docId]);

  const fetchCollaborators = async () => {
    try {
      const res = await axios.get(`http://localhost:8888/documents/${docId}/collaborators`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setCollaborators(res.data);
    } catch (err) {
      console.error("Erreur chargement collaborateurs", err);
    }
  };

  // Nouvelle fonction pour modifier l'accès (Réutilise l'endpoint de partage)
  const updateAccess = async (email, newType) => {
    try {
      await axios.post("http://localhost:8888/documents/share", {
        documentId: docId,
        targetEmail: email,
        accessType: newType
      }, {
        headers: { Authorization: `Bearer ${token}` }
      });
      fetchCollaborators(); // Rafraîchir la liste
    } catch (err) {
      alert("Erreur lors de la modification des droits");
    }
  };

  const removeAccess = async (userId) => {
    if (!window.confirm("Retirer l'accès à cet utilisateur ?")) return;
    try {
      await axios.delete(`http://localhost:8888/documents/access/${docId}/${userId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setCollaborators(collaborators.filter(c => c.userId !== userId));
    } catch (err) {
      alert("Erreur lors de la suppression");
    }
  };

  if (!open) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <h2>Gérer les accès</h2>
        <ul className="collab-list">
          {collaborators.length === 0 && <p>Aucun collaborateur.</p>}
          {collaborators.map(c => (
            <li key={c.userId} className="collab-item" style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "15px", borderBottom: "1px solid #eee", paddingBottom: "10px" }}>
              <div style={{ flex: 1 }}>
                <strong>{c.name}</strong>
                <div style={{ fontSize: "12px", color: "#666" }}>{c.email}</div>
              </div>
              
              {/* Menu déroulant pour modifier le type d'accès */}
              <select 
                value={c.accessType} 
                onChange={(e) => updateAccess(c.email, e.target.value)}
                style={{ marginRight: "10px", padding: "4px" }}
              >
                <option value="read">Lecture</option>
                <option value="write">Écriture</option>
              </select>

              <button onClick={() => removeAccess(c.userId)} className="btn btn-danger" title="Supprimer">✖</button>
            </li>
          ))}
        </ul>
        <button onClick={onClose} className="btn btn-outline" style={{ width: "100%" }}>Fermer</button>
      </div>
    </div>
  );
}