import useOffers from "../hooks/useOffers";
import LoadingSpinner from "../components/common/LoadingSpinner";
import { Alert } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faScissors } from "@fortawesome/free-solid-svg-icons";
import useTableData from "../hooks/useTableData";
import PaginationControl from "../components/common/PaginationControl";
import SearchBox from "../components/common/SearchBox";

const fields = ["idOffer", "kind", "cost"];

const Offer = () => {
  const { offers, isLoading, error } = useOffers();

  const filterOffers = (offer, term) => {
    const searchStr = `${offer.idOffer} ${offer.kind} ${offer.cost}`;
    return searchStr.toLowerCase().includes(term.toLowerCase());
  };

  const {
    searchTerm,
    handleSearchChange,
    currentData,
    currentPage,
    totalPages,
    setCurrentPage,
  } = useTableData(offers, filterOffers);

  if (isLoading) return <LoadingSpinner text="Ładowanie usług..." />;

  if (error) {
    return (
      <Alert variant="danger" className="text-center">
        {error}
      </Alert>
    );
  }

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
      <p className="lead text-center mb-4">
        Zapoznaj się z naszą szeroką ofertą. Nasi fryzjerzy zadbają o Twój
        wygląd i samopoczucie!
      </p>

      {/* search box */}
      <SearchBox
        value={searchTerm}
        onChange={handleSearchChange}
        placeholder="Szukaj usługi..."
        width="400px"
      />

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
            {currentData.length > 0 ? (
              currentData.map((offer) => (
                <tr key={offer.idOffer}>
                  {fields.map((field) => (
                    <td key={field} className="text-center align-middle">
                      {field === "cost" ? `${offer[field]} zł` : offer[field]}
                    </td>
                  ))}
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={fields.length} className="text-center py-4">
                  <Alert variant="info" className="mb-0">
                    Nie znaleziono usług pasujących do wyszukiwania.
                  </Alert>
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <PaginationControl
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
      />

      <p className="lead text-center mt-5">
        Nie zwlekaj - umów się na wizytę już dziś i poczuj się wyjątkowo!
      </p>
    </div>
  );
};

export default Offer;
