const ON_SITE_PAYMENT_METHODS = new Set(["GOTOWKA", "KARTA_NA_MIEJSCU"]);

/**
 * Determines whether an administrator can cancel a reservation.
 *
 * Paid online reservations require a full Stripe refund.
 * Unpaid on-site reservations can be cancelled without a refund.
 *
 * The backend remains the authoritative source of validation.
 *
 * @param {object|null} reservation - Registered or guest reservation.
 * @returns {boolean} Whether administrative cancellation is available.
 */
export const canAdminCancelReservation = (reservation) => {
  if (!reservation || reservation.orderStatus !== "NOWE") {
    return false;
  }

  const { paymentMethod, paymentStatus } = reservation;

  const isPaidOnlineReservation =
    paymentMethod === "KARTA_ONLINE" && paymentStatus === "OPLACONA";

  const isUnpaidOnSiteReservation =
    ON_SITE_PAYMENT_METHODS.has(paymentMethod) &&
    paymentStatus === "NIE_WYMAGANA";

  return isPaidOnlineReservation || isUnpaidOnSiteReservation;
};
