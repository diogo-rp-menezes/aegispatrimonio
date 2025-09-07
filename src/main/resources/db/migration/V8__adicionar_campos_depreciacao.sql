ALTER TABLE ativos 
ADD COLUMN depreciacao_acumulada DECIMAL(15,2) DEFAULT 0,
ADD COLUMN valor_contabil_atual DECIMAL(15,2),
ADD COLUMN data_ultima_depreciacao DATE;

-- Atualizar valores iniciais
UPDATE ativos 
SET valor_contabil_atual = valor_aquisicao,
    depreciacao_acumulada = 0
WHERE valor_contabil_atual IS NULL;