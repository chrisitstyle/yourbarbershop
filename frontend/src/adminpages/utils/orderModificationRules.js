const TERMINAL_ORDER_STATUSES = new Set(["ZREALIZOWANE", "ANULOWANE"]);

const BLOCKED_OFFER_PAYMENT_STATUSES = new Set(["OPLACONA", "ZWROCONA"]);

/**
 * Resolves frontend order modification permissions based on the current
 * order and payment state.
 *
 * These rules improve the user experience by disabling invalid editing
 * actions. Administrative cancellation is handled separately through
 * canAdminCancelReservation().
 *
 * The backend remains the authoritative source of validation.
 *
 * @param {object} order - Order or guest order returned by the API.
 * @returns {{
 *   orderStatus: string,
 *   paymentMethod: string,
 *   paymentStatus: string,
 *   isTerminalOrder: boolean,
 *   isPendingOnlinePayment: boolean,
 *   isOfferChangeBlocked: boolean,
 *   isPaymentSettled: boolean,
 *   canComplete: boolean
 * }} Available order modification rules.
 */
export const getOrderModificationRules = (order = {}) => {
  const orderStatus = order?.orderStatus ?? "";
  const paymentMethod = order?.paymentMethod ?? "";
  const paymentStatus = order?.paymentStatus ?? "";

  const isTerminalOrder = TERMINAL_ORDER_STATUSES.has(orderStatus);

  /*
   * Pending online payments may still be confirmed.
   * Offer changes are therefore temporarily blocked.
   */
  const isPendingOnlinePayment =
    paymentMethod === "KARTA_ONLINE" &&
    paymentStatus === "OCZEKUJE_NA_PLATNOSC";

  const isOfferChangeBlocked =
    BLOCKED_OFFER_PAYMENT_STATUSES.has(paymentStatus) || isPendingOnlinePayment;

  /*
   * Online payments must be completed before the reservation
   * can be marked as fulfilled.
   * On-site payments do not require advance payment confirmation.
   */
  const isPaymentSettled =
    paymentStatus === "OPLACONA" ||
    (paymentMethod !== "KARTA_ONLINE" && paymentStatus === "NIE_WYMAGANA");

  // Only active reservations with settled payments can be completed.
  const canComplete = !isTerminalOrder && isPaymentSettled;

  return {
    orderStatus,
    paymentMethod,
    paymentStatus,
    isTerminalOrder,
    isPendingOnlinePayment,
    isOfferChangeBlocked,
    isPaymentSettled,
    canComplete,
  };
};
