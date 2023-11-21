import React, { useState, useEffect } from "react";
import axios from "axios";

const RegisterOrderWithoutAcc = () => {
  const [firstname, setFirstName] = useState("");
  const [lastname, setLastName] = useState("");
  const [phonenumber, setPhoneNumber] = useState("");
  const [selectedOffer, setSelectedOffer] = useState("");
  const [offers, setOffers] = useState([]);

  useEffect(() => {
    loadOffers();
  }, []);

  const loadOffers = async () => {
    try {
      const result = await axios.get("http://localhost:8080/offers/get");
      setOffers(result.data);
    } catch (error) {
      console.error("Error loading offers:", error);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    console.log("Selected offer:", selectedOffer);
    console.log("First name:", firstname);
    console.log("Last name:", lastname);
    console.log("PhoneNumber:", phonenumber);
  };

  return (
    <>
      <div className="container mt-5">
        <div className="row justify-content-center">
          <div className="col-md-4 border p-3">
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label htmlFor="inputfirstname" className="form-label">
                  Imie
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="firstname"
                  value={firstname}
                  onChange={(e) => setFirstName(e.target.value)}
                  required
                />
              </div>
              <div className="mb-3">
                <label htmlFor="inputlastname" className="form-label">
                  Nazwisko
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="lastname"
                  value={lastname}
                  onChange={(e) => setLastName(e.target.value)}
                  required
                />
              </div>
              <div className="mb-3">
                <label htmlFor="inputphonenumber" className="form-label">
                  Numer telefonu
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="phonenumber"
                  value={phonenumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  required
                />
              </div>
              <div className="mb-3">
                <label htmlFor="selectoffer" className="form-label">
                  Wybierz usługę
                </label>
                <select
                  className="form-select"
                  id="selectoffer"
                  value={selectedOffer}
                  onChange={(e) => setSelectedOffer(e.target.value)}
                  required
                >
                  <option value="" disabled></option>
                  {offers.map((offer) => (
                    <option key={offer.idOffer} value={offer.idOffer}>
                      {offer.kind} - {offer.cost} zł
                    </option>
                  ))}
                </select>
              </div>
              <button type="submit" className="btn btn-primary">
                Umów wizytę!
              </button>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default RegisterOrderWithoutAcc;
