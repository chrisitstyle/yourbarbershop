import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { format, subHours } from "date-fns";

const OrdersTable = ({ data, onDeleteOrder }) => {
  const navigate = useNavigate();
  const ordersPerPage = 10;
  const [currentPage, setCurrentPage] = useState(1);
  const indexOfLastOrder = currentPage * ordersPerPage;
  const indexOfFirstOrder = indexOfLastOrder - ordersPerPage;
  const currentData = data.slice(indexOfFirstOrder, indexOfLastOrder);

  const totalPages = Math.ceil(data.length / ordersPerPage);
  const handlePageClick = (page) => {
    setCurrentPage(page);
  };

  const handleEditClick = (order) => {
    navigate(`/adminpanel/editorder/${order.idOrder}`, {
      state: { orderData: order },
    });
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
          <h2>Lista wizyt użytkowników</h2>
          <table className="table border shadow">
            <thead>
              <tr>
                <th scope="col">Identyfikator wizyty</th>
                <th scope="col">Imię</th>
                <th scope="col">Nazwisko</th>
                <th scope="col">Email</th>
                <th scope="col">Usługa</th>
                <th scope="col">Koszt</th>
                <th scope="col">Data zamówienia</th>
                <th scope="col">Data wizyty</th>
                <th scope="col">Akcja</th>
              </tr>
            </thead>
            <tbody>
              {currentData.map((order) => (
                <tr key={order.idOrder}>
                  <td>{order.idOrder}</td>
                  <td>{order.user.firstname || "brak"}</td>
                  <td>{order.user.lastname || "brak"}</td>
                  <td>{order.user.username}</td>
                  <td>{order.offer ? order.offer.kind || "brak" : "brak"}</td>
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
                  <td>
                    <div className="d-flex">
                      <button
                        className="btn btn-warning"
                        style={{ marginRight: "6px" }}
                        onClick={() => {
                          handleEditClick(order);
                        }}
                      >
                        Edytuj
                      </button>
                      <button
                        className="btn btn-danger"
                        style={{ marginRight: "-3px" }}
                        onClick={() => onDeleteOrder(order.idOrder)}
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

export default OrdersTable;
