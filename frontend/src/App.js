import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Header from './components/Header';
import Navigation from './components/Navigation';
import Dashboard from './pages/Dashboard';
import Assets from './pages/Assets';
import Sensors from './pages/Sensors';
import Predictions from './pages/Predictions';
import Login from './pages/Login';
import { AuthService } from './services/AuthService';

function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is already logged in
    const token = localStorage.getItem('authToken');
    if (token) {
      AuthService.validateToken(token)
        .then(response => {
          if (response.data.valid) {
            setUser({
              username: response.data.username,
              role: response.data.role,
              token: token
            });
          } else {
            localStorage.removeItem('authToken');
          }
        })
        .catch(error => {
          console.error('Token validation failed:', error);
          localStorage.removeItem('authToken');
        })
        .finally(() => {
          setLoading(false);
        });
    } else {
      setLoading(false);
    }
  }, []);

  const handleLogin = (userData) => {
    setUser(userData);
    localStorage.setItem('authToken', userData.token);
  };

  const handleLogout = () => {
    setUser(null);
    localStorage.removeItem('authToken');
  };

  if (loading) {
    return (
      <div className="loading">
        正在加载...
      </div>
    );
  }

  if (!user) {
    return <Login onLogin={handleLogin} />;
  }

  return (
    <Router>
      <div className="app">
        <Header user={user} onLogout={handleLogout} />
        <Navigation />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<Dashboard user={user} />} />
            <Route path="/assets" element={<Assets user={user} />} />
            <Route path="/sensors" element={<Sensors user={user} />} />
            <Route path="/predictions" element={<Predictions user={user} />} />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;