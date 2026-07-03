import { createContext, useState, useEffect } from 'react';
import { login as loginService, register as registerService } from '../services/authService';
import api from '../services/api';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');
    
    if (storedToken && storedUser) {
      setToken(storedToken);
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const data = await loginService(email, password);
    if (data.success) {
      const { token: jwt, ...userInfo } = data.data;
      setToken(jwt);
      setUser(userInfo);
      localStorage.setItem('token', jwt);
      localStorage.setItem('user', JSON.stringify(userInfo));
    }
    return data;
  };

  const register = async (userData) => {
    const data = await registerService(userData);
    if (data.success) {
      const { token: jwt, ...userInfo } = data.data;
      setToken(jwt);
      setUser(userInfo);
      localStorage.setItem('token', jwt);
      localStorage.setItem('user', JSON.stringify(userInfo));
    }
    return data;
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };

  if (loading) {
    return <div className="flex h-screen items-center justify-center">Loading...</div>;
  }

  return (
    <AuthContext.Provider value={{ user, token, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
