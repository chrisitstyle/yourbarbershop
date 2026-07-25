import { useQuery } from "@tanstack/react-query";
import guestOrderService from "../api/guestOrderService";

const useGuestOrders = () => {
  const {
    data: guestOrders = [], // default to an empty array
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ["guestOrders"], // unique key for caching purposes
    queryFn: guestOrderService.getGuestOrders, // function responsible for fetching guest orders
  });

  return {
    guestOrders,
    isLoading,
    error: error
      ? error?.message || "Błąd podczas ładowania wizyt gości"
      : null,
    refetch,
  };
};

export default useGuestOrders;
