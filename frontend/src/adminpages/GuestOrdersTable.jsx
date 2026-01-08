import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { formatDate } from "../api/dataParser";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPen, faTrashAlt } from "@fortawesome/free-solid-svg-icons";
import { useAuth } from "../AuthContext";
import useGuestOrders from "../hooks/useGuestOrders";
import { Alert } from "react-bootstrap";
import LoadingSpinner from "../components/common/LoadingSpinner";

const GuestOrdersTable = ({ onDeleteGuestOrder }) => {
  const { user } = useAuth();
  const { guestOrders, isLoading, error, refetch } = useGuestOrders(
    user?.token
  );

  const navigate = useNavigate();
  const guestOrdersPerPage = 10;
  const [currentPage, setCurrentPage] = useState(1);
  const indexOfLastOrder = currentPage * guestOrdersPerPage;
  const indexOfFirstOrder = indexOfLastOrder - guestOrdersPerPage;
  const [searchTerm, setSearchTerm] = useState("");
  const [deleteLoadingId, setDeleteLoadingId] = useState(null);

  const currentData = guestOrders
    .filter((order) =>
      ` ${order.idGuestOrder} ${order.firstname}  ${order.lastname} ${
        order.email
      } ${order.offer?.kind || ""} ${order.offer?.cost || ""} ${
        order.phonenumber || ""
      } ${order.orderDate} ${order.visitDate} ${order.status}`
        .toLowerCase()
        .includes(searchTerm.toLowerCase())
    )
    .slice(indexOfFirstOrder, indexOfLastOrder);

  const totalPages = Math.ceil(guestOrders.length / guestOrdersPerPage);

  const handleEditClick = (guestOrder) => {
    navigate(`/adminpanel/editguestorder/${guestOrder.idGuestOrder}`, {
      state: { guestOrderData: guestOrder },
    });
  };

  const handlePageClick = (page) => {
    setCurrentPage(page);
  };

  const handleDeleteGuestOrder = async (idGuestOrder) => {
    setDeleteLoadingId(idGuestOrder);
    try {
      await onDeleteGuestOrder(idGuestOrder);
      await refetch();
    } catch (err) {
    } finally {
      setDeleteLoadingId(null);
    }
  };

  if (isLoading) return <LoadingSpinner text="Ładowanie wizyt gości..." />;
  if (error) return <Alert variant="danger">{error}</Alert>;

  return (
    <div className="container text-center">
      <div className="py-4">
        <div>
          <h2>Wizyty gości</h2>
          <div className="mb-3 mt-4">
            <input
              type="text"
              placeholder="Szukaj wizyty..."
              value={searchTerm}
              onChange={(e) => {
                setSearchTerm(e.target.value);
                setCurrentPage(1);
              }}
              className="form-control"
              style={{ width: "200px" }}
            />
          </div>
          <table className="table border shadow table-hover">
            <thead>
              <tr>
                <th scope="col">Identyfikator zamówienia</th>
                <th scope="col">Imię</th>
                <th scope="col">Nazwisko</th>
                <th scope="col">Numer telefonu</th>
                <th scope="col">Usługa</th>
                <th scope="col">Koszt</th>
                <th scope="col">Data zamówienia</th>
                <th scope="col">Data wizyty</th>
                <th scope="col">Status</th>
                <th scope="col">Akcja</th>
              </tr>
            </thead>
            <tbody>
              {currentData.map((guestOrder) => (
                <tr key={guestOrder.idGuestOrder}>
                  <td>{guestOrder.idGuestOrder}</td>
                  <td>{guestOrder.firstname}</td>
                  <td>{guestOrder.lastname}</td>
                  <td>{guestOrder.phonenumber || "brak"}</td>
                  <td>{guestOrder.offer ? guestOrder.offer.kind : "brak"}</td>
                  <td>
                    {guestOrder.offer ? guestOrder.offer.cost + " zł" : "brak"}
                  </td>
                  <td>
                    {guestOrder.orderDate
                      ? formatDate(guestOrder.orderDate)
                      : "brak"}
                  </td>
                  <td>
                    {guestOrder.visitDate
                      ? formatDate(guestOrder.visitDate)
                      : "brak"}
                  </td>
                  <td>{guestOrder.status}</td>
                  <td>
                    <div className="d-flex">
                      <button
                        className="btn btn-warning"
                        style={{ marginRight: "6px" }}
                        onClick={() => handleEditClick(guestOrder)}
                      >
                        <FontAwesomeIcon
                          icon={faPen}
                          style={{ color: "black" }}
                        />
                      </button>
                      <button
                        className="btn btn-danger"
                        style={{ marginRight: "-3px" }}
                        onClick={() =>
                          handleDeleteGuestOrder(guestOrder.idGuestOrder)
                        }
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
              ))}
            </tbody>
          </table>
          {totalPages > 1 && (
            <nav className="pagination justify-content-center">
              <ul className="pagination">
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
                    >
                      {index + 1}
                    </button>
                  </li>
                ))}
              </ul>
            </nav>
          )}
        </div>
      </div>
    </div>
  );
};

export default GuestOrdersTable;
