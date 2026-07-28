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
    // given / when
    const { result } = renderHook(() => useAutoDismiss(null));

    // then
    expect(result.current[0]).toBeNull();
  });

  it("should dismiss state after the specified timeout when it has content", () => {
    // given
    const { result } = renderHook(() => useAutoDismiss([], 3000));

    // when (set some content)
    act(() => {
      result.current[1](["Error 1"]);
    });

    // then
    expect(result.current[0]).toEqual(["Error 1"]);

    // when (advance time by 2999ms)
    act(() => {
      vi.advanceTimersByTime(2999);
    });

    // then (state should still be there)
    expect(result.current[0]).toEqual(["Error 1"]);

    // when (reach the timeout)
    act(() => {
      vi.advanceTimersByTime(1);
    });

    // then (state should be reset)
    expect(result.current[0]).toEqual([]);
  });

  it("should not trigger timeout if state has no content", () => {
    // given
    const { result } = renderHook(() => useAutoDismiss(null, 3000));

    // state is null (falsy), so timer shouldn't start
    // when
    act(() => {
      vi.advanceTimersByTime(4000);
    });

    // then
    expect(result.current[0]).toBeNull();
  });
});
