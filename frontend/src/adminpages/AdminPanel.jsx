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
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faScissors,
  faUser,
  faCalendarCheck,
  faImages,
} from "@fortawesome/free-solid-svg-icons";

const AdminPanel = () => {
  const { user } = useAuth();
  const [selectedTable, setSelectedTable] = useState(null);

  // table/form visibility states
  const [showUserTable, setShowUserTable] = useState(false);
  const [showAddUserForm, setShowAddUserForm] = useState(false);
  const [showOfferTable, setShowOfferTable] = useState(false);
  const [showOrderTable, setShowOrderTable] = useState(false);
  const [showAddOfferForm, setShowAddOfferForm] = useState(false);
  const [showGuestOrderTable, setShowGuestOrderTable] = useState(false);
  const [showGallerySettings, setShowGallerySettings] = useState(false);

  // alert (feedback) state
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
      setAddOfferSuccessfulMsg("Pomyślnie dodano nową usługę.");
    } catch (error) {
      console.error("Error adding offer:", error);
      setAddOfferErrorMsg("Wystąpił błąd podczas dodawania usługi.");
    }
  };

  const handleDeleteOffer = async (idOffer) => {
    try {
      await offerService.deleteOffer(idOffer, user.token);
    } catch (error) {
      console.error("Error deleting offer:", error);
      setDeleteOfferErrorMsg("Nie udało się usunąć usługi.");
    }
  };

  const handleAddUser = async (newUser) => {
    try {
      await userService.addUser(newUser);
      setAddUserSuccessfulMsg("Pomyślnie dodano nowego użytkownika.");
      handleToggleTable("users");
    } catch (error) {
      setAddUserErrorMsg("Wystąpił błąd podczas dodawania użytkownika.");
      console.error("Error adding user:", error);
    }
  };

  const handleDeleteUser = async (idUser) => {
    try {
      await userService.deleteUser(idUser, user.token);
    } catch (error) {
      setDeleteUserErrorMsg("Wystąpił błąd podczas usuwania użytkownika.");
      console.error("Error deleting user:", error);
    }
  };

  const handleDeleteOrder = async (idOrder) => {
    try {
      await orderService.deleteOrder(idOrder, user.token);
    } catch (error) {
      setDeleteOrderErrorMsg("Wystąpił błąd podczas usuwania wizyty.");
      console.error("Error deleting order:", error);
    }
  };

  const handleDeleteGuestOrder = async (idGuestOrder) => {
    try {
      await guestOrderService.deleteGuestOrder(idGuestOrder, user.token);
    } catch (error) {
      setDeleteGuestOrderErrorMsg(
        "Wystąpił błąd podczas usuwania wizyty gościa."
      );
      console.error("Error deleting order:", error);
    }
  };

  // handle switching between modules/views
  const handleToggleTable = (table) => {
    setSelectedTable((prev) => (prev === table ? null : table));
    setShowAddUserForm(false);
    setShowAddOfferForm(false);
    setShowUserTable(false);
    setShowOfferTable(false);
    setShowOrderTable(false);
    setShowGuestOrderTable(false);
    setShowGallerySettings(false);

    switch (table) {
      case "users":
        setShowUserTable(true);
        break;
      case "offers":
        setShowOfferTable(true);
        break;
      case "orders":
        setShowOrderTable(true);
        break;
      case "guestorders":
        setShowGuestOrderTable(true);
        break;
      case "gallerysettings":
        setShowGallerySettings(true);
        break;
      default:
        break;
    }
  };

  return (
    <div className="admin-panel-wrapper py-4 px-2">
      <div className="text-center mb-4">
        <h2 className="fw-bold">Panel administratora</h2>
        <p className="text-muted mb-0">
          Zarządzaj głównymi funkcjami: użytkownikami, usługami, wizytami oraz
          galerią.
        </p>
      </div>
      <div className="d-flex flex-wrap justify-content-center gap-3 mb-4">
        <div className="dropdown">
          <AdminMenuButton
            title={
              <span>
                <FontAwesomeIcon icon={faScissors} className="me-2" />
                Usługi
              </span>
            }
          />
          <div className="dropdown-menu">
            <button
              type="button"
              className="dropdown-item"
              onClick={() => {
                handleToggleTable("offers");
              }}
            >
              Pokaż usługi
            </button>
            <button
              type="button"
              className="dropdown-item"
              onClick={() => {
                setShowAddOfferForm(true);
                setShowUserTable(false);
                setShowOfferTable(false);
                setShowOrderTable(false);
                setShowAddUserForm(false);
                setShowGuestOrderTable(false);
                setShowGallerySettings(false);
              }}
            >
              Dodaj usługę
            </button>
          </div>
        </div>
        <div className="dropdown">
          <AdminMenuButton
            title={
              <span>
                <FontAwesomeIcon icon={faUser} className="me-2" />
                Użytkownicy
              </span>
            }
          />
          <div className="dropdown-menu">
            <button
              type="button"
              className="dropdown-item"
              onClick={() => {
                handleToggleTable("users");
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
                setShowOfferTable(false);
                setShowOrderTable(false);
                setShowAddOfferForm(false);
                setShowGuestOrderTable(false);
                setShowGallerySettings(false);
              }}
            >
              Dodaj użytkownika
            </button>
          </div>
        </div>
        <div className="dropdown">
          <AdminMenuButton
            title={
              <span>
                <FontAwesomeIcon icon={faCalendarCheck} className="me-2" />
                Wizyty
              </span>
            }
          />
          <div className="dropdown-menu">
            <button
              type="button"
              className="dropdown-item"
              onClick={() => {
                handleToggleTable("orders");
              }}
            >
              Pokaż wizyty użytkowników
            </button>
            <button
              type="button"
              className="dropdown-item"
              onClick={() => {
                handleToggleTable("guestorders");
              }}
            >
              Pokaż wizyty gości
            </button>
          </div>
        </div>
        <div className="dropdown">
          <AdminMenuButton
            title={
              <span>
                <FontAwesomeIcon icon={faImages} className="me-2" />
                Galeria
              </span>
            }
          />
          <div className="dropdown-menu">
            <button
              type="button"
              className="dropdown-item"
              onClick={() => {
                handleToggleTable("gallerysettings");
              }}
            >
              Ustawienia galerii
            </button>
          </div>
        </div>
      </div>
      {/* alert messages for feedback */}
      <div className="mb-2">
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
      </div>
      <div
        className="admin-panel-content mx-auto"
        style={{ maxWidth: "1100px" }}
      >
        {showAddOfferForm && (
          <div className="fade-in card shadow p-4 mb-5">
            <AddOffer onAddOffer={handleAddOffer} />
          </div>
        )}
        {showAddUserForm && (
          <div className="fade-in card shadow p-4 mb-5">
            <AddUser onSubmit={handleAddUser} />
          </div>
        )}
        {showOfferTable && (
          <div className="fade-in card shadow p-4 mb-5">
            <OffersTable onDeleteOffer={handleDeleteOffer} />
          </div>
        )}
        {showUserTable && (
          <div className="fade-in card shadow p-4 mb-5">
            <UsersTable onDeleteUser={handleDeleteUser} />
          </div>
        )}
        {showOrderTable && (
          <div className="fade-in card shadow p-4 mb-5">
            <OrdersTable onDeleteOrder={handleDeleteOrder} />
          </div>
        )}
        {showGuestOrderTable && (
          <div className="fade-in card shadow p-4 mb-5">
            <GuestOrdersTable onDeleteGuestOrder={handleDeleteGuestOrder} />
          </div>
        )}
        {showGallerySettings && (
          <div className="fade-in card shadow p-4 mb-5">
            <GallerySettings />
          </div>
        )}
      </div>
      <style>
        {`
          .fade-in {
            animation: fadein 0.4s;
          }
          @keyframes fadein {
            from { opacity: 0; transform: translateY(12px);}
            to { opacity: 1; transform: translateY(0);}
          }
        `}
      </style>
    </div>
  );
};

export default AdminPanel;
