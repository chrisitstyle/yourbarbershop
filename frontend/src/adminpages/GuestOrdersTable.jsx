import { memo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faBan,
  faEye,
  faPen,
  faSpinner,
  faTrashAlt,
} from "@fortawesome/free-solid-svg-icons";
import { Alert } from "react-bootstrap";
import { toast } from "sonner";
import { useTranslation } from "react-i18next";

import useGuestOrders from "../hooks/useGuestOrders";
import useTableData from "../hooks/useTableData";
import useSortableData from "../hooks/useSortableData";
import useDeleteModal from "../hooks/useDeleteModal";
import useAdminReservationRefundDetails from "../hooks/useAdminReservationRefundDetails";

import { getNestedValue } from "../utils/tableHelpers";
import LoadingSpinner from "../components/common/LoadingSpinner";
import ConfirmDeleteModal from "../components/common/ConfirmDeleteModal";
import PaginationControl from "../components/common/PaginationControl";
import SearchBox from "../components/common/SearchBox";
import SortableTableHeader from "../components/SortableTableHeader";
import { StatusBadge } from "../components/common/StatusBadge";

import AdminOrderCancellationModal from "./AdminOrderCancellationModal";
import AdminRefundDetailsModal from "./AdminRefundDetailsModal";
import { getOrderModificationRules } from "./utils/orderModificationRules";
import { canAdminCancelReservation } from "./utils/adminOrderCancellationRules";

import "./styles/AdminTables.css";

const guestOrderFieldsHeaders = [
  "admin.guestOrders.id",
  "admin.guestOrders.firstname",
  "admin.guestOrders.lastname",
  "admin.guestOrders.phone",
  "admin.guestOrders.email",
  "admin.guestOrders.service",
  "admin.guestOrders.cost",
  "admin.guestOrders.orderDate",
  "admin.guestOrders.visitDate",
  "admin.guestOrders.orderStatus",
  "admin.guestOrders.paymentMethod",
  "admin.guestOrders.paymentStatus",
];

const guestOrderFields = [
  "idGuestOrder",
  "firstname",
  "lastname",
  "phonenumber",
  "email",
  "offer.kind",
  "offer.cost",
  "orderDate",
  "visitDate",
  "orderStatus",
  "paymentMethod",
  "paymentStatus",
];

const STATUS_FIELDS = new Set(["orderStatus", "paymentStatus"]);

/**
 * Displays a guest reservation and its available
 * administrative actions.
 *
 * @param {object} props - Component properties.
 * @param {object} props.guestOrder - Guest reservation.
 * @param {Function} props.onEdit - Opens the reservation editor.
 * @param {Function} props.onDelete - Opens the deletion confirmation.
 * @param {Function} props.onCancel - Opens the cancellation confirmation.
 * @param {Function} props.onViewRefund - Finds refund details for the reservation.
 * @param {boolean} props.isLookingUpRefund - Whether a refund lookup is active.
 * @param {Function} props.isLookingUpRefundFor - Identifies the refund lookup target.
 * @param {boolean} props.isDeleting - Whether the reservation is being deleted.
 * @param {string[]} props.headerLabels - Translated table header labels.
 */
const GuestOrderRow = memo(function GuestOrderRow({
  guestOrder,
  onEdit,
  onDelete,
  onCancel,
  onViewRefund,
  isLookingUpRefund,
  isLookingUpRefundFor,
  isDeleting,
  headerLabels,
}) {
  const { t } = useTranslation();

  const { isTerminalOrder } = getOrderModificationRules(guestOrder);

  const cancellationAllowed = canAdminCancelReservation(guestOrder);

  const hasPotentialOnlineRefund =
    guestOrder.orderStatus === "ANULOWANE" &&
    guestOrder.paymentMethod === "KARTA_ONLINE";

  const editButtonTitle = isTerminalOrder
    ? t("admin.orderRules.terminal")
    : t("admin.common.edit");

  const cancellationButtonTitle = cancellationAllowed
    ? t("admin.orderCancellation.cancelReservation")
    : t("admin.orderCancellation.cancellationNotAllowed");

  return (
    <tr>
      {guestOrderFields.map((field, index) => {
        const fieldValue = getNestedValue(guestOrder, field);

        return (
          <td
            key={field}
            className="align-middle text-center"
            data-label={headerLabels[index]}
          >
            {STATUS_FIELDS.has(field) ? (
              <StatusBadge value={fieldValue} />
            ) : (
              <>
                {fieldValue}

                {field === "offer.cost" ? ` ${t("common.currency")}` : ""}
              </>
            )}
          </td>
        );
      })}

      <td
        className="align-middle text-center rtable-actions"
        data-label={t("admin.common.action")}
      >
        <div className="d-flex justify-content-center align-items-center flex-wrap gap-2">
          <span title={editButtonTitle}>
            <button
              type="button"
              className="btn btn-warning btn-sm"
              style={{ minWidth: "40px" }}
              aria-label={editButtonTitle}
              onClick={() => onEdit(guestOrder)}
              disabled={isTerminalOrder || isDeleting}
            >
              <FontAwesomeIcon icon={faPen} />
            </button>
          </span>

          {guestOrder.orderStatus === "NOWE" && (
            <span title={cancellationButtonTitle}>
              <button
                type="button"
                className="btn btn-outline-danger btn-sm"
                style={{ minWidth: "40px" }}
                aria-label={t("admin.orderCancellation.cancelReservation")}
                onClick={() => onCancel(guestOrder)}
                disabled={!cancellationAllowed || isDeleting}
              >
                <FontAwesomeIcon icon={faBan} />
              </button>
            </span>
          )}

          {/* Refund details for cancelled online payments */}
          {hasPotentialOnlineRefund && (
            <span title={t("admin.refundHistory.viewReservationRefund")}>
              <button
                type="button"
                className="btn btn-outline-info btn-sm"
                style={{ minWidth: "40px" }}
                aria-label={t("admin.refundHistory.viewReservationRefund")}
                onClick={() => onViewRefund(guestOrder.idGuestOrder, "GUEST")}
                disabled={isDeleting || isLookingUpRefund}
              >
                <FontAwesomeIcon
                  icon={
                    isLookingUpRefundFor(guestOrder.idGuestOrder, "GUEST")
                      ? faSpinner
                      : faEye
                  }
                  spin={isLookingUpRefundFor(guestOrder.idGuestOrder, "GUEST")}
                  aria-hidden="true"
                />
              </button>
            </span>
          )}

          <button
            type="button"
            className="btn btn-danger btn-sm"
            style={{ minWidth: "40px" }}
            title={t("admin.common.delete")}
            aria-label={t("admin.common.delete")}
            onClick={() => onDelete(guestOrder)}
            disabled={isDeleting}
          >
            <FontAwesomeIcon icon={faTrashAlt} />
          </button>
        </div>
      </td>
    </tr>
  );
});

/**
 * Displays guest reservations and provides editing, deletion,
 * administrative cancellation, and refund details actions.
 *
 * @param {object} props - Component properties.
 * @param {Function} props.onDeleteGuestOrder - Deletes a guest reservation.
 */
const GuestOrdersTable = ({ onDeleteGuestOrder }) => {
  const { guestOrders, isLoading, error, refetch } = useGuestOrders();

  const navigate = useNavigate();
  const { t } = useTranslation();

  const [
    selectedGuestOrderForCancellation,
    setSelectedGuestOrderForCancellation,
  ] = useState(null);

  const {
    selectedRefund,
    openRefundDetails,
    closeRefundDetails,
    handleRefundUpdated,
    isLookingUpRefund,
    isLookingUpRefundFor,
  } = useAdminReservationRefundDetails();

  const headerLabels = guestOrderFieldsHeaders.map((translationKey) =>
    t(translationKey),
  );

  /**
   * Filters guest reservations using their displayed details.
   */
  const filterGuestOrders = (guestOrder, searchTerm) => {
    const searchableGuestOrderDetails = `
      ${guestOrder.idGuestOrder}
      ${guestOrder.firstname || ""}
      ${guestOrder.lastname || ""}
      ${guestOrder.email || ""}
      ${guestOrder.phonenumber || ""}
      ${guestOrder.offer?.kind || ""}
      ${guestOrder.offer?.cost || ""}
      ${guestOrder.orderDate || ""}
      ${guestOrder.visitDate || ""}
      ${guestOrder.orderStatus || ""}
      ${guestOrder.paymentMethod || ""}
      ${guestOrder.paymentStatus || ""}
    `;

    return searchableGuestOrderDetails
      .toLowerCase()
      .includes(searchTerm.toLowerCase());
  };

  const availableGuestOrders = Array.isArray(guestOrders) ? guestOrders : [];

  const { sortedData, sortConfig, handleSort } = useSortableData(
    availableGuestOrders,
    {
      field: "visitDate",
      direction: "desc",
    },
  );

  const {
    searchTerm,
    handleSearchChange,
    currentData,
    currentPage,
    totalPages,
    setCurrentPage,
  } = useTableData(sortedData, filterGuestOrders);

  const handleHeaderSort = (field) => {
    handleSort(field);
    setCurrentPage(1);
  };

  const {
    show: showDeleteModal,
    setShow: setShowDeleteModal,
    itemToDelete: guestOrderToDelete,
    askDelete: handleAskDelete,
    confirmDelete,
    isDeleting,
  } = useDeleteModal(async (guestOrder) => {
    try {
      await onDeleteGuestOrder(guestOrder.idGuestOrder);

      toast.success(t("admin.messages.deleteGuestOrderSuccess"));
    } catch (deletionError) {
      console.error("Failed to delete guest reservation:", deletionError);

      const errorMessage = deletionError?.data || deletionError?.message;

      if (typeof errorMessage === "string" && errorMessage.trim()) {
        toast.error(errorMessage);
      } else {
        toast.error(t("admin.messages.deleteGuestOrderError"));
      }
    }
  }, refetch);

  /**
   * Opens the guest reservation editor when editing is allowed.
   */
  const handleEditGuestOrder = (guestOrder) => {
    const { isTerminalOrder } = getOrderModificationRules(guestOrder);

    if (isTerminalOrder) {
      toast.error(t("admin.orderRules.terminal"));
      return;
    }

    navigate(`/adminpanel/editguestorder/${guestOrder.idGuestOrder}`, {
      state: {
        guestOrderData: guestOrder,
      },
    });
  };

  /**
   * Opens the administrative cancellation confirmation modal.
   *
   * The backend decides whether the reservation requires
   * a Stripe refund.
   */
  const handleOpenCancellationModal = (guestOrder) => {
    if (!canAdminCancelReservation(guestOrder)) {
      toast.error(t("admin.orderCancellation.cancellationNotAllowed"));
      return;
    }

    setSelectedGuestOrderForCancellation(guestOrder);
  };

  /**
   * Closes the cancellation modal and clears the selected reservation.
   */
  const handleCloseCancellationModal = () => {
    setSelectedGuestOrderForCancellation(null);
  };

  if (isLoading) {
    return <LoadingSpinner text={t("admin.guestOrders.loading")} />;
  }

  if (error) {
    return <Alert variant="danger">{error}</Alert>;
  }

  return (
    <div className="container my-5 py-4 text-center">
      <h2 className="mb-4">{t("admin.guestOrders.title")}</h2>

      {/* Search box */}
      <SearchBox
        value={searchTerm}
        onChange={handleSearchChange}
        placeholder={t("admin.guestOrders.searchPlaceholder")}
      />

      {/* Responsive reservation table */}
      <div
        className="rtable-wrap shadow-sm rounded mx-auto"
        style={{ maxWidth: "1200px" }}
      >
        <table className="table table-hover align-middle mb-0 rtable">
          <SortableTableHeader
            headers={guestOrderFieldsHeaders}
            fields={guestOrderFields}
            sortConfig={sortConfig}
            onSort={handleHeaderSort}
          >
            <th className="text-center align-middle">
              {t("admin.common.action")}
            </th>
          </SortableTableHeader>

          <tbody>
            {currentData.length > 0 ? (
              currentData.map((guestOrder) => (
                <GuestOrderRow
                  key={guestOrder.idGuestOrder}
                  guestOrder={guestOrder}
                  onEdit={handleEditGuestOrder}
                  onDelete={handleAskDelete}
                  onCancel={handleOpenCancellationModal}
                  onViewRefund={openRefundDetails}
                  isLookingUpRefund={isLookingUpRefund}
                  isLookingUpRefundFor={isLookingUpRefundFor}
                  headerLabels={headerLabels}
                  isDeleting={
                    isDeleting &&
                    guestOrderToDelete?.idGuestOrder === guestOrder.idGuestOrder
                  }
                />
              ))
            ) : (
              <tr>
                <td
                  colSpan={guestOrderFields.length + 1}
                  className="text-center py-4 rtable-empty"
                >
                  <Alert variant="info" className="mb-0">
                    {t("admin.common.noResults")}
                  </Alert>
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      <PaginationControl
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
      />

      {/* Guest reservation deletion */}
      <ConfirmDeleteModal
        show={showDeleteModal}
        onHide={() => setShowDeleteModal(false)}
        onConfirm={confirmDelete}
        itemName={
          guestOrderToDelete
            ? `${
                guestOrderToDelete.offer?.kind ?? t("admin.common.none")
              } (${guestOrderToDelete.idGuestOrder})`
            : ""
        }
        label={t("admin.guestOrders.deleteLabel")}
      />

      {/* Administrative cancellation */}
      <AdminOrderCancellationModal
        show={selectedGuestOrderForCancellation !== null}
        onHide={handleCloseCancellationModal}
        reservation={selectedGuestOrderForCancellation}
        reservationType="GUEST"
        onCancellationAttemptFinished={refetch}
      />

      {/* Existing read-only refund details modal */}
      <AdminRefundDetailsModal
        show={selectedRefund !== null}
        onHide={closeRefundDetails}
        refund={selectedRefund}
        onRefundUpdated={handleRefundUpdated}
      />
    </div>
  );
};

export default GuestOrdersTable;
