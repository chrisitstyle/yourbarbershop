import { useState } from "react";
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

  // filter by user/order data using the search bar
  const filteredOrders = orders.filter((order) =>
    ` ${order.idOrder} ${order.user.firstname}  ${order.user.lastname}  ${order.user.username} ${order.offer.kind} ${order.offer.cost} ${order.orderDate} ${order.visitDate} ${order.status}`
      .toLowerCase()
      .includes(searchTerm.toLowerCase())
  );

  const indexOfLastOrder = currentPage * ordersPerPage;
  const indexOfFirstOrder = indexOfLastOrder - ordersPerPage;
  const currentData = filteredOrders.slice(indexOfFirstOrder, indexOfLastOrder);
  const totalPages = Math.ceil(filteredOrders.length / ordersPerPage);

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
              <th className="text-center align-middle">Identyfikator wizyty</th>
              <th className="text-center align-middle">Imię</th>
              <th className="text-center align-middle">Nazwisko</th>
              <th className="text-center align-middle">Email</th>
              <th className="text-center align-middle">Usługa</th>
              <th className="text-center align-middle">Koszt</th>
              <th className="text-center align-middle">Data zamówienia</th>
              <th className="text-center align-middle">Data wizyty</th>
              <th className="text-center align-middle">Status</th>
              <th className="text-center align-middle">Akcja</th>
            </tr>
          </thead>
          <tbody>
            {currentData.length > 0 ? (
              currentData.map((order) => (
                <tr key={order.idOrder}>
                  <td className="align-middle text-center">{order.idOrder}</td>
                  <td className="align-middle text-center">
                    {order.user.firstname || "brak"}
                  </td>
                  <td className="align-middle text-center">
                    {order.user.lastname || "brak"}
                  </td>
                  <td className="align-middle text-center">
                    {order.user.username}
                  </td>
                  <td className="align-middle text-center">
                    {order.offer ? order.offer.kind : "brak"}
                  </td>
                  <td className="align-middle text-center">
                    {order.offer ? order.offer.cost + " zł" : "brak"}
                  </td>
                  <td className="align-middle text-center">
                    {order.orderDate ? formatDate(order.orderDate) : "brak"}
                  </td>
                  <td className="align-middle text-center">
                    {order.visitDate ? formatDate(order.visitDate) : "brak"}
                  </td>
                  <td className="align-middle text-center">{order.status}</td>
                  <td className="align-middle text-center">
                    <div className="d-flex justify-content-center">
                      {/* edit button with tooltip */}
                      <button
                        className="btn btn-warning btn-sm me-2"
                        style={{ minWidth: "40px" }}
                        title="Edytuj wizytę"
                        onClick={() => handleEditClick(order)}
                      >
                        <FontAwesomeIcon icon={faPen} />
                      </button>
                      {/* delete button with tooltip */}
                      <button
                        className="btn btn-danger btn-sm"
                        style={{ minWidth: "40px" }}
                        title="Usuń wizytę"
                        onClick={() => handleAskDeleteOrder(order)}
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
              ))
            ) : (
              <tr>
                <td colSpan="10" className="text-center py-4">
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
