import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faPen,
  faTrashAlt,
  faEnvelope,
} from "@fortawesome/free-solid-svg-icons";

const UsersTable = ({ data, onDeleteUser }) => {
  const navigate = useNavigate();
  const usersPerPage = 10;
  const [currentPage, setCurrentPage] = useState(1);
  const [searchTerm, setSearchTerm] = useState(""); // search input
  const indexOfLastUser = currentPage * usersPerPage;
  const indexOfFirstUser = indexOfLastUser - usersPerPage;

  // aktualizacja danych na podstawie search input
  const currentData = data
    .filter((user) =>
      ` ${user.idUser} ${user.firstname} ${user.lastname} ${user.email} ${user.role}`
        .toLowerCase()
        .includes(searchTerm.toLowerCase())
    )
    .slice(indexOfFirstUser, indexOfLastUser);

  const totalPages = Math.ceil(data.length / usersPerPage);

  const handlePageClick = (page) => {
    setCurrentPage(page);
  };

  const handleEditClick = (user) => {
    navigate(`/adminpanel/edituser/${user.idUser}`, {
      state: { userData: user },
    });
  };

  return (
    <>
      <div className="container text-center">
        <div className="py-4 ">
          <h2>Użytkownicy</h2>

          {/* pole wyszukiwania */}
          <div className="mb-3 mt-4">
            <input
              type="text"
              placeholder="Szukaj użytkownika..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="form-control"
              style={{ width: "200px" }}
            />
          </div>

          <div>
            <table className="table border shadow table-hover">
              <thead>
                <tr>
                  <th scope="col">Identyfikator użytkownika</th>
                  <th scope="col">Imię</th>
                  <th scope="col">Nazwisko</th>
                  <th scope="col">E-mail</th>
                  <th scope="col">Rola</th>
                  <th scope="col">Akcja</th>
                </tr>
              </thead>
              <tbody>
                {currentData.map((user) => (
                  <tr key={user.idUser}>
                    <td>{user.idUser}</td>
                    <td>{user.firstname}</td>
                    <td>{user.lastname}</td>
                    <td>{user.email}</td>
                    <td>{user.role}</td>
                    <td>
                      <button class="btn btn-primary mx-2">
                        <FontAwesomeIcon icon={faEnvelope} />
                      </button>
                      <button
                        className="btn btn-warning"
                        onClick={() => {
                          handleEditClick(user);
                        }}
                      >
                        <FontAwesomeIcon
                          icon={faPen}
                          style={{ color: "black" }}
                        />
                      </button>

                      <button
                        className="btn btn-danger mx-2"
                        onClick={() => onDeleteUser(user.idUser)}
                      >
                        <FontAwesomeIcon icon={faTrashAlt} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {totalPages > 1 && (
              <nav className="pagination justify-content-center">
                <ul className="pagination">
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
                      >
                        {index + 1}
                      </button>
                    </li>
                  ))}
                </ul>
              </nav>
            )}
          </div>
        </div>
      </div>
    </>
  );
};

export default UsersTable;
