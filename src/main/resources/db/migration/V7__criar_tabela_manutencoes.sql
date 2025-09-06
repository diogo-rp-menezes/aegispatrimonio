CREATE TABLE manutencoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ativo_id BIGINT NOT NULL,
    tipo ENUM('PREVENTIVA', 'CORRETIVA', 'PREDITIVA', 'AJUSTE', 'CALIBRACAO', 'LIMPEZA', 'OUTROS') NOT NULL,
    status ENUM('SOLICITADA', 'APROVADA', 'EM_ANDAMENTO', 'AGUARDANDO_PECAS', 'CONCLUIDA', 'CANCELADA', 'REPROVADA') NOT NULL DEFAULT 'SOLICITADA',
    data_solicitacao DATE NOT NULL,
    data_inicio DATE NULL,
    data_conclusao DATE NULL,
    data_prevista_conclusao DATE NULL,
    descricao_problema TEXT NOT NULL,
    descricao_servico TEXT NULL,
    custo_estimado DECIMAL(15,2) NULL,
    custo_real DECIMAL(15,2) NULL,
    fornecedor_id BIGINT NULL,
    solicitante_id BIGINT NOT NULL,
    tecnico_responsavel_id BIGINT NULL,
    tempo_execucao_minutos INT NULL,
    observacoes TEXT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (ativo_id) REFERENCES ativos(id),
    FOREIGN KEY (fornecedor_id) REFERENCES fornecedores(id),
    FOREIGN KEY (solicitante_id) REFERENCES pessoas(id),
    FOREIGN KEY (tecnico_responsavel_id) REFERENCES pessoas(id)
);

-- Índices para melhor performance
CREATE INDEX idx_manutencoes_ativo ON manutencoes(ativo_id);
CREATE INDEX idx_manutencoes_status ON manutencoes(status);
CREATE INDEX idx_manutencoes_tipo ON manutencoes(tipo);
CREATE INDEX idx_manutencoes_data_solicitacao ON manutencoes(data_solicitacao);