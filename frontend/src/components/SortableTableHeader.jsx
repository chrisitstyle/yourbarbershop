import { ArrowDownUp, ArrowDown, ArrowUp } from "lucide-react";
import { useTranslation } from "react-i18next";

const SortableTableHeader = ({
  headers,
  fields,
  sortConfig,
  onSort,
  children,
}) => {
  const { t } = useTranslation();

  const getSortIcon = (field) => {
    if (sortConfig.field !== field) {
      return <ArrowDownUp size={16} />;
    }

    return sortConfig.direction === "asc" ? (
      <ArrowUp size={16} />
    ) : (
      <ArrowDown size={16} />
    );
  };

  const getAriaSortValue = (field) => {
    if (sortConfig.field !== field) return "none";

    return sortConfig.direction === "asc" ? "ascending" : "descending";
  };

  return (
    <thead className="table-dark">
      <tr>
        {headers.map((header, index) => {
          const field = fields[index];

          return (
            <th
              key={header}
              scope="col"
              className="text-center align-middle"
              aria-sort={getAriaSortValue(field)}
            >
              <button
                type="button"
                className="btn btn-link p-0 text-white text-decoration-none fw-bold"
                onClick={() => onSort(field)}
              >
                <span className="d-inline-flex align-items-center justify-content-center gap-1">
                  {t(header)}
                  {getSortIcon(field)}
                </span>
              </button>
            </th>
          );
        })}

        {children}
      </tr>
    </thead>
  );
};

export default SortableTableHeader;
