import useOffers from "../hooks/useOffers";
import useSortableData from "../hooks/useSortableData";
import LoadingSpinner from "../components/common/LoadingSpinner";
import { Alert } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faScissors } from "@fortawesome/free-solid-svg-icons";
import useTableData from "../hooks/useTableData";
import PaginationControl from "../components/common/PaginationControl";
import SearchBox from "../components/common/SearchBox";
import SortableTableHeader from "../components/SortableTableHeader";
import { useTranslation } from "react-i18next";
import "../adminpages/styles/AdminTables.css";

const offerHeaders = [
  "offers.tableId",
  "offers.tableKind",
  "offers.tablePrice",
];

const fields = ["idOffer", "kind", "cost"];

const Offer = () => {
  const { offers, isLoading, error } = useOffers();
  const { t } = useTranslation();

  const filterOffers = (offer, term) => {
    const searchStr = `${offer.idOffer} ${offer.kind} ${offer.cost}`;
    return searchStr.toLowerCase().includes(term.toLowerCase());
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

  // human-readable label for each column, reused as the data-label on mobile cards
  const headerLabels = offerHeaders.map((key) => t(key));

  const formatCell = (offer, field) =>
    field === "cost" ? `${offer[field]} ${t("common.currency")}` : offer[field];

  if (isLoading) return <LoadingSpinner text={t("offers.loading")} />;

  if (error) return <Alert variant="danger">{error}</Alert>;

  return (
    <div className="container my-5 py-4">
      <h2 className="mb-4 text-center">
        <FontAwesomeIcon icon={faScissors} className="me-2" />
        {t("offers.title")}
      </h2>

      {/* search box */}
      <div className="mx-auto" style={{ maxWidth: "700px" }}>
        <SearchBox
          value={searchTerm}
          onChange={handleSearchChange}
          placeholder={t("offers.searchPlaceholder")}
        />
      </div>

      {/* table: switches to stacked cards below the mobile breakpoint */}
      <div className="rtable-wrap mx-auto mt-3" style={{ maxWidth: "700px" }}>
        <table className="table rtable align-middle shadow-sm rounded overflow-hidden mb-0">
          <SortableTableHeader
            headers={offerHeaders}
            fields={fields}
            sortConfig={sortConfig}
            onSort={handleHeaderSort}
          />

          <tbody>
            {currentData.length > 0 ? (
              currentData.map((offer) => (
                <tr key={offer.idOffer}>
                  {fields.map((field, idx) => (
                    <td
                      key={field}
                      data-label={headerLabels[idx]}
                      className="text-center align-middle"
                    >
                      {formatCell(offer, field)}
                    </td>
                  ))}
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={fields.length} className="text-center py-4">
                  <Alert variant="info" className="mb-0">
                    {t("offers.noResults")}
                  </Alert>
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* pagination control */}
      <PaginationControl
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
      />

      <p className="lead text-center mt-5">{t("offers.footerLead")}</p>
    </div>
  );
};

export default Offer;
