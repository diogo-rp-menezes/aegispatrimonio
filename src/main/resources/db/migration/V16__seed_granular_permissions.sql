-- V16__seed_granular_permissions.sql
-- Seed dos pares RESOURCE:ACTION granulares usados pelos controllers (mapeamento do package-info
-- do pacote controller, subtask H1). Idempotente: usa INSERT ... SELECT ... WHERE NOT EXISTS,
-- compatível com MySQL 8 e H2 (os ITs rodam em H2).
--
-- IMPORTANTE: os controllers foram migrados para os pares granulares (subtask H1c).
-- context_key reflete exatamente o que cada @PreAuthorize passa: apenas
-- DEPARTAMENTO:CREATE e LOCALIZACAO:CREATE recebem 'filialId'; todos os demais pares
-- recebem context=null (PermissionServiceImpl nega quando o contexto é requerido e ausente).
-- Nenhum vínculo ATIVO:* existente é removido.

-- ============================================================
-- 1. Novas permissões granulares (rbac_permission)
--    Idempotente via NOT EXISTS sobre a UNIQUE (resource, action).
-- ============================================================

-- MANUTENCAO: CREATE, READ, UPDATE, DELETE
-- context_key = NULL: os controllers passam context=null no @PreAuthorize (H1c).
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'MANUTENCAO', 'CREATE', 'Criar Manutenções', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'MANUTENCAO' AND action = 'CREATE');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'MANUTENCAO', 'READ', 'Ler Manutenções', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'MANUTENCAO' AND action = 'READ');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'MANUTENCAO', 'UPDATE', 'Atualizar Manutenções', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'MANUTENCAO' AND action = 'UPDATE');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'MANUTENCAO', 'DELETE', 'Deletar Manutenções', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'MANUTENCAO' AND action = 'DELETE');

-- MOVIMENTACAO: CREATE, READ, UPDATE, DELETE
-- context_key = NULL: os controllers passam context=null no @PreAuthorize (H1c).
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'MOVIMENTACAO', 'CREATE', 'Criar Movimentações', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'MOVIMENTACAO' AND action = 'CREATE');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'MOVIMENTACAO', 'READ', 'Ler Movimentações', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'MOVIMENTACAO' AND action = 'READ');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'MOVIMENTACAO', 'UPDATE', 'Atualizar Movimentações', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'MOVIMENTACAO' AND action = 'UPDATE');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'MOVIMENTACAO', 'DELETE', 'Deletar Movimentações', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'MOVIMENTACAO' AND action = 'DELETE');

-- DEPRECIACAO: READ, UPDATE
-- context_key = NULL: os controllers passam context=null no @PreAuthorize (H1c).
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'DEPRECIACAO', 'READ', 'Ler Depreciações', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'DEPRECIACAO' AND action = 'READ');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'DEPRECIACAO', 'UPDATE', 'Atualizar Depreciações', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'DEPRECIACAO' AND action = 'UPDATE');

-- TIPO_ATIVO: CREATE, READ, UPDATE, DELETE
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'TIPO_ATIVO', 'CREATE', 'Criar Tipos de Ativo', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'TIPO_ATIVO' AND action = 'CREATE');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'TIPO_ATIVO', 'READ', 'Ler Tipos de Ativo', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'TIPO_ATIVO' AND action = 'READ');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'TIPO_ATIVO', 'UPDATE', 'Atualizar Tipos de Ativo', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'TIPO_ATIVO' AND action = 'UPDATE');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'TIPO_ATIVO', 'DELETE', 'Deletar Tipos de Ativo', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'TIPO_ATIVO' AND action = 'DELETE');

-- FILIAL: CREATE, READ, UPDATE, DELETE
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'FILIAL', 'CREATE', 'Criar Filiais', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'FILIAL' AND action = 'CREATE');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'FILIAL', 'READ', 'Ler Filiais', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'FILIAL' AND action = 'READ');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'FILIAL', 'UPDATE', 'Atualizar Filiais', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'FILIAL' AND action = 'UPDATE');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'FILIAL', 'DELETE', 'Deletar Filiais', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'FILIAL' AND action = 'DELETE');

-- DEPARTAMENTO: CREATE, READ, UPDATE, DELETE
-- context_key: apenas CREATE recebe 'filialId' (DepartamentoController CREATE passa
-- #departamentoCreateDTO.filialId); READ/UPDATE/DELETE recebem context=null (H1c).
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'DEPARTAMENTO', 'CREATE', 'Criar Departamentos', 'filialId'
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'DEPARTAMENTO' AND action = 'CREATE');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'DEPARTAMENTO', 'READ', 'Ler Departamentos', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'DEPARTAMENTO' AND action = 'READ');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'DEPARTAMENTO', 'UPDATE', 'Atualizar Departamentos', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'DEPARTAMENTO' AND action = 'UPDATE');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'DEPARTAMENTO', 'DELETE', 'Deletar Departamentos', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'DEPARTAMENTO' AND action = 'DELETE');

-- LOCALIZACAO: CREATE, READ, UPDATE, DELETE
-- context_key: apenas CREATE recebe 'filialId' (LocalizacaoController CREATE passa
-- #localizacaoCreateDTO.filialId); READ/UPDATE/DELETE recebem context=null (H1c).
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'LOCALIZACAO', 'CREATE', 'Criar Localizações', 'filialId'
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'LOCALIZACAO' AND action = 'CREATE');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'LOCALIZACAO', 'READ', 'Ler Localizações', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'LOCALIZACAO' AND action = 'READ');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'LOCALIZACAO', 'UPDATE', 'Atualizar Localizações', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'LOCALIZACAO' AND action = 'UPDATE');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'LOCALIZACAO', 'DELETE', 'Deletar Localizações', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'LOCALIZACAO' AND action = 'DELETE');

-- FORNECEDOR: CREATE, READ, UPDATE, DELETE
-- context_key = NULL: FornecedorController passa context=null no @PreAuthorize.
-- Pré-H1 o FornecedorController já usava permissionService (fail-closed), portanto
-- apenas ROLE_ADMIN recebe estes pares; acesso granular de USER fica para concessão
-- explícita via RBAC admin (decisão N5, 2026-09-26).
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'FORNECEDOR', 'CREATE', 'Criar Fornecedores', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'FORNECEDOR' AND action = 'CREATE');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'FORNECEDOR', 'READ', 'Ler Fornecedores', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'FORNECEDOR' AND action = 'READ');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'FORNECEDOR', 'UPDATE', 'Atualizar Fornecedores', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'FORNECEDOR' AND action = 'UPDATE');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'FORNECEDOR', 'DELETE', 'Deletar Fornecedores', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'FORNECEDOR' AND action = 'DELETE');

-- DASHBOARD: READ
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'DASHBOARD', 'READ', 'Ler Dashboard', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'DASHBOARD' AND action = 'READ');

-- ALERTA: READ, UPDATE
-- context_key = NULL: os controllers passam context=null no @PreAuthorize (H1c).
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'ALERTA', 'READ', 'Ler Alertas', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'ALERTA' AND action = 'READ');
INSERT INTO rbac_permission (resource, action, description, context_key)
SELECT 'ALERTA', 'UPDATE', 'Atualizar Alertas', NULL
WHERE NOT EXISTS (SELECT 1 FROM rbac_permission WHERE resource = 'ALERTA' AND action = 'UPDATE');

-- ============================================================
-- 2. Vínculos de role (rbac_role_permission)
--    Regra de concessão (correção N4, 2026-09-26): ROLE_USER recebe
--    EXATAMENTE o que o modelo pré-H1 (hasRole/hasAnyRole) dava a USER,
--    verificado no git history (commit 4d815dd, imediatamente anterior à H1):
--      - hasAnyRole('ADMIN','USER') em listagens/detalhes  -> *:READ
--      - MOVIMENTACAO:CREATE (criar movimentação era ADMIN+USER)
--      - ALERTA:UPDATE (marcar como lida era ADMIN+USER)
--    Tudo que era hasRole('ADMIN') fica vinculado apenas a ROLE_ADMIN.
--    FORNECEDOR:* fica apenas em ROLE_ADMIN (pré-H1 já era fail-closed
--    via permissionService; concessão a USER fica para RBAC admin explícito).
--    Idempotente via NOT EXISTS sobre a PK (role_id, permission_id).
--    Nenhum vínculo ATIVO:* existente é removido.
-- ============================================================

-- ADMIN: todos os pares granulares novos (inclui FORNECEDOR)
INSERT INTO rbac_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM rbac_role r
JOIN rbac_permission p ON p.resource IN (
    'MANUTENCAO', 'MOVIMENTACAO', 'DEPRECIACAO', 'TIPO_ATIVO',
    'FILIAL', 'DEPARTAMENTO', 'LOCALIZACAO', 'DASHBOARD', 'ALERTA',
    'FORNECEDOR'
)
WHERE r.name = 'ROLE_ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM rbac_role_permission rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- USER: apenas o que o modelo pré-H1 concedia a USER (regra N4)
INSERT INTO rbac_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM rbac_role r
JOIN rbac_permission p ON (
    (p.resource IN ('MANUTENCAO', 'TIPO_ATIVO', 'FILIAL', 'DEPARTAMENTO',
                    'LOCALIZACAO', 'DASHBOARD')
     AND p.action = 'READ')
    OR (p.resource = 'MOVIMENTACAO' AND p.action IN ('READ', 'CREATE'))
    OR (p.resource = 'ALERTA' AND p.action IN ('READ', 'UPDATE'))
)
WHERE r.name = 'ROLE_USER'
  AND NOT EXISTS (
      SELECT 1 FROM rbac_role_permission rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
