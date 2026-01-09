import { useState, useMemo, memo } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faPen,
  faTrashAlt,
  faChevronLeft,
  faChevronRight,
} from "@fortawesome/free-solid-svg-icons";
import { useNavigate } from "react-router-dom";
import { formatDate } from "../api/dataParser";
import { Alert } from "react-bootstrap";
import { useAuth } from "../AuthContext";
import useOrders from "../hooks/useOrders";
import LoadingSpinner from "../components/common/LoadingSpinner";
import ConfirmDeleteModal from "../components/common/ConfirmDeleteModal";

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

function getValue(order, field) {
  if (field === "offer.cost")
    return order.offer ? order.offer.cost + " zł" : "brak";
  if (field === "orderDate")
    return order.orderDate ? formatDate(order.orderDate) : "brak";
  if (field === "visitDate")
    return order.visitDate ? formatDate(order.visitDate) : "brak";
  if (field.includes(".")) {
    const [a, b] = field.split(".");
    return order[a] && order[a][b] ? order[a][b] : "brak";
  }
  return order[field];
}

const OrderRow = memo(function OrderRow({
  order,
  onEdit,
  onDelete,
  deleteLoadingId,
}) {
  return (
    <tr>
      {orderFields.map((field) => (
        <td key={field} className="align-middle text-center">
          {getValue(order, field)}
        </td>
      ))}
      <td className="align-middle text-center">
        <div className="d-flex justify-content-center">
          {/* edit button with tooltip */}
          <button
            className="btn btn-warning btn-sm me-2"
            style={{ minWidth: "40px" }}
            title="Edytuj wizytę"
            onClick={() => onEdit(order)}
          >
            <FontAwesomeIcon icon={faPen} />
          </button>
          {/* delete button with tooltip */}
          <button
            className="btn btn-danger btn-sm"
            style={{ minWidth: "40px" }}
            title="Usuń wizytę"
            onClick={() => onDelete(order)}
            disabled={deleteLoadingId === order.idOrder}
          >
            {deleteLoadingId === order.idOrder ? (
              "Usuwanie..."
            ) : (
              <FontAwesomeIcon icon={faTrashAlt} />
            )}
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
  const ordersPerPage = 10;
  const [currentPage, setCurrentPage] = useState(1);
  const [searchTerm, setSearchTerm] = useState("");
  const [deleteLoadingId, setDeleteLoadingId] = useState(null);

  // modal state for confirmation of deletion
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [orderToDelete, setOrderToDelete] = useState(null);

  const filteredOrders = useMemo(
    () =>
      orders.filter((order) =>
        ` ${order.idOrder} ${order.user.firstname}  ${order.user.lastname}  ${order.user.username} ${order.offer.kind} ${order.offer.cost} ${order.orderDate} ${order.visitDate} ${order.status}`
          .toLowerCase()
          .includes(searchTerm.toLowerCase())
      ),
    [orders, searchTerm]
  );

  const totalPages = useMemo(
    () => Math.ceil(filteredOrders.length / ordersPerPage),
    [filteredOrders.length, ordersPerPage]
  );

  const currentData = useMemo(
    () =>
      filteredOrders.slice(
        (currentPage - 1) * ordersPerPage,
        currentPage * ordersPerPage
      ),
    [filteredOrders, currentPage, ordersPerPage]
  );

  // handlers
  const handlePageClick = (page) => {
    setCurrentPage(page);
  };

  const handleEditClick = (order) => {
    navigate(`/adminpanel/editorder/${order.idOrder}`, {
      state: { orderData: order },
    });
  };

  const handleAskDeleteOrder = (order) => {
    setOrderToDelete(order);
    setShowDeleteModal(true);
  };

  const confirmDeleteOrder = async () => {
    if (orderToDelete) {
      setDeleteLoadingId(orderToDelete.idOrder);
      try {
        await onDeleteOrder(orderToDelete.idOrder);
        await refetch();
      } catch (err) {
        console.error(err);
      } finally {
        setDeleteLoadingId(null);
        setShowDeleteModal(false);
        setOrderToDelete(null);
      }
    }
  };

  if (isLoading) return <LoadingSpinner text="Ładowanie zamówień..." />;
  if (error) return <Alert variant="danger">{error}</Alert>;

  return (
    <div className="container my-5 py-4 text-center">
      <h2 className="mb-4">Wizyty użytkowników</h2>
      {/* search field */}
      <div className="mb-3">
        <input
          type="text"
          placeholder="Szukaj wizyty..."
          value={searchTerm}
          onChange={(e) => {
            setSearchTerm(e.target.value);
            setCurrentPage(1);
          }}
          className="form-control mx-auto"
          style={{ width: "300px", fontSize: "1rem" }}
        />
      </div>
      <div className="table-responsive">
        <table
          className="table table-bordered table-hover shadow rounded mx-auto"
          style={{ maxWidth: "1100px" }}
        >
          <thead className="table-dark">
            <tr>
              {orderFieldsHeaders.map((header, idx) => (
                <th key={orderFields[idx]} className="text-center align-middle">
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
                  onDelete={handleAskDeleteOrder}
                  deleteLoadingId={deleteLoadingId}
                />
              ))
            ) : (
              <tr>
                <td
                  colSpan={orderFields.length + 1}
                  className="text-center py-4"
                >
                  <Alert variant="info" className="mb-0">
                    Brak wyników do wyświetlenia.
                  </Alert>
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      {totalPages > 1 && (
        <nav className="pagination justify-content-center mt-4">
          <ul className="pagination">
            {/* previous button */}
            <li className={`page-item ${currentPage === 1 ? "disabled" : ""}`}>
              <button
                className="page-link"
                onClick={() => handlePageClick(currentPage - 1)}
                disabled={currentPage === 1}
                aria-label="Poprzednia"
                style={{ minWidth: "38px" }}
              >
                <FontAwesomeIcon icon={faChevronLeft} />
              </button>
            </li>
            {/* page numbers */}
            {[...Array(totalPages)].map((_, index) => (
              <li
                key={index + 1}
                className={`page-item ${
                  index + 1 === currentPage ? "active" : ""
                }`}
              >
                <button
                  className="page-link"
                  onClick={() => handlePageClick(index + 1)}
                  style={{ minWidth: "38px" }}
                >
                  {index + 1}
                </button>
              </li>
            ))}
            {/* next button */}
            <li
              className={`page-item ${
                currentPage === totalPages ? "disabled" : ""
              }`}
            >
              <button
                className="page-link"
                onClick={() => handlePageClick(currentPage + 1)}
                disabled={currentPage === totalPages}
                aria-label="Następna"
                style={{ minWidth: "38px" }}
              >
                <FontAwesomeIcon icon={faChevronRight} />
              </button>
            </li>
          </ul>
        </nav>
      )}
      <ConfirmDeleteModal
        show={showDeleteModal}
        onHide={() => setShowDeleteModal(false)}
        onConfirm={confirmDeleteOrder}
        itemName={
          orderToDelete
            ? `${orderToDelete.offer ? orderToDelete.offer.kind : "brak"} (${
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
