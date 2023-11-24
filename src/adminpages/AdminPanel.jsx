import React, { useState, useEffect, useCallback } from "react";
import axios from "axios";
import OffersTable from "./OffersTable";
import AddOffer from "./AddOffer";
import UsersTable from "./UsersTable";
import AddUser from "./AddUser";
import { useAuth } from "../AuthContext";
import Button from "../components/Button";
import EditOffer from "./EditOffer";
const AdminPanel = () => {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [offers, setOffers] = useState([]);
  const [selectedTable, setSelectedTable] = useState(null);
  const [showEditOffer, setShowEditOffer] = useState(false);
  const [activeButton, setActiveButton] = useState(null);
  const [showUserTable, setShowUserTable] = useState(false);
  const [showAddUserForm, setShowAddUserForm] = useState(false);
  const [showOfferTable, setShowOfferTable] = useState(false);
  const [showAddOfferForm, setShowAddOfferForm] = useState(false);
  const loadOffers = useCallback(async () => {
    try {
      const result = await axios.get("http://localhost:8080/offers/get");
      setOffers(result.data);
    } catch (error) {
      console.error("Error loading offers:", error);
    }
  }, []);
  const handleAddOffer = async (newOffer) => {
    try {
      await axios.post("http://localhost:8080/offers/add", newOffer, {
        withCredentials: true,
        headers: {
          Authorization: `Bearer ${user.token}`,
        },
      });

      loadOffers();
      handleToggleTable("offers");
    } catch (error) {
      console.error("Error adding offer:", error);
    }
  };

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

  const handleAddUser = async (newUser) => {
    try {
      await axios.post("http://localhost:8080/register", newUser);
      loadUsers();
      handleToggleTable("users"); // switch to table user after added user
    } catch (error) {
      console.error("Error adding user:", error);
    }
  };
  const handleDeleteUser = async (idUser) => {
    try {
      await axios.delete(`http://localhost:8080/users/delete/${idUser}`, {
        withCredentials: true,
        headers: {
          Authorization: `Bearer ${user.token}`,
        },
      });
      setUsers((prevUsers) =>
        prevUsers.filter((user) => user.idUser !== idUser)
      );
    } catch (error) {}
  };
  useEffect(() => {
    if (selectedTable === "offers" && offers) {
      loadOffers();
    } else if (selectedTable === "users" && user) {
      loadUsers();
    }
  }, [selectedTable, user, loadOffers, loadUsers]);

  const handleAddUserFormToggle = () => {
    setShowAddUserForm(!showAddUserForm);
    setShowUserTable(false);
  };

  const handleToggleTable = (table) => {
    if (table === "users") {
      // check user table if is visible
      setSelectedTable((prev) => (prev === table ? null : table));
      setActiveButton(table);
      setShowAddUserForm(false);
      setShowUserTable((prev) => !prev); // if user table was visible then hid, if not then show
    } else if (table === "offers") {
      setSelectedTable((prev) => (prev === table ? null : table));
      setActiveButton(table);

      setShowAddOfferForm(false);
      setShowUserTable(false);
      setShowAddUserForm(false);

      setShowOfferTable((prev) => !prev);
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
              className={`dropdown-item`}
              onClick={() => {
                handleToggleTable("offers");
                setShowOfferTable(true);
                setShowAddOfferForm(false);
                setShowUserTable(false);
              }}
            >
              Pokaż usługi
            </button>
            <button
              type="button"
              className="dropdown-item"
              onClick={() => {
                setShowAddOfferForm(true);
                setShowOfferTable(false);
                setShowUserTable(false);
                setShowAddUserForm(false);
              }}
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
                setShowOfferTable(false);
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
                setShowUserTable(false);
                setShowAddUserForm(true);

                setShowOfferTable(false);
                setShowAddOfferForm(false);
              }}
            >
              Dodaj użytkownika
            </button>
          </div>
        </div>
      </div>

      {showAddOfferForm && (
        <AddOffer data={offers} onAddOffer={handleAddOffer} />
      )}
      {showAddUserForm && <AddUser onSubmit={handleAddUser} />}
      {showOfferTable && (
        <OffersTable data={offers} onDeleteOffer={handleDeleteOffer} />
      )}
      {showUserTable && (
        <UsersTable data={users} onDeleteUser={handleDeleteUser} />
      )}
    </>
  );
};

export default AdminPanel;
