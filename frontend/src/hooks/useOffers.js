import { useState, useEffect } from "react";
import { getOffers } from "../api/offerService";

const useOffers = () => {
  const [offers, setOffers] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchOffers = async () => {
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
    };

    fetchOffers();
  }, []);

  return { offers, isLoading, error };
};

export default useOffers;
