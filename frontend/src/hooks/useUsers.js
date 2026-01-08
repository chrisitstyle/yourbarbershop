import { useState, useEffect, useCallback } from "react";
import userService from "../api/userService";

const useUsers = (userToken) => {
  const [users, setUsers] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchUsers = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await userService.getUsers(userToken);
      setUsers(data);
    } catch (err) {
      setError("Błąd podczas ładowania użytkowników");
    } finally {
      setIsLoading(false);
    }
  }, [userToken]);

  useEffect(() => {
    if (userToken) {
      fetchUsers();
    }
  }, [userToken, fetchUsers]);

  const refetch = fetchUsers;

  return { users, isLoading, error, refetch };
};

export default useUsers;
