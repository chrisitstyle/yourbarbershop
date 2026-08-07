import { API_BASE_URL } from "./config";
import { apiRequest } from "./httpClient";

/**
 * Resolves a signed payment link to an active Stripe Checkout URL.
 *
 * @param {string} token signed payment link token
 * @returns {Promise<{checkoutUrl: string}>} active Stripe Checkout data
 */
export const resolvePaymentCheckout = async (token) => {
  const response = await apiRequest(
    `${API_BASE_URL}/payments/link/${encodeURIComponent(token)}/checkout`,
    {
      method: "POST",
      skipAuthRefresh: true,
    },
  );

  return response.data;
};