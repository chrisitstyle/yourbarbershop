import { useCallback, useEffect, useState } from "react";
import { getOrders } from "../api/orderService";

const useOrders = () => {
  const [orders, setOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchOrders = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const data = await getOrders();
      setOrders(data);
    } catch (error) {
      setError(error?.message || "Błąd podczas ładowania zamówień");
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  return {
    orders,
    isLoading,
    error,
    refetch: fetchOrders,
  };
};

export default useOrders;
