import React, { useEffect, useState } from "react";
import { useAuth } from "../AuthContext";
import axios from "axios";
import { useParams, useLocation } from "react-router-dom";
import { format, subHours } from "date-fns";
import { Alert } from "react-bootstrap";

const Profile = () => {
  const { user } = useAuth();
  const { id } = useParams();
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const registrationOrderSuccess = searchParams.get("registrationOrderSuccess");
  const [userDetails, setUserDetails] = useState(null);

  const visitsPerPage = 10;
  const [currentPage, setCurrentPage] = useState(1);
  const indexOfLastVisit = currentPage * visitsPerPage;
  const indexOfFirstVisit = indexOfLastVisit - visitsPerPage;
  const [searchTerm, setSearchTerm] = useState("");

  const currentData = userDetails?.userOrders
    ?.filter((order) =>
      `${order.idOrder}  ${order.offer.kind} ${order.offer.cost} ${order.orderDate} ${order.visitDate} ${order.status}`
        .toLowerCase()
        .includes(searchTerm.toLowerCase())
    )
    .slice(indexOfFirstVisit, indexOfLastVisit);

  const totalPages = Math.ceil(
    (userDetails?.userOrders?.length || 0) / visitsPerPage
  );
  const handlePageClick = (page) => {
    setCurrentPage(page);
  };

  const formatVisitDate = (date) => {
    return format(subHours(new Date(date), 0), "yyyy-MM-dd HH:mm:ss");
  };

  const formatOrderDate = (date) => {
    return format(subHours(new Date(date), 0), "yyyy-MM-dd HH:mm:ss");
  };
  useEffect(() => {
    const loadUser = async () => {
      try {
        const result = await axios.get(
          `http://localhost:8080/users/get/${id}`,
          {
            withCredentials: true,
            headers: {
              Authorization: `Bearer ${user?.token}`,
            },
          }
        );
        setUserDetails(result.data);
      } catch (error) {
        console.error("Błąd ładowania użytkownika", error);
      }
    };

    if (id && user?.token) {
      loadUser();
    }
  }, [id, user?.token]);

  if (!userDetails) {
    return <div>ładowanie...</div>;
  }

  return (
    <div className="container">
      <div className="py-4 ">
        <h6 className="text-center">{`Jesteś zalogowany jako ${userDetails.email}`}</h6>
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
          {userDetails.userOrders && userDetails.userOrders.length > 0 && (
            <>
              <h6 className="text-center">
                {userDetails.firstname}, poniżej znajdują się wszystkie
                dotychczasowe wizyty
              </h6>
              {/* search field */}
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
                        {order.orderDate
                          ? formatOrderDate(order.orderDate)
                          : "brak"}
                      </td>
                      <td>
                        {order.visitDate
                          ? formatVisitDate(order.visitDate)
                          : "brak"}
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
          )}
        </div>
      </div>
    </div>
  );
};

export default Profile;
