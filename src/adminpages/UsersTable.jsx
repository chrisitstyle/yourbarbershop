import React from "react";

const UsersTable = ({ data, onDeleteUser }) => {
  return (
    <>
      <div className="container">
        <div className="py-4 ">
          <div>
            <table className="table border shadow text-center">
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
                {data.map((user) => (
                  <tr key={user.idUser}>
                    <td>{user.idUser}</td>
                    <td>{user.firstname}</td>
                    <td>{user.lastname}</td>
                    <td>{user.email}</td>
                    <td>{user.role}</td>
                    <td>
                      <button type="button" className="btn btn-warning">
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
          </div>
        </div>
      </div>
    </>
  );
};

export default UsersTable;
