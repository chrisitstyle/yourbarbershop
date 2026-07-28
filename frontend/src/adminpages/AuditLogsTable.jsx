import { useState, memo } from "react";
import { useQuery } from "@tanstack/react-query";
import { Alert, Modal, Button, Badge } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faClipboardList,
  faEye,
  faInfoCircle,
} from "@fortawesome/free-solid-svg-icons";
import useTableData from "../hooks/useTableData";
import useSortableData from "../hooks/useSortableData";
import LoadingSpinner from "../components/common/LoadingSpinner";
import PaginationControl from "../components/common/PaginationControl";
import SearchBox from "../components/common/SearchBox";
import SortableTableHeader from "../components/SortableTableHeader";
import { StatusBadge } from "../components/common/StatusBadge";
import { fetchAuditLogs } from "../api/auditService";
import { useTranslation } from "react-i18next";
import "./styles/AdminTables.css";

const headers = [
  "admin.audit.id",
  "admin.audit.timestamp",
  "admin.audit.actor",
  "admin.audit.action",
  "admin.audit.entityType",
  "admin.audit.entityId",
];

const fields = [
  "id",
  "timestamp",
  "actorEmail",
  "action",
  "entityType",
  "entityId",
];

// map field -> header key for mobile card labels
const fieldLabels = fields.reduce((acc, field, idx) => {
  acc[field] = headers[idx];
  return acc;
}, {});

/**
 * formats a utc timestamp string to readable local datetime
 */
function formatTimestamp(value) {
  if (!value) return "—";
  return new Date(value).toLocaleString(undefined, {
    dateStyle: "short",
    timeStyle: "medium",
  });
}

/**
 * safely parses json details — returns object or null
 */
function parseDetails(raw) {
  if (!raw) return null;
  try {
    return typeof raw === "string" ? JSON.parse(raw) : raw;
  } catch {
    return raw;
  }
}

/**
 * renders a single audit log row with mobile card support
 */
const AuditRow = memo(function AuditRow({ log, onViewDetails }) {
  const { t } = useTranslation();
  const parsed = parseDetails(log.details);

  return (
    <tr>
      {fields.map((field) => (
        <td
          key={field}
          data-label={t(fieldLabels[field])}
          className="align-middle text-center"
        >
          {field === "action" ? (
            <StatusBadge value={log[field]} />
          ) : field === "entityType" ? (
            <StatusBadge value={log[field]} />
          ) : field === "timestamp" ? (
            <span className="text-nowrap">{formatTimestamp(log[field])}</span>
          ) : (
            <span>{log[field] ?? "—"}</span>
          )}
        </td>
      ))}

      {/* actions column */}
      <td
        className="align-middle text-center rtable-actions"
        data-label={t("admin.common.action")}
      >
        {parsed ? (
          <button
            type="button"
            className="btn btn-outline-info btn-sm d-inline-flex align-items-center gap-1"
            onClick={() => onViewDetails(parsed)}
            aria-label={t("admin.audit.details")}
          >
            <FontAwesomeIcon icon={faEye} />
            <span className="d-none d-md-inline">
              {t("admin.audit.details")}
            </span>
          </button>
        ) : (
          <span className="text-body-tertiary">—</span>
        )}
      </td>
    </tr>
  );
});

const AuditLogsTable = () => {
  const { t } = useTranslation();
  const [selectedDetails, setSelectedDetails] = useState(null);

  const {
    data: logs = [],
    isLoading,
    error,
  } = useQuery({
    queryKey: ["auditLogs"],
    queryFn: fetchAuditLogs,
  });

  const filterLogs = (log, term) =>
    `${log.id} ${log.actorEmail} ${log.action} ${log.entityType} ${log.entityId ?? ""}`
      .toLowerCase()
      .includes(term.toLowerCase());

  const { sortedData, sortConfig, handleSort } = useSortableData(logs, {
    field: "timestamp",
    direction: "desc",
  });

  const {
    searchTerm,
    handleSearchChange,
    currentData,
    currentPage,
    totalPages,
    setCurrentPage,
  } = useTableData(sortedData, filterLogs);

  const handleHeaderSort = (field) => {
    handleSort(field);
    setCurrentPage(1);
  };

  if (isLoading) return <LoadingSpinner text={t("admin.audit.loading")} />;
  if (error) return <Alert variant="danger">{error.message}</Alert>;

  return (
    <div className="container my-5 py-4 text-center">
      {/* section header */}
      <div className="mb-4">
        <span className="rtable-eyebrow">
          <FontAwesomeIcon icon={faClipboardList} className="me-2" />
          {t("admin.menu.audit", "Audit")}
        </span>
        <h2 className="fw-bold mt-1">{t("admin.audit.title")}</h2>
        {logs.length > 0 && (
          <Badge bg="secondary" className="mt-1">
            {logs.length} {t("admin.audit.entries", "entries")}
          </Badge>
        )}
      </div>

      {/* search */}
      <SearchBox
        value={searchTerm}
        onChange={handleSearchChange}
        placeholder={t("admin.audit.searchPlaceholder")}
      />

      {/* responsive table */}
      <div className="rtable-wrap mx-auto" style={{ maxWidth: "1200px" }}>
        <table className="table table-hover align-middle rtable mb-0">
          <SortableTableHeader
            headers={headers}
            fields={fields}
            sortConfig={sortConfig}
            onSort={handleHeaderSort}
          >
            <th className="text-center align-middle">
              {t("admin.common.action")}
            </th>
          </SortableTableHeader>

          <tbody>
            {currentData.length > 0 ? (
              currentData.map((log) => (
                <AuditRow
                  key={log.id}
                  log={log}
                  onViewDetails={setSelectedDetails}
                />
              ))
            ) : (
              <tr>
                <td colSpan={fields.length + 1} className="py-5 rtable-empty">
                  <div className="d-flex flex-column align-items-center gap-2 text-body-secondary">
                    <FontAwesomeIcon icon={faInfoCircle} size="2x" />
                    <span>{t("admin.common.noResults")}</span>
                  </div>
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

      {/* details modal */}
      <Modal
        show={Boolean(selectedDetails)}
        onHide={() => setSelectedDetails(null)}
        centered
        size="lg"
      >
        <Modal.Header closeButton>
          <Modal.Title>
            <FontAwesomeIcon icon={faClipboardList} className="me-2" />
            {t("admin.audit.modalTitle")}
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <pre
            className="bg-body-tertiary p-3 rounded text-start mb-0"
            style={{ overflowX: "auto", fontSize: "0.8rem", lineHeight: 1.6 }}
          >
            {JSON.stringify(selectedDetails, null, 2)}
          </pre>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setSelectedDetails(null)}>
            {t("common.close")}
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};

export default AuditLogsTable;
