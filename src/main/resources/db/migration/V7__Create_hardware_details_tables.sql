-- V7: Criação das tabelas para detalhes de hardware e health check

-- Tabela principal para os detalhes de hardware, com relação 1-para-1 com ativos
CREATE TABLE ativo_detalhes_hardware (
    id BIGINT NOT NULL,
    computer_name VARCHAR(255),
    domain VARCHAR(255),
    os_name VARCHAR(255),
    os_version VARCHAR(255),
    motherboard_manufacturer VARCHAR(255),
    motherboard_model VARCHAR(255),
    motherboard_serial_number VARCHAR(255),
    cpu_model VARCHAR(255),
    cpu_architecture VARCHAR(255),
    cpu_cores INT,
    cpu_threads INT,
    atualizado_em DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_detalhe_hardware_on_ativo FOREIGN KEY (id) REFERENCES ativos (id)
);

-- Tabela para os discos de um ativo
CREATE TABLE discos (
    id BIGINT AUTO_INCREMENT NOT NULL,
    ativo_detalhe_hardware_id BIGINT NOT NULL,
    model VARCHAR(255),
    serial_number VARCHAR(255),
    type VARCHAR(255),
    total_gb DOUBLE,
    free_gb DOUBLE,
    free_percent DOUBLE,
    PRIMARY KEY (id),
    CONSTRAINT fk_disco_on_detalhe_hardware FOREIGN KEY (ativo_detalhe_hardware_id) REFERENCES ativo_detalhes_hardware (id)
);

-- Tabela para os módulos de memória de um ativo
CREATE TABLE memorias (
    id BIGINT AUTO_INCREMENT NOT NULL,
    ativo_detalhe_hardware_id BIGINT NOT NULL,
    size_gb INT,
    manufacturer VARCHAR(255),
    part_number VARCHAR(255),
    serial_number VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_memoria_on_detalhe_hardware FOREIGN KEY (ativo_detalhe_hardware_id) REFERENCES ativo_detalhes_hardware (id)
);

-- Tabela para os adaptadores de rede de um ativo
CREATE TABLE adaptadores_rede (
    id BIGINT AUTO_INCREMENT NOT NULL,
    ativo_detalhe_hardware_id BIGINT NOT NULL,
    description VARCHAR(255),
    mac_address VARCHAR(255),
    ip_address VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_adaptador_rede_on_detalhe_hardware FOREIGN KEY (ativo_detalhe_hardware_id) REFERENCES ativo_detalhes_hardware (id)
);
