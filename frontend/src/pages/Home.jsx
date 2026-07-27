import {
  Container,
  Row,
  Col,
  Card,
  Image,
  Button,
  Ratio,
} from "react-bootstrap";
import { useTranslation } from "react-i18next";

// data kept in arrays
const SERVICES = [
  {
    key: "haircut",
    titleKey: "home.services.haircut",
    descKey: "home.services.haircutDesc",
  },
  {
    key: "beard",
    titleKey: "home.services.beard",
    descKey: "home.services.beardDesc",
  },
  {
    key: "facial",
    titleKey: "home.services.facial",
    descKey: "home.services.facialDesc",
  },
];

const BARBERS = [
  {
    key: "marcin",
    name: "Marcin Wolny",
    img: "/images/employees/marcinwolny.jpg",
    bioKey: "home.barbers.marcinBio",
  },
  {
    key: "monika",
    name: "Monika Kowalska",
    img: "/images/employees/monikakowalska.jpg",
    bioKey: "home.barbers.monikaBio",
  },
  {
    key: "oskar",
    name: "Oskar Kozłowski",
    img: "/images/employees/oskarkozlowski.jpg",
    bioKey: "home.barbers.oskarBio",
  },
];

const PROMOTIONS = [
  {
    key: "combo",
    titleKey: "home.promotions.combo",
    descKey: "home.promotions.comboPrice",
  },
  {
    key: "discount",
    titleKey: "home.promotions.discount",
    descKey: "home.promotions.discountDesc",
  },
  {
    key: "products",
    titleKey: "home.promotions.products",
    descKey: "home.promotions.productsDesc",
  },
];

const Home = () => {
  const { t } = useTranslation();

  return (
    <div className="bg-body">
      {/* hero */}
      <header className="py-5 text-center">
        <Container>
          <h1 className="display-4 fw-bold">{t("home.hero.title")}</h1>
          <p
            className="lead text-body-secondary mx-auto"
            style={{ maxWidth: "42rem" }}
          >
            {t("home.hero.lead")}
          </p>
          {/* accent cta: amber pill button stands out on both light and dark themes */}
          <Button
            as={Link}
            to="/registerorder"
            size="lg"
            variant="warning"
            className="mt-4 px-5 py-3 fw-semibold rounded-pill shadow text-dark"
          >
            {t("home.hero.cta", "Zarezerwuj wizytę")}
          </Button>
        </Container>
      </header>

      {/* Usługi */}
      <section aria-labelledby="services-title" className="py-5">
        <Container>
          <h2 id="services-title" className="display-6 text-center mb-4">
            {t("home.services.title")}
          </h2>
          <Row className="g-4">
            {SERVICES.map((service) => (
              <Col key={service.key} md={4}>
                <Card className="h-100 text-center shadow-sm border-0">
                  <Card.Body>
                    <Card.Title as="h3" className="h5">
                      {t(service.titleKey)}
                    </Card.Title>
                    <Card.Text className="text-body-secondary">
                      {t(service.descKey)}
                    </Card.Text>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>
        </Container>
      </section>

      {/* Barberzy */}
      <section aria-labelledby="barbers-title" className="py-5">
        <Container>
          <h2 id="barbers-title" className="display-6 text-center mb-4">
            {t("home.barbers.title")}
          </h2>
          <Row className="g-4">
            {BARBERS.map((barber) => (
              <Col key={barber.key} md={4}>
                <Card className="h-100 shadow-sm border-0 overflow-hidden">
                  {/* square ratio + top-anchored crop so portrait heads are never cut off */}
                  <Ratio aspectRatio="1x1">
                    <Image
                      src={barber.img}
                      alt={t("home.barbers.photoAlt", {
                        name: barber.name,
                        defaultValue: `Zdjęcie: ${barber.name}`,
                      })}
                      style={{
                        objectFit: "cover",
                        objectPosition: "center top",
                      }}
                      loading="lazy"
                    />
                  </Ratio>
                  <Card.Body>
                    <Card.Title>{barber.name}</Card.Title>
                    <Card.Text className="text-body-secondary">
                      {t(barber.bioKey)}
                    </Card.Text>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>
        </Container>
      </section>

      {/* Promocje */}
      <section aria-labelledby="promotions-title" className="py-5">
        <Container>
          <h2 id="promotions-title" className="display-6 text-center mb-4">
            {t("home.promotions.title")}
          </h2>
          <Row className="g-4">
            {PROMOTIONS.map((promo) => (
              <Col key={promo.key} md={4}>
                <Card className="h-100 text-center shadow-sm border-0">
                  <Card.Body>
                    <Card.Title>{t(promo.titleKey)}</Card.Title>
                    <Card.Text className="text-body-secondary">
                      {t(promo.descKey)}
                    </Card.Text>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>
        </Container>
      </section>
    </div>
  );
};

export default Home;
