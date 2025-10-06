-- Tabela principal para os detalhes de hardware, com relação 1-para-1 com ativos
CREATE TABLE ativo_detalhes_hardware (
    id BIGINT NOT NULL,
    computer_name VARCHAR(255),
    domain VARCHAR(255),
    os_name VARCHAR(255),
    os_version VARCHAR(255),
    os_architecture VARCHAR(255),
    motherboard_manufacturer VARCHAR(255),
    motherboard_model VARCHAR(255),
    motherboard_serial_number VARCHAR(255),
    cpu_model VARCHAR(255),
    cpu_cores INT,
    cpu_threads INT,
    last_updated DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_detalhes_hardware_on_ativo FOREIGN KEY (id) REFERENCES ativos (id)
);

-- Tabela para os discos, com relação N-para-1 com ativo_detalhes_hardware
CREATE TABLE discos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ativo_detalhe_hardware_id BIGINT NOT NULL,
    model VARCHAR(255),
    serial VARCHAR(255),
    type VARCHAR(255),
    total_gb DECIMAL(10, 2),
    free_gb DECIMAL(10, 2),
    free_percent INT,
    PRIMARY KEY (id),
    CONSTRAINT fk_discos_on_detalhes_hardware FOREIGN KEY (ativo_detalhe_hardware_id) REFERENCES ativo_detalhes_hardware (id)
);

-- Tabela para as memórias, com relação N-para-1 com ativo_detalhes_hardware
CREATE TABLE memorias (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ativo_detalhe_hardware_id BIGINT NOT NULL,
    manufacturer VARCHAR(255),
    serial_number VARCHAR(255),
    part_number VARCHAR(255),
    size_gb INT,
    PRIMARY KEY (id),
    CONSTRAINT fk_memorias_on_detalhes_hardware FOREIGN KEY (ativo_detalhe_hardware_id) REFERENCES ativo_detalhes_hardware (id)
);

-- Tabela para os adaptadores de rede, com relação N-para-1 com ativo_detalhes_hardware
CREATE TABLE adaptadores_rede (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ativo_detalhe_hardware_id BIGINT NOT NULL,
    description VARCHAR(255),
    mac_address VARCHAR(255),
    ip_addresses TEXT,
    PRIMARY KEY (id),
    CONSTRAINT fk_adaptadores_rede_on_detalhes_hardware FOREIGN KEY (ativo_detalhe_hardware_id) REFERENCES ativo_detalhes_hardware (id)
);
