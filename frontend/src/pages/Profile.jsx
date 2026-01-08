import { useState, useEffect } from "react";
import { useAuth } from "../AuthContext";
import { useParams, useLocation } from "react-router-dom";
import { formatDate } from "../api/dataParser";
import { Alert } from "react-bootstrap";
import LoadingSpinner from "../components/common/LoadingSpinner";
import useUserDetails from "../hooks/useUserDetails";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faChevronLeft,
  faChevronRight,
} from "@fortawesome/free-solid-svg-icons";

const Profile = () => {
  const { user } = useAuth();
  const { id } = useParams();
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const registrationOrderSuccess = searchParams.get("registrationOrderSuccess");

  const { userDetails, isLoading, error } = useUserDetails(id, user?.token);

  const visitsPerPage = 10;
  const [currentPage, setCurrentPage] = useState(1);
  const [searchTerm, setSearchTerm] = useState("");
  const [showSuccessAlert, setShowSuccessAlert] = useState(
    Boolean(registrationOrderSuccess)
  );
  const indexOfLastVisit = currentPage * visitsPerPage;
  const indexOfFirstVisit = indexOfLastVisit - visitsPerPage;

  const filteredData =
    userDetails?.userOrders?.filter((order) =>
      `${order.idOrder} ${order.offer?.kind || ""} ${order.offer?.cost || ""} ${
        order.orderDate
      } ${order.visitDate} ${order.status}`
        .toLowerCase()
        .includes(searchTerm.toLowerCase())
    ) || [];

  const currentData = filteredData.slice(indexOfFirstVisit, indexOfLastVisit);
  const totalPages = Math.ceil(filteredData.length / visitsPerPage);

  useEffect(() => {
    let timeout;
    if (showSuccessAlert) {
      timeout = setTimeout(() => {
        setShowSuccessAlert(false);
      }, 3000);
    }
    return () => clearTimeout(timeout);
  }, [showSuccessAlert]);

  const handlePageClick = (page) => {
    setCurrentPage(page);
  };

  if (isLoading) {
    return <LoadingSpinner text="Ładowanie profilu..." />;
  }

  if (error) {
    return (
      <Alert variant="danger" className="text-center">
        {error}
      </Alert>
    );
  }

  return (
    <div className="container my-5 py-4 text-center">
      <div>
        {showSuccessAlert && (
          <Alert
            variant="success"
            className="text-center mx-auto"
            style={{ maxWidth: 440 }}
          >
            Twoja wizyta została zarejestrowana
          </Alert>
        )}
        <h2 className="mb-3">Twój profil</h2>
        <div
          className="mb-2"
          style={{ fontWeight: "500", fontSize: "1.06rem" }}
        >
          {`Jesteś zalogowany jako `}
          <span className="fw-bold">{userDetails?.email ?? "Brak danych"}</span>
        </div>
        <div className="mb-4" style={{ color: "#666" }}>
          {(userDetails?.firstname ?? "Użytkownik") +
            ", poniżej znajdują się wszystkie dotychczasowe wizyty"}
        </div>

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
            style={{ maxWidth: "900px" }}
          >
            <thead className="table-dark">
              <tr>
                <th scope="col" className="text-center align-middle">
                  Identyfikator wizyty
                </th>
                <th scope="col" className="text-center align-middle">
                  Usługa
                </th>
                <th scope="col" className="text-center align-middle">
                  Koszt
                </th>
                <th scope="col" className="text-center align-middle">
                  Data złożenia wizyty
                </th>
                <th scope="col" className="text-center align-middle">
                  Data wizyty
                </th>
                <th scope="col" className="text-center align-middle">
                  Status
                </th>
              </tr>
            </thead>
            <tbody>
              {currentData.length > 0 ? (
                currentData.map((order) => (
                  <tr key={order.idOrder}>
                    <td className="align-middle text-center">
                      {order.idOrder}
                    </td>
                    <td className="align-middle text-center">
                      {order.offer ? order.offer.kind : "brak"}
                    </td>
                    <td className="align-middle text-center">
                      {order.offer ? `${order.offer.cost} zł` : "brak"}
                    </td>
                    <td className="align-middle text-center">
                      {order.orderDate ? formatDate(order.orderDate) : "brak"}
                    </td>
                    <td className="align-middle text-center">
                      {order.visitDate ? formatDate(order.visitDate) : "brak"}
                    </td>
                    <td className="align-middle text-center">{order.status}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="6" className="text-center py-4">
                    <Alert variant="info" className="mb-0">
                      Brak wyników do wyświetlenia.
                    </Alert>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* pagination */}
        {totalPages > 1 && (
          <nav className="pagination justify-content-center mt-4">
            <ul className="pagination">
              <li
                className={`page-item ${currentPage === 1 ? "disabled" : ""}`}
              >
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
      </div>
    </div>
  );
};

export default Profile;
