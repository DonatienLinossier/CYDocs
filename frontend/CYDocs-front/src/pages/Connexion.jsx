import { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import "../styles/Connexion.css";
import PasswordResetModal from "./PasswordResetModal";
import axios from "axios";

export default function Connexion() {
  const loc = useLocation();
  const navigate = useNavigate();
  const params = new URLSearchParams(loc.search);
  const mode = params.get("mode") === "signup" ? "signup" : "login";
  const token = params.get("token");

  // --- NOUVEAUX ÉTATS ---
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState(""); 
  const [showResetModal, setShowResetModal] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const submit = async (e) => {
    e.preventDefault();

    try {
      if (mode === "signup") {
        // 1. Inscription : on envoie le prénom et le nom au UserService
        await axios.post("http://localhost:8888/user/api/users/register", {
          firstName,
          lastName,
          email,
          password,
        });
      } 

      // 2. Connexion (Login)
      const responceLogin = await axios.post("http://localhost:8888/user/api/users/login", {
        email,
        password,
      });

      // 3. Persistance de la session
      // On utilise le prénom et nom récupérés pour l'affichage
      const user = { 
        name: mode === "signup" ? `${firstName} ${lastName}` : (responceLogin.data.firstName + " " + responceLogin.data.lastName), 
        email, 
        id: responceLogin.data.id 
      };

      localStorage.setItem("cy_user", JSON.stringify(user));
      localStorage.setItem("cy_token", responceLogin.data.token);
      
      navigate("/", { replace: true });
      window.location.reload();
      
    } catch (erreur) {
      setErrorMessage(erreur.response?.data?.error || "Une erreur est survenue");
    }
  };

  return (
    <div className="signin-container">
      <div className="signin-card">
        <h1 className="signin-title">{mode === "signup" ? "Get Started" : "Log in"}</h1>
        
        <form onSubmit={submit}>
          {/* Champs Prénom et Nom uniquement en mode SIGNUP */}
{mode === "signup" && (
  <div className="signup-name-row">
    <div className="form-group">
      <label htmlFor="firstname">Prénom</label>
      <input
        id="firstname"
        className="search-input"
        type="text"
        placeholder="Jean"
        value={firstName}
        onChange={(e) => setFirstName(e.target.value)}
        required
      />
    </div>
    <div className="form-group">
      <label htmlFor="lastname">Nom</label>
      <input
        id="lastname"
        className="search-input"
        type="text"
        placeholder="Dupont"
        value={lastName}
        onChange={(e) => setLastName(e.target.value)}
        required
      />
    </div>
  </div>
)}

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
            <label htmlFor="password">Mot de passe</label>
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

          {errorMessage && <span className="signin-error-message">{errorMessage}</span>}

          <button className="signin-button" type="submit">
            {mode === "signup" ? "Create account" : "Log in"}
          </button>
        </form>
      </div>
    </div>
  );
}