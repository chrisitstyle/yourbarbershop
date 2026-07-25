import { useState } from "react";
import userService from "../api/userService.js";
import offerService from "../api/offerService.js";
import orderService from "../api/orderService.js";
import guestOrderService from "../api/guestOrderService.js";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Alert } from "react-bootstrap";
import OffersTable from "./OffersTable";
import AddOffer from "./AddOffer";
import UsersTable from "./UsersTable";
import AddUser from "./AddUser";
import OrdersTable from "./OrdersTable";
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
import { useTranslation } from "react-i18next";

const AdminPanel = () => {
  const { t } = useTranslation();
  const queryClient = useQueryClient();

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

  // mutations for handling api operations with query invalidation
  const addOfferMutation = useMutation({
    mutationFn: (newOffer) => offerService.addOffer(newOffer),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["offers"] });
      handleToggleTable("offers");
      setAddOfferSuccessfulMsg(t("admin.messages.addOfferSuccess"));
    },
    onError: (error) => {
      console.error("error adding offer:", error);
      setAddOfferErrorMsg(t("admin.messages.addOfferError"));
    },
  });

  const deleteOfferMutation = useMutation({
    mutationFn: (idOffer) => offerService.deleteOffer(idOffer),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["offers"] });
    },
    onError: (error) => {
      console.error("error deleting offer:", error);
      setDeleteOfferErrorMsg(t("admin.messages.deleteOfferError"));
    },
  });

  const addUserMutation = useMutation({
    mutationFn: (newUser) => userService.addUser(newUser),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
      setAddUserSuccessfulMsg(t("admin.messages.addUserSuccess"));
      handleToggleTable("users");
    },
    onError: (error) => {
      console.error("error adding user:", error);
      setAddUserErrorMsg(t("admin.messages.addUserError"));
    },
  });

  const deleteUserMutation = useMutation({
    mutationFn: (idUser) => userService.deleteUser(idUser),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
    },
    onError: (error) => {
      console.error("error deleting user:", error);
      setDeleteUserErrorMsg(t("admin.messages.deleteUserError"));
    },
  });

  const deleteOrderMutation = useMutation({
    mutationFn: (idOrder) => orderService.deleteOrder(idOrder),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["orders"] });
    },
    onError: (error) => {
      console.error("error deleting order:", error);
      setDeleteOrderErrorMsg(t("admin.messages.deleteOrderError"));
    },
  });

  const deleteGuestOrderMutation = useMutation({
    mutationFn: (idGuestOrder) =>
      guestOrderService.deleteGuestOrder(idGuestOrder),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["guestOrders"] });
    },
    onError: (error) => {
      console.error("error deleting order:", error);
      setDeleteGuestOrderErrorMsg(t("admin.messages.deleteGuestOrderError"));
    },
  });

  const handleAddOffer = (newOffer) => addOfferMutation.mutateAsync(newOffer);
  const handleDeleteOffer = (idOffer) =>
    deleteOfferMutation.mutateAsync(idOffer);
  const handleAddUser = (newUser) => addUserMutation.mutateAsync(newUser);
  const handleDeleteUser = (idUser) => deleteUserMutation.mutateAsync(idUser);
  const handleDeleteOrder = (idOrder) =>
    deleteOrderMutation.mutateAsync(idOrder);
  const handleDeleteGuestOrder = (idGuestOrder) =>
    deleteGuestOrderMutation.mutateAsync(idGuestOrder);

  // handle switching between modules/views
  const handleToggleTable = (table) => {
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
        <h2 className="fw-bold">{t("admin.panelTitle")}</h2>
        <p className="text-muted mb-0">{t("admin.panelDesc")}</p>
      </div>

      <div className="d-flex flex-wrap justify-content-center gap-3 mb-4">
        <div className="dropdown">
          <AdminMenuButton
            className="admin-menu-button"
            title={
              <span>
                <FontAwesomeIcon icon={faScissors} className="me-2" />
                {t("admin.menu.offers")}
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
              {t("admin.actions.showOffers")}
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
              {t("admin.actions.addOffer")}
            </button>
          </div>
        </div>

        <div className="dropdown">
          <AdminMenuButton
            className="admin-menu-button"
            title={
              <span>
                <FontAwesomeIcon icon={faUser} className="me-2" />
                {t("admin.menu.users")}
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
              {t("admin.actions.showUsers")}
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
              {t("admin.actions.addUser")}
            </button>
          </div>
        </div>

        <div className="dropdown">
          <AdminMenuButton
            className="admin-menu-button"
            title={
              <span>
                <FontAwesomeIcon icon={faCalendarCheck} className="me-2" />
                {t("admin.menu.orders")}
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
              {t("admin.actions.showUserOrders")}
            </button>

            <button
              type="button"
              className="dropdown-item"
              onClick={() => {
                handleToggleTable("guestorders");
              }}
            >
              {t("admin.actions.showGuestOrders")}
            </button>
          </div>
        </div>

        <div className="dropdown">
          <AdminMenuButton
            className="admin-menu-button"
            title={
              <span>
                <FontAwesomeIcon icon={faImages} className="me-2" />
                {t("admin.menu.gallery")}
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
              {t("admin.actions.gallerySettings")}
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
        style={{ maxWidth: "1400px" }}
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
            from {
              opacity: 0;
              transform: translateY(12px);
            }

            to {
              opacity: 1;
              transform: translateY(0);
            }
          }
        `}
      </style>
    </div>
  );
};

export default AdminPanel;
