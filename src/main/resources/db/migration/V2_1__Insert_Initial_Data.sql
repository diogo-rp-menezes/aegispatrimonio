-- V2: Insert Initial RBAC Data (Seeds)
-- This script populates the database with essential initial data for roles and permissions.

-- Permissions for ATIVO domain
INSERT INTO rbac_permission (resource, action, description, context_key) VALUES
 ('ATIVO', 'READ',   'Ler ativos', NULL),
 ('ATIVO', 'CREATE', 'Criar ativos', NULL),
 ('ATIVO', 'UPDATE', 'Atualizar ativos', NULL),
 ('ATIVO', 'DELETE', 'Excluir ativos', NULL)
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- Permissions for FUNCIONARIO domain
INSERT INTO rbac_permission (resource, action, description, context_key) VALUES
 ('FUNCIONARIO', 'READ',   'Ler funcionários', NULL),
 ('FUNCIONARIO', 'CREATE', 'Criar funcionários', NULL),
 ('FUNCIONARIO', 'UPDATE', 'Atualizar funcionários', NULL),
 ('FUNCIONARIO', 'DELETE', 'Excluir funcionários', NULL)
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- Roles
INSERT INTO rbac_role (name, description) VALUES
 ('ADMIN', 'Administrador com acesso total'),
 ('USER',  'Usuário com acesso de leitura por padrão')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- Map ADMIN -> ALL permissions
INSERT INTO rbac_role_permission (role_id, permission_id)
SELECT r.id AS role_id, p.id AS permission_id
FROM rbac_role r
CROSS JOIN rbac_permission p
WHERE r.name = 'ADMIN'
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- Map USER -> READ only for both domains
INSERT INTO rbac_role_permission (role_id, permission_id)
SELECT r.id AS role_id, p.id AS permission_id
FROM rbac_role r
JOIN rbac_permission p ON p.action = 'READ' AND p.resource IN ('ATIVO','FUNCIONARIO')
WHERE r.name = 'USER'
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);
