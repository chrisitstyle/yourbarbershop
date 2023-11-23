import React, { useState, useEffect, useCallback } from "react";
import axios from "axios";
import OffersTable from "./OffersTable";
import AddOffer from "./AddOffer";
import UsersTable from "./UsersTable";
import AddUser from "./AddUser";
import { useAuth } from "../AuthContext";
import Button from "../components/Button";

const AdminPanel = () => {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [offers, setOffers] = useState([]);
  const [selectedTable, setSelectedTable] = useState(null);
  const [showAddOfferForm, setShowAddOfferForm] = useState(false);
  const [showAddUserForm, setShowAddUserForm] = useState(false);
  const [activeButton, setActiveButton] = useState(null);
  const [showUserTable, setShowUserTable] = useState(false);

  const loadOffers = useCallback(async () => {
    try {
      const result = await axios.get("http://localhost:8080/offers/get");
      setOffers(result.data);
    } catch (error) {
      console.error("Error loading offers:", error);
    }
  }, []);

  const loadUsers = useCallback(async () => {
    try {
      const result = await axios.get("http://localhost:8080/users/get", {
        withCredentials: true,
        headers: {
          Authorization: `Bearer ${user.token}`,
        },
      });
      setUsers(result.data);
    } catch (error) {
      console.error("Error loading users: ", error);
    }
  }, [user]);

  useEffect(() => {
    if (selectedTable === "offers") {
      loadOffers();
    } else if (selectedTable === "users" && user) {
      loadUsers();
    }
  }, [selectedTable, user, loadOffers, loadUsers]);

  const handleDeleteOffer = async (idOffer) => {
    try {
      await axios.delete(`http://localhost:8080/offers/delete/${idOffer}`, {
        withCredentials: true,
        headers: {
          Authorization: `Bearer ${user.token}`,
        },
      });
      setOffers((prevOffers) =>
        prevOffers.filter((offer) => offer.idOffer !== idOffer)
      );
    } catch (error) {
      console.error("Error deleting offer:", error);
    }
  };

  const handleAddOffer = async (newOffer) => {
    try {
      await axios.post("http://localhost:8080/offers/add", newOffer, {
        withCredentials: true,
        headers: {
          Authorization: `Bearer ${user.token}`,
        },
      });

      loadOffers();
    } catch (error) {
      console.error("Error adding offer:", error);
    }
  };

  const handleAddUserFormToggle = () => {
    setShowAddUserForm(!showAddUserForm);
    setShowUserTable(false);
  };

  const handleAddUser = async (newUser) => {
    try {
      await axios.post("http://localhost:8080/register", newUser);
      loadUsers();
      handleToggleTable("users"); // Usuń tę linię, aby nie przełączać automatycznie na tabelę użytkowników po dodaniu użytkownika
    } catch (error) {
      console.error("Error adding user:", error);
    }
  };

  const handleToggleTable = (table) => {
    if (table === "users") {
      // Jeżeli tabela ma być użytkowników, to sprawdź czy już nie jest widoczna
      setSelectedTable((prev) => (prev === table ? null : table));
      setActiveButton(table);
      setShowAddUserForm(false);
      setShowUserTable((prev) => !prev); // Jeżeli tabela użytkowników była widoczna, to ukryj ją, a jeżeli była ukryta, to pokaż
    } else {
      // Dla innych tabel przełączaj normalnie
      setSelectedTable((prev) => (prev === table ? null : table));
      setActiveButton(table);
      setShowAddUserForm(false);
      setShowUserTable(false);
    }
  };

  return (
    <>
      <div className="btn-group" role="group">
        <div className="dropdown">
          <button
            className="btn btn-light dropdown-toggle"
            type="button"
            id="servicesDropdown"
            data-bs-toggle="dropdown"
            aria-haspopup="true"
            aria-expanded="false"
          >
            Usługi
          </button>
          <div className="dropdown-menu" aria-labelledby="servicesDropdown">
            <button
              type="button"
              className={`dropdown-item ${
                activeButton === "offers" ? "active" : ""
              }`}
              onClick={() => handleToggleTable("offers")}
            >
              Pokaż usługi
            </button>
            <button
              type="button"
              className="dropdown-item"
              onClick={() => setShowAddOfferForm(!showAddOfferForm)}
            >
              Dodaj usługę
            </button>
          </div>
        </div>

        <div className="dropdown">
          {/* <button
            className={`btn btn-light dropdown-toggle`}
            type="button"
            id="servicesDropdown"
            data-bs-toggle="dropdown"
            aria-haspopup="true"
            aria-expanded="false"
          >
            Użytkownicy
          </button> */}
          {<Button />}
          <div className="dropdown-menu" aria-labelledby="usersDropdown">
            <button
              type="button"
              className={`dropdown-item`}
              onClick={() => {
                handleToggleTable("users");
                setShowAddOfferForm(false);
                setShowUserTable(true);
              }}
            >
              Pokaż użytkowników
            </button>
            <button
              type="button"
              className="dropdown-item"
              onClick={() => {
                setShowAddUserForm(true);
                setShowUserTable(false);
              }}
            >
              Dodaj użytkownika
            </button>
          </div>
        </div>
      </div>

      {showAddOfferForm && selectedTable === "offers" && (
        <AddOffer data={offers} onAddOffer={handleAddOffer} />
      )}
      {showAddUserForm && <AddUser onSubmit={handleAddUser} />}
      {selectedTable === "offers" && (
        <OffersTable data={offers} onDeleteOffer={handleDeleteOffer} />
      )}
      {showUserTable && <UsersTable data={users} />}
    </>
  );
};

export default AdminPanel;
