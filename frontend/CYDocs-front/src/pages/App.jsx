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
  useEffect(() => { // a revoir le backend ne demande pas le token FIO
    if (!user) return;

    axios
      .get(`http://localhost:8080/document/documents/user/2`)// a changer FIO ${user.id}
      .then((res) => {
        setDocs(res.data);
      })
      .catch((err) => {
        console.error("Erreur lors de la récupération des documents :", err);
      });
  }, [user]);

  const filtered = docs.filter(
    (d) =>
      d.title.toLowerCase().includes(query.toLowerCase()) ||
      d.author.toLowerCase().includes(query.toLowerCase())
  );

const signOut = async () => {
  try {
    const token = localStorage.getItem("cy_token");

    if (token) {
      
      //Version pour tester avec la gateway
      await axios.post(`http://127.0.0.1:8080/user/api/users/logout?token=${token}`);
    }
  } catch (err) {
    console.error("Erreur lors de la déconnexion :", err);
  }

  // Quoi qu'il arrive, on nettoie le front
  localStorage.removeItem("cy_user");
  localStorage.removeItem("cy_token");
  setUser(null);
  window.location.reload();
};


  // add this helper inside the App component
  const shareDoc = (doc) => {
    const raw = window.prompt(`Share "${doc.title}"\nEmail :`);
    if (!raw) return;
    const emails = raw.split(/[,;|\n]+/).map(s => s.trim()).filter(Boolean);
    const invalid = emails.filter(e => !/\S+@\S+\.\S+/.test(e));
    if (invalid.length) {
      window.alert("Invalid email(s): " + invalid.join(", "));
      return;
    }
    const shares = JSON.parse(localStorage.getItem("cy_shares") || "[]");
    shares.push({
      docId: doc.id,
      title: doc.title,
      emails,
      date: new Date().toISOString(),
      link: `${window.location.origin}/document/${doc.id}`
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
                  <li className="activity-item">Alice updated "Project Plan - Q4"</li>
                  <li className="activity-item">Bob commented on "Design Guidelines"</li>
                  <li className="activity-item">Carol uploaded "User Onboarding"</li>
                </>
              ) : null}
            </ul>
          </aside>
        </section>

        <section className="suggested">
          <h2>Suggested documents</h2>

          {!user ? (
            <div className="auth-notice">
              <p>Please sign in to view documents and access content.</p>
            </div>
          ) : (
            <div className="docs-grid">
              {filtered.map((d) => (
                <article key={d.id} className="doc-card">
                  <div className="doc-title">{d.title}</div>
                  <div className="doc-excerpt">{d.excerpt}</div>
                  <div className="doc-author">By {d.author}</div>
                  <div className="doc-actions">
                    <Link className="btn btn-outline" to={`/document/${d.id}`}>
                      Open
                    </Link>
                    <button
                      className="btn btn-secondary"
                      onClick={() => shareDoc(d)}
                    >
                      Share
                    </button>
                  </div>
                </article>
              ))}
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
