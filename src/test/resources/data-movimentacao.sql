-- Adiciona uma filial, localizações e pessoas extras para o teste de movimentação

-- Filial B
INSERT INTO filial (id, nome) VALUES (2, 'Filial RJ');

-- Localizações na Filial A
INSERT INTO localizacao (id, nome, filial_id) VALUES (2, 'Desenvolvimento', 1), (3, 'Almoxarifado', 1);

-- Pessoas na Filial A
INSERT INTO pessoa (id, nome, email, role, filial_id) VALUES (2, 'Usuário Comum', 'user@aegis.com', 'ROLE_USER', 1), (3, 'João Ninguém', 'joao@aegis.com', 'ROLE_USER', 1);

-- Ativo específico para movimentação
INSERT INTO ativo (id, nome, numero_patrimonio, status, valor_aquisicao, data_aquisicao, vida_util_meses, valor_residual, metodo_depreciacao, data_inicio_depreciacao, depreciacao_acumulada, valor_contabil_atual, tipo_ativo_id, localizacao_id, fornecedor_id, pessoa_responsavel_id, filial_id) VALUES 
(3, 'Cadeira de Escritório', 'CAD001', 'ATIVO', 500.00, '2023-05-10', 60, 50.00, 'LINEAR', '2023-06-01', 0.00, 500.00, 1, 2, 1, 2, 1);

-- Movimentação Pendente para teste
INSERT INTO movimentacao (id, ativo_id, localizacao_origem_id, localizacao_destino_id, pessoa_origem_id, pessoa_destino_id, data_movimentacao, status, motivo, criado_em, atualizado_em) VALUES
(1, 3, 2, 3, 2, 3, '2024-10-01', 'PENDENTE', 'Transferência para estoque', NOW(), NOW());
