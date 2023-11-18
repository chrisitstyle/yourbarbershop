import React, { createContext, useContext, useState } from "react";

// Utwórz kontekst
const AuthContext = createContext();

// Eksportuj dostawcę kontekstu
export const AuthProvider = ({ children }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  const login = () => {
    setIsLoggedIn(true);
  };

  const logout = () => {
    setIsLoggedIn(false);
  };

  return (
    <AuthContext.Provider value={{ isLoggedIn, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

// Eksportuj hook do korzystania z kontekstu
export const useAuth = () => {
  return useContext(AuthContext);
};
