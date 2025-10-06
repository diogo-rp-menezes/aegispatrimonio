CREATE TABLE movimentacoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ativo_id BIGINT NOT NULL,
    localizacao_origem_id BIGINT NOT NULL,
    localizacao_destino_id BIGINT NOT NULL,
    pessoa_origem_id BIGINT NOT NULL,
    pessoa_destino_id BIGINT NOT NULL,
    data_movimentacao DATE NOT NULL,
    data_efetivacao DATE NULL,
    status ENUM('PENDENTE', 'EFETIVADA', 'CANCELADA') NOT NULL DEFAULT 'PENDENTE',
    motivo VARCHAR(255) NOT NULL,
    observacoes TEXT,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (ativo_id) REFERENCES ativos(id),
    FOREIGN KEY (localizacao_origem_id) REFERENCES localizacoes(id),
    FOREIGN KEY (localizacao_destino_id) REFERENCES localizacoes(id),
    FOREIGN KEY (pessoa_origem_id) REFERENCES pessoas(id),
    FOREIGN KEY (pessoa_destino_id) REFERENCES pessoas(id)
);

-- Índices para melhor performance
CREATE INDEX idx_movimentacoes_ativo ON movimentacoes(ativo_id);
CREATE INDEX idx_movimentacoes_status ON movimentacoes(status);
CREATE INDEX idx_movimentacoes_data ON movimentacoes(data_movimentacao);