SET FOREIGN_KEY_CHECKS = 0;

-- Filial
INSERT INTO filiais (id, nome) VALUES (1, 'Sede SP');

-- Pessoa
INSERT INTO pessoas (id, nome, email, role, filial_id) VALUES (1, 'Admin', 'admin@aegis.com', 'ROLE_ADMIN', 1);

-- Fornecedor
INSERT INTO fornecedores (id, nome, cnpj, telefone, email) VALUES (1, 'Dell', '12345678901234', '11999999999', 'contact@dell.com');

-- Tipo de Ativo
INSERT INTO tipos_ativo (id, nome, categoria_contabil) VALUES (1, 'Notebook', 'IMOBILIZADO');

-- Localização
INSERT INTO localizacoes (id, nome, filial_id) VALUES (1, 'TI', 1);

-- Ativos
INSERT INTO ativos (id, nome, numero_patrimonio, status, valor_aquisicao, data_aquisicao, vida_util_meses, valor_residual, metodo_depreciacao, data_inicio_depreciacao, depreciacao_acumulada, valor_contabil_atual, tipo_ativo_id, localizacao_id, fornecedor_id, pessoa_responsavel_id, filial_id) VALUES 
(1, 'Notebook Dell XPS', 'NTB001', 'ATIVO', 12000.00, '2023-01-01', 60, 1200.00, 'LINEAR', '2023-01-01', 0.00, 12000.00, 1, 1, 1, 1, 1),
(2, 'Monitor Dell 24', 'MON002', 'ATIVO', 1500.00, '2023-01-01', 36, 150.00, 'LINEAR', '2023-01-01', 0.00, 1500.00, 1, 1, 1, 1, 1);

SET FOREIGN_KEY_CHECKS = 1;
