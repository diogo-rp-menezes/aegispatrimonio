ALTER TABLE filiais
    ADD COLUMN tipo VARCHAR(255) NOT NULL DEFAULT 'FILIAL',
    ADD COLUMN cnpj VARCHAR(18) NULL, -- Temporariamente nulo para preencher depois
    ADD COLUMN endereco VARCHAR(255) NULL;

-- Atualiza a coluna para NOT NULL e adiciona a constraint de unicidade após preencher os dados
-- UPDATE filiais SET cnpj = 'SEU_CNPJ_PADRAO' WHERE cnpj IS NULL;
-- ALTER TABLE filiais MODIFY COLUMN cnpj VARCHAR(18) NOT NULL;
-- ALTER TABLE filiais ADD CONSTRAINT uc_filiais_cnpj UNIQUE (cnpj);
