-- MySQL-specific indexes for Ativo
CREATE INDEX idx_ativo_filial_id ON ativos (filial_id);
CREATE INDEX idx_ativo_tipo_ativo_id ON ativos (tipo_ativo_id);
CREATE INDEX idx_ativo_status ON ativos (`status`);
CREATE UNIQUE INDEX idx_ativo_numero_patrimonio ON ativos (numero_patrimonio);
