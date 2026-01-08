import { Modal, Button } from "react-bootstrap";

const ConfirmDeleteModal = ({
  show,
  onHide,
  onConfirm,
  itemName,
  label = "usługę",
}) => {
  return (
    <Modal show={show} onHide={onHide} centered>
      <Modal.Header closeButton>
        <Modal.Title>Potwierdzenie usunięcia</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        Czy na pewno chcesz usunąć {label}: <strong>{itemName}</strong>?
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={onHide}>
          Anuluj
        </Button>
        <Button variant="danger" onClick={onConfirm}>
          Usuń
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default ConfirmDeleteModal;
