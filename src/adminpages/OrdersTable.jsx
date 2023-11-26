import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
const OrdersTable = ({ data, onDeleteOrder }) => {
  // const navigate = useNavigate();
  const ordersPerPage = 10;
  const [currentPage, setCurrentPage] = useState(1);

  const handlePageClick = (page) => {
    setCurrentPage(page);
  };

  const indexOfLastOrder = currentPage * ordersPerPage;
  const indexOfFirstOrder = indexOfLastOrder - ordersPerPage;
  const currentData = data.slice(indexOfFirstOrder, indexOfLastOrder);

  const totalPages = Math.ceil(data.length / ordersPerPage);

  return (
    <div className="container text-center">
      <div className="py-4">
        <div>
          <h2>Lista zamówień</h2>
          <table className="table border shadow">
            <thead>
              <tr>
                <th scope="col">Identyfikator zamówienia</th>
                <th scope="col">Imie</th>
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
                  <td>{order.user.firstname}</td>
                  <td>{order.user.lastname}</td>
                  <td>{order.user.username}</td>
                  <td>{order.offer ? order.offer.kind : "brak"}</td>
                  <td>{order.offer ? order.offer.cost + " zł" : "brak"}</td>
                  <td>{order.orderDate ? order.orderDate : "brak"}</td>
                  <td>{order.visitDate ? order.visitDate : "brak"}</td>
                  <td>
                    <button className="btn btn-warning">Edytuj</button>
                    <button
                      className="btn btn-danger mx-2"
                      onClick={() => onDeleteOrder(order.idOrder)}
                    >
                      Usuń
                    </button>
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
                    <a
                      className="page-link"
                      onClick={() => handlePageClick(index + 1)}
                    >
                      {index + 1}
                    </a>
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
