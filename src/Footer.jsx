import React from "react";

const Footer = () => {
  const currentYear = new Date().getFullYear();
  const author = "Krzysztof Podjacki";
  return (
    <footer className="footer mt-auto fixed-bottom py-3 bg-primary text-white">
      <div className="container text-center">
        <p>
          &copy; {currentYear} Twój Barber. Wszelkie prawa zastrzeżone. Projekt
          wykonany przez {author}
        </p>
      </div>
    </footer>
  );
};

export default Footer;
