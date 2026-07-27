import { useState, useCallback, useRef, useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useSupabaseClient, CDNURL } from "../api/supabaseApi";
import { Container, Modal, Button } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faTrashAlt,
  faCloudArrowUp,
  faXmark,
  faImage,
} from "@fortawesome/free-solid-svg-icons";
import { toast } from "sonner";
import useTableData from "../hooks/useTableData";
import useDeleteModal from "../hooks/useDeleteModal";
import SearchBox from "../components/common/SearchBox";
import PaginationControl from "../components/common/PaginationControl";
import ConfirmDeleteModal from "../components/common/ConfirmDeleteModal";
import ButtonSpinner from "../components/common/ButtonSpinner";
import { useTranslation } from "react-i18next";
import "./styles/GallerySettings.css";

const GallerySettings = () => {
  const [showModal, setShowModal] = useState(false);
  const [selectedImage, setSelectedImage] = useState("");
  const [pendingFiles, setPendingFiles] = useState([]); // files chosen but not yet uploaded
  const [isDragging, setIsDragging] = useState(false);
  const fileInputRef = useRef(null);
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

  // revoke object urls on unmount to prevent memory leaks
  useEffect(() => {
    return () => {
      pendingFiles.forEach((file) => {
        if (file.previewUrl) URL.revokeObjectURL(file.previewUrl);
      });
    };
  }, [pendingFiles]);

  // upload images sequentially, handle ui feedback and reset pending files
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
      // revoke object urls to free memory before clearing pending files
      pendingFiles.forEach((file) => {
        if (file.previewUrl) URL.revokeObjectURL(file.previewUrl);
      });
      setPendingFiles([]);
      if (fileInputRef.current) fileInputRef.current.value = null;
      queryClient.invalidateQueries({ queryKey: ["galleryImages"] });
    },
    onError: (error) => {
      toast.error(t("admin.gallery.messages.uploadError"));
      console.error("file upload error:", error.message);
    },
  });

  // accept only supported image types
  const isValidImage = (file) =>
    ["image/png", "image/jpeg", "image/jpg"].includes(file.type);

  // add newly chosen files to the pending preview list (from input or drop)
  const addFiles = (fileList) => {
    const valid = Array.from(fileList).filter(isValidImage);
    if (valid.length === 0) {
      toast.error(t("admin.gallery.messages.noFileSelected"));
      return;
    }
    // create and attach preview url to file object for easy rendering and revocation
    const filesWithPreview = valid.map((file) =>
      Object.assign(file, { previewUrl: URL.createObjectURL(file) }),
    );
    setPendingFiles((prev) => [...prev, ...filesWithPreview]);
  };

  const handleFileInput = (e) => addFiles(e.target.files);

  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files?.length) addFiles(e.dataTransfer.files);
  };

  const removePendingFile = (index) => {
    setPendingFiles((prev) => {
      const fileToRemove = prev[index];
      if (fileToRemove?.previewUrl) {
        URL.revokeObjectURL(fileToRemove.previewUrl);
      }
      return prev.filter((_, i) => i !== index);
    });
  };

  const handleUploadImage = () => {
    if (pendingFiles.length === 0) {
      toast.error(t("admin.gallery.messages.noFileSelected"));
      return;
    }
    uploadImageMutation.mutate(pendingFiles);
  };

  return (
    <>
      <h2 className="text-center mt-4">{t("admin.gallery.title")}</h2>

      {/* drag & drop upload zone with live preview of pending files */}
      <Container className="mt-4" style={{ maxWidth: "720px" }}>
        <div
          className={`gs-dropzone${isDragging ? " gs-dropzone--active" : ""}`}
          onClick={() => fileInputRef.current?.click()}
          onDragOver={(e) => {
            e.preventDefault();
            setIsDragging(true);
          }}
          onDragLeave={() => setIsDragging(false)}
          onDrop={handleDrop}
          role="button"
          tabIndex={0}
          onKeyDown={(e) => {
            if (e.key === "Enter" || e.key === " ")
              fileInputRef.current?.click();
          }}
        >
          <FontAwesomeIcon
            icon={faCloudArrowUp}
            className="gs-dropzone__icon"
          />
          <p className="mb-0 fw-semibold">
            {t(
              "admin.gallery.dropzoneTitle",
              "Przeciągnij zdjęcia lub kliknij, aby wybrać",
            )}
          </p>
          <small className="text-muted">PNG, JPG, JPEG</small>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/png, image/jpeg, image/jpg"
            multiple
            hidden
            onChange={handleFileInput}
          />
        </div>

        {/* thumbnails of files queued for upload */}
        {pendingFiles.length > 0 && (
          <div className="gs-pending mt-3">
            <div className="gs-pending__grid">
              {pendingFiles.map((file, i) => (
                <div className="gs-pending__item" key={`${file.name}-${i}`}>
                  <img src={file.previewUrl} alt={file.name} />
                  <button
                    type="button"
                    className="gs-pending__remove"
                    title={t("common.close")}
                    onClick={() => removePendingFile(i)}
                  >
                    <FontAwesomeIcon icon={faXmark} />
                  </button>
                </div>
              ))}
            </div>
            <ButtonSpinner
              variant="primary"
              className="mt-3"
              onClick={handleUploadImage}
              loading={uploadImageMutation.isPending}
              loadingText={t("admin.gallery.uploading")}
            >
              <FontAwesomeIcon icon={faCloudArrowUp} className="me-2" />
              {t("admin.gallery.uploadBtn")} ({pendingFiles.length})
            </ButtonSpinner>
          </div>
        )}
      </Container>

      {/* existing images as a thumbnail grid with hover delete */}
      <Container className="text-center mt-5">
        <SearchBox
          value={searchTerm}
          onChange={handleSearchChange}
          placeholder={t("admin.gallery.searchPlaceholder")}
        />

        {currentData.length > 0 ? (
          <div className="gs-grid mt-4">
            {currentData.map((image) => (
              <figure className="gs-card" key={image.name}>
                <button
                  type="button"
                  className="gs-card__btn"
                  title={t("admin.gallery.showPreview")}
                  onClick={() => handleImageClick(image)}
                >
                  <img
                    src={CDNURL + "images/" + image.name}
                    alt={image.name}
                    loading="lazy"
                  />
                </button>
                <figcaption className="gs-card__name" title={image.name}>
                  {image.name}
                </figcaption>
                <ButtonSpinner
                  variant="danger"
                  size="sm"
                  className="gs-card__delete"
                  title={t("admin.gallery.deleteImage")}
                  onClick={(e) => {
                    e.stopPropagation();
                    handleAskDeleteImage(image);
                  }}
                  loading={isDeleting && imageToDelete?.name === image.name}
                  loadingText=""
                >
                  <FontAwesomeIcon icon={faTrashAlt} />
                </ButtonSpinner>
              </figure>
            ))}
          </div>
        ) : (
          <div className="gs-empty mt-5">
            <FontAwesomeIcon icon={faImage} className="gs-empty__icon" />
            <h5 className="mt-3">{t("admin.gallery.noImagesFound")}</h5>
          </div>
        )}

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
