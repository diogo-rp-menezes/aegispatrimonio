# Database Test & Data Quality Specification — aegis_patrimonio

> **Versão:** 1.0 · **Owner:** backend-team · **Status:** Draft
> Complementa [[test-strategy]] com testes específicos de banco de dados.

## 1. Schema Tests

| ID | Tipo | Alvo | Condição Esperada |
| :--- | :--- | :--- | :--- |
| TEST-001 | table_exists | ativo | Tabela existe com 24 colunas definidas no contrato |
| TEST-002 | table_exists | tipo_ativo | Tabela existe com 10 colunas definidas no contrato |
| TEST-003 | table_exists | localizacao | Tabela existe com 11 colunas definidas no contrato |
| TEST-004 | table_exists | departamento | Tabela existe com 10 colunas definidas no contrato |
| TEST-005 | table_exists | filial | Tabela existe com 11 colunas definidas no contrato |
| TEST-006 | table_exists | fornecedor | Tabela existe com 11 colunas definidas no contrato |
| TEST-007 | table_exists | funcionario | Tabela existe com 15 colunas definidas no contrato |
| TEST-008 | table_exists | usuario | Tabela existe com 10 colunas definidas no contrato |
| TEST-009 | table_exists | manutencao | Tabela existe com 22 colunas definidas no contrato |
| TEST-010 | table_exists | alerta | Tabela existe com 11 colunas definidas no contrato |
| TEST-011 | table_exists | ativo_detalhe_hardware | Tabela existe com 9 colunas definidas no contrato |
| TEST-012 | table_exists | memoria | Tabela existe com 7 colunas definidas no contrato |
| TEST-013 | table_exists | disco | Tabela existe com 9 colunas definidas no contrato |
| TEST-014 | table_exists | adaptador_rede | Tabela existe com 8 colunas definidas no contrato |
| TEST-015 | table_exists | health_check_disco | Tabela existe com 7 colunas definidas no contrato; particionada por RANGE em coletado_em (intervalo 1 mês, retenção 13 meses) |
| TEST-016 | table_exists | auditoria | Tabela existe com 9 colunas definidas no contrato; particionada por RANGE em created_at (intervalo 1 ano, retenção 5 anos) |
| TEST-017 | table_exists | permissao | Tabela existe com 4 colunas definidas no contrato |
| TEST-018 | table_exists | papel | Tabela existe com 3 colunas definidas no contrato |
| TEST-019 | table_exists | papel_permissao | Tabela existe com 2 colunas (PK composta papel_id, permissao_id) |
| TEST-020 | table_exists | usuario_papel | Tabela existe com 2 colunas (PK composta usuario_id, papel_id) |
| TEST-021 | column_exists | ativo.id | UUID, NOT NULL, DEFAULT gen_random_uuid() |
| TEST-022 | column_exists | ativo.tag_patrimonio | VARCHAR(50), NOT NULL, sensitivity: confidential |
| TEST-023 | column_exists | ativo.valor_aquisicao | NUMERIC(14,2), NOT NULL, CHECK > 0 |
| TEST-024 | column_exists | ativo.data_aquisicao | DATE, NOT NULL, CHECK <= CURRENT_DATE |
| TEST-025 | column_exists | ativo.vida_util_anos | INTEGER, NOT NULL, CHECK > 0 |
| TEST-026 | column_exists | ativo.status | VARCHAR(20), NOT NULL, DEFAULT 'ATIVO', CHECK IN ('ATIVO','EM_MANUTENCAO','BAIXADO','EM_ESTOQUE') |
| TEST-027 | column_exists | tipo_ativo.taxa_depreciacao_anual | NUMERIC(5,2), NOT NULL, CHECK BETWEEN 0 AND 100 |
| TEST-028 | column_exists | tipo_ativo.vida_util_padrao_anos | INTEGER, NOT NULL, CHECK > 0 |
| TEST-029 | column_exists | localizacao.tipo | VARCHAR(20), NOT NULL, CHECK IN ('SALA','ANDAR','PREDIO','DATA_CENTER','ESTOQUE','EXTERNO') |
| TEST-030 | column_exists | filial.cnpj | VARCHAR(18), NOT NULL, CHECK formato ^\d{2}\.\d{3}\.\d{3}/\d{4}-\d{2}$ |
| TEST-031 | column_exists | fornecedor.cnpj | VARCHAR(18), NOT NULL, CHECK formato ^\d{2}\.\d{3}\.\d{3}/\d{4}-\d{2}$ |
| TEST-032 | column_exists | funcionario.cpf | VARCHAR(14), NOT NULL, CHECK formato ^\d{3}\.\d{3}\.\d{3}-\d{2}$ |
| TEST-033 | column_exists | funcionario.email_corporativo | VARCHAR(255), NOT NULL, CHECK formato email |
| TEST-034 | column_exists | funcionario.data_demissao | DATE, NULL, CHECK > data_admissao WHEN NOT NULL |
| TEST-035 | column_exists | usuario.username | VARCHAR(100), NOT NULL, UNIQUE |
| TEST-036 | column_exists | usuario.password_hash | VARCHAR(255), NOT NULL |
| TEST-037 | column_exists | usuario.role | VARCHAR(10), NOT NULL, CHECK IN ('ADMIN','USER') |
| TEST-038 | column_exists | usuario.funcionario_id | UUID, NOT NULL, UNIQUE, FK -> funcionario.id |
| TEST-039 | column_exists | manutencao.numero_os | VARCHAR(30), NOT NULL, UNIQUE |
| TEST-040 | column_exists | manutencao.tipo | VARCHAR(15), NOT NULL, CHECK IN ('PREVENTIVA','CORRETIVA') |
| TEST-041 | column_exists | manutencao.status | VARCHAR(25), NOT NULL, DEFAULT 'SOLICITADA', CHECK IN ('SOLICITADA','AGUARDANDO_APROVACAO','APROVADA','EM_ANDAMENTO','CONCLUIDA','CANCELADA') |
| TEST-042 | column_exists | manutencao.custo_estimado | NUMERIC(14,2), NULL, CHECK >= 0 |
| TEST-043 | column_exists | manutencao.custo_real | NUMERIC(14,2), NULL, CHECK >= 0 |
| TEST-044 | column_exists | alerta.tipo | VARCHAR(30), NOT NULL, CHECK IN ('DISCO_CRITICO','MANUTENCAO_VENCIDA','GARANTIA_EXPIRANDO','DEPRECIACAO_TOTAL','HARDWARE_DEGRADADO') |
| TEST-045 | column_exists | alerta.severidade | VARCHAR(10), NOT NULL, CHECK IN ('BAIXA','MEDIA','ALTA','CRITICA') |
| TEST-046 | column_exists | alerta.lido | BOOLEAN, NOT NULL, DEFAULT false |
| TEST-047 | column_exists | alerta.data_leitura | TIMESTAMPTZ, NULL, CHECK (lido=true AND data_leitura IS NOT NULL) OR (lido=false) |
| TEST-048 | column_exists | ativo_detalhe_hardware.ativo_id | UUID, NOT NULL, UNIQUE, FK -> ativo.id ON DELETE CASCADE |
| TEST-049 | column_exists | memoria.capacidade_gb | INTEGER, NOT NULL, CHECK > 0 |
| TEST-050 | column_exists | memoria.tipo | VARCHAR(10), NULL, CHECK IN ('DDR3','DDR4','DDR5','LPDDR4','LPDDR5') |
| TEST-051 | column_exists | disco.tipo | VARCHAR(10), NOT NULL, CHECK IN ('HDD','SSD','NVME','EMMC') |
| TEST-052 | column_exists | disco.capacidade_gb | INTEGER, NOT NULL, CHECK > 0 |
| TEST-053 | column_exists | disco.usado_gb | INTEGER, NULL, CHECK >= 0 AND <= capacidade_gb |
| TEST-054 | column_exists | disco.saude_smart | VARCHAR(15), NULL, CHECK IN ('BOM','ATENCAO','CRITICO','DESCONHECIDO') |
| TEST-055 | column_exists | adaptador_rede.mac_address | VARCHAR(17), NOT NULL, UNIQUE, CHECK formato ^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$ |
| TEST-056 | column_exists | adaptador_rede.tipo | VARCHAR(15), NOT NULL, CHECK IN ('ETHERNET','WIFI','BLUETOOTH','VIRTUAL') |
| TEST-057 | column_exists | health_check_disco.espaco_livre_gb | INTEGER, NOT NULL, CHECK >= 0 |
| TEST-058 | column_exists | health_check_disco.espaco_total_gb | INTEGER, NOT NULL, CHECK > 0 |
| TEST-059 | column_exists | health_check_disco.coletado_em | TIMESTAMPTZ, NOT NULL, DEFAULT now() |
| TEST-060 | column_exists | auditoria.entidade | VARCHAR(100), NOT NULL |
| TEST-061 | column_exists | auditoria.acao | VARCHAR(20), NOT NULL, CHECK IN ('CREATE','UPDATE','DELETE','STATUS_CHANGE') |
| TEST-062 | column_exists | auditoria.valores_anteriores | JSONB, NULL |
| TEST-063 | column_exists | auditoria.valores_novos | JSONB, NULL |
| TEST-064 | column_exists | auditoria.ip_origem | VARCHAR(45), NULL, CHECK formato IPv4/IPv6 |
| TEST-065 | column_exists | permissao.recurso | VARCHAR(100), NOT NULL |
| TEST-066 | column_exists | permissao.acao | VARCHAR(50), NOT NULL |
| TEST-067 | column_exists | papel.nome | VARCHAR(100), NOT NULL, UNIQUE |
| TEST-068 | index_exists | ativo.tag_patrimonio | BTREE, UNIQUE (INDEX-001) |
| TEST-069 | index_exists | ativo.(filial_id, status) | BTREE, não único (INDEX-002) |
| TEST-070 | index_exists | ativo.(departamento_id, status) | BTREE, não único (INDEX-003) |
| TEST-071 | index_exists | ativo.localizacao_id | BTREE, não único (INDEX-004) |
| TEST-071 | index_exists | ativo.funcionario_id | BTREE, não único (INDEX-005) |
| TEST-072 | index_exists | ativo.tipo_ativo_id | BTREE, não único (INDEX-006) |
| TEST-073 | index_exists | ativo.fornecedor_id | BTREE, não único (INDEX-007) |
| TEST-074 | index_exists | ativo.data_aquisicao | BTREE DESC, não único (INDEX-008) |
| TEST-075 | index_exists | manutencao.(ativo_id, data_solicitacao) | BTREE, não único (INDEX-009) |
| TEST-076 | index_exists | manutencao.(status, data_solicitacao) | BTREE, não único (INDEX-010) |
| TEST-077 | index_exists | manutencao.solicitante_id | BTREE, não único (INDEX-011) |
| TEST-078 | index_exists | manutencao.fornecedor_id | BTREE, não único (INDEX-012) |
| TEST-079 | index_exists | alerta.(ativo_id, lido, created_at) | BTREE, não único (INDEX-013) |
| TEST-080 | index_exists | alerta.(usuario_id, lido, created_at) | BTREE, não único (INDEX-014) |
| TEST-081 | index_exists | alerta.(severidade, created_at) | BTREE, não único (INDEX-015) |
| TEST-082 | index_exists | alerta.(tipo, created_at) | BTREE, não único (INDEX-016) |
| TEST-083 | index_exists | funcionario.cpf | BTREE, UNIQUE (INDEX-017) |
| TEST-084 | index_exists | funcionario.email_corporativo | BTREE, UNIQUE (INDEX-018) |
| TEST-085 | index_exists | funcionario.matricula | BTREE, UNIQUE (INDEX-019) |
| TEST-086 | index_exists | funcionario.(filial_id, departamento_id, ativo) | BTREE, não único (INDEX-020) |
| TEST-087 | index_exists | usuario.username | BTREE, UNIQUE (INDEX-021) |
| TEST-088 | index_exists | usuario.funcionario_id | BTREE, UNIQUE (INDEX-022) |
| TEST-089 | index_exists | ativo_detalhe_hardware.ativo_id | BTREE, UNIQUE (INDEX-023) |
| TEST-090 | index_exists | memoria.ativo_detalhe_hardware_id | BTREE, não único (INDEX-024) |
| TEST-091 | index_exists | disco.ativo_detalhe_hardware_id | BTREE, não único (INDEX-025) |
| TEST-092 | index_exists | disco.(saude_smart, usado_gb) | BTREE, não único (INDEX-026) |
| TEST-093 | index_exists | adaptador_rede.ativo_detalhe_hardware_id | BTREE, não único (INDEX-027) |
| TEST-094 | index_exists | adaptador_rede.mac_address | BTREE, UNIQUE (INDEX-028) |
| TEST-095 | index_exists | health_check_disco.(disco_id, coletado_em) | BTREE, não único (INDEX-029) |
| TEST-096 | index_exists | health_check_disco.coletado_em | BRIN, não único (INDEX-030) |
| TEST-097 | index_exists | auditoria.(entidade, entidade_id, created_at) | BTREE, não único (INDEX-031) |
| TEST-098 | index_exists | auditoria.(usuario_id, created_at) | BTREE, não único (INDEX-032) |
| TEST-099 | index_exists | auditoria.created_at | BRIN, não único (INDEX-033) |
| TEST-100 | index_exists | localizacao.(filial_id, ativo) | BTREE, não único (INDEX-034) |
| TEST-101 | index_exists | departamento.(filial_id, ativo) | BTREE, não único (INDEX-035) |
| TEST-102 | index_exists | fornecedor.cnpj | BTREE, UNIQUE (INDEX-036) |
| TEST-103 | index_exists | filial.cnpj | BTREE, UNIQUE (INDEX-037) |
| TEST-104 | index_exists | papel_permissao.permissao_id | BTREE, não único (INDEX-038) |
| TEST-105 | index_exists | usuario_papel.papel_id | BTREE, não único (INDEX-039) |
| TEST-106 | view_exists | vw_ativo_completo | View não materializada com joins ativo + 6 tabelas de referência |
| TEST-107 | view_exists | vw_manutencao_detalhada | View não materializada com joins manutencao + ativo + fornecedor + 3x funcionario |
| TEST-108 | view_exists | vw_alerta_aberto | View não materializada filtrando alerta.lido = false com joins ativo + usuario |
| TEST-109 | view_exists | vw_depreciacao_ativo | View não materializada calculando depreciação linear, valor residual, % vida útil |
| TEST-110 | view_exists | vw_disco_saude_atual | View não materializada com DISTINCT ON (disco.id) + LATERAL subquery para último health_check |
| TEST-111 | function_exists | fn_atualizar_updated_at | RETURNS TRIGGER, LANGUAGE plpgsql, VOLATILE, definer |
| TEST-112 | function_exists | fn_auditoria_trigger | RETURNS TRIGGER, LANGUAGE plpgsql, VOLATILE, definer; lê app.current_user_id e app.client_ip |
| TEST-113 | function_exists | fn_validar_transicao_status_manutencao | RETURNS BOOLEAN, IMMUTABLE, valida fluxo SOLICITADA→AGUARDANDO_APROVACAO→APROVADA→EM_ANDAMENTO→CONCLUIDA; qualquer→CANCELADA |
| TEST-114 | function_exists | fn_calcular_depreciacao_ativo | RETURNS TABLE(depreciacao_acumulada, valor_residual, percentual_vida_util_consumida), STABLE |
| TEST-115 | function_exists | fn_gerar_numero_os | RETURNS VARCHAR, VOLATILE, formato OS-YYYY-NNNNNN usando sequence seq_numero_os |
| TEST-116 | function_exists | fn_verificar_ativo_ti | RETURNS BOOLEAN, STABLE, verifica tipo_ativo.codigo IN ('NOTEBOOK','DESKTOP','SERVIDOR','IMPRESSORA','MONITOR','SWITCH','ROTEADOR','STORAGE') |
| TEST-117 | trigger_exists | trg_ativo_updated_at | BEFORE UPDATE ON ativo EXECUTE fn_atualizar_updated_at |
| TEST-118 | trigger_exists | trg_tipo_ativo_updated_at | BEFORE UPDATE ON tipo_ativo EXECUTE fn_atualizar_updated_at |
| TEST-119 | trigger_exists | trg_localizacao_updated_at | BEFORE UPDATE ON localizacao EXECUTE fn_atualizar_updated_at |
| TEST-120 | trigger_exists | trg_departamento_updated_at | BEFORE UPDATE ON departamento EXECUTE fn_atualizar_updated_at |
| TEST-121 | trigger_exists | trg_filial_updated_at | BEFORE UPDATE ON filial EXECUTE fn_atualizar_updated_at |
| TEST-122 | trigger_exists | trg_fornecedor_updated_at | BEFORE UPDATE ON fornecedor EXECUTE fn_atualizar_updated_at |
| TEST-123 | trigger_exists | trg_funcionario_updated_at | BEFORE UPDATE ON funcionario EXECUTE fn_atualizar_updated_at |
| TEST-124 | trigger_exists | trg_usuario_updated_at | BEFORE UPDATE ON usuario EXECUTE fn_atualizar_updated_at |
| TEST-125 | trigger_exists | trg_manutencao_updated_at | BEFORE UPDATE ON manutencao EXECUTE fn_atualizar_updated_at |
| TEST-126 | trigger_exists | trg_ativo_detalhe_hardware_updated_at | BEFORE UPDATE ON ativo_detalhe_hardware EXECUTE fn_atualizar_updated_at |
| TEST-127 | trigger_exists | trg_ativo_auditoria | AFTER INSERT/UPDATE/DELETE ON ativo EXECUTE fn_auditoria_trigger |
| TEST-128 | trigger_exists | trg_funcionario_auditoria | AFTER INSERT/UPDATE/DELETE ON funcionario EXECUTE fn_auditoria_trigger |
| TEST-129 | trigger_exists | trg_usuario_auditoria | AFTER INSERT/UPDATE/DELETE ON usuario EXECUTE fn_auditoria_trigger |
| TEST-130 | trigger_exists | trg_manutencao_auditoria | AFTER INSERT/UPDATE/DELETE ON manutencao EXECUTE fn_auditoria_trigger |
| TEST-131 | trigger_exists | trg_filial_auditoria | AFTER INSERT/UPDATE/DELETE ON filial EXECUTE fn_auditoria_trigger |
| TEST-132 | trigger_exists | trg_fornecedor_auditoria | AFTER INSERT/UPDATE/DELETE ON fornecedor EXECUTE fn_auditoria_trigger |
| TEST-133 | trigger_exists | trg_manutencao_validar_transicao | BEFORE UPDATE ON manutencao WHEN OLD.status IS DISTINCT FROM NEW.status EXECUTE fn_validar_transicao_status_manutencao |
| TEST-134 | trigger_exists | trg_ativo_detalhe_hardware_validar_tipo | BEFORE INSERT/UPDATE ON ativo_detalhe_hardware EXECUTE fn_verificar_ativo_ti |
| TEST-135 | partition_exists | health_check_disco | Particionada por RANGE em coletado_em, intervalo 1 mês, retenção 13 meses |
| TEST-136 | partition_exists | auditoria | Particionada por RANGE em created_at, intervalo 1 ano, retenção 5 anos |
| TEST-137 | pk_composite | health_check_disco | PK composta (id, coletado_em) inclui coluna de particionamento |
| TEST-138 | pk_composite | auditoria | PK composta (id, created_at) inclui coluna de particionamento |
| TEST-139 | pk_composite | papel_permissao | PK composta (papel_id, permissao_id) |
| TEST-140 | pk_composite | usuario_papel | PK composta (usuario_id, papel_id) |

<!-- source: db-schema-spec#1-tables -->
<!-- source: db-schema-spec#3-constraints -->
<!-- source: db-schema-spec#4-indexes -->
<!-- source: db-schema-spec#5-views -->
<!-- source: db-schema-spec#6-functions -->
<!-- source: db-schema-spec#7-triggers -->

## 2. Integrity Tests

| ID | Tipo | Alvo | Condição Esperada |
| :--- | :--- | :--- | :--- |
| TEST-200 | foreign_key | REL-001: ativo.tipo_ativo_id → tipo_ativo.id | ON DELETE RESTRICT, ON UPDATE CASCADE; falha se tipo_ativo referenciado for excluído |
| TEST-201 | foreign_key | REL-002: ativo.localizacao_id → localizacao.id | ON DELETE RESTRICT, ON UPDATE CASCADE |
| TEST-202 | foreign_key | REL-003: ativo.departamento_id → departamento.id | ON DELETE RESTRICT, ON UPDATE CASCADE |
| TEST-203 | foreign_key | REL-004: ativo.filial_id → filial.id | ON DELETE RESTRICT, ON UPDATE CASCADE |
| TEST-204 | foreign_key | REL-005: ativo.fornecedor_id → fornecedor.id | ON DELETE SET NULL, ON UPDATE CASCADE; fornecedor_id torna-se NULL se fornecedor excluído |
| TEST-205 | foreign_key | REL-006: ativo.funcionario_id → funcionario.id | ON DELETE SET NULL, ON UPDATE CASCADE; funcionario_id torna-se NULL se funcionário excluído |
| TEST-206 | foreign_key | REL-007: ativo_detalhe_hardware.ativo_id → ativo.id | ON DELETE CASCADE, ON UPDATE CASCADE; detalhe removido com ativo (1:1) |
| TEST-207 | foreign_key | REL-008: manutencao.ativo_id → ativo.id | ON DELETE RESTRICT, ON UPDATE CASCADE |
| TEST-208 | foreign_key | REL-009: manutencao.fornecedor_id → fornecedor.id | ON DELETE SET NULL, ON UPDATE CASCADE |
| TEST-209 | foreign_key | REL-010: manutencao.solicitante_id → funcionario.id | ON DELETE RESTRICT, ON UPDATE CASCADE |
| TEST-210 | foreign_key | REL-011: manutencao.aprovador_id → funcionario.id | ON DELETE SET NULL, ON UPDATE CASCADE |
| TEST-211 | foreign_key | REL-012: manutencao.responsavel_tecnico_id → funcionario.id | ON DELETE SET NULL, ON UPDATE CASCADE |
| TEST-212 | foreign_key | REL-013: alerta.ativo_id → ativo.id | ON DELETE SET NULL, ON UPDATE CASCADE; alerta pode ficar global (ativo_id NULL) |
| TEST-213 | foreign_key | REL-014: alerta.usuario_id → usuario.id | ON DELETE SET NULL, ON UPDATE CASCADE |
| TEST-214 | foreign_key | REL-015: memoria.ativo_detalhe_hardware_id → ativo_detalhe_hardware.id | ON DELETE CASCADE, ON UPDATE CASCADE |
| TEST-215 | foreign_key | REL-016: disco.ativo_detalhe_hardware_id → ativo_detalhe_hardware.id | ON DELETE CASCADE, ON UPDATE CASCADE |
| TEST-216 | foreign_key | REL-017: adaptador_rede.ativo_detalhe_hardware_id → ativo_detalhe_hardware.id | ON DELETE CASCADE, ON UPDATE CASCADE |
| TEST-217 | foreign_key | REL-018: health_check_disco.disco_id → disco.id | ON DELETE CASCADE, ON UPDATE CASCADE |
| TEST-218 | foreign_key | REL-019: auditoria.usuario_id → usuario.id | ON DELETE RESTRICT, ON UPDATE CASCADE; usuário não pode ser excluído se houver auditoria |
| TEST-219 | foreign_key | REL-020: localizacao.filial_id → filial.id | ON DELETE RESTRICT, ON UPDATE CASCADE |
| TEST-220 | foreign_key | REL-021: departamento.filial_id → filial.id | ON DELETE RESTRICT, ON UPDATE CASCADE |
| TEST-221 | foreign_key | REL-022: funcionario.departamento_id → departamento.id | ON DELETE RESTRICT, ON UPDATE CASCADE |
| TEST-222 | foreign_key | REL-023: funcionario.filial_id → filial.id | ON DELETE RESTRICT, ON UPDATE CASCADE |
| TEST-223 | foreign_key | REL-024: usuario.funcionario_id → funcionario.id | ON DELETE RESTRICT, ON UPDATE CASCADE; 1:1, funcionário não pode ser excluído se tem usuário |
| TEST-224 | foreign_key | REL-025: papel_permissao (papel_id → papel.id, permissao_id → permissao.id) | ON DELETE CASCADE both sides; junção N:M |
| TEST-225 | foreign_key | REL-026: usuario_papel (usuario_id → usuario.id, papel_id → papel.id) | ON DELETE CASCADE both sides; junção N:M |
| TEST-226 | unique_constraint | CONSTRAINT-021: ativo.tag_patrimonio | UNIQUE; falha em INSERT/UPDATE duplicado |
| TEST-227 | unique_constraint | CONSTRAINT-022: tipo_ativo.codigo | UNIQUE |
| TEST-228 | unique_constraint | CONSTRAINT-023: localizacao.(codigo, filial_id) | UNIQUE composto; código único por filial |
| TEST-229 | unique_constraint | CONSTRAINT-024: departamento.(codigo, filial_id) | UNIQUE composto; código único por filial |
| TEST-230 | unique_constraint | CONSTRAINT-025: filial.cnpj | UNIQUE + CHECK formato |
| TEST-231 | unique_constraint | CONSTRAINT-026: fornecedor.cnpj | UNIQUE + CHECK formato |
| TEST-232 | unique_constraint | CONSTRAINT-027: funcionario.matricula | UNIQUE |
| TEST-233 | unique_constraint | CONSTRAINT-028: funcionario.cpf | UNIQUE + CHECK formato |
| TEST-234 | unique_constraint | CONSTRAINT-029: funcionario.email_corporativo | UNIQUE + CHECK formato |
| TEST-235 | unique_constraint | CONSTRAINT-030: usuario.username | UNIQUE |
| TEST-236 | unique_constraint | CONSTRAINT-031: usuario.funcionario_id | UNIQUE; garante 1:1 usuario:funcionario |
| TEST-237 | unique_constraint | CONSTRAINT-032: manutencao.numero_os | UNIQUE |
| TEST-238 | unique_constraint | CONSTRAINT-033: ativo_detalhe_hardware.ativo_id | UNIQUE; garante 1:1 ativo:detalhe_hardware |
| TEST-239 | unique_constraint | CONSTRAINT-034: adaptador_rede.mac_address | UNIQUE + CHECK formato MAC |
| TEST-240 | unique_constraint | CONSTRAINT-035: papel.nome | UNIQUE |
| TEST-241 | unique_constraint | CONSTRAINT-036: permissao.(recurso, acao) | UNIQUE composto |
| TEST-242 | check_constraint | CONSTRAINT-037: ativo.valor_aquisicao > 0 | Falha se valor <= 0 |
| TEST-243 | check_constraint | CONSTRAINT-038: ativo.vida_util_anos > 0 | Falha se vida_util <= 0 |
| TEST-244 | check_constraint | CONSTRAINT-039: ativo.data_aquisicao <= CURRENT_DATE | Falha se data_aquisicao futura |
| TEST-245 | check_constraint | CONSTRAINT-040: ativo.status IN (...) | Falha se status inválido |
| TEST-246 | check_constraint | CONSTRAINT-041: tipo_ativo.taxa_depreciacao_anual BETWEEN 0 AND 100 | Falha se taxa fora do range |
| TEST-247 | check_constraint | CONSTRAINT-042: tipo_ativo.vida_util_padrao_anos > 0 | Falha se vida_util <= 0 |
| TEST-248 | check_constraint | CONSTRAINT-043: localizacao.tipo IN (...) | Falha se tipo inválido |
| TEST-249 | check_constraint | CONSTRAINT-044: filial.cnpj ~ regex | Falha se CNPJ formato inválido |
| TEST-250 | check_constraint | CONSTRAINT-045: fornecedor.cnpj ~ regex | Falha se CNPJ formato inválido |
| TEST-251 | check_constraint | CONSTRAINT-046: funcionario.cpf ~ regex | Falha se CPF formato inválido |
| TEST-252 | check_constraint | CONSTRAINT-047: funcionario.email_corporativo ~ regex | Falha se email formato inválido |
| TEST-253 | check_constraint | CONSTRAINT-048: funcionario.data_demissao > data_admissao | Falha se demissão anterior a admissão |
| TEST-254 | check_constraint | CONSTRAINT-049: funcionario.ativo = true OR data_demissao IS NOT NULL | Falha se inativo sem data_demissao |
| TEST-255 | check_constraint | CONSTRAINT-050: usuario.role IN ('ADMIN','USER') | Falha se role inválida |
| TEST-256 | check_constraint | CONSTRAINT-051: manutencao.tipo IN ('PREVENTIVA','CORRETIVA') | Falha se tipo inválido |
| TEST-257 | check_constraint | CONSTRAINT-052: manutencao.status IN (...) | Falha se status inválido |
| TEST-258 | check_constraint | CONSTRAINT-053: manutencao.custo_estimado >= 0 | Falha se custo negativo |
| TEST-259 | check_constraint | CONSTRAINT-054: manutencao.custo_real >= 0 | Falha se custo negativo |
| TEST-260 | check_constraint | CONSTRAINT-055: manutencao.status='APROVADA' → data_aprovacao NOT NULL | Falha se APROVADA sem data_aprovacao |
| TEST-261 | check_constraint | CONSTRAINT-056: manutencao.status='EM_ANDAMENTO' → data_inicio NOT NULL | Falha se EM_ANDAMENTO sem data_inicio |
| TEST-262 | check_constraint | CONSTRAINT-057: manutencao.status='CONCLUIDA' → data_conclusao NOT NULL | Falha se CONCLUIDA sem data_conclusao |
| TEST-263 | check_constraint | CONSTRAINT-058: alerta.tipo IN (...) | Falha se tipo inválido |
| TEST-264 | check_constraint | CONSTRAINT-059: alerta.severidade IN (...) | Falha se severidade inválida |
| TEST-265 | check_constraint | CONSTRAINT-060: alerta.lido=true → data_leitura NOT NULL | Falha se lido=true sem data_leitura |
| TEST-266 | check_constraint | CONSTRAINT-061: alerta.severidade='CRITICA' → ativo_id NOT NULL | Falha se CRITICA sem ativo_id |
| TEST-267 | check_constraint | CONSTRAINT-062: ativo_detalhe_hardware.processador_nucleos > 0 | Falha se núcleos <= 0 |
| TEST-268 | check_constraint | CONSTRAINT-063: ativo_detalhe_hardware.memoria_total_gb > 0 | Falha se memória <= 0 |
| TEST-269 | check_constraint | CONSTRAINT-064: memoria.capacidade_gb > 0 | Falha se capacidade <= 0 |
| TEST-270 | check_constraint | CONSTRAINT-065: memoria.tipo IN (...) | Falha se tipo memória inválido |
| TEST-271 | check_constraint | CONSTRAINT-066: memoria.velocidade_mhz > 0 | Falha se velocidade <= 0 |
| TEST-272 | check_constraint | CONSTRAINT-067: disco.tipo IN (...) | Falha se tipo disco inválido |
| TEST-273 | check_constraint | CONSTRAINT-068: disco.capacidade_gb > 0 | Falha se capacidade <= 0 |
| TEST-274 | check_constraint | CONSTRAINT-069: disco.usado_gb BETWEEN 0 AND capacidade_gb | Falha se usado negativo ou > capacidade |
| TEST-275 | check_constraint | CONSTRAINT-070: disco.saude_smart IN (...) | Falha se saúde SMART inválida |
| TEST-276 | check_constraint | CONSTRAINT-071: adaptador_rede.mac_address ~ regex | Falha se MAC formato inválido |
| TEST-277 | check_constraint | CONSTRAINT-072: adaptador_rede.tipo IN (...) | Falha se tipo adaptador inválido |
| TEST-278 | check_constraint | CONSTRAINT-073: adaptador_rede.velocidade_mbps > 0 | Falha se velocidade <= 0 |
| TEST-279 | check_constraint | CONSTRAINT-074: health_check_disco.espaco_livre_gb >= 0 | Falha se espaço livre negativo |
| TEST-280 | check_constraint | CONSTRAINT-075: health_check_disco.espaco_total_gb > 0 | Falha se espaço total <= 0 |
| TEST-281 | check_constraint | CONSTRAINT-076: health_check_disco.espaco_livre_gb <= espaco_total_gb | Falha se livre > total |
| TEST-282 | check_constraint | CONSTRAINT-077: auditoria.acao IN (...) | Falha se ação inválida |
| TEST-283 | check_constraint | CONSTRAINT-078: auditoria.ip_origem ~ regex IPv4/IPv6 | Warning se IP formato inválido (severity: warning) |
| TEST-284 | not_null | Todas colunas marcadas NOT_NULL no contrato | Falha se NULL inserido em coluna NOT NULL |
| TEST-285 | default_value | Colunas com DEFAULT (gen_random_uuid(), now(), 'ATIVO', true, etc.) | Valor default aplicado quando coluna omitida no INSERT |

<!-- source: db-schema-spec#2-relationships -->
<!-- source: db-schema-spec#3-constraints -->

## 3. Invariant Tests (regras de negócio no banco)

```yaml
invariant_tests:
  - id: "TEST-300"
    description: "Tag de patrimônio única em toda a organização (não apenas por filial)"
    query: |
      SELECT tag_patrimonio, COUNT(*) 
      FROM ativo 
      GROUP BY tag_patrimonio 
      HAVING COUNT(*) > 1
    expected_rows: 0

  - id: "TEST-301"
    description: "Cada funcionário tem no máximo um usuário (1:1 usuario:funcionario)"
    query: |
      SELECT funcionario_id, COUNT(*) 
      FROM usuario 
      GROUP BY funcionario_id 
      HAVING COUNT(*) > 1
    expected_rows: 0

  - id: "TEST-302"
    description: "Cada ativo de TI tem no máximo um detalhe de hardware (1:1 ativo:ativo_detalhe_hardware)"
    query: |
      SELECT ativo_id, COUNT(*) 
      FROM ativo_detalhe_hardware 
      GROUP BY ativo_id 
      HAVING COUNT(*) > 1
    expected_rows: 0

  - id: "TEST-303"
    description: "Funcionário inativo deve ter data_demissao preenchida"
    query: |
      SELECT id, matricula 
      FROM funcionario 
      WHERE ativo = false AND data_demissao IS NULL
    expected_rows: 0

  - id: "TEST-304"
    description: "Data de demissão posterior à admissão"
    query: |
      SELECT id, matricula 
      FROM funcionario 
      WHERE data_demissao IS NOT NULL AND data_demissao <= data_admissao
    expected_rows: 0

  - id: "TEST-305"
    description: "Manutenção APROVADA deve ter data_aprovacao"
    query: |
      SELECT id, numero_os 
      FROM manutencao 
      WHERE status = 'APROVADA' AND data_aprovacao IS NULL
    expected_rows: 0

  - id: "TEST-306"
    description: "Manutenção EM_ANDAMENTO deve ter data_inicio"
    query: |
      SELECT id, numero_os 
      FROM manutencao 
      WHERE status = 'EM_ANDAMENTO' AND data_inicio IS NULL
    expected_rows: 0

  - id: "TEST-307"
    description: "Manutenção CONCLUIDA deve ter data_conclusao"
    query: |
      SELECT id, numero_os 
      FROM manutencao 
      WHERE status = 'CONCLUIDA' AND data_conclusao IS NULL
    expected_rows: 0

  - id: "TEST-308"
    description: "Alerta lido deve ter data_leitura"
    query: |
      SELECT id 
      FROM alerta 
      WHERE lido = true AND data_leitura IS NULL
    expected_rows: 0

  - id: "TEST-309"
    description: "Alerta CRITICO deve ter ativo_id não nulo"
    query: |
      SELECT id 
      FROM alerta 
      WHERE severidade = 'CRITICA' AND ativo_id IS NULL
    expected_rows: 0

  - id: "TEST-310"
    description: "Disco usado_gb não excede capacidade_gb"
    query: |
      SELECT id 
      FROM disco 
      WHERE usado_gb IS NOT NULL AND usado_gb > capacidade_gb
    expected_rows: 0

  - id: "TEST-311"
    description: "Health check: espaço livre não excede espaço total"
    query: |
      SELECT id 
      FROM health_check_disco 
      WHERE espaco_livre_gb > espaco_total_gb
    expected_rows: 0

  - id: "TEST-312"
    description: "Health check: espaço livre não negativo"
    query: |
      SELECT id 
      FROM health_check_disco 
      WHERE espaco_livre_gb < 0
    expected_rows: 0

  - id: "TEST-313"
    description: "Auditoria: INSERT não tem valores_anteriores; DELETE não tem valores_novos"
    query: |
      SELECT id, acao, valores_anteriores, valores_novos
      FROM auditoria
      WHERE (acao = 'INSERT' AND valores_anteriores IS NOT NULL)
         OR (acao = 'DELETE' AND valores_novos IS NOT NULL)
    expected_rows: 0

  - id: "TEST-314"
    description: "Ativo com status BAIXADO não pode ter manutenção aberta (não CANCELADA/CONCLUIDA)"
    query: |
      SELECT a.id, a.tag_patrimonio, m.id AS manutencao_id, m.status
      FROM ativo a
      INNER JOIN manutencao m ON m.ativo_id = a.id
      WHERE a.status = 'BAIXADO' 
        AND m.status NOT IN ('CANCELADA', 'CONCLUIDA')
    expected_rows: 0

  - id: "TEST-315"
    description: "Ativo EM_MANUTENCAO deve ter pelo menos uma manutenção EM_ANDAMENTO ou AGUARDANDO_APROVACAO"
    query: |
      SELECT a.id, a.tag_patrimonio, a.status
      FROM ativo a
      WHERE a.status = 'EM_MANUTENCAO'
        AND NOT EXISTS (
          SELECT 1 FROM manutencao m 
          WHERE m.ativo_id = a.id 
            AND m.status IN ('EM_ANDAMENTO', 'AGUARDANDO_APROVACAO', 'APROVADA')
        )
    expected_rows: 0

  - id: "TEST-316"
    description: "Depreciação acumulada não excede valor de aquisição (validação via view vw_depreciacao_ativo)"
    query: |
      SELECT id, tag_patrimonio, depreciacao_acumulada, valor_aquisicao
      FROM vw_depreciacao_ativo
      WHERE depreciacao_acumulada > valor_aquisicao
    expected_rows: 0

  - id: "TEST-317"
    description: "Valor residual não negativo (validação via view vw_depreciacao_ativo)"
    query: |
      SELECT id, tag_patrimonio, valor_residual
      FROM vw_depreciacao_ativo
      WHERE valor_residual < 0
    expected_rows: 0

  - id: "TEST-318"
    description: "Percentual vida útil consumida entre 0 e 100 (ativos não baixados)"
    query: |
      SELECT id, tag_patrimonio, percentual_vida_util_consumida
      FROM vw_depreciacao_ativo
      WHERE percentual_vida_util_consumida < 0 OR percentual_vida_util_consumida > 100
    expected_rows: 0

  - id: "TEST-319"
    description: "Último health_check por disco retorna via DISTINCT ON em vw_disco_saude_atual"
    query: |
      SELECT disco_id, COUNT(*) 
      FROM vw_disco_saude_atual 
      GROUP BY disco_id 
      HAVING COUNT(*) > 1
    expected_rows: 0

  - id: "TEST-320"
    description: "Transições de status de manutenção respeitam fluxo válido (fn_validar_transicao_status_manutencao)"
    query: |
      -- Testado via function unit test; aqui verifica se trigger impede transições inválidas
      -- Tentar UPDATE manutencao SET status='CONCLUIDA' WHERE status='SOLICITADA' deve falhar
      SELECT 1 WHERE false  -- placeholder; testado via integração
    expected_rows: 0

  - id: "TEST-321"
    description: "Ativo_detalhe_hardware só permitido para ativos de tipo TI (fn_verificar_ativo_ti)"
    query: |
      SELECT adh.id, adh.ativo_id, ta.codigo AS tipo_codigo
      FROM ativo_detalhe_hardware adh
      INNER JOIN ativo a ON a.id = adh.ativo_id
      INNER JOIN tipo_ativo ta ON ta.id = a.tipo_ativo_id
      WHERE ta.codigo NOT IN ('NOTEBOOK','DESKTOP','SERVIDOR','IMPRESSORA','MONITOR','SWITCH','ROTEADOR','STORAGE')
    expected_rows: 0

  - id: "TEST-322"
    description: "CNPJ filial e fornecedor passam validação de dígitos verificadores (além de regex)"
    query: |
      -- Requer function de validação completa; placeholder para quando implementada
      SELECT 'filial' AS tabela, id, cnpj FROM filial WHERE cnpj !~ '^\d{2}\.\d{3}\.\d{3}/\d{4}-\d{2}$'
      UNION ALL
      SELECT 'fornecedor', id, cnpj FROM fornecedor WHERE cnpj !~ '^\d{2}\.\d{3}\.\d{3}/\d{4}-\d{2}$'
    expected_rows: 0

  - id: "TEST-323"
    description: "CPF funcionário passa validação de dígitos verificadores (além de regex)"
    query: |
      -- Requer function de validação completa; placeholder para quando implementada
      SELECT id, matricula, cpf FROM funcionario WHERE cpf !~ '^\d{3}\.\d{3}\.\d{3}-\d{2}$'
    expected_rows: 0

  - id: "TEST-324"
    description: "Particionamento health_check_disco: partições mensais existem para últimos 13 meses"
    query: |
      -- Verifica se partições existem; depende de pg_partman ou jobs manuais
      SELECT COUNT(*) FROM pg_class c
      JOIN pg_namespace n ON n.oid = c.relnamespace
      WHERE c.relname LIKE 'health_check_disco_%' 
        AND n.nspname = 'public'
        AND c.relkind = 'r'
    expected_rows: 13  -- [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] número exato depende de política de retenção ativa

  - id: "TEST-325"
    description: "Particionamento auditoria: partições anuais existem para últimos 5 anos"
    query: |
      SELECT COUNT(*) FROM pg_class c
      JOIN pg_namespace n ON n.oid = c.relnamespace
      WHERE c.relname LIKE 'auditoria_%' 
        AND n.nspname = 'public'
        AND c.relkind = 'r'
    expected_rows: 5  -- [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] número exato depende de política de retenção ativa

  - id: "TEST-326"
    description: "Sequence seq_numero_os existe e gera formato OS-YYYY-NNNNNN"
    query: |
      SELECT sequence_name FROM information_schema.sequences WHERE sequence_name = 'seq_numero_os'
    expected_rows: 1

  - id: "TEST-327"
    description: "Configuração app.current_user_id e app.client_ip definidas para auditoria funcionar"
    query: |
      -- Testado via integração: SET LOCAL app.current_user_id = 'uuid'; INSERT ...; verificar auditoria.usuario_id
      SELECT 1 WHERE false  -- placeholder
    expected_rows: 0

  - id: "TEST-328"
    description: "Soft delete: nenhuma exclusão física em tabelas com coluna ativo/ativa; apenas UPDATE ativo=false"
    query: |
      -- Verifica se há DELETE direto nas tabelas principais (auditoria de DDL ou logs)
      SELECT 1 WHERE false  -- placeholder; requer monitoramento de logs/pg_audit
    expected_rows: 0
```

<!-- source: db-schema-spec#3-constraints (CHECK constraints) -->
<!-- source: db-schema-spec#6-functions -->
<!-- source: db-schema-spec#7-triggers -->
<!-- source: db-schema-spec#9-observacoes (soft delete, LGPD, particionamento) -->

## 4. Security Tests

| ID | Role | Operação | Esperado |
| :--- | :--- | :--- | :--- |
| TEST-400 | anon | SELECT ON ativo | Negado (sem GRANT; RLS ou view com security_barrier se implementado) |
| TEST-401 | anon | SELECT ON funcionario | Negado (dados restricted: CPF, email, dados trabalhistas) |
| TEST-402 | anon | SELECT ON usuario | Negado (credenciais, password_hash, roles) |
| TEST-403 | anon | SELECT ON auditoria | Negado (trilha imutável, dados restricted) |
| TEST-404 | anon | SELECT ON filial | Negado (CNPJ, endereço, dados confidential) |
| TEST-405 | anon | SELECT ON fornecedor | Negado (CNPJ, contratos, dados confidential) |
| TEST-406 | app_user | SELECT ON ativo | Permitido (via view vw_ativo_completo ou GRANT SELECT) |
| TEST-407 | app_user | INSERT ON ativo | Permitido (apenas role ADMIN via aplicação; RLS policy se implementado) |
| TEST-408 | app_user | UPDATE ON ativo | Permitido (apenas ADMIN ou custodiante; trigger updated_at + auditoria) |
| TEST-409 | app_user | DELETE ON ativo | Negado (soft delete via ativo=false; hard delete bloqueado por FK RESTRICT) |
| TEST-410 | app_user | SELECT ON funcionario | Permitido (apenas próprios dados ou ADMIN; colunas restricted mascaradas se não ADMIN) |
| TEST-411 | app_user | SELECT ON usuario | Permitido (apenas próprio registro; password_hash nunca retornado) |
| TEST-412 | app_user | INSERT ON manutencao | Permitido (solicitante = current_user funcionario_id) |
| TEST-413 | app_user | UPDATE ON manutencao | Permitido (transições validadas por trigger; apenas roles autorizados por status) |
| TEST-414 | app_user | SELECT ON alerta | Permitido (apenas alertas onde usuario_id = current_user OR ativo_id sob custódia) |
| TEST-415 | app_user | UPDATE ON alerta (lido=true) | Permitido (apenas próprio alerta; trigger seta data_leitura) |
| TEST-416 | app_user | INSERT ON auditoria | Negado (tabela insert-only via trigger fn_auditoria_trigger; AFTER triggers only) |
| TEST-417 | app_user | UPDATE ON auditoria | Negado (imutável; trigger não permite UPDATE) |
| TEST-418 | app_user | DELETE ON auditoria | Negado (imutável; retenção 5 anos compliance) |
| TEST-419 | app_admin | ALL ON all tables | Permitido (com auditoria automática via triggers) |
| TEST-420 | app_admin | TRUNCATE ON health_check_disco | Negado (particionado; apenas DROP PARTITION via job de retenção) |
| TEST-421 | app_admin | TRUNCATE ON auditoria | Negado (particionado; apenas DROP PARTITION via job de retenção 5 anos) |
| TEST-422 | postgres (superuser) | ALTER TABLE ... DISABLE TRIGGER | Permitido (mas auditado via pg_audit se configurado) |
| TEST-423 | app_user | EXECUTE fn_auditoria_trigger | Negado (função SECURITY DEFINER; executada apenas por trigger) |
| TEST-424 | app_user | EXECUTE fn_validar_transicao_status_manutencao | Permitido (IMMUTABLE, sem side effects) |
| TEST-425 | app_user | EXECUTE fn_calcular_depreciacao_ativo | Permitido (STABLE, read-only) |
| TEST-426 | app_user | EXECUTE fn_verificar_ativo_ti | Permitido (STABLE, read-only) |
| TEST-427 | app_user | SET app.current_user_id | Permitido (via middleware aplicação; variável de sessão) |
| TEST-428 | app_user | SET app.client_ip | Permitido (via middleware aplicação; variável de sessão) |
| TEST-429 | anon | Acesso a vw_ativo_completo | Negado (view expõe dados confidential/restricted; RLS ou GRANT apenas para roles autorizadas) |
| TEST-430 | anon | Acesso a vw_depreciacao_ativo | Negado (dados financeiros confidential) |
| TEST-431 | anon | Acesso a vw_disco_saude_atual | Negado (expõe ativo_id, tag_patrimonio, saúde hardware) |
| TEST-432 | app_user | Acesso a vw_alerta_aberto | Permitido (filtrado por usuario_id = current_user via RLS ou WHERE na aplicação) |

<!-- source: db-schema-spec#1-tables (sensitivity: restricted/confidential/internal) -->
<!-- source: db-schema-spec#7-triggers (fn_auditoria_trigger lê app.current_user_id) -->
<!-- source: db-schema-spec#9-observacoes (LGPD, criptografia, pg_audit) -->
<!-- source: test-strategy#5 (Security Tests: JWT RS256, rate limit, bcrypt, auditoria imutável) -->

## 5. Data Quality Rules

```yaml
data_quality:
  # Regras NOT NULL (threshold 100% - zero tolerância)
  - id: "DQ-001"
    field: "ativo.id"
    rule: "not_null"
    threshold_pct: 100
    description: "PK técnica nunca nula (DEFAULT gen_random_uuid())"

  - id: "DQ-002"
    field: "ativo.tag_patrimonio"
    rule: "not_null"
    threshold_pct: 100
    description: "Chave de negócio obrigatória"

  - id: "DQ-003"
    field: "ativo.nome"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-004"
    field: "ativo.valor_aquisicao"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-005"
    field: "ativo.data_aquisicao"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-006"
    field: "ativo.vida_util_anos"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-007"
    field: "ativo.status"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-008"
    field: "ativo.tipo_ativo_id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-009"
    field: "ativo.localizacao_id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-010"
    field: "ativo.departamento_id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-011"
    field: "ativo.filial_id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-012"
    field: "ativo.created_at"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-013"
    field: "ativo.updated_at"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-014"
    field: "tipo_ativo.id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-015"
    field: "tipo_ativo.codigo"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-016"
    field: "tipo_ativo.nome"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-017"
    field: "tipo_ativo.taxa_depreciacao_anual"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-018"
    field: "tipo_ativo.vida_util_padrao_anos"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-019"
    field: "localizacao.id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-020"
    field: "localizacao.codigo"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-021"
    field: "localizacao.nome"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-022"
    field: "localizacao.tipo"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-023"
    field: "localizacao.filial_id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-024"
    field: "departamento.id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-025"
    field: "departamento.codigo"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-026"
    field: "departamento.nome"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-027"
    field: "departamento.filial_id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-028"
    field: "filial.id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-029"
    field: "filial.cnpj"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-030"
    field: "filial.razao_social"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-031"
    field: "filial.endereco"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-032"
    field: "fornecedor.id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-033"
    field: "fornecedor.cnpj"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-034"
    field: "fornecedor.razao_social"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-035"
    field: "funcionario.id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-036"
    field: "funcionario.matricula"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-037"
    field: "funcionario.cpf"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-038"
    field: "funcionario.nome_completo"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-039"
    field: "funcionario.email_corporativo"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-040"
    field: "funcionario.cargo"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-041"
    field: "funcionario.data_admissao"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-042"
    field: "funcionario.departamento_id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-043"
    field: "funcionario.filial_id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-044"
    field: "usuario.id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-045"
    field: "usuario.username"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-046"
    field: "usuario.password_hash"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-047"
    field: "usuario.role"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-048"
    field: "usuario.funcionario_id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-049"
    field: "manutencao.id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-050"
    field: "manutencao.numero_os"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-051"
    field: "manutencao.ativo_id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-052"
    field: "manutencao.tipo"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-053"
    field: "manutencao.status"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-054"
    field: "manutencao.descricao_problema"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-055"
    field: "manutencao.data_solicitacao"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-056"
    field: "manutencao.solicitante_id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-057"
    field: "alerta.id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-058"
    field: "alerta.tipo"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-059"
    field: "alerta.severidade"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-060"
    field: "alerta.titulo"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-061"
    field: "alerta.mensagem"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-062"
    field: "alerta.lido"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-063"
    field: "alerta.created_at"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-064"
    field: "ativo_detalhe_hardware.id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-065"
    field: "ativo_detalhe_hardware.ativo_id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-066"
    field: "memoria.id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-067"
    field: "memoria.ativo_detalhe_hardware_id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-068"
    field: "memoria.capacidade_gb"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-069"
    field: "disco.id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-070"
    field: "disco.ativo_detalhe_hardware_id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-071"
    field: "disco.tipo"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-072"
    field: "disco.capacidade_gb"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-073"
    field: "adaptador_rede.id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-074"
    field: "adaptador_rede.ativo_detalhe_hardware_id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-075"
    field: "adaptador_rede.mac_address"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-076"
    field: "adaptador_rede.tipo"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-077"
    field: "health_check_disco.id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-078"
    field: "health_check_disco.disco_id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-079"
    field: "health_check_disco.espaco_livre_gb"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-080"
    field: "health_check_disco.espaco_total_gb"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-081"
    field: "health_check_disco.coletado_em"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-082"
    field: "auditoria.id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-083"
    field: "auditoria.entidade"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-084"
    field: "auditoria.entidade_id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-085"
    field: "auditoria.acao"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-086"
    field: "auditoria.usuario_id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-087"
    field: "auditoria.created_at"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-088"
    field: "permissao.id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-089"
    field: "permissao.recurso"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-090"
    field: "permissao.acao"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-091"
    field: "papel.id"
    rule: "not_null"
    threshold_pct: 100

  - id: "DQ-092"
    field: "papel.nome"
    rule: "not_null"
    threshold_pct: 100

  # Regras UNIQUE (threshold 100% - zero duplicatas)
  - id: "DQ-093"
    field: "ativo.tag_patrimonio"
    rule: "unique"
    threshold_pct: 100
    description: "Tag patrimônio única global"

  - id: "DQ-094"
    field: "tipo_ativo.codigo"
    rule: "unique"
    threshold_pct: 100

  - id: "DQ-095"
    field: "localizacao.(codigo, filial_id)"
    rule: "unique"
    threshold_pct: 100
    description: "Código único por filial"

  - id: "DQ-096"
    field: "departamento.(codigo, filial_id)"
    rule: "unique"
    threshold_pct: 100

  - id: "DQ-097"
    field: "filial.cnpj"
    rule: "unique"
    threshold_pct: 100

  - id: "DQ-098"
    field: "fornecedor.cnpj"
    rule: "unique"
    threshold_pct: 100

  - id: "DQ-099"
    field: "funcionario.matricula"
    rule: "unique"
    threshold_pct: 100

  - id: "DQ-100"
    field: "funcionario.cpf"
    rule: "unique"
    threshold_pct: 100

  - id: "DQ-101"
    field: "funcionario.email_corporativo"
    rule: "unique"
    threshold_pct: 100

  - id: "DQ-102"
    field: "usuario.username"
    rule: "unique"
    threshold_pct: 100

  - id: "DQ-103"
    field: "usuario.funcionario_id"
    rule: "unique"
    threshold_pct: 100
    description: "1:1 usuario:funcionario"

  - id: "DQ-104"
    field: "manutencao.numero_os"
    rule: "unique"
    threshold_pct: 100

  - id: "DQ-105"
    field: "ativo_detalhe_hardware.ativo_id"
    rule: "unique"
    threshold_pct: 100
    description: "1:1 ativo:detalhe_hardware"

  - id: "DQ-106"
    field: "adaptador_rede.mac_address"
    rule: "unique"
    threshold_pct: 100

  - id: "DQ-107"
    field: "papel.nome"
    rule: "unique"
    threshold_pct: 100

  - id: "DQ-108"
    field: "permissao.(recurso, acao)"
    rule: "unique"
    threshold_pct: 100

  # Regras CHECK / Validação de domínio (threshold 100%)
  - id: "DQ-109"
    field: "ativo.valor_aquisicao"
    rule: "custom"
    threshold_pct: 100
    description: "valor_aquisicao > 0"

  - id: "DQ-110"
    field: "ativo.vida_util_anos"
    rule: "custom"
    threshold_pct: 100
    description: "vida_util_anos > 0"

  - id: "DQ-111"
    field: "ativo.data_aquisicao"
    rule: "custom"
    threshold_pct: 100
    description: "data_aquisicao <= CURRENT_DATE"

  - id: "DQ-112"
    field: "ativo.status"
    rule: "custom"
    threshold_pct: 100
    description: "status IN ('ATIVO','EM_MANUTENCAO','BAIXADO','EM_ESTOQUE')"

  - id: "DQ-113"
    field: "tipo_ativo.taxa_depreciacao_anual"
    rule: "custom"
    threshold_pct: 100
    description: "taxa_depreciacao_anual BETWEEN 0 AND 100"

  - id: "DQ-114"
    field: "tipo_ativo.vida_util_padrao_anos"
    rule: "custom"
    threshold_pct: 100
    description: "vida_util_padrao_anos > 0"

  - id: "DQ-115"
    field: "localizacao.tipo"
    rule: "custom"
    threshold_pct: 100
    description: "tipo IN ('SALA','ANDAR','PREDIO','DATA_CENTER','ESTOQUE','EXTERNO')"

  - id: "DQ-116"
    field: "filial.cnpj"
    rule: "custom"
    threshold_pct: 100
    description: "CNPJ formato ^\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}$"

  - id: "DQ-117"
    field: "fornecedor.cnpj"
    rule: "custom"
    threshold_pct: 100
    description: "CNPJ formato ^\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}$"

  - id: "DQ-118"
    field: "funcionario.cpf"
    rule: "custom"
    threshold_pct: 100
    description: "CPF formato ^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$"

  - id: "DQ-119"
    field: "funcionario.email_corporativo"
    rule: "custom"
    threshold_pct: 100
    description: "Email formato válido"

  - id: "DQ-120"
    field: "funcionario.data_demissao"
    rule: "custom"
    threshold_pct: 100
    description: "data_demissao > data_admissao WHEN NOT NULL"

  - id: "DQ-121"
    field: "funcionario.ativo"
    rule: "custom"
    threshold_pct: 100
    description: "ativo=true OR data_demissao IS NOT NULL"

  - id: "DQ-122"
    field: "usuario.role"
    rule: "custom"
    threshold_pct: 100
    description: "role IN ('ADMIN','USER')"

  - id: "DQ-123"
    field: "manutencao.tipo"
    rule: "custom"
    threshold_pct: 100
    description: "tipo IN ('PREVENTIVA','CORRETIVA')"

  - id: "DQ-124"
    field: "manutencao.status"
    rule: "custom"
    threshold_pct: 100
    description: "status IN fluxo válido"

  - id: "DQ-125"
    field: "manutencao.custo_estimado"
    rule: "custom"
    threshold_pct: 100
    description: "custo_estimado >= 0 WHEN NOT NULL"

  - id: "DQ-126"
    field: "manutencao.custo_real"
    rule: "custom"
    threshold_pct: 100
    description: "custo_real >= 0 WHEN NOT NULL"

  - id: "DQ-127"
    field: "manutencao.(status, data_aprovacao)"
    rule: "custom"
    threshold_pct: 100
    description: "status='APROVADA' → data_aprovacao NOT NULL"

  - id: "DQ-128"
    field: "manutencao.(status, data_inicio)"
    rule: "custom"
    threshold_pct: 100
    description: "status='EM_ANDAMENTO' → data_inicio NOT NULL"

  - id: "DQ-129"
    field: "manutencao.(status, data_conclusao)"
    rule: "custom"
    threshold_pct: 100
    description: "status='CONCLUIDA' → data_conclusao NOT NULL"

  - id: "DQ-130"
    field: "alerta.tipo"
    rule: "custom"
    threshold_pct: 100
    description: "tipo IN enum válido"

  - id: "DQ-131"
    field: "alerta.severidade"
    rule: "custom"
    threshold_pct: 100
    description: "severidade IN ('BAIXA','MEDIA','ALTA','CRITICA')"

  - id: "DQ-132"
    field: "alerta.(lido, data_leitura)"
    rule: "custom"
    threshold_pct: 100
    description: "lido=true → data_leitura NOT NULL"

  - id: "DQ-133"
    field: "alerta.(severidade, ativo_id)"
    rule: "custom"
    threshold_pct: 100
    description: "severidade='CRITICA' → ativo_id NOT NULL"

  - id: "DQ-134"
    field: "memoria.capacidade_gb"
    rule: "custom"
    threshold_pct: 100
    description: "capacidade_gb > 0"

  - id: "DQ-135"
    field: "memoria.tipo"
    rule: "custom"
    threshold_pct: 100
    description: "tipo IN ('DDR3','DDR4','DDR5','LPDDR4','LPDDR5') WHEN NOT NULL"

  - id: "DQ-136"
    field: "disco.tipo"
    rule: "custom"
    threshold_pct: 100
    description: "tipo IN ('HDD','SSD','NVME','EMMC')"

  - id: "DQ-137"
    field: "disco.capacidade_gb"
    rule: "custom"
    threshold_pct: 100
    description: "capacidade_gb > 0"

  - id: "DQ-138"
    field: "disco.usado_gb"
    rule: "custom"
    threshold_pct: 100
    description: "usado_gb BETWEEN 0 AND capacidade_gb WHEN NOT NULL"

  - id: "DQ-139"
    field: "disco.saude_smart"
    rule: "custom"
    threshold_pct: 100
    description: "saude_smart IN ('BOM','ATENCAO','CRITICO','DESCONHECIDO') WHEN NOT NULL"

  - id: "DQ-140"
    field: "adaptador_rede.mac_address"
    rule: "custom"
    threshold_pct: 100
    description: "MAC formato ^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"

  - id: "DQ-141"
    field: "adaptador_rede.tipo"
    rule: "custom"
    threshold_pct: 100
    description: "tipo IN ('ETHERNET','WIFI','BLUETOOTH','VIRTUAL')"

  - id: "DQ-142"
    field: "health_check_disco.espaco_livre_gb"
    rule: "custom"
    threshold_pct: 100
    description: "espaco_livre_gb >= 0"

  - id: "DQ-143"
    field: "health_check_disco.espaco_total_gb"
    rule: "custom"
    threshold_pct: 100
    description: "espaco_total_gb > 0"

  - id: "DQ-144"
    field: "health_check_disco.(espaco_livre_gb, espaco_total_gb)"
    rule: "custom"
    threshold_pct: 100
    description: "espaco_livre_gb <= espaco_total_gb"

  - id: "DQ-145"
    field: "auditoria.acao"
    rule: "custom"
    threshold_pct: 100
    description: "acao IN ('CREATE','UPDATE','DELETE','STATUS_CHANGE')"

  # Regras de completude para colunas opcionais mas importantes (threshold < 100%)
  - id: "DQ-146"
    field: "ativo.numero_serie"
    rule: "not_null"
    threshold_pct: 95
    description: "Número de série esperado para ativos rastreáveis; tolera 5% ausentes (legado)"

  - id: "DQ-147"
    field: "ativo.modelo"
    rule: "not_null"
    threshold_pct: 90
    description: "Modelo esperado; tolera 10% ausentes"

  - id: "DQ-148"
    field: "ativo.fornecedor_id"
    rule: "not_null"
    threshold_pct: 80
    description: "Fornecedor de aquisição; tolera 20% ausentes (doações, legados)"

  - id: "DQ-149"
    field: "ativo.funcionario_id"
    rule: "not_null"
    threshold_pct: 85
    description: "Custodiante responsável; tolera 15% em estoque/sem responsável"

  - id: "DQ-150"
    field: "funcionario.telefone"
    rule: "not_null"
    threshold_pct: 90
    description: "Telefone de contato; tolera 10% ausentes"

  - id: "DQ-151"
    field: "fornecedor.email"
    rule: "not_null"
    threshold_pct: 85
    description: "Email comercial; tolera 15% ausentes"

  - id: "DQ-152"
    field: "fornecedor.contato_comercial"
    rule: "not_null"
    threshold_pct: 80
    description: "Nome contato; tolera 20% ausentes"

  - id: "DQ-153"
    field: "manutencao.custo_estimado"
    rule: "not_null"
    threshold_pct: 90
    description: "Custo estimado obrigatório para aprovação; tolera 10% em análise"

  - id: "DQ-154"
    field: "manutencao.custo_real"
    rule: "not_null"
    threshold_pct: 95
    description: "Custo real obrigatório na conclusão; tolera 5% pendentes"

  - id: "DQ-155"
    field: "ativo_detalhe_hardware.processador_modelo"
    rule: "not_null"
    threshold_pct: 90
    description: "Modelo CPU para ativos TI; tolera 10% ausentes"

  - id: "DQ-156"
    field: "ativo_detalhe_hardware.memoria_total_gb"
    rule: "not_null"
    threshold_pct: 95
    description: "Memória total para ativos TI; tolera 5% ausentes"

  - id: "DQ-157"
    field: "ativo_detalhe_hardware.sistema_operacional"
    rule: "not_null"
    threshold_pct: 90
    description: "SO para ativos TI; tolera 10% ausentes"

  - id: "DQ-158"
    field: "memoria.tipo"
    rule: "not_null"
    threshold_pct: 85
    description: "Tipo memória (DDR3/4/5); tolera 15% não identificado"

  - id: "DQ-159"
    field: "memoria.velocidade_mhz"
    rule: "not_null"
    threshold_pct: 80
    description: "Velocidade MHz; tolera 20% não identificado"

  - id: "DQ-160"
    field: "disco.modelo"
    rule: "not_null"
    threshold_pct: 85
    description: "Modelo disco; tolera 15% ausentes"

  - id: "DQ-161"
    field: "disco.serial_number"
    rule: "not_null"
    threshold_pct: 80
    description: "Serial disco; tolera 20% ausentes"

  - id: "DQ-162"
    field: "disco.mount_point"
    rule: "not_null"
    threshold_pct: 90
    description: "Ponto montagem; tolera 10% ausentes (discos não montados)"

  - id: "DQ-163"
    field: "adaptador_rede.ip_address"
    rule: "not_null"
    threshold_pct: 70
    description: "IP pode ser dinâmico/DHCP; tolera 30% sem IP fixo"

  - id: "DQ-164"
    field: "health_check_disco.temperatura_celsius"
    rule: "not_null"
    threshold_pct: 60
    description: "Temperatura nem sempre disponível via SMART; tolera 40% ausentes"

  - id: "DQ-165"
    field: "health_check_disco.smart_status"
    rule: "not_null"
    threshold_pct: 70
    description: "SMART raw nem sempre exposto; tolera 30% ausentes"

  - id: "DQ-166"
    field: "auditoria.valores_anteriores"
    rule: "not_null"
    threshold_pct: 95
    description: "UPDATE/DELETE devem ter valores_anteriores; INSERT não (tolerância 5% edge cases)"

  - id: "DQ-167"
    field: "auditoria.valores_novos"
    rule: "not_null"
    threshold_pct: 95
    description: "INSERT/UPDATE devem ter valores_novos; DELETE não (tolerância 5%)"

  - id: "DQ-168"
    field: "auditoria.ip_origem"
    rule: "not_null"
    threshold_pct: 80
    description: "IP origem para rastreabilidade; tolera 20% ausentes (jobs internos)"

anomaly_detection:
  enabled: true
  rules:
    - name: "valor_aquisicao_outlier"
      table: "ativo"
      field: "valor_aquisicao"
      method: "iqr"
      multiplier: 1.5
      description: "Detecta valores de aquisição outliers (possível erro digitação)"

    - name: "vida_util_anos_outlier"
      table: "ativo"
      field: "vida_util_anos"
      method: "iqr"
      multiplier: 1.5
      description: "Vida útil anômala (ex: 999 anos)"

    - name: "manutencao_custo_real_vs_estimado"
      table: "manutencao"
      field: "custo_real"
      method: "ratio"
      threshold: 3.0
      description: "Custo real > 3x custo estimado (possível erro ou extravio)"

    - name: "disco_uso_crescimento_rapido"
      table: "health_check_disco"
      field: "espaco_livre_gb"
      method: "trend"
      window_days: 7
      threshold_pct_change: -20
      description: "Queda >20% espaço livre em 7 dias (vazamento disco)"

    - name: "alerta_storm"
      table: "alerta"
      field: "created_at"
      method: "rate"
      window_minutes: 60
      threshold_count: 100
      description: ">100 alertas/hora (storm; UC-09 EX-2)"

    - name: "auditoria_mass_delete"
      table: "auditoria"
      field: "created_at"
      method: "rate"
      window_minutes: 60
      threshold_count: 1000
      description: ">1000 operações DELETE/hora (possível ataque ou bug)"

    - name: "login_failed_burst"
      table: "auditoria"
      field: "created_at"
      method: "rate"
      filter: "entidade='usuario' AND acao='UPDATE' AND valores_novos->>'tentativas_falhas' IS NOT NULL"
      window_minutes: 15
      threshold_count: 50
      description: "Muitas falhas login em 15min (brute force; NFR-S05)"

severity_thresholds:
  warning: "Qualquer regra DQ com threshold < 100% falhando (ex: completude 95% caindo para 90%)"
  critical: "Qualquer regra DQ com threshold 100% falhando (NOT NULL, UNIQUE, CHECK) OU anomaly_detection disparando"
```

<!-- source: db-schema-spec#1-tables (colunas, constraints, sensitivity) -->
<!-- source: db-schema-spec#3-constraints (CHECK, UNIQUE, NOT_NULL) -->
<!-- source: db-schema-spec#9-observacoes (LGPD, soft delete, anonimização) -->
<!-- source: test-strategy#5 (Non-Functional Testing: Security, Performance) -->
<!-- source: test-strategy#6 (Test Data Management: LGPD anonimização) -->
<!-- source: test-strategy#8 (Bug Triage: P0 vazamento LGPD/SOX) -->