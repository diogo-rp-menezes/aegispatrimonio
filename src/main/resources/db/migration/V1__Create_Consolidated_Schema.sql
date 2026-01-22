-- V1: Consolidated Initial Schema for Aegis Patrimônio
-- This single script creates the complete, correct initial schema.

-- Criação da tabela de Filiais
CREATE TABLE filiais (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    cnpj VARCHAR(18) NOT NULL UNIQUE,
    tipo VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    endereco VARCHAR(255),
    criado_em TIMESTAMP,
    atualizado_em TIMESTAMP
);

-- Criação da tabela de Departamentos (CORRIGIDA)
CREATE TABLE departamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    filial_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL, -- Coluna adicionada para conformidade com a entidade
    criado_em TIMESTAMP,
    atualizado_em TIMESTAMP,
    FOREIGN KEY (filial_id) REFERENCES filiais(id)
);

-- Criação da tabela de Funcionários
CREATE TABLE funcionarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    matricula VARCHAR(50) UNIQUE,
    cargo VARCHAR(100) NOT NULL,
    departamento_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    criado_em TIMESTAMP,
    atualizado_em TIMESTAMP,
    FOREIGN KEY (departamento_id) REFERENCES departamentos(id)
);

-- Criação da tabela de Usuários
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL, -- Coluna legada, pode ser removida em V2
    status VARCHAR(50) NOT NULL,
    funcionario_id BIGINT UNIQUE,
    criado_em TIMESTAMP,
    atualizado_em TIMESTAMP,
    FOREIGN KEY (funcionario_id) REFERENCES funcionarios(id)
);

-- Criação da tabela de junção para Funcionário <-> Filial
CREATE TABLE funcionario_filial (
    funcionario_id BIGINT NOT NULL,
    filial_id BIGINT NOT NULL,
    PRIMARY KEY (funcionario_id, filial_id),
    FOREIGN KEY (funcionario_id) REFERENCES funcionarios(id),
    FOREIGN KEY (filial_id) REFERENCES filiais(id)
);

-- Criação da tabela de Fornecedores
CREATE TABLE fornecedores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cnpj VARCHAR(18) NOT NULL UNIQUE,
    endereco VARCHAR(255),
    nome_contato_principal VARCHAR(255),
    email_principal VARCHAR(255),
    telefone_principal VARCHAR(50),
    observacoes TEXT,
    status VARCHAR(50) NOT NULL,
    criado_em TIMESTAMP,
    atualizado_em TIMESTAMP
);

-- Criação da tabela de Tipos de Ativo
CREATE TABLE tipos_ativo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL UNIQUE,
    descricao VARCHAR(255),
    categoria_contabil VARCHAR(50) NOT NULL,
    status VARCHAR(50),
    icone VARCHAR(255),
    criado_em TIMESTAMP,
    atualizado_em TIMESTAMP
);

-- Criação da tabela de Localizações
CREATE TABLE localizacoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao VARCHAR(255),
    localizacao_pai_id BIGINT,
    filial_id BIGINT NOT NULL,
    status VARCHAR(50),
    criado_em TIMESTAMP,
    atualizado_em TIMESTAMP,
    FOREIGN KEY (filial_id) REFERENCES filiais(id),
    FOREIGN KEY (localizacao_pai_id) REFERENCES localizacoes(id)
);

-- Criação da tabela de Ativos
CREATE TABLE ativos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    numero_patrimonio VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50),
    data_aquisicao DATE NOT NULL,
    valor_aquisicao DECIMAL(15, 2) NOT NULL,
    valor_residual DECIMAL(15, 2),
    vida_util_meses INT,
    metodo_depreciacao VARCHAR(50),
    data_inicio_depreciacao DATE,
    taxa_depreciacao_mensal DECIMAL(15, 6),
    informacoes_garantia VARCHAR(255),
    observacoes TEXT,
    data_registro DATE NOT NULL,
    depreciacao_acumulada DECIMAL(15, 2),
    valor_contabil_atual DECIMAL(15, 2),
    data_ultima_depreciacao DATE,
    filial_id BIGINT NOT NULL,
    tipo_ativo_id BIGINT NOT NULL,
    localizacao_id BIGINT,
    fornecedor_id BIGINT NOT NULL,
    funcionario_responsavel_id BIGINT,
    criado_em TIMESTAMP,
    atualizado_em TIMESTAMP,
    FOREIGN KEY (filial_id) REFERENCES filiais(id),
    FOREIGN KEY (tipo_ativo_id) REFERENCES tipos_ativo(id),
    FOREIGN KEY (localizacao_id) REFERENCES localizacoes(id),
    FOREIGN KEY (fornecedor_id) REFERENCES fornecedores(id),
    FOREIGN KEY (funcionario_responsavel_id) REFERENCES funcionarios(id)
);

-- Criação da tabela de Movimentações
CREATE TABLE movimentacoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ativo_id BIGINT NOT NULL,
    localizacao_origem_id BIGINT NOT NULL,
    localizacao_destino_id BIGINT NOT NULL,
    funcionario_origem_id BIGINT NOT NULL,
    funcionario_destino_id BIGINT NOT NULL,
    data_movimentacao DATE NOT NULL,
    data_efetivacao DATE,
    status VARCHAR(50) NOT NULL,
    motivo VARCHAR(255),
    observacoes TEXT,
    criado_em TIMESTAMP,
    atualizado_em TIMESTAMP,
    FOREIGN KEY (ativo_id) REFERENCES ativos(id),
    FOREIGN KEY (localizacao_origem_id) REFERENCES localizacoes(id),
    FOREIGN KEY (localizacao_destino_id) REFERENCES localizacoes(id),
    FOREIGN KEY (funcionario_origem_id) REFERENCES funcionarios(id),
    FOREIGN KEY (funcionario_destino_id) REFERENCES funcionarios(id)
);

-- Criação da tabela de Manutenções
CREATE TABLE manutencoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ativo_id BIGINT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    descricao_problema TEXT NOT NULL,
    solucao_aplicada TEXT,
    data_solicitacao DATE NOT NULL,
    data_inicio DATE,
    data_conclusao DATE,
    data_prevista_conclusao DATE,
    custo_estimado DECIMAL(15, 2),
    custo_real DECIMAL(15, 2),
    solicitante_id BIGINT NOT NULL,
    tecnico_responsavel_id BIGINT,
    fornecedor_id BIGINT,
    tempo_execucao_minutos INT,
    observacoes TEXT,
    criado_em TIMESTAMP,
    atualizado_em TIMESTAMP,
    FOREIGN KEY (ativo_id) REFERENCES ativos(id),
    FOREIGN KEY (solicitante_id) REFERENCES funcionarios(id),
    FOREIGN KEY (tecnico_responsavel_id) REFERENCES funcionarios(id),
    FOREIGN KEY (fornecedor_id) REFERENCES fornecedores(id)
);

-- Criação da tabela de detalhes de hardware
CREATE TABLE ativo_detalhe_hardware (
    id BIGINT PRIMARY KEY,
    nome_maquina VARCHAR(255),
    dominio VARCHAR(255),
    sistema_operacional VARCHAR(255),
    versao_so VARCHAR(50),
    arquitetura_so VARCHAR(20),
    fabricante VARCHAR(255),
    modelo VARCHAR(255),
    numero_serie VARCHAR(255),
    processador VARCHAR(255),
    processadores_fisicos INT,
    processadores_logicos INT,
    FOREIGN KEY (id) REFERENCES ativos(id)
);

CREATE TABLE discos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ativo_detalhe_hardware_id BIGINT NOT NULL,
    modelo VARCHAR(255),
    numero_serie VARCHAR(255),
    tamanho_gb BIGINT,
    FOREIGN KEY (ativo_detalhe_hardware_id) REFERENCES ativo_detalhe_hardware(id)
);

CREATE TABLE memorias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ativo_detalhe_hardware_id BIGINT NOT NULL,
    capacidade_gb INT,
    tipo VARCHAR(50),
    velocidade_mhz INT,
    FOREIGN KEY (ativo_detalhe_hardware_id) REFERENCES ativo_detalhe_hardware(id)
);

CREATE TABLE adaptadores_rede (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ativo_detalhe_hardware_id BIGINT NOT NULL,
    nome VARCHAR(255),
    endereco_mac VARCHAR(20),
    endereco_ip VARCHAR(50),
    FOREIGN KEY (ativo_detalhe_hardware_id) REFERENCES ativo_detalhe_hardware(id)
);

-- RBAC Schema
CREATE TABLE rbac_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource VARCHAR(64) NOT NULL,
    action VARCHAR(32) NOT NULL,
    description VARCHAR(255),
    context_key VARCHAR(32) NULL,
    CONSTRAINT uq_perm_resource_action UNIQUE (resource, action)
);

CREATE TABLE rbac_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT uq_role_name UNIQUE (name)
);

CREATE TABLE rbac_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT uq_group_name UNIQUE (name)
);

-- Association tables
CREATE TABLE rbac_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES usuarios(id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES rbac_role(id)
);

CREATE TABLE rbac_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_perm_role FOREIGN KEY (role_id) REFERENCES rbac_role(id),
    CONSTRAINT fk_role_perm_perm FOREIGN KEY (permission_id) REFERENCES rbac_permission(id)
);

CREATE TABLE rbac_group_permission (
    group_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (group_id, permission_id),
    CONSTRAINT fk_group_perm_group FOREIGN KEY (group_id) REFERENCES rbac_group(id),
    CONSTRAINT fk_group_perm_perm FOREIGN KEY (permission_id) REFERENCES rbac_permission(id)
);

CREATE TABLE rbac_user_group (
    user_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, group_id),
    CONSTRAINT fk_user_group_user FOREIGN KEY (user_id) REFERENCES usuarios(id),
    CONSTRAINT fk_user_group_group FOREIGN KEY (group_id) REFERENCES rbac_group(id)
);

-- Optional specific context grants
CREATE TABLE rbac_user_permission_context (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    filial_id BIGINT NULL,
    CONSTRAINT fk_upc_user FOREIGN KEY (user_id) REFERENCES usuarios(id),
    CONSTRAINT fk_upc_perm FOREIGN KEY (permission_id) REFERENCES rbac_permission(id),
    CONSTRAINT idx_upc_user_perm UNIQUE (user_id, permission_id, filial_id)
);

-- Indexes for performance
CREATE INDEX idx_perm_resource_action ON rbac_permission(resource, action);
CREATE INDEX idx_user_role_user ON rbac_user_role(user_id);
CREATE INDEX idx_role_perm_role ON rbac_role_permission(role_id);
CREATE INDEX idx_group_perm_group ON rbac_group_permission(group_id);
CREATE INDEX idx_user_group_user ON rbac_user_group(user_id);
