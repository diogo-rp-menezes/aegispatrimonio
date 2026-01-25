-- Seed Data for MVP

-- 1. Filial (Matriz)
INSERT INTO filiais (id, nome, codigo, cnpj, tipo, status, criado_em, atualizado_em)
VALUES (1, 'Matriz Global', 'MTZ001', '00.000.000/0001-00', 'MATRIZ', 'ATIVO', NOW(), NOW());

-- 2. Departamento (TI) -- CORRECTION: Added status
INSERT INTO departamentos (id, nome, filial_id, status, criado_em, atualizado_em)
VALUES (1, 'Tecnologia da Informação', 1, 'ATIVO', NOW(), NOW());

-- 3. Funcionario (Admin)
INSERT INTO funcionarios (id, nome, matricula, cargo, departamento_id, status, criado_em, atualizado_em)
VALUES (1, 'Administrador do Sistema', 'ADM001', 'System Admin', 1, 'ATIVO', NOW(), NOW());

-- 4. Usuario (Admin) - Password: "123456" (BCrypt)
INSERT INTO usuarios (id, email, password, role, status, funcionario_id, criado_em, atualizado_em)
VALUES (1, 'admin@aegis.com', '$2b$12$KovI10j2uB8v2H1xK7YgI.t9Hj/5Lw6E7pQ8Z9jK0mI1nO2pQ3rS.', 'ADMIN', 'ATIVO', 1, NOW(), NOW());

-- 5. Funcionario <-> Filial
INSERT INTO funcionario_filial (funcionario_id, filial_id)
VALUES (1, 1);

-- 6. Tipos de Ativo
INSERT INTO tipos_ativo (id, nome, descricao, categoria_contabil, status, icone, criado_em, atualizado_em)
VALUES
(1, 'Notebook', 'Computadores portáteis', 'IMOBILIZADO', 'ATIVO', 'bi-laptop', NOW(), NOW()),
(2, 'Monitor', 'Monitores de vídeo', 'IMOBILIZADO', 'ATIVO', 'bi-display', NOW(), NOW()),
(3, 'Cadeira', 'Cadeiras de escritório', 'IMOBILIZADO', 'ATIVO', 'bi-chair', NOW(), NOW());

-- 7. Fornecedores
INSERT INTO fornecedores (id, nome, cnpj, status, criado_em, atualizado_em)
VALUES
(1, 'Dell Computadores', '00.000.000/0002-00', 'ATIVO', NOW(), NOW()),
(2, 'Herman Miller', '00.000.000/0003-00', 'ATIVO', NOW(), NOW());

-- 8. Localizacoes
INSERT INTO localizacoes (id, nome, descricao, filial_id, status, criado_em, atualizado_em)
VALUES (1, 'Sala de Servidores', 'Data Center Principal', 1, 'ATIVO', NOW(), NOW());
