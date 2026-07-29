import { describe, it, expect, vi } from "vitest";
import { getPaymentMethods } from "../paymentMethods";

describe("paymentMethods utility", () => {
  it("should return an array of payment methods mapped with translation function", () => {
    // given
    // mock the translation function (t) to return a predictable string
    const mockTranslationFn = vi.fn((key) => `translated_${key}`);

    // when
    const result = getPaymentMethods(mockTranslationFn);

    // then
    expect(result).toHaveLength(3);

    // verify the structure and correct mapping
    expect(result[0]).toEqual({
      value: "GOTOWKA",
      label: "translated_orders.paymentCash",
    });

    expect(result[1]).toEqual({
      value: "KARTA_NA_MIEJSCU",
      label: "translated_orders.paymentCardOnSite",
    });

    // verify that our mocked translation function was called exactly 3 times
    expect(mockTranslationFn).toHaveBeenCalledTimes(3);
  });
});
