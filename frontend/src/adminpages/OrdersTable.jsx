import { memo, useState } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faBan, faPen, faTrashAlt } from "@fortawesome/free-solid-svg-icons";
import { useNavigate } from "react-router-dom";
import { Alert } from "react-bootstrap";
import { toast } from "sonner";
import { useTranslation } from "react-i18next";

import useOrders from "../hooks/useOrders";
import useTableData from "../hooks/useTableData";
import useSortableData from "../hooks/useSortableData";
import useDeleteModal from "../hooks/useDeleteModal";

import { getNestedValue } from "../utils/tableHelpers";
import LoadingSpinner from "../components/common/LoadingSpinner";
import ConfirmDeleteModal from "../components/common/ConfirmDeleteModal";
import PaginationControl from "../components/common/PaginationControl";
import SearchBox from "../components/common/SearchBox";
import SortableTableHeader from "../components/SortableTableHeader";
import { StatusBadge } from "../components/common/StatusBadge";

import AdminOrderCancellationModal from "./AdminOrderCancellationModal";
import { getOrderModificationRules } from "./utils/orderModificationRules";
import { canAdminCancelReservation } from "./utils/adminOrderCancellationRules";

import "./styles/AdminTables.css";

const orderFieldsHeaders = [
  "admin.orders.id",
  "admin.orders.firstname",
  "admin.orders.lastname",
  "admin.orders.email",
  "admin.orders.service",
  "admin.orders.cost",
  "admin.orders.orderDate",
  "admin.orders.visitDate",
  "admin.orders.orderStatus",
  "admin.orders.paymentMethod",
  "admin.orders.paymentStatus",
];

const orderFields = [
  "idOrder",
  "user.firstname",
  "user.lastname",
  "user.email",
  "offer.kind",
  "offer.cost",
  "orderDate",
  "visitDate",
  "orderStatus",
  "paymentMethod",
  "paymentStatus",
];

const BADGE_FIELDS = new Set(["orderStatus", "paymentStatus"]);

const fieldLabels = orderFields.reduce((fieldLabelsByName, field, index) => {
  fieldLabelsByName[field] = orderFieldsHeaders[index];
  return fieldLabelsByName;
}, {});

/**
 * Renders a registered customer's reservation and available
 * administrative actions.
 *
 * @param {object} props - Component properties.
 * @param {object} props.order - Registered customer's reservation.
 * @param {Function} props.onEdit - Opens the reservation editor.
 * @param {Function} props.onDelete - Opens the deletion confirmation.
 * @param {Function} props.onCancel - Opens the cancellation confirmation.
 * @param {boolean} props.isDeleting - Whether this reservation is being deleted.
 */
const OrderRow = memo(function OrderRow({
  order,
  onEdit,
  onDelete,
  onCancel,
  isDeleting,
}) {
  const { t } = useTranslation();

  const { isTerminalOrder } = getOrderModificationRules(order);

  const cancellationAllowed = canAdminCancelReservation(order);

  const editButtonTitle = isTerminalOrder
    ? t("admin.orderRules.terminal")
    : t("admin.common.edit");

  const cancellationButtonTitle = cancellationAllowed
    ? t("admin.orderCancellation.cancelReservation")
    : t("admin.orderCancellation.cancellationNotAllowed");

  return (
    <tr>
      {orderFields.map((field) => {
        const fieldValue = getNestedValue(order, field);

        return (
          <td
            key={field}
            data-label={t(fieldLabels[field])}
            className="align-middle text-center"
          >
            {BADGE_FIELDS.has(field) ? (
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

      <td className="align-middle text-center rtable-actions">
        <div className="d-flex justify-content-center align-items-center flex-wrap gap-2">
          <span title={editButtonTitle}>
            <button
              type="button"
              className="btn btn-warning btn-sm"
              style={{ minWidth: "40px" }}
              aria-label={editButtonTitle}
              onClick={() => onEdit(order)}
              disabled={isTerminalOrder || isDeleting}
            >
              <FontAwesomeIcon icon={faPen} />
            </button>
          </span>

          {order.orderStatus === "NOWE" && (
            <span title={cancellationButtonTitle}>
              <button
                type="button"
                className="btn btn-outline-danger btn-sm"
                style={{ minWidth: "40px" }}
                aria-label={t("admin.orderCancellation.cancelReservation")}
                onClick={() => onCancel(order)}
                disabled={!cancellationAllowed || isDeleting}
              >
                <FontAwesomeIcon icon={faBan} />
              </button>
            </span>
          )}

          <button
            type="button"
            className="btn btn-danger btn-sm"
            style={{ minWidth: "40px" }}
            title={t("admin.common.delete")}
            aria-label={t("admin.common.delete")}
            onClick={() => onDelete(order)}
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
 * Displays registered customer reservations and provides
 * editing, deletion, and administrative cancellation actions.
 *
 * @param {object} props - Component properties.
 * @param {Function} props.onDeleteOrder - Deletes a reservation.
 */
const OrdersTable = ({ onDeleteOrder }) => {
  const { orders, isLoading, error, refetch } = useOrders();

  const navigate = useNavigate();
  const { t } = useTranslation();

  const [selectedOrderForCancellation, setSelectedOrderForCancellation] =
    useState(null);

  /**
   * Filters reservations using their displayed fields.
   */
  const filterOrders = (order, searchTerm) => {
    const searchableOrderDetails = `
      ${order.idOrder}
      ${order.user?.firstname || ""}
      ${order.user?.lastname || ""}
      ${order.user?.email || ""}
      ${order.offer?.kind || ""}
      ${order.offer?.cost || ""}
      ${order.orderDate}
      ${order.visitDate}
      ${order.orderStatus}
      ${order.paymentMethod || ""}
      ${order.paymentStatus || ""}
    `;

    return searchableOrderDetails
      .toLowerCase()
      .includes(searchTerm.toLowerCase());
  };

  const availableOrders = Array.isArray(orders) ? orders : [];

  const { sortedData, sortConfig, handleSort } = useSortableData(
    availableOrders,
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
  } = useTableData(sortedData, filterOrders);

  const handleHeaderSort = (field) => {
    handleSort(field);
    setCurrentPage(1);
  };

  const {
    show: showDeleteModal,
    setShow: setShowDeleteModal,
    itemToDelete: orderToDelete,
    askDelete: handleAskDelete,
    confirmDelete,
    isDeleting,
  } = useDeleteModal(async (order) => {
    try {
      await onDeleteOrder(order.idOrder);

      toast.success(
        t("admin.messages.deleteOrderSuccess", "Pomyślnie usunięto wizytę."),
      );
    } catch (deletionError) {
      console.error("Failed to delete registered order:", deletionError);

      const errorMessage = deletionError?.data || deletionError?.message;

      if (typeof errorMessage === "string" && errorMessage.trim()) {
        toast.error(errorMessage);
      } else {
        toast.error(t("admin.messages.deleteOrderError"));
      }
    }
  }, refetch);

  /**
   * Navigates to the reservation editor when editing is allowed.
   */
  const handleEditOrder = (order) => {
    const { isTerminalOrder } = getOrderModificationRules(order);

    if (isTerminalOrder) {
      toast.error(t("admin.orderRules.terminal"));
      return;
    }

    navigate(`/adminpanel/editorder/${order.idOrder}`, {
      state: { orderData: order },
    });
  };

  /**
   * Opens the administrative cancellation confirmation.
   *
   * The actual cancellation request is performed only after
   * the administrator confirms the action inside the modal.
   */
  const handleOpenCancellationModal = (order) => {
    if (!canAdminCancelReservation(order)) {
      toast.error(t("admin.orderCancellation.cancellationNotAllowed"));
      return;
    }

    setSelectedOrderForCancellation(order);
  };

  /**
   * Closes the cancellation modal and clears the selected order.
   */
  const handleCloseCancellationModal = () => {
    setSelectedOrderForCancellation(null);
  };

  if (isLoading) {
    return <LoadingSpinner text={t("admin.orders.loading")} />;
  }

  if (error) {
    return <Alert variant="danger">{error}</Alert>;
  }

  return (
    <div className="container my-5 py-4 text-center">
      <h2 className="mb-4">{t("admin.orders.title")}</h2>

      {/* search box */}
      <SearchBox
        value={searchTerm}
        onChange={handleSearchChange}
        placeholder={t("admin.orders.searchPlaceholder")}
      />

      {/* table (rtable-wrap + rtable enable the responsive card view) */}
      <div className="rtable-wrap mx-auto" style={{ maxWidth: "1100px" }}>
        <table className="table table-hover align-middle rtable mb-0">
          <SortableTableHeader
            headers={orderFieldsHeaders}
            fields={orderFields}
            sortConfig={sortConfig}
            onSort={handleHeaderSort}
          >
            <th className="text-center align-middle">
              {t("admin.common.action")}
            </th>
          </SortableTableHeader>

          <tbody>
            {currentData.length > 0 ? (
              currentData.map((order) => (
                <OrderRow
                  key={order.idOrder}
                  order={order}
                  onEdit={handleEditOrder}
                  onDelete={handleAskDelete}
                  onCancel={handleOpenCancellationModal}
                  isDeleting={
                    isDeleting && orderToDelete?.idOrder === order.idOrder
                  }
                />
              ))
            ) : (
              <tr>
                <td
                  colSpan={orderFields.length + 1}
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

      {/* pagination component */}
      <PaginationControl
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
      />

      <ConfirmDeleteModal
        show={showDeleteModal}
        onHide={() => setShowDeleteModal(false)}
        onConfirm={confirmDelete}
        itemName={
          orderToDelete
            ? `${orderToDelete.offer?.kind || t("admin.common.none")} (${
                orderToDelete.idOrder
              })`
            : ""
        }
        label={t("admin.orders.deleteLabel")}
      />

      <AdminOrderCancellationModal
        show={selectedOrderForCancellation !== null}
        onHide={handleCloseCancellationModal}
        reservation={selectedOrderForCancellation}
        reservationType="REGISTERED"
        onCancellationAttemptFinished={refetch}
      />
    </div>
  );
};

export default OrdersTable;
