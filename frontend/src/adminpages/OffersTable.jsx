import { useState, useMemo, memo } from "react";
import { useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faPen,
  faTrashAlt,
  faChevronLeft,
  faChevronRight,
} from "@fortawesome/free-solid-svg-icons";
import useOffers from "../hooks/useOffers";
import LoadingSpinner from "../components/common/LoadingSpinner";
import { Alert } from "react-bootstrap";
import ConfirmDeleteModal from "../components/common/ConfirmDeleteModal";

const OfferRow = memo(function OfferRow({ offer, onEdit, onDelete }) {
  return (
    <tr>
      <td className="align-middle text-center">{offer.idOffer}</td>
      <td className="align-middle text-center">{offer.kind}</td>
      <td className="align-middle text-center">{offer.cost} zł</td>
      <td className="align-middle text-center">
        {/* edit button with tooltip */}
        <button
          className="btn btn-warning btn-sm me-2"
          style={{ minWidth: "40px" }}
          title="Edytuj usługę"
          onClick={() => onEdit(offer)}
        >
          <FontAwesomeIcon icon={faPen} />
        </button>
        {/* delete button with tooltip */}
        <button
          className="btn btn-danger btn-sm"
          style={{ minWidth: "40px" }}
          title="Usuń usługę"
          onClick={() => onDelete(offer)}
        >
          <FontAwesomeIcon icon={faTrashAlt} />
        </button>
      </td>
    </tr>
  );
});

const OffersTable = ({ onDeleteOffer }) => {
  const { offers, isLoading, error, refetch } = useOffers();

  const [searchTerm, setSearchTerm] = useState("");
  const navigate = useNavigate();
  const offersPerPage = 10;
  const [currentPage, setCurrentPage] = useState(1);

  // delete modal state
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [offerToDelete, setOfferToDelete] = useState(null);

  const handlePageClick = (page) => {
    setCurrentPage(page);
  };

  const handleAskDeleteOffer = (offer) => {
    setOfferToDelete(offer);
    setShowDeleteModal(true);
  };

  const confirmDeleteOffer = async () => {
    if (offerToDelete) {
      await onDeleteOffer(offerToDelete.idOffer);
      await refetch();
    }
    setShowDeleteModal(false);
    setOfferToDelete(null);
  };

  const filteredOffers = useMemo(
    () =>
      offers.filter((offer) =>
        ` ${offer.idOffer} ${offer.kind} ${offer.cost}`
          .toLowerCase()
          .includes(searchTerm.toLowerCase())
      ),
    [offers, searchTerm]
  );

  const totalPages = useMemo(
    () => Math.ceil(filteredOffers.length / offersPerPage),
    [filteredOffers.length, offersPerPage]
  );

  const currentData = useMemo(
    () =>
      filteredOffers.slice(
        (currentPage - 1) * offersPerPage,
        currentPage * offersPerPage
      ),
    [filteredOffers, currentPage, offersPerPage]
  );

  const handleEditClick = (offer) => {
    navigate(`/adminpanel/editoffer/${offer.idOffer}`, {
      state: { offerData: offer },
    });
  };

  if (isLoading) return <LoadingSpinner text="Ładowanie usług..." />;
  if (error) return <Alert variant="danger">{error}</Alert>;

  return (
    <div className="container my-5 py-4 text-center">
      <div>
        <h2 className="mb-4">Usługi</h2>
        <div className="mb-3">
          {/* search box */}
          <input
            type="text"
            placeholder="Szukaj usługi..."
            value={searchTerm}
            onChange={(e) => {
              setSearchTerm(e.target.value);
              setCurrentPage(1);
            }}
            className="form-control mx-auto"
            style={{ width: "300px", fontSize: "1rem" }}
          />
        </div>
        <div className="table-responsive">
          <table
            className="table table-bordered table-hover shadow rounded mx-auto"
            style={{ maxWidth: "900px" }}
          >
            <thead className="table-dark">
              <tr>
                <th scope="col" className="text-center align-middle">
                  Identyfikator usługi
                </th>
                <th scope="col" className="text-center align-middle">
                  Usługa
                </th>
                <th scope="col" className="text-center align-middle">
                  Koszt
                </th>
                <th scope="col" className="text-center align-middle">
                  Akcja
                </th>
              </tr>
            </thead>
            <tbody>
              {currentData.length > 0 ? (
                currentData.map((offer) => (
                  <OfferRow
                    key={offer.idOffer}
                    offer={offer}
                    onEdit={handleEditClick}
                    onDelete={handleAskDeleteOffer}
                  />
                ))
              ) : (
                <tr>
                  <td colSpan="4" className="text-center py-4">
                    <Alert variant="info" className="mb-0">
                      Brak wyników do wyświetlenia.
                    </Alert>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        {totalPages > 1 && (
          <nav className="pagination justify-content-center mt-4">
            <ul className="pagination">
              {/* previous button */}
              <li
                className={`page-item ${currentPage === 1 ? "disabled" : ""}`}
              >
                <button
                  className="page-link"
                  onClick={() => handlePageClick(currentPage - 1)}
                  disabled={currentPage === 1}
                  aria-label="Poprzednia"
                  style={{ minWidth: "38px" }}
                >
                  <FontAwesomeIcon icon={faChevronLeft} />
                </button>
              </li>
              {/* page number buttons */}
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
                    style={{ minWidth: "38px" }}
                  >
                    {index + 1}
                  </button>
                </li>
              ))}
              {/* next button */}
              <li
                className={`page-item ${
                  currentPage === totalPages ? "disabled" : ""
                }`}
              >
                <button
                  className="page-link"
                  onClick={() => handlePageClick(currentPage + 1)}
                  disabled={currentPage === totalPages}
                  aria-label="Następna"
                  style={{ minWidth: "38px" }}
                >
                  <FontAwesomeIcon icon={faChevronRight} />
                </button>
              </li>
            </ul>
          </nav>
        )}
      </div>
      <ConfirmDeleteModal
        show={showDeleteModal}
        onHide={() => setShowDeleteModal(false)}
        onConfirm={confirmDeleteOffer}
        itemName={offerToDelete?.kind}
        label="usługę"
      />
    </div>
  );
};

export default OffersTable;
