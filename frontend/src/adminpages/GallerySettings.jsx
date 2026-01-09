import { useEffect, useState, useMemo, memo } from "react";
import { useSupabaseClient } from "../api/supabaseApi";
import { Container, Table, Modal, Button, Form } from "react-bootstrap";
import { CDNURL } from "../api/supabaseApi";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPlus, faTrashAlt } from "@fortawesome/free-solid-svg-icons";
import { Alert } from "react-bootstrap";
import ConfirmDeleteModal from "../components/common/ConfirmDeleteModal";

const imageFieldsHeaders = ["Nazwa obrazu"];
const imageFields = ["name"];

const ImageRow = memo(function ImageRow({
  image,
  onImageClick,
  onDelete,
  deleteLoading,
  imageToDelete,
}) {
  return (
    <tr key={image.name} style={{ cursor: "pointer" }}>
      {imageFields.map((field) => (
        <td
          key={field}
          onClick={() => onImageClick(image)}
          title="Pokaż podgląd obrazu"
          className="align-middle text-center"
        >
          {image[field]}
        </td>
      ))}
      <td className="align-middle text-center">
        {/* delete button with tooltip */}
        <button
          className="btn btn-danger btn-sm"
          title="Usuń obraz"
          style={{ minWidth: "38px" }}
          onClick={(e) => {
            e.stopPropagation();
            onDelete(image);
          }}
          disabled={deleteLoading}
        >
          {deleteLoading && imageToDelete?.name === image.name ? (
            "Usuwanie..."
          ) : (
            <FontAwesomeIcon icon={faTrashAlt} />
          )}
        </button>
      </td>
    </tr>
  );
});

const GallerySettings = () => {
  const [images, setImages] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [selectedImage, setSelectedImage] = useState("");
  const supabase = useSupabaseClient();
  const [deleteLoading, setDeleteLoading] = useState(false);

  // modal state for confirming delete
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [imageToDelete, setImageToDelete] = useState(null);

  // messages for different scenarios
  const [deleteImageErrorMsg, setDeleteImageErrorMsg] = useState(null);
  const [uploadImageSuccessfulMsg, setUploadImageSuccessfulMsg] =
    useState(null);
  const [uploadImageErrorMsg, setUploadImageErrorMsg] = useState(null);
  const [uploadingImageMsg, setUploadingImageMsg] = useState(null);
  const [uploadingImageTimeout, setUploadingImageTimeout] = useState(null);

  const handleImageClick = (image) => {
    setSelectedImage(image);
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
  };

  // fetch the image list from Supabase storage (filtered by file type)
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
        // filter out unwanted files (non-images and placeholder files)
        const filteredImages = data.filter((image) => {
          const lowercasedName = image.name.toLowerCase();
          return (
            !lowercasedName.includes(".emptyfolderplaceholder") &&
            (lowercasedName.endsWith(".png") ||
              lowercasedName.endsWith(".jpeg") ||
              lowercasedName.endsWith(".jpg"))
          );
        });
        setImages(filteredImages);
      } else {
        console.error("Data is null, error:", error);
      }
    } catch (error) {
      console.error("Error fetching images:", error.message);
    }
  }

  // upload images sequentially, handle UI feedback and reset file input
  const handleUploadImage = async (e) => {
    e.preventDefault();
    const inputElement = document.getElementById("formFile");
    const files = inputElement.files;

    if (!files || files.length === 0) {
      setUploadImageErrorMsg("Nie wybrano pliku");
      return;
    }

    let uploadSuccessful = true;

    try {
      // upload each file, show status message
      for (const file of files) {
        setUploadingImageMsg(`Przesyłanie pliku ${file.name}...`);
        const { data, error } = await supabase.storage
          .from("barbershopimages")
          .upload("images/" + encodeURIComponent(file.name), file);

        if (!data) {
          uploadSuccessful = false;
          setUploadImageErrorMsg(`Błąd podczas przesyłania pliku ${file.name}`);
          break;
        }
      }
      if (uploadSuccessful) {
        setUploadImageSuccessfulMsg("Pomyślnie przesłano pliki!");
        inputElement.value = null;
        setUploadingImageMsg(null);

        const timeoutID = setTimeout(() => {
          setUploadImageSuccessfulMsg(null);
        }, 5000);
        setUploadingImageTimeout(timeoutID);
        getImages();
      }
    } catch (error) {
      setUploadImageSuccessfulMsg(null);
      setUploadImageErrorMsg("Wystąpił błąd podczas przesyłania pliku");
      console.error("File upload error:", error.message);
    }
  };

  const handleAskDeleteImage = (image) => {
    setImageToDelete(image);
    setShowDeleteModal(true);
  };

  const confirmDeleteImage = async () => {
    if (!imageToDelete) return;
    setDeleteLoading(true);
    try {
      const { error } = await supabase.storage
        .from("barbershopimages")
        .remove([`images/${imageToDelete.name}`]);
      if (error) {
        setDeleteImageErrorMsg("Usuwanie obrazu nie powiodło się");
        console.error("Error removing image:", error.message);
      } else {
        getImages();
      }
    } catch (error) {
      setDeleteImageErrorMsg("Usuwanie obrazu nie powiodło się");
      console.error("Error deleting image:", error.message);
    }
    setDeleteLoading(false);
    setShowDeleteModal(false);
    setImageToDelete(null);
  };

  useEffect(() => {
    getImages();
  }, []);

  // cleanup any timeouts left from upload feedback
  useEffect(() => {
    return () => {
      clearTimeout(uploadingImageTimeout);
    };
  }, [uploadingImageTimeout]);

  const memoizedImages = useMemo(() => images, [images]);

  return (
    <>
      <h2 className="text-center mt-4">Ustawienia galerii</h2>
      {/* feedback during upload */}
      {uploadingImageMsg && (
        <Alert
          variant="info"
          onClose={() => setUploadingImageMsg(null)}
          dismissible
          className="text-center"
        >
          {uploadingImageMsg}
        </Alert>
      )}

      {/* image upload Form */}
      <Container className="mt-5 d-flex flex-column align-items-center">
        <Form className="mb-3 d-flex align-items-center">
          {/* file input (multiple files) */}
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
            onClick={handleUploadImage}
            title="Wgraj obraz(y)"
            style={{ minWidth: "40px" }}
          >
            <FontAwesomeIcon icon={faPlus} style={{ color: "white" }} />
          </Button>
        </Form>
      </Container>

      {/* success feedback */}
      {uploadImageSuccessfulMsg && (
        <Alert
          variant="success"
          onClose={() => setUploadImageSuccessfulMsg(null)}
          dismissible
          className="text-center"
        >
          {uploadImageSuccessfulMsg}
        </Alert>
      )}
      {/* error feedback */}
      {uploadImageErrorMsg && (
        <Alert
          variant="danger"
          onClose={() => setUploadImageErrorMsg(null)}
          dismissible
          className="text-center"
        >
          {uploadImageErrorMsg}
        </Alert>
      )}
      {/* feedback on delete error */}
      {deleteImageErrorMsg && (
        <Alert
          variant="danger"
          onClose={() => setDeleteImageErrorMsg(null)}
          dismissible
          className="text-center"
        >
          {deleteImageErrorMsg}
        </Alert>
      )}

      {/* table with images and their actions */}
      <Container className="text-center mt-4">
        {memoizedImages.length > 0 ? (
          <div
            className="table-responsive mx-auto"
            style={{ maxWidth: "500px" }}
          >
            <Table bordered hover size="sm" className="shadow rounded">
              <thead className="table-dark">
                <tr>
                  {imageFieldsHeaders.map((header, idx) => (
                    <th
                      key={imageFields[idx]}
                      scope="col"
                      className="text-center align-middle"
                    >
                      {header}
                    </th>
                  ))}
                  <th scope="col" className="text-center align-middle">
                    Akcja
                  </th>
                </tr>
              </thead>
              <tbody>
                {memoizedImages.map((image) => (
                  <ImageRow
                    key={image.name}
                    image={image}
                    onImageClick={handleImageClick}
                    onDelete={handleAskDeleteImage}
                    deleteLoading={deleteLoading}
                    imageToDelete={imageToDelete}
                  />
                ))}
              </tbody>
            </Table>
          </div>
        ) : (
          <h5 className="mt-5">Nie znaleziono zdjęć</h5>
        )}

        {/* modal for image preview */}
        <Modal show={showModal} onHide={handleCloseModal} centered>
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
            {selectedImage.name && (
              <img
                src={CDNURL + "images/" + selectedImage.name}
                alt={selectedImage.name}
                style={{ width: "100%", borderRadius: "6px" }}
              />
            )}
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={handleCloseModal}>
              Zamknij
            </Button>
          </Modal.Footer>
        </Modal>
        <ConfirmDeleteModal
          show={showDeleteModal}
          onHide={() => setShowDeleteModal(false)}
          onConfirm={confirmDeleteImage}
          itemName={imageToDelete?.name}
          label="obraz"
        />
      </Container>
    </>
  );
};

export default GallerySettings;
