-- Tabela de tipos de ativo
CREATE TABLE tipos_ativo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao VARCHAR(255),
    categoria_contabil VARCHAR(50) NOT NULL COMMENT 'Imobilizado - Máquinas e Equipamentos, Imobilizado - Móveis e Utensílios, etc.',
    icone VARCHAR(50),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabela de filiais
CREATE TABLE filiais (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    codigo VARCHAR(10) UNIQUE NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabela de localizações
CREATE TABLE localizacoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    localizacao_pai_id BIGINT NULL,
    filial_id BIGINT NOT NULL,
    descricao VARCHAR(255),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (localizacao_pai_id) REFERENCES localizacoes(id),
    FOREIGN KEY (filial_id) REFERENCES filiais(id)
);

-- Tabela de fornecedores
CREATE TABLE fornecedores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email_contato VARCHAR(100),
    telefone_contato VARCHAR(20),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Inserir dados iniciais
INSERT INTO tipos_ativo (nome, descricao, categoria_contabil, icone) VALUES
('Computador', 'Equipamentos de informática', 'Imobilizado - Máquinas e Equipamentos', 'bi-cpu'),
('Mobiliário', 'Mesas, cadeiras e armários', 'Imobilizado - Móveis e Utensílios', 'bi-table'),
('Veículo', 'Automóveis e utilitários', 'Imobilizado - Veículos', 'bi-truck'),
('Equipamento Especial', 'Equipamentos específicos', 'Imobilizado - Máquinas e Equipamentos', 'bi-tools');

INSERT INTO filiais (nome, codigo) VALUES
('Matriz', 'MTZ001'),
('Filial São Paulo', 'SP002'),
('Filial Rio de Janeiro', 'RJ003');