import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import "../styles/Connexion.css";

export default function Connexion() {
  const loc = useLocation();
  const navigate = useNavigate();
  const params = new URLSearchParams(loc.search);
  const mode = params.get("mode") === "signup" ? "signup" : "login";

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const submit = (e) => {
    e.preventDefault();
    // No name field in the form — derive a display name from the email local part
    const derivedName = email ? email.split("@")[0] : "User";
    const user = { name: derivedName, email };
    localStorage.setItem("cy_user", JSON.stringify(user));
    navigate("/", { replace: true });
    window.location.reload();
  };

  return (
    <div className="signin-container">
      <div className="signin-card">
        <h1 className="signin-title">{mode === "signup" ? "Get Started" : "Log in"}</h1>
        <p className="signin-sub">
          {mode === "signup"
            ? "Create an account to access documents and collaborate with your team."
            : "Access documents, upload and collaborate with your team."}
        </p>

        <form onSubmit={submit}>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              className="search-input"
              type="email"
              placeholder="you@company.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">
              {mode === "signup" ? "Choose a password" : "Password"}
            </label>
            <input
              id="password"
              className="search-input"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button className="signin-button" type="submit">
            {mode === "signup" ? "Create account" : "Log in"}
          </button>
        </form>
      </div>
    </div>
  );
}