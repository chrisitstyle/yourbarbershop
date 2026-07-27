import { useState, memo } from "react";
import { useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faPen,
  faTrashAlt,
  faEnvelope,
} from "@fortawesome/free-solid-svg-icons";
import { Alert } from "react-bootstrap";
import { toast } from "sonner";
import { sendCustomEmail } from "../api/emailService";
import useUsers from "../hooks/useUsers";
import useTableData from "../hooks/useTableData";
import useSortableData from "../hooks/useSortableData";
import useDeleteModal from "../hooks/useDeleteModal";
import LoadingSpinner from "../components/common/LoadingSpinner";
import ConfirmDeleteModal from "../components/common/ConfirmDeleteModal";
import EmailMessageModal from "../components/EmailMessageModal";
import PaginationControl from "../components/common/PaginationControl";
import SearchBox from "../components/common/SearchBox";
import SortableTableHeader from "../components/SortableTableHeader";
import { useTranslation } from "react-i18next";
import { StatusBadge } from "./utils/adminTableHelpers";
import "./styles/AdminTables.css";

const userFieldsHeaders = [
  "admin.users.id",
  "admin.users.firstname",
  "admin.users.lastname",
  "admin.users.email",
  "admin.users.role",
];

const userFields = ["idUser", "firstname", "lastname", "email", "role"];

const UserRow = memo(function UserRow({
  user,
  onEdit,
  onEmail,
  onDelete,
  headerLabels,
}) {
  const { t } = useTranslation();

  return (
    <tr>
      {userFields.map((field, i) => (
        <td
          key={field}
          className="align-middle text-center"
          data-label={headerLabels[i]}
        >
          {field === "role" ? <StatusBadge value={user[field]} /> : user[field]}
        </td>
      ))}
      <td
        className="align-middle text-center"
        data-label={t("admin.common.action")}
      >
        <div className="d-flex justify-content-center gap-2">
          <button
            className="btn btn-primary btn-sm"
            title={t("admin.common.sendEmail")}
            onClick={() => onEmail(user)}
            style={{ minWidth: "38px" }}
          >
            <FontAwesomeIcon icon={faEnvelope} />
          </button>

          <button
            className="btn btn-warning btn-sm"
            title={t("admin.common.edit")}
            onClick={() => onEdit(user)}
            style={{ minWidth: "38px" }}
          >
            <FontAwesomeIcon icon={faPen} />
          </button>

          <button
            className="btn btn-danger btn-sm"
            title={t("admin.common.delete")}
            onClick={() => onDelete(user)}
            style={{ minWidth: "38px" }}
          >
            <FontAwesomeIcon icon={faTrashAlt} />
          </button>
        </div>
      </td>
    </tr>
  );
});

const UsersTable = ({ onDeleteUser }) => {
  const navigate = useNavigate();
  const { users, isLoading, error, refetch } = useUsers();
  const { t } = useTranslation();

  const headerLabels = userFieldsHeaders.map((key) => t(key));

  // email state
  const [showEmailModal, setShowEmailModal] = useState(false);
  const [emailTo, setEmailTo] = useState("");
  const [emailSubject, setEmailSubject] = useState("");
  const [emailMessage, setEmailMessage] = useState("");

  const filterUsers = (user, term) => {
    return ` ${user.idUser} ${user.firstname} ${user.lastname} ${user.email} ${user.role}`
      .toLowerCase()
      .includes(term.toLowerCase());
  };

  const safeUsers = Array.isArray(users) ? users : [];

  const { sortedData, sortConfig, handleSort } = useSortableData(safeUsers, {
    field: "idUser",
    direction: "asc",
  });

  const {
    searchTerm,
    handleSearchChange,
    currentData,
    currentPage,
    totalPages,
    setCurrentPage,
  } = useTableData(sortedData, filterUsers);

  const handleHeaderSort = (field) => {
    handleSort(field);
    setCurrentPage(1);
  };

  const {
    show: showDeleteModal,
    setShow: setShowDeleteModal,
    itemToDelete: userToDelete,
    askDelete: handleAskDeleteUser,
    confirmDelete,
  } = useDeleteModal(async (item) => {
    try {
      await onDeleteUser(item.idUser);
      toast.success(
        t(
          "admin.messages.deleteUserSuccess",
          "Pomyślnie usunięto użytkownika.",
        ),
      );
    } catch (err) {
      console.error("error deleting user:", err);
      const status = err?.status || err?.response?.status;
      const errorMsg = err?.data || err?.message;

      // handle self deletion error using i18n key instead of hardcoded backend message
      if (
        status === 403 ||
        (typeof errorMsg === "string" && errorMsg.toLowerCase().includes("sam"))
      ) {
        toast.error(t("admin.messages.deleteSelfError"));
      } else if (typeof errorMsg === "string" && errorMsg) {
        toast.error(errorMsg);
      } else {
        toast.error(t("admin.messages.deleteUserError"));
      }
    }
  }, refetch);

  // email handlers
  const handleEmailClick = (user) => {
    setEmailTo(user.email);
    setShowEmailModal(true);
  };

  const handleEmailSend = async () => {
    try {
      await sendCustomEmail(emailTo, emailSubject, emailMessage);
      toast.success(
        t("admin.messages.emailSentSuccess", "Pomyślnie wysłano e-mail."),
      );
    } catch (err) {
      console.error("error sending email:", err);
      const errorMsg = err?.data || err?.message;

      if (typeof errorMsg === "string" && errorMsg) {
        toast.error(errorMsg);
      } else {
        toast.error(
          t("admin.messages.emailSentError", "Błąd podczas wysyłania e-maila."),
        );
      }
    }
    setShowEmailModal(false);
    setEmailTo("");
    setEmailSubject("");
    setEmailMessage("");
  };

  const handleEditClick = (user) => {
    navigate(`/adminpanel/edituser/${user.idUser}`, {
      state: { userData: user },
    });
  };

  if (isLoading) return <LoadingSpinner text={t("admin.users.loading")} />;

  if (error) return <Alert variant="danger">{error}</Alert>;

  return (
    <>
      <div className="container text-center py-4">
        <h2>{t("admin.users.title")}</h2>

        {/* search box */}
        <SearchBox
          value={searchTerm}
          onChange={handleSearchChange}
          placeholder={t("admin.users.searchPlaceholder")}
        />

        {/* responsive table -> collapses to cards on mobile */}
        <div
          className="rtable-wrap shadow-sm rounded mx-auto"
          style={{ maxWidth: "900px" }}
        >
          <table className="table table-hover align-middle mb-0 rtable">
            <SortableTableHeader
              headers={userFieldsHeaders}
              fields={userFields}
              sortConfig={sortConfig}
              onSort={handleHeaderSort}
            >
              <th className="text-center align-middle">
                {t("admin.common.action")}
              </th>
            </SortableTableHeader>

            <tbody>
              {currentData.length > 0 ? (
                currentData.map((user) => (
                  <UserRow
                    key={user.idUser}
                    user={user}
                    onEdit={handleEditClick}
                    onEmail={handleEmailClick}
                    onDelete={handleAskDeleteUser}
                    headerLabels={headerLabels}
                  />
                ))
              ) : (
                <tr>
                  <td
                    colSpan={userFields.length + 1}
                    className="text-center py-4"
                  >
                    {t("admin.common.noResults")}
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

      <ConfirmDeleteModal
        show={showDeleteModal}
        onHide={() => setShowDeleteModal(false)}
        onConfirm={confirmDelete}
        itemName={
          userToDelete
            ? `${userToDelete.firstname} ${userToDelete.lastname}`
            : ""
        }
        label={t("admin.users.deleteLabel")}
      />

      <EmailMessageModal
        show={showEmailModal}
        handleClose={() => setShowEmailModal(false)}
        emailTo={emailTo}
        emailSubject={emailSubject}
        setEmailSubject={setEmailSubject}
        emailMessage={emailMessage}
        setEmailMessage={setEmailMessage}
        handleEmailSend={handleEmailSend}
        resetEmailFields={() => {
          setEmailTo("");
          setEmailSubject("");
          setEmailMessage("");
        }}
      />
    </>
  );
};

export default UsersTable;
