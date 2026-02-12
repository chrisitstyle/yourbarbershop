import { useState, useEffect } from "react";
import { useAuth } from "../AuthContext";
import { useParams, useLocation } from "react-router-dom";
import { Alert } from "react-bootstrap";
import LoadingSpinner from "../components/common/LoadingSpinner";
import useUserDetails from "../hooks/useUserDetails";
import useTableData from "../hooks/useTableData";
import PaginationControl from "../components/common/PaginationControl";
import SearchBox from "../components/common/SearchBox";
import { getNestedValue } from "../utils/tableHelpers";
import { useTranslation } from "react-i18next";

const visitHeaders = [
  "profile.table.id",
  "profile.table.service",
  "profile.table.cost",
  "profile.table.orderDate",
  "profile.table.visitDate",
  "profile.table.status",
];

const visitFields = [
  "idOrder",
  "offer.kind",
  "offer.cost",
  "orderDate",
  "visitDate",
  "status",
];

const Profile = () => {
  const { user } = useAuth();
  const { id } = useParams();
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const registrationOrderSuccess = searchParams.get("registrationOrderSuccess");
  const { t } = useTranslation();

  const { userDetails, isLoading, error } = useUserDetails(id, user?.token);

  const [showSuccessAlert, setShowSuccessAlert] = useState(
    Boolean(registrationOrderSuccess),
  );

  useEffect(() => {
    let timeout;
    if (showSuccessAlert) {
      timeout = setTimeout(() => setShowSuccessAlert(false), 3000);
    }
    return () => clearTimeout(timeout);
  }, [showSuccessAlert]);

  const filterVisits = (order, term) => {
    return `${order.idOrder} ${order.offer?.kind || ""} ${
      order.offer?.cost || ""
    } ${order.orderDate} ${order.visitDate} ${order.status}`
      .toLowerCase()
      .includes(term.toLowerCase());
  };

  const safeOrders = userDetails?.userOrders || [];
  const {
    searchTerm,
    handleSearchChange,
    currentData,
    currentPage,
    totalPages,
    setCurrentPage,
  } = useTableData(safeOrders, filterVisits);

  if (isLoading) return <LoadingSpinner text={t("profile.loading")} />;

  if (error) {
    return (
      <Alert variant="danger" className="text-center">
        {error}
      </Alert>
    );
  }

  return (
    <div className="container my-5 py-4 text-center">
      <div>
        {showSuccessAlert && (
          <Alert
            variant="success"
            className="text-center mx-auto"
            style={{ maxWidth: 440 }}
          >
            {t("profile.successOrder")}
          </Alert>
        )}

        <h2 className="mb-3">{t("profile.title")}</h2>
        <div
          className="mb-2"
          style={{ fontWeight: "500", fontSize: "1.06rem" }}
        >
          {t("profile.loggedInAs") + " "}
          <span className="fw-bold">
            {userDetails?.email ?? t("profile.noData")}
          </span>
        </div>
        <div className="mb-4" style={{ color: "#666" }}>
          {(userDetails?.firstname ?? t("profile.defaultUser")) +
            t("profile.visitInfo")}
        </div>

        {/* search box */}
        <SearchBox
          value={searchTerm}
          onChange={handleSearchChange}
          placeholder={t("profile.searchPlaceholder")}
        />

        {/* table */}
        <div className="table-responsive">
          <table
            className="table table-bordered table-hover shadow rounded mx-auto"
            style={{ maxWidth: "900px" }}
          >
            <thead className="table-dark">
              <tr>
                {visitHeaders.map((header) => (
                  <th
                    key={header}
                    scope="col"
                    className="text-center align-middle"
                  >
                    {t(header)}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {currentData.length > 0 ? (
                currentData.map((order) => (
                  <tr key={order.idOrder}>
                    {visitFields.map((field) => (
                      <td key={field} className="align-middle text-center">
                        {getNestedValue(order, field)}
                        {field === "offer.cost"
                          ? ` ${t("common.currency")}`
                          : ""}
                      </td>
                    ))}
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={visitFields.length} className="text-center py-4">
                    <Alert variant="info" className="mb-0">
                      {t("profile.noResults")}
                    </Alert>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* pagination */}
        <PaginationControl
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={setCurrentPage}
        />
      </div>
    </div>
  );
};

export default Profile;
