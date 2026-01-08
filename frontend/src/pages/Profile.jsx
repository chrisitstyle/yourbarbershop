import { useState } from "react";
import { useAuth } from "../AuthContext";
import { useParams, useLocation } from "react-router-dom";
import { formatDate } from "../api/dataParser";
import { Alert } from "react-bootstrap";
import LoadingSpinner from "../components/common/LoadingSpinner";
import useUserDetails from "../hooks/useUserDetails";

const Profile = () => {
  const { user } = useAuth();
  const { id } = useParams();
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const registrationOrderSuccess = searchParams.get("registrationOrderSuccess");

  const { userDetails, isLoading, error } = useUserDetails(id, user?.token);

  const visitsPerPage = 10;
  const [currentPage, setCurrentPage] = useState(1);
  const indexOfLastVisit = currentPage * visitsPerPage;
  const indexOfFirstVisit = indexOfLastVisit - visitsPerPage;
  const [searchTerm, setSearchTerm] = useState("");

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
    <div className="container">
      <div className="py-4 ">
        <h6 className="text-center">
          {`Jesteś zalogowany jako ${userDetails?.email ?? "Brak danych"}`}
        </h6>
        <div>
          {registrationOrderSuccess && (
            <Alert
              variant="success"
              onClose={() => {}}
              dismissible
              className="text-center"
            >
              Twoja wizyta została zarejestrowana
            </Alert>
          )}
          {userDetails?.userOrders && userDetails.userOrders.length > 0 ? (
            <>
              <h6 className="text-center">
                {(userDetails?.firstname ?? "Użytkownik") +
                  ", poniżej znajdują się wszystkie dotychczasowe wizyty"}
              </h6>
              <div className="mb-3 mt-4">
                <input
                  type="text"
                  placeholder="Szukaj wizyty..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="form-control"
                  style={{ width: "200px" }}
                />
              </div>
              <table className="table border shadow text-center table-hover">
                <thead>
                  <tr>
                    <th scope="col">Identyfikator wizyty</th>
                    <th scope="col">Usługa</th>
                    <th scope="col">Koszt</th>
                    <th scope="col">Data złożenia wizyty</th>
                    <th scope="col">Data wizyty</th>
                    <th scope="col">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {currentData.map((order) => (
                    <tr key={order.idOrder}>
                      <td>{order.idOrder}</td>
                      <td>{order.offer ? order.offer.kind : "brak"}</td>
                      <td>{order.offer ? order.offer.cost + " zł" : "brak"}</td>
                      <td>
                        {order.orderDate ? formatDate(order.orderDate) : "brak"}
                      </td>
                      <td>
                        {order.visitDate ? formatDate(order.visitDate) : "brak"}
                      </td>
                      <td>{order.status}</td>
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
            </>
          ) : (
            <Alert variant="info" className="text-center">
              Brak zarejestrowanych wizyt.
            </Alert>
          )}
        </div>
      </div>
    </div>
  );
};

export default Profile;
