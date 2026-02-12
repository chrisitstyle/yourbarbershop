import { Container, Row, Col, Card, Image } from "react-bootstrap";
import { useTranslation } from "react-i18next";

const Home = () => {
  const { t } = useTranslation();

  return (
    <div className="bg-body">
      <Container className="py-5 text-center">
        <h1 className="display-4">{t("home.hero.title")}</h1>
        <p className="lead">{t("home.hero.lead")}</p>
      </Container>

      <Container className="py-5">
        <Row className="text-center">
          <Col>
            <h2 className="display-6">{t("home.services.title")}</h2>
          </Col>
        </Row>
        <Row className="mt-4 text-center">
          <Col md={4}>
            <Card className="mb-4">
              <Card.Body>
                <h3 className="card-title">{t("home.services.haircut")}</h3>
                <p className="card-text">{t("home.services.haircutDesc")}</p>
              </Card.Body>
            </Card>
          </Col>
          <Col md={4}>
            <Card className="mb-4">
              <Card.Body>
                <h3 className="card-title">{t("home.services.beard")}</h3>
                <p className="card-text">{t("home.services.beardDesc")}</p>
              </Card.Body>
            </Card>
          </Col>
          <Col md={4}>
            <Card className="mb-4">
              <Card.Body>
                <h3 className="card-title">{t("home.services.facial")}</h3>
                <p className="card-text">{t("home.services.facialDesc")}</p>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>

      <Container className="py-5 text-center bg-body">
        <Row>
          <Col>
            <h2 className="display-6">{t("home.barbers.title")}</h2>
          </Col>
        </Row>
        <Row className="mt-4">
          <Col md={4}>
            <Card className="mb-4">
              <Image
                src="./images/employees/marcinwolny.jpg"
                alt="employee photo"
                fluid
                style={{
                  width: "900px",
                  height: "500px",
                  objectFit: "cover",
                }}
              />
              <Card.Body>
                <Card.Title>Marcin Wolny</Card.Title>
                <Card.Text>{t("home.barbers.marcinBio")}</Card.Text>
              </Card.Body>
            </Card>
          </Col>

          <Col md={4}>
            <Card className="mb-4">
              <Image
                src="./images/employees/monikakowalska.jpg"
                fluid
                style={{
                  width: "900px",
                  height: "500px",
                  objectFit: "cover",
                }}
              />
              <Card.Body>
                <Card.Title>Monika Kowalska</Card.Title>
                <Card.Text>{t("home.barbers.monikaBio")}</Card.Text>
              </Card.Body>
            </Card>
          </Col>
          <Col md={4}>
            <Card className="mb-4">
              <Image
                src="./images/employees/oskarkozlowski.jpg"
                fluid
                style={{
                  width: "900px",
                  height: "500px",
                  objectFit: "cover",
                }}
              />
              <Card.Body>
                <Card.Title>Oskar Kozłowski</Card.Title>
                <Card.Text>{t("home.barbers.oskarBio")}</Card.Text>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>

      <Container className="py-5 text-center bg-body">
        <Row>
          <Col>
            <h2 className="display-6">{t("home.promotions.title")}</h2>
          </Col>
        </Row>
        <Row className="mt-4">
          <Col md={4}>
            <Card className="mb-4">
              <Card.Body>
                <Card.Title>{t("home.promotions.combo")}</Card.Title>
                <Card.Text>{t("home.promotions.comboPrice")}</Card.Text>
              </Card.Body>
            </Card>
          </Col>
          <Col md={4}>
            <Card className="mb-4">
              <Card.Body>
                <Card.Title>{t("home.promotions.discount")}</Card.Title>
                <Card.Text>{t("home.promotions.discountDesc")}</Card.Text>
              </Card.Body>
            </Card>
          </Col>
          <Col md={4}>
            <Card className="mb-4">
              <Card.Body>
                <Card.Title>{t("home.promotions.products")}</Card.Title>
                <Card.Text>{t("home.promotions.productsDesc")}</Card.Text>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </div>
  );
};

export default Home;
