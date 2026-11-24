import { createContext, useContext, useState } from 'react';
import api from '../api/client.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('devtrack_user');
    return saved ? JSON.parse(saved) : null;
  });

  const save = (data) => {
    localStorage.setItem('devtrack_token', data.token);
    const u = { name: data.name, email: data.email };
    localStorage.setItem('devtrack_user', JSON.stringify(u));
    setUser(u);
  };

  const login = async (email, password) => save((await api.post('/auth/login', { email, password })).data);
  const register = async (payload) => save((await api.post('/auth/register', payload)).data);
  const logout = () => {
    localStorage.removeItem('devtrack_token');
    localStorage.removeItem('devtrack_user');
    setUser(null);
  };

  return <AuthContext.Provider value={{ user, login, register, logout }}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
