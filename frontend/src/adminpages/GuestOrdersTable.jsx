import { useState, useMemo, memo } from "react";
import { useNavigate } from "react-router-dom";
import { formatDate } from "../api/dataParser";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faPen,
  faTrashAlt,
  faChevronLeft,
  faChevronRight,
} from "@fortawesome/free-solid-svg-icons";
import { useAuth } from "../AuthContext";
import useGuestOrders from "../hooks/useGuestOrders";
import { Alert } from "react-bootstrap";
import LoadingSpinner from "../components/common/LoadingSpinner";
import ConfirmDeleteModal from "../components/common/ConfirmDeleteModal";

const GuestOrderRow = memo(function GuestOrderRow({
  guestOrder,
  onEdit,
  onDelete,
  deleteLoadingId,
}) {
  return (
    <tr>
      <td className="align-middle text-center">{guestOrder.idGuestOrder}</td>
      <td className="align-middle text-center">
        {guestOrder.firstname || "brak"}
      </td>
      <td className="align-middle text-center">
        {guestOrder.lastname || "brak"}
      </td>
      <td className="align-middle text-center">
        {guestOrder.phonenumber || "brak"}
      </td>
      <td className="align-middle text-center">
        {guestOrder.offer ? guestOrder.offer.kind : "brak"}
      </td>
      <td className="align-middle text-center">
        {guestOrder.offer ? guestOrder.offer.cost + " zł" : "brak"}
      </td>
      <td className="align-middle text-center">
        {guestOrder.orderDate ? formatDate(guestOrder.orderDate) : "brak"}
      </td>
      <td className="align-middle text-center">
        {guestOrder.visitDate ? formatDate(guestOrder.visitDate) : "brak"}
      </td>
      <td className="align-middle text-center">{guestOrder.status}</td>
      <td className="align-middle text-center">
        <div className="d-flex justify-content-center">
          <button
            className="btn btn-warning btn-sm me-2"
            style={{ minWidth: "40px" }}
            title="Edit guest visit"
            onClick={() => onEdit(guestOrder)}
          >
            <FontAwesomeIcon icon={faPen} />
          </button>
          <button
            className="btn btn-danger btn-sm"
            style={{ minWidth: "40px" }}
            title="Delete guest visit"
            onClick={() => onDelete(guestOrder)}
            disabled={deleteLoadingId === guestOrder.idGuestOrder}
          >
            {deleteLoadingId === guestOrder.idGuestOrder ? (
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

const GuestOrdersTable = ({ onDeleteGuestOrder }) => {
  const { user } = useAuth();
  const { guestOrders, isLoading, error, refetch } = useGuestOrders(
    user?.token
  );

  const navigate = useNavigate();
  const guestOrdersPerPage = 10;
  const [currentPage, setCurrentPage] = useState(1);
  const [searchTerm, setSearchTerm] = useState("");
  const [deleteLoadingId, setDeleteLoadingId] = useState(null);

  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [guestOrderToDelete, setGuestOrderToDelete] = useState(null);

  const filteredOrders = useMemo(
    () =>
      guestOrders.filter((order) =>
        ` ${order.idGuestOrder} ${order.firstname} ${order.lastname} ${
          order.email
        } ${order.offer?.kind || ""} ${order.offer?.cost || ""} ${
          order.phonenumber || ""
        } ${order.orderDate} ${order.visitDate} ${order.status}`
          .toLowerCase()
          .includes(searchTerm.toLowerCase())
      ),
    [guestOrders, searchTerm]
  );

  const totalPages = useMemo(
    () => Math.ceil(filteredOrders.length / guestOrdersPerPage),
    [filteredOrders.length, guestOrdersPerPage]
  );

  const currentData = useMemo(
    () =>
      filteredOrders.slice(
        (currentPage - 1) * guestOrdersPerPage,
        currentPage * guestOrdersPerPage
      ),
    [filteredOrders, currentPage, guestOrdersPerPage]
  );

  const handleEditClick = (guestOrder) => {
    navigate(`/adminpanel/editguestorder/${guestOrder.idGuestOrder}`, {
      state: { guestOrderData: guestOrder },
    });
  };

  const handlePageClick = (page) => {
    setCurrentPage(page);
  };

  const handleAskDeleteGuestOrder = (guestOrder) => {
    setGuestOrderToDelete(guestOrder);
    setShowDeleteModal(true);
  };

  const confirmDeleteGuestOrder = async () => {
    if (guestOrderToDelete) {
      setDeleteLoadingId(guestOrderToDelete.idGuestOrder);
      try {
        await onDeleteGuestOrder(guestOrderToDelete.idGuestOrder);
        await refetch();
      } catch (err) {
        console.error(err);
      } finally {
        setDeleteLoadingId(null);
        setShowDeleteModal(false);
        setGuestOrderToDelete(null);
      }
    }
  };

  if (isLoading) return <LoadingSpinner text="Ładowanie wizyt gości..." />;
  if (error) return <Alert variant="danger">{error}</Alert>;

  return (
    <div className="container my-5 py-4 text-center">
      <h2 className="mb-4">Wizyty gości</h2>
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
              <th className="text-center align-middle">
                Identyfikator zamówienia
              </th>
              <th className="text-center align-middle">Imię</th>
              <th className="text-center align-middle">Nazwisko</th>
              <th className="text-center align-middle">Numer telefonu</th>
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
              currentData.map((guestOrder) => (
                <GuestOrderRow
                  key={guestOrder.idGuestOrder}
                  guestOrder={guestOrder}
                  onEdit={handleEditClick}
                  onDelete={handleAskDeleteGuestOrder}
                  deleteLoadingId={deleteLoadingId}
                />
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
        onConfirm={confirmDeleteGuestOrder}
        itemName={
          guestOrderToDelete
            ? `${guestOrderToDelete.offer?.kind ?? "brak"} (${
                guestOrderToDelete.idGuestOrder
              })`
            : ""
        }
        label="wizytę gościa"
      />
    </div>
  );
};

export default GuestOrdersTable;
