import { useState, useEffect, useRef, useCallback } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faScissors,
  faUser,
  faCalendarCheck,
  faImages,
  faClipboardList,
  faMoneyBillTransfer,
} from "@fortawesome/free-solid-svg-icons";
import { useTranslation } from "react-i18next";

import userService from "../api/userService.js";
import offerService from "../api/offerService.js";
import orderService from "../api/orderService.js";
import guestOrderService from "../api/guestOrderService.js";

import OffersTable from "./OffersTable";
import AddOffer from "./AddOffer";
import UsersTable from "./UsersTable";
import AddUser from "./AddUser";
import OrdersTable from "./OrdersTable";
import GuestOrdersTable from "./GuestOrdersTable.jsx";
import GallerySettings from "./GallerySettings.jsx";
import AuditLogsTable from "./AuditLogsTable.jsx";
import AdminRefundHistory from "./AdminRefundHistory.jsx";

/**
 * Provides the administrative dashboard and module navigation.
 *
 * Reservation cancellation is available in the order tables,
 * while refund history provides read-only access to persisted
 * Stripe refund information.
 */
const AdminPanel = () => {
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const [activeView, setActiveView] = useState(null);
  const [openMenu, setOpenMenu] = useState(null);

  const menuBarRef = useRef(null);

  /**
   * Closes the currently expanded navigation menu.
   */
  const closeMenu = useCallback(() => {
    setOpenMenu(null);
  }, []);

  /**
   * Selects an administrative module and closes the menu.
   *
   * @param {string} view - Administrative module identifier.
   */
  const selectView = useCallback((view) => {
    setActiveView(view);
    setOpenMenu(null);
  }, []);

  /**
   * Closes the navigation menu when the administrator clicks
   * outside the menu bar or presses Escape.
   */
  useEffect(() => {
    if (openMenu === null) {
      return undefined;
    }

    const handleOutsideClick = (event) => {
      if (menuBarRef.current && !menuBarRef.current.contains(event.target)) {
        closeMenu();
      }
    };

    const handleEscapeKey = (event) => {
      if (event.key === "Escape") {
        closeMenu();
      }
    };

    document.addEventListener("mousedown", handleOutsideClick);
    document.addEventListener("keydown", handleEscapeKey);

    return () => {
      document.removeEventListener("mousedown", handleOutsideClick);
      document.removeEventListener("keydown", handleEscapeKey);
    };
  }, [openMenu, closeMenu]);

  /**
   * Creates a service and refreshes the service list.
   */
  const addOfferMutation = useMutation({
    mutationFn: (newOffer) => offerService.addOffer(newOffer),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["offers"],
      });

      setActiveView("offers");
    },

    onError: (error) => {
      console.error("Failed to add offer:", error);
    },
  });

  /**
   * Deletes a service and refreshes the service list.
   */
  const deleteOfferMutation = useMutation({
    mutationFn: (offerId) => offerService.deleteOffer(offerId),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["offers"],
      });
    },

    onError: (error) => {
      console.error("Failed to delete offer:", error);
    },
  });

  /**
   * Creates a user and refreshes the user list.
   */
  const addUserMutation = useMutation({
    mutationFn: (newUser) => userService.addUser(newUser),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["users"],
      });

      setActiveView("users");
    },

    onError: (error) => {
      console.error("Failed to add user:", error);
    },
  });

  /**
   * Deletes a user and refreshes the user list.
   */
  const deleteUserMutation = useMutation({
    mutationFn: (userId) => userService.deleteUser(userId),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["users"],
      });
    },

    onError: (error) => {
      console.error("Failed to delete user:", error);
    },
  });

  /**
   * Deletes a registered reservation and refreshes its list.
   */
  const deleteOrderMutation = useMutation({
    mutationFn: (orderId) => orderService.deleteOrder(orderId),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["orders"],
      });
    },

    onError: (error) => {
      console.error("Failed to delete order:", error);
    },
  });

  /**
   * Deletes a guest reservation and refreshes its list.
   */
  const deleteGuestOrderMutation = useMutation({
    mutationFn: (guestOrderId) =>
      guestOrderService.deleteGuestOrder(guestOrderId),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["guestOrders"],
      });
    },

    onError: (error) => {
      console.error("Failed to delete guest order:", error);
    },
  });

  const handleAddOffer = (newOffer) => addOfferMutation.mutateAsync(newOffer);

  const handleDeleteOffer = (offerId) =>
    deleteOfferMutation.mutateAsync(offerId);

  const handleAddUser = (newUser) => addUserMutation.mutateAsync(newUser);

  const handleDeleteUser = (userId) => deleteUserMutation.mutateAsync(userId);

  const handleDeleteOrder = (orderId) =>
    deleteOrderMutation.mutateAsync(orderId);

  const handleDeleteGuestOrder = (guestOrderId) =>
    deleteGuestOrderMutation.mutateAsync(guestOrderId);

  /**
   * Defines the available administrative navigation modules.
   *
   * Every action points to a view registered in the views map.
   */
  const menus = [
    {
      icon: faScissors,
      label: t("admin.menu.offers"),
      actions: [
        {
          view: "offers",
          label: t("admin.actions.showOffers"),
        },
        {
          view: "addOffer",
          label: t("admin.actions.addOffer"),
        },
      ],
    },
    {
      icon: faUser,
      label: t("admin.menu.users"),
      actions: [
        {
          view: "users",
          label: t("admin.actions.showUsers"),
        },
        {
          view: "addUser",
          label: t("admin.actions.addUser"),
        },
      ],
    },
    {
      icon: faCalendarCheck,
      label: t("admin.menu.orders"),
      actions: [
        {
          view: "orders",
          label: t("admin.actions.showUserOrders"),
        },
        {
          view: "guestorders",
          label: t("admin.actions.showGuestOrders"),
        },
      ],
    },
    {
      icon: faMoneyBillTransfer,
      label: t("admin.menu.refunds"),
      actions: [
        {
          view: "refundHistory",
          label: t("admin.actions.showRefundHistory"),
        },
      ],
    },
    {
      icon: faImages,
      label: t("admin.menu.gallery"),
      actions: [
        {
          view: "gallerysettings",
          label: t("admin.actions.gallerySettings"),
        },
      ],
    },
    {
      icon: faClipboardList,
      label: t("admin.menu.audit"),
      actions: [
        {
          view: "auditlogs",
          label: t("admin.actions.showAudit"),
        },
      ],
    },
  ];

  /**
   * Maps navigation identifiers to administrative components.
   */
  const views = {
    addOffer: <AddOffer onAddOffer={handleAddOffer} />,

    addUser: <AddUser onSubmit={handleAddUser} />,

    offers: <OffersTable onDeleteOffer={handleDeleteOffer} />,

    users: <UsersTable onDeleteUser={handleDeleteUser} />,

    orders: <OrdersTable onDeleteOrder={handleDeleteOrder} />,

    guestorders: (
      <GuestOrdersTable onDeleteGuestOrder={handleDeleteGuestOrder} />
    ),

    refundHistory: <AdminRefundHistory />,

    gallerysettings: <GallerySettings />,

    auditlogs: <AuditLogsTable />,
  };

  return (
    <div className="admin-panel-wrapper py-4 px-2">
      <div className="text-center mb-4">
        <h2 className="fw-bold">{t("admin.panelTitle")}</h2>

        <p className="text-muted mb-0">{t("admin.panelDesc")}</p>
      </div>

      {/* Administrative navigation */}
      <div
        className="d-flex flex-wrap justify-content-center gap-3 mb-4"
        ref={menuBarRef}
      >
        {menus.map((menu, menuIndex) => {
          const isActive = menu.actions.some(
            (action) => action.view === activeView,
          );

          const isOpen = openMenu === menuIndex;

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
                onClick={() => setOpenMenu(isOpen ? null : menuIndex)}
              >
                <FontAwesomeIcon
                  icon={menu.icon}
                  className="me-2"
                  aria-hidden="true"
                />

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

      {/* Selected administrative module */}
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
