import { useState, useEffect } from "react";
import { useParams, useLocation } from "react-router-dom";
import { Alert } from "react-bootstrap";
import LoadingSpinner from "../components/common/LoadingSpinner";
import useUserDetails from "../hooks/useUserDetails";
import useTableData from "../hooks/useTableData";
import useSortableData from "../hooks/useSortableData";
import PaginationControl from "../components/common/PaginationControl";
import SearchBox from "../components/common/SearchBox";
import SortableTableHeader from "../components/SortableTableHeader";
import { getNestedValue } from "../utils/tableHelpers";
import { useTranslation } from "react-i18next";
import { StatusBadge } from "../adminpages/utils/adminTableHelpers";
import "../adminpages/styles/AdminTables.css";

const visitHeaders = [
  "profile.table.id",
  "profile.table.service",
  "profile.table.cost",
  "profile.table.orderDate",
  "profile.table.visitDate",
  "profile.table.status",
  "profile.table.paymentMethod",
  "profile.table.paymentStatus",
];

const visitFields = [
  "idOrder",
  "offer.kind",
  "offer.cost",
  "orderDate",
  "visitDate",
  "status",
  "paymentMethod",
  "paymentStatus",
];

// enum fields render as colored badges instead of raw text
const BADGE_FIELDS = new Set(["status", "paymentMethod", "paymentStatus"]);

const Profile = () => {
  const { id } = useParams();
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const registrationOrderSuccess = searchParams.get("registrationOrderSuccess");
  const { t } = useTranslation();

  const { userDetails, isLoading, error } = useUserDetails(id);

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
    } ${order.orderDate} ${order.visitDate} ${order.status} ${
      order.paymentMethod || ""
    } ${order.paymentStatus || ""}`
      .toLowerCase()
      .includes(term.toLowerCase());
  };

  const safeOrders = userDetails?.userOrders || [];

  const { sortedData, sortConfig, handleSort } = useSortableData(safeOrders, {
    field: "visitDate",
    direction: "desc",
  });

  const {
    searchTerm,
    handleSearchChange,
    currentData,
    currentPage,
    totalPages,
    setCurrentPage,
  } = useTableData(sortedData, filterVisits);

  const handleHeaderSort = (field) => {
    handleSort(field);
    setCurrentPage(1);
  };

  // render a single cell value, using badges for enum fields and currency suffix for cost
  const renderCell = (order, field) => {
    const value = getNestedValue(order, field);

    if (BADGE_FIELDS.has(field)) {
      return value ? <StatusBadge value={value} /> : "—";
    }
    if (field === "offer.cost") {
      return `${value} ${t("common.currency")}`;
    }
    return value ?? "—";
  };

  if (isLoading) return <LoadingSpinner text={t("profile.loading")} />;

  if (error) {
    return (
      <Alert variant="danger" className="text-center">
        {error}
      </Alert>
    );
  }

  return (
    <div className="container my-5 py-4">
      {showSuccessAlert && (
        <Alert
          variant="success"
          className="text-center mx-auto"
          style={{ maxWidth: 440 }}
        >
          {t("profile.successOrder")}
        </Alert>
      )}

      {/* profile header card */}
      <div className="admin-panel-header text-center mb-4">
        <h2 className="mb-3">{t("profile.title")}</h2>
        <div className="d-inline-flex align-items-center gap-2 flex-wrap justify-content-center">
          <span className="text-body-secondary">{t("profile.loggedInAs")}</span>
          <span className="badge rounded-pill text-bg-dark fs-6 fw-semibold">
            {userDetails?.email ?? t("profile.noData")}
          </span>
        </div>
        <div className="mt-2 text-body-secondary">
          {(userDetails?.firstname ?? t("profile.defaultUser")) +
            t("profile.visitInfo")}
        </div>
      </div>

      {/* search box */}
      <div className="mx-auto mb-3" style={{ maxWidth: "1100px" }}>
        <SearchBox
          value={searchTerm}
          onChange={handleSearchChange}
          placeholder={t("profile.searchPlaceholder")}
        />
      </div>

      {/* visits table (turns into stacked cards on mobile) */}
      <div className="rtable-wrap mx-auto" style={{ maxWidth: "1100px" }}>
        <table className="table table-hover align-middle rtable mb-0">
          <SortableTableHeader
            headers={visitHeaders}
            fields={visitFields}
            sortConfig={sortConfig}
            onSort={handleHeaderSort}
          />

          <tbody>
            {currentData.length > 0 ? (
              currentData.map((order) => (
                <tr key={order.idOrder}>
                  {visitFields.map((field, i) => (
                    <td
                      key={field}
                      className="text-center"
                      data-label={t(visitHeaders[i])}
                    >
                      {renderCell(order, field)}
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

      {/* pagination control */}
      <PaginationControl
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
      />
    </div>
  );
};

export default Profile;
