CREATE TABLE revinfo (
    rev INTEGER NOT NULL AUTO_INCREMENT,
    revtstmp BIGINT,
    username VARCHAR(255),
    filial_id BIGINT,
    PRIMARY KEY (rev)
);

CREATE TABLE ativos_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype TINYINT,
    filial_id BIGINT,
    nome VARCHAR(255),
    tipo_ativo_id BIGINT,
    numero_patrimonio VARCHAR(255),
    localizacao_id BIGINT,
    status VARCHAR(50),
    data_aquisicao DATE,
    fornecedor_id BIGINT,
    valor_aquisicao DECIMAL(15, 2),
    valor_residual DECIMAL(15, 2),
    vida_util_meses INT,
    metodo_depreciacao VARCHAR(50),
    data_inicio_depreciacao DATE,
    taxa_depreciacao_mensal DECIMAL(15, 6),
    informacoes_garantia VARCHAR(255),
    funcionario_responsavel_id BIGINT,
    observacoes TEXT,
    data_registro DATE,
    criado_em TIMESTAMP,
    atualizado_em TIMESTAMP,
    depreciacao_acumulada DECIMAL(15, 2),
    valor_contabil_atual DECIMAL(15, 2),
    data_ultima_depreciacao DATE,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_ativos_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo (rev)
);
