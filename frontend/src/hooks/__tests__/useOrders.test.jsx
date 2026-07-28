import { renderHook, waitFor } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import useOrders from "../useOrders";
import * as orderService from "../../api/orderService";
import { QueryWrapper } from "./wrapper";

vi.mock("../../api/orderService", () => ({
  getOrders: vi.fn(),
}));

describe("useOrders hook", () => {
  it("should fetch and return orders successfully", async () => {
    // given
    const mockOrders = [{ id: 1, status: "PENDING" }];
    orderService.getOrders.mockResolvedValueOnce(mockOrders);

    // when
    const { result } = renderHook(() => useOrders(), { wrapper: QueryWrapper });

    // then
    await waitFor(() => expect(result.current.isLoading).toBe(false));
    expect(result.current.orders).toEqual(mockOrders);
  });
});
