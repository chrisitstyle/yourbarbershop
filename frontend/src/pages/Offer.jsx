import { useState } from "react";
import useOffers from "../hooks/useOffers";
import LoadingSpinner from "../components/common/LoadingSpinner";
import { Alert } from "react-bootstrap";

const Offer = () => {
  const { offers, isLoading, error } = useOffers();

  const [currentPage, setCurrentPage] = useState(1);
  const offersPerPage = 10;

  const indexOfLastOffer = currentPage * offersPerPage;
  const indexOfFirstOffer = indexOfLastOffer - offersPerPage;
  const currentOffers = offers.slice(indexOfFirstOffer, indexOfLastOffer);

  const paginate = (pageNumber) => setCurrentPage(pageNumber);

  const showPagination = offers.length > offersPerPage;

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

  if (offers.length === 0) {
    return (
      <div className="container">
        <h1 className="display-6 text-center mb-4">Nasza oferta</h1>
        <Alert variant="info" className="text-center">
          Brak usług w systemie.
        </Alert>
      </div>
    );
  }

  return (
    <div className="container">
      <h1 className="display-6 text-center mb-4">Nasza oferta</h1>
      <p className="lead text-center">
        Zapoznaj się z naszą szeroką ofertą usług, które zadbają o Twój wygląd i
        samopoczucie. Nasz doświadczony zespół fryzjerów stworzy dla Ciebie
        unikalną stylizację, dopasowaną do Twoich preferencji.
      </p>
      <div className="table-responsive">
        <table
          className="table table-bordered table-hover mx-auto"
          style={{ maxWidth: "600px" }}
        >
          <thead>
            <tr>
              <th className="text-center">Numer usługi</th>
              <th className="text-center">Rodzaj usługi</th>
              <th className="text-center">Cena</th>
            </tr>
          </thead>
          <tbody>
            {currentOffers.map((offer) => (
              <tr key={offer.idOffer}>
                <td className="text-center">{offer.idOffer}</td>
                <td className="text-center">{offer.kind}</td>
                <td className="text-center">{offer.cost} zł</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <p className="lead text-center">
        Nie zwlekaj, umów się na wizytę już dziś i poczuj się wyjątkowo!
      </p>
      {showPagination && (
        <ul className="pagination justify-content-center mt-4">
          {[...Array(Math.ceil(offers.length / offersPerPage))].map(
            (_, index) => (
              <li
                key={index + 1}
                className={`page-item ${
                  index + 1 === currentPage ? "active" : ""
                }`}
              >
                <button
                  className="page-link"
                  onClick={() => paginate(index + 1)}
                >
                  {index + 1}
                </button>
              </li>
            )
          )}
        </ul>
      )}
    </div>
  );
};

export default Offer;
