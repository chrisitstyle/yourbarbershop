import { useState } from "react";
import useOffers from "../hooks/useOffers";
import LoadingSpinner from "../components/common/LoadingSpinner";
import { Alert } from "react-bootstrap";

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faScissors } from "@fortawesome/free-solid-svg-icons";

const Offer = () => {
  const { offers, isLoading, error } = useOffers();
  const [currentPage, setCurrentPage] = useState(1);
  const offersPerPage = 10;

  const indexOfLastOffer = currentPage * offersPerPage;
  const indexOfFirstOffer = indexOfLastOffer - offersPerPage;
  const currentOffers = offers.slice(indexOfFirstOffer, indexOfLastOffer);

  const paginate = (pageNumber) => setCurrentPage(pageNumber);

  const showPagination = offers.length > offersPerPage;
  const totalPages = Math.ceil(offers.length / offersPerPage);

  if (isLoading) {
    return <LoadingSpinner text="Ładowanie usług..." />;
  }

  if (error) {
    return (
      <Alert variant="danger" className="text-center">
        {error}
      </Alert>
    );
  }

  // in case there are no offers in the system
  if (offers.length === 0) {
    return (
      <div className="container my-5 py-4">
        <h1 className="display-6 text-center mb-4">
          <FontAwesomeIcon icon={faScissors} className="me-2 text-primary" />
          Nasza oferta
        </h1>
        <Alert variant="info" className="text-center">
          Brak usług w systemie.
        </Alert>
      </div>
    );
  }

  return (
    <div className="container my-5 py-4">
      <h1 className="display-6 text-center mb-4">
        <FontAwesomeIcon icon={faScissors} className="me-2 text-primary" />
        Nasza oferta
      </h1>
      <p className="lead text-center mb-5">
        Zapoznaj się z naszą szeroką ofertą. Nasi fryzjerzy zadbają o Twój
        wygląd i samopoczucie!
      </p>
      <div className="table-responsive">
        <table
          className="table table-bordered table-hover mx-auto shadow rounded"
          style={{ maxWidth: "700px" }}
        >
          <thead className="table-dark">
            <tr>
              <th className="text-center align-middle">Numer usługi</th>
              <th className="text-center align-middle">Rodzaj usługi</th>
              <th className="text-center align-middle">Cena</th>
            </tr>
          </thead>
          <tbody>
            {currentOffers.map((offer) => (
              <tr key={offer.idOffer}>
                <td className="text-center align-middle">{offer.idOffer}</td>
                <td className="text-center align-middle">{offer.kind}</td>
                <td className="text-center align-middle">{offer.cost} zł</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {showPagination && (
        <ul className="pagination justify-content-center mt-4">
          {/* previous page button */}
          <li className={`page-item${currentPage === 1 ? " disabled" : ""}`}>
            <button
              className="page-link"
              onClick={() => paginate(currentPage - 1)}
              disabled={currentPage === 1}
              aria-label="Poprzednia"
            >
              &laquo;
            </button>
          </li>
          {/* page numbers */}
          {[...Array(totalPages)].map((_, index) => (
            <li
              key={index + 1}
              className={`page-item${
                index + 1 === currentPage ? " active" : ""
              }`}
            >
              <button
                className="page-link"
                onClick={() => paginate(index + 1)}
                style={{ minWidth: "38px" }}
              >
                {index + 1}
              </button>
            </li>
          ))}
          {/* next page button */}
          <li
            className={`page-item${
              currentPage === totalPages ? " disabled" : ""
            }`}
          >
            <button
              className="page-link"
              onClick={() => paginate(currentPage + 1)}
              disabled={currentPage === totalPages}
              aria-label="Następna"
            >
              &raquo;
            </button>
          </li>
        </ul>
      )}
      <p className="lead text-center mt-5">
        Nie zwlekaj - umów się na wizytę już dziś i poczuj się wyjątkowo!
      </p>
    </div>
  );
};

export default Offer;
