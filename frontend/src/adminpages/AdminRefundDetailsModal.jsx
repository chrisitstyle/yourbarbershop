import { Alert, Badge, Button, Modal, Spinner } from "react-bootstrap";
import { useQuery } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faCheckCircle,
  faClock,
  faExclamationTriangle,
  faSyncAlt,
  faTimesCircle,
} from "@fortawesome/free-solid-svg-icons";

import { getAdminRefundDetails } from "../api/adminRefundService.js";

const REFUND_STATUS_PRESENTATION = {
  REQUESTED: {
    variant: "info",
    icon: faClock,
    messageKey: "processingMessage",
  },
  SUBMITTING: {
    variant: "info",
    icon: faClock,
    messageKey: "processingMessage",
  },
  PENDING: {
    variant: "info",
    icon: faClock,
    messageKey: "processingMessage",
  },
  SUCCEEDED: {
    variant: "success",
    icon: faCheckCircle,
    messageKey: "succeededMessage",
  },
  FAILED: {
    variant: "danger",
    icon: faTimesCircle,
    messageKey: "failedMessage",
  },
  CANCELED: {
    variant: "danger",
    icon: faTimesCircle,
    messageKey: "canceledMessage",
  },
  REQUIRES_ACTION: {
    variant: "warning",
    icon: faExclamationTriangle,
    messageKey: "requiresActionMessage",
  },
  RECONCILIATION_REQUIRED: {
    variant: "warning",
    icon: faExclamationTriangle,
    messageKey: "reconciliationRequiredMessage",
  },
};

/**
 * Formats a monetary amount using its persisted currency.
 *
 * @param {number|string|null} amount - Refund amount.
 * @param {string} currency - ISO currency code.
 * @param {string} locale - Formatting locale.
 * @returns {string} Formatted monetary amount.
 */
const formatRefundAmount = (amount, currency, locale) => {
  if (amount === null || amount === undefined) {
    return "—";
  }

  const numericAmount = Number(amount);

  if (!Number.isFinite(numericAmount) || !currency) {
    return "—";
  }

  return new Intl.NumberFormat(locale, {
    style: "currency",
    currency: currency.toUpperCase(),
  }).format(numericAmount);
};

/**
 * Formats an ISO timestamp in the administrator's local timezone.
 *
 * @param {string|null} timestamp - ISO timestamp.
 * @param {string} locale - Formatting locale.
 * @returns {string} Localized date and time.
 */
const formatRefundDateTime = (timestamp, locale) => {
  if (!timestamp) {
    return "—";
  }

  const refundDate = new Date(timestamp);

  if (Number.isNaN(refundDate.getTime())) {
    return "—";
  }

  return new Intl.DateTimeFormat(locale, {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(refundDate);
};

/**
 * Displays a labeled refund detail.
 *
 * @param {object} props - Component properties.
 * @param {string} props.label - Detail label.
 * @param {React.ReactNode} props.value - Detail value.
 */
const RefundDetailRow = ({ label, value }) => (
  <div className="row gy-1 py-2 border-bottom">
    <div className="col-sm-5 text-body-secondary">{label}</div>

    <div className="col-sm-7 text-sm-end fw-medium text-break">
      {value ?? "—"}
    </div>
  </div>
);

/**
 * Displays persisted Stripe refund details for administrators.
 *
 * This modal is read-only and never submits another refund.
 * Reservation information comes from the selected history entry,
 * while financial details are loaded using the refund identifier.
 *
 * @param {object} props - Component properties.
 * @param {boolean} props.show - Whether the modal is visible.
 * @param {Function} props.onHide - Closes the modal.
 * @param {object|null} props.refund - Selected refund history entry.
 * @param {Function} [props.onRefundUpdated] - Called after a successful manual refresh.
 */
const AdminRefundDetailsModal = ({ show, onHide, refund, onRefundUpdated }) => {
  const { t, i18n } = useTranslation();

  const refundId = refund?.refundId;

  const locale = i18n.resolvedLanguage?.startsWith("en") ? "en-GB" : "pl-PL";

  const {
    data: refundDetails,
    error: refundDetailsError,
    isLoading,
    isFetching,
    refetch,
  } = useQuery({
    queryKey: ["adminRefundDetails", refundId],

    queryFn: () => getAdminRefundDetails(refundId),

    enabled: show && Number.isInteger(refundId) && refundId > 0,

    staleTime: 0,
    retry: 1,
  });

  const refundStatus = refundDetails?.status;

  const statusPresentation = REFUND_STATUS_PRESENTATION[refundStatus] ?? {
    variant: "secondary",
    icon: faClock,
    messageKey: "unknownStatusMessage",
  };

  const reservationType = refund?.reservationType
    ? t(`admin.refundHistory.reservationTypes.${refund.reservationType}`, {
        defaultValue: refund.reservationType,
      })
    : "—";

  const formattedRefundAmount = refundDetails
    ? formatRefundAmount(refundDetails.amount, refundDetails.currency, locale)
    : "—";

  /**
   * Reloads the persisted refund status without initiating
   * any new Stripe refund operation.
   */
  const handleRefreshRefundStatus = async () => {
    if (!refundId || isFetching) {
      return;
    }

    const refreshResult = await refetch();

    if (refreshResult.isSuccess && onRefundUpdated) {
      try {
        await onRefundUpdated(refreshResult.data);
      } catch (callbackError) {
        console.error(
          "Failed to refresh refund history after status update:",
          callbackError,
        );
      }
    }
  };

  return (
    <Modal
      show={show}
      onHide={onHide}
      centered
      scrollable
      size="lg"
      aria-labelledby="admin-refund-details-title"
    >
      <Modal.Header closeButton>
        <Modal.Title id="admin-refund-details-title">
          {t("admin.refundDetails.title", {
            refundId: refundId ?? "—",
          })}
        </Modal.Title>
      </Modal.Header>

      <Modal.Body aria-busy={isFetching}>
        {isLoading && (
          <div className="text-center py-5">
            <Spinner
              animation="border"
              role="status"
              aria-label={t("admin.refundDetails.loading")}
            />

            <p className="text-body-secondary mt-3 mb-0">
              {t("admin.refundDetails.loading")}
            </p>
          </div>
        )}

        {refundDetailsError && (
          <Alert variant={refundDetails ? "warning" : "danger"} role="alert">
            {t("admin.refundDetails.loadingError")}

            {refundDetails && (
              <p className="mb-0 mt-2">
                {t("admin.refundDetails.cachedDetailsWarning")}
              </p>
            )}
          </Alert>
        )}

        {!isLoading && refundDetails && (
          <>
            {/* Current refund status */}
            <Alert
              variant={statusPresentation.variant}
              aria-live="polite"
              className="mb-4"
            >
              <div className="d-flex align-items-center gap-2 mb-2">
                <FontAwesomeIcon
                  icon={statusPresentation.icon}
                  aria-hidden="true"
                />

                <strong>
                  {t(`admin.orderCancellation.refundStatuses.${refundStatus}`, {
                    defaultValue: refundStatus ?? "—",
                  })}
                </strong>
              </div>

              <p className="mb-0">
                {t(`admin.refundDetails.${statusPresentation.messageKey}`)}
              </p>
            </Alert>

            {/* Reservation details */}
            <section className="mb-4">
              <h6 className="fw-semibold mb-3">
                {t("admin.refundDetails.reservationSection")}
              </h6>

              <RefundDetailRow
                label={t("admin.refundDetails.reservationId")}
                value={refund?.reservationId ? `#${refund.reservationId}` : "—"}
              />

              <RefundDetailRow
                label={t("admin.refundDetails.reservationType")}
                value={reservationType}
              />
            </section>

            {/* Payment and refund details */}
            <section className="mb-4">
              <h6 className="fw-semibold mb-3">
                {t("admin.refundDetails.paymentSection")}
              </h6>

              <RefundDetailRow
                label={t("admin.refundDetails.refundId")}
                value={`#${refundDetails.refundId}`}
              />

              <RefundDetailRow
                label={t("admin.refundDetails.paymentId")}
                value={
                  refundDetails.paymentId ? `#${refundDetails.paymentId}` : "—"
                }
              />

              <RefundDetailRow
                label={t("admin.refundDetails.amount")}
                value={<span className="fw-bold">{formattedRefundAmount}</span>}
              />

              <RefundDetailRow
                label={t("admin.refundDetails.currency")}
                value={refundDetails.currency?.toUpperCase() ?? "—"}
              />

              <RefundDetailRow
                label={t("admin.refundDetails.stripeRefundId")}
                value={
                  refundDetails.stripeRefundId ? (
                    <code className="text-break">
                      {refundDetails.stripeRefundId}
                    </code>
                  ) : (
                    "—"
                  )
                }
              />

              <RefundDetailRow
                label={t("admin.refundDetails.status")}
                value={
                  <Badge bg={statusPresentation.variant}>
                    {t(
                      `admin.orderCancellation.refundStatuses.${refundStatus}`,
                      { defaultValue: refundStatus ?? "—" },
                    )}
                  </Badge>
                }
              />
            </section>

            {/* Refund processing timestamps */}
            <section className="mb-4">
              <h6 className="fw-semibold mb-3">
                {t("admin.refundDetails.timelineSection")}
              </h6>

              <RefundDetailRow
                label={t("admin.refundDetails.requestedAt")}
                value={formatRefundDateTime(refundDetails.requestedAt, locale)}
              />

              <RefundDetailRow
                label={t("admin.refundDetails.completedAt")}
                value={formatRefundDateTime(refundDetails.completedAt, locale)}
              />
            </section>

            {/* Failure or reconciliation details */}
            {refundDetails.failureReason && (
              <Alert variant="warning" className="mb-3">
                <strong>{t("admin.refundDetails.failureReason")}</strong>

                <p className="mt-2 mb-0 text-break">
                  {refundDetails.failureReason}
                </p>
              </Alert>
            )}

            {[
              "FAILED",
              "CANCELED",
              "RECONCILIATION_REQUIRED",
              "REQUIRES_ACTION",
            ].includes(refundStatus) && (
              <Alert variant="secondary" className="mb-0">
                {t("admin.refundDetails.administratorWarning")}
              </Alert>
            )}
          </>
        )}

        {!isLoading && !refundDetails && !refundDetailsError && (
          <Alert variant="info" className="mb-0">
            {t("admin.refundDetails.noDetails")}
          </Alert>
        )}
      </Modal.Body>

      <Modal.Footer>
        <Button
          variant="outline-secondary"
          onClick={handleRefreshRefundStatus}
          disabled={!refundId || isFetching}
        >
          {isFetching ? (
            <Spinner
              animation="border"
              size="sm"
              className="me-2"
              aria-hidden="true"
            />
          ) : (
            <FontAwesomeIcon
              icon={faSyncAlt}
              className="me-2"
              aria-hidden="true"
            />
          )}

          {t("admin.refundDetails.refreshStatus")}
        </Button>

        <Button variant="secondary" onClick={onHide}>
          {t("admin.refundDetails.close")}
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default AdminRefundDetailsModal;
