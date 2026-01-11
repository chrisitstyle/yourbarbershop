import { useState, memo } from "react";
import { useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faPen,
  faTrashAlt,
  faEnvelope,
} from "@fortawesome/free-solid-svg-icons";
import { Alert } from "react-bootstrap";
import { sendCustomEmail } from "../api/emailService";
import { useAuth } from "../AuthContext";
import useUsers from "../hooks/useUsers";
import useTableData from "../hooks/useTableData";
import useDeleteModal from "../hooks/useDeleteModal";
import LoadingSpinner from "../components/common/LoadingSpinner";
import ConfirmDeleteModal from "../components/common/ConfirmDeleteModal";
import EmailMessageModal from "../components/EmailMessageModal";
import PaginationControl from "../components/common/PaginationControl";
import SearchBox from "../components/common/SearchBox";

const userFieldsHeaders = [
  "Identyfikator użytkownika",
  "Imię",
  "Nazwisko",
  "E-mail",
  "Rola",
];
const userFields = ["idUser", "firstname", "lastname", "email", "role"];

const UserRow = memo(function UserRow({ user, onEdit, onEmail, onDelete }) {
  return (
    <tr>
      {userFields.map((field) => (
        <td key={field} className="align-middle text-center">
          {user[field]}
        </td>
      ))}
      <td className="align-middle text-center">
        <button
          className="btn btn-primary btn-sm mx-1"
          title="Wyślij e-mail"
          onClick={() => onEmail(user)}
          style={{ minWidth: "38px" }}
        >
          <FontAwesomeIcon icon={faEnvelope} />
        </button>
        <button
          className="btn btn-warning btn-sm mx-1"
          title="Edytuj"
          onClick={() => onEdit(user)}
          style={{ minWidth: "38px" }}
        >
          <FontAwesomeIcon icon={faPen} />
        </button>
        <button
          className="btn btn-danger btn-sm mx-1"
          title="Usuń"
          onClick={() => onDelete(user)}
          style={{ minWidth: "38px" }}
        >
          <FontAwesomeIcon icon={faTrashAlt} />
        </button>
      </td>
    </tr>
  );
});

const UsersTable = ({ onDeleteUser }) => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { users, isLoading, error, refetch } = useUsers(user?.token);

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
  const {
    searchTerm,
    handleSearchChange,
    currentData,
    currentPage,
    totalPages,
    setCurrentPage,
  } = useTableData(safeUsers, filterUsers);

  const {
    show: showDeleteModal,
    setShow: setShowDeleteModal,
    itemToDelete: userToDelete,
    askDelete: handleAskDeleteUser,
    confirmDelete,
  } = useDeleteModal((item) => onDeleteUser(item.idUser), refetch);

  // email handlers
  const handleEmailClick = (user) => {
    setEmailTo(user.email);
    setShowEmailModal(true);
  };

  const handleEmailSend = () => {
    sendCustomEmail(emailTo, emailSubject, emailMessage);
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

  if (isLoading) return <LoadingSpinner text="Ładowanie użytkowników..." />;
  if (error) return <Alert variant="danger">{error}</Alert>;

  return (
    <>
      <div className="container text-center py-4">
        <h2>Użytkownicy</h2>
        <SearchBox
          value={searchTerm}
          onChange={handleSearchChange}
          placeholder="Szukaj użytkownika..."
        />

        <div className="table-responsive">
          <table
            className="table border shadow table-hover mx-auto"
            style={{ maxWidth: "900px" }}
          >
            <thead className="table-dark">
              <tr>
                {userFieldsHeaders.map((header) => (
                  <th key={header} className="text-center align-middle">
                    {header}
                  </th>
                ))}
                <th className="text-center align-middle">Akcja</th>
              </tr>
            </thead>
            <tbody>
              {currentData.length > 0 ? (
                currentData.map((user) => (
                  <UserRow
                    key={user.idUser}
                    user={user}
                    onEdit={handleEditClick}
                    onEmail={handleEmailClick}
                    onDelete={handleAskDeleteUser}
                  />
                ))
              ) : (
                <tr>
                  <td
                    colSpan={userFields.length + 1}
                    className="text-center py-4"
                  >
                    Brak wyników.
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
        itemName={
          userToDelete
            ? `${userToDelete.firstname} ${userToDelete.lastname}`
            : ""
        }
        label="użytkownika"
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
