import { useState } from "react";

import userService from "../api/userService.js";
import offerService from "../api/offerService.js";
import orderService from "../api/orderService.js";
import guestOrderService from "../api/guestOrderService.js";
import { Alert } from "react-bootstrap";
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
  const [selectedTable, setSelectedTable] = useState(null);

  const [showUserTable, setShowUserTable] = useState(false);
  const [showAddUserForm, setShowAddUserForm] = useState(false);
  const [showOfferTable, setShowOfferTable] = useState(false);
  const [showOrderTable, setShowOrderTable] = useState(false);
  const [showAddOfferForm, setShowAddOfferForm] = useState(false);
  const [showGuestOrderTable, setShowGuestOrderTable] = useState(false);
  const [showGallerySettings, setShowGallerySettings] = useState(false);

  const [addOfferErrorMsg, setAddOfferErrorMsg] = useState(null);
  const [addOfferSuccessfulMsg, setAddOfferSuccessfulMsg] = useState(null);
  const [deleteOfferErrorMsg, setDeleteOfferErrorMsg] = useState(null);

  const [addUserErrorMsg, setAddUserErrorMsg] = useState(null);
  const [addUserSuccessfulMsg, setAddUserSuccessfulMsg] = useState(null);
  const [deleteUserErrorMsg, setDeleteUserErrorMsg] = useState(null);

  const [deleteOrderErrorMsg, setDeleteOrderErrorMsg] = useState(null);

  const [deleteGuestOrderErrorMsg, setDeleteGuestOrderErrorMsg] =
    useState(null);

  const handleAddOffer = async (newOffer) => {
    try {
      await offerService.addOffer(newOffer, user.token);
      handleToggleTable("offers");
      setAddOfferSuccessfulMsg("Pomyślnie dodano nową usługę");
    } catch (error) {
      console.error("Error adding offer:", error);
      setAddOfferErrorMsg("Wystąpił błąd dodawania usługi");
    }
  };

  const handleDeleteOffer = async (idOffer) => {
    try {
      await offerService.deleteOffer(idOffer, user.token);
    } catch (error) {
      console.error("Error deleting offer:", error);
      setDeleteOfferErrorMsg("Nie udało się usunąć usługi");
    }
  };

  const handleAddUser = async (newUser) => {
    try {
      await userService.addUser(newUser);
      setAddUserSuccessfulMsg("Pomyślnie dodano nowego użytkownika");
      handleToggleTable("users");
    } catch (error) {
      setAddUserErrorMsg("Wystąpił błąd z dodawaniem użytkownika");
      console.error("Error adding user:", error);
    }
  };

  const handleDeleteUser = async (idUser) => {
    try {
      await userService.deleteUser(idUser, user.token);
    } catch (error) {
      setDeleteUserErrorMsg("Wystąpił błąd podczas usuwania użytkownika");
      console.error("Error deleting user:", error);
    }
  };

  const handleDeleteOrder = async (idOrder) => {
    try {
      await orderService.deleteOrder(idOrder, user.token);
    } catch (error) {
      setDeleteOrderErrorMsg("Wystąpił błąd podczas usuwania wizyty");
      console.error("Error deleting order:", error);
    }
  };

  const handleDeleteGuestOrder = async (idGuestOrder) => {
    try {
      await guestOrderService.deleteGuestOrder(idGuestOrder, user.token);
    } catch (error) {
      setDeleteGuestOrderErrorMsg(
        "Wystąpił błąd podczas usuwania wizyty gościa"
      );
      console.error("Error deleting order:", error);
    }
  };

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
      {/* navbar menu admin*/}
      <div className="btn-group flex-column flex-sm-row" role="group">
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
      {/* alerty */}

      {addOfferErrorMsg && (
        <Alert
          variant="danger"
          onClose={() => setAddOfferErrorMsg(null)}
          dismissible
          className="text-center"
        >
          {addOfferErrorMsg}
        </Alert>
      )}
      {addOfferSuccessfulMsg && (
        <Alert
          variant="success"
          onClose={() => setAddOfferSuccessfulMsg(null)}
          dismissible
          className="text-center"
        >
          {addOfferSuccessfulMsg}
        </Alert>
      )}

      {deleteOfferErrorMsg && (
        <Alert
          variant="danger"
          onClose={() => setDeleteOfferErrorMsg(null)}
          dismissible
          className="text-center"
        >
          {deleteOfferErrorMsg}
        </Alert>
      )}
      {addUserSuccessfulMsg && (
        <Alert
          variant="success"
          onClose={() => setAddUserSuccessfulMsg(null)}
          dismissible
          className="text-center"
        >
          {addUserSuccessfulMsg}
        </Alert>
      )}
      {addUserErrorMsg && (
        <Alert
          variant="danger"
          onClose={() => setAddUserErrorMsg(null)}
          dismissible
          className="text-center"
        >
          {addUserErrorMsg}
        </Alert>
      )}
      {deleteUserErrorMsg && (
        <Alert
          variant="danger"
          onClose={() => setDeleteUserErrorMsg(null)}
          dismissible
          className="text-center"
        >
          {deleteUserErrorMsg}
        </Alert>
      )}
      {deleteOrderErrorMsg && (
        <Alert
          variant="danger"
          onClose={() => setDeleteOrderErrorMsg(null)}
          dismissible
          className="text-center"
        >
          {deleteOrderErrorMsg}
        </Alert>
      )}
      {deleteGuestOrderErrorMsg && (
        <Alert
          variant="danger"
          onClose={() => setDeleteGuestOrderErrorMsg(null)}
          dismissible
          className="text-center"
        >
          {deleteGuestOrderErrorMsg}
        </Alert>
      )}

      {/* conditional rendering of menu components */}
      {showAddOfferForm && <AddOffer onAddOffer={handleAddOffer} />}
      {showAddUserForm && <AddUser onSubmit={handleAddUser} />}
      {showOfferTable && <OffersTable onDeleteOffer={handleDeleteOffer} />}
      {showUserTable && <UsersTable onDeleteUser={handleDeleteUser} />}
      {showOrderTable && <OrdersTable onDeleteOrder={handleDeleteOrder} />}
      {showGuestOrderTable && (
        <GuestOrdersTable onDeleteGuestOrder={handleDeleteGuestOrder} />
      )}
      {showGallerySettings && <GallerySettings />}
    </>
  );
};

export default AdminPanel;
