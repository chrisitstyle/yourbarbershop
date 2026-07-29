import { describe, it, expect, vi } from "vitest";
import { getNestedValue } from "../tableHelpers";
import { formatShortDate } from "../../api/dataParser";

// given (global mocks for external dependencies)
vi.mock("../../api/dataParser", () => ({
  formatShortDate: vi.fn(),
}));

vi.mock("../../api/i18n", () => ({
  default: {
    language: "pl", // mock polish language
  },
}));

describe("tableHelpers utility - getNestedValue", () => {
  it("should return a simple root field value", () => {
    // given
    const obj = { status: "PENDING" };

    // when
    const result = getNestedValue(obj, "status");

    // then
    expect(result).toBe("PENDING");
  });

  it("should return a deeply nested field value", () => {
    // given
    const obj = { user: { profile: { firstname: "John" } } };

    // when
    const result = getNestedValue(obj, "user.profile.firstname");

    // then
    expect(result).toBe("John");
  });

  it("should return 'brak' if the nested field or object does not exist", () => {
    // given
    const obj = { user: null }; // missing profile and firstname

    // when
    const result = getNestedValue(obj, "user.profile.firstname");

    // then
    expect(result).toBe("brak");
  });

  it("should extract offer cost correctly or return 'brak' if empty", () => {
    // given
    const validOfferObj = { offer: { cost: 150 } };
    const emptyOfferObj = {};

    // when
    const validResult = getNestedValue(validOfferObj, "offer.cost");
    const emptyResult = getNestedValue(emptyOfferObj, "offer.cost");

    // then
    expect(validResult).toBe(150);
    expect(emptyResult).toBe("brak");
  });

  it("should properly format date fields via external parser", () => {
    // given
    const obj = { orderDate: "2026-07-29T10:00:00Z" };
    // tell our mocked formatter what to return
    formatShortDate.mockReturnValue("29.07.2026");

    // when
    const result = getNestedValue(obj, "orderDate");

    // then
    expect(formatShortDate).toHaveBeenCalledWith(
      "2026-07-29T10:00:00Z",
      "pl-PL",
    );
    expect(result).toBe("29.07.2026");
  });
});
