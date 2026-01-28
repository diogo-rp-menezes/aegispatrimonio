CREATE TABLE security_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME(6) NOT NULL,
    username VARCHAR(255) NOT NULL,
    resource VARCHAR(255),
    action VARCHAR(255),
    context VARCHAR(255),
    outcome VARCHAR(255) NOT NULL,
    details TEXT
);
