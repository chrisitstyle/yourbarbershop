import React, { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../AuthContext";
import axios from "axios";

const EditOffer = () => {
  const { user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const offerData = location.state?.offerData;

  const [kind, setKind] = useState("");
  const [cost, setCost] = useState("");

  useEffect(() => {
    if (offerData) {
      setKind(offerData.kind);
      setCost(offerData.cost);
    }
  }, [offerData]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const response = await axios.put(
        `http://localhost:8080/offers/update/${offerData.idOffer}`,
        {
          kind,
          cost,
        },
        {
          withCredentials: true,
          headers: {
            Authorization: `Bearer ${user.token}`,
          },
        }
      );

      if (response.status === 200) {
        console.log("Pomyślnie zaktualizowano ofertę");
        navigate("/adminpanel");
      } else {
        console.error("Błąd podczas aktualizacji oferty:", response.statusText);
      }
    } catch (error) {
      console.error("Błąd podczas aktualizacji oferty:", error.message);
    }
  };

  return (
    <>
      <h2 className="text-center">Edytowanie oferty</h2>
      <div className="container mt-5">
        <div className="row justify-content-center">
          <div className="col-md-4 border p-3">
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label htmlFor="inputkind" className="form-label">
                  Rodzaj
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="kind"
                  name="kind"
                  value={kind}
                  onChange={(e) => setKind(e.target.value)}
                  required
                />
              </div>
              <div className="mb-3">
                <label htmlFor="inputcost" className="form-label">
                  Cena
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="cost"
                  name="cost"
                  value={cost}
                  onChange={(e) => setCost(e.target.value)}
                  required
                />
              </div>
              <button type="submit" className="btn btn-primary mx-auto d-block">
                Zapisz zmiany
              </button>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default EditOffer;
