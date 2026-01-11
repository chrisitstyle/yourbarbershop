import { useState, useMemo } from "react";

/**
 * Hook to manage table data, including client-side filtering and pagination.
 *
 * @param {Array} data - The source array of objects to be displayed in the table.
 * @param {Function} filterFn - A callback function that accepts an item and the search term; must return true if the item matches.
 * @param {Number} [itemsPerPage=10] - The maximum number of items to display per page (defaults to 10).
 * @returns {Object} An object containing the current data slice, pagination state, and search handlers.
 */
const useTableData = (data, filterFn, itemsPerPage = 10) => {
  const [searchTerm, setSearchTerm] = useState("");
  const [currentPage, setCurrentPage] = useState(1);

  const handleSearchChange = (term) => {
    setSearchTerm(term);
    setCurrentPage(1);
  };

  const filteredData = useMemo(() => {
    if (!searchTerm) return data;
    return data.filter((item) => filterFn(item, searchTerm));
  }, [data, searchTerm, filterFn]);

  const totalPages = useMemo(
    () => Math.ceil(filteredData.length / itemsPerPage),
    [filteredData.length, itemsPerPage]
  );

  const currentData = useMemo(
    () =>
      filteredData.slice(
        (currentPage - 1) * itemsPerPage,
        currentPage * itemsPerPage
      ),
    [filteredData, currentPage, itemsPerPage]
  );

  return {
    searchTerm,
    handleSearchChange,
    currentData,
    currentPage,
    totalPages,
    setCurrentPage,
    totalCount: filteredData.length,
  };
};

export default useTableData;
