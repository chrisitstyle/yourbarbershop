import { API_BASE_URL } from "./config.js";
import { apiRequest } from "./httpClient.js";

/**
 * Reads the most recent refund status stored by the backend.
 *
 * This read-only request does not initiate a refund or contact Stripe directly.
 * Webhooks and backend reconciliation update the persisted refund status.
 *
 * @param {number} refundId - Identifier of the persisted payment refund.
 * @returns {Promise<object>} Refund details and the latest persisted status.
 */
export const getAdminRefundDetails = async (refundId) => {
  const response = await apiRequest(
    `${API_BASE_URL}/admin/refunds/${refundId}`,
  );

  return response.data;
};
