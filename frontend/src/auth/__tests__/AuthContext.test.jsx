import { renderHook, act, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { useContext } from "react";
import { AuthProvider } from "../AuthContext";
import { AuthContext } from "../AuthContextValue";
import * as authService from "../../api/authService";
import * as httpClient from "../../api/httpClient";

// mock external services and api clients
vi.mock("../../api/authService", () => ({
  loginUser: vi.fn(),
  logoutUser: vi.fn(),
  refreshSession: vi.fn(),
  registerUser: vi.fn(),
  verifyEmailLoginCode: vi.fn(),
}));

vi.mock("../../api/httpClient", () => ({
  clearAccessToken: vi.fn(),
  setAccessToken: vi.fn(),
}));

describe("AuthContext", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // mock localStorage
    Storage.prototype.removeItem = vi.fn();
  });

  it("should initialize with user data when session refresh succeeds on mount", async () => {
    // given
    const mockAuthResponse = {
      id: 1,
      role: "USER",
      accessToken: "mock-jwt-token",
    };
    authService.refreshSession.mockResolvedValueOnce(mockAuthResponse);

    // when
    const { result } = renderHook(() => useContext(AuthContext), {
      wrapper: AuthProvider,
    });

    // then
    // wait for the bootstrapAuth to finish (authLoading becomes false)
    await waitFor(() => expect(result.current.authLoading).toBe(false));

    expect(result.current.isLoggedIn).toBe(true);
    expect(result.current.user).toEqual({ id: 1, role: "USER" });
    expect(httpClient.setAccessToken).toHaveBeenCalledWith("mock-jwt-token");
  });

  it("should clear state and finish loading when session refresh fails on mount", async () => {
    // given
    // simulate a 401 unauthorized response (e.g. no valid refresh cookie)
    authService.refreshSession.mockRejectedValueOnce({ status: 401 });

    // when
    const { result } = renderHook(() => useContext(AuthContext), {
      wrapper: AuthProvider,
    });

    // then
    await waitFor(() => expect(result.current.authLoading).toBe(false));

    expect(result.current.isLoggedIn).toBe(false);
    expect(result.current.user).toBeNull();
    expect(httpClient.clearAccessToken).toHaveBeenCalled();
  });

  it("should set user and token on successful login", async () => {
    // given
    // bootstrap fails initially so we start logged out
    authService.refreshSession.mockRejectedValueOnce({ status: 401 });

    const mockLoginResponse = {
      id: 2,
      role: "ADMIN",
      accessToken: "new-login-token",
    };
    authService.loginUser.mockResolvedValueOnce(mockLoginResponse);

    const { result } = renderHook(() => useContext(AuthContext), {
      wrapper: AuthProvider,
    });

    // wait for initial load to finish
    await waitFor(() => expect(result.current.authLoading).toBe(false));

    // when
    await act(async () => {
      await result.current.login("admin@test.com", "password123");
    });

    // then
    expect(authService.loginUser).toHaveBeenCalledWith(
      "admin@test.com",
      "password123",
    );
    expect(httpClient.setAccessToken).toHaveBeenCalledWith("new-login-token");
    expect(result.current.isLoggedIn).toBe(true);
    expect(result.current.user).toEqual({ id: 2, role: "ADMIN" });
  });

  it("should clear user state and tokens on logout", async () => {
    // given
    // start with a successful logged-in state
    const mockAuthResponse = {
      id: 3,
      role: "USER",
      accessToken: "existing-token",
    };
    authService.refreshSession.mockResolvedValueOnce(mockAuthResponse);
    authService.logoutUser.mockResolvedValueOnce();

    const { result } = renderHook(() => useContext(AuthContext), {
      wrapper: AuthProvider,
    });

    await waitFor(() => expect(result.current.authLoading).toBe(false));
    expect(result.current.isLoggedIn).toBe(true); // ensure we are logged in

    // when
    await act(async () => {
      await result.current.logout();
    });

    // then
    expect(authService.logoutUser).toHaveBeenCalled();
    expect(httpClient.clearAccessToken).toHaveBeenCalled();
    expect(result.current.isLoggedIn).toBe(false);
    expect(result.current.user).toBeNull();
    expect(localStorage.removeItem).toHaveBeenCalledWith("token");
    expect(localStorage.removeItem).toHaveBeenCalledWith("user");
  });
});
