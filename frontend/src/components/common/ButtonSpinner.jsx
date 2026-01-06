import React from "react";
import { Button, Spinner } from "react-bootstrap";

const ButtonSpinner = ({
  loading,
  children,
  loadingText = "Przetwarzanie...",
  ...props
}) => {
  return (
    <Button {...props} disabled={loading || props.disabled}>
      {loading ? (
        <>
          <Spinner
            as="span"
            animation="border"
            size="sm"
            role="status"
            aria-hidden="true"
            className="me-2"
          />
          {loadingText}
        </>
      ) : (
        children
      )}
    </Button>
  );
};

export default ButtonSpinner;
