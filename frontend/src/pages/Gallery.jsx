import React, { useEffect, useState } from "react";
import { useSupabaseClient } from "@supabase/auth-helpers-react";
import { Container, Carousel, Image, Modal, Button } from "react-bootstrap";
import { CDNURL } from "../api/supabaseApi";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faArrowCircleLeft,
  faArrowCircleRight,
} from "@fortawesome/free-solid-svg-icons";

const Gallery = () => {
  const [images, setImages] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [selectedImage, setSelectedImage] = useState("");
  const supabase = useSupabaseClient();

  const handleImageClick = (image) => {
    setSelectedImage(image);
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
  };

  async function getImages() {
    try {
      const { data, error } = await supabase.storage
        .from("barbershopimages")
        .list("images", {
          limit: 100,
          offset: 0,
          sortBy: { column: "created_at", order: "desc" },
        });

      if (data !== null) {
        const filteredImages = data.filter((image) => {
          const lowercasedName = image.name.toLowerCase();
          return (
            !lowercasedName.includes(".emptyFolderPlaceholder") &&
            (lowercasedName.endsWith(".png") ||
              lowercasedName.endsWith(".jpeg") ||
              lowercasedName.endsWith(".jpg"))
          );
        });
        setImages(filteredImages);
      } else {
        //alert("Error loading images");
        console.error("Data is null, error:", error);
      }
    } catch (error) {
      console.error("Error fetching images:", error.message);
    }
  }

  useEffect(() => {
    getImages();
  }, []);

  return (
    <Container className="text-center mt-4">
      <h2 className="display-6">Nasze portfolio</h2>
      <p className="lead">
        Odkryj artystyczną i kreatywną pracę naszych utalentowanych barberów.
        Każde zdjęcie prezentuje precyzję i umiejętności, jakie wkładamy w każdą
        przemianę naszego klienta.
      </p>
      {images.length > 0 ? (
        <Carousel
          nextIcon={
            <FontAwesomeIcon
              icon={faArrowCircleRight}
              className="arrow-icon arrow-right"
            />
          }
          prevIcon={
            <FontAwesomeIcon
              icon={faArrowCircleLeft}
              className="arrow-icon arrow-left"
            />
          }
          interval={null}
        >
          {images.map((image) => (
            <Carousel.Item
              key={CDNURL + "images/" + image.name}
              onClick={() => handleImageClick(image)}
              style={{ cursor: "pointer" }}
            >
              <Image
                className="d-block w-100 "
                src={CDNURL + "images/" + image.name}
                alt={image.name}
                style={{
                  width: "500px",
                  height: "500px",
                  objectFit: "contain",
                }}
              />
            </Carousel.Item>
          ))}
        </Carousel>
      ) : (
        <h5 className="mt-5">Ładowanie... </h5>
      )}
      {/* Modal */}
      <Modal show={showModal} onHide={handleCloseModal}>
        <Modal.Header closeButton>
          <Modal.Title style={{ textAlign: "center", width: "100%" }}>
            {selectedImage.name &&
              selectedImage.name.slice(0, selectedImage.name.lastIndexOf("."))}
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Image
            src={CDNURL + "images/" + selectedImage.name}
            alt={selectedImage.name}
            style={{ width: "100%" }}
          />
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={handleCloseModal}>
            Zamknij
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
};

export default Gallery;
