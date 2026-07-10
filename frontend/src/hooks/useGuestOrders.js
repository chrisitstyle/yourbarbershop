import { useCallback, useEffect, useState } from "react";
import guestOrderService from "../api/guestOrderService";

const useGuestOrders = () => {
  const [guestOrders, setGuestOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchGuestOrders = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const data = await guestOrderService.getGuestOrders();
      setGuestOrders(data);
    } catch (error) {
      setError(error?.message || "Błąd podczas ładowania wizyt gości");
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchGuestOrders();
  }, [fetchGuestOrders]);

  return {
    guestOrders,
    isLoading,
    error,
    refetch: fetchGuestOrders,
  };
};

export default useGuestOrders;
