import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { useLocation } from "react-router-dom";
import "../styles/PasswordResetModal.css";
import axios from "axios";

export default function PasswordResetModal({ open, onClose, prefillEmail = "", onSend }) {
  const [step, setStep] = useState(1); // 1 = enter email, 2 = set new password
  const [email, setEmail] = useState(prefillEmail || "");
  const [error, setError] = useState("");
  const [sending, setSending] = useState(false);
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const inputRef = useRef(null);
  const navigate = useNavigate();
  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const token = queryParams.get("token");

  useEffect(() => {
    if (token) {
      setStep(2);    // Lien cliqué → passer à l’étape 2
    } else {
      setStep(1);    // Pas de token → rester sur étape 1
    }
  }, [token]);



  useEffect(() => {
  if (open) {
    if (token) {
      setStep(2); // 🔥 reset-password → étape 2
    } else {
      setStep(1); // 🔑 mode normal → étape 1
    }

    setEmail(prefillEmail || "");
    setPassword("");
    setConfirm("");
    setError("");
    setSending(false);

    setTimeout(() => inputRef.current?.focus(), 0);
  }
}, [open, token, prefillEmail]);


  useEffect(() => {
    const onKey = (e) => {
      if (e.key === "Escape" && open) onClose?.();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, onClose]);

  if (!open) return null;

  const isValidEmail = (v) => /\S+@\S+\.\S+/.test(v);

  // added live validation helpers
  const passwordsMatch = password === confirm;

  const submitEmail = async (e) => {
    e?.preventDefault();
    const v = email.trim();
    if (!isValidEmail(v)) {
      setError("Please enter a valid email address.");
      return;
    }
    setError("");
    setSending(true);
    console.log("token =", token);

    try {
      
      
     const response = await axios.post(
        "http://localhost:8888/user/api/users/forgot-password",
        null, // body vide
        { params: { email } }
      );


      console.log("Password reset request response:", response);
      window.alert(response.data);

      setSending(false);
      
      setTimeout(() => inputRef.current && inputRef.current.focus(), 60);
    } catch (err) {
      setSending(false);
      console.log("Error during password reset request:", err.response.data);
      setError(err.response.data);
    }
  };

  const submitNewPassword = async (e) => { 
    e?.preventDefault();
    const p = password;
    
    if (p !== confirm) {
      setError("Passwords do not match.");
      return;
    }
    setError("");
    setSending(true);

    try {
    const response = await axios.post(
      "http://localhost:8888/user/api/users/reset-password",
      null, // body vide
      { params: { token, newPassword: p } }
    );

    console.log("Reset password response:", response.data);

    // feedback utilisateur
    setSending(false);
    onClose?.();
    alert(response.data); // ou mieux : un toast / message React
    navigate("/", { replace: true });
    window.location.reload();
      
    } catch (err) {
      setSending(false);
      if (err.response.status === 400) {
        // token invalide ou expiré
        setError("Le lien de réinitialisation est invalide ou expiré. Veuillez revenir à l'étape précédente.");
      } else {
        setError("Une erreur est survenue. Veuillez réessayer utérieurement.");
      }
      console.log("Error during setting new password:", err);
    }
  };

  return (
    <div className="prm-backdrop" role="dialog" aria-modal="true" aria-label="Password reset">
      <div className="prm-card">
        <header className="prm-header">
          <h3>{step === 1 ? "Reset your password" : "Choose a new password"}</h3>
          <button className="prm-close" onClick={onClose} aria-label="Close">✕</button>
        </header>

        {step === 1 ? (
          <form className="prm-form" onSubmit={submitEmail}>
            <label className="prm-label">
              Email
              <input
                ref={inputRef}
                className="prm-input"
                type="email"
                value={email}
                onChange={(e) => { setEmail(e.target.value); setError(""); }}
                placeholder="you@company.com"
                required
              />
            </label>

            {error && <div className="prm-error" role="alert">{error}</div>}

            <div className="prm-actions">
              <button type="button" className="btn btn-outline" onClick={onClose} disabled={sending}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary" disabled={sending}>
                {sending ? "Sending..." : "Continue"}
              </button>
            </div>
          </form>
        ) : (
          <form className="prm-form" onSubmit={submitNewPassword}>
            <label className="prm-label">
              New password
              <input
                ref={inputRef}
                className="prm-input"
                type="password"
                value={password}
                onChange={(e) => { setPassword(e.target.value); setError(""); }}
                placeholder="New password"
                required
                aria-describedby="prm-password-help"
              />
            </label>

            <label className="prm-label">
              Confirm password
              <input
                className="prm-input"
                type="password"
                value={confirm}
                onChange={(e) => { setConfirm(e.target.value); setError(""); }}
                placeholder="Confirm password"
                required
                aria-invalid={!passwordsMatch}
              />
            </label>

            {/* live validation messages */}
            <div id="prm-password-help" style={{ fontSize: 13, minHeight: 18 }}>
              {password.length > 0 ? "Password length OK." : ""}
            </div>
            {!passwordsMatch && confirm.length > 0 && (
              <div className="prm-error" role="alert">Passwords do not match.</div>
            )}

            {error && <div className="prm-error" role="alert">{error}</div>}

            <div className="prm-actions">
              <button type="button" className="btn btn-outline" onClick={() => { setStep(1); setError(""); }} disabled={sending}>
                Back
              </button>
              <button
                type="submit"
                className="btn btn-primary"
                disabled={sending ||  !passwordsMatch}
                title={!passwordsMatch ? "Passwords must match" : "Save new password"}
              >
                {sending ? "Saving..." : "Save new password"}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}