-- Inserir dados iniciais para departamentos
INSERT INTO departamentos (nome, filial_id, centro_custo, status) VALUES
('TI', 1, 'CC001', 'ATIVO'),
('Financeiro', 1, 'CC002', 'ATIVO'),
('Recursos Humanos', 1, 'CC003', 'ATIVO'),
('Vendas - SP', 1, 'CC004', 'ATIVO'),
('Produção - RJ', 2, 'CC005', 'ATIVO'),
('Administrativo - MG', 3, 'CC006', 'ATIVO');

-- Inserir dados iniciais para pessoas
INSERT INTO pessoas (nome, email, departamento_id, status) VALUES
('João Silva', 'joao.silva@empresa.com', 1, 'ATIVO'),
('Maria Santos', 'maria.santos@empresa.com', 2, 'ATIVO'),
('Pedro Costa', 'pedro.costa@empresa.com', 3, 'ATIVO'),
('Ana Oliveira', 'ana.oliveira@empresa.com', 4, 'ATIVO'),
('Carlos Pereira', 'carlos.pereira@empresa.com', 5, 'ATIVO'),
('Fernanda Lima', 'fernanda.lima@empresa.com', 6, 'ATIVO');

-- Inserir dados iniciais para localizações
INSERT INTO localizacoes (nome, localizacao_pai_id, filial_id, descricao, status) VALUES
('Sede Principal', NULL, 1, 'Prédio da matriz', 'ATIVO'),
('Andar Térreo', 1, 1, 'Recepção e atendimento', 'ATIVO'),
('Andar 1', 1, 1, 'Departamentos administrativos', 'ATIVO'),
('Filial RJ - Copacabana', NULL, 2, 'Filial Rio de Janeiro', 'ATIVO'),
('Filial MG - Centro', NULL, 3, 'Filial Minas Gerais', 'ATIVO');