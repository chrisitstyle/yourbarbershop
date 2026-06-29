import { Spinner } from "react-bootstrap";

const LoadingSpinner = ({
  text = "Ładowanie.. .",
  size = "md",
  variant = "dark",
}) => {
  const spinnerSize = size === "sm" ? "sm" : undefined;

  return (
    <div className="d-flex flex-column justify-content-center align-items-center py-5">
      <Spinner
        animation="border"
        role="status"
        variant={variant}
        size={spinnerSize}
      >
        <span className="visually-hidden">{text}</span>
      </Spinner>
      {text && <p className="mt-3 text-muted">{text}</p>}
    </div>
  );
};

export default LoadingSpinner;
