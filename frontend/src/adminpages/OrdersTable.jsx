import { memo } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPen, faTrashAlt } from "@fortawesome/free-solid-svg-icons";
import { useNavigate } from "react-router-dom";
import { Alert } from "react-bootstrap";
import { useAuth } from "../AuthContext";
import useOrders from "../hooks/useOrders";
import useTableData from "../hooks/useTableData";
import useDeleteModal from "../hooks/useDeleteModal";
import { getNestedValue } from "../utils/tableHelpers";
import LoadingSpinner from "../components/common/LoadingSpinner";
import ConfirmDeleteModal from "../components/common/ConfirmDeleteModal";
import PaginationControl from "../components/common/PaginationControl";
import SearchBox from "../components/common/SearchBox";

const orderFieldsHeaders = [
  "Identyfikator wizyty",
  "Imię",
  "Nazwisko",
  "Email",
  "Usługa",
  "Koszt",
  "Data zamówienia",
  "Data wizyty",
  "Status",
];

const orderFields = [
  "idOrder",
  "user.firstname",
  "user.lastname",
  "user.username",
  "offer.kind",
  "offer.cost",
  "orderDate",
  "visitDate",
  "status",
];

const OrderRow = memo(function OrderRow({
  order,
  onEdit,
  onDelete,
  isDeleting,
}) {
  return (
    <tr>
      {orderFields.map((field) => (
        <td key={field} className="align-middle text-center">
          {getNestedValue(order, field)}
        </td>
      ))}
      <td className="align-middle text-center">
        <div className="d-flex justify-content-center">
          <button
            className="btn btn-warning btn-sm me-2"
            style={{ minWidth: "40px" }}
            title="Edytuj"
            onClick={() => onEdit(order)}
          >
            <FontAwesomeIcon icon={faPen} />
          </button>
          <button
            className="btn btn-danger btn-sm"
            style={{ minWidth: "40px" }}
            title="Usuń"
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
  const { user } = useAuth();
  const { orders, isLoading, error, refetch } = useOrders(user?.token);
  const navigate = useNavigate();

  const filterOrders = (order, term) => {
    const searchStr = ` ${order.idOrder} ${order.user?.firstname} ${order.user?.lastname} ${order.user?.username} ${order.offer?.kind} ${order.offer?.cost} ${order.orderDate} ${order.visitDate} ${order.status}`;
    return searchStr.toLowerCase().includes(term.toLowerCase());
  };

  const {
    searchTerm,
    handleSearchChange,
    currentData,
    currentPage,
    totalPages,
    setCurrentPage,
  } = useTableData(orders, filterOrders);

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

  if (isLoading) return <LoadingSpinner text="Ładowanie zamówień..." />;
  if (error) return <Alert variant="danger">{error}</Alert>;

  return (
    <div className="container my-5 py-4 text-center">
      <h2 className="mb-4">Wizyty użytkowników</h2>

      {/* search box*/}
      <SearchBox
        value={searchTerm}
        onChange={handleSearchChange}
        placeholder="Szukaj wizyty..."
      />

      {/* table */}
      <div className="table-responsive">
        <table
          className="table table-bordered table-hover shadow rounded mx-auto"
          style={{ maxWidth: "1100px" }}
        >
          <thead className="table-dark">
            <tr>
              {orderFieldsHeaders.map((header) => (
                <th key={header} className="text-center align-middle">
                  {header}
                </th>
              ))}
              <th className="text-center align-middle">Akcja</th>
            </tr>
          </thead>
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
                    Brak wyników.
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
            ? `${orderToDelete.offer?.kind || "brak"} (${
                orderToDelete.idOrder
              })`
            : ""
        }
        label="wizytę"
      />
    </div>
  );
};

export default OrdersTable;
