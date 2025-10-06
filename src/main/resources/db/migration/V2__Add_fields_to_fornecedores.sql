ALTER TABLE fornecedores
    ADD COLUMN cnpj VARCHAR(18) NOT NULL,
    ADD COLUMN endereco VARCHAR(255) NULL,
    ADD COLUMN nome_contato_principal VARCHAR(255) NULL,
    ADD COLUMN observacoes TEXT NULL,
    CHANGE email_contato email_principal VARCHAR(255) NULL,
    CHANGE telefone_contato telefone_principal VARCHAR(255) NULL;

-- Adiciona a restrição de unicidade para o CNPJ após a coluna ter sido adicionada.
-- Isso assume que a tabela está vazia ou que os CNPJs existentes são únicos.
ALTER TABLE fornecedores ADD CONSTRAINT uc_fornecedores_cnpj UNIQUE (cnpj);
