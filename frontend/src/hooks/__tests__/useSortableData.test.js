import { renderHook, act } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import useSortableData from "../useSortableData";

describe("useSortableData hook", () => {
  // given
  const mockData = [
    { id: 1, info: { price: 50 }, name: "Zebra" },
    { id: 2, info: { price: 20 }, name: "Apple" },
    { id: 3, info: { price: 100 }, name: "Monkey" },
  ];

  it("should return unsorted data initially if no field is set", () => {
    // when
    const { result } = renderHook(() => useSortableData(mockData));

    // then
    expect(result.current.sortedData[0].name).toBe("Zebra");
  });

  it("should sort data alphabetically ascending", () => {
    // given
    const { result } = renderHook(() => useSortableData(mockData));

    // when
    act(() => {
      result.current.handleSort("name");
    });

    // then
    expect(result.current.sortedData[0].name).toBe("Apple");
    expect(result.current.sortedData[2].name).toBe("Zebra");
  });

  it("should sort nested numerical values", () => {
    // given
    const { result } = renderHook(() => useSortableData(mockData));

    // when
    act(() => {
      result.current.handleSort("info.price");
    });

    // then
    expect(result.current.sortedData[0].name).toBe("Apple"); // 20
    expect(result.current.sortedData[2].name).toBe("Monkey"); // 100
  });
});
