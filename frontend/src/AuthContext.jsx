import React, { createContext, useContext, useState, useEffect } from "react";
import { isTokenValid } from "./utils/jwt";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState(null);
  const [authLoading, setAuthLoading] = useState(true);
  // check localStorage for credentials on app init/refresh
  useEffect(() => {
    const checkLocalStorage = () => {
      const storedToken = localStorage.getItem("token");
      const storedUserRaw = localStorage.getItem("user");
      const storedUser = storedUserRaw ? JSON.parse(storedUserRaw) : null;

      if (storedToken && storedUser && isTokenValid(storedToken)) {
        setIsLoggedIn(true);
        setUser(storedUser);
      } else {
        setIsLoggedIn(false);
        setUser(null);
        localStorage.removeItem("token");
        localStorage.removeItem("user");
      }
      setAuthLoading(false);
    };

    checkLocalStorage();
  }, []);

  const login = (userData) => {
    if (userData && isTokenValid(userData.token)) {
      setIsLoggedIn(true);
      setUser(userData);
      localStorage.setItem("token", userData.token);
      localStorage.setItem("user", JSON.stringify(userData));
    } else {
      setIsLoggedIn(false);
      setUser(null);
      localStorage.removeItem("token");
      localStorage.removeItem("user");
    }
  };

  const logout = () => {
    setIsLoggedIn(false);
    setUser(null);
    localStorage.removeItem("token");
    localStorage.removeItem("user");
  };

  return (
    <AuthContext.Provider
      value={{ isLoggedIn, user, login, logout, authLoading }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
