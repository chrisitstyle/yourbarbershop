import { useState, useEffect, useRef, useCallback } from "react";
import userService from "../api/userService.js";
import offerService from "../api/offerService.js";
import orderService from "../api/orderService.js";
import guestOrderService from "../api/guestOrderService.js";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import OffersTable from "./OffersTable";
import AddOffer from "./AddOffer";
import UsersTable from "./UsersTable";
import AddUser from "./AddUser";
import OrdersTable from "./OrdersTable";
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

  // single source of truth for which module/view is visible (null = nothing selected)
  const [activeView, setActiveView] = useState(null);

  // index of the currently open dropdown menu (null = all closed)
  const [openMenu, setOpenMenu] = useState(null);
  const menuBarRef = useRef(null);

  const closeMenu = useCallback(() => setOpenMenu(null), []);

  // selecting a view switches the panel and always hides the dropdown
  const selectView = useCallback((view) => {
    setActiveView(view);
    setOpenMenu(null);
  }, []);

  // close the open menu on outside click and on escape
  useEffect(() => {
    if (openMenu === null) return undefined;

    const handlePointer = (e) => {
      if (menuBarRef.current && !menuBarRef.current.contains(e.target)) {
        closeMenu();
      }
    };
    const handleKey = (e) => {
      if (e.key === "Escape") closeMenu();
    };

    document.addEventListener("mousedown", handlePointer);
    document.addEventListener("keydown", handleKey);
    return () => {
      document.removeEventListener("mousedown", handlePointer);
      document.removeEventListener("keydown", handleKey);
    };
  }, [openMenu, closeMenu]);

  // mutations for handling api operations with query invalidation
  const addOfferMutation = useMutation({
    mutationFn: (newOffer) => offerService.addOffer(newOffer),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["offers"] });
      setActiveView("offers");
    },
    onError: (error) => {
      console.error("error adding offer:", error);
    },
  });

  const deleteOfferMutation = useMutation({
    mutationFn: (idOffer) => offerService.deleteOffer(idOffer),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["offers"] });
    },
    onError: (error) => {
      console.error("error deleting offer:", error);
    },
  });

  const addUserMutation = useMutation({
    mutationFn: (newUser) => userService.addUser(newUser),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
      setActiveView("users");
    },
    onError: (error) => {
      console.error("error adding user:", error);
    },
  });

  const deleteUserMutation = useMutation({
    mutationFn: (idUser) => userService.deleteUser(idUser),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
    },
    onError: (error) => {
      console.error("error deleting user:", error);
    },
  });

  const deleteOrderMutation = useMutation({
    mutationFn: (idOrder) => orderService.deleteOrder(idOrder),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["orders"] });
    },
    onError: (error) => {
      console.error("error deleting order:", error);
    },
  });

  const deleteGuestOrderMutation = useMutation({
    mutationFn: (idGuestOrder) =>
      guestOrderService.deleteGuestOrder(idGuestOrder),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["guestOrders"] });
    },
    onError: (error) => {
      console.error("error deleting guest order:", error);
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

  // data-driven menu -> each dropdown + its actions described declaratively, no duplicated jsx
  const menus = [
    {
      icon: faScissors,
      label: t("admin.menu.offers"),
      actions: [
        { view: "offers", label: t("admin.actions.showOffers") },
        { view: "addOffer", label: t("admin.actions.addOffer") },
      ],
    },
    {
      icon: faUser,
      label: t("admin.menu.users"),
      actions: [
        { view: "users", label: t("admin.actions.showUsers") },
        { view: "addUser", label: t("admin.actions.addUser") },
      ],
    },
    {
      icon: faCalendarCheck,
      label: t("admin.menu.orders"),
      actions: [
        { view: "orders", label: t("admin.actions.showUserOrders") },
        { view: "guestorders", label: t("admin.actions.showGuestOrders") },
      ],
    },
    {
      icon: faImages,
      label: t("admin.menu.gallery"),
      actions: [
        { view: "gallerysettings", label: t("admin.actions.gallerySettings") },
      ],
    },
  ];

  // maps each view key to the content rendered in the panel body
  const views = {
    addOffer: <AddOffer onAddOffer={handleAddOffer} />,
    addUser: <AddUser onSubmit={handleAddUser} />,
    offers: <OffersTable onDeleteOffer={handleDeleteOffer} />,
    users: <UsersTable onDeleteUser={handleDeleteUser} />,
    orders: <OrdersTable onDeleteOrder={handleDeleteOrder} />,
    guestorders: (
      <GuestOrdersTable onDeleteGuestOrder={handleDeleteGuestOrder} />
    ),
    gallerysettings: <GallerySettings />,
  };

  return (
    <div className="admin-panel-wrapper py-4 px-2">
      <div className="text-center mb-4">
        <h2 className="fw-bold">{t("admin.panelTitle")}</h2>
        <p className="text-muted mb-0">{t("admin.panelDesc")}</p>
      </div>

      <div
        className="d-flex flex-wrap justify-content-center gap-3 mb-4"
        ref={menuBarRef}
      >
        {menus.map((menu, i) => {
          // highlight the menu that owns the currently active view
          const isActive = menu.actions.some((a) => a.view === activeView);
          const isOpen = openMenu === i;
          return (
            <div
              className={`dropdown${isOpen ? " show" : ""}`}
              key={menu.label}
            >
              <button
                type="button"
                aria-haspopup="true"
                aria-expanded={isOpen}
                className={`admin-menu-button dropdown-toggle${
                  isActive ? " active" : ""
                }`}
                onClick={() => setOpenMenu(isOpen ? null : i)}
              >
                <FontAwesomeIcon icon={menu.icon} className="me-2" />
                {menu.label}
              </button>
              <div className={`dropdown-menu${isOpen ? " show" : ""}`}>
                {menu.actions.map((action) => (
                  <button
                    key={action.view}
                    type="button"
                    className={`dropdown-item${
                      action.view === activeView ? " active" : ""
                    }`}
                    onClick={() => selectView(action.view)}
                  >
                    {action.label}
                  </button>
                ))}
              </div>
            </div>
          );
        })}
      </div>

      <div
        className="admin-panel-content mx-auto"
        style={{ maxWidth: "1400px" }}
      >
        {activeView && views[activeView] && (
          <div className="fade-in card shadow p-4 mb-5" key={activeView}>
            {views[activeView]}
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
