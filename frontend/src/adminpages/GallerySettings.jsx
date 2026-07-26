import { useState, memo, useCallback } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useSupabaseClient, CDNURL } from "../api/supabaseApi";
import { Container, Table, Modal, Button, Form } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPlus, faTrashAlt } from "@fortawesome/free-solid-svg-icons";
import { toast } from "sonner";
import useTableData from "../hooks/useTableData";
import useDeleteModal from "../hooks/useDeleteModal";
import SearchBox from "../components/common/SearchBox";
import PaginationControl from "../components/common/PaginationControl";
import ConfirmDeleteModal from "../components/common/ConfirmDeleteModal";
import ButtonSpinner from "../components/common/ButtonSpinner";
import { useTranslation } from "react-i18next";

const imageFieldsHeaders = ["admin.gallery.imageName"];
const imageFields = ["name"];

const ImageRow = memo(function ImageRow({
  image,
  onImageClick,
  onDelete,
  isDeleting,
}) {
  const { t } = useTranslation();
  return (
    <tr key={image.name} style={{ cursor: "pointer" }}>
      {imageFields.map((field) => (
        <td
          key={field}
          onClick={() => onImageClick(image)}
          title={t("admin.gallery.showPreview")}
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
          title={t("admin.gallery.deleteImage")}
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
  const [showModal, setShowModal] = useState(false);
  const [selectedImage, setSelectedImage] = useState("");
  const supabase = useSupabaseClient();
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  // fetch the image list from supabase storage (filtered by file type)
  const { data: images = [] } = useQuery({
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
        throw error;
      }

      if (!data) return [];

      // filter out unwanted files (non-images and placeholder files)
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

  const performDeleteImage = useCallback(
    async (image) => {
      const { error } = await supabase.storage
        .from("barbershopimages")
        .remove([`images/${image.name}`]);

      if (error) {
        toast.error(t("admin.gallery.messages.deleteError"));
        console.error("error removing image:", error.message);
        throw error;
      }

      toast.success(t("admin.gallery.messages.deleteSuccess"));
    },
    [supabase, t],
  );

  const {
    show: showDeleteModal,
    setShow: setShowDeleteModal,
    itemToDelete: imageToDelete,
    askDelete: handleAskDeleteImage,
    confirmDelete: confirmDeleteImage,
    isDeleting,
  } = useDeleteModal(performDeleteImage, () =>
    queryClient.invalidateQueries({ queryKey: ["galleryImages"] }),
  );

  const handleImageClick = (image) => {
    setSelectedImage(image);
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
  };

  // upload images sequentially, handle ui feedback and reset file input
  const uploadImageMutation = useMutation({
    mutationFn: async (files) => {
      for (const file of files) {
        const { data, error: uploadError } = await supabase.storage
          .from("barbershopimages")
          .upload("images/" + encodeURIComponent(file.name), file);

        if (uploadError || !data) {
          toast.error(
            t("admin.gallery.messages.uploadErrorFile", { name: file.name }),
          );

          if (uploadError) {
            console.error("error uploading image:", uploadError.message);
          }

          throw new Error(uploadError?.message || "upload failed");
        }
      }
    },
    onSuccess: () => {
      toast.success(t("admin.gallery.messages.uploadSuccess"));
      const inputElement = document.getElementById("formFile");
      if (inputElement) inputElement.value = null;

      queryClient.invalidateQueries({ queryKey: ["galleryImages"] });
    },
    onError: (error) => {
      toast.error(t("admin.gallery.messages.uploadError"));
      console.error("file upload error:", error.message);
    },
  });

  const handleUploadImage = (e) => {
    e.preventDefault();
    const inputElement = document.getElementById("formFile");
    const files = inputElement?.files;

    if (!files || files.length === 0) {
      toast.error(t("admin.gallery.messages.noFileSelected"));
      return;
    }

    uploadImageMutation.mutate(Array.from(files));
  };

  return (
    <>
      <h2 className="text-center mt-4">{t("admin.gallery.title")}</h2>

      {/* image upload form */}
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
            title={t("admin.gallery.uploadBtnTooltip")}
            style={{ minWidth: "40px" }}
            disabled={uploadImageMutation.isPending}
          >
            <FontAwesomeIcon icon={faPlus} style={{ color: "white" }} />
          </Button>
        </Form>
      </Container>

      {/* table with images and their actions */}
      <Container className="text-center mt-4">
        {/* search box */}
        <SearchBox
          value={searchTerm}
          onChange={handleSearchChange}
          placeholder={t("admin.gallery.searchPlaceholder")}
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
                      {t(header)}
                    </th>
                  ))}
                  <th scope="col" className="text-center align-middle">
                    {t("admin.common.action")}
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
          <h5 className="mt-5">{t("admin.gallery.noImagesFound")}</h5>
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
                  selectedImage.name.lastIndexOf("."),
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
              {t("common.close")}
            </Button>
          </Modal.Footer>
        </Modal>

        {/* delete confirmation modal */}
        <ConfirmDeleteModal
          show={showDeleteModal}
          onHide={() => setShowDeleteModal(false)}
          onConfirm={confirmDeleteImage}
          itemName={imageToDelete?.name}
          label={t("admin.gallery.deleteLabel")}
        />
      </Container>
    </>
  );
};

export default GallerySettings;
