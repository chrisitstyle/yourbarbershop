import { useState, useEffect, useCallback } from "react";
import { getOffers } from "../api/offerService";

const useOffers = () => {
  const [offers, setOffers] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchOffers = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const data = await getOffers();
      setOffers(data);
    } catch (err) {
      setError("Błąd podczas ładowania usług");
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchOffers();
  }, [fetchOffers]);

  return { offers, isLoading, error, refetch: fetchOffers };
};

export default useOffers;
