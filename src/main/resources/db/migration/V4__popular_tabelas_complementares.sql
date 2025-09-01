-- Inserir dados iniciais para departamentos
INSERT INTO departamentos (nome, filial_id, centro_custo) VALUES
('TI', 1, 'CC001'),
('Financeiro', 1, 'CC002'),
('Recursos Humanos', 1, 'CC003'),
('Vendas - SP', 1, 'CC004'),
('Produção - RJ', 2, 'CC005'),
('Administrativo - MG', 3, 'CC006');

-- Inserir dados iniciais para pessoas
INSERT INTO pessoas (nome, email, departamento_id) VALUES
('João Silva', 'joao.silva@empresa.com', 1),
('Maria Santos', 'maria.santos@empresa.com', 2),
('Pedro Costa', 'pedro.costa@empresa.com', 3),
('Ana Oliveira', 'ana.oliveira@empresa.com', 4),
('Carlos Pereira', 'carlos.pereira@empresa.com', 5),
('Fernanda Lima', 'fernanda.lima@empresa.com', 6);

-- Inserir dados iniciais para localizações
INSERT INTO localizacoes (nome, localizacao_pai_id, filial_id, descricao) VALUES
('Sede Principal', NULL, 1, 'Prédio da matriz'),
('Andar Térreo', 1, 1, 'Recepção e atendimento'),
('Andar 1', 1, 1, 'Departamentos administrativos'),
('Filial RJ - Copacabana', NULL, 2, 'Filial Rio de Janeiro'),
('Filial MG - Centro', NULL, 3, 'Filial Minas Gerais');