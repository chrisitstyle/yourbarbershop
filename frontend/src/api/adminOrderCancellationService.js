import { API_BASE_URL } from "./config.js";
import { apiRequest } from "./httpClient.js";

/**
 * Cancels a registered customer's reservation as an administrator.
 *
 * The backend decides whether the cancellation requires a full Stripe refund.
 * Both HTTP 200 and 202 are successful responses; the returned refund status
 * describes whether the financial operation has completed.
 *
 * @param {number} orderId - Identifier of the registered customer's order.
 * @returns {Promise<object>} Cancellation details, including refund information when applicable.
 */
export const cancelRegisteredOrder = async (orderId) => {
  const response = await apiRequest(
    `${API_BASE_URL}/admin/orders/${orderId}/cancel`,
    { method: "POST" },
  );

  return response.data;
};

/**
 * Cancels a guest reservation as an administrator.
 *
 * The backend decides whether a Stripe refund is required. This method must
 * not be retried manually after an uncertain network outcome; check the
 * reservation and refund status before attempting any further action.
 *
 * @param {number} guestOrderId - Identifier of the guest order.
 * @returns {Promise<object>} Cancellation details, including refund information when applicable.
 */
export const cancelGuestOrder = async (guestOrderId) => {
  const response = await apiRequest(
    `${API_BASE_URL}/admin/guest-orders/${guestOrderId}/cancel`,
    { method: "POST" },
  );

  return response.data;
};
