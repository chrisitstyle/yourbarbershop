import { Modal, Button } from "react-bootstrap";
import { useTranslation } from "react-i18next";

const ConfirmDeleteModal = ({
  show,
  onHide,
  onConfirm,
  itemName,
  label = "usługę",
}) => {
  const { t } = useTranslation();

  return (
    <Modal show={show} onHide={onHide} centered>
      <Modal.Header closeButton>
        <Modal.Title>{t("admin.deleteModal.title")}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        {t("admin.deleteModal.message", { label: label, itemName: itemName })}
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={onHide}>
          {t("admin.deleteModal.cancel")}
        </Button>
        <Button variant="danger" onClick={onConfirm}>
          {t("admin.common.delete")}
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default ConfirmDeleteModal;
