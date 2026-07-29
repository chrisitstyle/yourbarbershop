import { describe, it, expect } from "vitest";
import { isTokenValid } from "../jwt";

describe("jwt utility - isTokenValid", () => {
  it("should return false for null, undefined, or empty token", () => {
    // given / when
    const resultNull = isTokenValid(null);
    const resultUndefined = isTokenValid(undefined);
    const resultEmpty = isTokenValid("");

    // then
    expect(resultNull).toBe(false);
    expect(resultUndefined).toBe(false);
    expect(resultEmpty).toBe(false);
  });

  it("should return false for a malformed token structure", () => {
    // given
    const malformedToken = "just.a.random.string";

    // when
    const result = isTokenValid(malformedToken);

    // then
    expect(result).toBe(false);
  });

  it("should return true for a valid, non-expired token", () => {
    // given
    // create a timestamp 1 hour in the future
    const futureTime = Math.floor(Date.now() / 1000) + 3600;
    const payload = btoa(JSON.stringify({ exp: futureTime }));
    const mockToken = `header.${payload}.signature`;

    // when
    const result = isTokenValid(mockToken);

    // then
    expect(result).toBe(true);
  });

  it("should return false for an expired token", () => {
    // given
    // create a timestamp 1 hour in the past
    const pastTime = Math.floor(Date.now() / 1000) - 3600;
    const payload = btoa(JSON.stringify({ exp: pastTime }));
    const mockToken = `header.${payload}.signature`;

    // when
    const result = isTokenValid(mockToken);

    // then
    expect(result).toBe(false);
  });

  it("should return false if token payload has no exp claim", () => {
    // given
    const payload = btoa(JSON.stringify({ role: "USER" })); // no 'exp'
    const mockToken = `header.${payload}.signature`;

    // when
    const result = isTokenValid(mockToken);

    // then
    expect(result).toBe(false);
  });
});
