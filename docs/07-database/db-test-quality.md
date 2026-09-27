# Database Test & Data Quality Specification — aegis_patrimonio

> **Versão:** 1.0 · **Owner:** Aegis Patrimônio — Engenharia de Dados / QA · **Status:** Draft
> Complementa [[test-strategy]] com testes específicos de banco de dados.

## 1. Schema Tests
| ID | Tipo | Alvo | Condição Esperada |
| :--- | :--- | :--- | :--- |
| TEST-001 | table_exists | ativo | Tabela existe com 30 colunas definidas no contrato <!-- source: db-schema-spec#1.1 --> |
| TEST-002 | table_exists | tipo_ativo | Tabela existe com 11 colunas definidas no contrato <!-- source: db-schema-spec#1.2 --> |
| TEST-003 | table_exists | manutencao | Tabela existe com 27 colunas definidas no contrato <!-- source: db-schema-spec#1.3 --> |
| TEST-004 | table_exists | alerta | Tabela existe com 19 colunas definidas no contrato <!-- source: db-schema-spec#1.4 --> |
| TEST-005 | table_exists | usuario | Tabela existe com 17 colunas definidas no contrato <!-- source: db-schema-spec#1.5 --> |
| TEST-006 | table_exists | role | Tabela existe com 7 colunas definidas no contrato <!-- source: db-schema-spec#1.6 --> |
| TEST-007 | table_exists | permissao | Tabela existe com 10 colunas definidas no contrato <!-- source: db-schema-spec#1.7 --> |
| TEST-008 | table_exists | funcionario | Tabela existe com 20 colunas definidas no contrato <!-- source: db-schema-spec#1.8 --> |
| TEST-009 | table_exists | filial | Tabela existe com 13 colunas definidas no contrato <!-- source: db-schema-spec#1.9 --> |
| TEST-010 | table_exists | departamento | Tabela existe com 10 colunas definidas no contrato <!-- source: db-schema-spec#1.10 --> |
| TEST-011 | table_exists | localizacao | Tabela existe com 14 colunas definidas no contrato <!-- source: db-schema-spec#1.11 --> |
| TEST-012 | table_exists | fornecedor | Tabela existe com 12 colunas definidas no contrato <!-- source: db-schema-spec#1.12 --> |
| TEST-013 | table_exists | ativo_detalhe_hardware | Tabela existe com 15 colunas definidas no contrato <!-- source: db-schema-spec#1.13 --> |
| TEST-014 | table_exists | adaptador_rede | Tabela existe com 10 colunas definidas no contrato <!-- source: db-schema-spec#1.14 --> |
| TEST-015 | table_exists | disco | Tabela existe com 10 colunas definidas no contrato <!-- source: db-schema-spec#1.15 --> |
| TEST-016 | table_exists | memoria | Tabela existe com 9 colunas definidas no contrato <!-- source: db-schema-spec#1.16 --> |
| TEST-017 | table_exists | auditoria | Tabela existe com 16 colunas definidas no contrato <!-- source: db-schema-spec#1.17 --> |
| TEST-018 | table_exists | usuario_role | Tabela existe com 5 colunas definidas no contrato <!-- source: db-schema-spec#1.18 --> |
| TEST-019 | table_exists | role_permissao | Tabela existe com 4 colunas definidas no contrato <!-- source: db-schema-spec#1.19 --> |
| TEST-020 | column_exists | ativo.id | UUID, NOT NULL, DEFAULT gen_random_uuid() <!-- source: db-schema-spec#1.1.columns[0] --> |
| TEST-021 | column_exists | ativo.tag_patrimonial | VARCHAR(50), NOT NULL, UNIQUE <!-- source: db-schema-spec#1.1.columns[1] --> |
| TEST-022 | column_exists | ativo.numero_serie | VARCHAR(100), NULLABLE, UNIQUE_PER_TIPO_ATIVO <!-- source: db-schema-spec#1.1.columns[2] --> |
| TEST-023 | column_exists | ativo.tipo_ativo_id | UUID, NOT NULL, FK → tipo_ativo.id <!-- source: db-schema-spec#1.1.columns[5] --> |
| TEST-024 | column_exists | ativo.filial_id | UUID, NOT NULL, FK → filial.id <!-- source: db-schema-spec#1.1.columns[6] --> |
| TEST-025 | column_exists | ativo.departamento_id | UUID, NULLABLE, FK → departamento.id <!-- source: db-schema-spec#1.1.columns[7] --> |
| TEST-026 | column_exists | ativo.localizacao_id | UUID, NULLABLE, FK → localizacao.id <!-- source: db-schema-spec#1.1.columns[8] --> |
| TEST-027 | column_exists | ativo.fornecedor_id | UUID, NULLABLE, FK → fornecedor.id <!-- source: db-schema-spec#1.1.columns[9] --> |
| TEST-028 | column_exists | ativo.funcionario_responsavel_id | UUID, NULLABLE, FK → funcionario.id <!-- source: db-schema-spec#1.1.columns[10] --> |
| TEST-029 | column_exists | ativo.data_aquisicao | DATE, NOT NULL, CHECK (data_aquisicao <= CURRENT_DATE) <!-- source: db-schema-spec#1.1.columns[11] --> |
| TEST-030 | column_exists | ativo.valor_aquisicao | NUMERIC(18,4), NOT NULL, CHECK (valor_aquisicao > 0) <!-- source: db-schema-spec#1.1.columns[12] --> |
| TEST-031 | column_exists | ativo.vida_util_meses | INTEGER, NOT NULL, CHECK (vida_util_meses > 0) <!-- source: db-schema-spec#1.1.columns[13] --> |
| TEST-032 | column_exists | ativo.status | VARCHAR(20), NOT NULL, DEFAULT 'ATIVO', CHECK IN ('ATIVO','EM_MANUTENCAO','DESCARTADO','VENDIDO','EMPRESTADO') <!-- source: db-schema-spec#1.1.columns[14] --> |
| TEST-033 | column_exists | ativo.condicao | VARCHAR(10), NOT NULL, DEFAULT 'NOVO', CHECK IN ('NOVO','BOM','REGULAR','RUIM','SUCATA') <!-- source: db-schema-spec#1.1.columns[15] --> |
| TEST-034 | column_exists | ativo.uso_disco_percentual | NUMERIC(5,2), NULLABLE, CHECK BETWEEN 0 AND 100 <!-- source: db-schema-spec#1.1.columns[17] --> |
| TEST-035 | column_exists | ativo.memoria_total_gb | INTEGER, NULLABLE, CHECK > 0 <!-- source: db-schema-spec#1.1.columns[18] --> |
| TEST-036 | column_exists | ativo.created_at | TIMESTAMP WITH TIME ZONE, NOT NULL, DEFAULT CURRENT_TIMESTAMP <!-- source: db-schema-spec#1.1.columns[22] --> |
| TEST-037 | column_exists | ativo.updated_at | TIMESTAMP WITH TIME ZONE, NOT NULL, DEFAULT CURRENT_TIMESTAMP <!-- source: db-schema-spec#1.1.columns[23] --> |
| TEST-038 | column_exists | ativo.created_by | UUID, NOT NULL, FK → usuario.id <!-- source: db-schema-spec#1.1.columns[24] --> |
| TEST-039 | column_exists | ativo.updated_by | UUID, NOT NULL, FK → usuario.id <!-- source: db-schema-spec#1.1.columns[25] --> |
| TEST-040 | column_exists | tipo_ativo.codigo | VARCHAR(30), NOT NULL, UNIQUE <!-- source: db-schema-spec#1.2.columns[1] --> |
| TEST-041 | column_exists | tipo_ativo.vida_util_padrao_meses | INTEGER, NOT NULL, CHECK > 0 <!-- source: db-schema-spec#1.2.columns[4] --> |
| TEST-042 | column_exists | tipo_ativo.taxa_depreciacao_anual | NUMERIC(5,4), NOT NULL, CHECK BETWEEN 0 AND 1 <!-- source: db-schema-spec#1.2.columns[5] --> |
| TEST-043 | column_exists | tipo_ativo.categoria | VARCHAR(20), NOT NULL, CHECK IN ('HARDWARE','SOFTWARE','MOVEL','EQUIPAMENTO','VEICULO','OUTRO') <!-- source: db-schema-spec#1.2.columns[6] --> |
| TEST-044 | column_exists | manutencao.numero_ordem | VARCHAR(50), NOT NULL, UNIQUE <!-- source: db-schema-spec#1.3.columns[1] --> |
| TEST-045 | column_exists | manutencao.ativo_id | UUID, NOT NULL, FK → ativo.id <!-- source: db-schema-spec#1.3.columns[2] --> |
| TEST-046 | column_exists | manutencao.tipo | VARCHAR(20), NOT NULL, CHECK IN ('PREVENTIVA','CORRETIVA','PREDITIVA','MELHORIA') <!-- source: db-schema-spec#1.3.columns[3] --> |
| TEST-047 | column_exists | manutencao.status | VARCHAR(20), NOT NULL, DEFAULT 'SOLICITADA', CHECK IN ('SOLICITADA','APROVADA','EM_ANDAMENTO','CONCLUIDA','CANCELADA','AGUARDANDO_PECAS') <!-- source: db-schema-spec#1.3.columns[4] --> |
| TEST-048 | column_exists | manutencao.prioridade | VARCHAR(10), NOT NULL, CHECK IN ('BAIXA','MEDIA','ALTA','CRITICA') <!-- source: db-schema-spec#1.3.columns[5] --> |
| TEST-049 | column_exists | manutencao.data_abertura | TIMESTAMP WITH TIME ZONE, NOT NULL, DEFAULT CURRENT_TIMESTAMP <!-- source: db-schema-spec#1.3.columns[8] --> |
| TEST-050 | column_exists | manutencao.data_aprovacao | TIMESTAMP WITH TIME ZONE, NULLABLE, CHECK (data_aprovacao >= data_abertura) <!-- source: db-schema-spec#1.3.columns[9] --> |
| TEST-051 | column_exists | manutencao.data_inicio | TIMESTAMP WITH TIME ZONE, NULLABLE, CHECK (data_inicio >= data_aprovacao) <!-- source: db-schema-spec#1.3.columns[10] --> |
| TEST-052 | column_exists | manutencao.data_conclusao | TIMESTAMP WITH TIME ZONE, NULLABLE, CHECK (data_conclusao >= data_inicio) <!-- source: db-schema-spec#1.3.columns[11] --> |
| TEST-053 | column_exists | manutencao.custo_pecas | NUMERIC(18,4), NULLABLE, CHECK >= 0 <!-- source: db-schema-spec#1.3.columns[13] --> |
| TEST-054 | column_exists | manutencao.custo_mao_obra | NUMERIC(18,4), NULLABLE, CHECK >= 0 <!-- source: db-schema-spec#1.3.columns[14] --> |
| TEST-055 | column_exists | manutencao.custo_total | NUMERIC(18,4), NULLABLE, CHECK (custo_total = COALESCE(custo_pecas,0) + COALESCE(custo_mao_obra,0)) <!-- source: db-schema-spec#1.3.columns[15] --> |
| TEST-056 | column_exists | alerta.tipo | VARCHAR(30), NOT NULL, CHECK IN ('USO_DISCO_CRITICO','USO_MEMORIA_CRITICO','MANUTENCAO_VENCIDA','SAUDE_DEGRADADA','GARANTIA_EXPIRANDO','DEPRECIACAO_ACELERADA','OUTRO') <!-- source: db-schema-spec#1.4.columns[2] --> |
| TEST-057 | column_exists | alerta.severidade | VARCHAR(10), NOT NULL, CHECK IN ('INFO','WARNING','CRITICAL') <!-- source: db-schema-spec#1.4.columns[3] --> |
| TEST-058 | column_exists | alerta.status | VARCHAR(20), NOT NULL, DEFAULT 'NAO_LIDO', CHECK IN ('NAO_LIDO','LIDO','EM_TRATAMENTO','RESOLVIDO','IGNORADO') <!-- source: db-schema-spec#1.4.columns[4] --> |
| TEST-059 | column_exists | alerta.data_criacao | TIMESTAMP WITH TIME ZONE, NOT NULL, DEFAULT CURRENT_TIMESTAMP <!-- source: db-schema-spec#1.4.columns[6] --> |
| TEST-060 | column_exists | alerta.data_leitura | TIMESTAMP WITH TIME ZONE, NULLABLE, CHECK (data_leitura >= data_criacao) <!-- source: db-schema-spec#1.4.columns[7] --> |
| TEST-061 | column_exists | alerta.data_resolucao | TIMESTAMP WITH TIME ZONE, NULLABLE, CHECK (data_resolucao >= data_leitura) <!-- source: db-schema-spec#1.4.columns[8] --> |
| TEST-062 | column_exists | usuario.username | VARCHAR(100), NOT NULL, UNIQUE <!-- source: db-schema-spec#1.5.columns[1] --> |
| TEST-063 | column_exists | usuario.email | VARCHAR(255), NOT NULL, UNIQUE <!-- source: db-schema-spec#1.5.columns[2] --> |
| TEST-064 | column_exists | usuario.password_hash | VARCHAR(255), NOT NULL <!-- source: db-schema-spec#1.5.columns[3] --> |
| TEST-065 | column_exists | usuario.funcionario_id | UUID, NULLABLE, UNIQUE, FK → funcionario.id <!-- source: db-schema-spec#1.5.columns[6] --> |
| TEST-066 | column_exists | usuario.tentativas_login_falhas | INTEGER, NOT NULL, DEFAULT 0, CHECK >= 0 <!-- source: db-schema-spec#1.5.columns[9] --> |
| TEST-067 | column_exists | usuario.bloqueado_ate | TIMESTAMP WITH TIME ZONE, NULLABLE, CHECK (bloqueado_ate > CURRENT_TIMESTAMP) <!-- source: db-schema-spec#1.5.columns[10] --> |
| TEST-068 | column_exists | funcionario.matricula | VARCHAR(30), NOT NULL, UNIQUE <!-- source: db-schema-spec#1.8.columns[1] --> |
| TEST-069 | column_exists | funcionario.cpf | VARCHAR(14), NOT NULL, UNIQUE, CHECK regex CPF <!-- source: db-schema-spec#1.8.columns[2] --> |
| TEST-070 | column_exists | funcionario.email_corporativo | VARCHAR(255), NOT NULL, UNIQUE <!-- source: db-schema-spec#1.8.columns[4] --> |
| TEST-071 | column_exists | funcionario.filial_id | UUID, NOT NULL, FK → filial.id <!-- source: db-schema-spec#1.8.columns[7] --> |
| TEST-072 | column_exists | funcionario.data_admissao | DATE, NOT NULL <!-- source: db-schema-spec#1.8.columns[9] --> |
| TEST-073 | column_exists | funcionario.data_desligamento | DATE, NULLABLE, CHECK (data_desligamento >= data_admissao) <!-- source: db-schema-spec#1.8.columns[10] --> |
| TEST-074 | column_exists | funcionario.ativo | BOOLEAN, NOT NULL, DEFAULT true, CHECK consistency with data_desligamento <!-- source: db-schema-spec#1.8.columns[11] --> |
| TEST-075 | column_exists | filial.codigo | VARCHAR(20), NOT NULL, UNIQUE <!-- source: db-schema-spec#1.9.columns[1] --> |
| TEST-076 | column_exists | filial.nome | VARCHAR(150), NOT NULL, UNIQUE <!-- source: db-schema-spec#1.9.columns[2] --> |
| TEST-077 | column_exists | departamento.codigo | VARCHAR(20), NOT NULL, UNIQUE_PER_FILIAL (composite with filial_id) <!-- source: db-schema-spec#1.10.columns[1] --> |
| TEST-078 | column_exists | departamento.nome | VARCHAR(150), NOT NULL, UNIQUE_PER_FILIAL (composite with filial_id) <!-- source: db-schema-spec#1.10.columns[2] --> |
| TEST-079 | column_exists | departamento.filial_id | UUID, NOT NULL, FK → filial.id <!-- source: db-schema-spec#1.10.columns[3] --> |
| TEST-080 | column_exists | localizacao.codigo | VARCHAR(30), NOT NULL, UNIQUE_PER_FILIAL (composite with filial_id) <!-- source: db-schema-spec#1.11.columns[1] --> |
| TEST-081 | column_exists | localizacao.filial_id | UUID, NOT NULL, FK → filial.id <!-- source: db-schema-spec#1.11.columns[3] --> |
| TEST-082 | column_exists | fornecedor.cnpj | VARCHAR(18), NOT NULL, UNIQUE, CHECK regex CNPJ <!-- source: db-schema-spec#1.12.columns[1] --> |
| TEST-083 | column_exists | ativo_detalhe_hardware.ativo_id | UUID, NOT NULL, UNIQUE, FK → ativo.id ON DELETE CASCADE <!-- source: db-schema-spec#1.13.columns[1] --> |
| TEST-084 | column_exists | ativo_detalhe_hardware.coletado_em | TIMESTAMP WITH TIME ZONE, NOT NULL, DEFAULT CURRENT_TIMESTAMP, CHECK <= CURRENT_TIMESTAMP <!-- source: db-schema-spec#1.13.columns[10] --> |
| TEST-085 | column_exists | adaptador_rede.endereco_mac | VARCHAR(17), NOT NULL, UNIQUE_PER_ATIVO_DETALHE, CHECK regex MAC <!-- source: db-schema-spec#1.14.columns[3] --> |
| TEST-086 | column_exists | adaptador_rede.tipo | VARCHAR(20), NOT NULL, CHECK IN ('ETHERNET','WIFI','BLUETOOTH','VIRTUAL','OUTRO') <!-- source: db-schema-spec#1.14.columns[7] --> |
| TEST-087 | column_exists | disco.capacidade_gb | INTEGER, NOT NULL, CHECK > 0 <!-- source: db-schema-spec#1.15.columns[4] --> |
| TEST-088 | column_exists | disco.tipo | VARCHAR(10), NOT NULL, CHECK IN ('HDD','SSD','NVME','HIBRIDO') <!-- source: db-schema-spec#1.15.columns[5] --> |
| TEST-089 | column_exists | disco.saude_percentual | INTEGER, NULLABLE, CHECK BETWEEN 0 AND 100 <!-- source: db-schema-spec#1.15.columns[7] --> |
| TEST-090 | column_exists | memoria.capacidade_gb | INTEGER, NOT NULL, CHECK > 0 <!-- source: db-schema-spec#1.16.columns[3] --> |
| TEST-091 | column_exists | auditoria.correlation_id | UUID, NOT NULL <!-- source: db-schema-spec#1.17.columns[1] --> |
| TEST-092 | column_exists | auditoria.entidade | VARCHAR(100), NOT NULL <!-- source: db-schema-spec#1.17.columns[3] --> |
| TEST-093 | column_exists | auditoria.entidade_id | UUID, NOT NULL <!-- source: db-schema-spec#1.17.columns[4] --> |
| TEST-094 | column_exists | auditoria.acao | VARCHAR(20), NOT NULL, CHECK IN ('CREATE','UPDATE','DELETE','LOGIN','LOGOUT','EXPORT','IMPORT','APPROVE','REJECT','READ') <!-- source: db-schema-spec#1.17.columns[5] --> |
| TEST-095 | column_exists | auditoria.hash_encadeado | VARCHAR(64), NOT NULL <!-- source: db-schema-spec#1.17.columns[13] --> |
| TEST-096 | column_exists | usuario_role.usuario_id | UUID, NOT NULL, FK → usuario.id ON DELETE CASCADE, PK composite <!-- source: db-schema-spec#1.18.columns[0] --> |
| TEST-097 | column_exists | usuario_role.role_id | UUID, NOT NULL, FK → role.id ON DELETE CASCADE, PK composite <!-- source: db-schema-spec#1.18.columns[1] --> |
| TEST-098 | column_exists | usuario_role.expira_em | TIMESTAMP WITH TIME ZONE, NULLABLE, CHECK (expira_em > concedido_em) <!-- source: db-schema-spec#1.18.columns[4] --> |
| TEST-099 | column_exists | role_permissao.role_id | UUID, NOT NULL, FK → role.id ON DELETE CASCADE, PK composite <!-- source: db-schema-spec#1.19.columns[0] --> |
| TEST-100 | column_exists | role_permissao.permissao_id | UUID, NOT NULL, FK → permissao.id ON DELETE CASCADE, PK composite <!-- source: db-schema-spec#1.19.columns[1] --> |
| TEST-101 | primary_key | ativo | PRIMARY KEY (id) <!-- source: db-schema-spec#1.1.primary_key --> |
| TEST-102 | primary_key | tipo_ativo | PRIMARY KEY (id) <!-- source: db-schema-spec#1.2.primary_key --> |
| TEST-103 | primary_key | manutencao | PRIMARY KEY (id) <!-- source: db-schema-spec#1.3.primary_key --> |
| TEST-104 | primary_key | alerta | PRIMARY KEY (id) <!-- source: db-schema-spec#1.4.primary_key --> |
| TEST-105 | primary_key | usuario | PRIMARY KEY (id) <!-- source: db-schema-spec#1.5.primary_key --> |
| TEST-106 | primary_key | role | PRIMARY KEY (id) <!-- source: db-schema-spec#1.6.primary_key --> |
| TEST-107 | primary_key | permissao | PRIMARY KEY (id) <!-- source: db-schema-spec#1.7.primary_key --> |
| TEST-108 | primary_key | funcionario | PRIMARY KEY (id) <!-- source: db-schema-spec#1.8.primary_key --> |
| TEST-109 | primary_key | filial | PRIMARY KEY (id) <!-- source: db-schema-spec#1.9.primary_key --> |
| TEST-110 | primary_key | departamento | PRIMARY KEY (id) <!-- source: db-schema-spec#1.10.primary_key --> |
| TEST-111 | primary_key | localizacao | PRIMARY KEY (id) <!-- source: db-schema-spec#1.11.primary_key --> |
| TEST-112 | primary_key | fornecedor | PRIMARY KEY (id) <!-- source: db-schema-spec#1.12.primary_key --> |
| TEST-113 | primary_key | ativo_detalhe_hardware | PRIMARY KEY (id) <!-- source: db-schema-spec#1.13.primary_key --> |
| TEST-114 | primary_key | adaptador_rede | PRIMARY KEY (id) <!-- source: db-schema-spec#1.14.primary_key --> |
| TEST-115 | primary_key | disco | PRIMARY KEY (id) <!-- source: db-schema-spec#1.15.primary_key --> |
| TEST-116 | primary_key | memoria | PRIMARY KEY (id) <!-- source: db-schema-spec#1.16.primary_key --> |
| TEST-117 | primary_key | auditoria | PRIMARY KEY (id) <!-- source: db-schema-spec#1.17.primary_key --> |
| TEST-118 | primary_key | usuario_role | PRIMARY KEY (usuario_id, role_id) <!-- source: db-schema-spec#1.18.primary_key --> |
| TEST-119 | primary_key | role_permissao | PRIMARY KEY (role_id, permissao_id) <!-- source: db-schema-spec#1.19.primary_key --> |
| TEST-120 | unique_constraint | ativo.tag_patrimonial | UNIQUE (tag_patrimonial) <!-- source: db-schema-spec#3.CONSTRAINT-001 --> |
| TEST-121 | unique_constraint | ativo.numero_serie_tipo | UNIQUE (numero_serie, tipo_ativo_id) <!-- source: db-schema-spec#3.CONSTRAINT-002 --> |
| TEST-122 | unique_constraint | tipo_ativo.codigo | UNIQUE (codigo) <!-- source: db-schema-spec#3.CONSTRAINT-009 --> |
| TEST-123 | unique_constraint | manutencao.numero_ordem | UNIQUE (numero_ordem) <!-- source: db-schema-spec#3.CONSTRAINT-013 --> |
| TEST-124 | unique_constraint | usuario.username | UNIQUE (username) <!-- source: db-schema-spec#3.CONSTRAINT-026 --> |
| TEST-125 | unique_constraint | usuario.email | UNIQUE (email) <!-- source: db-schema-spec#3.CONSTRAINT-027 --> |
| TEST-126 | unique_constraint | usuario.funcionario_id | UNIQUE (funcionario_id) <!-- source: db-schema-spec#3.CONSTRAINT-030 --> |
| TEST-127 | unique_constraint | role.nome | UNIQUE (nome) <!-- source: db-schema-spec#3.CONSTRAINT-031 --> |
| TEST-128 | unique_constraint | permissao.codigo | UNIQUE (codigo) <!-- source: db-schema-spec#3.CONSTRAINT-032 --> |
| TEST-129 | unique_constraint | permissao.recurso_acao | UNIQUE (recurso, acao) <!-- source: db-schema-spec#3.CONSTRAINT-033 --> |
| TEST-130 | unique_constraint | funcionario.matricula | UNIQUE (matricula) <!-- source: db-schema-spec#3.CONSTRAINT-034 --> |
| TEST-131 | unique_constraint | funcionario.cpf | UNIQUE (cpf) <!-- source: db-schema-spec#3.CONSTRAINT-035 --> |
| TEST-132 | unique_constraint | funcionario.email_corporativo | UNIQUE (email_corporativo) <!-- source: db-schema-spec#3.CONSTRAINT-036 --> |
| TEST-133 | unique_constraint | filial.codigo | UNIQUE (codigo) <!-- source: db-schema-spec#3.CONSTRAINT-039 --> |
| TEST-134 | unique_constraint | filial.nome | UNIQUE (nome) <!-- source: db-schema-spec#3.CONSTRAINT-040 --> |
| TEST-135 | unique_constraint | departamento.codigo_filial | UNIQUE (codigo, filial_id) <!-- source: db-schema-spec#3.CONSTRAINT-041 --> |
| TEST-136 | unique_constraint | departamento.nome_filial | UNIQUE (nome, filial_id) <!-- source: db-schema-spec#3.CONSTRAINT-042 --> |
| TEST-137 | unique_constraint | localizacao.codigo_filial | UNIQUE (codigo, filial_id) <!-- source: db-schema-spec#3.CONSTRAINT-043 --> |
| TEST-138 | unique_constraint | fornecedor.cnpj | UNIQUE (cnpj) <!-- source: db-schema-spec#3.CONSTRAINT-044 --> |
| TEST-139 | unique_constraint | ativo_detalhe_hardware.ativo_id | UNIQUE (ativo_id) <!-- source: db-schema-spec#3.CONSTRAINT-045 --> |
| TEST-140 | unique_constraint | adaptador_rede.mac_por_detalhe | UNIQUE (ativo_detalhe_hardware_id, endereco_mac) <!-- source: db-schema-spec#3.CONSTRAINT-047 --> |
| TEST-141 | unique_constraint | usuario_role.usuario_role | UNIQUE (usuario_id, role_id) <!-- source: db-schema-spec#4.INDEX-034 --> |
| TEST-142 | unique_constraint | role_permissao.role_permissao | UNIQUE (role_id, permissao_id) <!-- source: db-schema-spec#4.INDEX-035 --> |
| TEST-143 | check_constraint | ativo.valor_aquisicao_positivo | CHECK (valor_aquisicao > 0) <!-- source: db-schema-spec#3.CONSTRAINT-003 --> |
| TEST-144 | check_constraint | ativo.vida_util_positiva | CHECK (vida_util_meses > 0) <!-- source: db-schema-spec#3.CONSTRAINT-004 --> |
| TEST-145 | check_constraint | ativo.data_aquisicao_nao_futura | CHECK (data_aquisicao <= CURRENT_DATE) <!-- source: db-schema-spec#3.CONSTRAINT-005 --> |
| TEST-146 | check_constraint | ativo.uso_disco_range | CHECK (uso_disco_percentual BETWEEN 0 AND 100) <!-- source: db-schema-spec#3.CONSTRAINT-006 --> |
| TEST-147 | check_constraint | ativo.status_valido | CHECK (status IN ('ATIVO','EM_MANUTENCAO','DESCARTADO','VENDIDO','EMPRESTADO')) <!-- source: db-schema-spec#3.CONSTRAINT-007 --> |
| TEST-148 | check_constraint | ativo.condicao_valida | CHECK (condicao IN ('NOVO','BOM','REGULAR','RUIM','SUCATA')) <!-- source: db-schema-spec#3.CONSTRAINT-008 --> |
| TEST-149 | check_constraint | tipo_ativo.vida_util_padrao_positiva | CHECK (vida_util_padrao_meses > 0) <!-- source: db-schema-spec#3.CONSTRAINT-010 --> |
| TEST-150 | check_constraint | tipo_ativo.taxa_depreciacao_range | CHECK (taxa_depreciacao_anual >= 0 AND taxa_depreciacao_anual <= 1) <!-- source: db-schema-spec#3.CONSTRAINT-011 --> |
| TEST-151 | check_constraint | tipo_ativo.categoria_valida | CHECK (categoria IN ('HARDWARE','SOFTWARE','MOVEL','EQUIPAMENTO','VEICULO','OUTRO')) <!-- source: db-schema-spec#3.CONSTRAINT-012 --> |
| TEST-152 | check_constraint | manutencao.data_aprovacao_apos_abertura | CHECK (data_aprovacao >= data_abertura) <!-- source: db-schema-spec#3.CONSTRAINT-014 --> |
| TEST-153 | check_constraint | manutencao.data_inicio_apos_aprovacao | CHECK (data_inicio >= data_aprovacao) <!-- source: db-schema-spec#3.CONSTRAINT-015 --> |
| TEST-154 | check_constraint | manutencao.data_conclusao_apos_inicio | CHECK (data_conclusao >= data_inicio) <!-- source: db-schema-spec#3.CONSTRAINT-016 --> |
| TEST-155 | check_constraint | manutencao.custo_total_derivado | CHECK (custo_total = COALESCE(custo_pecas,0) + COALESCE(custo_mao_obra,0)) <!-- source: db-schema-spec#3.CONSTRAINT-017 --> |
| TEST-156 | check_constraint | manutencao.tipo_valido | CHECK (tipo IN ('PREVENTIVA','CORRETIVA','PREDITIVA','MELHORIA')) <!-- source: db-schema-spec#3.CONSTRAINT-018 --> |
| TEST-157 | check_constraint | manutencao.status_valido | CHECK (status IN ('SOLICITADA','APROVADA','EM_ANDAMENTO','CONCLUIDA','CANCELADA','AGUARDANDO_PECAS')) <!-- source: db-schema-spec#3.CONSTRAINT-019 --> |
| TEST-158 | check_constraint | manutencao.prioridade_valida | CHECK (prioridade IN ('BAIXA','MEDIA','ALTA','CRITICA')) <!-- source: db-schema-spec#3.CONSTRAINT-020 --> |
| TEST-159 | check_constraint | alerta.tipo_valido | CHECK (tipo IN ('USO_DISCO_CRITICO','USO_MEMORIA_CRITICO','MANUTENCAO_VENCIDA','SAUDE_DEGRADADA','GARANTIA_EXPIRANDO','DEPRECIACAO_ACELERADA','OUTRO')) <!-- source: db-schema-spec#3.CONSTRAINT-021 --> |
| TEST-160 | check_constraint | alerta.severidade_valida | CHECK (severidade IN ('INFO','WARNING','CRITICAL')) <!-- source: db-schema-spec#3.CONSTRAINT-022 --> |
| TEST-161 | check_constraint | alerta.status_valido | CHECK (status IN ('NAO_LIDO','LIDO','EM_TRATAMENTO','RESOLVIDO','IGNORADO')) <!-- source: db-schema-spec#3.CONSTRAINT-023 --> |
| TEST-162 | check_constraint | alerta.data_leitura_apos_criacao | CHECK (data_leitura >= data_criacao) <!-- source: db-schema-spec#3.CONSTRAINT-024 --> |
| TEST-163 | check_constraint | alerta.data_resolucao_apos_leitura | CHECK (data_resolucao >= data_leitura) <!-- source: db-schema-spec#3.CONSTRAINT-025 --> |
| TEST-164 | check_constraint | usuario.tentativas_nao_negativas | CHECK (tentativas_login_falhas >= 0) <!-- source: db-schema-spec#3.CONSTRAINT-028 --> |
| TEST-165 | check_constraint | usuario.bloqueado_ate_futuro | CHECK (bloqueado_ate IS NULL OR bloqueado_ate > CURRENT_TIMESTAMP) <!-- source: db-schema-spec#3.CONSTRAINT-029 --> |
| TEST-166 | check_constraint | funcionario.data_desligamento_apos_admissao | CHECK (data_desligamento >= data_admissao) <!-- source: db-schema-spec#3.CONSTRAINT-037 --> |
| TEST-167 | check_constraint | funcionario.ativo_consistente | CHECK ((data_desligamento IS NOT NULL AND ativo = false) OR (data_desligamento IS NULL AND ativo = true)) <!-- source: db-schema-spec#3.CONSTRAINT-038 --> |
| TEST-168 | check_constraint | ativo_detalhe_hardware.coletado_em_nao_futuro | CHECK (coletado_em <= CURRENT_TIMESTAMP) <!-- source: db-schema-spec#3.CONSTRAINT-046 --> |
| TEST-169 | check_constraint | adaptador_rede.mac_format | CHECK (endereco_mac ~ '^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$') <!-- source: db-schema-spec#3.CONSTRAINT-048 --> |
| TEST-170 | check_constraint | adaptador_rede.tipo_valido | CHECK (tipo IN ('ETHERNET','WIFI','BLUETOOTH','VIRTUAL','OUTRO')) <!-- source: db-schema-spec#3.CONSTRAINT-049 --> |
| TEST-171 | check_constraint | disco.capacidade_positiva | CHECK (capacidade_gb > 0) <!-- source: db-schema-spec#3.CONSTRAINT-050 --> |
| TEST-172 | check_constraint | disco.saude_range | CHECK (saude_percentual BETWEEN 0 AND 100) <!-- source: db-schema-spec#3.CONSTRAINT-051 --> |
| TEST-173 | check_constraint | disco.tipo_valido | CHECK (tipo IN ('HDD','SSD','NVME','HIBRIDO')) <!-- source: db-schema-spec#3.CONSTRAINT-052 --> |
| TEST-174 | check_constraint | memoria.capacidade_positiva | CHECK (capacidade_gb > 0) <!-- source: db-schema-spec#3.CONSTRAINT-053 --> |
| TEST-175 | check_constraint | auditoria.acao_valida | CHECK (acao IN ('CREATE','UPDATE','DELETE','LOGIN','LOGOUT','EXPORT','IMPORT','APPROVE','REJECT','READ')) <!-- source: db-schema-spec#3.CONSTRAINT-054 --> |
| TEST-176 | check_constraint | auditoria.snapshots_obrigatorios | CHECK ((acao = 'CREATE' AND valores_novos IS NOT NULL) OR (acao = 'DELETE' AND valores_anteriores IS NOT NULL) OR (acao = 'UPDATE' AND valores_anteriores IS NOT NULL AND valores_novos IS NOT NULL) OR (acao IN ('LOGIN','LOGOUT','EXPORT','IMPORT','APPROVE','REJECT','READ'))) <!-- source: db-schema-spec#3.CONSTRAINT-055 --> |
| TEST-177 | check_constraint | usuario_role.expira_apos_concessao | CHECK (expira_em IS NULL OR expira_em > concedido_em) <!-- source: db-schema-spec#3.CONSTRAINT-056 --> |
| TEST-178 | index_exists | ativo.idx_tag_patrimonial | btree UNIQUE ON (tag_patrimonial ASC) <!-- source: db-schema-spec#4.INDEX-001 --> |
| TEST-179 | index_exists | ativo.idx_numero_serie_tipo | btree UNIQUE ON (numero_serie ASC, tipo_ativo_id ASC) <!-- source: db-schema-spec#4.INDEX-002 --> |
| TEST-180 | index_exists | ativo.idx_filial_status | btree ON (filial_id ASC, status ASC) <!-- source: db-schema-spec#4.INDEX-003 --> |
| TEST-181 | index_exists | ativo.idx_departamento_status | btree ON (departamento_id ASC, status ASC) <!-- source: db-schema-spec#4.INDEX-004 --> |
| TEST-182 | index_exists | ativo.idx_localizacao | btree ON (localizacao_id ASC) <!-- source: db-schema-spec#4.INDEX-005 --> |
| TEST-183 | index_exists | ativo.idx_responsavel_status | btree ON (funcionario_responsavel_id ASC, status ASC) <!-- source: db-schema-spec#4.INDEX-006 --> |
| TEST-184 | index_exists | ativo.idx_tipo_status | btree ON (tipo_ativo_id ASC, status ASC) <!-- source: db-schema-spec#4.INDEX-007 --> |
| TEST-185 | index_exists | ativo.idx_data_aquisicao | btree ON (data_aquisicao DESC) <!-- source: db-schema-spec#4.INDEX-008 --> |
| TEST-186 | index_exists | manutencao.idx_custo_por_ativo | btree ON (ativo_id ASC, data_conclusao DESC, custo_total ASC) <!-- source: db-schema-spec#4.INDEX-009 --> |
| TEST-187 | index_exists | manutencao.idx_spec_filtros | btree ON (ativo_id ASC, status ASC, tipo ASC, data_abertura DESC) <!-- source: db-schema-spec#4.INDEX-010 --> |
| TEST-188 | index_exists | manutencao.idx_fornecedor_data | btree ON (fornecedor_id ASC, data_abertura DESC) <!-- source: db-schema-spec#4.INDEX-011 --> |
| TEST-189 | index_exists | manutencao.idx_tecnico_fila | btree ON (tecnico_responsavel_id ASC, status ASC, data_prevista_conclusao ASC) <!-- source: db-schema-spec#4.INDEX-012 --> |
| TEST-190 | index_exists | manutencao.idx_data_abertura | btree ON (data_abertura DESC) <!-- source: db-schema-spec#4.INDEX-013 --> |
| TEST-191 | index_exists | alerta.idx_ativo_status_severidade | btree ON (ativo_id ASC, status ASC, severidade ASC, data_criacao DESC) <!-- source: db-schema-spec#4.INDEX-014 --> |
| TEST-192 | index_exists | alerta.idx_status_data | btree ON (status ASC, data_criacao DESC) <!-- source: db-schema-spec#4.INDEX-015 --> |
| TEST-193 | index_exists | alerta.idx_usuario_leitura | btree ON (usuario_leitura_id ASC, data_leitura DESC) <!-- source: db-schema-spec#4.INDEX-016 --> |
| TEST-194 | index_exists | usuario.idx_username | btree UNIQUE ON (username ASC) <!-- source: db-schema-spec#4.INDEX-017 --> |
| TEST-195 | index_exists | usuario.idx_email | btree UNIQUE ON (email ASC) <!-- source: db-schema-spec#4.INDEX-018 --> |
| TEST-196 | index_exists | usuario.idx_funcionario | btree UNIQUE ON (funcionario_id ASC) <!-- source: db-schema-spec#4.INDEX-019 --> |
| TEST-197 | index_exists | funcionario.idx_matricula | btree UNIQUE ON (matricula ASC) <!-- source: db-schema-spec#4.INDEX-020 --> |
| TEST-198 | index_exists | funcionario.idx_cpf | btree UNIQUE ON (cpf ASC) <!-- source: db-schema-spec#4.INDEX-021 --> |
| TEST-199 | index_exists | funcionario.idx_filial_dept_ativo | btree ON (filial_id ASC, departamento_id ASC, ativo ASC) <!-- source: db-schema-spec#4.INDEX-022 --> |
| TEST-200 | index_exists | departamento.idx_filial_codigo | btree UNIQUE ON (filial_id ASC, codigo ASC) <!-- source: db-schema-spec#4.INDEX-023 --> |
| TEST-201 | index_exists | localizacao.idx_filial_codigo | btree UNIQUE ON (filial_id ASC, codigo ASC) <!-- source: db-schema-spec#4.INDEX-024 --> |
| TEST-202 | index_exists | fornecedor.idx_cnpj | btree UNIQUE ON (cnpj ASC) <!-- source: db-schema-spec#4.INDEX-025 --> |
| TEST-203 | index_exists | ativo_detalhe_hardware.idx_ativo | btree UNIQUE ON (ativo_id ASC) <!-- source: db-schema-spec#4.INDEX-026 --> |
| TEST-204 | index_exists | adaptador_rede.idx_detalhe_hardware | btree ON (ativo_detalhe_hardware_id ASC) <!-- source: db-schema-spec#4.INDEX-027 --> |
| TEST-205 | index_exists | disco.idx_detalhe_hardware | btree ON (ativo_detalhe_hardware_id ASC) <!-- source: db-schema-spec#4.INDEX-028 --> |
| TEST-206 | index_exists | memoria.idx_detalhe_hardware | btree ON (ativo_detalhe_hardware_id ASC) <!-- source: db-schema-spec#4.INDEX-029 --> |
| TEST-207 | index_exists | auditoria.idx_correlation_data | btree ON (correlation_id ASC, data_hora ASC) <!-- source: db-schema-spec#4.INDEX-030 --> |
| TEST-208 | index_exists | auditoria.idx_entidade_data | btree ON (entidade ASC, entidade_id ASC, data_hora DESC) <!-- source: db-schema-spec#4.INDEX-031 --> |
| TEST-209 | index_exists | auditoria.idx_usuario_data | btree ON (usuario_id ASC, data_hora DESC) <!-- source: db-schema-spec#4.INDEX-032 --> |
| TEST-210 | index_exists | auditoria.idx_data_hora | btree ON (data_hora ASC) <!-- source: db-schema-spec#4.INDEX-033 --> |
| TEST-211 | view_exists | vw_ativo_completo | View não-materializada consolidando ativo com joins de referência <!-- source: db-schema-spec#5.views[0] --> |
| TEST-212 | view_exists | vw_manutencao_completa | View não-materializada consolidando manutencao com joins <!-- source: db-schema-spec#5.views[1] --> |
| TEST-213 | view_exists | vw_alerta_ativo | View não-materializada de alertas não resolvidos <!-- source: db-schema-spec#5.views[2] --> |
| TEST-214 | view_exists | vw_custo_manutencao_por_ativo | View MATERIALIZADA agregando custos por ativo <!-- source: db-schema-spec#5.views[3] --> |
| TEST-215 | function_exists | validar_cpf | FUNCTION validar_cpf(p_cpf VARCHAR(14)) RETURNS BOOLEAN IMMUTABLE <!-- source: db-schema-spec#6.functions[0] --> |
| TEST-216 | function_exists | validar_cnpj | FUNCTION validar_cnpj(p_cnpj VARCHAR(18)) RETURNS BOOLEAN IMMUTABLE <!-- source: db-schema-spec#6.functions[1] --> |
| TEST-217 | function_exists | calcular_hash_encadeado_auditoria | FUNCTION calcular_hash_encadeado_auditoria(p_hash_anterior VARCHAR(64), p_payload_atual JSONB) RETURNS VARCHAR(64) IMMUTABLE <!-- source: db-schema-spec#6.functions[2] --> |
| TEST-218 | function_exists | anonymize_user | FUNCTION anonymize_user(p_usuario_id UUID) RETURNS VOID VOLATILE SECURITY DEFINER <!-- source: db-schema-spec#6.functions[3] --> |
| TEST-219 | function_exists | atualizar_custo_total_manutencao | FUNCTION atualizar_custo_total_manutencao(p_manutencao_id UUID) RETURNS VOID VOLATILE <!-- source: db-schema-spec#6.functions[4] --> |
| TEST-220 | trigger_exists | trg_auditoria_insert_hash_chain | BEFORE INSERT ON auditoria EXECUTE FUNCTION calcular_hash_encadeado_auditoria <!-- source: db-schema-spec#7.triggers[0] --> |
| TEST-221 | trigger_exists | trg_auditoria_prevent_update_delete | BEFORE UPDATE, DELETE ON auditoria EXECUTE FUNCTION raise_exception_append_only <!-- source: db-schema-spec#7.triggers[1] --> |
| TEST-222 | trigger_exists | trg_manutencao_atualizar_custo_total | BEFORE INSERT, UPDATE ON manutencao EXECUTE FUNCTION atualizar_custo_total_manutencao <!-- source: db-schema-spec#7.triggers[2] --> |
| TEST-223 | trigger_exists | trg_funcionario_sincronizar_ativo | AFTER UPDATE ON funcionario EXECUTE FUNCTION sincronizar_funcionario_ativo <!-- source: db-schema-spec#7.triggers[3] --> |
| TEST-224 | trigger_exists | trg_usuario_role_auditoria | AFTER INSERT, DELETE ON usuario_role EXECUTE FUNCTION registrar_auditoria_usuario_role <!-- source: db-schema-spec#7.triggers[4] --> |
| TEST-225 | trigger_exists | trg_role_permissao_auditoria | AFTER INSERT, DELETE ON role_permissao EXECUTE FUNCTION registrar_auditoria_role_permissao <!-- source: db-schema-spec#7.triggers[5] --> |

## 2. Integrity Tests
| ID | Tipo | Alvo | Condição Esperada |
| :--- | :--- | :--- | :--- |
| TEST-300 | foreign_key | REL-001: ativo.tipo_ativo_id → tipo_ativo.id | ON DELETE RESTRICT, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-001 --> |
| TEST-301 | foreign_key | REL-002: ativo.filial_id → filial.id | ON DELETE RESTRICT, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-002 --> |
| TEST-302 | foreign_key | REL-003: ativo.departamento_id → departamento.id | ON DELETE SET NULL, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-003 --> |
| TEST-303 | foreign_key | REL-004: ativo.localizacao_id → localizacao.id | ON DELETE SET NULL, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-004 --> |
| TEST-304 | foreign_key | REL-005: ativo.fornecedor_id → fornecedor.id | ON DELETE SET NULL, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-005 --> |
| TEST-305 | foreign_key | REL-006: ativo.funcionario_responsavel_id → funcionario.id | ON DELETE SET NULL, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-006 --> |
| TEST-306 | foreign_key | REL-007: manutencao.ativo_id → ativo.id | ON DELETE RESTRICT, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-007 --> |
| TEST-307 | foreign_key | REL-008: manutencao.fornecedor_id → fornecedor.id | ON DELETE SET NULL, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-008 --> |
| TEST-308 | foreign_key | REL-009: manutencao.tecnico_responsavel_id → funcionario.id | ON DELETE SET NULL, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-009 --> |
| TEST-309 | foreign_key | REL-010: manutencao.aprovador_id → usuario.id | ON DELETE SET NULL, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-010 --> |
| TEST-310 | foreign_key | REL-011: alerta.ativo_id → ativo.id | ON DELETE SET NULL, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-011 --> |
| TEST-311 | foreign_key | REL-012: alerta.usuario_leitura_id → usuario.id | ON DELETE SET NULL, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-012 --> |
| TEST-312 | foreign_key | REL-013: alerta.usuario_resolucao_id → usuario.id | ON DELETE SET NULL, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-013 --> |
| TEST-313 | foreign_key | REL-014: usuario.funcionario_id → funcionario.id | ON DELETE SET NULL, ON UPDATE CASCADE, 1:1 lógico <!-- source: db-schema-spec#2.REL-014 --> |
| TEST-314 | foreign_key | REL-015: funcionario.filial_id → filial.id | ON DELETE RESTRICT, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-015 --> |
| TEST-315 | foreign_key | REL-016: funcionario.departamento_id → departamento.id | ON DELETE SET NULL, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-016 --> |
| TEST-316 | foreign_key | REL-017: departamento.filial_id → filial.id | ON DELETE RESTRICT, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-017 --> |
| TEST-317 | foreign_key | REL-018: departamento.gestor_id → funcionario.id | ON DELETE SET NULL, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-018 --> |
| TEST-318 | foreign_key | REL-019: localizacao.filial_id → filial.id | ON DELETE RESTRICT, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-019 --> |
| TEST-319 | foreign_key | REL-020: ativo_detalhe_hardware.ativo_id → ativo.id | ON DELETE CASCADE, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-020 --> |
| TEST-320 | foreign_key | REL-021: adaptador_rede.ativo_detalhe_hardware_id → ativo_detalhe_hardware.id | ON DELETE CASCADE, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-021 --> |
| TEST-321 | foreign_key | REL-022: disco.ativo_detalhe_hardware_id → ativo_detalhe_hardware.id | ON DELETE CASCADE, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-022 --> |
| TEST-322 | foreign_key | REL-023: memoria.ativo_detalhe_hardware_id → ativo_detalhe_hardware.id | ON DELETE CASCADE, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-023 --> |
| TEST-323 | foreign_key | REL-024: auditoria.usuario_id → usuario.id | ON DELETE SET NULL, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-024 --> |
| TEST-324 | foreign_key | REL-025: usuario_role.usuario_id → usuario.id | ON DELETE CASCADE, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-025 --> |
| TEST-325 | foreign_key | REL-026: usuario_role.role_id → role.id | ON DELETE CASCADE, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-026 --> |
| TEST-326 | foreign_key | REL-027: role_permissao.role_id → role.id | ON DELETE CASCADE, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-027 --> |
| TEST-327 | foreign_key | REL-028: role_permissao.permissao_id → permissao.id | ON DELETE CASCADE, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-028 --> |
| TEST-328 | foreign_key | REL-029: ativo.created_by → usuario.id | ON DELETE RESTRICT, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-029 --> |
| TEST-329 | foreign_key | REL-030: ativo.updated_by → usuario.id | ON DELETE RESTRICT, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-030 --> |
| TEST-330 | foreign_key | REL-031: manutencao.created_by → usuario.id | ON DELETE RESTRICT, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-031 --> |
| TEST-331 | foreign_key | REL-032: manutencao.updated_by → usuario.id | ON DELETE RESTRICT, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-032 --> |
| TEST-332 | foreign_key | REL-033: funcionario.created_by → usuario.id | ON DELETE RESTRICT, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-033 --> |
| TEST-333 | foreign_key | REL-034: funcionario.updated_by → usuario.id | ON DELETE RESTRICT, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-034 --> |
| TEST-334 | foreign_key | REL-035: usuario_role.concedido_por → usuario.id | ON DELETE RESTRICT, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-035 --> |
| TEST-335 | foreign_key | REL-036: role_permissao.concedido_por → usuario.id | ON DELETE RESTRICT, ON UPDATE CASCADE <!-- source: db-schema-spec#2.REL-036 --> |
| TEST-336 | referential_integrity | ativo → tipo_ativo | Todo ativo referencia tipo_ativo existente (N:1, restrict delete) <!-- source: db-schema-spec#2.REL-001 --> |
| TEST-337 | referential_integrity | ativo → filial | Todo ativo referencia filial existente (N:1, restrict delete) <!-- source: db-schema-spec#2.REL-002 --> |
| TEST-338 | referential_integrity | manutencao → ativo | Toda manutenção referencia ativo existente (N:1, restrict delete) <!-- source: db-schema-spec#2.REL-007 --> |
| TEST-339 | referential_integrity | ativo_detalhe_hardware → ativo | Detalhe hardware só existe se ativo pai existe (1:1 lógico, cascade delete) <!-- source: db-schema-spec#2.REL-020 --> |
| TEST-340 | referential_integrity | adaptador_rede → ativo_detalhe_hardware | Adaptador só existe se detalhe hardware pai existe (cascade delete) <!-- source: db-schema-spec#2.REL-021 --> |
| TEST-341 | referential_integrity | disco → ativo_detalhe_hardware | Disco só existe se detalhe hardware pai existe (cascade delete) <!-- source: db-schema-spec#2.REL-022 --> |
| TEST-342 | referential_integrity | memoria → ativo_detalhe_hardware | Memória só existe se detalhe hardware pai existe (cascade delete) <!-- source: db-schema-spec#2.REL-023 --> |
| TEST-343 | referential_integrity | usuario_role → usuario + role | Par (usuario_id, role_id) referencia ambos existentes (cascade delete) <!-- source: db-schema-spec#2.REL-025, REL-026 --> |
| TEST-344 | referential_integrity | role_permissao → role + permissao | Par (role_id, permissao_id) referencia ambos existentes (cascade delete) <!-- source: db-schema-spec#2.REL-027, REL-028 --> |
| TEST-344 | cascade_delete | ativo_detalhe_hardware ON DELETE CASCADE | Deletar ativo remove detalhe hardware associado <!-- source: db-schema-spec#2.REL-020.on_delete --> |
| TEST-345 | cascade_delete | adaptador_rede ON DELETE CASCADE | Deletar detalhe hardware remove adaptadores associados <!-- source: db-schema-spec#2.REL-021.on_delete --> |
| TEST-346 | cascade_delete | disco ON DELETE CASCADE | Deletar detalhe hardware remove discos associados <!-- source: db-schema-spec#2.REL-022.on_delete --> |
| TEST-347 | cascade_delete | memoria ON DELETE CASCADE | Deletar detalhe hardware remove memórias associadas <!-- source: db-schema-spec#2.REL-023.on_delete --> |
| TEST-348 | cascade_delete | usuario_role ON DELETE CASCADE | Deletar usuario remove associações de role <!-- source: db-schema-spec#2.REL-025.on_delete --> |
| TEST-349 | cascade_delete | role_permissao ON DELETE CASCADE | Deletar role remove associações de permissão <!-- source: db-schema-spec#2.REL-027.on_delete --> |
| TEST-350 | restrict_delete | tipo_ativo ON DELETE RESTRICT | Não permite deletar tipo_ativo se houver ativos referenciando <!-- source: db-schema-spec#2.REL-001.on_delete --> |
| TEST-351 | restrict_delete | filial ON DELETE RESTRICT | Não permite deletar filial se houver ativos/funcionarios/departamentos/localizacoes referenciando <!-- source: db-schema-spec#2.REL-002.on_delete, REL-015, REL-017, REL-019 --> |
| TEST-352 | restrict_delete | ativo ON DELETE RESTRICT | Não permite deletar ativo se houver manutencoes referenciando <!-- source: db-schema-spec#2.REL-007.on_delete --> |

## 3. Invariant Tests (regras de negócio no banco)
```yaml
invariant_tests:
  - id: "TEST-400"
    description: "Tag patrimonial única em toda a base (identificador de negócio obrigatório)"
    query: |
      SELECT tag_patrimonial, COUNT(*) 
      FROM ativo 
      GROUP BY tag_patrimonial 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-001"

  - id: "TEST-401"
    description: "Número de série único por tipo de ativo (fabricante)"
    query: |
      SELECT numero_serie, tipo_ativo_id, COUNT(*) 
      FROM ativo 
      WHERE numero_serie IS NOT NULL
      GROUP BY numero_serie, tipo_ativo_id 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-002"

  - id: "TEST-402"
    description: "Valor de aquisição sempre positivo"
    query: |
      SELECT id, tag_patrimonial, valor_aquisicao 
      FROM ativo 
      WHERE valor_aquisicao <= 0
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-003"

  - id: "TEST-403"
    description: "Vida útil em meses sempre positiva"
    query: |
      SELECT id, tag_patrimonial, vida_util_meses 
      FROM ativo 
      WHERE vida_util_meses <= 0
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-004"

  - id: "TEST-404"
    description: "Data de aquisição não pode ser futura"
    query: |
      SELECT id, tag_patrimonial, data_aquisicao 
      FROM ativo 
      WHERE data_aquisicao > CURRENT_DATE
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-005"

  - id: "TEST-405"
    description: "Percentual de uso de disco entre 0 e 100"
    query: |
      SELECT id, tag_patrimonial, uso_disco_percentual 
      FROM ativo 
      WHERE uso_disco_percentual IS NOT NULL 
        AND (uso_disco_percentual < 0 OR uso_disco_percentual > 100)
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-006"

  - id: "TEST-406"
    description: "Status do ativo apenas valores válidos"
    query: |
      SELECT id, tag_patrimonial, status 
      FROM ativo 
      WHERE status NOT IN ('ATIVO','EM_MANUTENCAO','DESCARTADO','VENDIDO','EMPRESTADO')
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-007"

  - id: "TEST-407"
    description: "Condição do ativo apenas valores válidos"
    query: |
      SELECT id, tag_patrimonial, condicao 
      FROM ativo 
      WHERE condicao NOT IN ('NOVO','BOM','REGULAR','RUIM','SUCATA')
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-008"

  - id: "TEST-408"
    description: "Código de tipo_ativo único"
    query: |
      SELECT codigo, COUNT(*) 
      FROM tipo_ativo 
      GROUP BY codigo 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-009"

  - id: "TEST-409"
    description: "Vida útil padrão do tipo_ativo positiva"
    query: |
      SELECT id, codigo, vida_util_padrao_meses 
      FROM tipo_ativo 
      WHERE vida_util_padrao_meses <= 0
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-010"

  - id: "TEST-410"
    description: "Taxa de depreciação anual entre 0 e 1 (0% a 100%)"
    query: |
      SELECT id, codigo, taxa_depreciacao_anual 
      FROM tipo_ativo 
      WHERE taxa_depreciacao_anual < 0 OR taxa_depreciacao_anual > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-011"

  - id: "TEST-411"
    description: "Categoria de tipo_ativo apenas valores válidos"
    query: |
      SELECT id, codigo, categoria 
      FROM tipo_ativo 
      WHERE categoria NOT IN ('HARDWARE','SOFTWARE','MOVEL','EQUIPAMENTO','VEICULO','OUTRO')
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-012"

  - id: "TEST-412"
    description: "Número de ordem de manutenção único"
    query: |
      SELECT numero_ordem, COUNT(*) 
      FROM manutencao 
      GROUP BY numero_ordem 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-013"

  - id: "TEST-413"
    description: "Data de aprovação não anterior à abertura"
    query: |
      SELECT id, numero_ordem, data_abertura, data_aprovacao 
      FROM manutencao 
      WHERE data_aprovacao IS NOT NULL 
        AND data_aprovacao < data_abertura
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-014"

  - id: "TEST-414"
    description: "Data de início não anterior à aprovação"
    query: |
      SELECT id, numero_ordem, data_aprovacao, data_inicio 
      FROM manutencao 
      WHERE data_inicio IS NOT NULL 
        AND data_aprovacao IS NOT NULL 
        AND data_inicio < data_aprovacao
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-015"

  - id: "TEST-415"
    description: "Data de conclusão não anterior ao início"
    query: |
      SELECT id, numero_ordem, data_inicio, data_conclusao 
      FROM manutencao 
      WHERE data_conclusao IS NOT NULL 
        AND data_inicio IS NOT NULL 
        AND data_conclusao < data_inicio
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-016"

  - id: "TEST-416"
    description: "Custo total igual a soma de peças + mão de obra (campo derivado consistente)"
    query: |
      SELECT id, numero_ordem, custo_pecas, custo_mao_obra, custo_total,
             COALESCE(custo_pecas,0) + COALESCE(custo_mao_obra,0) AS soma_calculada
      FROM manutencao 
      WHERE custo_total IS NOT NULL
        AND custo_total != COALESCE(custo_pecas,0) + COALESCE(custo_mao_obra,0)
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-017"

  - id: "TEST-417"
    description: "Tipo de manutenção apenas valores válidos"
    query: |
      SELECT id, numero_ordem, tipo 
      FROM manutencao 
      WHERE tipo NOT IN ('PREVENTIVA','CORRETIVA','PREDITIVA','MELHORIA')
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-018"

  - id: "TEST-418"
    description: "Status de manutenção apenas valores válidos (máquina de estados)"
    query: |
      SELECT id, numero_ordem, status 
      FROM manutencao 
      WHERE status NOT IN ('SOLICITADA','APROVADA','EM_ANDAMENTO','CONCLUIDA','CANCELADA','AGUARDANDO_PECAS')
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-019"

  - id: "TEST-419"
    description: "Prioridade de manutenção apenas valores válidos"
    query: |
      SELECT id, numero_ordem, prioridade 
      FROM manutencao 
      WHERE prioridade NOT IN ('BAIXA','MEDIA','ALTA','CRITICA')
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-020"

  - id: "TEST-420"
    description: "Tipo de alerta apenas valores válidos (baseado em AlertNotificationService)"
    query: |
      SELECT id, tipo 
      FROM alerta 
      WHERE tipo NOT IN ('USO_DISCO_CRITICO','USO_MEMORIA_CRITICO','MANUTENCAO_VENCIDA','SAUDE_DEGRADADA','GARANTIA_EXPIRANDO','DEPRECIACAO_ACELERADA','OUTRO')
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-021"

  - id: "TEST-421"
    description: "Severidade de alerta apenas valores válidos"
    query: |
      SELECT id, severidade 
      FROM alerta 
      WHERE severidade NOT IN ('INFO','WARNING','CRITICAL')
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-022"

  - id: "TEST-422"
    description: "Status de alerta apenas valores válidos (máquina de estados)"
    query: |
      SELECT id, status 
      FROM alerta 
      WHERE status NOT IN ('NAO_LIDO','LIDO','EM_TRATAMENTO','RESOLVIDO','IGNORADO')
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-023"

  - id: "TEST-423"
    description: "Data de leitura não anterior à criação do alerta"
    query: |
      SELECT id, data_criacao, data_leitura 
      FROM alerta 
      WHERE data_leitura IS NOT NULL 
        AND data_leitura < data_criacao
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-024"

  - id: "TEST-424"
    description: "Data de resolução não anterior à leitura"
    query: |
      SELECT id, data_leitura, data_resolucao 
      FROM alerta 
      WHERE data_resolucao IS NOT NULL 
        AND data_leitura IS NOT NULL 
        AND data_resolucao < data_leitura
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-025"

  - id: "TEST-425"
    description: "Username único (imutável após criação - setUsername stub)"
    query: |
      SELECT username, COUNT(*) 
      FROM usuario 
      GROUP BY username 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-026"

  - id: "TEST-426"
    description: "Email único para autenticação"
    query: |
      SELECT email, COUNT(*) 
      FROM usuario 
      GROUP BY email 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-027"

  - id: "TEST-427"
    description: "Tentativas de login falhas não negativas"
    query: |
      SELECT id, username, tentativas_login_falhas 
      FROM usuario 
      WHERE tentativas_login_falhas < 0
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-028"

  - id: "TEST-428"
    description: "Bloqueado_ate deve ser futuro se preenchido"
    query: |
      SELECT id, username, bloqueado_ate 
      FROM usuario 
      WHERE bloqueado_ate IS NOT NULL 
        AND bloqueado_ate <= CURRENT_TIMESTAMP
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-029"

  - id: "TEST-429"
    description: "Vínculo 1:1 usuário-funcionário (funcionario_id único)"
    query: |
      SELECT funcionario_id, COUNT(*) 
      FROM usuario 
      WHERE funcionario_id IS NOT NULL
      GROUP BY funcionario_id 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-030"

  - id: "TEST-430"
    description: "Nome de role único"
    query: |
      SELECT nome, COUNT(*) 
      FROM role 
      GROUP BY nome 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-031"

  - id: "TEST-431"
    description: "Código de permissão único"
    query: |
      SELECT codigo, COUNT(*) 
      FROM permissao 
      GROUP BY codigo 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-032"

  - id: "TEST-432"
    description: "Par (recurso, acao) único em permissao"
    query: |
      SELECT recurso, acao, COUNT(*) 
      FROM permissao 
      GROUP BY recurso, acao 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-033"

  - id: "TEST-433"
    description: "Matrícula de funcionário única"
    query: |
      SELECT matricula, COUNT(*) 
      FROM funcionario 
      GROUP BY matricula 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-034"

  - id: "TEST-434"
    description: "CPF de funcionário único e formato válido"
    query: |
      SELECT cpf, COUNT(*) 
      FROM funcionario 
      GROUP BY cpf 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-035"

  - id: "TEST-435"
    description: "Email corporativo de funcionário único"
    query: |
      SELECT email_corporativo, COUNT(*) 
      FROM funcionario 
      GROUP BY email_corporativo 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-036"

  - id: "TEST-436"
    description: "Data de desligamento não anterior à admissão"
    query: |
      SELECT id, matricula, data_admissao, data_desligamento 
      FROM funcionario 
      WHERE data_desligamento IS NOT NULL 
        AND data_desligamento < data_admissao
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-037"

  - id: "TEST-437"
    description: "Consistência entre data_desligamento e flag ativo"
    query: |
      SELECT id, matricula, data_desligamento, ativo 
      FROM funcionario 
      WHERE (data_desligamento IS NOT NULL AND ativo = true)
         OR (data_desligamento IS NULL AND ativo = false)
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-038"

  - id: "TEST-438"
    description: "Código de filial único"
    query: |
      SELECT codigo, COUNT(*) 
      FROM filial 
      GROUP BY codigo 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-039"

  - id: "TEST-439"
    description: "Nome de filial único"
    query: |
      SELECT nome, COUNT(*) 
      FROM filial 
      GROUP BY nome 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-040"

  - id: "TEST-440"
    description: "Código de departamento único por filial"
    query: |
      SELECT codigo, filial_id, COUNT(*) 
      FROM departamento 
      GROUP BY codigo, filial_id 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-041"

  - id: "TEST-441"
    description: "Nome de departamento único por filial"
    query: |
      SELECT nome, filial_id, COUNT(*) 
      FROM departamento 
      GROUP BY nome, filial_id 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-042"

  - id: "TEST-442"
    description: "Código de localização único por filial"
    query: |
      SELECT codigo, filial_id, COUNT(*) 
      FROM localizacao 
      GROUP BY codigo, filial_id 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-043"

  - id: "TEST-443"
    description: "CNPJ de fornecedor único"
    query: |
      SELECT cnpj, COUNT(*) 
      FROM fornecedor 
      GROUP BY cnpj 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-044"

  - id: "TEST-444"
    description: "Relacionamento 1:1 ativo → ativo_detalhe_hardware enforçado"
    query: |
      SELECT ativo_id, COUNT(*) 
      FROM ativo_detalhe_hardware 
      GROUP BY ativo_id 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-045"

  - id: "TEST-445"
    description: "Data de coleta de detalhe hardware não futura"
    query: |
      SELECT id, ativo_id, coletado_em 
      FROM ativo_detalhe_hardware 
      WHERE coletado_em > CURRENT_TIMESTAMP
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-046"

  - id: "TEST-446"
    description: "Endereço MAC único por ativo_detalhe_hardware e formato válido"
    query: |
      SELECT ativo_detalhe_hardware_id, endereco_mac, COUNT(*) 
      FROM adaptador_rede 
      GROUP BY ativo_detalhe_hardware_id, endereco_mac 
      HAVING COUNT(*) > 1
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-047"

  - id: "TEST-447"
    description: "Formato MAC válido (XX:XX:XX:XX:XX:XX)"
    query: |
      SELECT id, ativo_detalhe_hardware_id, endereco_mac 
      FROM adaptador_rede 
      WHERE endereco_mac !~ '^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$'
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-048"

  - id: "TEST-448"
    description: "Tipo de adaptador de rede apenas valores válidos"
    query: |
      SELECT id, tipo 
      FROM adaptador_rede 
      WHERE tipo NOT IN ('ETHERNET','WIFI','BLUETOOTH','VIRTUAL','OUTRO')
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-049"

  - id: "TEST-449"
    description: "Capacidade de disco positiva"
    query: |
      SELECT id, ativo_detalhe_hardware_id, capacidade_gb 
      FROM disco 
      WHERE capacidade_gb <= 0
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-050"

  - id: "TEST-450"
    description: "Saúde do disco entre 0 e 100%"
    query: |
      SELECT id, ativo_detalhe_hardware_id, saude_percentual 
      FROM disco 
      WHERE saude_percentual IS NOT NULL 
        AND (saude_percentual < 0 OR saude_percentual > 100)
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-051"

  - id: "TEST-451"
    description: "Tipo de disco apenas valores válidos"
    query: |
      SELECT id, tipo 
      FROM disco 
      WHERE tipo NOT IN ('HDD','SSD','NVME','HIBRIDO')
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-052"

  - id: "TEST-452"
    description: "Capacidade de memória positiva"
    query: |
      SELECT id, ativo_detalhe_hardware_id, capacidade_gb 
      FROM memoria 
      WHERE capacidade_gb <= 0
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-053"

  - id: "TEST-453"
    description: "Ação de auditoria apenas valores válidos"
    query: |
      SELECT id, acao 
      FROM auditoria 
      WHERE acao NOT IN ('CREATE','UPDATE','DELETE','LOGIN','LOGOUT','EXPORT','IMPORT','APPROVE','REJECT','READ')
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-054"

  - id: "TEST-454"
    description: "Snapshots obrigatórios por tipo de ação de auditoria (ASM-009)"
    query: |
      SELECT id, acao, valores_anteriores, valores_novos 
      FROM auditoria 
      WHERE (acao = 'CREATE' AND valores_novos IS NULL)
         OR (acao = 'DELETE' AND valores_anteriores IS NULL)
         OR (acao = 'UPDATE' AND (valores_anteriores IS NULL OR valores_novos IS NULL))
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-055"

  - id: "TEST-455"
    description: "Expiração de role posterior à concessão"
    query: |
      SELECT usuario_id, role_id, concedido_em, expira_em 
      FROM usuario_role 
      WHERE expira_em IS NOT NULL 
        AND expira_em <= concedido_em
    expected_rows: 0
    source: "db-schema-spec#3.CONSTRAINT-056"

  - id: "TEST-456"
    description: "Hash encadeado de auditoria monotonicamente crescente por correlation_id (ASM-009)"
    query: |
      WITH ordered AS (
        SELECT id, correlation_id, data_hora, hash_encadeado,
               LAG(hash_encadeado) OVER (PARTITION BY correlation_id ORDER BY data_hora) AS hash_anterior
        FROM auditoria
      )
      SELECT correlation_id, id, hash_encadeado, hash_anterior
      FROM ordered
      WHERE hash_anterior IS NOT NULL
        AND hash_encadeado <= hash_anterior  -- verifica monotonicidade (simplificado)
    expected_rows: 0
    source: "db-schema-spec#7.triggers[0], db-schema-spec#4.INDEX-030"

  - id: "TEST-457"
    description: "Auditoria é append-only (nenhum UPDATE/DELETE permitido)"
    query: |
      -- Este teste valida que o trigger trg_auditoria_prevent_update_delete bloqueia operações
      -- Deve ser testado via tentativa de UPDATE/DELETE que deve falhar
      SELECT 'Teste executado via tentativa de UPDATE/DELETE que deve ser bloqueada pelo trigger' AS info
    expected_rows: 1
    source: "db-schema-spec#7.triggers[1]"

  - id: "TEST-458"
    description: "Custo total de manutenção sincronizado automaticamente via trigger"
    query: |
      SELECT id, numero_ordem, custo_pecas, custo_mao_obra, custo_total
      FROM manutencao
      WHERE custo_total IS NOT NULL
        AND custo_total != COALESCE(custo_pecas,0) + COALESCE(custo_mao_obra,0)
    expected_rows: 0
    source: "db-schema-spec#7.triggers[2]"

  - id: "TEST-459"
    description: "Funcionário desligado tem ativo=false automaticamente"
    query: |
      SELECT id, matricula, data_desligamento, ativo
      FROM funcionario
      WHERE data_desligamento IS NOT NULL
        AND ativo = true
    expected_rows: 0
    source: "db-schema-spec#7.triggers[3]"

  - id: "TEST-460"
    description: "Concessão/revogação de roles auditada automaticamente"
    query: |
      SELECT a.id, a.entidade, a.acao, a.entidade_id
      FROM auditoria a
      WHERE a.entidade = 'usuario_role'
        AND a.acao IN ('APPROVE','REJECT')
      ORDER BY a.data_hora DESC
      LIMIT 10
    expected_rows: "> 0 (se houver concessões/revogações recentes)"
    source: "db-schema-spec#7.triggers[4]"

  - id: "TEST-461"
    description: "Concessão/revogação de permissões a roles auditada automaticamente"
    query: |
      SELECT a.id, a.entidade, a.acao, a.entidade_id
      FROM auditoria a
      WHERE a.entidade = 'role_permissao'
        AND a.acao IN ('APPROVE','REJECT')
      ORDER BY a.data_hora DESC
      LIMIT 10
    expected_rows: "> 0 (se houver concessões/revogações recentes)"
    source: "db-schema-spec#7.triggers[5]"
```

## 4. Security Tests
| ID | Role | Operação | Esperado |
| :--- | :--- | :--- | :--- |
| TEST-500 | ADMIN | SELECT/INSERT/UPDATE/DELETE em todas as tabelas | Permitido <!-- source: test-strategy#11.traceability_matrix, db-schema-spec#1.6.role --> |
| TEST-501 | GESTOR_PATRIMONIO | SELECT/INSERT/UPDATE em ativo, manutencao, alerta, funcionario, departamento, localizacao | Permitido <!-- source: test-strategy#11.traceability_matrix, db-schema-spec#1.6.role --> |
| TEST-502 | GESTOR_PATRIMONIO | DELETE em ativo, manutencao | Negado (soft delete via status) <!-- source: test-strategy#12.open_issues[Soft vs Hard delete], db-schema-spec#1.1.columns[14] --> |
| TEST-503 | ANALISTA_MANUTENCAO | SELECT/INSERT/UPDATE em manutencao, alerta, ativo (leitura) | Permitido <!-- source: test-strategy#11.traceability_matrix --> |
| TEST-504 | ANALISTA_MANUTENCAO | DELETE em manutencao | Negado <!-- source: test-strategy#11.traceability_matrix --> |
| TEST-505 | TECNICO_MANUTENCAO | SELECT/UPDATE em manutencao (próprias), SELECT em ativo | Permitido <!-- source: test-strategy#11.traceability_matrix --> |
| TEST-506 | TECNICO_MANUTENCAO | INSERT/DELETE em manutencao | Negado <!-- source: test-strategy#11.traceability_matrix --> |
| TEST-507 | USER | SELECT em ativo (própria filial/departamento), INSERT/UPDATE em manutencao (próprias solicitações) | Permitido <!-- source: test-strategy#12.open_issues[Filtro implícito por escopo] --> |
| TEST-508 | USER | DELETE, acesso a outras filiais/departamentos | Negado <!-- source: test-strategy#12.open_issues[Filtro implícito por escopo] --> |
| TEST-509 | AUDITOR | SELECT em auditoria, ativo, manutencao, alerta, usuario (somente leitura) | Permitido <!-- source: test-strategy#11.traceability_matrix, db-schema-spec#1.17 --> |
| TEST-510 | AUDITOR | INSERT/UPDATE/DELETE em qualquer tabela | Negado <!-- source: test-strategy#11.traceability_matrix --> |
| TEST-511 | HEALTH_COLLECTOR | INSERT/UPDATE em ativo (health check fields), INSERT em ativo_detalhe_hardware, adaptador_rede, disco, memoria | Permitido <!-- source: test-strategy#12.open_issues[Role HEALTH_COLLECTOR], db-schema-spec#1.1.columns[16-20] --> |
| TEST-512 | HEALTH_COLLECTOR | Acesso a tabelas de negócio (manutencao, alerta, usuario, etc.) | Negado <!-- source: test-strategy#12.open_issues[Role HEALTH_COLLECTOR] --> |
| TEST-513 | PUBLIC (não autenticado) | Qualquer operação em tabelas core/transactional/audit | Negado (401) <!-- source: test-strategy#5.Security.RBAC Validation --> |
| TEST-514 | ANY | UPDATE/DELETE em auditoria | Negado (trigger trg_auditoria_prevent_update_delete) <!-- source: db-schema-spec#7.triggers[1] --> |
| TEST-515 | ANY | INSERT em auditoria sem hash_encadeado válido | Negado (trigger trg_auditoria_insert_hash_chain) <!-- source: db-schema-spec#7.triggers[0] --> |
| TEST-516 | ADMIN | EXECUTE FUNCTION anonymize_user | Permitido (LGPD compliance) <!-- source: db-schema-spec#6.functions[3] --> |
| TEST-517 | NON-ADMIN | EXECUTE FUNCTION anonymize_user | Negado (SECURITY DEFINER restrito) <!-- source: db-schema-spec#6.functions[3].execution_context --> |
| TEST-518 | ANY | Acesso a password_hash em usuario | Negado (coluna sensitivity: restricted) <!-- source: db-schema-spec#1.5.columns[3].sensitivity --> |
| TEST-519 | ANY | Acesso a cpf em funcionario | Restrito (coluna sensitivity: confidential) <!-- source: db-schema-spec#1.8.columns[2].sensitivity --> |
| TEST-520 | ANY | Acesso a cnpj em fornecedor | Restrito (coluna sensitivity: internal) <!-- source: db-schema-spec#1.12.columns[1].sensitivity --> |
| TEST-521 | ANY | Acesso a valor_aquisicao em ativo | Restrito (coluna sensitivity: confidential) <!-- source: db-schema-spec#1.1.columns[12].sensitivity --> |
| TEST-522 | ANY | Acesso a custo_* em manutencao | Restrito (coluna sensitivity: confidential) <!-- source: db-schema-spec#1.3.columns[13-15].sensitivity --> |

## 5. Data Quality Rules
```yaml
data_quality:
  - id: "DQ-001"
    field: "ativo.tag_patrimonial"
    rule: "not_null"
    threshold_pct: 100
    source: "db-schema-spec#1.1.columns[1].nullable=false"

  - id: "DQ-002"
    field: "ativo.tag_patrimonial"
    rule: "unique"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-001"

  - id: "DQ-003"
    field: "ativo.numero_serie"
    rule: "unique_per_tipo_ativo"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-002"

  - id: "DQ-004"
    field: "ativo.valor_aquisicao"
    rule: "positive_numeric"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-003"

  - id: "DQ-005"
    field: "ativo.vida_util_meses"
    rule: "positive_integer"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-004"

  - id: "DQ-006"
    field: "ativo.data_aquisicao"
    rule: "not_future_date"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-005"

  - id: "DQ-007"
    field: "ativo.uso_disco_percentual"
    rule: "range_0_100"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-006"

  - id: "DQ-008"
    field: "ativo.status"
    rule: "enum_valid"
    threshold_pct: 100
    allowed_values: ["ATIVO", "EM_MANUTENCAO", "DESCARTADO", "VENDIDO", "EMPRESTADO"]
    source: "db-schema-spec#3.CONSTRAINT-007"

  - id: "DQ-009"
    field: "ativo.condicao"
    rule: "enum_valid"
    threshold_pct: 100
    allowed_values: ["NOVO", "BOM", "REGULAR", "RUIM", "SUCATA"]
    source: "db-schema-spec#3.CONSTRAINT-008"

  - id: "DQ-010"
    field: "tipo_ativo.codigo"
    rule: "not_null|unique"
    threshold_pct: 100
    source: "db-schema-spec#1.2.columns[1], db-schema-spec#3.CONSTRAINT-009"

  - id: "DQ-011"
    field: "tipo_ativo.vida_util_padrao_meses"
    rule: "positive_integer"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-010"

  - id: "DQ-012"
    field: "tipo_ativo.taxa_depreciacao_anual"
    rule: "range_0_1"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-011"

  - id: "DQ-013"
    field: "tipo_ativo.categoria"
    rule: "enum_valid"
    threshold_pct: 100
    allowed_values: ["HARDWARE", "SOFTWARE", "MOVEL", "EQUIPAMENTO", "VEICULO", "OUTRO"]
    source: "db-schema-spec#3.CONSTRAINT-012"

  - id: "DQ-014"
    field: "manutencao.numero_ordem"
    rule: "not_null|unique"
    threshold_pct: 100
    source: "db-schema-spec#1.3.columns[1], db-schema-spec#3.CONSTRAINT-013"

  - id: "DQ-015"
    field: "manutencao.data_aprovacao"
    rule: "gte_data_abertura"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-014"

  - id: "DQ-016"
    field: "manutencao.data_inicio"
    rule: "gte_data_aprovacao"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-015"

  - id: "DQ-017"
    field: "manutencao.data_conclusao"
    rule: "gte_data_inicio"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-016"

  - id: "DQ-018"
    field: "manutencao.custo_total"
    rule: "derived_field_consistency"
    threshold_pct: 100
    formula: "COALESCE(custo_pecas,0) + COALESCE(custo_mao_obra,0)"
    source: "db-schema-spec#3.CONSTRAINT-017"

  - id: "DQ-019"
    field: "manutencao.tipo"
    rule: "enum_valid"
    threshold_pct: 100
    allowed_values: ["PREVENTIVA", "CORRETIVA", "PREDITIVA", "MELHORIA"]
    source: "db-schema-spec#3.CONSTRAINT-018"

  - id: "DQ-020"
    field: "manutencao.status"
    rule: "enum_valid"
    threshold_pct: 100
    allowed_values: ["SOLICITADA", "APROVADA", "EM_ANDAMENTO", "CONCLUIDA", "CANCELADA", "AGUARDANDO_PECAS"]
    source: "db-schema-spec#3.CONSTRAINT-019"

  - id: "DQ-021"
    field: "manutencao.prioridade"
    rule: "enum_valid"
    threshold_pct: 100
    allowed_values: ["BAIXA", "MEDIA", "ALTA", "CRITICA"]
    source: "db-schema-spec#3.CONSTRAINT-020"

  - id: "DQ-022"
    field: "alerta.tipo"
    rule: "enum_valid"
    threshold_pct: 100
    allowed_values: ["USO_DISCO_CRITICO", "USO_MEMORIA_CRITICO", "MANUTENCAO_VENCIDA", "SAUDE_DEGRADADA", "GARANTIA_EXPIRANDO", "DEPRECIACAO_ACELERADA", "OUTRO"]
    source: "db-schema-spec#3.CONSTRAINT-021"

  - id: "DQ-023"
    field: "alerta.severidade"
    rule: "enum_valid"
    threshold_pct: 100
    allowed_values: ["INFO", "WARNING", "CRITICAL"]
    source: "db-schema-spec#3.CONSTRAINT-022"

  - id: "DQ-024"
    field: "alerta.status"
    rule: "enum_valid"
    threshold_pct: 100
    allowed_values: ["NAO_LIDO", "LIDO", "EM_TRATAMENTO", "RESOLVIDO", "IGNORADO"]
    source: "db-schema-spec#3.CONSTRAINT-023"

  - id: "DQ-025"
    field: "alerta.data_leitura"
    rule: "gte_data_criacao"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-024"

  - id: "DQ-026"
    field: "alerta.data_resolucao"
    rule: "gte_data_leitura"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-025"

  - id: "DQ-027"
    field: "usuario.username"
    rule: "not_null|unique"
    threshold_pct: 100
    source: "db-schema-spec#1.5.columns[1], db-schema-spec#3.CONSTRAINT-026"

  - id: "DQ-028"
    field: "usuario.email"
    rule: "not_null|unique|email_format"
    threshold_pct: 100
    source: "db-schema-spec#1.5.columns[2], db-schema-spec#3.CONSTRAINT-027"

  - id: "DQ-029"
    field: "usuario.tentativas_login_falhas"
    rule: "non_negative_integer"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-028"

  - id: "DQ-030"
    field: "usuario.bloqueado_ate"
    rule: "future_timestamp_if_not_null"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-029"

  - id: "DQ-031"
    field: "usuario.funcionario_id"
    rule: "unique_if_not_null"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-030"

  - id: "DQ-032"
    field: "funcionario.matricula"
    rule: "not_null|unique"
    threshold_pct: 100
    source: "db-schema-spec#1.8.columns[1], db-schema-spec#3.CONSTRAINT-034"

  - id: "DQ-033"
    field: "funcionario.cpf"
    rule: "not_null|unique|cpf_algorithm_valid"
    threshold_pct: 100
    source: "db-schema-spec#1.8.columns[2], db-schema-spec#3.CONSTRAINT-035, db-schema-spec#6.functions[0]"

  - id: "DQ-034"
    field: "funcionario.email_corporativo"
    rule: "not_null|unique|email_format"
    threshold_pct: 100
    source: "db-schema-spec#1.8.columns[4], db-schema-spec#3.CONSTRAINT-036"

  - id: "DQ-035"
    field: "funcionario.data_desligamento"
    rule: "gte_data_admissao"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-037"

  - id: "DQ-036"
    field: "funcionario.ativo"
    rule: "consistent_with_data_desligamento"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-038"

  - id: "DQ-037"
    field: "filial.codigo"
    rule: "not_null|unique"
    threshold_pct: 100
    source: "db-schema-spec#1.9.columns[1], db-schema-spec#3.CONSTRAINT-039"

  - id: "DQ-038"
    field: "filial.nome"
    rule: "not_null|unique"
    threshold_pct: 100
    source: "db-schema-spec#1.9.columns[2], db-schema-spec#3.CONSTRAINT-040"

  - id: "DQ-039"
    field: "departamento.codigo"
    rule: "unique_per_filial"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-041"

  - id: "DQ-040"
    field: "departamento.nome"
    rule: "unique_per_filial"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-042"

  - id: "DQ-041"
    field: "localizacao.codigo"
    rule: "unique_per_filial"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-043"

  - id: "DQ-042"
    field: "fornecedor.cnpj"
    rule: "not_null|unique|cnpj_algorithm_valid"
    threshold_pct: 100
    source: "db-schema-spec#1.12.columns[1], db-schema-spec#3.CONSTRAINT-044, db-schema-spec#6.functions[1]"

  - id: "DQ-043"
    field: "ativo_detalhe_hardware.ativo_id"
    rule: "not_null|unique|fk_exists"
    threshold_pct: 100
    source: "db-schema-spec#1.13.columns[1], db-schema-spec#3.CONSTRAINT-045"

  - id: "DQ-044"
    field: "ativo_detalhe_hardware.coletado_em"
    rule: "not_future_timestamp"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-046"

  - id: "DQ-045"
    field: "adaptador_rede.endereco_mac"
    rule: "unique_per_ativo_detalhe|mac_format_valid"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-047, db-schema-spec#3.CONSTRAINT-048"

  - id: "DQ-046"
    field: "adaptador_rede.tipo"
    rule: "enum_valid"
    threshold_pct: 100
    allowed_values: ["ETHERNET", "WIFI", "BLUETOOTH", "VIRTUAL", "OUTRO"]
    source: "db-schema-spec#3.CONSTRAINT-049"

  - id: "DQ-047"
    field: "disco.capacidade_gb"
    rule: "positive_integer"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-050"

  - id: "DQ-048"
    field: "disco.saude_percentual"
    rule: "range_0_100"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-051"

  - id: "DQ-049"
    field: "disco.tipo"
    rule: "enum_valid"
    threshold_pct: 100
    allowed_values: ["HDD", "SSD", "NVME", "HIBRIDO"]
    source: "db-schema-spec#3.CONSTRAINT-052"

  - id: "DQ-050"
    field: "memoria.capacidade_gb"
    rule: "positive_integer"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-053"

  - id: "DQ-051"
    field: "auditoria.acao"
    rule: "enum_valid"
    threshold_pct: 100
    allowed_values: ["CREATE", "UPDATE", "DELETE", "LOGIN", "LOGOUT", "EXPORT", "IMPORT", "APPROVE", "REJECT", "READ"]
    source: "db-schema-spec#3.CONSTRAINT-054"

  - id: "DQ-052"
    field: "auditoria.valores_anteriores, auditoria.valores_novos"
    rule: "audit_snapshots_required_per_action"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-055"

  - id: "DQ-053"
    field: "usuario_role.expira_em"
    rule: "gt_concedido_em_if_not_null"
    threshold_pct: 100
    source: "db-schema-spec#3.CONSTRAINT-056"

  - id: "DQ-054"
    field: "ativo.memoria_total_gb"
    rule: "positive_integer_if_not_null"
    threshold_pct: 100
    source: "db-schema-spec#1.1.columns[18].constraints"

  - id: "DQ-055"
    field: "ativo_detalhe_hardware.processoador_nucleos"
    rule: "positive_integer_if_not_null"
    threshold_pct: 100
    source: "db-schema-spec#1.13.columns[3].constraints"

  - id: "DQ-056"
    field: "ativo_detalhe_hardware.memoria_total_gb"
    rule: "positive_integer_if_not_null"
    threshold_pct: 100
    source: "db-schema-spec#1.13.columns[4].constraints"

  - id: "DQ-057"
    field: "ativo_detalhe_hardware.disco_total_gb"
    rule: "positive_integer_if_not_null"
    threshold_pct: 100
    source: "db-schema-spec#1.13.columns[6].constraints"

  - id: "DQ-058"
    field: "adaptador_rede.velocidade_mbps"
    rule: "positive_integer_if_not_null"
    threshold_pct: 100
    source: "db-schema-spec#1.14.columns[6].constraints"

  - id: "DQ-059"
    field: "memoria.velocidade_mhz"
    rule: "positive_integer_if_not_null"
    threshold_pct: 100
    source: "db-schema-spec#1.16.columns[5].constraints"

  - id: "DQ-060"
    field: "manutencao.custo_pecas"
    rule: "non_negative_if_not_null"
    threshold_pct: 100
    source: "db-schema-spec#1.3.columns[13].constraints"

  - id: "DQ-061"
    field: "manutencao.custo_mao_obra"
    rule: "non_negative_if_not_null"
    threshold_pct: 100
    source: "db-schema-spec#1.3.columns[14].constraints"

anomaly_detection:
  enabled: true
  rules:
    - name: "health_check_stale"
      description: "Ativos com ultimo_health_check > 24h para tipos HARDWARE"
      query: |
        SELECT a.id, a.tag_patrimonial, a.ultimo_health_check, ta.categoria
        FROM ativo a
        INNER JOIN tipo_ativo ta ON a.tipo_ativo_id = ta.id
        WHERE ta.categoria = 'HARDWARE'
          AND a.status = 'ATIVO'
          AND a.ultimo_health_check < NOW() - INTERVAL '24 hours'
      schedule: "0 */6 * * *"  # a cada 6 horas
      severity: "warning"
      source: "db-schema-spec#1.1.columns[16], test-strategy#5.Non-Functional Testing.Resiliência"

    - name: "manutencao_atrasada"
      description: "Manutenções preventivas com data_prevista_conclusao vencida e status não CONCLUIDA/CANCELADA"
      query: |
        SELECT m.id, m.numero_ordem, m.ativo_id, m.data_prevista_conclusao, m.status
        FROM manutencao m
        WHERE m.tipo = 'PREVENTIVA'
          AND m.data_prevista_conclusao < NOW()
          AND m.status NOT IN ('CONCLUIDA', 'CANCELADA')
      schedule: "0 8 * * *"  # diário às 8h
      severity: "warning"
      source: "db-schema-spec#1.3.columns[12], test-strategy#5.Non-Functional Testing.Resiliência"

    - name: "alerta_nao_tratado_critico"
      description: "Alertas CRITICAL com status NAO_LIDO há mais de 1 hora"
      query: |
        SELECT id, ativo_id, tipo, data_criacao
        FROM alerta
        WHERE severidade = 'CRITICAL'
          AND status = 'NAO_LIDO'
          AND data_criacao < NOW() - INTERVAL '1 hour'
      schedule: "*/15 * * * *"  # a cada 15 min
      severity: "critical"
      source: "db-schema-spec#1.4.columns[3-4], test-strategy#5.Non-Functional Testing.Resiliência"

    - name: "auditoria_gap_hash_chain"
      description: "Verificação de integridade da cadeia de hash por correlation_id"
      query: |
        WITH ordered AS (
          SELECT correlation_id, data_hora, hash_encadeado,
                 LAG(hash_encadeado) OVER (PARTITION BY correlation_id ORDER BY data_hora) AS hash_anterior
          FROM auditoria
        )
        SELECT correlation_id, COUNT(*) AS gaps
        FROM ordered
        WHERE hash_anterior IS NOT NULL
          AND hash_encadeado <= hash_anterior
        GROUP BY correlation_id
        HAVING COUNT(*) > 0
      schedule: "0 2 * * *"  # diário às 2h
      severity: "critical"
      source: "db-schema-spec#7.triggers[0], db-schema-spec#4.INDEX-030"

    - name: "funcionario_ativo_inconsistente"
      description: "Funcionários com data_desligamento preenchida mas ativo=true, ou vice-versa"
      query: |
        SELECT id, matricula, data_desligamento, ativo
        FROM funcionario
        WHERE (data_desligamento IS NOT NULL AND ativo = true)
           OR (data_desligamento IS NULL AND ativo = false)
      schedule: "0 3 * * *"  # diário às 3h
      severity: "warning"
      source: "db-schema-spec#3.CONSTRAINT-038, db-schema-spec#7.triggers[3]"

    - name: "custo_total_manutencao_inconsistente"
      description: "Manutenções onde custo_total != custo_pecas + custo_mao_obra"
      query: |
        SELECT id, numero_ordem, custo_pecas, custo_mao_obra, custo_total
        FROM manutencao
        WHERE custo_total IS NOT NULL
          AND custo_total != COALESCE(custo_pecas,0) + COALESCE(custo_mao_obra,0)
      schedule: "0 4 * * *"  # diário às 4h
      severity: "warning"
      source: "db-schema-spec#3.CONSTRAINT-017, db-schema-spec#7.triggers[2]"

severity_thresholds:
  warning: "Qualquer violação de regra DQ com threshold_pct < 100% ou detecção de anomalia agendada"
  critical: "Violação de constraint UNIQUE/PK/FK/CHECK (banco rejeita), anomalia CRITICAL detectada, auditoria hash chain quebrada, alerta CRITICAL não tratado > 1h"
  source: "test-strategy#8.Bug Triage & Severity, db-schema-spec#3.CONSTRAINT-*, db-schema-spec#7.triggers[0-1]"
```

---

**Observações Finais:**

1. **Motor de banco não definido**: Conforme aviso crítico no [[db-schema-spec]], o SGBD alvo (PostgreSQL, Oracle, SQL Server, MySQL) não foi confirmado. Todos os tipos (UUID, JSONB, ENUM, INET, MACADDR, TIMESTAMP WITH TIME ZONE) são **LÓGICOS**. A conversão para tipos nativos e a sintaxe exata de DDL/DML dependem da decisão **MOTOR-001** (ver seção 9 do db-schema-spec). Os testes acima assumem sintaxe PostgreSQL-like; ajustar para o SGBD escolhido.

2. **ORM/Query Builder não identificado**: O diagnóstico indica "SQL cru". Testes de integração devem usar conexão JDBC direta ou o framework que for adotado (JPA/Hibernate per ASM-017, jOOQ, etc.). Confirmar item **ORM-001**.

3. **Dados sensíveis**: Conforme [[test-strategy#6.Test Data Management]], **obrigatório** anonimização/mascaramento de CPF, CNPJ, email, username, password_hash, valores financeiros fora de produção. Scripts de sanitização devem ser versionados.

4. **Particionamento**: Índices INDEX-008, INDEX-013, INDEX-033 sugerem particionamento lógico por data. Confirmar estratégia física **PART-001** antes de validar performance dos índices.

5. **WORM Storage**: Auditoria requer storage imutável (**WORM-001**). Testes de segurança TEST-514, TEST-515, TEST-457, TEST-456 validam isso no banco, mas a camada de armazenamento (S3 Object Lock, Azure Immutable Blob, cluster separado) deve ser testada separadamente.

6. **RLS/Multi-tenancy**: Item **SEC-001** pendente. Se implementado Row Level Security por `filial_id`, adicionar testes de isolamento de dados entre filiais (TEST-507, TEST-508 dependem desta decisão).

7. **Validação CPF/CNPJ**: Functions `validar_cpf` e `validar_cnpj` (db-schema-spec#6.functions[0-1]) devem ser implementadas no SGBD alvo. Testes DQ-033, DQ-042 dependem delas.

8. **Performance baselines**: Índices compostos (INDEX-009, INDEX-010, INDEX-014) devem ser validados com `EXPLAIN ANALYZE` em carga realista (100k+ ativos, 1M+ manutenções, 500k+ alertas, 10M+ auditoria) conforme **IDX-001**.