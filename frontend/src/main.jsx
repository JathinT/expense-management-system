import React from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.jsx'
import './styles.css'

// Apply saved theme before React mounts to avoid a flash of the wrong theme.
try {
  const saved = localStorage.getItem('theme')
  if (saved === 'dark' || saved === 'light') {
    document.documentElement.setAttribute('data-theme', saved)
  } else {
    // default to light
    document.documentElement.setAttribute('data-theme', 'light')
  }
} catch (e) {
  // ignore
}

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
