-- V6__create_rbac_tables.sql

CREATE TABLE rbac_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource VARCHAR(64) NOT NULL,
    action VARCHAR(32) NOT NULL,
    description VARCHAR(255),
    context_key VARCHAR(32),
    CONSTRAINT uk_permission_resource_action UNIQUE (resource, action)
);

CREATE TABLE rbac_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE rbac_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES rbac_role(id) ON DELETE CASCADE
);

CREATE TABLE rbac_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_perm_role FOREIGN KEY (role_id) REFERENCES rbac_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_perm_perm FOREIGN KEY (permission_id) REFERENCES rbac_permission(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_perm_resource_action ON rbac_permission(resource, action);
CREATE INDEX idx_user_role_user ON rbac_user_role(user_id);
CREATE INDEX idx_role_perm_role ON rbac_role_permission(role_id);

-- Seed Data: Roles
INSERT INTO rbac_role (name, description) VALUES ('ROLE_ADMIN', 'Administrador com acesso total');
INSERT INTO rbac_role (name, description) VALUES ('ROLE_USER', 'Usuário padrão com acesso limitado');

-- Seed Data: Permissions
-- ATIVO
INSERT INTO rbac_permission (resource, action, description, context_key) VALUES ('ATIVO', 'READ', 'Ler Ativos', 'filialId');
INSERT INTO rbac_permission (resource, action, description, context_key) VALUES ('ATIVO', 'CREATE', 'Criar Ativos', 'filialId');
INSERT INTO rbac_permission (resource, action, description, context_key) VALUES ('ATIVO', 'UPDATE', 'Atualizar Ativos', 'filialId');
INSERT INTO rbac_permission (resource, action, description, context_key) VALUES ('ATIVO', 'DELETE', 'Deletar Ativos', 'filialId');

-- FUNCIONARIO
INSERT INTO rbac_permission (resource, action, description, context_key) VALUES ('FUNCIONARIO', 'READ', 'Ler Funcionários', 'filialId');
INSERT INTO rbac_permission (resource, action, description, context_key) VALUES ('FUNCIONARIO', 'CREATE', 'Criar Funcionários', 'filialId');
INSERT INTO rbac_permission (resource, action, description, context_key) VALUES ('FUNCIONARIO', 'UPDATE', 'Atualizar Funcionários', 'filialId');
INSERT INTO rbac_permission (resource, action, description, context_key) VALUES ('FUNCIONARIO', 'DELETE', 'Deletar Funcionários', 'filialId');

-- Assign Permissions to Roles
-- ADMIN gets everything
INSERT INTO rbac_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM rbac_role r, rbac_permission p WHERE r.name = 'ROLE_ADMIN';

-- USER gets READ on ATIVO and FUNCIONARIO
INSERT INTO rbac_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM rbac_role r, rbac_permission p
WHERE r.name = 'ROLE_USER' AND p.action = 'READ';

-- Migrate Existing Users
INSERT INTO rbac_user_role (user_id, role_id)
SELECT u.id, r.id
FROM usuarios u
JOIN rbac_role r ON (
    (u.role LIKE '%ADMIN%' AND r.name = 'ROLE_ADMIN') OR
    (u.role NOT LIKE '%ADMIN%' AND r.name = 'ROLE_USER')
);
