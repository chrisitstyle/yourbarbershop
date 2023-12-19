import React from "react";

import { Container, Row, Col, Card, Image } from "react-bootstrap";

const Home = () => {
  return (
    <div className="bg-light">
      <Container className="py-5 text-center">
        <h1 className="display-4">Witaj w Naszym Barbershopie</h1>
        <p className="lead">
          Doświadcz najlepszych usług w dziedzinie pielęgnacji i obsługi
          klienta.
        </p>
      </Container>

      <Container className="py-5">
        <Row className="text-center">
          <Col>
            <h2 className="display-6">Nasze Usługi</h2>
          </Col>
        </Row>
        <Row className="mt-4 text-center">
          <Col md={4}>
            <Card className="mb-4">
              <Card.Body>
                <h3 className="card-title">Strzyżenie</h3>
                <p className="card-text">
                  Profesjonalne i stylowe usługi strzyżenia.
                </p>
              </Card.Body>
            </Card>
          </Col>
          <Col md={4}>
            <Card className="mb-4">
              <Card.Body>
                <h3 className="card-title">Stylizacja brody</h3>
                <p className="card-text">
                  Usługi wysokiej jakości w zakresie stylizacji brody.
                </p>
              </Card.Body>
            </Card>
          </Col>
          <Col md={4}>
            <Card className="mb-4">
              <Card.Body>
                <h3 className="card-title">Zabiegi na twarz</h3>
                <p className="card-text">
                  Relaksujące i odmładzające zabiegi na twarz.
                </p>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>

      <Container className="py-5 text-center bg-light">
        <Row>
          <Col>
            <h2 className="display-6">Poznaj Naszych Barberów</h2>
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
                <Card.Text>
                  Doświadczony barber specjalizujący się w nowoczesnych i
                  dostojnych stylizacjach.
                </Card.Text>
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
                <Card.Text>
                  Ekspertka w dziedzinie pielęgnacji twarzy.
                </Card.Text>
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
                <Card.Text>
                  Specjalizuje się w klasycznych strzyżeniach męskich i
                  brodowych.
                </Card.Text>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>

      <Container className="py-5 text-center bg-light">
        <Row>
          <Col>
            <h2 className="display-6">Aktualne Promocje</h2>
          </Col>
        </Row>
        <Row className="mt-4">
          <Col md={4}>
            <Card className="mb-4">
              <Card.Body>
                <Card.Title>Strzyżenie + zabieg na twarz</Card.Title>
                <Card.Text>Teraz tylko za 99 złotych!</Card.Text>
              </Card.Body>
            </Card>
          </Col>
          <Col md={4}>
            <Card className="mb-4">
              <Card.Body>
                <Card.Title>Rabat dla stałych klientów</Card.Title>
                <Card.Text>Zyskaj 10% zniżki po pięciu wizytach!</Card.Text>
              </Card.Body>
            </Card>
          </Col>
          <Col md={4}>
            <Card className="mb-4">
              <Card.Body>
                <Card.Title>Promocja na produkty do pielęgnacji</Card.Title>
                <Card.Text>
                  Kup dwa produkty, a trzeci otrzymasz gratis!
                </Card.Text>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </div>
  );
};

export default Home;
