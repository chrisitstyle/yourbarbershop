import { useState, useMemo, memo } from "react";
import { useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faPen,
  faTrashAlt,
  faEnvelope,
  faChevronLeft,
  faChevronRight,
} from "@fortawesome/free-solid-svg-icons";
import { sendCustomEmail } from "../api/emailService";
import EmailMessageModal from "../components/EmailMessageModal";
import { useAuth } from "../AuthContext";
import useUsers from "../hooks/useUsers";
import LoadingSpinner from "../components/common/LoadingSpinner";
import { Alert } from "react-bootstrap";
import ConfirmDeleteModal from "../components/common/ConfirmDeleteModal";

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
          <FontAwesomeIcon icon={faPen} style={{ color: "black" }} />
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
  const usersPerPage = 10;
  const [currentPage, setCurrentPage] = useState(1);
  const [searchTerm, setSearchTerm] = useState("");

  const [showEmailModal, setShowEmailModal] = useState(false);
  const [emailTo, setEmailTo] = useState("");
  const [emailSubject, setEmailSubject] = useState("");
  const [emailMessage, setEmailMessage] = useState("");

  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [userToDelete, setUserToDelete] = useState(null);

  const { user } = useAuth();
  const { users, isLoading, error, refetch } = useUsers(user?.token);

  const safeUsers = Array.isArray(users) ? users : [];

  const filteredUsers = useMemo(
    () =>
      safeUsers.filter((user) =>
        ` ${user.idUser} ${user.firstname} ${user.lastname} ${user.email} ${user.role}`
          .toLowerCase()
          .includes(searchTerm.toLowerCase())
      ),
    [safeUsers, searchTerm]
  );

  const totalPages = useMemo(
    () => Math.ceil(filteredUsers.length / usersPerPage),
    [filteredUsers.length, usersPerPage]
  );

  const currentData = useMemo(
    () =>
      filteredUsers.slice(
        (currentPage - 1) * usersPerPage,
        currentPage * usersPerPage
      ),
    [filteredUsers, currentPage, usersPerPage]
  );

  // handlers
  const handlePageClick = (page) => setCurrentPage(page);

  const handleEditClick = (user) => {
    navigate(`/adminpanel/edituser/${user.idUser}`, {
      state: { userData: user },
    });
  };

  const handleEmailClick = (user) => {
    setEmailTo(user.email);
    setShowEmailModal(true);
  };

  const handleEmailSend = () => {
    sendCustomEmail(emailTo, emailSubject, emailMessage);
    setShowEmailModal(false);
    resetEmailFields();
  };

  const resetEmailFields = () => {
    setEmailTo("");
    setEmailSubject("");
    setEmailMessage("");
  };

  const handleAskDeleteUser = (user) => {
    setUserToDelete(user);
    setShowDeleteModal(true);
  };

  const confirmDeleteUser = async () => {
    if (userToDelete) {
      await onDeleteUser(userToDelete.idUser);
      await refetch();
    }
    setShowDeleteModal(false);
    setUserToDelete(null);
  };

  if (isLoading) return <LoadingSpinner text="Ładowanie użytkowników..." />;
  if (error) return <Alert variant="danger">{error}</Alert>;

  return (
    <>
      <div className="container text-center">
        <div className="py-4">
          <h2>Użytkownicy</h2>
          {/* search box */}
          <div className="mb-3 mt-4">
            <input
              type="text"
              placeholder="Szukaj użytkownika..."
              value={searchTerm}
              onChange={(e) => {
                setSearchTerm(e.target.value);
                setCurrentPage(1);
              }}
              className="form-control mx-auto"
              style={{ width: "220px" }}
            />
          </div>
          <div className="table-responsive">
            <table
              className="table border shadow table-hover mx-auto"
              style={{ maxWidth: "900px" }}
            >
              <thead className="table-dark">
                <tr>
                  {userFieldsHeaders.map((header, idx) => (
                    <th
                      key={userFields[idx]}
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
                      Brak wyników do wyświetlenia.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
          {/* pagination with arrows */}
          {totalPages > 1 && (
            <nav className="pagination justify-content-center mt-4">
              <ul className="pagination">
                {/* previous */}
                <li
                  className={`page-item ${currentPage === 1 ? "disabled" : ""}`}
                >
                  <button
                    className="page-link"
                    onClick={() => handlePageClick(currentPage - 1)}
                    disabled={currentPage === 1}
                    aria-label="Poprzednia"
                    style={{ minWidth: "38px" }}
                  >
                    <FontAwesomeIcon icon={faChevronLeft} />
                  </button>
                </li>
                {[...Array(totalPages)].map((_, index) => (
                  <li
                    key={index + 1}
                    className={`page-item ${
                      index + 1 === currentPage ? "active" : ""
                    }`}
                  >
                    <button
                      className="page-link"
                      onClick={() => handlePageClick(index + 1)}
                      style={{ minWidth: "38px" }}
                    >
                      {index + 1}
                    </button>
                  </li>
                ))}
                {/* next */}
                <li
                  className={`page-item ${
                    currentPage === totalPages ? "disabled" : ""
                  }`}
                >
                  <button
                    className="page-link"
                    onClick={() => handlePageClick(currentPage + 1)}
                    disabled={currentPage === totalPages}
                    aria-label="Następna"
                    style={{ minWidth: "38px" }}
                  >
                    <FontAwesomeIcon icon={faChevronRight} />
                  </button>
                </li>
              </ul>
            </nav>
          )}
        </div>
      </div>
      <ConfirmDeleteModal
        show={showDeleteModal}
        onHide={() => setShowDeleteModal(false)}
        onConfirm={confirmDeleteUser}
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
        resetEmailFields={resetEmailFields}
      />
    </>
  );
};

export default UsersTable;
