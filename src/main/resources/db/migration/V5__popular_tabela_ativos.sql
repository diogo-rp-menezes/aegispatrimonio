-- Inserir dados iniciais para ativos
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

('Veículo Fiat Toro', 3, 'PAT-003', 4, 'ATIVO', 
 '2024-03-10', 3, 85000.00, 15000.00, 
 84, 'LINEAR', '2024-04-01', 
 0.011905, 3, '2024-03-15'),

('Software ERP', 5, 'PAT-004', 3, 'ATIVO', 
 '2024-04-01', 1, 25000.00, 0.00, 
 36, 'LINEAR', '2024-05-01', 
 0.027778, 4, '2024-04-05');