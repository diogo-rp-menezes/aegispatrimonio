-- ======================================================================
-- Aegis Patrimonio - V2 Seed Initial Data
-- This script populates the database with essential initial data.
-- ======================================================================

-- 1. Filial Matriz
INSERT INTO filiais (id, nome, codigo, tipo, cnpj, endereco, status, criado_em, atualizado_em)
VALUES (1, 'Matriz Principal', 'MATRIZ', 'MATRIZ', '00.000.000/0001-00', 'Sede da Empresa, 123, Centro', 'ATIVO', NOW(), NOW());

-- 2. Departamentos Essenciais
INSERT INTO departamentos (id, filial_id, nome, criado_em, atualizado_em)
VALUES (1, 1, 'Tecnologia da Informação', NOW(), NOW()),
       (2, 1, 'Administrativo', NOW(), NOW());

-- 3. Usuário Administrador
-- A senha é 'password'. O hash abaixo é o resultado de BCryptPasswordEncoder().encode("password")
INSERT INTO pessoas (id, filial_id, departamento_id, nome, matricula, cargo, email, password, role, status, criado_em, atualizado_em)
VALUES (1, 1, 1, 'Admin do Sistema', 'ADM-001', 'Administrador de TI', 'admin@aegis.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'ROLE_ADMIN', 'ATIVO', NOW(), NOW());

-- 4. Fornecedor Padrão
INSERT INTO fornecedores (id, nome, cnpj, status, criado_em, atualizado_em)
VALUES (1, 'Fornecedor Padrão', '99.999.999/0001-99', 'ATIVO', NOW(), NOW());

-- 5. Tipos de Ativo
INSERT INTO tipos_ativo (id, nome, categoria_contabil, status, criado_em, atualizado_em)
VALUES (1, 'Desktop', 'EQUIP-TI', 'ATIVO', NOW(), NOW()),
       (2, 'Notebook', 'EQUIP-TI', 'ATIVO', NOW(), NOW()),
       (3, 'Monitor', 'EQUIP-TI', 'ATIVO', NOW(), NOW()),
       (4, 'Impressora', 'EQUIP-TI', 'ATIVO', NOW(), NOW());

-- 6. Localização Inicial
INSERT INTO localizacoes (id, filial_id, nome, descricao, status, criado_em, atualizado_em)
VALUES (1, 1, 'Escritório Principal', 'Andar principal do prédio da matriz', 'ATIVO', NOW(), NOW());
