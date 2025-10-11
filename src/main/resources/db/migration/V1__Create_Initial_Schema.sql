-- Flyway migration script para a criação do schema inicial completo.

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

-- Criação da tabela de Departamentos
CREATE TABLE departamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    filial_id BIGINT NOT NULL,
    criado_em TIMESTAMP,
    atualizado_em TIMESTAMP,
    FOREIGN KEY (filial_id) REFERENCES filiais(id)
);

-- Criação da tabela de Funcionários (antiga Pessoas)
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

-- Criação da tabela de Usuários (nova)
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    funcionario_id BIGINT UNIQUE,
    criado_em TIMESTAMP,
    atualizado_em TIMESTAMP,
    FOREIGN KEY (funcionario_id) REFERENCES funcionarios(id)
);

-- Criação da tabela de junção para Funcionário <-> Filial (nova)
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

-- CORREÇÃO: Adicionadas as tabelas de detalhes de hardware que faltavam
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
