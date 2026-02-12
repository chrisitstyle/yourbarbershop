import useOffers from "../hooks/useOffers";
import LoadingSpinner from "../components/common/LoadingSpinner";
import { Alert } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faScissors } from "@fortawesome/free-solid-svg-icons";
import useTableData from "../hooks/useTableData";
import PaginationControl from "../components/common/PaginationControl";
import SearchBox from "../components/common/SearchBox";
import { useTranslation } from "react-i18next";

const fields = ["idOffer", "kind", "cost"];

const Offer = () => {
  const { offers, isLoading, error } = useOffers();
  const { t } = useTranslation();

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

  if (isLoading) return <LoadingSpinner text={t("offers.loading")} />;

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
          {t("offers.title")}
        </h1>
        <Alert variant="info" className="text-center">
          {t("offers.empty")}
        </Alert>
      </div>
    );
  }

  return (
    <div className="container my-5 py-4">
      <h1 className="display-6 text-center mb-4">
        <FontAwesomeIcon icon={faScissors} className="me-2 text-primary" />
        {t("offers.title")}
      </h1>
      <p className="lead text-center mb-4">{t("offers.lead")}</p>

      {/* search box */}
      <SearchBox
        value={searchTerm}
        onChange={handleSearchChange}
        placeholder={t("offers.searchPlaceholder")}
        width="400px"
      />

      <div className="table-responsive">
        <table
          className="table table-bordered table-hover mx-auto shadow rounded"
          style={{ maxWidth: "700px" }}
        >
          <thead className="table-dark">
            <tr>
              <th className="text-center align-middle">
                {t("offers.tableId")}
              </th>
              <th className="text-center align-middle">
                {t("offers.tableKind")}
              </th>
              <th className="text-center align-middle">
                {t("offers.tablePrice")}
              </th>
            </tr>
          </thead>
          <tbody>
            {currentData.length > 0 ? (
              currentData.map((offer) => (
                <tr key={offer.idOffer}>
                  {fields.map((field) => (
                    <td key={field} className="text-center align-middle">
                      {field === "cost"
                        ? `${offer[field]} ${t("common.currency")}`
                        : offer[field]}
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
