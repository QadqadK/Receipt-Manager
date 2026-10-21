import { createContext, useContext, useState, useCallback } from "react";
import apiClient from "../api/client";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [username, setUsername] = useState(() => localStorage.getItem("username"));

  const login = useCallback(async (username, password) => {
    const { data } = await apiClient.post("/auth/login", { username, password });
    localStorage.setItem("token", data.token);
    localStorage.setItem("username", data.username);
    setUsername(data.username);
  }, []);

  const register = useCallback(async (username, password) => {
    const { data } = await apiClient.post("/auth/register", { username, password });
    localStorage.setItem("token", data.token);
    localStorage.setItem("username", data.username);
    setUsername(data.username);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    setUsername(null);
  }, []);

  return (
    <AuthContext.Provider value={{ username, isAuthenticated: !!username, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return ctx;
}
