import { useState, useEffect } from "react";
import { getOrders } from "../api/orderService";

const useOrders = (userToken) => {
  const [orders, setOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!userToken) {
      setIsLoading(false);
      setError("Brak tokena użytkownika!");
      return;
    }
    const fetchOrders = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const data = await getOrders(userToken);
        setOrders(data);
      } catch (err) {
        setError("Błąd podczas ładowania zamówień");
      } finally {
        setIsLoading(false);
      }
    };

    fetchOrders();
  }, [userToken]);

  const refetch = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await getOrders(userToken);
      setOrders(data);
    } catch (err) {
      setError("Błąd podczas ładowania zamówień");
    } finally {
      setIsLoading(false);
    }
  };

  return { orders, isLoading, error, refetch };
};

export default useOrders;
