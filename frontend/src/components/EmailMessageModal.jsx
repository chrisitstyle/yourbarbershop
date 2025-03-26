import React from "react";
import { Modal, Button, Form } from "react-bootstrap";

const EmailMessageModal = ({
  show,
  handleClose,
  emailTo,
  emailSubject,
  setEmailSubject,
  emailMessage,
  setEmailMessage,
  handleEmailSend,
  resetEmailFields,
}) => {
  return (
    <Modal show={show} onHide={handleClose}>
      <Modal.Header closeButton>
        <Modal.Title>Wyślij e-mail</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form>
          <Form.Group controlId="formEmailTo">
            <Form.Label>Do</Form.Label>
            <Form.Control type="email" value={emailTo} readOnly />
          </Form.Group>
          <Form.Group controlId="formEmailSubject" className="mt-3">
            <Form.Label>Temat</Form.Label>
            <Form.Control
              type="text"
              placeholder="Temat"
              required
              value={emailSubject}
              onChange={(e) => setEmailSubject(e.target.value)}
            />
          </Form.Group>
          <Form.Group controlId="formEmailMessage" className="mt-3">
            <Form.Label>Wiadomość</Form.Label>
            <Form.Control
              as="textarea"
              placeholder="Wiadomość"
              rows={3}
              value={emailMessage}
              required
              onChange={(e) => setEmailMessage(e.target.value)}
            />
          </Form.Group>
        </Form>
      </Modal.Body>
      <Modal.Footer>
        <Button
          variant="secondary"
          onClick={() => {
            handleClose();
            resetEmailFields();
          }}
        >
          Zamknij
        </Button>
        <Button variant="primary" onClick={handleEmailSend}>
          Wyślij
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default EmailMessageModal;
