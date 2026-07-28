import { useState, useEffect, useCallback, useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { useSupabaseClient, CDNURL } from "../api/supabaseApi";
import { Container, Modal, Button } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faChevronLeft,
  faChevronRight,
  faXmark,
  faImages,
  faExpand,
  faTriangleExclamation,
} from "@fortawesome/free-solid-svg-icons";
import LoadingSpinner from "../components/common/LoadingSpinner";
import { useTranslation } from "react-i18next";
import "./styles/Gallery.css";

const IMAGE_EXTENSIONS = [".png", ".jpeg", ".jpg", ".webp"];

/**
 * turns a raw storage filename into a readable caption
 * e.g. "fade_haircut-03.jpg" --> "Fade Haircut 03"
 */
const formatImageLabel = (fileName) => {
  const withoutExtension = fileName.slice(0, fileName.lastIndexOf("."));
  return withoutExtension
    .replace(/[_-]+/g, " ")
    .trim()
    .replace(/\b\w/g, (char) => char.toUpperCase());
};

const Gallery = () => {
  const [lightboxIndex, setLightboxIndex] = useState(null);
  const [loadedThumbnails, setLoadedThumbnails] = useState(() => new Set());
  const [erroredThumbnails, setErroredThumbnails] = useState(() => new Set());
  const supabase = useSupabaseClient();
  const { t } = useTranslation();

  const {
    data: images = [],
    isLoading,
    isError,
    refetch,
  } = useQuery({
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
          IMAGE_EXTENSIONS.some((ext) => lowercasedName.endsWith(ext))
        );
      });
    },
    enabled: !!supabase,
  });

  const imageUrls = useMemo(
    () => images.map((image) => `${CDNURL}images/${image.name}`),
    [images],
  );

  const isLightboxOpen = lightboxIndex !== null;
  const activeImage = isLightboxOpen ? images[lightboxIndex] : null;

  const openLightbox = useCallback((index) => setLightboxIndex(index), []);
  const closeLightbox = useCallback(() => setLightboxIndex(null), []);

  const showPrevious = useCallback(() => {
    setLightboxIndex((current) =>
      current === null
        ? current
        : (current - 1 + images.length) % images.length,
    );
  }, [images.length]);

  const showNext = useCallback(() => {
    setLightboxIndex((current) =>
      current === null ? current : (current + 1) % images.length,
    );
  }, [images.length]);

  const markThumbnailLoaded = useCallback((name) => {
    setLoadedThumbnails((prev) => {
      if (prev.has(name)) return prev;
      const next = new Set(prev);
      next.add(name);
      return next;
    });
  }, []);

  const markThumbnailErrored = useCallback((name) => {
    setErroredThumbnails((prev) => {
      if (prev.has(name)) return prev;
      const next = new Set(prev);
      next.add(name);
      return next;
    });
  }, []);

  // arrow-key navigation while the lightbox is open
  useEffect(() => {
    if (!isLightboxOpen) return undefined;

    const handleKeyDown = (event) => {
      if (event.key === "ArrowLeft") showPrevious();
      if (event.key === "ArrowRight") showNext();
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isLightboxOpen, showPrevious, showNext]);

  const renderGalleryContent = () => {
    if (isLoading) {
      return <LoadingSpinner text={t("gallery.loading")} />;
    }

    if (isError) {
      return (
        <div className="gallery-state gallery-state--error">
          <FontAwesomeIcon
            icon={faTriangleExclamation}
            className="gallery-state__icon"
          />
          <p className="gallery-state__text">
            {t("gallery.error", "Nie udało się wczytać galerii.")}
          </p>
          <Button
            variant="outline-secondary"
            size="sm"
            onClick={() => refetch()}
          >
            {t("gallery.retry", "Spróbuj ponownie")}
          </Button>
        </div>
      );
    }

    if (images.length === 0) {
      return (
        <div className="gallery-state gallery-state--empty">
          <FontAwesomeIcon icon={faImages} className="gallery-state__icon" />
          <p className="gallery-state__text">
            {t("gallery.empty", "Galeria jest jeszcze pusta.")}
          </p>
        </div>
      );
    }

    return (
      <div className="gallery-grid">
        {images.map((image, index) => {
          const label = formatImageLabel(image.name);
          const isLoaded = loadedThumbnails.has(image.name);
          const hasError = erroredThumbnails.has(image.name);

          return (
            <button
              type="button"
              key={image.name}
              className={`gallery-tile${
                isLoaded || hasError ? " is-loaded" : ""
              }${hasError ? " is-broken" : ""}`}
              onClick={() => openLightbox(index)}
              aria-label={`${t("gallery.view", "Zobacz zdjęcie")}: ${label}`}
            >
              {hasError ? (
                <span className="gallery-tile__fallback">
                  <FontAwesomeIcon icon={faImages} />
                </span>
              ) : (
                <img
                  src={imageUrls[index]}
                  alt={label}
                  loading="lazy"
                  className="gallery-tile__image"
                  onLoad={() => markThumbnailLoaded(image.name)}
                  onError={() => markThumbnailErrored(image.name)}
                />
              )}
              <span className="gallery-tile__overlay">
                <FontAwesomeIcon
                  icon={faExpand}
                  className="gallery-tile__icon"
                />
                <span className="gallery-tile__label">{label}</span>
              </span>
            </button>
          );
        })}
      </div>
    );
  };

  return (
    <Container className="gallery text-center mt-4">
      <h2 className="display-6">{t("gallery.title")}</h2>
      <p className="lead">{t("gallery.lead")}</p>

      {renderGalleryContent()}

      <Modal
        show={isLightboxOpen}
        onHide={closeLightbox}
        centered
        size="lg"
        className="gallery-lightbox"
      >
        <Modal.Body className="gallery-lightbox__body">
          <button
            type="button"
            className="gallery-lightbox__close"
            onClick={closeLightbox}
            aria-label={t("common.close")}
          >
            <FontAwesomeIcon icon={faXmark} />
          </button>

          {images.length > 1 && (
            <button
              type="button"
              className="gallery-lightbox__nav gallery-lightbox__nav--prev"
              onClick={showPrevious}
              aria-label={t("gallery.previous", "Poprzednie zdjęcie")}
            >
              <FontAwesomeIcon icon={faChevronLeft} />
            </button>
          )}

          {activeImage && (
            <figure className="gallery-lightbox__figure">
              <img
                src={`${CDNURL}images/${activeImage.name}`}
                alt={formatImageLabel(activeImage.name)}
                className="gallery-lightbox__image"
              />
              <figcaption className="gallery-lightbox__caption">
                <span>{formatImageLabel(activeImage.name)}</span>
                <span className="gallery-lightbox__count">
                  {lightboxIndex + 1} / {images.length}
                </span>
              </figcaption>
            </figure>
          )}

          {images.length > 1 && (
            <button
              type="button"
              className="gallery-lightbox__nav gallery-lightbox__nav--next"
              onClick={showNext}
              aria-label={t("gallery.next", "Następne zdjęcie")}
            >
              <FontAwesomeIcon icon={faChevronRight} />
            </button>
          )}
        </Modal.Body>
      </Modal>
    </Container>
  );
};

export default Gallery;
