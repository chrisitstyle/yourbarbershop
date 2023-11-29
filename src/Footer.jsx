import React from "react";

const Footer = () => {
  const currentYear = new Date().getFullYear();
  const author = "Krzysztof Podjacki";

  return (
    <footer className="footer mt-auto py-3 bg-primary text-white fixed-bottom">
      <div className="container text-center">
        <p className="mb-0">
          &copy; {currentYear} Barbershop. Wszelkie prawa zastrzeżone. Projekt
          wykonany przez {author}
        </p>
      </div>
    </footer>
  );
};

export default Footer;
