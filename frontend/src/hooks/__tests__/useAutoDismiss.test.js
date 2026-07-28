import { renderHook, act } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import useAutoDismiss from "../useAutoDismiss";

describe("useAutoDismiss hook", () => {
  beforeEach(() => {
    // enable fake timers before each test
    vi.useFakeTimers();
  });

  afterEach(() => {
    // restore real timers after each test
    vi.useRealTimers();
  });

  it("should initialize with the provided initial value", () => {
    const { result } = renderHook(() => useAutoDismiss(null));
    expect(result.current[0]).toBeNull();
  });

  it("should dismiss state after the specified timeout when it has content", () => {
    const { result } = renderHook(() => useAutoDismiss([], 3000));

    // set some content
    act(() => {
      result.current[1](["Error 1"]);
    });

    expect(result.current[0]).toEqual(["Error 1"]);

    // advance time by 2999ms (state should still be there)
    act(() => {
      vi.advanceTimersByTime(2999);
    });
    expect(result.current[0]).toEqual(["Error 1"]);

    // advance time to reach the timeout
    act(() => {
      vi.advanceTimersByTime(1);
    });

    // state should be reset to initial value
    expect(result.current[0]).toEqual([]);
  });

  it("should not trigger timeout if state has no content", () => {
    const { result } = renderHook(() => useAutoDismiss(null, 3000));

    // state is null (falsy), so timer shouldn't start
    act(() => {
      vi.advanceTimersByTime(4000);
    });

    expect(result.current[0]).toBeNull();
  });
});
