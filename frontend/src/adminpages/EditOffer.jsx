import React, { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../AuthContext";
import { updateOffer } from "../api/offerService";
import { Alert } from "react-bootstrap";

const EditOffer = () => {
  const { user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const offerData = location.state?.offerData;

  const [kind, setKind] = useState("");
  const [cost, setCost] = useState("");
  const [editOfferError, setEditOfferError] = useState(false);

  useEffect(() => {
    if (offerData) {
      setKind(offerData.kind);
      setCost(offerData.cost);
    }
  }, [offerData]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      await updateOffer(offerData.idOffer, { kind, cost }, user.token);
      navigate("/adminpanel");
    } catch (error) {
      setEditOfferError(true);
    }
  };

  return (
    <>
      <div className="container mt-5">
        <div className="row justify-content-center">
          <div className="col-md-4 border p-3">
            <h2 className="text-center">Edytowanie usługi</h2>
            <Alert
              variant="danger"
              show={editOfferError}
              onClose={() => setEditOfferError(false)}
              dismissible
            >
              Błąd podczas edytowania usługi. Spróbuj ponownie.
            </Alert>
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
              <button type="submit" className="btn btn-dark mx-auto d-block">
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
