SET FOREIGN_KEY_CHECKS = 0;
-- Clean children first to avoid FK issues (use DELETE for H2 compatibility)
DELETE FROM movimentacoes;
DELETE FROM manutencoes;
DELETE FROM adaptadores_rede;
DELETE FROM discos;
DELETE FROM memorias;
DELETE FROM ativo_detalhe_hardware;
DELETE FROM ativos;
DELETE FROM usuarios;
DELETE FROM funcionario_filial;
DELETE FROM departamentos;
DELETE FROM localizacoes;
DELETE FROM funcionarios;
DELETE FROM fornecedores;
DELETE FROM tipos_ativo;
DELETE FROM filiais;

-- RBAC Cleanup
DELETE FROM rbac_user_group;
DELETE FROM rbac_group_permission;
DELETE FROM rbac_group;
DELETE FROM rbac_user_role;
DELETE FROM rbac_role_permission;
DELETE FROM rbac_permission;
DELETE FROM rbac_role;

SET FOREIGN_KEY_CHECKS = 1;
