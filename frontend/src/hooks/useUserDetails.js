import { useEffect, useState } from "react";

import { API_BASE_URL } from "../api/config";
import { apiRequest } from "../api/httpClient";

const useUserDetails = (userId) => {
  const [userDetails, setUserDetails] = useState(null);
  const [isLoading, setIsLoading] = useState(Boolean(userId));
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!userId) {
      setUserDetails(null);
      setIsLoading(false);
      return;
    }

    const abortController = new AbortController();

    const loadUserDetails = async () => {
      setIsLoading(true);
      setError(null);

      try {
        const response = await apiRequest(`${API_BASE_URL}/users/${userId}`, {
          method: "GET",
          signal: abortController.signal,
        });

        setUserDetails(response.data);
      } catch (error) {
        if (error.name !== "AbortError") {
          console.error("Error loading user details:", error);
          setError("Błąd ładowania użytkownika");
        }
      } finally {
        if (!abortController.signal.aborted) {
          setIsLoading(false);
        }
      }
    };

    loadUserDetails();

    return () => {
      abortController.abort();
    };
  }, [userId]);

  return {
    userDetails,
    isLoading,
    error,
  };
};

export default useUserDetails;
