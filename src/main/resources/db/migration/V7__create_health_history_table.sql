CREATE TABLE ativo_health_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ativo_id BIGINT NOT NULL,
    data_registro DATETIME NOT NULL,
    componente VARCHAR(255) NOT NULL,
    valor DOUBLE NOT NULL,
    metrica VARCHAR(255) NOT NULL,
    CONSTRAINT fk_health_history_ativo FOREIGN KEY (ativo_id) REFERENCES ativos(id) ON DELETE CASCADE
);

CREATE INDEX idx_health_history_ativo_metrica ON ativo_health_history(ativo_id, metrica);
