import { useQuery } from "@tanstack/react-query";
import { getOffers } from "../api/offerService";

const useOffers = () => {
  const {
    data: offers = [], // default to an empty array
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ["offers"], // unique key for caching purposes
    queryFn: getOffers, // function responsible for fetching offers
  });

  return {
    offers,
    isLoading,
    error: error ? error?.message || "Błąd podczas ładowania usług" : null,
    refetch,
  };
};

export default useOffers;
