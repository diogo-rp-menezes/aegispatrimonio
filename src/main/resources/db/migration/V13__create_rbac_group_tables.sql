-- V13__create_rbac_group_tables.sql

CREATE TABLE rbac_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE rbac_group_permission (
    group_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (group_id, permission_id),
    CONSTRAINT fk_group_perm_group FOREIGN KEY (group_id) REFERENCES rbac_group(id) ON DELETE CASCADE,
    CONSTRAINT fk_group_perm_perm FOREIGN KEY (permission_id) REFERENCES rbac_permission(id) ON DELETE CASCADE
);

CREATE TABLE rbac_user_group (
    user_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, group_id),
    CONSTRAINT fk_user_group_user FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_group_group FOREIGN KEY (group_id) REFERENCES rbac_group(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_group_perm_group ON rbac_group_permission(group_id);
CREATE INDEX idx_user_group_user ON rbac_user_group(user_id);

-- Seed Data: Group
INSERT INTO rbac_group (name, description) VALUES ('GROUP_DEFAULT', 'Grupo padrão de exemplo');
