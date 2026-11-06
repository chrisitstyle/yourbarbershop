import { useEffect, useRef, useState } from "react";
import { Alert, Button, Modal, Spinner } from "react-bootstrap";
import { useTranslation } from "react-i18next";
import {
  cancelGuestOrder,
  cancelRegisteredOrder,
} from "../api/adminOrderCancellationService";
import { getAdminRefundDetails } from "../api/adminRefundService";
import { canAdminCancelReservation } from "./utils/adminOrderCancellationRules";

const RESERVATION_TYPES = {
  REGISTERED: "REGISTERED",
  GUEST: "GUEST",
};

const FAILED_REFUND_STATUSES = new Set(["FAILED", "CANCELED"]);

const REFUND_STATUSES_REQUIRING_ATTENTION = new Set([
  "RECONCILIATION_REQUIRED",
  "REQUIRES_ACTION",
]);

/**
 * Resolves the visual severity of a refund status.
 *
 * @param {string} refundStatus - Refund status returned by the backend.
 * @returns {string} React-Bootstrap alert variant.
 */
const getRefundAlertVariant = (refundStatus) => {
  if (refundStatus === "SUCCEEDED") {
    return "success";
  }

  if (FAILED_REFUND_STATUSES.has(refundStatus)) {
    return "danger";
  }

  if (REFUND_STATUSES_REQUIRING_ATTENTION.has(refundStatus)) {
    return "warning";
  }

  return "info";
};

/**
 * Displays and handles administrative reservation cancellation.
 *
 * The component supports registered and guest reservations.
 * It never retries an uncertain cancellation request automatically.
 *
 * @param {object} props - Component properties.
 * @param {boolean} props.show - Whether the modal is visible.
 * @param {Function} props.onHide - Closes the modal.
 * @param {object|null} props.reservation - Selected reservation.
 * @param {"REGISTERED"|"GUEST"} props.reservationType - Reservation type.
 * @param {Function} [props.onCancellationSuccess] - Called after successful cancellation.
 * @param {Function} [props.onCancellationAttemptFinished] - Called after every cancellation attempt to refresh reservation data.
 */
const AdminOrderCancellationModal = ({
  show,
  onHide,
  reservation,
  reservationType,
  onCancellationSuccess,
  onCancellationAttemptFinished,
}) => {
  const { t } = useTranslation();

  const [isSubmittingCancellation, setIsSubmittingCancellation] =
    useState(false);

  const [hasSubmittedCancellation, setHasSubmittedCancellation] =
    useState(false);

  const [cancellationResult, setCancellationResult] = useState(null);
  const [cancellationError, setCancellationError] = useState(null);

  const [refundDetails, setRefundDetails] = useState(null);
  const [refundLookupError, setRefundLookupError] = useState(null);
  const [isRefreshingRefund, setIsRefreshingRefund] = useState(false);

  const cancellationRequestStartedRef = useRef(false);

  const reservationId =
    reservationType === RESERVATION_TYPES.GUEST
      ? reservation?.idGuestOrder
      : reservation?.idOrder;

  const customerName =
    reservationType === RESERVATION_TYPES.GUEST
      ? [reservation?.firstname, reservation?.lastname]
          .filter(Boolean)
          .join(" ")
      : [reservation?.user?.firstname, reservation?.user?.lastname]
          .filter(Boolean)
          .join(" ");

  const isPaidOnlineReservation =
    reservation?.paymentMethod === "KARTA_ONLINE" &&
    reservation?.paymentStatus === "OPLACONA";

  const cancellationAllowed = canAdminCancelReservation(reservation);

  const refundId = cancellationResult?.refund?.refundId;

  const refundStatus =
    refundDetails?.status ?? cancellationResult?.refund?.status ?? null;

  const refundFailureReason = refundDetails?.failureReason;

  const isCancellationCompleted = cancellationResult !== null;

  const reservationServicePrice = Number(reservation?.offer?.cost);

  const formattedServicePrice = Number.isFinite(reservationServicePrice)
    ? new Intl.NumberFormat("pl-PL", {
        style: "currency",
        currency: "PLN",
      }).format(reservationServicePrice)
    : "—";

  useEffect(() => {
    if (!show) {
      return;
    }

    setIsSubmittingCancellation(false);
    setHasSubmittedCancellation(false);
    setCancellationResult(null);
    setCancellationError(null);
    setRefundDetails(null);
    setRefundLookupError(null);
    setIsRefreshingRefund(false);

    cancellationRequestStartedRef.current = false;
  }, [show, reservationId, reservationType]);

  /**
   * Refreshes reservation data after a cancellation attempt.
   *
   * This callback is also invoked after a failed request because
   * a network error does not guarantee that cancellation failed
   * on the backend.
   */
  const notifyCancellationAttemptFinished = async () => {
    try {
      await onCancellationAttemptFinished?.();
    } catch (error) {
      console.error("Failed to refresh reservation data:", error);
    }
  };

  /**
   * Submits the cancellation request at most once per modal session.
   */
  const handleConfirmCancellation = async () => {
    if (
      !cancellationAllowed ||
      !reservationId ||
      cancellationRequestStartedRef.current
    ) {
      return;
    }

    cancellationRequestStartedRef.current = true;

    setHasSubmittedCancellation(true);
    setIsSubmittingCancellation(true);
    setCancellationError(null);

    try {
      const cancellationResponse =
        reservationType === RESERVATION_TYPES.GUEST
          ? await cancelGuestOrder(reservationId)
          : await cancelRegisteredOrder(reservationId);

      setCancellationResult(cancellationResponse);

      try {
        await onCancellationSuccess?.(cancellationResponse);
      } catch (callbackError) {
        console.error(
          "Failed to handle successful cancellation:",
          callbackError,
        );
      }
    } catch (error) {
      console.error("Administrative cancellation failed:", error);

      setCancellationError(error);
    } finally {
      setIsSubmittingCancellation(false);

      await notifyCancellationAttemptFinished();
    }
  };

  /**
   * Retrieves the latest persisted refund status without
   * initiating another refund operation.
   */
  const handleRefreshRefundStatus = async () => {
    if (!refundId || isRefreshingRefund) {
      return;
    }

    setIsRefreshingRefund(true);
    setRefundLookupError(null);

    try {
      const latestRefundDetails = await getAdminRefundDetails(refundId);

      setRefundDetails(latestRefundDetails);
    } catch (error) {
      console.error("Failed to retrieve refund details:", error);
      setRefundLookupError(error);
    } finally {
      setIsRefreshingRefund(false);
    }
  };

  const handleClose = () => {
    if (isSubmittingCancellation) {
      return;
    }

    onHide();
  };

  const getRefundMessage = () => {
    if (refundStatus === "SUCCEEDED") {
      return t("admin.orderCancellation.refundSucceeded");
    }

    if (FAILED_REFUND_STATUSES.has(refundStatus)) {
      return t("admin.orderCancellation.refundFailed");
    }

    if (REFUND_STATUSES_REQUIRING_ATTENTION.has(refundStatus)) {
      return t("admin.orderCancellation.refundRequiresAttention");
    }

    return t("admin.orderCancellation.refundPending");
  };

  return (
    <Modal
      show={show}
      onHide={handleClose}
      centered
      backdrop={isSubmittingCancellation ? "static" : true}
      keyboard={!isSubmittingCancellation}
      aria-labelledby="admin-order-cancellation-title"
    >
      <Modal.Header closeButton={!isSubmittingCancellation}>
        <Modal.Title id="admin-order-cancellation-title">
          {t("admin.orderCancellation.title", {
            reservationId,
          })}
        </Modal.Title>
      </Modal.Header>

      <Modal.Body>
        <div className="mb-3">
          <p className="mb-2">
            {t("admin.orderCancellation.customer")}
            {": "}
            <strong>{customerName || "—"}</strong>
          </p>

          <p className="mb-2">
            {t("admin.orderCancellation.service")}
            {": "}
            <strong>{reservation?.offer?.kind || "—"}</strong>
          </p>

          <p className="mb-2">
            {t("admin.orderCancellation.servicePrice")}
            {": "}
            <strong>{formattedServicePrice}</strong>
          </p>

          <p className="mb-0">
            {t("admin.orderCancellation.paymentMethod")}
            {": "}
            <strong>
              {t(`enums.${reservation?.paymentMethod}`, {
                defaultValue: reservation?.paymentMethod ?? "—",
              })}
            </strong>
          </p>
        </div>

        {!hasSubmittedCancellation && (
          <>
            <p>{t("admin.orderCancellation.confirmationMessage")}</p>

            {isPaidOnlineReservation ? (
              <Alert variant="warning" className="mb-0">
                <strong>
                  {t("admin.orderCancellation.onlineRefundTitle")}
                </strong>

                <p className="mb-0 mt-2">
                  {t("admin.orderCancellation.onlineRefundDescription")}
                </p>
              </Alert>
            ) : (
              <Alert variant="info" className="mb-0">
                {t("admin.orderCancellation.onSiteDescription")}
              </Alert>
            )}

            {!cancellationAllowed && (
              <Alert variant="danger" className="mt-3 mb-0">
                {t("admin.orderCancellation.cancellationNotAllowed")}
              </Alert>
            )}
          </>
        )}

        {cancellationError && (
          <Alert variant="danger" className="mb-0">
            <strong>
              {t("admin.orderCancellation.cancellationErrorTitle")}
            </strong>

            <p className="mt-2 mb-2">
              {cancellationError?.response?.data?.detail ||
                cancellationError?.message ||
                t("admin.orderCancellation.unknownError")}
            </p>

            <p className="mb-0">
              {t("admin.orderCancellation.uncertainOutcomeWarning")}
            </p>
          </Alert>
        )}

        {isCancellationCompleted && (
          <>
            <Alert variant="success">
              {t("admin.orderCancellation.cancellationSucceeded")}
            </Alert>

            {refundId ? (
              <Alert variant={getRefundAlertVariant(refundStatus)}>
                <strong>
                  {t("admin.orderCancellation.refundStatus")}
                  {": "}
                  {t(`admin.orderCancellation.refundStatuses.${refundStatus}`, {
                    defaultValue: refundStatus,
                  })}
                </strong>

                <p className="mt-2 mb-0">{getRefundMessage()}</p>

                {refundFailureReason && (
                  <p className="mt-2 mb-0">
                    <strong>
                      {t("admin.orderCancellation.failureReason")}
                      {": "}
                    </strong>
                    {refundFailureReason}
                  </p>
                )}

                <div className="mt-3">
                  <Button
                    variant="outline-secondary"
                    size="sm"
                    onClick={handleRefreshRefundStatus}
                    disabled={isRefreshingRefund}
                  >
                    {isRefreshingRefund && (
                      <Spinner
                        animation="border"
                        size="sm"
                        className="me-2"
                        aria-hidden="true"
                      />
                    )}

                    {t("admin.orderCancellation.refreshRefundStatus")}
                  </Button>
                </div>

                {refundLookupError && (
                  <p className="text-danger mt-2 mb-0">
                    {t("admin.orderCancellation.refundLookupError")}
                  </p>
                )}
              </Alert>
            ) : (
              <Alert variant="info" className="mb-0">
                {t("admin.orderCancellation.noRefundRequired")}
              </Alert>
            )}
          </>
        )}
      </Modal.Body>

      <Modal.Footer>
        <Button
          variant="secondary"
          onClick={handleClose}
          disabled={isSubmittingCancellation}
        >
          {isCancellationCompleted || hasSubmittedCancellation
            ? t("admin.orderCancellation.close")
            : t("admin.orderCancellation.back")}
        </Button>

        {!hasSubmittedCancellation && (
          <Button
            variant="danger"
            onClick={handleConfirmCancellation}
            disabled={!cancellationAllowed || isSubmittingCancellation}
          >
            {isSubmittingCancellation && (
              <Spinner
                animation="border"
                size="sm"
                className="me-2"
                aria-hidden="true"
              />
            )}

            {isPaidOnlineReservation
              ? t("admin.orderCancellation.cancelAndRefund")
              : t("admin.orderCancellation.confirmCancellation")}
          </Button>
        )}
      </Modal.Footer>
    </Modal>
  );
};

export default AdminOrderCancellationModal;
