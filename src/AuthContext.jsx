import React, { createContext, useContext, useState, useEffect } from "react";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState(null);

  useEffect(() => {
    const checkLocalStorage = () => {
      const storedToken = localStorage.getItem("token");

      if (storedToken) {
        const storedUser = JSON.parse(localStorage.getItem("user"));
        if (storedUser) {
          setIsLoggedIn(true);
          setUser(storedUser);
        }
      }
    };

    checkLocalStorage();
  }, []);

  const login = (userData) => {
    setIsLoggedIn(true);
    setUser(userData);

    localStorage.setItem("token", userData.token);
    localStorage.setItem("user", JSON.stringify(userData));
  };

  const logout = () => {
    setIsLoggedIn(false);
    setUser(null);

    localStorage.removeItem("token");
    localStorage.removeItem("user");
  };

  return (
    <AuthContext.Provider value={{ isLoggedIn, user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  return useContext(AuthContext);
};
