import React, { useEffect, useState } from "react";
import { useSupabaseClient } from "@supabase/auth-helpers-react";
import { Container, Table, Modal, Button, Form } from "react-bootstrap";
import { CDNURL } from "../api/supabaseApi";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPlus, faTrashAlt } from "@fortawesome/free-solid-svg-icons";

const GallerySettings = () => {
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
          sortBy: { column: "name", order: "asc" },
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
        alert("Error loading images");
        console.error("Data is null, error:", error);
      }
    } catch (error) {
      console.error("Error fetching images:", error.message);
    }
  }

  const handleUploadImage = async (e) => {
    e.preventDefault();

    const inputElement = document.getElementById("formFile");
    const files = inputElement.files;

    if (!files || files.length === 0) {
      console.error("Nie wybrano plików");
      return;
    }

    try {
      // Iteruj przez wszystkie wybrane pliki i przesyłaj je pojedynczo
      for (const file of files) {
        console.log(`Przesyłanie pliku: ${file.name}`);
        const { data, error } = await supabase.storage
          .from("barbershopimages")
          .upload("images" + "/" + encodeURIComponent(file.name), file);

        if (data) {
          continue;
        } else {
          console.error(`Błąd przesyłania pliku ${file.name}:`, error);
        }
      }

      // resetowanie pola wyboru pliku
      inputElement.value = null;

      // Odśwież listę zdjęć
      getImages();
    } catch (error) {
      console.error("Błąd przesyłania pliku:", error.message);
    }
  };

  const handleDeleteImage = async (selectedImage) => {
    try {
      const { error } = await supabase.storage
        .from("barbershopimages")
        .remove([`images/${selectedImage.name}`]);

      if (error) {
        console.error("Error removing image:", error.message);
        return;
      }
      getImages();
    } catch (error) {
      console.error("Error deleting image:", error.message);
    }
  };

  useEffect(() => {
    getImages();
  }, []);

  return (
    <>
      <h2 className="text-center mt-4">Ustawienia Galerii</h2>
      <Container className=" mt-5 d-flex flex-column align-items-center">
        <Form className="mb-3 d-flex align-items-center">
          <Form.Group controlId="formFile" className="mb-3 me-2">
            <Form.Control
              type="file"
              accept="image/png, image/jpeg, image/jpg"
              multiple
            />
          </Form.Group>
          <Button
            className="mb-3"
            variant="primary"
            onClick={(e) => handleUploadImage(e)}
          >
            <FontAwesomeIcon icon={faPlus} style={{ color: "white" }} />
          </Button>
        </Form>
      </Container>
      <Container className="text-center mt-4">
        {images.length > 0 ? (
          <div className="table-responsive mx-auto" style={{ maxWidth: "50%" }}>
            <Table bordered hover size="sm">
              <thead className="thead-dark">
                <tr>
                  <th scope="col">Nazwa zdjęcia</th>
                  <th scope="col">Akcja</th>
                </tr>
              </thead>
              <tbody>
                {images.map((image) => (
                  <tr key={image.name} style={{ cursor: "pointer" }}>
                    <td onClick={() => handleImageClick(image)}>
                      {image.name}
                    </td>
                    <td>
                      <button
                        className="btn btn-danger btn-sm"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDeleteImage(image);
                        }}
                      >
                        <FontAwesomeIcon icon={faTrashAlt} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </div>
        ) : (
          <h5 className="mt-5">Brak zdjęć</h5>
        )}

        <Modal show={showModal} onHide={handleCloseModal}>
          <Modal.Header closeButton>
            <Modal.Title style={{ textAlign: "center", width: "100%" }}>
              {selectedImage.name &&
                selectedImage.name.slice(
                  0,
                  selectedImage.name.lastIndexOf(".")
                )}
            </Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <img
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
    </>
  );
};

export default GallerySettings;
