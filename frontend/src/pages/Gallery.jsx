import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { useSupabaseClient, CDNURL } from "../api/supabaseApi";
import { Container, Carousel, Image, Modal, Button } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faArrowCircleLeft,
  faArrowCircleRight,
} from "@fortawesome/free-solid-svg-icons";
import LoadingSpinner from "../components/common/LoadingSpinner";
import { useTranslation } from "react-i18next";

const Gallery = () => {
  const [showModal, setShowModal] = useState(false);
  const [selectedImage, setSelectedImage] = useState(null);
  const supabase = useSupabaseClient();
  const { t } = useTranslation();

  const handleImageClick = (image) => {
    setSelectedImage(image);
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
  };

  // fetch images using tanstack query
  const { data: images = [], isLoading } = useQuery({
    queryKey: ["galleryImages"],
    queryFn: async () => {
      const { data, error } = await supabase.storage
        .from("barbershopimages")
        .list("images", {
          limit: 100,
          offset: 0,
          sortBy: { column: "created_at", order: "desc" },
        });

      if (error) {
        console.error("error fetching images:", error.message);
        throw new Error(error.message);
      }

      if (!data) return [];

      return data.filter((image) => {
        const lowercasedName = image.name.toLowerCase();
        return (
          !lowercasedName.includes(".emptyfolderplaceholder") &&
          (lowercasedName.endsWith(".png") ||
            lowercasedName.endsWith(".jpeg") ||
            lowercasedName.endsWith(".jpg"))
        );
      });
    },
    enabled: !!supabase,
  });

  /**
   * renders gallery carousel content based on loading and data availability (S3358)
   *
   * @returns {JSX.Element|null} spinner, carousel, or null
   */
  const renderGalleryContent = () => {
    if (isLoading) {
      return <LoadingSpinner text={t("gallery.loading")} />;
    }

    if (images.length > 0) {
      return (
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
                className="d-block w-100"
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
      );
    }

    return null;
  };

  return (
    <Container className="text-center mt-4">
      <h2 className="display-6">{t("gallery.title")}</h2>
      <p className="lead">{t("gallery.lead")}</p>

      {renderGalleryContent()}

      {/* modal */}
      <Modal show={showModal} onHide={handleCloseModal}>
        <Modal.Header closeButton>
          <Modal.Title style={{ textAlign: "center", width: "100%" }}>
            {selectedImage?.name?.slice(0, selectedImage.name.lastIndexOf("."))}
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedImage?.name && (
            <Image
              src={CDNURL + "images/" + selectedImage.name}
              alt={selectedImage.name}
              style={{ width: "100%" }}
            />
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={handleCloseModal}>
            {t("common.close")}
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
};

export default Gallery;
