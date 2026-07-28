CREATE TABLE audit_logs (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
actor_email VARCHAR(255) NOT NULL,
action VARCHAR(100) NOT NULL,
entity_type VARCHAR(50) NOT NULL,
entity_id VARCHAR(100),
-- optional JSON payload storing contextual details about the event (e.g. old vs new values)
details JSON
);

CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp DESC);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_email);