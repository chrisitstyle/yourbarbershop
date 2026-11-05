import { keepPreviousData, useQuery } from "@tanstack/react-query";

import { getAdminRefundHistory } from "../api/adminRefundService.js";

export const ADMIN_REFUND_HISTORY_QUERY_KEY = ["adminRefundHistory"];

const EMPTY_REFUND_SUMMARY = {
  completed: 0,
  inProgress: 0,
  requiresAttention: 0,
};

/**
 * Retrieves and manages paginated refund history for administrators.
 *
 * Each combination of page, size, refund status, and reservation ID
 * receives a separate TanStack Query cache entry.
 *
 * Previously loaded results remain visible while another page
 * or filter combination is being fetched.
 *
 * @param {object} filters - Refund history filters.
 * @param {number} [filters.page=0] - Zero-based page number.
 * @param {number} [filters.size=20] - Number of refunds per page.
 * @param {string|null} [filters.status=null] - Optional refund status.
 * @param {number|null} [filters.reservationId=null] - Optional reservation ID.
 * @returns {object} Refund history, summary statistics, and query state.
 */
const useAdminRefunds = ({
  page = 0,
  size = 20,
  status = null,
  reservationId = null,
} = {}) => {
  const {
    data: refundHistory,
    isLoading,
    isFetching,
    isPlaceholderData,
    error,
    refetch,
  } = useQuery({
    queryKey: [
      ...ADMIN_REFUND_HISTORY_QUERY_KEY,
      {
        page,
        size,
        status,
        reservationId,
      },
    ],

    queryFn: ({ signal }) =>
      getAdminRefundHistory(
        {
          page,
          size,
          status,
          reservationId,
        },
        { signal },
      ),

    placeholderData: keepPreviousData,

    staleTime: 30_000,
  });

  return {
    refundHistory,

    refunds: refundHistory?.content ?? [],

    summary: refundHistory?.summary ?? EMPTY_REFUND_SUMMARY,

    currentPage: refundHistory?.page ?? page,
    pageSize: refundHistory?.size ?? size,
    totalElements: refundHistory?.totalElements ?? 0,
    totalPages: refundHistory?.totalPages ?? 0,

    isLoading,
    isFetching,
    isPlaceholderData,
    error,
    refetch,
  };
};

export default useAdminRefunds;
