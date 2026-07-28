import { renderHook, act } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import useTableData from "../useTableData";

describe("useTableData hook", () => {
  // given
  const mockData = [
    { id: 1, name: "Apple" },
    { id: 2, name: "Banana" },
    { id: 3, name: "Cherry" },
    { id: 4, name: "Date" },
    { id: 5, name: "Elderberry" },
  ];

  const filterFn = (item, term) =>
    item.name.toLowerCase().includes(term.toLowerCase());

  it("should return paginated data correctly (first page)", () => {
    // when
    const { result } = renderHook(() => useTableData(mockData, filterFn, 2));

    // then
    expect(result.current.currentData).toHaveLength(2);
    expect(result.current.currentData[0].name).toBe("Apple");
    expect(result.current.totalPages).toBe(3);
  });

  it("should filter data and reset to page 1", () => {
    // given
    const { result } = renderHook(() => useTableData(mockData, filterFn, 2));

    // navigate to page 2
    act(() => {
      result.current.setCurrentPage(2);
    });

    // search for "Cherry"
    // when
    act(() => {
      result.current.handleSearchChange("Cherry");
    });

    // then
    expect(result.current.searchTerm).toBe("Cherry");
    expect(result.current.currentData).toHaveLength(1);
    expect(result.current.currentData[0].name).toBe("Cherry");
    expect(result.current.currentPage).toBe(1); // should reset to page 1
  });
});
