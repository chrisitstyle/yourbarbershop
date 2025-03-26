import React, { useState, useEffect } from "react";
import axios from "axios";

const Offer = () => {
  const [offers, setOffers] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [offersPerPage] = useState(10);

  useEffect(() => {
    loadOffers();
  }, []);

  const loadOffers = async () => {
    try {
      const result = await axios.get("http://localhost:8080/offers/get");
      setOffers(result.data);
    } catch (error) {
      console.error("Error loading offers:", error);
    }
  };

  const indexOfLastOffer = currentPage * offersPerPage;
  const indexOfFirstOffer = indexOfLastOffer - offersPerPage;
  const currentOffers = offers.slice(indexOfFirstOffer, indexOfLastOffer);

  const paginate = (pageNumber) => setCurrentPage(pageNumber);

  const showPagination = offers.length > offersPerPage;

  return (
    <div className="container">
      <h1 className="display-6 text-center mb-4">Nasza oferta</h1>
      {!offers.length > 0 ? (
        <h5 className="text-center">Ładowanie...</h5>
      ) : (
        <>
          <p className="lead text-center">
            Zapoznaj się z naszą szeroką ofertą usług, które zadbają o Twój
            wygląd i samopoczucie. Nasz doświadczony zespół fryzjerów stworzy
            dla Ciebie unikalną stylizację, dopasowaną do Twoich preferencji.
          </p>

          <div className="table-responsive">
            <table
              className="table  table-bordered table-hover mx-auto"
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
                {currentOffers.map((offer, index) => (
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
        </>
      )}
    </div>
  );
};

export default Offer;
