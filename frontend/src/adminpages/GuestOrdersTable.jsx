import { memo } from "react";
import { useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPen, faTrashAlt } from "@fortawesome/free-solid-svg-icons";
import { useAuth } from "../AuthContext";
import useGuestOrders from "../hooks/useGuestOrders";
import useTableData from "../hooks/useTableData";
import useDeleteModal from "../hooks/useDeleteModal";
import { getNestedValue } from "../utils/tableHelpers";
import { Alert } from "react-bootstrap";
import LoadingSpinner from "../components/common/LoadingSpinner";
import ConfirmDeleteModal from "../components/common/ConfirmDeleteModal";
import PaginationControl from "../components/common/PaginationControl";
import SearchBox from "../components/common/SearchBox";
const guestOrderFieldsHeaders = [
  "Identyfikator zamówienia",
  "Imię",
  "Nazwisko",
  "Numer telefonu",
  "Usługa",
  "Koszt",
  "Data zamówienia",
  "Data wizyty",
  "Status",
];

const guestOrderFields = [
  "idGuestOrder",
  "firstname",
  "lastname",
  "phonenumber",
  "offer.kind",
  "offer.cost",
  "orderDate",
  "visitDate",
  "status",
];

const GuestOrderRow = memo(function GuestOrderRow({
  guestOrder,
  onEdit,
  onDelete,
  isDeleting,
}) {
  return (
    <tr>
      {guestOrderFields.map((field) => (
        <td key={field} className="align-middle text-center">
          {getNestedValue(guestOrder, field)}
        </td>
      ))}
      <td className="align-middle text-center">
        <div className="d-flex justify-content-center">
          <button
            className="btn btn-warning btn-sm me-2"
            style={{ minWidth: "40px" }}
            title="Edytuj"
            onClick={() => onEdit(guestOrder)}
          >
            <FontAwesomeIcon icon={faPen} />
          </button>
          <button
            className="btn btn-danger btn-sm"
            style={{ minWidth: "40px" }}
            title="Usuń"
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
    user?.token
  );
  const navigate = useNavigate();

  const filterGuestOrders = (order, term) => {
    const searchStr = ` ${order.idGuestOrder} ${order.firstname} ${
      order.lastname
    } ${order.email} ${order.offer?.kind || ""} ${order.offer?.cost || ""} ${
      order.phonenumber || ""
    } ${order.orderDate} ${order.visitDate} ${order.status}`;
    return searchStr.toLowerCase().includes(term.toLowerCase());
  };

  const {
    searchTerm,
    handleSearchChange,
    currentData,
    currentPage,
    totalPages,
    setCurrentPage,
  } = useTableData(guestOrders, filterGuestOrders);

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

  if (isLoading) return <LoadingSpinner text="Ładowanie wizyt gości..." />;
  if (error) return <Alert variant="danger">{error}</Alert>;

  return (
    <div className="container my-5 py-4 text-center">
      <h2 className="mb-4">Wizyty gości</h2>

      <SearchBox
        value={searchTerm}
        onChange={handleSearchChange}
        placeholder="Szukaj wizyty..."
      />

      <div className="table-responsive">
        <table
          className="table table-bordered table-hover shadow rounded mx-auto"
          style={{ maxWidth: "1100px" }}
        >
          <thead className="table-dark">
            <tr>
              {guestOrderFieldsHeaders.map((header) => (
                <th key={header} className="text-center align-middle">
                  {header}
                </th>
              ))}
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
                    Brak wyników.
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
