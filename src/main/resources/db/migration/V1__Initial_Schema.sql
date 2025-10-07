-- ======================================================================
-- Aegis Patrimonio - V1 Initial Schema
-- This script creates the entire database schema from scratch.
-- ======================================================================

-- Tabela para Filiais (Matriz/Filial)
CREATE TABLE filiais (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    codigo VARCHAR(10) NOT NULL UNIQUE,
    tipo VARCHAR(255) NOT NULL,
    cnpj VARCHAR(18) NOT NULL UNIQUE,
    endereco VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    criado_em DATETIME,
    atualizado_em DATETIME,
    PRIMARY KEY (id)
);

-- Tabela para Fornecedores (Global)
CREATE TABLE fornecedores (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    cnpj VARCHAR(18) NOT NULL UNIQUE,
    endereco VARCHAR(255),
    nome_contato_principal VARCHAR(255),
    email_principal VARCHAR(255),
    telefone_principal VARCHAR(50),
    observacoes TEXT,
    status VARCHAR(255) NOT NULL,
    criado_em DATETIME,
    atualizado_em DATETIME,
    PRIMARY KEY (id)
);

-- Tabela para Tipos de Ativo (ex: Desktop, Notebook, Cadeira)
CREATE TABLE tipos_ativo (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao VARCHAR(255),
    categoria_contabil VARCHAR(255) NOT NULL,
    icone VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    criado_em DATETIME,
    atualizado_em DATETIME,
    PRIMARY KEY (id)
);

-- Tabela para Departamentos (vinculados a uma Filial)
CREATE TABLE departamentos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    filial_id BIGINT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    criado_em DATETIME,
    atualizado_em DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_departamentos_on_filial FOREIGN KEY (filial_id) REFERENCES filiais (id)
);

-- Tabela para Localizações (vinculadas a uma Filial)
CREATE TABLE localizacoes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    filial_id BIGINT NOT NULL,
    localizacao_pai_id BIGINT,
    nome VARCHAR(255) NOT NULL,
    descricao VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    criado_em DATETIME,
    atualizado_em DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_localizacoes_on_filial FOREIGN KEY (filial_id) REFERENCES filiais (id),
    CONSTRAINT fk_localizacoes_on_pai FOREIGN KEY (localizacao_pai_id) REFERENCES localizacoes (id)
);

-- Tabela para Pessoas/Usuários (vinculados a uma Filial e Departamento)
CREATE TABLE pessoas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    filial_id BIGINT NOT NULL,
    departamento_id BIGINT NOT NULL,
    nome VARCHAR(255) NOT NULL,
    matricula VARCHAR(50) UNIQUE,
    cargo VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    criado_em DATETIME,
    atualizado_em DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_pessoas_on_filial FOREIGN KEY (filial_id) REFERENCES filiais (id),
    CONSTRAINT fk_pessoas_on_departamento FOREIGN KEY (departamento_id) REFERENCES departamentos (id)
);

-- Tabela principal de Ativos
CREATE TABLE ativos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    filial_id BIGINT NOT NULL,
    tipo_ativo_id BIGINT NOT NULL,
    localizacao_id BIGINT,
    fornecedor_id BIGINT NOT NULL,
    pessoa_responsavel_id BIGINT,
    nome VARCHAR(255) NOT NULL,
    numero_patrimonio VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(255),
    data_aquisicao DATE NOT NULL,
    valor_aquisicao DECIMAL(15, 2) NOT NULL,
    valor_residual DECIMAL(15, 2),
    vida_util_meses INT,
    metodo_depreciacao VARCHAR(255),
    data_inicio_depreciacao DATE,
    taxa_depreciacao_mensal DECIMAL(15, 6),
    informacoes_garantia VARCHAR(255),
    observacoes TEXT,
    data_registro DATE NOT NULL,
    criado_em DATETIME,
    atualizado_em DATETIME,
    depreciacao_acumulada DECIMAL(15, 2),
    valor_contabil_atual DECIMAL(15, 2),
    data_ultima_depreciacao DATE,
    PRIMARY KEY (id),
    CONSTRAINT fk_ativos_on_filial FOREIGN KEY (filial_id) REFERENCES filiais (id),
    CONSTRAINT fk_ativos_on_tipo_ativo FOREIGN KEY (tipo_ativo_id) REFERENCES tipos_ativo (id),
    CONSTRAINT fk_ativos_on_localizacao FOREIGN KEY (localizacao_id) REFERENCES localizacoes (id),
    CONSTRAINT fk_ativos_on_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES fornecedores (id),
    CONSTRAINT fk_ativos_on_pessoa FOREIGN KEY (pessoa_responsavel_id) REFERENCES pessoas (id)
);

-- Tabela de detalhes de hardware (relação 1-para-1 com Ativos)
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

-- Tabela para Discos
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

-- Tabela para Memórias
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

-- Tabela para Adaptadores de Rede
CREATE TABLE adaptadores_rede (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ativo_detalhe_hardware_id BIGINT NOT NULL,
    description VARCHAR(255),
    mac_address VARCHAR(255),
    ip_addresses TEXT,
    PRIMARY KEY (id),
    CONSTRAINT fk_adaptadores_rede_on_detalhes_hardware FOREIGN KEY (ativo_detalhe_hardware_id) REFERENCES ativo_detalhes_hardware (id)
);

-- Tabela para Manutenções
CREATE TABLE manutencoes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ativo_id BIGINT NOT NULL,
    fornecedor_id BIGINT,
    solicitante_id BIGINT NOT NULL,
    tecnico_responsavel_id BIGINT,
    tipo VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    data_solicitacao DATE NOT NULL,
    data_inicio DATE,
    data_conclusao DATE,
    data_prevista_conclusao DATE,
    descricao_problema TEXT,
    descricao_servico TEXT,
    custo_estimado DECIMAL(15, 2),
    custo_real DECIMAL(15, 2),
    tempo_execucao_minutos INT,
    observacoes TEXT,
    criado_em DATETIME,
    atualizado_em DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_manutencoes_on_ativo FOREIGN KEY (ativo_id) REFERENCES ativos (id),
    CONSTRAINT fk_manutencoes_on_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES fornecedores (id),
    CONSTRAINT fk_manutencoes_on_solicitante FOREIGN KEY (solicitante_id) REFERENCES pessoas (id),
    CONSTRAINT fk_manutencoes_on_tecnico FOREIGN KEY (tecnico_responsavel_id) REFERENCES pessoas (id)
);

-- Tabela para Movimentações
CREATE TABLE movimentacoes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ativo_id BIGINT NOT NULL,
    localizacao_origem_id BIGINT NOT NULL,
    localizacao_destino_id BIGINT NOT NULL,
    pessoa_origem_id BIGINT NOT NULL,
    pessoa_destino_id BIGINT NOT NULL,
    data_movimentacao DATE NOT NULL,
    data_efetivacao DATE,
    status VARCHAR(255) NOT NULL,
    motivo TEXT,
    observacoes TEXT,
    criado_em DATETIME,
    atualizado_em DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_movimentacoes_on_ativo FOREIGN KEY (ativo_id) REFERENCES ativos (id),
    CONSTRAINT fk_movimentacoes_on_loc_origem FOREIGN KEY (localizacao_origem_id) REFERENCES localizacoes (id),
    CONSTRAINT fk_movimentacoes_on_loc_destino FOREIGN KEY (localizacao_destino_id) REFERENCES localizacoes (id),
    CONSTRAINT fk_movimentacoes_on_pessoa_origem FOREIGN KEY (pessoa_origem_id) REFERENCES pessoas (id),
    CONSTRAINT fk_movimentacoes_on_pessoa_destino FOREIGN KEY (pessoa_destino_id) REFERENCES pessoas (id)
);
