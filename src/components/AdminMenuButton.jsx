import React from "react";

export default function Button({ title }) {
  return (
    <button
      className={`btn btn-light dropdown-toggle`}
      type="button"
      id="servicesDropdown"
      data-bs-toggle="dropdown"
      aria-haspopup="true"
      aria-expanded="false"
    >
      {title}
    </button>
  );
}
