import { useEffect, useState, memo } from "react";
import { useSupabaseClient, CDNURL } from "../api/supabaseApi";
import { Container, Table, Modal, Button, Form, Alert } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPlus, faTrashAlt } from "@fortawesome/free-solid-svg-icons";
import useTableData from "../hooks/useTableData";
import useDeleteModal from "../hooks/useDeleteModal";
import SearchBox from "../components/common/SearchBox";
import PaginationControl from "../components/common/PaginationControl";
import ConfirmDeleteModal from "../components/common/ConfirmDeleteModal";
import ButtonSpinner from "../components/common/ButtonSpinner";

const imageFieldsHeaders = ["Nazwa obrazu"];
const imageFields = ["name"];

const ImageRow = memo(function ImageRow({
  image,
  onImageClick,
  onDelete,
  isDeleting,
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
        <ButtonSpinner
          variant="danger"
          size="sm"
          title="Usuń obraz"
          style={{ minWidth: "38px" }}
          onClick={(e) => {
            e.stopPropagation();
            onDelete(image);
          }}
          loading={isDeleting}
          loadingText="" // no text during loading, just spinner because of small button size
        >
          <FontAwesomeIcon icon={faTrashAlt} />
        </ButtonSpinner>
      </td>
    </tr>
  );
});

const GallerySettings = () => {
  const [images, setImages] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [selectedImage, setSelectedImage] = useState("");
  const supabase = useSupabaseClient();

  // messages for different scenarios
  const [deleteImageErrorMsg, setDeleteImageErrorMsg] = useState(null);
  const [uploadImageSuccessfulMsg, setUploadImageSuccessfulMsg] =
    useState(null);
  const [uploadImageErrorMsg, setUploadImageErrorMsg] = useState(null);
  const [uploadingImageMsg, setUploadingImageMsg] = useState(null);
  const [uploadingImageTimeout, setUploadingImageTimeout] = useState(null);

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

  const filterImages = (image, term) => {
    return image.name.toLowerCase().includes(term.toLowerCase());
  };

  const {
    searchTerm,
    handleSearchChange,
    currentData,
    currentPage,
    totalPages,
    setCurrentPage,
  } = useTableData(images, filterImages);

  const performDeleteImage = async (image) => {
    const { error } = await supabase.storage
      .from("barbershopimages")
      .remove([`images/${image.name}`]);

    if (error) {
      setDeleteImageErrorMsg("Usuwanie obrazu nie powiodło się");
      console.error("Error removing image:", error.message);
      throw error;
    }
  };

  const {
    show: showDeleteModal,
    setShow: setShowDeleteModal,
    itemToDelete: imageToDelete,
    askDelete: handleAskDeleteImage,
    confirmDelete: confirmDeleteImage,
    isDeleting,
  } = useDeleteModal(performDeleteImage, getImages);

  const handleImageClick = (image) => {
    setSelectedImage(image);
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
  };

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

  useEffect(() => {
    getImages();
  }, []);

  // cleanup any timeouts left from upload feedback
  useEffect(() => {
    return () => {
      clearTimeout(uploadingImageTimeout);
    };
  }, [uploadingImageTimeout]);

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
        {/* search box */}
        <SearchBox
          value={searchTerm}
          onChange={handleSearchChange}
          placeholder="Szukaj obrazu..."
        />

        {currentData.length > 0 ? (
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
                {currentData.map((image) => (
                  <ImageRow
                    key={image.name}
                    image={image}
                    onImageClick={handleImageClick}
                    onDelete={handleAskDeleteImage}
                    isDeleting={
                      isDeleting && imageToDelete?.name === image.name
                    }
                  />
                ))}
              </tbody>
            </Table>
          </div>
        ) : (
          <h5 className="mt-5">Nie znaleziono zdjęć</h5>
        )}

        {/* pagination control */}
        <PaginationControl
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={setCurrentPage}
        />

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

        {/* delete confirmation modal */}
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
