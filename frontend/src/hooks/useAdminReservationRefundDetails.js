import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";

import { getAdminRefundHistory } from "../api/adminRefundService.js";
import { ADMIN_REFUND_HISTORY_QUERY_KEY } from "./useAdminRefunds.js";

const REFUND_LOOKUP_PAGE_SIZE = 100;

/**
 * Finds the latest refund associated with a specific reservation.
 *
 * Registered and guest reservations have separate ID sequences, so
 * both their identifier and type must match the requested reservation.
 *
 * The existing history API filters by reservation ID only. Following
 * subsequent pages ensures a matching refund is not overlooked when
 * another reservation type shares the same numeric identifier.
 *
 * @param {object} reservation - Reservation lookup parameters.
 * @param {number} reservation.reservationId - Reservation identifier.
 * @param {string} reservation.reservationType - REGISTERED or GUEST.
 * @returns {Promise<object|null>} Latest matching refund, if available.
 */
const findLatestReservationRefund = async ({
  reservationId,
  reservationType,
}) => {
  let pageIndex = 0;

  while (true) {
    const refundHistory = await getAdminRefundHistory({
      reservationId,
      page: pageIndex,
      size: REFUND_LOOKUP_PAGE_SIZE,
    });

    const matchingRefund = refundHistory.content.find(
      (refund) =>
        refund.reservationType === reservationType &&
        Number(refund.reservationId) === Number(reservationId),
    );

    if (matchingRefund) {
      return matchingRefund;
    }

    if (pageIndex + 1 >= refundHistory.totalPages) {
      return null;
    }

    pageIndex += 1;
  }
};

/**
 * Manages on-demand refund lookup and the shared refund details modal.
 *
 * The history endpoint is called only after an administrator clicks
 * the action in a cancelled reservation row. No refund is initiated.
 *
 * @returns {object} Refund details modal state and event handlers.
 */
const useAdminReservationRefundDetails = () => {
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const [selectedRefund, setSelectedRefund] = useState(null);

  const refundLookupMutation = useMutation({
    mutationFn: findLatestReservationRefund,

    onSuccess: (refund) => {
      if (refund) {
        setSelectedRefund(refund);
      } else {
        toast.info(t("admin.refundHistory.noRefundForReservation"));
      }
    },

    onError: (error) => {
      console.error("Failed to find reservation refund:", error);
      toast.error(t("admin.refundHistory.refundLookupError"));
    },
  });

  /**
   * Looks up a refund for the selected cancelled reservation.
   *
   * @param {number} reservationId - Reservation identifier.
   * @param {string} reservationType - REGISTERED or GUEST.
   */
  const openRefundDetails = (reservationId, reservationType) => {
    if (refundLookupMutation.isPending) {
      return;
    }

    refundLookupMutation.mutate({ reservationId, reservationType });
  };

  /**
   * Closes the shared details modal.
   */
  const closeRefundDetails = () => {
    setSelectedRefund(null);
  };

  /**
   * Marks cached refund history as stale after a manual status check.
   * Active history views will refetch automatically.
   */
  const handleRefundUpdated = async () => {
    await queryClient.invalidateQueries({
      queryKey: ADMIN_REFUND_HISTORY_QUERY_KEY,
    });
  };

  /**
   * Indicates whether a particular row is currently being looked up.
   *
   * @param {number} reservationId - Reservation identifier.
   * @param {string} reservationType - REGISTERED or GUEST.
   * @returns {boolean} Whether the lookup targets this reservation.
   */
  const isLookingUpRefundFor = (reservationId, reservationType) =>
    refundLookupMutation.isPending &&
    refundLookupMutation.variables?.reservationId === reservationId &&
    refundLookupMutation.variables?.reservationType === reservationType;

  return {
    selectedRefund,
    openRefundDetails,
    closeRefundDetails,
    handleRefundUpdated,
    isLookingUpRefund: refundLookupMutation.isPending,
    isLookingUpRefundFor,
  };
};

export default useAdminReservationRefundDetails;
