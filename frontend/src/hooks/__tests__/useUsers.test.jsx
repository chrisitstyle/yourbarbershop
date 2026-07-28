import { renderHook, waitFor } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import useUsers from "../useUsers";
import userService from "../../api/userService";
import { QueryWrapper } from "./wrapper";

// mock the API service
vi.mock("../../api/userService", () => ({
  default: {
    getUsers: vi.fn(),
  },
}));

describe("useUsers hook", () => {
  it("should fetch and return users successfully", async () => {
    // given
    const mockUsers = [{ id: 1, email: "test@test.com" }];
    userService.getUsers.mockResolvedValueOnce(mockUsers);

    // when
    const { result } = renderHook(() => useUsers(), { wrapper: QueryWrapper });

    // wait for query to finish loading
    // then
    await waitFor(() => expect(result.current.isLoading).toBe(false));
    expect(result.current.users).toEqual(mockUsers);
    expect(result.current.error).toBeNull();
  });

  it("should handle error when fetching fails", async () => {
    // given
    userService.getUsers.mockRejectedValueOnce(new Error("API Error"));

    // when
    const { result } = renderHook(() => useUsers(), { wrapper: QueryWrapper });

    // then
    await waitFor(() => expect(result.current.isLoading).toBe(false));
    expect(result.current.users).toEqual([]);
    expect(result.current.error).toBe("API Error");
  });
});
