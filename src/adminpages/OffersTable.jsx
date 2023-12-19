import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPen, faTrashAlt } from "@fortawesome/free-solid-svg-icons";

const OffersTable = ({ data, onDeleteOffer }) => {
  const [searchTerm, setSearchTerm] = useState("");
  const navigate = useNavigate();
  const offersPerPage = 10;
  const [currentPage, setCurrentPage] = useState(1);

  const handlePageClick = (page) => {
    setCurrentPage(page);
  };

  const indexOfLastOffer = currentPage * offersPerPage;
  const indexOfFirstOffer = indexOfLastOffer - offersPerPage;
  const currentData = data
    .filter((offer) =>
      ` ${offer.idOffer} ${offer.kind} ${offer.cost}`
        .toLowerCase()
        .includes(searchTerm.toLowerCase())
    )
    .slice(indexOfFirstOffer, indexOfLastOffer);

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
          <h2>Usługi</h2>
          {/* pole wyszukiwania */}
          <div className="mb-3 mt-4">
            <input
              type="text"
              placeholder="Szukaj usługi..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="form-control"
              style={{ width: "200px" }}
            />
          </div>
          <table className="table border shadow table-hover">
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
                      <FontAwesomeIcon
                        icon={faPen}
                        style={{ color: "black" }}
                      />
                    </button>
                    <button
                      className="btn btn-danger mx-2"
                      onClick={() => onDeleteOffer(offer.idOffer)}
                    >
                      <FontAwesomeIcon icon={faTrashAlt} />
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

export default OffersTable;
