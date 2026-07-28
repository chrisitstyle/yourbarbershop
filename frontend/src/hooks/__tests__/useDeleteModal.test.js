import { renderHook, act } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import useDeleteModal from "../useDeleteModal";

describe("useDeleteModal hook", () => {
  it("should initialize with default closed state", () => {
    const { result } = renderHook(() => useDeleteModal(vi.fn()));

    expect(result.current.show).toBe(false);
    expect(result.current.itemToDelete).toBeNull();
    expect(result.current.isDeleting).toBe(false);
  });

  it("should open modal and set item to delete when askDelete is called", () => {
    const { result } = renderHook(() => useDeleteModal(vi.fn()));
    const mockItem = { id: 1, name: "Test Offer" };

    act(() => {
      result.current.askDelete(mockItem);
    });

    expect(result.current.show).toBe(true);
    expect(result.current.itemToDelete).toEqual(mockItem);
  });

  it("should call deleteAction and refreshAction on confirmDelete", async () => {
    const deleteActionMock = vi.fn().mockResolvedValue(true);
    const refreshActionMock = vi.fn().mockResolvedValue(true);
    const mockItem = { id: 1 };

    const { result } = renderHook(() =>
      useDeleteModal(deleteActionMock, refreshActionMock),
    );

    // open modal first
    act(() => {
      result.current.askDelete(mockItem);
    });

    // confirm deletion
    await act(async () => {
      await result.current.confirmDelete();
    });

    expect(deleteActionMock).toHaveBeenCalledWith(mockItem);
    expect(refreshActionMock).toHaveBeenCalled();
    expect(result.current.show).toBe(false);
    expect(result.current.itemToDelete).toBeNull();
  });
});
