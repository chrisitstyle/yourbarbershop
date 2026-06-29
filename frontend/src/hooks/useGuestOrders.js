import { useState, useEffect } from "react";
import guestOrderService from "../api/guestOrderService";

const useGuestOrders = (userToken) => {
  const [guestOrders, setGuestOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!userToken) {
      setIsLoading(false);
      setError("Brak tokena użytkownika!");
      return;
    }
    const fetchGuestOrders = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const data = await guestOrderService.getGuestOrders(userToken);
        setGuestOrders(data);
      } catch {
        setError("Błąd podczas ładowania wizyt gości");
      } finally {
        setIsLoading(false);
      }
    };

    fetchGuestOrders();
  }, [userToken]);

  const refetch = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await guestOrderService.getGuestOrders(userToken);
      setGuestOrders(data);
    } catch {
      setError("Błąd podczas ładowania wizyt gości");
    } finally {
      setIsLoading(false);
    }
  };

  return { guestOrders, isLoading, error, refetch };
};

export default useGuestOrders;
