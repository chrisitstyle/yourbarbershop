import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faChevronLeft,
  faChevronRight,
} from "@fortawesome/free-solid-svg-icons";

const PaginationControl = ({ currentPage, totalPages, onPageChange }) => {
  if (totalPages <= 1) return null;

  return (
    <nav className="pagination justify-content-center mt-4">
      <ul className="pagination">
        {/* previous */}
        <li className={`page-item ${currentPage === 1 ? "disabled" : ""}`}>
          <button
            className="page-link"
            onClick={() => onPageChange(currentPage - 1)}
            disabled={currentPage === 1}
            aria-label="Poprzednia"
            style={{ minWidth: "38px" }}
          >
            <FontAwesomeIcon icon={faChevronLeft} />
          </button>
        </li>

        {/* page numbers */}
        {[...Array(totalPages)].map((_, index) => (
          <li
            key={index + 1}
            className={`page-item ${index + 1 === currentPage ? "active" : ""}`}
          >
            <button
              className="page-link"
              onClick={() => onPageChange(index + 1)}
              style={{ minWidth: "38px" }}
            >
              {index + 1}
            </button>
          </li>
        ))}

        {/* next */}
        <li
          className={`page-item ${
            currentPage === totalPages ? "disabled" : ""
          }`}
        >
          <button
            className="page-link"
            onClick={() => onPageChange(currentPage + 1)}
            disabled={currentPage === totalPages}
            aria-label="Następna"
            style={{ minWidth: "38px" }}
          >
            <FontAwesomeIcon icon={faChevronRight} />
          </button>
        </li>
      </ul>
    </nav>
  );
};

export default PaginationControl;
