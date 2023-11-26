import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
const UsersTable = ({ data, onDeleteUser }) => {
  const navigate = useNavigate();
  const usersPerPage = 10;
  const [currentPage, setCurrentPage] = useState(1);
  const indexOfLastUser = currentPage * usersPerPage;
  const indexOfFirstUser = indexOfLastUser - usersPerPage;
  const currentData = data.slice(indexOfFirstUser, indexOfLastUser);

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
          <h2>Lista użytkowników</h2>
          <div>
            <table className="table border shadow">
              <thead>
                <tr>
                  <th scope="col">Identyfikator użytkownika</th>
                  <th scope="col">Imie</th>
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
                      <button
                        className="btn btn-warning"
                        onClick={() => {
                          handleEditClick(user);
                        }}
                      >
                        Edytuj
                      </button>
                      <button
                        className="btn btn-danger mx-2"
                        onClick={() => onDeleteUser(user.idUser)}
                      >
                        Usuń
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
                      <a
                        className="page-link"
                        onClick={() => handlePageClick(index + 1)}
                      >
                        {index + 1}
                      </a>
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
