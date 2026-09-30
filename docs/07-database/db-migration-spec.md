# Migration Specification & Plan — MIG-20250115-001

> **Versão:** 1.0 · **Status:** Proposed
> Migration inicial para criação completa do schema do banco de dados `aegis_patrimonio` conforme [[db-schema-spec]].

```yaml
migration:
  id: "MIG-20250115-001"
  title: "Initial schema creation for aegis_patrimonio — 20 tables, constraints, indexes, views, functions, triggers, partitioning"
  source_state: { version: "0.0.0 (empty database)" }
  target_state: { version: "1.0.0 (full schema per db-schema-spec v1.0)" }

  changes:
    - type: "CREATE"
      object: "table ativo"
      description: "Entidade central de ativos patrimoniais com 24 colunas, PK, 7 FKs, 1 UK, 4 CHECKs, 8 índices"
    - type: "CREATE"
      object: "table tipo_ativo"
      description: "Classificação de ativos com regras de depreciação — 10 colunas, PK, 1 UK, 2 CHECKs"
    - type: "CREATE"
      object: "table localizacao"
      description: "Locais físicos/lógicos de alocação — 11 colunas, PK, 1 FK, 1 UK composto, 1 CHECK"
    - type: "CREATE"
      object: "table departamento"
      description: "Unidades organizacionais — 10 colunas, PK, 1 FK, 1 UK composto"
    - type: "CREATE"
      object: "table filial"
      description: "Unidades de negócio/estabelecimentos — 11 colunas, PK, 1 UK (CNPJ), 2 CHECKs"
    - type: "CREATE"
      object: "table fornecedor"
      description: "Entidades externas de aquisição/manutenção — 11 colunas, PK, 1 UK (CNPJ), 1 CHECK"
    - type: "CREATE"
      object: "table funcionario"
      description: "Colaboradores/custodiantes — 15 colunas, PK, 2 FKs, 3 UKs, 3 CHECKs, 5 índices"
    - type: "CREATE"
      object: "table usuario"
      description: "Credenciais de acesso (ADMIN/USER) — 9 colunas, PK, 1 FK, 2 UKs, 1 CHECK, 2 índices"
    - type: "CREATE"
      object: "table manutencao"
      description: "Ordens de manutenção preventiva/corretiva — 22 colunas, PK, 5 FKs, 1 UK, 7 CHECKs, 4 índices"
    - type: "CREATE"
      object: "table alerta"
      description: "Notificações de risco/atenção — 11 colunas, PK, 2 FKs, 3 CHECKs, 4 índices"
    - type: "CREATE"
      object: "table ativo_detalhe_hardware"
      description: "Detalhes técnicos TI (1:1 com ativo) — 9 colunas, PK, 1 FK, 1 UK, 2 CHECKs, 1 índice"
    - type: "CREATE"
      object: "table memoria"
      description: "Pentes de RAM — 7 colunas, PK, 1 FK, 2 CHECKs, 1 índice"
    - type: "CREATE"
      object: "table disco"
      description: "Discos/volumes de armazenamento — 9 colunas, PK, 1 FK, 4 CHECKs, 2 índices"
    - type: "CREATE"
      object: "table adaptador_rede"
      description: "Interfaces de rede — 8 colunas, PK, 1 FK, 1 UK (MAC), 3 CHECKs, 2 índices"
    - type: "CREATE"
      object: "table health_check_disco"
      description: "Monitoramento periódico de saúde de discos (particionado por mês, retenção 13 meses) — 7 colunas, PK composta, 1 FK, 3 CHECKs, 2 índices (1 BRIN)"
    - type: "CREATE"
      object: "table auditoria"
      description: "Trilha imutável de alterações (particionado por ano, retenção 5 anos) — 9 colunas, PK composta, 1 FK, 2 CHECKs, 3 índices (1 BRIN)"
    - type: "CREATE"
      object: "table permissao"
      description: "Autorizações granulares RBAC — 4 colunas, PK, 1 UK composto"
    - type: "CREATE"
      object: "table papel"
      description: "Papéis de acesso granular — 3 colunas, PK, 1 UK"
    - type: "CREATE"
      object: "table papel_permissao"
      description: "Junção N:M papel↔permissao — PK composta, 2 FKs, 1 índice"
    - type: "CREATE"
      object: "table usuario_papel"
      description: "Junção N:M usuario↔papel — PK composta, 2 FKs, 1 índice"
    - type: "CREATE"
      object: "view vw_ativo_completo"
      description: "Visão consolidada do ativo com todas as referências para listagens/relatórios"
    - type: "CREATE"
      object: "view vw_manutencao_detalhada"
      description: "Visão de manutenção com ativo, fornecedor e funcionários envolvidos"
    - type: "CREATE"
      object: "view vw_alerta_aberto"
      description: "Alertas não lidos com dados do ativo e usuário destinatário"
    - type: "CREATE"
      object: "view vw_depreciacao_ativo"
      description: "Cálculo de depreciação linear acumulada e valor residual por ativo"
    - type: "CREATE"
      object: "view vw_disco_saude_atual"
      description: "Último health check de cada disco com indicadores de uso/saúde"
    - type: "CREATE"
      object: "function fn_atualizar_updated_at"
      description: "Trigger genérico para atualizar updated_at em UPDATE"
    - type: "CREATE"
      object: "function fn_auditoria_trigger"
      description: "Trigger de auditoria insert-only com captura de usuário/IP via session variables"
    - type: "CREATE"
      object: "function fn_validar_transicao_status_manutencao"
      description: "Valida transições de status permitidas no fluxo de manutenção"
    - type: "CREATE"
      object: "function fn_calcular_depreciacao_ativo"
      description: "Calcula depreciação acumulada e valor residual na data de referência"
    - type: "CREATE"
      object: "function fn_gerar_numero_os"
      description: "Gera número sequencial de OS no formato OS-YYYY-NNNNNN"
    - type: "CREATE"
      object: "function fn_verificar_ativo_ti"
      description: "Verifica se ativo é de categoria TI baseado no tipo_ativo.codigo"
    - type: "CREATE"
      object: "trigger trg_*_updated_at (10 tabelas)"
      description: "BEFORE UPDATE triggers para atualizar updated_at automaticamente"
    - type: "CREATE"
      object: "trigger trg_*_auditoria (7 tabelas sensíveis)"
      description: "AFTER INSERT/UPDATE/DELETE triggers para trilha de auditoria imutável"
    - type: "CREATE"
      object: "trigger trg_manutencao_validar_transicao"
      description: "BEFORE UPDATE para impedir transições de status inválidas"
    - type: "CREATE"
      object: "trigger trg_ativo_detalhe_hardware_validar_tipo"
      description: "BEFORE INSERT/UPDATE para garantir que apenas ativos TI tenham detalhe de hardware"
    - type: "CREATE"
      object: "sequence seq_numero_os"
      description: "Sequence global para geração de números de OS (reset anual via job)"

  compatibility:
    backward_compatible: true
    forward_compatible: true

  deployment_strategy: "offline"

  rollback:
    possible: true
    strategy: "DROP all objects in reverse dependency order (triggers → functions → views → tables → sequence). Requires downtime window."
```

## 2. Plano Operacional (Expand → Migrate → Contract)

| Fase | Nome | Operações | Checks |
| :--- | :--- | :--- | :--- |
| 1 | Preflight | Verificar PostgreSQL ≥ 15; confirmar extensões `uuid-ossp` (para `gen_random_uuid()`) e `pgcrypto`; validar permissões de `CREATE TABLE`, `CREATE FUNCTION`, `CREATE TRIGGER`, `CREATE VIEW`; confirmar tablespace/default tablespace | `SELECT version();` · `\dx` · `\du` · `SHOW default_tablespace;` |
| 2 | Schema Preparation (Expand) | 1. Criar extensões necessárias (`uuid-ossp`, `pgcrypto`)<br>2. Criar sequence `seq_numero_os`<br>3. Criar 20 tabelas **sem FKs** (ordem topológica: filial → tipo_ativo → localizacao → departamento → fornecedor → funcionario → ativo → usuario → manutencao → alerta → ativo_detalhe_hardware → memoria/disco/adaptador_rede → health_check_disco → auditoria → permissao → papel → papel_permissao → usuario_papel)<br>4. Adicionar PKs, UKs, CHECKs, índices (incl. BRIN)<br>5. Criar particionamento nativo em `health_check_disco` (RANGE mensal) e `auditoria` (RANGE anual)<br>6. Adicionar FKs (ordem respeitando dependências)<br>7. Criar 5 views<br>8. Criar 6 functions<br>9. Criar 20 triggers | Após cada tabela: `\d+ tabela` · `SELECT * FROM information_schema.table_constraints WHERE table_name='...';` · `SELECT indexname FROM pg_indexes WHERE tablename='...';` |
| 3 | Data Transformation | *(N/A — schema vazio, sem dados legados)* | — |
| 4 | Application Compatibility | Aplicação deve:<br>- Definir `SET LOCAL app.current_user_id = '<uuid>';` e `SET LOCAL app.client_ip = '<ip>';` no início de cada transação (middleware)<br>- Usar `gen_random_uuid()` para IDs (não gerar client-side)<br>- Filtrar `WHERE ativo = true` em queries de listagem (soft-delete pattern)<br>- Não fazer `DELETE` físico em tabelas com coluna `ativo`/`ativa` | Code review do middleware de sessão; testes de integração com schema v1.0.0 |
| 5 | Validation | 1. Contar objetos criados vs. especificação<br>2. Verificar FKs resolvem corretamente<br>3. Testar triggers: INSERT/UPDATE/DELETE em `ativo` → registro em `auditoria`<br>4. Testar transição de status inválida em `manutencao` → deve falhar<br>5. Testar `fn_verificar_ativo_ti` com ativo TI e não-TI<br>6. Verificar particionamento: `\d+ health_check_disco` mostra partições<br>7. Testar views retornam dados esperados (com seed mínimo)<br>8. Verificar índices BRIN criados | Queries de validação em seção 5 |
| 6 | Cleanup (Contract) | *(N/A — nada obsoleto na criação inicial)* | — |

## 3. Risk Assessment

```yaml
risk:
  overall_level: "MEDIUM"
  dimensions:
    data_loss: { score: 0, evidence: ["Schema vazio — nenhum dado existente para perder"] }
    downtime: { score: 2, evidence: ["Offline deployment requer janela de manutenção; ~15-30 min para DDL completo em DB vazio"] }
    locking: { score: 1, evidence: ["CREATE TABLE/INDEX em DB vazio não trava nada; FKs adicionadas após carga"] }
    compatibility: { score: 1, evidence: ["Nova aplicação — sem versão anterior; compatibilidade forward garantida por design"] }
    reversibility: { score: 2, evidence: ["Rollback possível via DROP CASCADE, mas requer downtime e perde tudo; testar em staging primeiro"] }
  destructive_operations: []
  required_controls:
    - "Executar em staging idêntico a produção antes de aplicar em prod"
    - "Validar particionamento nativo suportado (PG ≥ 10) — confirmado PG 15+"
    - "Confirmar middleware de sessão implementado antes de habilitar triggers de auditoria"
    - "Job de reset anual de seq_numero_os deve ser criado (pg_cron ou application scheduler)"
    - "Política de retenção de partições (pg_partman ou job manual) deve ser configurada pós-deploy"
```

## 4. Approval Gate

| Campo | Valor |
| :--- | :--- |
| Aprovação obrigatória? | Sim |
| Motivo | Criação de schema completo com particionamento, triggers de auditoria sensíveis a LGPD, e funções de negócio críticas (depreciação, validação de fluxo) |
| Aprovado por | [Tech Lead / DBA / Security Officer] |
| Status | Pending |

## 5. Verification (pós-execução)

> Preencher somente após a migration ser aplicada — checks de schema, integridade, compatibilidade e performance.

```sql
-- 5.1 Contagem de objetos vs. especificação
SELECT 'tables' AS object_type, count(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE';
-- Esperado: 20

SELECT 'views' AS object_type, count(*) FROM information_schema.views WHERE table_schema = 'public';
-- Esperado: 5

SELECT 'functions' AS object_type, count(*) FROM information_schema.routines WHERE routine_schema = 'public' AND routine_type = 'FUNCTION';
-- Esperado: 6

SELECT 'triggers' AS object_type, count(*) FROM information_schema.triggers WHERE trigger_schema = 'public';
-- Esperado: 20 (10 updated_at + 7 auditoria + 1 transição + 1 validação TI + 1 sequence)

-- 5.2 Verificar FKs resolvem
SELECT tc.table_name, kcu.column_name, ccu.table_name AS foreign_table_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage ccu ON tc.constraint_name = ccu.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY' AND tc.table_schema = 'public'
ORDER BY tc.table_name;

-- 5.3 Testar trigger de auditoria
SET LOCAL app.current_user_id = '00000000-0000-0000-0000-000000000001';
SET LOCAL app.client_ip = '192.168.1.100';
INSERT INTO filial (cnpj, razao_social, endereco) VALUES ('11.222.333/0001-44', 'Filial Teste', 'Rua Teste, 123');
SELECT * FROM auditoria WHERE entidade = 'filial' ORDER BY created_at DESC LIMIT 1;
-- Deve retornar 1 registro com acao='CREATE', usuario_id e ip_origem preenchidos

-- 5.4 Testar validação de transição de manutenção
INSERT INTO tipo_ativo (codigo, nome, taxa_depreciacao_anual, vida_util_padrao_anos) VALUES ('TESTE', 'Teste', 10, 5);
INSERT INTO filial (cnpj, razao_social, endereco) VALUES ('11.222.333/0002-55', 'Filial Teste 2', 'Rua Teste 2, 456');
INSERT INTO localizacao (codigo, nome, tipo, filial_id) SELECT 'LOC1', 'Local 1', 'SALA', id FROM filial WHERE cnpj = '11.222.333/0002-55';
INSERT INTO departamento (codigo, nome, filial_id) SELECT 'DEP1', 'Depto 1', id FROM filial WHERE cnpj = '11.222.333/0002-55';
INSERT INTO funcionario (matricula, cpf, nome_completo, email_corporativo, cargo, data_admissao, departamento_id, filial_id)
SELECT 'MAT1', '111.222.333-44', 'Func Teste', 'func@teste.com', 'Cargo', CURRENT_DATE, d.id, f.id
FROM departamento d, filial f WHERE d.codigo = 'DEP1' AND f.cnpj = '11.222.333/0002-55';
INSERT INTO ativo (tag_patrimonio, nome, valor_aquisicao, data_aquisicao, vida_util_anos, tipo_ativo_id, localizacao_id, departamento_id, filial_id)
SELECT 'TAG001', 'Ativo Teste', 1000, CURRENT_DATE, 5, ta.id, l.id, d.id, f.id
FROM tipo_ativo ta, localizacao l, departamento d, filial f
WHERE ta.codigo = 'TESTE' AND l.codigo = 'LOC1' AND d.codigo = 'DEP1' AND f.cnpj = '11.222.333/0002-55';
INSERT INTO manutencao (numero_os, ativo_id, tipo, status, descricao_problema, solicitante_id)
SELECT 'OS-2025-000001', a.id, 'CORRETIVA', 'SOLICITADA', 'Problema teste', func.id
FROM ativo a, funcionario func WHERE a.tag_patrimonio = 'TAG001' AND func.matricula = 'MAT1';
-- Tentar transição inválida: SOLICITADA → APROVADA (pula AGUARDANDO_APROVACAO)
UPDATE manutencao SET status = 'APROVADA' WHERE numero_os = 'OS-2025-000001';
-- Deve falhar com RAISE EXCEPTION do trigger

-- 5.5 Testar fn_verificar_ativo_ti
SELECT fn_verificar_ativo_ti(id) FROM ativo WHERE tag_patrimonio = 'TAG001';
-- Deve retornar false (tipo_ativo TESTE não está na lista de TI)

-- 5.6 Verificar particionamento
\d+ health_check_disco
\d+ auditoria
-- Devem mostrar "Partitioned table" com estratégia RANGE

-- 5.7 Testar views com seed mínimo
SELECT * FROM vw_ativo_completo WHERE tag_patrimonio = 'TAG001';
SELECT * FROM vw_manutencao_detalhada WHERE numero_os = 'OS-2025-000001';
SELECT * FROM vw_alerta_aberto;
SELECT * FROM vw_depreciacao_ativo WHERE tag_patrimonio = 'TAG001';
SELECT * FROM vw_disco_saude_atual;

-- 5.8 Verificar índices BRIN
SELECT indexname, indexdef FROM pg_indexes WHERE tablename IN ('health_check_disco', 'auditoria') AND indexdef ILIKE '%brin%';
-- Esperado: 1 BRIN em health_check_disco(coletado_em), 1 BRIN em auditoria(created_at)

-- 5.9 Performance baseline (opcional, em staging com volume simulado)
EXPLAIN ANALYZE SELECT * FROM ativo WHERE filial_id = '...' AND status = 'ATIVO';
EXPLAIN ANALYZE SELECT * FROM manutencao WHERE ativo_id = '...' ORDER BY data_solicitacao DESC LIMIT 20;
EXPLAIN ANALYZE SELECT * FROM health_check_disco WHERE disco_id = '...' ORDER BY coletado_em DESC LIMIT 100;
-- Verificar uso de índices esperados (Index Scan, não Seq Scan)
```

---

## 6. Migrations Flyway aplicadas (registro incremental)

### V20 — FULLTEXT index em `ativos(nome)` (M4, audit 2026-09-30)

```yaml
migration:
  id: "V20__create_ativos_fulltext_index"
  type: "Java (BaseJavaMigration, padrão V17)"
  motivation: >
    M4 do audit: a busca por nome carregava até 1000 candidatos (id+nome) e fazia
    ranking Levenshtein em memória. Migrada para FULLTEXT (MATCH ... AGAINST em
    NATURAL LANGUAGE MODE) com ranking por relevância no banco.
  changes:
    - type: "CREATE"
      object: "FULLTEXT INDEX ft_ativos_nome ON ativos(nome)"
      description: "MySQL 8 apenas; idempotente via JDBC DatabaseMetaData"
  portability:
    h2: "no-op (H2 não suporta FULLTEXT); AtivoService usa fallback LIKE"
    mysql: "CREATE FULLTEXT INDEX, guardado por verificação de metadados"
  rollback:
    manual: "DROP INDEX ft_ativos_nome ON ativos;"
    note: "Sem perda de dados; aplicação continua funcional via fallback LIKE do AtivoService"
  downtime: "Nenhum — CREATE INDEX online no MySQL 8 (InnoDB); código novo é retrocompatível (fallback LIKE)"
  verification: >
    MySQL: EXPLAIN SELECT ... WHERE MATCH(nome) AGAINST('desktop' IN NATURAL LANGUAGE MODE)
    deve usar ft_ativos_nome (fulltext scan), não Seq Scan.
```

---

<!-- source: db-schema-spec#1.1 -->
<!-- source: db-schema-spec#1.2 -->
<!-- source: db-schema-spec#1.3 -->
<!-- source: db-schema-spec#1.4 -->
<!-- source: db-schema-spec#1.5 -->
<!-- source: db-schema-spec#1.6 -->
<!-- source: db-schema-spec#1.7 -->
<!-- source: db-schema-spec#1.8 -->
<!-- source: db-schema-spec#1.9 -->
<!-- source: db-schema-spec#1.10 -->
<!-- source: db-schema-spec#1.11 -->
<!-- source: db-schema-spec#1.12 -->
<!-- source: db-schema-spec#1.13 -->
<!-- source: db-schema-spec#1.14 -->
<!-- source: db-schema-spec#1.15 -->
<!-- source: db-schema-spec#1.16 -->
<!-- source: db-schema-spec#1.17 -->
<!-- source: db-schema-spec#1.18 -->
<!-- source: db-schema-spec#1.19 -->
<!-- source: db-schema-spec#1.20 -->
<!-- source: db-schema-spec#2 -->
<!-- source: db-schema-spec#3 -->
<!-- source: db-schema-spec#4 -->
<!-- source: db-schema-spec#5 -->
<!-- source: db-schema-spec#6 -->
<!-- source: db-schema-spec#7 -->
<!-- source: db-schema-spec#8 -->
<!-- source: db-schema-spec#9 -->
<!-- source: db-schema-spec#10 -->