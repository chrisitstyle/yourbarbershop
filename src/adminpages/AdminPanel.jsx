// AdminPanel.jsx
import React, { useState, useEffect, useCallback } from "react";
import * as api from "../api/api.js";
import OffersTable from "./OffersTable";
import AddOffer from "./AddOffer";
import UsersTable from "./UsersTable";
import AddUser from "./AddUser";
import OrdersTable from "./OrdersTable";
import { useAuth } from "../AuthContext";
import AdminMenuButton from "../components/AdminMenuButton.jsx";
import GuestOrdersTable from "./GuestOrdersTable.jsx";
import GallerySettings from "./GallerySettings.jsx";

const AdminPanel = () => {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [offers, setOffers] = useState([]);
  const [orders, setOrders] = useState([]);
  const [guestOrders, setGuestOrders] = useState([]);
  const [selectedTable, setSelectedTable] = useState(null);

  const [showUserTable, setShowUserTable] = useState(false);
  const [showAddUserForm, setShowAddUserForm] = useState(false);
  const [showOfferTable, setShowOfferTable] = useState(false);
  const [showOrderTable, setShowOrderTable] = useState(false);
  const [showAddOfferForm, setShowAddOfferForm] = useState(false);
  const [showGuestOrderTable, setShowGuestOrderTable] = useState(false);
  const [showGallerySettings, setShowGallerySettings] = useState(false);
  const loadOffers = useCallback(async () => {
    try {
      const offersData = await api.getOffers();
      setOffers(offersData);
    } catch (error) {
      console.error("Error loading offers:", error);
    }
  }, []);

  const handleAddOffer = async (newOffer) => {
    try {
      await api.addOffer(newOffer, user.token);
      loadOffers();
      handleToggleTable("offers");
    } catch (error) {
      console.error("Error adding offer:", error);
    }
  };

  const handleDeleteOffer = async (idOffer) => {
    try {
      await api.deleteOffer(idOffer, user.token);
      setOffers((prevOffers) =>
        prevOffers.filter((offer) => offer.idOffer !== idOffer)
      );
    } catch (error) {
      console.error("Error deleting offer:", error);
    }
  };

  const loadUsers = useCallback(async () => {
    try {
      const usersData = await api.getUsers(user.token);
      setUsers(usersData);
    } catch (error) {
      console.error("Error loading users: ", error);
    }
  }, [user]);

  const handleAddUser = async (newUser) => {
    try {
      await api.addUser(newUser);
      loadUsers();
      handleToggleTable("users");
    } catch (error) {
      console.error("Error adding user:", error);
    }
  };

  const handleDeleteUser = async (idUser) => {
    try {
      await api.deleteUser(idUser, user.token);
      setUsers((prevUsers) =>
        prevUsers.filter((user) => user.idUser !== idUser)
      );
    } catch (error) {
      console.error("Error deleting user:", error);
    }
  };

  const loadOrders = useCallback(async () => {
    try {
      const ordersData = await api.getOrders(user.token);
      setOrders(ordersData);
    } catch (error) {
      console.error("Error loading orders:", error);
    }
  }, [user]);

  const handleDeleteOrder = async (idOrder) => {
    try {
      await api.deleteOrder(idOrder, user.token);
      setOrders((prevOrders) =>
        prevOrders.filter((order) => order.idOrder !== idOrder)
      );
    } catch (error) {
      console.error("Error deleting order:", error);
    }
  };

  const loadGuestOrders = useCallback(async () => {
    try {
      const guestOrdersData = await api.getGuestOrders(user.token);
      setGuestOrders(guestOrdersData);
    } catch (error) {
      console.error("Error loading  guest orders:", error);
    }
  }, [user]);
  useEffect(() => {
    selectedTable === "offers" && offers
      ? loadOffers()
      : selectedTable === "users" && user
      ? loadUsers()
      : selectedTable === "orders" && orders
      ? loadOrders()
      : selectedTable === "guestorders" && guestOrders && loadGuestOrders();
  }, [selectedTable, user, loadOffers, loadUsers, loadOrders, loadGuestOrders]);

  const handleToggleTable = (table) => {
    if (table === "users") {
      setSelectedTable((prev) => (prev === table ? null : table));

      setShowAddUserForm(false);
      setShowGallerySettings(false);
      setShowUserTable((prev) => !prev);
    } else if (table === "offers") {
      setSelectedTable((prev) => (prev === table ? null : table));

      setShowAddOfferForm(false);
      setShowUserTable(false);
      setShowAddUserForm(false);
      setShowGallerySettings(false);
      setShowOfferTable((prev) => !prev);
    } else if (table === "orders") {
      setSelectedTable((prev) => (prev === table ? null : table));

      setShowAddOfferForm(false);
      setShowUserTable(false);
      setShowAddUserForm(false);
      setShowGallerySettings(false);
      setShowOfferTable((prev) => !prev);
    } else if (table === "guestorders") {
      setSelectedTable((prev) => (prev === table ? null : table));

      setShowAddOfferForm(false);
      setShowUserTable(false);
      setShowAddUserForm(false);
      setShowGallerySettings(false);
      setShowGuestOrderTable((prev) => !prev);
    } else if (table === "gallerysettings") {
      setShowAddOfferForm(false);
      setShowUserTable(false);
      setShowAddUserForm(false);
      setShowGallerySettings(false);
      setShowAddUserForm(false);
      setSelectedTable((prev) => (prev === table ? null : table));
    }
  };

  return (
    <>
      <div className="btn-group" role="group">
        <div className="dropdown">
          <AdminMenuButton title="Usługi" />

          <div className="dropdown-menu" aria-labelledby="servicesDropdown">
            <button
              type="button"
              className={`dropdown-item`}
              onClick={() => {
                handleToggleTable("offers");
                setShowOfferTable(true);
                setShowAddOfferForm(false);
                setShowUserTable(false);
                setShowOrderTable(false);
                setShowGuestOrderTable(false);
                setShowGallerySettings(false);
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
                setShowOrderTable(false);
                setShowGuestOrderTable(false);
                setShowGallerySettings(false);
              }}
            >
              Dodaj usługę
            </button>
          </div>
        </div>

        <div className="dropdown">
          <AdminMenuButton title="Użytkownicy" />
          <div className="dropdown-menu" aria-labelledby="usersDropdown">
            <button
              type="button"
              className={`dropdown-item`}
              onClick={() => {
                handleToggleTable("users");
                setShowOfferTable(false);
                setShowAddOfferForm(false);
                setShowUserTable(true);
                setShowOrderTable(false);
                setShowGuestOrderTable(false);
                setShowGallerySettings(false);
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
                setShowOrderTable(false);
                setShowGuestOrderTable(false);
                setShowGallerySettings(false);
              }}
            >
              Dodaj użytkownika
            </button>
          </div>
        </div>
        <div className="dropdown">
          <AdminMenuButton title="Wizyty" />
          <div className="dropdown-menu" aria-labelledby="servicesDropdown">
            <button
              type="button"
              className={`dropdown-item`}
              onClick={() => {
                handleToggleTable("orders");
                setShowOfferTable(false);
                setShowAddOfferForm(false);
                setShowUserTable(false);
                setShowOrderTable(true);
                setShowAddUserForm(false);
                setShowGuestOrderTable(false);
                setShowGallerySettings(false);
              }}
            >
              Pokaż wizyty użytkowników
            </button>
            <button
              type="button"
              className={`dropdown-item`}
              onClick={() => {
                handleToggleTable("guestorders");
                setShowOfferTable(false);
                setShowAddOfferForm(false);
                setShowUserTable(false);
                setShowAddUserForm(false);
                setShowOrderTable(false);
                setShowGallerySettings(false);
                setShowGuestOrderTable(true);
              }}
            >
              Pokaż wizyty gości
            </button>
          </div>
        </div>
        <div className="dropdown">
          <AdminMenuButton title="Galeria" />
          <div className="dropdown-menu" aria-labelledby="servicesDropdown">
            <button
              type="button"
              className={`dropdown-item`}
              onClick={() => {
                handleToggleTable("gallerysettings");
                setShowOfferTable(false);
                setShowAddOfferForm(false);
                setShowAddUserForm(false);
                setShowUserTable(false);
                setShowOrderTable(false);
                setShowGuestOrderTable(false);
                setShowGallerySettings(true);
              }}
            >
              Ustawienia
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
      {showOrderTable && (
        <OrdersTable data={orders} onDeleteOrder={handleDeleteOrder} />
      )}
      {showGuestOrderTable && <GuestOrdersTable data={guestOrders} />}
      {showGallerySettings && <GallerySettings />}
    </>
  );
};

export default AdminPanel;
