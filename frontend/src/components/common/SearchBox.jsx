const SearchBox = ({
  value,
  onChange,
  placeholder = "Szukaj...",
  width = "300px",
}) => {
  return (
    <div className="mb-3">
      <input
        type="text"
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="form-control mx-auto"
        style={{ maxWidth: width, width: "100%", fontSize: "1rem" }}
      />
    </div>
  );
};

export default SearchBox;
