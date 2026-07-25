import { useQuery } from "@tanstack/react-query";
import { getOrders } from "../api/orderService";

const useOrders = () => {
  const {
    data: orders = [], // default to an empty array
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ["orders"], // unique key for caching purposes
    queryFn: getOrders, // function responsible for fetching orders
  });

  return {
    orders,
    isLoading,
    error: error ? error?.message || "Błąd podczas ładowania zamówień" : null,
    refetch,
  };
};

export default useOrders;
