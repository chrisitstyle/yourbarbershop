import { useQuery } from "@tanstack/react-query";
import userService from "../api/userService";

const useUsers = () => {
  const {
    data: users = [], // default to an empty array, same as previous usestate([])
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ["users"], // unique key for caching purposes
    queryFn: userService.getUsers, // function responsible for fetching data
  });

  return {
    users,
    isLoading,
    error: error
      ? error?.message || "Błąd podczas ładowania użytkowników"
      : null,
    refetch,
  };
};

export default useUsers;
