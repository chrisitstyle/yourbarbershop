export default function AdminMenuButton({ title, className = "" }) {
  return (
    <button
      className={`btn dropdown-toggle ${className}`.trim()}
      type="button"
      data-bs-toggle="dropdown"
      aria-expanded="false"
    >
      {title}
    </button>
  );
}
