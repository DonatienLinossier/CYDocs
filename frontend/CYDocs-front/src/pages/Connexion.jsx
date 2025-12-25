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

  useEffect(() => {
    if (token) {
      setShowResetModal(true); 
    }
  }, [token]);


  const [email, setEmail] = useState("");
  const [password, setPassword] = useState(""); 
  const [showResetModal, setShowResetModal] = useState(false);
  
  const [errorMessage, setErrorMessage] = useState("");

  const submit = async (e) => {
    e.preventDefault();
    // No name field in the form — derive a display name from the email local part
    const derivedName = email ? email.split("@")[0] : "User";

    try {
      if (mode === "signup") {
        // register
        
        
        // Version pour tester avec la gateway
        const response = await axios.post("http://localhost:8888/user/api/users/register", {
          
          
          email,
          password,
        });
        console.log("Registration response:", response.data);
        
      } 
        // login

        
        //Version pour tester avec la gateway
        const responceLogin = await axios.post("http://localhost:8888/user/api/users/login", {
          email,
          password,
        });

      // persist a simple session locally and redirect to home
      
      
      
      const user = { name: derivedName, email };
      localStorage.setItem("cy_user", JSON.stringify(user));
      localStorage.setItem("cy_token", responceLogin.data.token);
      navigate("/", { replace: true });
      window.location.reload();
      
    } catch (erreur) {
      if(mode === "signup"){
        console.error("Erreur sing in  = ", erreur.response.data);
        setErrorMessage( erreur.response.data);
      }else{
      console.error("Erreur recuperer = ", erreur.response.data.error);
      setErrorMessage( erreur.response.data.error);
      }
    }
  };

  const forgotPassword = () => {
    // open the password reset modal and prefill with current email (if any)
    setShowResetModal(true);
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

          {errorMessage && (
                <span className="signin-error-message">
                  {errorMessage}
                </span>
              )}

          <button className="signin-button" type="submit">
            {mode === "signup" ? "Create account" : "Log in"}
          </button>

          {mode === "login" && (
            <div style={{ marginTop: 12, display: "flex", alignItems: "center", gap: "10px" }}>
              <button
                type="button"
                className="signin-forgot btn btn-link"
                onClick={forgotPassword}
              >
                Forgot password?
              </button>

              
            </div>
          )}

        </form>
      </div>

      

      <PasswordResetModal
        open={showResetModal}
        onClose={() => setShowResetModal(false)}
        prefillEmail={email}
      />
    </div>
  );
}