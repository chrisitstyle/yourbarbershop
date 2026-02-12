import { useTranslation } from "react-i18next";

const Footer = () => {
  const { t } = useTranslation();
  const currentYear = new Date().getFullYear();
  const author = "Krzysztof Podjacki";
  const githubUrl = "https://github.com/chrisitstyle";

  return (
    <footer className="footer mt-auto py-3 bg-dark text-white">
      <div className="container text-center">
        <p className="mb-0">
          &copy; {currentYear} YourBarbershop. {t("footer.rights")}{" "}
          {t("footer.createdBy")}{" "}
          <a
            href={githubUrl}
            target="_blank"
            rel="noopener noreferrer"
            style={{ color: "inherit" }}
          >
            {author}
          </a>
        </p>
      </div>
    </footer>
  );
};

export default Footer;
