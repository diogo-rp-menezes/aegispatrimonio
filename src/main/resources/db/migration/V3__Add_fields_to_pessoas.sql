ALTER TABLE pessoas
    ADD COLUMN matricula VARCHAR(255) NULL,
    ADD COLUMN cargo VARCHAR(255) NOT NULL;

-- Adiciona a restrição de unicidade para a matrícula após a coluna ter sido adicionada.
-- Isso assume que a tabela está vazia ou que as matrículas existentes são únicas.
ALTER TABLE pessoas ADD CONSTRAINT uc_pessoas_matricula UNIQUE (matricula);
