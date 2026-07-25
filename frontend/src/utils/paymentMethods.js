// utility helper to get translated payment method options
export const getPaymentMethods = (t) => [
  {
    value: "GOTOWKA",
    label: t("orders.paymentCash", "Gotówka na miejscu"),
  },
  {
    value: "KARTA_NA_MIEJSCU",
    label: t("orders.paymentCardOnSite", "Karta na miejscu"),
  },
  {
    value: "KARTA_ONLINE",
    label: t("orders.paymentCardOnline", "Karta online"),
  },
];
