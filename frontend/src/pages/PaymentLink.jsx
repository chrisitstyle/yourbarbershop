import { useState } from "react";

import { Alert, Button, Spinner } from "react-bootstrap";
import { Link, useParams } from "react-router-dom";

import { resolvePaymentCheckout } from "../api/paymentService";

const PaymentLink = () => {
  const { token } = useParams();

  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const handlePayment = async () => {
    if (!token || isLoading) {
      return;
    }

    setIsLoading(true);
    setErrorMessage("");

    try {
      const response = await resolvePaymentCheckout(token);
      const checkoutUrl = response?.checkoutUrl;

      if (!checkoutUrl) {
        throw new Error("Nie udało się uzyskać adresu płatności");
      }

      window.location.assign(checkoutUrl);
    } catch (error) {
      setErrorMessage(
        error?.response?.data?.message ||
          error?.message ||
          "Nie udało się przejść do płatności",
      );

      setIsLoading(false);
    }
  };

  return (
    <div className="container my-5 py-5">
      <div className="row justify-content-center">
        <div className="col-md-6">
          <div className="card shadow-sm">
            <div className="card-body p-4 text-center">
              <h2 className="mb-3">Dokończ płatność</h2>

              <p className="text-body-secondary mb-4">
                Twoja rezerwacja oczekuje na płatność online. Kliknij poniżej,
                aby bezpiecznie przejść do Stripe Checkout.
              </p>

              {errorMessage && <Alert variant="danger">{errorMessage}</Alert>}

              <div className="d-flex justify-content-center gap-2">
                <Button
                  variant="dark"
                  onClick={handlePayment}
                  disabled={isLoading || !token}
                >
                  {isLoading ? (
                    <>
                      <Spinner
                        animation="border"
                        size="sm"
                        className="me-2"
                        aria-hidden="true"
                      />
                      Przekierowywanie...
                    </>
                  ) : (
                    "Przejdź do płatności"
                  )}
                </Button>

                <Button
                  as={Link}
                  to="/"
                  variant="outline-secondary"
                  disabled={isLoading}
                >
                  Wróć na stronę główną
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PaymentLink;
