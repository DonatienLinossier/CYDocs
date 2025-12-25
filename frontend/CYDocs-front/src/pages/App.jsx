import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import axios from "axios";
import "../styles/App.css";

function App() {
  const [query, setQuery] = useState("");
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem("cy_user");
    return stored ? JSON.parse(stored) : null;
  });
  const [docs, setDocs] = useState([]);

  useEffect(() => {
    // 1. Récupération sécurisée des identifiants de session
    const token = localStorage.getItem("cy_token");
    
    // On ne lance la requête que si l'utilisateur est connecté et possède un token
    if (!user || !token) return;

    // 2. Appel à l'endpoint sécurisé par Token (plus besoin d'ID dans l'URL)
    axios
      .get("http://localhost:8888/documents/my-documents", {
        headers: { Authorization: `Bearer ${token}` } //
      })
      .then((res) => {
        setDocs(res.data);
      })
      .catch((err) => {
        console.error("Erreur lors de la récupération des documents :", err);
        // Si le token est expiré (401), on peut forcer la déconnexion ici
        if (err.response?.status === 401) signOut();
      });
  }, [user]);

  const filtered = docs.filter(
    (d) => 
      d && // Vérifie que le document 'd' n'est pas null
      (
        (d.title?.toLowerCase().includes(query.toLowerCase())) ||
        (d.author?.toLowerCase().includes(query.toLowerCase()))
      )
  );

  const signOut = async () => {
    try {
      const token = localStorage.getItem("cy_token");
      if (token) {
        await axios.post(`http://localhost:8888/user/api/users/logout?token=${token}`);
      }
    } catch (err) {
      console.error("Erreur lors de la déconnexion :", err);
    }
    localStorage.removeItem("cy_user");
    localStorage.removeItem("cy_token");
    localStorage.removeItem("cy_user_id");
    setUser(null);
    window.location.reload();
  };

  const shareDoc = (doc) => {
    const raw = window.prompt(`Share "${doc.title}"\nEmail :`);
    if (!raw) return;
    const emails = raw.split(/[,;|\n]+/).map(s => s.trim()).filter(Boolean);
    const shares = JSON.parse(localStorage.getItem("cy_shares") || "[]");
    shares.push({
      docId: doc.id,
      title: doc.title,
      emails,
      date: new Date().toISOString(),
    });
    localStorage.setItem("cy_shares", JSON.stringify(shares));
    window.alert(`Invitation sent.`);
  };

  return (
    <div className="app-root">
      <header className="site-header">
        <div className="brand">
          <div className="brand-logo">CY</div>
          <div className="brand-text">
            <div className="site-title">CYDocs</div>
            <div className="site-sub">Collaborative document sharing</div>
          </div>
        </div>
        <nav className="site-nav">
          {user ? (
            <>
              <div className="site-user">Hello, {user.name}</div>
              <button className="btn btn-outline" onClick={signOut}>
                Sign out
              </button>
            </>
          ) : (
            <>
              <Link to="/connexion?mode=login" className="btn btn-outline">
                Log in
              </Link>
              <Link to="/connexion?mode=signup" className="btn btn-primary">
                Get Started
              </Link>
            </>
          )}
        </nav>
      </header>

      <main className="site-main">
        <section className="hero">
          <div className="hero-content">
            <h1 className="hero-title">Work on the same document together</h1>
            <div className="hero-actions">
              <input
                className="search-input"
                aria-label="Search documents"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder={user ? "Search documents, authors..." : "Sign in to search"}
                disabled={!user}
              />
            </div>
            <div className="hero-quick">
              {user ? (
                <Link to="/document/new" className="btn btn-outline">
                  Create a document
                </Link>
              ) : (
                <button className="btn btn-outline" disabled>
                  Create a document
                </button>
              )}
            </div>
          </div>
          <aside className="sidebar">
            <div className="sidebar-header">Welcome back</div>
            <div className="sidebar-title">Recent activity</div>
            <ul className="activity-list">
              {user ? (
                <>
                  <li className="activity-item">Alice updated "Project Plan"</li>
                  <li className="activity-item">Bob commented on "Guidelines"</li>
                </>
              ) : <li className="activity-item">Please log in</li>}
            </ul>
          </aside>
        </section>

        <section className="suggested">
          <h2>Your documents</h2>
          {!user ? (
            <div className="auth-notice">
              <p>Please sign in to view documents and access content.</p>
            </div>
          ) : (
            <div className="docs-grid">
              {filtered.length > 0 ? (
                filtered.map((d) => (
                  <article key={d.id} className="doc-card">
                    <div className="doc-title">{d.title}</div>
                    <div className="doc-author">By {d.author}</div>
                    <div className="doc-actions">
                      <Link className="btn btn-outline" to={`/document/${d.id}`}>
                        Open
                      </Link>
                      <button className="btn btn-secondary" onClick={() => shareDoc(d)}>
                        Share
                      </button>
                    </div>
                  </article>
                ))
              ) : (
                <p>No documents found.</p>
              )}
            </div>
          )}
        </section>

        <footer className="site-footer">
          © {new Date().getFullYear()} CYDocs — Document sharing for teams
        </footer>
      </main>
    </div>
  );
}

export default App;