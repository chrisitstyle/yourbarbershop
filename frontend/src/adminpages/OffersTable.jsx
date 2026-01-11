import { memo } from "react";
import { useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPen, faTrashAlt } from "@fortawesome/free-solid-svg-icons";
import { Alert } from "react-bootstrap";
import useOffers from "../hooks/useOffers";
import useTableData from "../hooks/useTableData";
import useDeleteModal from "../hooks/useDeleteModal";
import LoadingSpinner from "../components/common/LoadingSpinner";
import ConfirmDeleteModal from "../components/common/ConfirmDeleteModal";
import PaginationControl from "../components/common/PaginationControl";
import SearchBox from "../components/common/SearchBox";

const offerFieldsHeaders = ["Identyfikator usługi", "Usługa", "Koszt"];
const offerFields = ["idOffer", "kind", "cost"];

const OfferRow = memo(function OfferRow({ offer, onEdit, onDelete }) {
  return (
    <tr>
      {offerFields.map((field) => (
        <td key={field} className="align-middle text-center">
          {field === "cost" ? `${offer[field]} zł` : offer[field]}
        </td>
      ))}
      <td className="align-middle text-center">
        <button
          className="btn btn-warning btn-sm me-2"
          style={{ minWidth: "40px" }}
          title="Edytuj"
          onClick={() => onEdit(offer)}
        >
          <FontAwesomeIcon icon={faPen} />
        </button>
        <button
          className="btn btn-danger btn-sm"
          style={{ minWidth: "40px" }}
          title="Usuń"
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
  const navigate = useNavigate();

  const filterOffers = (offer, term) => {
    return ` ${offer.idOffer} ${offer.kind} ${offer.cost}`
      .toLowerCase()
      .includes(term.toLowerCase());
  };

  const {
    searchTerm,
    handleSearchChange,
    currentData,
    currentPage,
    totalPages,
    setCurrentPage,
  } = useTableData(offers, filterOffers);

  const {
    show: showDeleteModal,
    setShow: setShowDeleteModal,
    itemToDelete: offerToDelete,
    askDelete: handleAskDelete,
    confirmDelete,
  } = useDeleteModal((item) => onDeleteOffer(item.idOffer), refetch);

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
        <SearchBox
          value={searchTerm}
          onChange={handleSearchChange}
          placeholder="Szukaj usługi..."
        />
        <div className="table-responsive">
          <table
            className="table table-bordered table-hover shadow rounded mx-auto"
            style={{ maxWidth: "900px" }}
          >
            <thead className="table-dark">
              <tr>
                {offerFieldsHeaders.map((header) => (
                  <th
                    key={header}
                    scope="col"
                    className="text-center align-middle"
                  >
                    {header}
                  </th>
                ))}
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
                    onDelete={handleAskDelete}
                  />
                ))
              ) : (
                <tr>
                  <td
                    colSpan={offerFields.length + 1}
                    className="text-center py-4"
                  >
                    <Alert variant="info" className="mb-0">
                      Brak wyników.
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
      </div>

      <ConfirmDeleteModal
        show={showDeleteModal}
        onHide={() => setShowDeleteModal(false)}
        onConfirm={confirmDelete}
        itemName={offerToDelete?.kind}
        label="usługę"
      />
    </div>
  );
};

export default OffersTable;
