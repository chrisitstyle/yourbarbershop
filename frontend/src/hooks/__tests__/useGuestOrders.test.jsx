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
    const mockGuestOrders = [{ id: 1, guestEmail: "guest@test.com" }];
    guestOrderService.getGuestOrders.mockResolvedValueOnce(mockGuestOrders);

    const { result } = renderHook(() => useGuestOrders(), {
      wrapper: QueryWrapper,
    });

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(result.current.guestOrders).toEqual(mockGuestOrders);
  });
});
