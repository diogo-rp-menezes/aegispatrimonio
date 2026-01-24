CREATE TABLE health_check_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME NOT NULL,
    host VARCHAR(128) NOT NULL,
    cpu_usage DECIMAL(5,4),
    mem_free_percent DECIMAL(5,4),
    disks TEXT,
    nets TEXT
);

CREATE INDEX idx_hch_created_at ON health_check_history(created_at);
CREATE INDEX idx_hch_host_created_at ON health_check_history(host, created_at);
