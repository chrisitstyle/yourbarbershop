import React, { createContext, useContext, useEffect, useState } from "react";
import { clearAccessToken, setAccessToken } from "./api/httpClient.js";
import {
  loginUser,
  logoutUser,
  refreshSession,
  registerUser,
  verifyEmailLoginCode,
} from "./api/authService.js";

const AuthContext = createContext();

const buildUserFromAuthResponse = (authResponse) => ({
  id: authResponse.id,
  role: authResponse.role,
});

export const AuthProvider = ({ children }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState(null);
  const [authLoading, setAuthLoading] = useState(true);

  const applyAuthResponse = (authResponse) => {
    setAccessToken(authResponse.accessToken);
    setUser(buildUserFromAuthResponse(authResponse));
    setIsLoggedIn(true);
  };

  const clearAuthState = () => {
    clearAccessToken();
    setUser(null);
    setIsLoggedIn(false);
    localStorage.removeItem("token");
    localStorage.removeItem("user");
  };

  useEffect(() => {
    const bootstrapAuth = async () => {
      try {
        localStorage.removeItem("token");
        localStorage.removeItem("user");

        const authResponse = await refreshSession();
        applyAuthResponse(authResponse);
      } catch {
        clearAuthState();
      } finally {
        setAuthLoading(false);
      }
    };

    bootstrapAuth();
  }, []);

  const login = async (email, password) => {
    const authResponse = await loginUser(email, password);
    applyAuthResponse(authResponse);
    return authResponse;
  };

  const register = async (userData) => {
    const authResponse = await registerUser(userData);
    applyAuthResponse(authResponse);
    return authResponse;
  };

  const loginWithEmailCode = async (email, code) => {
    const authResponse = await verifyEmailLoginCode(email, code);
    applyAuthResponse(authResponse);
    return authResponse;
  };

  const refreshAuth = async () => {
    const authResponse = await refreshSession();
    applyAuthResponse(authResponse);
    return authResponse;
  };

  const logout = async () => {
    try {
      await logoutUser();
    } finally {
      clearAuthState();
    }
  };

  return (
    <AuthContext.Provider
      value={{
        isLoggedIn,
        user,
        authLoading,
        login,
        register,
        loginWithEmailCode,
        refreshAuth,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
