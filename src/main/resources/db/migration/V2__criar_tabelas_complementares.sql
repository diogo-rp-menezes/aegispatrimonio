-- Tabela de departamentos
CREATE TABLE departamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    filial_id BIGINT NOT NULL,
    centro_custo VARCHAR(20),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (filial_id) REFERENCES filiais(id)
);

-- Tabela de pessoas
CREATE TABLE pessoas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    departamento_id BIGINT NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (departamento_id) REFERENCES departamentos(id)
);

-- Tabela de ativos (principal)
CREATE TABLE ativos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    tipo_ativo_id BIGINT NOT NULL,
    numero_patrimonio VARCHAR(50) UNIQUE NOT NULL,
    localizacao_id BIGINT NOT NULL,
    status ENUM('ATIVO', 'EM_MANUTENCAO', 'INATIVO', 'BAIXADO') NOT NULL DEFAULT 'ATIVO',
    data_aquisicao DATE NOT NULL,
    fornecedor_id BIGINT NOT NULL,
    valor_aquisicao DECIMAL(15,2) NOT NULL,
    valor_residual DECIMAL(15,2) DEFAULT 0,
    vida_util_meses INT,
    metodo_depreciacao ENUM('LINEAR', 'ACELERADA') DEFAULT 'LINEAR',
    data_inicio_depreciacao DATE,
    taxa_depreciacao_mensal DECIMAL(15,6),
    informacoes_garantia VARCHAR(255),
    pessoa_responsavel_id BIGINT NOT NULL,
    observacoes TEXT,
    data_registro DATE NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tipo_ativo_id) REFERENCES tipos_ativo(id),
    FOREIGN KEY (localizacao_id) REFERENCES localizacoes(id),
    FOREIGN KEY (fornecedor_id) REFERENCES fornecedores(id),
    FOREIGN KEY (pessoa_responsavel_id) REFERENCES pessoas(id)
);