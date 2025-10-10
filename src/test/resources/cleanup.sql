SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM movimentacoes;
DELETE FROM manutencoes;
DELETE FROM ativos;
DELETE FROM tipos_ativo;
DELETE FROM localizacoes;
DELETE FROM pessoas;
DELETE FROM fornecedores;
DELETE FROM departamentos;
DELETE FROM filiais;

SET FOREIGN_KEY_CHECKS = 1;
