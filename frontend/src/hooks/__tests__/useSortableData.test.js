import { renderHook, act } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import useSortableData from "../useSortableData";

describe("useSortableData hook", () => {
  const mockData = [
    { id: 1, info: { price: 50 }, name: "Zebra" },
    { id: 2, info: { price: 20 }, name: "Apple" },
    { id: 3, info: { price: 100 }, name: "Monkey" },
  ];

  it("should return unsorted data initially if no field is set", () => {
    const { result } = renderHook(() => useSortableData(mockData));
    expect(result.current.sortedData[0].name).toBe("Zebra");
  });

  it("should sort data alphabetically ascending", () => {
    const { result } = renderHook(() => useSortableData(mockData));

    act(() => {
      result.current.handleSort("name");
    });

    // 1st click = ascending
    expect(result.current.sortedData[0].name).toBe("Apple");
    expect(result.current.sortedData[2].name).toBe("Zebra");
  });

  it("should sort nested numerical values", () => {
    const { result } = renderHook(() => useSortableData(mockData));

    act(() => {
      result.current.handleSort("info.price");
    });

    expect(result.current.sortedData[0].name).toBe("Apple"); // 20
    expect(result.current.sortedData[2].name).toBe("Monkey"); // 100
  });
});
