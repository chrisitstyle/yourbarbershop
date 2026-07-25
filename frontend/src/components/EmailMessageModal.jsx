import { Modal, Button, Form, OverlayTrigger, Tooltip } from "react-bootstrap";
import { useTranslation } from "react-i18next";

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
  const { t } = useTranslation();
  const isFormValid = emailSubject.trim() && emailMessage.trim();

  const renderTooltip = (props) => (
    <Tooltip id="tooltip-email-to" {...props}>
      {t("admin.emailModal.recipientTooltip")}
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
        <Modal.Title>{t("admin.emailModal.title")}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form>
          <Form.Group controlId="formEmailTo">
            <Form.Label>
              <OverlayTrigger placement="right" overlay={renderTooltip}>
                <span>{t("admin.emailModal.to")}</span>
              </OverlayTrigger>
            </Form.Label>
            {/* dynamic bootstrap class for theme-compatible read-only input background */}
            <Form.Control
              type="email"
              value={emailTo}
              readOnly
              tabIndex={-1}
              className="bg-body-secondary"
            />
          </Form.Group>
          <Form.Group controlId="formEmailSubject" className="mt-3">
            <Form.Label>
              {t("admin.emailModal.subject")}{" "}
              <span className="text-danger">*</span>
            </Form.Label>
            <Form.Control
              type="text"
              placeholder={t("admin.emailModal.subjectPlaceholder")}
              required
              autoFocus
              maxLength={120}
              value={emailSubject}
              onChange={(e) => setEmailSubject(e.target.value)}
            />
          </Form.Group>
          <Form.Group controlId="formEmailMessage" className="mt-3">
            <Form.Label>
              {t("admin.emailModal.message")}{" "}
              <span className="text-danger">*</span>
            </Form.Label>
            <Form.Control
              as="textarea"
              placeholder={t("admin.emailModal.messagePlaceholder")}
              rows={6}
              style={{ minHeight: "120px", resize: "vertical" }}
              value={emailMessage}
              required
              maxLength={1200}
              onChange={(e) => setEmailMessage(e.target.value)}
            />
            <Form.Text muted>{t("admin.emailModal.maxChars")}</Form.Text>
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
          {t("common.close")}
        </Button>
        <Button
          variant="primary"
          onClick={handleEmailSend}
          disabled={!isFormValid}
        >
          {t("admin.emailModal.send")}
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
