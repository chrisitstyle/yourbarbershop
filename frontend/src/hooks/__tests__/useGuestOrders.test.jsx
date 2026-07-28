import { renderHook, waitFor } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import useGuestOrders from "../useGuestOrders";
import guestOrderService from "../../api/guestOrderService";
import { QueryWrapper } from "./wrapper";

vi.mock("../../api/guestOrderService", () => ({
  default: {
    getGuestOrders: vi.fn(),
  },
}));

describe("useGuestOrders hook", () => {
  it("should fetch and return guest orders successfully", async () => {
    // given
    const mockGuestOrders = [{ id: 1, guestEmail: "guest@test.com" }];
    guestOrderService.getGuestOrders.mockResolvedValueOnce(mockGuestOrders);

    // when
    const { result } = renderHook(() => useGuestOrders(), {
      wrapper: QueryWrapper,
    });

    // then
    await waitFor(() => expect(result.current.isLoading).toBe(false));
    expect(result.current.guestOrders).toEqual(mockGuestOrders);
  });
});
