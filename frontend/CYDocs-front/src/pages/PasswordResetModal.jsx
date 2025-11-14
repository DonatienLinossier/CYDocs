import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/PasswordResetModal.css";

export default function PasswordResetModal({ open, onClose, prefillEmail = "", onSend }) {
  const [step, setStep] = useState(1); // 1 = enter email, 2 = set new password
  const [email, setEmail] = useState(prefillEmail || "");
  const [error, setError] = useState("");
  const [sending, setSending] = useState(false);
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const inputRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (open) {
      setStep(1);
      setEmail(prefillEmail || "");
      setPassword("");
      setConfirm("");
      setError("");
      setSending(false);
      setTimeout(() => inputRef.current && inputRef.current.focus(), 0);
    }
  }, [open, prefillEmail]);

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
  const passwordTooShort = password.length > 0 && password.length < 6;

  const submitEmail = async (e) => {
    e?.preventDefault();
    const v = email.trim();
    if (!isValidEmail(v)) {
      setError("Please enter a valid email address.");
      return;
    }
    setError("");
    setSending(true);

    try {
      // store a reset request (simulated)
      const token = `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 9)}`;
      const resets = JSON.parse(localStorage.getItem("cy_password_resets") || "[]");
      resets.push({ email: v, token, date: new Date().toISOString() });
      localStorage.setItem("cy_password_resets", JSON.stringify(resets));

      // inform user a mail was sent then proceed to password entry step
      window.alert(`A reset link has been sent to ${v}. Please check your inbox.`);

      setSending(false);
      setStep(2);
      setTimeout(() => inputRef.current && inputRef.current.focus(), 60);
    } catch (err) {
      setSending(false);
      setError("Failed to start reset. Try again.");
    }
  };

  const submitNewPassword = async (e) => {
    e?.preventDefault();
    const p = password;
    if (p.length < 6) {
      setError("Password must be at least 6 characters.");
      return;
    }
    if (p !== confirm) {
      setError("Passwords do not match.");
      return;
    }
    setError("");
    setSending(true);

    try {
      // update or create a user record in localStorage (simulation)
      // we keep single 'cy_user' (current user) for simplicity; also store in 'cy_users' list
      const usersRaw = localStorage.getItem("cy_users");
      const users = usersRaw ? JSON.parse(usersRaw) : [];
      const existingIndex = users.findIndex((u) => u.email === email.trim());
      const derivedName = email ? email.split("@")[0] : "User";
      const userRecord = { name: derivedName, email: email.trim(), password: p };
      if (existingIndex >= 0) users[existingIndex] = { ...users[existingIndex], ...userRecord };
      else users.push(userRecord);
      localStorage.setItem("cy_users", JSON.stringify(users));

      // also set cy_user (current session) to the updated user
      localStorage.setItem("cy_user", JSON.stringify({ name: derivedName, email: email.trim() }));

      onSend?.({ email: email.trim() });
      setSending(false);
      onClose?.();
      // redirect to home and reload so App sees updated state
      navigate("/", { replace: true });
      window.location.reload();
    } catch (err) {
      setSending(false);
      setError("Failed to set new password. Try again.");
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
            <div id="prm-password-help" style={{ fontSize: 13, color: passwordTooShort ? "#b00020" : "#64748b", minHeight: 18 }}>
              {passwordTooShort ? "Password is too short (min 6 characters)." : (password.length > 0 ? "Password length OK." : "")}
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
                disabled={sending || passwordTooShort || !passwordsMatch}
                title={!passwordsMatch ? "Passwords must match" : passwordTooShort ? "Password too short" : "Save new password"}
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