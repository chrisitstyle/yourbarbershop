import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { format, subHours } from "date-fns";
const GuestOrdersTable = ({ data }) => {
  const navigate = useNavigate();
  const guestOrdersPerPage = 10;
  const [currentPage, setCurrentPage] = useState(1);
  const indexOfLastGuestOrder = currentPage * guestOrdersPerPage;
  const indexOfFirstGuestOrder = indexOfLastGuestOrder - guestOrdersPerPage;
  const currentData = data.slice(indexOfFirstGuestOrder, indexOfLastGuestOrder);

  const totalPages = Math.ceil(data.length / guestOrdersPerPage);

  const handleEditClick = (guestOrder) => {
    navigate(`/adminpanel/editguestorder/${guestOrder.idGuestOrder}`, {
      state: { guestOrderData: guestOrder },
    });
  };

  const handlePageClick = (page) => {
    setCurrentPage(page);
  };

  const formatVisitDate = (date) => {
    return format(subHours(new Date(date), 1), "yyyy-MM-dd HH:mm:ss");
  };

  const formatOrderDate = (date) => {
    return format(subHours(new Date(date), 1), "yyyy-MM-dd HH:mm:ss");
  };

  return (
    <div className="container text-center">
      <div className="py-4">
        <div>
          <h2>Lista wizyt gości</h2>
          <table className="table border shadow">
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
                <th scope="col">Akcja</th>
              </tr>
            </thead>
            <tbody>
              {currentData.map((guestOrder) => (
                <tr key={guestOrder.idGuestOrder}>
                  <td>{guestOrder.idGuestOrder}</td>
                  <td>{guestOrder.firstname}</td>
                  <td>{guestOrder.lastname}</td>
                  <td>{guestOrder.phonenumber}</td>
                  <td>{guestOrder.offer ? guestOrder.offer.kind : "brak"}</td>
                  <td>
                    {guestOrder.offer ? guestOrder.offer.cost + " zł" : "brak"}
                  </td>
                  <td>
                    {guestOrder.orderDate
                      ? formatOrderDate(guestOrder.orderDate)
                      : "brak"}
                  </td>
                  <td>
                    {guestOrder.visitDate
                      ? formatVisitDate(guestOrder.visitDate)
                      : "brak"}
                  </td>
                  <td>
                    <div className="d-flex">
                      <button
                        className="btn btn-warning"
                        style={{ marginRight: "6px" }}
                        onClick={() => {
                          handleEditClick(guestOrder);
                        }}
                      >
                        Edytuj
                      </button>
                      <button
                        className="btn btn-danger"
                        style={{ marginRight: "-3px" }}
                        //   onClick={() => onDeleteOrder(guestOrder.idOrder)}
                      >
                        Usuń
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
