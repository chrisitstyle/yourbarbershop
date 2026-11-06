import { API_BASE_URL } from "./config.js";
import { apiRequest } from "./httpClient.js";

/**
 * Retrieves paginated refund history for administrators.
 *
 * Supports filtering by refund status and reservation identifier.
 * The backend also returns global refund summary statistics.
 *
 * @param {object} filters - Refund history filters.
 * @param {number} [filters.page=0] - Zero-based page number.
 * @param {number} [filters.size=20] - Number of refunds per page.
 * @param {string|null} [filters.status=null] - Optional refund status.
 * @param {number|null} [filters.reservationId=null] - Optional reservation identifier.
 * @param {object} options - Additional request options.
 * @param {AbortSignal} [options.signal] - Request cancellation signal.
 * @returns {Promise<object>} Paginated refund history and summary statistics.
 */
export const getAdminRefundHistory = async (
  { page = 0, size = 20, status = null, reservationId = null } = {},
  { signal } = {},
) => {
  const searchParameters = new URLSearchParams({
    page: String(page),
    size: String(size),
  });

  if (status) {
    searchParameters.set("status", status);
  }

  if (
    reservationId !== null &&
    reservationId !== undefined &&
    reservationId !== ""
  ) {
    searchParameters.set("reservationId", String(reservationId));
  }

  const response = await apiRequest(
    `${API_BASE_URL}/admin/refunds?${searchParameters.toString()}`,
    {
      method: "GET",
      signal,
    },
  );

  return response.data;
};

/**
 * Retrieves the latest persisted details of a single refund.
 *
 * This read-only request does not initiate a refund or contact
 * Stripe directly. Refund statuses are updated by the backend
 * through webhooks and reconciliation.
 *
 * @param {number} refundId - Identifier of the persisted refund.
 * @returns {Promise<object>} Latest persisted refund details.
 */
export const getAdminRefundDetails = async (refundId) => {
  const response = await apiRequest(
    `${API_BASE_URL}/admin/refunds/${refundId}`,
  );

  return response.data;
};
