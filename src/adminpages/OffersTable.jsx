import React from "react";
import { Link, useNavigate } from "react-router-dom";

const OffersTable = ({ data, onDeleteOffer }) => {
  const navigate = useNavigate();

  const handleEditClick = (offer) => {
    navigate(`/adminpanel/editoffer/${offer.idOffer}`, {
      state: { offerData: offer },
    });
  };

  return (
    <>
      <div className="container">
        <div className="py-4">
          <div>
            <table className="table border shadow text-center">
              <thead>
                <tr>
                  <th scope="col">Identyfikator usługi</th>
                  <th scope="col">Usługa</th>
                  <th scope="col">Koszt</th>
                  <th scope="col">Akcja</th>
                </tr>
              </thead>
              <tbody>
                {data.map((offer) => (
                  <tr key={offer.idOffer}>
                    <td>{offer.idOffer}</td>
                    <td>{offer.kind}</td>
                    <td>{offer.cost} zł</td>
                    <td>
                      <button
                        className="btn btn-warning"
                        onClick={() => {
                          handleEditClick(offer);
                        }}
                      >
                        Edytuj
                      </button>
                      <button
                        className="btn btn-danger mx-2"
                        onClick={() => onDeleteOffer(offer.idOffer)}
                      >
                        Usuń
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </>
  );
};

export default OffersTable;
