-- Adiciona a coluna filial_id em todas as tabelas que precisam do vínculo.

-- Adiciona filial_id à tabela de ativos
ALTER TABLE ativos
    ADD COLUMN filial_id BIGINT;

-- Adiciona filial_id à tabela de pessoas
ALTER TABLE pessoas
    ADD COLUMN filial_id BIGINT;

-- Adiciona filial_id à tabela de departamentos
ALTER TABLE departamentos
    ADD COLUMN filial_id BIGINT;

-- Adiciona as chaves estrangeiras (foreign keys)
-- É uma boa prática fazer isso separadamente após adicionar as colunas.
ALTER TABLE ativos
    ADD CONSTRAINT fk_ativos_on_filial FOREIGN KEY (filial_id) REFERENCES filiais (id);

ALTER TABLE pessoas
    ADD CONSTRAINT fk_pessoas_on_filial FOREIGN KEY (filial_id) REFERENCES filiais (id);

ALTER TABLE departamentos
    ADD CONSTRAINT fk_departamentos_on_filial FOREIGN KEY (filial_id) REFERENCES filiais (id);

-- OBSERVAÇÃO: Se suas tabelas já contêm dados, você precisará preencher
-- a coluna filial_id com um valor padrão antes de torná-la NOT NULL.
-- Exemplo:
-- UPDATE ativos SET filial_id = 1 WHERE filial_id IS NULL;
-- ALTER TABLE ativos MODIFY COLUMN filial_id BIGINT NOT NULL;
