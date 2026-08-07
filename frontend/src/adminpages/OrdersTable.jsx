import { memo } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPen, faTrashAlt } from "@fortawesome/free-solid-svg-icons";
import { useNavigate } from "react-router-dom";
import { Alert } from "react-bootstrap";
import { toast } from "sonner";
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
import { useTranslation } from "react-i18next";
import { getOrderModificationRules } from "./utils/orderModificationRules";
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

// fields that render as a colored pill instead of plain text
const BADGE_FIELDS = new Set(["orderStatus", "paymentStatus"]);

// map field -> header key so mobile cards can render a label per cell
const fieldLabels = orderFields.reduce((acc, field, idx) => {
  acc[field] = orderFieldsHeaders[idx];
  return acc;
}, {});

const OrderRow = memo(function OrderRow({
  order,
  onEdit,
  onDelete,
  isDeleting,
}) {
  const { t } = useTranslation();

  const { isTerminalOrder } = getOrderModificationRules(order);

  const editButtonTitle = isTerminalOrder
    ? t("admin.orderRules.terminal")
    : t("admin.common.edit");

  return (
    <tr>
      {orderFields.map((field) => {
        const value = getNestedValue(order, field);

        return (
          <td
            key={field}
            data-label={t(fieldLabels[field])}
            className="align-middle text-center"
          >
            {BADGE_FIELDS.has(field) ? (
              <StatusBadge value={value} />
            ) : (
              <>
                {value}
                {field === "offer.cost" ? ` ${t("common.currency")}` : ""}
              </>
            )}
          </td>
        );
      })}

      <td className="align-middle text-center rtable-actions">
        <div className="d-flex justify-content-center">
          <span title={editButtonTitle}>
            <button
              type="button"
              className="btn btn-warning btn-sm me-2"
              style={{ minWidth: "40px" }}
              aria-label={editButtonTitle}
              onClick={() => onEdit(order)}
              disabled={isTerminalOrder}
            >
              <FontAwesomeIcon icon={faPen} />
            </button>
          </span>

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

const OrdersTable = ({ onDeleteOrder }) => {
  const { orders, isLoading, error, refetch } = useOrders();
  const navigate = useNavigate();
  const { t } = useTranslation();

  const filterOrders = (order, term) => {
    const searchStr = ` ${order.idOrder} ${order.user?.firstname || ""} ${
      order.user?.lastname || ""
    } ${order.user?.email || ""} ${order.offer?.kind || ""} ${
      order.offer?.cost || ""
    } ${order.orderDate} ${order.visitDate} ${order.orderStatus} ${
      order.paymentMethod || ""
    } ${order.paymentStatus || ""}`;

    return searchStr.toLowerCase().includes(term.toLowerCase());
  };

  const safeOrders = Array.isArray(orders) ? orders : [];

  const { sortedData, sortConfig, handleSort } = useSortableData(safeOrders, {
    field: "visitDate",
    direction: "desc",
  });

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
  } = useDeleteModal(async (item) => {
    try {
      await onDeleteOrder(item.idOrder);

      toast.success(
        t("admin.messages.deleteOrderSuccess", "Pomyślnie usunięto wizytę."),
      );
    } catch (err) {
      console.error("error deleting order:", err);

      const errorMsg = err?.data || err?.message;

      if (typeof errorMsg === "string" && errorMsg) {
        toast.error(errorMsg);
      } else {
        toast.error(t("admin.messages.deleteOrderError"));
      }
    }
  }, refetch);

  const handleEditClick = (order) => {
    const { isTerminalOrder } = getOrderModificationRules(order);

    if (isTerminalOrder) {
      toast.error(t("admin.orderRules.terminal"));
      return;
    }

    navigate(`/adminpanel/editorder/${order.idOrder}`, {
      state: { orderData: order },
    });
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
                  onEdit={handleEditClick}
                  onDelete={handleAskDelete}
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
    </div>
  );
};

export default OrdersTable;
