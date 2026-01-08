import { Modal, Button, Form, OverlayTrigger, Tooltip } from "react-bootstrap";

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
  const isFormValid = emailSubject.trim() && emailMessage.trim();

  const renderTooltip = (props) => (
    <Tooltip id="tooltip-email-to" {...props}>
      Adres e-mail odbiorcy, nie można edytować
    </Tooltip>
  );

  return (
    <Modal
      show={show}
      onHide={() => {
        handleClose();
        resetEmailFields();
      }}
      centered
      dialogClassName="w-100"
      contentClassName="border-0"
      keyboard={true}
    >
      <Modal.Header closeButton>
        <Modal.Title>Wyślij e-mail</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form>
          <Form.Group controlId="formEmailTo">
            <Form.Label>
              <OverlayTrigger placement="right" overlay={renderTooltip}>
                <span>Do</span>
              </OverlayTrigger>
            </Form.Label>
            <Form.Control
              type="email"
              value={emailTo}
              readOnly
              tabIndex={-1}
              style={{ backgroundColor: "#f8f9fa" }}
            />
          </Form.Group>
          <Form.Group controlId="formEmailSubject" className="mt-3">
            <Form.Label>
              Temat <span className="text-danger">*</span>
            </Form.Label>
            <Form.Control
              type="text"
              placeholder="Temat"
              required
              autoFocus
              maxLength={120}
              value={emailSubject}
              onChange={(e) => setEmailSubject(e.target.value)}
            />
          </Form.Group>
          <Form.Group controlId="formEmailMessage" className="mt-3">
            <Form.Label>
              Wiadomość <span className="text-danger">*</span>
            </Form.Label>
            <Form.Control
              as="textarea"
              placeholder="Wiadomość"
              rows={6}
              style={{ minHeight: "120px", resize: "vertical" }}
              value={emailMessage}
              required
              maxLength={1200}
              onChange={(e) => setEmailMessage(e.target.value)}
            />
            <Form.Text muted>Maks. 1200 znaków</Form.Text>
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
        <Button
          variant="primary"
          onClick={handleEmailSend}
          disabled={!isFormValid}
        >
          Wyślij
        </Button>
      </Modal.Footer>
      <style>
        {`
          .modal-dialog {
            max-width: 430px;
            margin: 1.75rem auto;
          }
        `}
      </style>
    </Modal>
  );
};

export default EmailMessageModal;
