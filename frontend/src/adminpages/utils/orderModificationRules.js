// Orders in a terminal state cannot be modified or transition to another status
const TERMINAL_ORDER_STATUSES = new Set(["ZREALIZOWANE", "ANULOWANE"]);

/* Changing the offer is blocked when the payment amount has already been
 finalized or returned */
const BLOCKED_OFFER_PAYMENT_STATUSES = new Set(["OPLACONA", "ZWROCONA"]);

/**
 * Resolves frontend order modification permissions based on the current
 * order and payment state.
 *
 * These rules improve the user experience by disabling invalid actions.
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
 *   canComplete: boolean,
 *   canCancel: boolean
 * }} Available order modification rules.
 */
export const getOrderModificationRules = (order = {}) => {
  const orderStatus = order?.orderStatus ?? "";
  const paymentMethod = order?.paymentMethod ?? "";
  const paymentStatus = order?.paymentStatus ?? "";

  const isTerminalOrder = TERMINAL_ORDER_STATUSES.has(orderStatus);

  /* A pending online payment may still create or confirm a transaction,
   so offer changes and cancellation are temporarily blocked */
  const isPendingOnlinePayment =
    paymentMethod === "KARTA_ONLINE" &&
    paymentStatus === "OCZEKUJE_NA_PLATNOSC";

  const isOfferChangeBlocked =
    BLOCKED_OFFER_PAYMENT_STATUSES.has(paymentStatus) || isPendingOnlinePayment;

  /* Online payments must be paid before completion.
   On-site payments do not require an earlier payment confirmation */
  const isPaymentSettled =
    paymentStatus === "OPLACONA" ||
    (paymentMethod !== "KARTA_ONLINE" && paymentStatus === "NIE_WYMAGANA");

  // Only an active order with a settled payment can be completed
  const canComplete = !isTerminalOrder && isPaymentSettled;

  /* A paid order requires a separate refund flow before cancellation.
   Pending online payments are also protected from cancellation. */
  const canCancel =
    !isTerminalOrder && paymentStatus !== "OPLACONA" && !isPendingOnlinePayment;

  return {
    orderStatus,
    paymentMethod,
    paymentStatus,
    isTerminalOrder,
    isPendingOnlinePayment,
    isOfferChangeBlocked,
    isPaymentSettled,
    canComplete,
    canCancel,
  };
};
