-- V15: Criação da tabela de Alertas e sua tabela de auditoria (Envers).
-- Derivada fielmente da entidade Alerta.java (mapeamento JPA).
-- IF NOT EXISTS: guarda idempotente para produção, onde a tabela pode já existir
-- (criada historicamente pelo Hibernate ddl-auto=update). Em banco novo (Testcontainers)
-- a migration cria normalmente.

CREATE TABLE IF NOT EXISTS alertas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ativo_id BIGINT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    mensagem TEXT NOT NULL,
    data_criacao DATETIME(6) NOT NULL,
    lido BIT NOT NULL,
    data_leitura DATETIME(6),
    FOREIGN KEY (ativo_id) REFERENCES ativos(id)
);

CREATE INDEX idx_alertas_ativo_id ON alertas(ativo_id);

CREATE TABLE IF NOT EXISTS alertas_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype TINYINT,
    ativo_id BIGINT,
    tipo VARCHAR(50),
    titulo VARCHAR(255),
    mensagem TEXT,
    data_criacao DATETIME(6),
    lido BIT,
    data_leitura DATETIME(6),
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_alertas_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo (id)
);
