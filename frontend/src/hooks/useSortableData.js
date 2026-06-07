import { useMemo, useState } from "react";

const getValueByPath = (obj, path) => {
  if (!path) return null;

  return path.split(".").reduce((value, key) => value?.[key], obj);
};

const isDateLikeString = (value) => {
  return typeof value === "string" && /^\d{4}-\d{2}-\d{2}/.test(value);
};

const normalizeSortableValue = (value) => {
  if (value === null || value === undefined || value === "") {
    return null;
  }

  if (typeof value === "number") {
    return value;
  }

  if (value instanceof Date) {
    return value.getTime();
  }

  if (isDateLikeString(value)) {
    const dateValue = new Date(value).getTime();
    return Number.isNaN(dateValue) ? value : dateValue;
  }

  const numberValue = Number(value);

  if (!Number.isNaN(numberValue)) {
    return numberValue;
  }

  return String(value).toLowerCase();
};

const compareValues = (firstValue, secondValue, direction) => {
  if (firstValue === null && secondValue === null) return 0;
  if (firstValue === null) return 1;
  if (secondValue === null) return -1;

  let result;

  if (typeof firstValue === "number" && typeof secondValue === "number") {
    result = firstValue - secondValue;
  } else {
    result = String(firstValue).localeCompare(String(secondValue), "pl", {
      numeric: true,
      sensitivity: "base",
    });
  }

  return direction === "asc" ? result : -result;
};

const useSortableData = (
  data,
  initialSortConfig = {
    field: null,
    direction: "asc",
  },
) => {
  const [sortConfig, setSortConfig] = useState(initialSortConfig);

  const sortedData = useMemo(() => {
    if (!sortConfig.field) return data;

    return [...data].sort((firstItem, secondItem) => {
      const firstValue = normalizeSortableValue(
        getValueByPath(firstItem, sortConfig.field),
      );

      const secondValue = normalizeSortableValue(
        getValueByPath(secondItem, sortConfig.field),
      );

      return compareValues(firstValue, secondValue, sortConfig.direction);
    });
  }, [data, sortConfig]);

  const handleSort = (field) => {
    setSortConfig((previousSortConfig) => ({
      field,
      direction:
        previousSortConfig.field === field &&
        previousSortConfig.direction === "asc"
          ? "desc"
          : "asc",
    }));
  };

  return {
    sortedData,
    sortConfig,
    handleSort,
  };
};

export default useSortableData;
