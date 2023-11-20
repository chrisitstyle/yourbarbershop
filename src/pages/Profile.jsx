import React, { useEffect, useState } from "react";
import { useAuth } from "../AuthContext";
import axios from "axios";
import { useParams } from "react-router-dom";

const Profile = () => {
  const { user } = useAuth();
  const { id } = useParams();

  const [userDetails, setUserDetails] = useState(null);

  useEffect(() => {
    const loadUser = async () => {
      try {
        const result = await axios.get(
          `http://localhost:8080/users/get/${id}`,
          {
            withCredentials: true,
            headers: {
              Authorization: `Bearer ${user.token}`,
            },
          }
        );
        setUserDetails(result.data);
      } catch (error) {
        console.error("Błąd ładowania użytkownika", error);
      }
    };

    if (id && user.token) {
      loadUser();
    }
  }, [id, user.token]);

  if (!userDetails) {
    return <div>Loading...</div>;
  }

  return (
    <div className="container">
      <div className="py-4 ">
        <h6 className="text-center">{`Jesteś zalogowany jako ${userDetails.email}`}</h6>
        <div>
          {userDetails.userOrders && userDetails.userOrders.length > 0 && (
            <>
              <h6 className="text-center">
                {userDetails.firstname}, poniżej znajdują się wszystkie
                dotychczasowe wizyty
              </h6>
              <table className="table border shadow text-center">
                <thead>
                  <tr>
                    <th scope="col">Identyfikator wizyty</th>
                    <th scope="col">Usługa</th>
                    <th scope="col">Koszt</th>
                    <th scope="col">Data złożenia wizyty</th>
                    <th scope="col">Data wizyty</th>
                  </tr>
                </thead>
                <tbody>
                  {userDetails.userOrders.map((order) => (
                    <tr key={order.idOrder}>
                      <td>{order.idOrder}</td>
                      <td>{order.offer.kind}</td>
                      <td>{order.offer.cost} zł</td>
                      <td>{order.orderDate}</td>
                      <td>{order.visitDate}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default Profile;
