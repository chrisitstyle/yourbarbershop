import { useState, useEffect } from "react";
import { useAuth } from "../AuthContext";
import { useParams, useLocation } from "react-router-dom";
import { Alert } from "react-bootstrap";
import LoadingSpinner from "../components/common/LoadingSpinner";
import useUserDetails from "../hooks/useUserDetails";
import useTableData from "../hooks/useTableData";
import PaginationControl from "../components/common/PaginationControl";
import SearchBox from "../components/common/SearchBox";
import { getNestedValue } from "../utils/tableHelpers";

const visitHeaders = [
  "Identyfikator wizyty",
  "Usługa",
  "Koszt",
  "Data złożenia wizyty",
  "Data wizyty",
  "Status",
];

const visitFields = [
  "idOrder",
  "offer.kind",
  "offer.cost",
  "orderDate",
  "visitDate",
  "status",
];

const Profile = () => {
  const { user } = useAuth();
  const { id } = useParams();
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const registrationOrderSuccess = searchParams.get("registrationOrderSuccess");

  const { userDetails, isLoading, error } = useUserDetails(id, user?.token);

  const [showSuccessAlert, setShowSuccessAlert] = useState(
    Boolean(registrationOrderSuccess)
  );

  useEffect(() => {
    let timeout;
    if (showSuccessAlert) {
      timeout = setTimeout(() => setShowSuccessAlert(false), 3000);
    }
    return () => clearTimeout(timeout);
  }, [showSuccessAlert]);

  const filterVisits = (order, term) => {
    return `${order.idOrder} ${order.offer?.kind || ""} ${
      order.offer?.cost || ""
    } ${order.orderDate} ${order.visitDate} ${order.status}`
      .toLowerCase()
      .includes(term.toLowerCase());
  };

  const safeOrders = userDetails?.userOrders || [];
  const {
    searchTerm,
    handleSearchChange,
    currentData,
    currentPage,
    totalPages,
    setCurrentPage,
  } = useTableData(safeOrders, filterVisits);

  if (isLoading) return <LoadingSpinner text="Ładowanie profilu..." />;

  if (error) {
    return (
      <Alert variant="danger" className="text-center">
        {error}
      </Alert>
    );
  }

  return (
    <div className="container my-5 py-4 text-center">
      <div>
        {showSuccessAlert && (
          <Alert
            variant="success"
            className="text-center mx-auto"
            style={{ maxWidth: 440 }}
          >
            Twoja wizyta została zarejestrowana
          </Alert>
        )}

        <h2 className="mb-3">Twój profil</h2>
        <div
          className="mb-2"
          style={{ fontWeight: "500", fontSize: "1.06rem" }}
        >
          {`Jesteś zalogowany jako `}
          <span className="fw-bold">{userDetails?.email ?? "Brak danych"}</span>
        </div>
        <div className="mb-4" style={{ color: "#666" }}>
          {(userDetails?.firstname ?? "Użytkownik") +
            ", poniżej znajdują się wszystkie dotychczasowe wizyty"}
        </div>

        {/* search box */}
        <SearchBox
          value={searchTerm}
          onChange={handleSearchChange}
          placeholder="Szukaj wizyty..."
        />

        {/* table */}
        <div className="table-responsive">
          <table
            className="table table-bordered table-hover shadow rounded mx-auto"
            style={{ maxWidth: "900px" }}
          >
            <thead className="table-dark">
              <tr>
                {visitHeaders.map((header) => (
                  <th
                    key={header}
                    scope="col"
                    className="text-center align-middle"
                  >
                    {header}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {currentData.length > 0 ? (
                currentData.map((order) => (
                  <tr key={order.idOrder}>
                    {visitFields.map((field) => (
                      <td key={field} className="align-middle text-center">
                        {getNestedValue(order, field)}
                      </td>
                    ))}
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={visitFields.length} className="text-center py-4">
                    <Alert variant="info" className="mb-0">
                      Brak wyników do wyświetlenia.
                    </Alert>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* pagination */}
        <PaginationControl
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={setCurrentPage}
        />
      </div>
    </div>
  );
};

export default Profile;
