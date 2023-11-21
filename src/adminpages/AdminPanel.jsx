import React, { useState } from "react";
import axios from "axios";
const AdminPanel = () => {
  const [offers, setOffers] = useState([]);

  const loadOffers = async () => {
    try {
      const result = await axios.get("http://localhost:8080/offers/get");
      setOffers(result.data);
      console.log(result.data);
    } catch (error) {
      console.error("Error loading offers:", error);
    }
  };
  return <div>AdminPanel</div>;
};

export default AdminPanel;
