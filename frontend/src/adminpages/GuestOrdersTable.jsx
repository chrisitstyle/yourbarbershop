import { memo } from "react";
import { useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPen, faTrashAlt } from "@fortawesome/free-solid-svg-icons";
import { toast } from "sonner";
import useGuestOrders from "../hooks/useGuestOrders";
import useTableData from "../hooks/useTableData";
import useSortableData from "../hooks/useSortableData";
import useDeleteModal from "../hooks/useDeleteModal";
import { getNestedValue } from "../utils/tableHelpers";
import { Alert } from "react-bootstrap";
import LoadingSpinner from "../components/common/LoadingSpinner";
import ConfirmDeleteModal from "../components/common/ConfirmDeleteModal";
import PaginationControl from "../components/common/PaginationControl";
import SearchBox from "../components/common/SearchBox";
import SortableTableHeader from "../components/SortableTableHeader";
import { useTranslation } from "react-i18next";
import { StatusBadge } from "./utils/adminTableHelpers";
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
  "admin.guestOrders.status",
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
  "status",
  "paymentMethod",
  "paymentStatus",
];

// fields that render as colored status pills instead of plain text
const STATUS_FIELDS = new Set(["status", "paymentStatus"]);

const GuestOrderRow = memo(function GuestOrderRow({
  guestOrder,
  onEdit,
  onDelete,
  isDeleting,
  headerLabels,
}) {
  const { t } = useTranslation();

  return (
    <tr>
      {guestOrderFields.map((field, i) => {
        const value = getNestedValue(guestOrder, field);
        return (
          <td
            key={field}
            className="align-middle text-center"
            data-label={headerLabels[i]}
          >
            {STATUS_FIELDS.has(field) ? (
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
      <td
        className="align-middle text-center"
        data-label={t("admin.common.action")}
      >
        <div className="d-flex justify-content-center gap-2">
          <button
            className="btn btn-warning btn-sm"
            style={{ minWidth: "40px" }}
            title={t("admin.common.edit")}
            onClick={() => onEdit(guestOrder)}
          >
            <FontAwesomeIcon icon={faPen} />
          </button>
          <button
            className="btn btn-danger btn-sm"
            style={{ minWidth: "40px" }}
            title={t("admin.common.delete")}
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

const GuestOrdersTable = ({ onDeleteGuestOrder }) => {
  const { guestOrders, isLoading, error, refetch } = useGuestOrders();
  const navigate = useNavigate();
  const { t } = useTranslation();

  const headerLabels = guestOrderFieldsHeaders.map((key) => t(key));

  const filterGuestOrders = (order, term) => {
    const searchStr = ` ${order.idGuestOrder} ${order.firstname} ${
      order.lastname
    } ${order.email} ${order.offer?.kind || ""} ${order.offer?.cost || ""} ${
      order.phonenumber || ""
    } ${order.orderDate} ${order.visitDate} ${order.status} ${
      order.paymentMethod || ""
    } ${order.paymentStatus || ""}`;

    return searchStr.toLowerCase().includes(term.toLowerCase());
  };

  const safeGuestOrders = Array.isArray(guestOrders) ? guestOrders : [];

  const { sortedData, sortConfig, handleSort } = useSortableData(
    safeGuestOrders,
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
  } = useDeleteModal(async (item) => {
    try {
      await onDeleteGuestOrder(item.idGuestOrder);
      toast.success(
        t(
          "admin.messages.deleteGuestOrderSuccess",
          "Pomyślnie usunięto wizytę gościa.",
        ),
      );
    } catch (err) {
      console.error("error deleting guest order:", err);
      const errorMsg = err?.data || err?.message;

      if (typeof errorMsg === "string" && errorMsg) {
        toast.error(errorMsg);
      } else {
        toast.error(t("admin.messages.deleteGuestOrderError"));
      }
    }
  }, refetch);

  const handleEditClick = (guestOrder) => {
    navigate(`/adminpanel/editguestorder/${guestOrder.idGuestOrder}`, {
      state: { guestOrderData: guestOrder },
    });
  };

  if (isLoading)
    return <LoadingSpinner text={t("admin.guestOrders.loading")} />;

  if (error) return <Alert variant="danger">{error}</Alert>;

  return (
    <div className="container my-5 py-4 text-center">
      <h2 className="mb-4">{t("admin.guestOrders.title")}</h2>

      {/* search box */}
      <SearchBox
        value={searchTerm}
        onChange={handleSearchChange}
        placeholder={t("admin.guestOrders.searchPlaceholder")}
      />

      {/* responsive table -> collapses to cards on mobile */}
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
                  onEdit={handleEditClick}
                  onDelete={handleAskDelete}
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

      {/* pagination control */}
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
          guestOrderToDelete
            ? `${guestOrderToDelete.offer?.kind ?? t("admin.common.none")} (${
                guestOrderToDelete.idGuestOrder
              })`
            : ""
        }
        label={t("admin.guestOrders.deleteLabel")}
      />
    </div>
  );
};

export default GuestOrdersTable;
