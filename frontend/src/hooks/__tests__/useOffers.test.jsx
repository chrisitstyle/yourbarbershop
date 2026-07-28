import { renderHook, waitFor } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import useOffers from "../useOffers";
import * as offerService from "../../api/offerService";
import { QueryWrapper } from "./wrapper";

vi.mock("../../api/offerService", () => ({
  getOffers: vi.fn(),
}));

describe("useOffers hook", () => {
  it("should fetch and return offers successfully", async () => {
    // given
    const mockOffers = [{ id: 1, title: "Haircut" }];
    offerService.getOffers.mockResolvedValueOnce(mockOffers);

    // when
    const { result } = renderHook(() => useOffers(), { wrapper: QueryWrapper });

    // then
    await waitFor(() => expect(result.current.isLoading).toBe(false));
    expect(result.current.offers).toEqual(mockOffers);
    expect(result.current.error).toBeNull();
  });
});
