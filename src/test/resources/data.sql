SET FOREIGN_KEY_CHECKS = 0;

-- Filial
INSERT INTO filiais (id, nome, codigo, cnpj, tipo, status) VALUES (1, 'Sede SP', 'SEDE-SP', '00000000000100', 'MATRIZ', 'ATIVO');

-- Departamento (Necessário para a Pessoa)
INSERT INTO departamentos (id, nome, filial_id) VALUES (1, 'TI', 1);

-- Pessoa
INSERT INTO pessoas (id, nome, email, password, role, filial_id, departamento_id, cargo, matricula, status) VALUES (1, 'Admin', 'admin@aegis.com', '$2a$10$someRandomHashForTests', 'ROLE_ADMIN', 1, 1, 'Administrador', 'ADM-001', 'ATIVO');

-- Fornecedor
INSERT INTO fornecedores (id, nome, cnpj, telefone_principal, email_principal) VALUES (1, 'Dell', '12345678901234', '11999999999', 'contact@dell.com');

-- Tipo de Ativo
INSERT INTO tipos_ativo (id, nome, categoria_contabil, status) VALUES (1, 'Notebook', 'IMOBILIZADO', 'ATIVO');

-- Localização
INSERT INTO localizacoes (id, nome, filial_id, status) VALUES (1, 'TI', 1, 'ATIVO');

-- Ativos
-- CORREÇÃO: Adicionada a coluna data_registro
INSERT INTO ativos (id, nome, numero_patrimonio, status, valor_aquisicao, data_aquisicao, vida_util_meses, valor_residual, metodo_depreciacao, data_inicio_depreciacao, depreciacao_acumulada, valor_contabil_atual, tipo_ativo_id, localizacao_id, fornecedor_id, pessoa_responsavel_id, filial_id, data_registro) VALUES 
(1, 'Notebook Dell XPS', 'NTB001', 'ATIVO', 12000.00, '2023-01-01', 60, 1200.00, 'LINEAR', '2023-01-01', 0.00, 12000.00, 1, 1, 1, 1, 1, '2023-01-01'),
(2, 'Monitor Dell 24', 'MON002', 'ATIVO', 1500.00, '2023-01-01', 36, 150.00, 'LINEAR', '2023-01-01', 0.00, 1500.00, 1, 1, 1, 1, 1, '2023-01-01');

SET FOREIGN_KEY_CHECKS = 1;
