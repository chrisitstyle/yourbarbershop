import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

const AddOffer = ({ onAddOffer, onCancel }) => {
  const [kind, setKind] = useState("");
  const [cost, setCost] = useState("");
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();

    onAddOffer({ kind, cost });

    setKind("");
    setCost("");
    navigate("/adminpanel");
  };

  return (
    <>
      <h2>Dodawanie oferty</h2>
      <div className="container mt-5">
        <div className="row justify-content-center">
          <div className="col-md-4 border p-3 ">
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label htmlFor="inputkind" className="form-label">
                  Rodzaj
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="kind"
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
                  value={cost}
                  onChange={(e) => setCost(e.target.value)}
                  required
                />
              </div>
              <button type="submit" className="btn btn-primary">
                Dodaj
              </button>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={onCancel}
              >
                Anuluj
              </button>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default AddOffer;
