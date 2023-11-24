import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

const OffersTable = ({ data, onDeleteOffer }) => {
  const navigate = useNavigate();
  const offersPerPage = 10;
  const [currentPage, setCurrentPage] = useState(1);

  const handlePageClick = (page) => {
    setCurrentPage(page);
  };

  const indexOfLastOffer = currentPage * offersPerPage;
  const indexOfFirstOffer = indexOfLastOffer - offersPerPage;
  const currentData = data.slice(indexOfFirstOffer, indexOfLastOffer);

  const totalPages = Math.ceil(data.length / offersPerPage);

  const handleEditClick = (offer) => {
    navigate(`/adminpanel/editoffer/${offer.idOffer}`, {
      state: { offerData: offer },
    });
  };

  return (
    <div className="container text-center">
      <div className="py-4">
        <div>
          <table className="table border shadow text-center">
            <thead>
              <tr>
                <th scope="col">Identyfikator usługi</th>
                <th scope="col">Usługa</th>
                <th scope="col">Koszt</th>
                <th scope="col">Akcja</th>
              </tr>
            </thead>
            <tbody>
              {currentData.map((offer) => (
                <tr key={offer.idOffer}>
                  <td>{offer.idOffer}</td>
                  <td>{offer.kind}</td>
                  <td>{offer.cost} zł</td>
                  <td>
                    <button
                      className="btn btn-warning"
                      onClick={() => {
                        handleEditClick(offer);
                      }}
                    >
                      Edytuj
                    </button>
                    <button
                      className="btn btn-danger mx-2"
                      onClick={() => onDeleteOffer(offer.idOffer)}
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

export default OffersTable;
