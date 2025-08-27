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

-- Inserir dados iniciais para departamentos
INSERT INTO departamentos (nome, filial_id, centro_custo) VALUES
('TI', 1, 'CC001'),
('Financeiro', 1, 'CC002'),
('Recursos Humanos', 1, 'CC003'),
('Vendas - SP', 2, 'CC004'),
('Produção - RJ', 3, 'CC005');

-- Inserir dados iniciais para pessoas
INSERT INTO pessoas (nome, email, departamento_id) VALUES
('João Silva', 'joao.silva@empresa.com', 1),
('Maria Santos', 'maria.santos@empresa.com', 2),
('Pedro Costa', 'pedro.costa@empresa.com', 3),
('Ana Oliveira', 'ana.oliveira@empresa.com', 4),
('Carlos Pereira', 'carlos.pereira@empresa.com', 5);

-- Inserir dados iniciais para localizações (depende de filiais já existentes)
INSERT INTO localizacoes (nome, localizacao_pai_id, filial_id, descricao) VALUES
('Sede Principal', NULL, 1, 'Prédio da matriz'),
('Andar Térreo', 1, 1, 'Recepção e atendimento'),
('Andar 1', 1, 1, 'Departamentos administrativos'),
('Filial SP - Centro', NULL, 2, 'Filial São Paulo centro'),
('Filial RJ - Copacabana', NULL, 3, 'Filial Rio de Janeiro');

-- Inserir dados iniciais para fornecedores
INSERT INTO fornecedores (nome, email_contato, telefone_contato) VALUES
('Tech Solutions Ltda', 'vendas@techsolutions.com', '(11) 9999-8888'),
('Office Furniture Brasil', 'contato@officefurniture.com', '(21) 7777-6666'),
('Auto Veículos S.A.', 'vendas@autoveiculos.com', '(31) 5555-4444'),
('Equipamentos Industriais RJ', 'compras@equipamentosrj.com', '(22) 3333-2222');

-- Inserir dados iniciais para ativos (exemplos)
INSERT INTO ativos (
    nome, tipo_ativo_id, numero_patrimonio, localizacao_id, status, 
    data_aquisicao, fornecedor_id, valor_aquisicao, valor_residual, 
    vida_util_meses, metodo_depreciacao, data_inicio_depreciacao,
    taxa_depreciacao_mensal, pessoa_responsavel_id, data_registro
) VALUES
('Notebook Dell i7', 1, 'PAT-001', 2, 'ATIVO', 
 '2024-01-15', 1, 4500.00, 500.00, 
 60, 'LINEAR', '2024-02-01', 
 0.016667, 1, '2024-01-20'),

('Mesa Executiva', 2, 'PAT-002', 3, 'ATIVO', 
 '2024-02-01', 2, 1200.00, 100.00, 
 120, 'LINEAR', '2024-03-01', 
 0.008333, 2, '2024-02-05'),

('Veículo Fiat Toro', 3, 'PAT-003', 5, 'ATIVO', 
 '2024-03-10', 3, 85000.00, 15000.00, 
 84, 'LINEAR', '2024-04-01', 
 0.011905, 3, '2024-03-15');