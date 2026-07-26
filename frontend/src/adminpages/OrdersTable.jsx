import { memo } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPen, faTrashAlt } from "@fortawesome/free-solid-svg-icons";
import { useNavigate } from "react-router-dom";
import { Alert } from "react-bootstrap";
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
import { useTranslation } from "react-i18next";

const orderFieldsHeaders = [
  "admin.orders.id",
  "admin.orders.firstname",
  "admin.orders.lastname",
  "admin.orders.email",
  "admin.orders.service",
  "admin.orders.cost",
  "admin.orders.orderDate",
  "admin.orders.visitDate",
  "admin.orders.status",
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
  "status",
  "paymentMethod",
  "paymentStatus",
];

const OrderRow = memo(function OrderRow({
  order,
  onEdit,
  onDelete,
  isDeleting,
}) {
  const { t } = useTranslation();

  return (
    <tr>
      {orderFields.map((field) => (
        <td key={field} className="align-middle text-center">
          {getNestedValue(order, field)}
          {field === "offer.cost" ? ` ${t("common.currency")}` : ""}
        </td>
      ))}
      <td className="align-middle text-center">
        <div className="d-flex justify-content-center">
          <button
            className="btn btn-warning btn-sm me-2"
            style={{ minWidth: "40px" }}
            title={t("admin.common.edit")}
            onClick={() => onEdit(order)}
          >
            <FontAwesomeIcon icon={faPen} />
          </button>
          <button
            className="btn btn-danger btn-sm"
            style={{ minWidth: "40px" }}
            title={t("admin.common.delete")}
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
    } ${order.orderDate} ${order.visitDate} ${order.status} ${
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
  } = useDeleteModal((item) => onDeleteOrder(item.idOrder), refetch);

  {
    /* handler */
  }
  const handleEditClick = (order) => {
    navigate(`/adminpanel/editorder/${order.idOrder}`, {
      state: { orderData: order },
    });
  };

  if (isLoading) return <LoadingSpinner text={t("admin.orders.loading")} />;

  if (error) return <Alert variant="danger">{error}</Alert>;

  return (
    <div className="container my-5 py-4 text-center">
      <h2 className="mb-4">{t("admin.orders.title")}</h2>

      {/* search box */}
      <SearchBox
        value={searchTerm}
        onChange={handleSearchChange}
        placeholder={t("admin.orders.searchPlaceholder")}
      />

      {/* table */}
      <div className="table-responsive">
        <table
          className="table table-bordered table-hover shadow rounded mx-auto"
          style={{ maxWidth: "1100px" }}
        >
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
                  className="text-center py-4"
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
