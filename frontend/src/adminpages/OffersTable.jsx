import { memo } from "react";
import { useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPen, faTrashAlt } from "@fortawesome/free-solid-svg-icons";
import { Alert } from "react-bootstrap";
import useOffers from "../hooks/useOffers";
import useTableData from "../hooks/useTableData";
import useSortableData from "../hooks/useSortableData";
import useDeleteModal from "../hooks/useDeleteModal";
import LoadingSpinner from "../components/common/LoadingSpinner";
import ConfirmDeleteModal from "../components/common/ConfirmDeleteModal";
import PaginationControl from "../components/common/PaginationControl";
import SearchBox from "../components/common/SearchBox";
import SortableTableHeader from "../components/SortableTableHeader";
import { useTranslation } from "react-i18next";

const offerFieldsHeaders = [
  "admin.offers.id",
  "admin.offers.service",
  "admin.offers.cost",
];

const offerFields = ["idOffer", "kind", "cost"];

const OfferRow = memo(function OfferRow({ offer, onEdit, onDelete }) {
  const { t } = useTranslation();

  return (
    <tr>
      {offerFields.map((field) => (
        <td key={field} className="align-middle text-center">
          {field === "cost"
            ? `${offer[field]} ${t("common.currency")}`
            : offer[field]}
        </td>
      ))}
      <td className="align-middle text-center">
        <button
          className="btn btn-warning btn-sm me-2"
          style={{ minWidth: "40px" }}
          title={t("admin.common.edit")}
          onClick={() => onEdit(offer)}
        >
          <FontAwesomeIcon icon={faPen} />
        </button>
        <button
          className="btn btn-danger btn-sm"
          style={{ minWidth: "40px" }}
          title={t("admin.common.delete")}
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
  const { t } = useTranslation();

  const filterOffers = (offer, term) => {
    return ` ${offer.idOffer} ${offer.kind} ${offer.cost}`
      .toLowerCase()
      .includes(term.toLowerCase());
  };

  const safeOffers = Array.isArray(offers) ? offers : [];

  const { sortedData, sortConfig, handleSort } = useSortableData(safeOffers, {
    field: "idOffer",
    direction: "asc",
  });

  const {
    searchTerm,
    handleSearchChange,
    currentData,
    currentPage,
    totalPages,
    setCurrentPage,
  } = useTableData(sortedData, filterOffers);

  const handleHeaderSort = (field) => {
    handleSort(field);
    setCurrentPage(1);
  };

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

  if (isLoading) return <LoadingSpinner text={t("admin.offers.loading")} />;

  if (error) return <Alert variant="danger">{error}</Alert>;

  return (
    <div className="container my-5 py-4 text-center">
      <div>
        <h2 className="mb-4">{t("admin.offers.title")}</h2>

        <SearchBox
          value={searchTerm}
          onChange={handleSearchChange}
          placeholder={t("admin.offers.searchPlaceholder")}
        />

        <div className="table-responsive">
          <table
            className="table table-bordered table-hover shadow rounded mx-auto"
            style={{ maxWidth: "900px" }}
          >
            <SortableTableHeader
              headers={offerFieldsHeaders}
              fields={offerFields}
              sortConfig={sortConfig}
              onSort={handleHeaderSort}
            >
              <th scope="col" className="text-center align-middle">
                {t("admin.common.action")}
              </th>
            </SortableTableHeader>

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
                      {t("admin.common.noResults")}
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
        label={t("admin.offers.deleteLabel")}
      />
    </div>
  );
};

export default OffersTable;
