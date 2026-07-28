import { renderHook, waitFor } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import useUserDetails from "../useUserDetails";
import * as httpClient from "../../api/httpClient";
import { QueryWrapper } from "./wrapper";

vi.mock("../../api/httpClient", () => ({
  apiRequest: vi.fn(),
}));

describe("useUserDetails hook", () => {
  it("should not fetch data if userId is falsy", () => {
    // given / when
    const { result } = renderHook(() => useUserDetails(null), {
      wrapper: QueryWrapper,
    });

    // isLoading should be false because the query is disabled
    // then
    expect(result.current.isLoading).toBe(false);
    expect(result.current.userDetails).toBeNull();
    expect(httpClient.apiRequest).not.toHaveBeenCalled();
  });

  it("should fetch user details if userId is provided", async () => {
    // given
    const mockUser = { id: 123, email: "user@test.com" };
    httpClient.apiRequest.mockResolvedValueOnce({ data: mockUser });

    // when
    const { result } = renderHook(() => useUserDetails(123), {
      wrapper: QueryWrapper,
    });

    // then
    await waitFor(() => expect(result.current.isLoading).toBe(false));
    expect(httpClient.apiRequest).toHaveBeenCalled();
    expect(result.current.userDetails).toEqual(mockUser);
  });
});
