SET REFERENTIAL_INTEGRITY FALSE;
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
SET REFERENTIAL_INTEGRITY TRUE;
