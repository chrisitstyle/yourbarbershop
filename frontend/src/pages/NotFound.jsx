export default function NotFound() {
  return (
    <div style={{ textAlign: "center", padding: "4rem" }}>
      <h1>404</h1>
      <h2>Nie znaleziono strony</h2>
      <p>Przepraszamy, taka podstrona nie istnieje.</p>
      <a href="/" className="btn btn-primary mt-4">
        Wróć na stronę główną
      </a>
    </div>
  );
}
