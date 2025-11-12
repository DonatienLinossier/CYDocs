import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import './index.css'
import App from './pages/App.jsx'
import Connexion from './pages/Connexion.jsx'
import Document from './pages/Document.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<App />} />
        <Route path="/connexion" element={<Connexion />} />
        <Route path="/document/:id" element={<Document />} />
      </Routes>
    </BrowserRouter>
  </StrictMode>,
)
