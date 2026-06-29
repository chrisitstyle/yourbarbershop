import { memo } from "react";
import { useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPen, faTrashAlt } from "@fortawesome/free-solid-svg-icons";
import { useAuth } from "../auth/AuthContextValue";
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

const GuestOrderRow = memo(function GuestOrderRow({
  guestOrder,
  onEdit,
  onDelete,
  isDeleting,
}) {
  const { t } = useTranslation();

  return (
    <tr>
      {guestOrderFields.map((field) => (
        <td key={field} className="align-middle text-center">
          {getNestedValue(guestOrder, field)}
          {field === "offer.cost" ? ` ${t("common.currency")}` : ""}
        </td>
      ))}
      <td className="align-middle text-center">
        <div className="d-flex justify-content-center">
          <button
            className="btn btn-warning btn-sm me-2"
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
  const { user } = useAuth();
  const { guestOrders, isLoading, error, refetch } = useGuestOrders(
    user?.token,
  );
  const navigate = useNavigate();
  const { t } = useTranslation();

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
  } = useDeleteModal((item) => onDeleteGuestOrder(item.idGuestOrder), refetch);

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

      <SearchBox
        value={searchTerm}
        onChange={handleSearchChange}
        placeholder={t("admin.guestOrders.searchPlaceholder")}
      />

      <div className="table-responsive">
        <table
          className="table table-bordered table-hover shadow rounded mx-auto"
          style={{ maxWidth: "1200px" }}
        >
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
