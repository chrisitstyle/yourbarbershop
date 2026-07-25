import { useQuery } from "@tanstack/react-query";
import { API_BASE_URL } from "../api/config";
import { apiRequest } from "../api/httpClient";

const useUserDetails = (userId) => {
  const {
    data: userDetails = null,
    isLoading,
    error,
    refetch,
  } = useQuery({
    // query key includes userid for caching purposes
    queryKey: ["userDetails", userId],

    // function responsible for fetching user details via apiRequest
    queryFn: async ({ signal }) => {
      const response = await apiRequest(`${API_BASE_URL}/users/${userId}`, {
        method: "GET",
        signal, // pass abortsignal to underlying fetch request
      });

      return response.data;
    },

    // execution guard - query runs only when userid is truthy
    enabled: Boolean(userId),
  });

  return {
    userDetails,
    isLoading: Boolean(userId) && isLoading,
    error: error ? "Błąd ładowania użytkownika" : null,
    refetch,
  };
};

export default useUserDetails;
