import { useState } from "react";
import { Alert, Spinner } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faCheckCircle,
  faClock,
  faExclamationTriangle,
  faEye,
  faInfoCircle,
  faSearch,
  faSyncAlt,
  faTimesCircle,
} from "@fortawesome/free-solid-svg-icons";
import { useTranslation } from "react-i18next";

import useAdminRefunds from "../hooks/useAdminRefunds";
import PaginationControl from "../components/common/PaginationControl";
import AdminRefundDetailsModal from "./AdminRefundDetailsModal";

import "./styles/AdminTables.css";
import "./styles/AdminRefundHistory.css";

const REFUNDS_PER_PAGE = 10;

const REFUND_STATUS_GROUPS = [
  {
    translationKey: "completed",
    statuses: ["SUCCEEDED"],
  },
  {
    translationKey: "inProgress",
    statuses: ["REQUESTED", "SUBMITTING", "PENDING"],
  },
  {
    translationKey: "requiresAttention",
    statuses: [
      "FAILED",
      "CANCELED",
      "REQUIRES_ACTION",
      "RECONCILIATION_REQUIRED",
    ],
  },
];

const FAILED_REFUND_STATUSES = new Set(["FAILED", "CANCELED"]);

const REFUND_STATUSES_REQUIRING_ATTENTION = new Set([
  "REQUIRES_ACTION",
  "RECONCILIATION_REQUIRED",
]);

const IN_PROGRESS_REFUND_STATUSES = new Set([
  "REQUESTED",
  "SUBMITTING",
  "PENDING",
]);

/**
 * Formats the refund amount using its persisted currency.
 *
 * @param {number|string} amount - Refund amount.
 * @param {string} currency - ISO currency code.
 * @param {string} locale - Display locale.
 * @returns {string} Formatted monetary amount.
 */
const formatRefundAmount = (amount, currency, locale) => {
  if (amount === null || amount === undefined) {
    return "—";
  }

  const numericAmount = Number(amount);

  if (!Number.isFinite(numericAmount)) {
    return "—";
  }

  return new Intl.NumberFormat(locale, {
    style: "currency",
    currency: currency || "PLN",
  }).format(numericAmount);
};

/**
 * Resolves the visual presentation of a refund status.
 *
 * @param {string} refundStatus - Backend refund status.
 * @returns {object} Status icon and visual variant.
 */
const getRefundStatusPresentation = (refundStatus) => {
  if (refundStatus === "SUCCEEDED") {
    return {
      icon: faCheckCircle,
      variant: "success",
    };
  }

  if (FAILED_REFUND_STATUSES.has(refundStatus)) {
    return {
      icon: faTimesCircle,
      variant: "danger",
    };
  }

  if (REFUND_STATUSES_REQUIRING_ATTENTION.has(refundStatus)) {
    return {
      icon: faExclamationTriangle,
      variant: "warning",
    };
  }

  if (IN_PROGRESS_REFUND_STATUSES.has(refundStatus)) {
    return {
      icon: faClock,
      variant: "pending",
    };
  }

  return {
    icon: faInfoCircle,
    variant: "pending",
  };
};

/**
 * Displays the current status of a persisted refund.
 *
 * @param {object} props - Component properties.
 * @param {string} props.refundStatus - Backend refund status.
 */
const RefundStatusIndicator = ({ refundStatus }) => {
  const { t } = useTranslation();

  const { icon, variant } = getRefundStatusPresentation(refundStatus);

  const translatedStatus = t(
    `admin.orderCancellation.refundStatuses.${refundStatus}`,
    { defaultValue: refundStatus || "—" },
  );

  return (
    <span className={`admin-refund-status admin-refund-status--${variant}`}>
      <FontAwesomeIcon icon={icon} aria-hidden="true" />
      <span>{translatedStatus}</span>
    </span>
  );
};

/**
 * Displays a single entry from the administrator refund history.
 *
 * @param {object} props - Component properties.
 * @param {object} props.refund - Refund history entry.
 * @param {Function} props.onViewRefundDetails - Opens refund details.
 * @param {boolean} props.isUpdating - Whether the table is refreshing.
 */
const RefundHistoryRow = ({ refund, onViewRefundDetails, isUpdating }) => {
  const { t, i18n } = useTranslation();

  const locale = i18n.resolvedLanguage === "en" ? "en-GB" : "pl-PL";

  const reservationType = t(
    `admin.refundHistory.reservationTypes.${refund.reservationType}`,
    { defaultValue: refund.reservationType },
  );

  return (
    <tr>
      <td
        data-label={t("admin.refundHistory.reservation")}
        className="align-middle"
      >
        <div className="fw-semibold">#{refund.reservationId}</div>

        <small className="text-body-secondary">{reservationType}</small>
      </td>

      <td data-label={t("admin.refundHistory.amount")} className="align-middle">
        <span className="fw-semibold text-nowrap">
          {formatRefundAmount(refund.amount, refund.currency, locale)}
        </span>
      </td>

      <td data-label={t("admin.refundHistory.status")} className="align-middle">
        <RefundStatusIndicator refundStatus={refund.status} />
      </td>

      <td
        className="align-middle rtable-actions"
        data-label={t("admin.refundHistory.actions")}
      >
        <button
          type="button"
          className="btn btn-link btn-sm admin-refund-details-button"
          onClick={() => onViewRefundDetails(refund)}
          disabled={isUpdating}
          aria-label={t("admin.refundHistory.viewDetails")}
        >
          <FontAwesomeIcon icon={faEye} className="me-2" aria-hidden="true" />

          {t("admin.refundHistory.viewDetails")}
        </button>
      </td>
    </tr>
  );
};

/**
 * Displays administrator refund history with server-side filtering,
 * pagination, and global refund processing statistics.
 *
 * Manages the selected refund and displays its latest persisted
 * details through a reusable modal.
 */
const AdminRefundHistory = () => {
  const { t } = useTranslation();

  const [pageIndex, setPageIndex] = useState(0);

  const [selectedRefundStatus, setSelectedRefundStatus] = useState("");

  const [reservationIdInput, setReservationIdInput] = useState("");

  const [searchedReservationId, setSearchedReservationId] = useState(null);

  const [reservationIdValidationError, setReservationIdValidationError] =
    useState(false);

  const [selectedRefund, setSelectedRefund] = useState(null);

  const {
    refunds,
    summary,
    totalElements,
    totalPages,
    isLoading,
    isFetching,
    isPlaceholderData,
    error,
    refetch,
  } = useAdminRefunds({
    page: pageIndex,
    size: REFUNDS_PER_PAGE,
    status: selectedRefundStatus || null,
    reservationId: searchedReservationId,
  });

  /**
   * Opens the details modal for a selected refund.
   *
   * The selected history entry provides reservation details,
   * while the modal loads the latest persisted financial status.
   *
   * @param {object} refund - Selected refund history entry.
   */
  const handleOpenRefundDetails = (refund) => {
    setSelectedRefund(refund);
  };

  /**
   * Closes the refund details modal and clears the selection.
   */
  const handleCloseRefundDetails = () => {
    setSelectedRefund(null);
  };

  /**
   * Refreshes refund history after the administrator manually
   * retrieves the latest refund details.
   *
   * This also updates the dashboard summary statistics.
   */
  const handleRefundDetailsRefreshed = async () => {
    await refetch();
  };

  /**
   * Applies a refund status filter and returns to the first page.
   */
  const handleRefundStatusChange = (event) => {
    setSelectedRefundStatus(event.target.value);
    setPageIndex(0);
  };

  /**
   * Updates the reservation identifier search input.
   *
   * Clearing the search input also removes the active filter.
   */
  const handleReservationIdInputChange = (event) => {
    const updatedReservationIdInput = event.target.value;

    setReservationIdInput(updatedReservationIdInput);
    setReservationIdValidationError(false);

    if (
      updatedReservationIdInput.trim() === "" &&
      searchedReservationId !== null
    ) {
      setSearchedReservationId(null);
      setPageIndex(0);
    }
  };

  /**
   * Searches refunds belonging to a registered or guest reservation.
   *
   * Reservation IDs are matched exactly by the backend.
   */
  const handleReservationSearch = (event) => {
    event.preventDefault();

    const normalizedReservationId = reservationIdInput.trim();

    if (!normalizedReservationId) {
      setSearchedReservationId(null);
      setReservationIdValidationError(false);
      setPageIndex(0);
      return;
    }

    const parsedReservationId = Number(normalizedReservationId);

    const isValidReservationId =
      /^\d+$/.test(normalizedReservationId) &&
      Number.isSafeInteger(parsedReservationId) &&
      parsedReservationId > 0;

    if (!isValidReservationId) {
      setReservationIdValidationError(true);
      return;
    }

    setReservationIdValidationError(false);
    setSearchedReservationId(parsedReservationId);
    setPageIndex(0);
  };

  /**
   * Converts the existing one-based pagination component to the
   * zero-based page index required by the backend.
   *
   * @param {number} nextPage - One-based page number.
   */
  const handlePageChange = (nextPage) => {
    if (isFetching || isPlaceholderData) {
      return;
    }

    const nextPageIndex = nextPage - 1;

    if (nextPageIndex < 0 || nextPageIndex >= totalPages) {
      return;
    }

    setPageIndex(nextPageIndex);
  };

  /**
   * Refreshes the currently selected refund history filters.
   */
  const handleRefreshRefundHistory = () => {
    if (!isFetching) {
      void refetch();
    }
  };

  const visibleElementsStart =
    refunds.length > 0 ? pageIndex * REFUNDS_PER_PAGE + 1 : 0;

  const visibleElementsEnd =
    refunds.length > 0 ? visibleElementsStart + refunds.length - 1 : 0;

  return (
    <section
      className="admin-refund-history"
      aria-labelledby="admin-refund-history-title"
    >
      {/* Section heading */}
      <div className="d-flex justify-content-between align-items-start gap-3 mb-4">
        <div>
          <h2 id="admin-refund-history-title" className="fw-bold mb-1">
            {t("admin.refundHistory.title")}
          </h2>

          <p className="text-body-secondary mb-0">
            {t("admin.refundHistory.description")}
          </p>
        </div>

        <button
          type="button"
          className="btn btn-outline-secondary"
          onClick={handleRefreshRefundHistory}
          disabled={isFetching}
          aria-label={t("admin.refundHistory.refresh")}
          title={t("admin.refundHistory.refresh")}
        >
          <FontAwesomeIcon
            icon={faSyncAlt}
            spin={isFetching}
            aria-hidden="true"
          />
        </button>
      </div>

      {/* Global refund summary */}
      <div className="admin-refund-summary mb-4">
        <div className="admin-refund-summary-card">
          <span className="admin-refund-summary-label">
            {t("admin.refundHistory.completed")}
          </span>

          <strong className="admin-refund-summary-value">
            {isLoading ? "—" : summary.completed}
          </strong>
        </div>

        <div className="admin-refund-summary-card">
          <span className="admin-refund-summary-label">
            {t("admin.refundHistory.inProgress")}
          </span>

          <strong className="admin-refund-summary-value">
            {isLoading ? "—" : summary.inProgress}
          </strong>
        </div>

        <div className="admin-refund-summary-card admin-refund-summary-card--attention">
          <span className="admin-refund-summary-label">
            <FontAwesomeIcon
              icon={faExclamationTriangle}
              className="me-2 text-danger"
              aria-hidden="true"
            />

            {t("admin.refundHistory.requiresAttention")}
          </span>

          <strong className="admin-refund-summary-value">
            {isLoading ? "—" : summary.requiresAttention}
          </strong>
        </div>
      </div>

      {/* Server-side filters */}
      <div className="admin-refund-filters mb-4">
        <form
          className="admin-refund-search"
          onSubmit={handleReservationSearch}
          role="search"
        >
          <label
            htmlFor="admin-refund-reservation-search"
            className="visually-hidden"
          >
            {t("admin.refundHistory.searchPlaceholder")}
          </label>

          <div className="input-group">
            <input
              id="admin-refund-reservation-search"
              type="search"
              inputMode="numeric"
              className={`form-control ${
                reservationIdValidationError ? "is-invalid" : ""
              }`}
              value={reservationIdInput}
              onChange={handleReservationIdInputChange}
              placeholder={t("admin.refundHistory.searchPlaceholder")}
              aria-invalid={reservationIdValidationError}
            />

            <button
              type="submit"
              className="btn btn-outline-secondary"
              aria-label={t("admin.refundHistory.search")}
              title={t("admin.refundHistory.search")}
            >
              <FontAwesomeIcon icon={faSearch} aria-hidden="true" />
            </button>
          </div>

          {reservationIdValidationError && (
            <div className="text-danger small mt-2">
              {t("admin.refundHistory.invalidReservationId")}
            </div>
          )}
        </form>

        <div className="admin-refund-status-filter">
          <label
            htmlFor="admin-refund-status-filter"
            className="visually-hidden"
          >
            {t("admin.refundHistory.filterByStatus")}
          </label>

          <select
            id="admin-refund-status-filter"
            className="form-select"
            value={selectedRefundStatus}
            onChange={handleRefundStatusChange}
          >
            <option value="">{t("admin.refundHistory.allStatuses")}</option>

            {REFUND_STATUS_GROUPS.map((statusGroup) => (
              <optgroup
                key={statusGroup.translationKey}
                label={t(`admin.refundHistory.${statusGroup.translationKey}`)}
              >
                {statusGroup.statuses.map((refundStatus) => (
                  <option key={refundStatus} value={refundStatus}>
                    {t(
                      `admin.orderCancellation.refundStatuses.${refundStatus}`,
                      { defaultValue: refundStatus },
                    )}
                  </option>
                ))}
              </optgroup>
            ))}
          </select>
        </div>
      </div>

      {/* API error */}
      {error && (
        <Alert variant="danger" role="alert">
          {t("admin.refundHistory.loadingError")} {error.message}
        </Alert>
      )}

      {/* Paginated refund table */}
      {isLoading ? (
        <div className="text-center py-5">
          <Spinner
            animation="border"
            role="status"
            aria-label={t("admin.refundHistory.loading")}
          />

          <p className="text-body-secondary mt-3">
            {t("admin.refundHistory.loading")}
          </p>
        </div>
      ) : (
        <>
          <div
            className={`rtable-wrap admin-refund-table-wrapper ${
              isFetching ? "admin-refund-table--updating" : ""
            }`}
            aria-busy={isFetching}
          >
            <table className="table table-hover align-middle rtable mb-0 admin-refund-table">
              <thead>
                <tr>
                  <th scope="col">{t("admin.refundHistory.reservation")}</th>

                  <th scope="col">{t("admin.refundHistory.amount")}</th>

                  <th scope="col">{t("admin.refundHistory.status")}</th>

                  <th scope="col" className="text-end">
                    {t("admin.refundHistory.actions")}
                  </th>
                </tr>
              </thead>

              <tbody>
                {refunds.length > 0 ? (
                  refunds.map((refund) => (
                    <RefundHistoryRow
                      key={refund.refundId}
                      refund={refund}
                      onViewRefundDetails={handleOpenRefundDetails}
                      isUpdating={isFetching}
                    />
                  ))
                ) : (
                  <tr>
                    <td colSpan={4} className="rtable-empty text-center py-5">
                      <FontAwesomeIcon
                        icon={faInfoCircle}
                        className="text-body-secondary mb-2"
                        size="lg"
                        aria-hidden="true"
                      />

                      <p className="text-body-secondary mb-0">
                        {t("admin.refundHistory.noResults")}
                      </p>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          {!isPlaceholderData && (
            <div className="mt-3">
              <p className="text-center text-body-secondary small mb-0">
                {t("admin.refundHistory.resultCount", {
                  start: visibleElementsStart,
                  end: visibleElementsEnd,
                  total: totalElements,
                })}
              </p>

              <PaginationControl
                currentPage={pageIndex + 1}
                totalPages={totalPages}
                onPageChange={handlePageChange}
              />
            </div>
          )}
        </>
      )}

      {/* Selected refund details */}
      <AdminRefundDetailsModal
        show={selectedRefund !== null}
        onHide={handleCloseRefundDetails}
        refund={selectedRefund}
        onRefundUpdated={handleRefundDetailsRefreshed}
      />
    </section>
  );
};

export default AdminRefundHistory;
