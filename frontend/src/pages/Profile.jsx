import { useState, useEffect, useMemo } from "react";
import { useParams, useLocation } from "react-router-dom";
import { Alert, Badge } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faCalendarCheck,
  faCircleCheck,
  faCircleXmark,
  faCalendarDays,
  faUserCircle,
  faInbox,
} from "@fortawesome/free-solid-svg-icons";
import LoadingSpinner from "../components/common/LoadingSpinner";
import useUserDetails from "../hooks/useUserDetails";
import useTableData from "../hooks/useTableData";
import useSortableData from "../hooks/useSortableData";
import PaginationControl from "../components/common/PaginationControl";
import SearchBox from "../components/common/SearchBox";
import SortableTableHeader from "../components/SortableTableHeader";
import { getNestedValue } from "../utils/tableHelpers";
import { useTranslation } from "react-i18next";
import { StatusBadge } from "../components/common/StatusBadge";
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
  "orderStatus",
  "paymentMethod",
  "paymentStatus",
];

// enum fields render as colored badges instead of raw text
const BADGE_FIELDS = new Set(["orderStatus", "paymentMethod", "paymentStatus"]);

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
    const orderStatusText = order.orderStatus
      ? t(`enums.${order.orderStatus}`)
      : "";

    const paymentMethodText = order.paymentMethod
      ? t(`enums.${order.paymentMethod}`)
      : "";

    const paymentStatusText = order.paymentStatus
      ? t(`enums.${order.paymentStatus}`)
      : "";

    return `${order.idOrder} ${order.offer?.kind || ""} ${
      order.offer?.cost || ""
    } ${order.orderDate} ${order.visitDate} ${
      order.orderStatus || ""
    } ${orderStatusText} ${order.paymentMethod || ""} ${paymentMethodText} ${
      order.paymentStatus || ""
    } ${paymentStatusText}`
      .toLowerCase()
      .includes(term.toLowerCase());
  };

  const safeOrders = useMemo(
    () => userDetails?.userOrders || [],
    [userDetails?.userOrders],
  );

  // derive quick stats from all orders (not just current page)
  const stats = useMemo(() => {
    const completed = safeOrders.filter(
      (order) => order.orderStatus === "ZREALIZOWANE",
    ).length;

    const cancelled = safeOrders.filter(
      (order) => order.orderStatus === "ANULOWANE",
    ).length;

    const upcoming = safeOrders.filter(
      (order) => order.orderStatus === "NOWE",
    ).length;

    return {
      total: safeOrders.length,
      completed,
      cancelled,
      upcoming,
    };
  }, [safeOrders]);

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

  /**
   * renders a single table cell wrapped in a jsx element to maintain consistent return type (S3800)
   *
   * @param {object} order - visit/order object
   * @param {string} field - field property path
   * @returns {JSX.Element|string|number} formatted jsx cell content
   */
  const renderCell = (order, field) => {
    const value = getNestedValue(order, field);

    if (BADGE_FIELDS.has(field)) {
      return value ? <StatusBadge value={value} /> : "—";
    }

    if (field === "offer.cost") {
      return value !== null && value !== undefined
        ? `${value} ${t("common.currency")}`
        : "—";
    }

    return value ?? "—";
  };

  if (isLoading) {
    return <LoadingSpinner text={t("profile.loading")} />;
  }

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
      <div className="text-center mb-5">
        {/* avatar circle with initials */}
        <div
          className="d-inline-flex align-items-center justify-content-center rounded-circle bg-body-secondary mb-3"
          style={{ width: 80, height: 80 }}
          aria-hidden="true"
        >
          <FontAwesomeIcon
            icon={faUserCircle}
            size="3x"
            className="text-body-tertiary"
          />
        </div>

        <h2 className="fw-bold mb-1">{t("profile.title")}</h2>

        <div className="d-inline-flex align-items-center gap-2 flex-wrap justify-content-center mb-1">
          <span className="text-body-secondary">{t("profile.loggedInAs")}</span>

          <Badge pill bg="dark" className="fs-6 fw-semibold">
            {userDetails?.email ?? t("profile.noData")}
          </Badge>
        </div>

        <p className="text-body-secondary mb-4">
          {(userDetails?.firstname ?? t("profile.defaultUser")) +
            t("profile.visitInfo")}
        </p>

        {/* quick stats row */}
        <div className="d-flex flex-wrap justify-content-center gap-3 mb-2">
          <div className="d-flex align-items-center gap-2 px-4 py-3 rounded-3 bg-body-secondary">
            <FontAwesomeIcon icon={faCalendarCheck} className="text-info" />

            <div className="text-start">
              <div className="fw-bold lh-1">{stats.total}</div>
              <div className="small text-body-secondary">
                {t("profile.stats.total")}
              </div>
            </div>
          </div>

          <div className="d-flex align-items-center gap-2 px-4 py-3 rounded-3 bg-body-secondary">
            <FontAwesomeIcon icon={faCircleCheck} className="text-success" />

            <div className="text-start">
              <div className="fw-bold lh-1">{stats.completed}</div>
              <div className="small text-body-secondary">
                {t("profile.stats.completed")}
              </div>
            </div>
          </div>

          <div className="d-flex align-items-center gap-2 px-4 py-3 rounded-3 bg-body-secondary">
            <FontAwesomeIcon icon={faCalendarDays} className="text-primary" />

            <div className="text-start">
              <div className="fw-bold lh-1">{stats.upcoming}</div>
              <div className="small text-body-secondary">
                {t("profile.stats.upcoming")}
              </div>
            </div>
          </div>

          <div className="d-flex align-items-center gap-2 px-4 py-3 rounded-3 bg-body-secondary">
            <FontAwesomeIcon icon={faCircleXmark} className="text-danger" />

            <div className="text-start">
              <div className="fw-bold lh-1">{stats.cancelled}</div>
              <div className="small text-body-secondary">
                {t("profile.stats.cancelled")}
              </div>
            </div>
          </div>
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
                  {visitFields.map((field, index) => (
                    <td
                      key={field}
                      className="text-center"
                      data-label={t(visitHeaders[index])}
                    >
                      {renderCell(order, field)}
                    </td>
                  ))}
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={visitFields.length} className="py-5 rtable-empty">
                  <div className="d-flex flex-column align-items-center gap-2 text-body-secondary">
                    <FontAwesomeIcon icon={faInbox} size="2x" />
                    <span>{t("profile.noResults")}</span>
                  </div>
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
