import { useState, useEffect } from "react";
import axios from "axios";

const useUserDetails = (userId, userToken) => {
  const [userDetails, setUserDetails] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!userId || !userToken) return;
    setIsLoading(true);
    setError(null);
    axios
      .get(`http://localhost:8080/users/${userId}`, {
        withCredentials: true,
        headers: { Authorization: `Bearer ${userToken}` },
      })
      .then((res) => setUserDetails(res.data))
      .catch(() => setError("Błąd ładowania użytkownika"))
      .finally(() => setIsLoading(false));
  }, [userId, userToken]);

  return { userDetails, isLoading, error };
};

export default useUserDetails;
