# Database Domain & Entity Model — aegis_patrimonio

> **Versão:** 1.0 · **Owner:** Aegis Patrimônio — Engenharia de Dados · **Status:** Draft
> Modelo conceitual — a IA pode inferir este modelo a partir do domínio, mas ele nunca deve ser convertido diretamente em SQL sem passar pelo contrato físico em [[db-schema-spec]].

## 1. Domain
```yaml
domain:
  id: "aegis-patrimonio"
  name: "Gestão de Patrimônio Corporativo"
  description: "Domínio de gestão de ativos (hardware, software, móveis), ciclo de vida de manutenções (preventiva/corretiva), alertas de uso/recursos, controle de acesso (RBAC) e trilha de auditoria imutável para conformidade SOX/LGPD."
```

## 2. Entities
```yaml
entities:
  - id: "ent-ativo"
    name: "Ativo"
    description: "Bem patrimonial rastreável (hardware, software, móvel, equipamento) com identificação única, classificação, localização, responsável e métricas de saúde para gestão de ciclo de vida e depreciação."
    table: "ativo"
    classification: "core"
    lifecycle:
      creation: "Cadastro via createAtivo (frontend) ou importação em lote (RealisticDataSeeder)"
      archival: "Particionamento por data de aquisição; move para storage frio após 2 anos de baixa/descarte"
      deletion: "Apenas com aprovação Legal + Compliance + Product; baixa lógica (status=DESCARTADO) preferida sobre hard delete"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "tag_patrimonial"
        type: "business"
      - name: "numero_serie"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "tag_patrimonial"
        type: "String(50)"
        required: true
      - name: "numero_serie"
        type: "String(100)"
        required: false
      - name: "nome"
        type: "String(200)"
        required: true
      - name: "descricao"
        type: "Text"
        required: false
      - name: "tipo_ativo_id"
        type: "UUID"
        required: true
      - name: "filial_id"
        type: "UUID"
        required: true
      - name: "departamento_id"
        type: "UUID"
        required: false
      - name: "localizacao_id"
        type: "UUID"
        required: false
      - name: "fornecedor_id"
        type: "UUID"
        required: false
      - name: "funcionario_responsavel_id"
        type: "UUID"
        required: false
      - name: "data_aquisicao"
        type: "LocalDate"
        required: true
      - name: "valor_aquisicao"
        type: "Money (NUMERIC(18,4))"
        required: true
      - name: "vida_util_meses"
        type: "Integer"
        required: true
      - name: "status"
        type: "Enum (ATIVO, EM_MANUTENCAO, DESCARTADO, VENDIDO, EMPRESTADO)"
        required: true
      - name: "condicao"
        type: "Enum (NOVO, BOM, REGULAR, RUIM, SUCATA)"
        required: true
      - name: "ultimo_health_check"
        type: "Instant"
        required: false
      - name: "uso_disco_percentual"
        type: "Decimal(5,2)"
        required: false
      - name: "memoria_total_gb"
        type: "Integer"
        required: false
      - name: "processador"
        type: "String(200)"
        required: false
      - name: "sistema_operacional"
        type: "String(100)"
        required: false
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
      - name: "created_by"
        type: "UUID"
        required: true
      - name: "updated_by"
        type: "UUID"
        required: true
    relationships:
      - target: "TipoAtivo"
        cardinality: "N:1"
        semantic: "Classificação do ativo (define regras de depreciação e métricas de saúde esperadas)"
      - target: "Filial"
        cardinality: "N:1"
        semantic: "Unidade organizacional onde o ativo está alocado"
      - target: "Departamento"
        cardinality: "N:1"
        semantic: "Departamento responsável pelo ativo (opcional)"
      - target: "Localizacao"
        cardinality: "N:1"
        semantic: "Local físico/sala onde o ativo se encontra (opcional)"
      - target: "Fornecedor"
        cardinality: "N:1"
        semantic: "Fornecedor de origem do ativo (opcional)"
      - target: "Funcionario"
        cardinality: "N:1"
        semantic: "Funcionário responsável/custodiante do ativo (opcional)"
      - target: "Manutencao"
        cardinality: "1:N"
        semantic: "Histórico de manutenções realizadas no ativo"
      - target: "Alerta"
        cardinality: "1:N"
        semantic: "Alertas gerados para este ativo (uso de disco, saúde, manutenção vencida)"
      - target: "AtivoDetalheHardware"
        cardinality: "1:1"
        semantic: "Detalhes técnicos de hardware (adaptadores de rede, discos, memórias) — mapeado por ativo_detalhe_hardware_id"
      - target: "Auditoria"
        cardinality: "1:N"
        semantic: "Trilha de auditoria de alterações neste ativo"
    invariants:
      - "tag_patrimonial deve ser único globalmente"
      - "numero_serie, se preenchido, deve ser único por tipo_ativo"
      - "valor_aquisicao > 0"
      - "vida_util_meses > 0"
      - "data_aquisicao <= data_atual"
      - "Se status = EM_MANUTENCAO, deve existir Manutencao aberta para este ativo"
      - "uso_disco_percentual deve estar entre 0 e 100 quando preenchido"
    data_sensitivity: "internal"

  - id: "ent-tipo-ativo"
    name: "TipoAtivo"
    description: "Classificação/categoria de ativos que define regras de depreciação, vida útil padrão, métricas de saúde esperadas e campos técnicos aplicáveis."
    table: "tipo_ativo"
    classification: "reference"
    lifecycle:
      creation: "Cadastro administrativo via createTipoAtivo"
      archival: "Não se aplica (tabela de referência pequena, imutável após criação)"
      deletion: "Apenas se não houver Ativos referenciando; caso contrário, soft delete (ativo=false)"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "codigo"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "codigo"
        type: "String(30)"
        required: true
      - name: "nome"
        type: "String(100)"
        required: true
      - name: "descricao"
        type: "Text"
        required: false
      - name: "vida_util_padrao_meses"
        type: "Integer"
        required: true
      - name: "taxa_depreciacao_anual"
        type: "Decimal(5,4)"
        required: true
      - name: "categoria"
        type: "Enum (HARDWARE, SOFTWARE, MOVEL, EQUIPAMENTO, VEICULO, OUTRO)"
        required: true
      - name: "campos_tecnicos_obrigatorios"
        type: "JSONB"
        required: false
      - name: "ativo"
        type: "Boolean"
        required: true
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
    relationships:
      - target: "Ativo"
        cardinality: "1:N"
        semantic: "Ativos classificados sob este tipo"
    invariants:
      - "codigo deve ser único"
      - "vida_util_padrao_meses > 0"
      - "taxa_depreciacao_anual >= 0 AND taxa_depreciacao_anual <= 1"
    data_sensitivity: "public"

  - id: "ent-manutencao"
    name: "Manutencao"
    description: "Ordem de manutenção (preventiva ou corretiva) associada a um ativo, com ciclo de vida de aprovação, execução e conclusão, custos, fornecedor e técnico responsável."
    table: "manutencao"
    classification: "transactional"
    lifecycle:
      creation: "Criação via solicitação (frontend) ou geração automática de preventivas (job agendado)"
      archival: "Particionamento por data_abertura; move para storage frio após 2 anos de conclusão"
      deletion: "Não permitido (rastreabilidade SOX); apenas cancelamento lógico (status=CANCELADA)"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "numero_ordem"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "numero_ordem"
        type: "String(50)"
        required: true
      - name: "ativo_id"
        type: "UUID"
        required: true
      - name: "tipo"
        type: "Enum (PREVENTIVA, CORRETIVA, PREDITIVA, MELHORIA)"
        required: true
      - name: "status"
        type: "Enum (SOLICITADA, APROVADA, EM_ANDAMENTO, CONCLUIDA, CANCELADA, AGUARDANDO_PECAS)"
        required: true
      - name: "prioridade"
        type: "Enum (BAIXA, MEDIA, ALTA, CRITICA)"
        required: true
      - name: "descricao_problema"
        type: "Text"
        required: true
      - name: "descricao_solucao"
        type: "Text"
        required: false
      - name: "data_abertura"
        type: "Instant"
        required: true
      - name: "data_aprovacao"
        type: "Instant"
        required: false
      - name: "data_inicio"
        type: "Instant"
        required: false
      - name: "data_conclusao"
        type: "Instant"
        required: false
      - name: "data_prevista_conclusao"
        type: "Instant"
        required: false
      - name: "custo_pecas"
        type: "Money (NUMERIC(18,4))"
        required: false
      - name: "custo_mao_obra"
        type: "Money (NUMERIC(18,4))"
        required: false
      - name: "custo_total"
        type: "Money (NUMERIC(18,4))"
        required: false
      - name: "fornecedor_id"
        type: "UUID"
        required: false
      - name: "tecnico_responsavel_id"
        type: "UUID"
        required: false
      - name: "aprovador_id"
        type: "UUID"
        required: false
      - name: "observacoes"
        type: "Text"
        required: false
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
      - name: "created_by"
        type: "UUID"
        required: true
      - name: "updated_by"
        type: "UUID"
        required: true
    relationships:
      - target: "Ativo"
        cardinality: "N:1"
        semantic: "Ativo objeto da manutenção"
      - target: "Fornecedor"
        cardinality: "N:1"
        semantic: "Fornecedor executor (terceirizado) — opcional"
      - target: "Funcionario"
        cardinality: "N:1"
        semantic: "Técnico responsável pela execução (interno) — opcional"
      - target: "Usuario"
        cardinality: "N:1"
        semantic: "Usuário aprovador da manutenção — opcional"
      - target: "Auditoria"
        cardinality: "1:N"
        semantic: "Trilha de auditoria de alterações na ordem de manutenção"
    invariants:
      - "numero_ordem deve ser único"
      - "data_aprovacao >= data_abertura (quando preenchida)"
      - "data_inicio >= data_aprovacao (quando preenchida)"
      - "data_conclusao >= data_inicio (quando preenchida)"
      - "custo_total = custo_pecas + custo_mao_obra (quando ambos preenchidos)"
      - "Se status = CONCLUIDA, data_conclusao e descricao_solucao são obrigatórios"
      - "Se status = CANCELADA, não pode voltar para status anterior"
      - "Transições de status válidas: SOLICITADA → APROVADA → EM_ANDAMENTO → CONCLUIDA; qualquer → CANCELADA"
    data_sensitivity: "internal"

  - id: "ent-alerta"
    name: "Alerta"
    description: "Notificação automática ou manual gerada para situações de atenção no patrimônio (uso de disco crítico, manutenção vencida, saúde do ativo degradada, etc.)."
    table: "alerta"
    classification: "transactional"
    lifecycle:
      creation: "Geração automática via AlertNotificationService.checkResourceUsageAlerts ou manual via frontend"
      archival: "Particionamento por data_criacao; arquiva alertas lidos > 90 dias"
      deletion: "Purge automático de alertas lidos > 2 anos (política de retenção operacional)"
    identifiers:
      - name: "id"
        type: "technical"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "ativo_id"
        type: "UUID"
        required: false
      - name: "tipo"
        type: "Enum (USO_DISCO_CRITICO, USO_MEMORIA_CRITICO, MANUTENCAO_VENCIDA, SAUDE_DEGRADADA, GARANTIA_EXPIRANDO, DEPRECIACAO_ACELERADA, OUTRO)"
        required: true
      - name: "severidade"
        type: "Enum (INFO, WARNING, CRITICAL)"
        required: true
      - name: "titulo"
        type: "String(200)"
        required: true
      - name: "mensagem"
        type: "Text"
        required: true
      - name: "status"
        type: "Enum (NAO_LIDO, LIDO, EM_TRATAMENTO, RESOLVIDO, IGNORADO)"
        required: true
      - name: "data_criacao"
        type: "Instant"
        required: true
      - name: "data_leitura"
        type: "Instant"
        required: false
      - name: "data_resolucao"
        type: "Instant"
        required: false
      - name: "usuario_leitura_id"
        type: "UUID"
        required: false
      - name: "usuario_resolucao_id"
        type: "UUID"
        required: false
      - name: "metadados"
        type: "JSONB"
        required: false
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
    relationships:
      - target: "Ativo"
        cardinality: "N:1"
        semantic: "Ativo relacionado ao alerta (opcional — alertas podem ser globais)"
      - target: "Usuario"
        cardinality: "N:1"
        semantic: "Usuário que marcou como lido — opcional"
      - target: "Usuario"
        cardinality: "N:1"
        semantic: "Usuário que resolveu o alerta — opcional"
      - target: "Auditoria"
        cardinality: "1:N"
        semantic: "Trilha de auditoria de alterações no alerta"
    invariants:
      - "Se status = LIDO, data_leitura e usuario_leitura_id são obrigatórios"
      - "Se status = RESOLVIDO, data_resolucao e usuario_resolucao_id são obrigatórios"
      - "data_leitura >= data_criacao (quando preenchida)"
      - "data_resolucao >= data_leitura (quando preenchida)"
    data_sensitivity: "internal"

  - id: "ent-usuario"
    name: "Usuario"
    description: "Usuário do sistema com credenciais de autenticação, perfil de acesso (RBAC) e vínculo opcional a funcionário para contexto patrimonial."
    table: "usuario"
    classification: "core"
    lifecycle:
      creation: "Provisionamento via createUsuario / createFuncionarioAndUsuario (admin only)"
      archival: "Inativação (ativo=false) em vez de exclusão; retenção de 7 anos para auditoria SOX"
      deletion: "Apenas anonimização LGPD (anonymize_user procedure) — hard delete proibido"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "username"
        type: "business"
      - name: "email"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "username"
        type: "String(100)"
        required: true
      - name: "email"
        type: "Email"
        required: true
      - name: "password_hash"
        type: "String(255)"
        required: true
      - name: "nome_completo"
        type: "String(200)"
        required: true
      - name: "ativo"
        type: "Boolean"
        required: true
      - name: "funcionario_id"
        type: "UUID"
        required: false
      - name: "ultimo_login"
        type: "Instant"
        required: false
      - name: "tentativas_login_falhas"
        type: "Integer"
        required: true
      - name: "bloqueado_ate"
        type: "Instant"
        required: false
      - name: "password_updated_at"
        type: "Instant"
        required: true
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
      - name: "created_by"
        type: "UUID"
        required: true
      - name: "updated_by"
        type: "UUID"
        required: true
    relationships:
      - target: "Funcionario"
        cardinality: "1:1"
        semantic: "Vínculo com cadastro de funcionário para contexto patrimonial (opcional)"
      - target: "Role"
        cardinality: "N:N"
        semantic: "Papéis de acesso do usuário (RBAC) — via tabela usuario_role"
      - target: "Auditoria"
        cardinality: "1:N"
        semantic: "Trilha de auditoria de ações realizadas por este usuário"
      - target: "Manutencao"
        cardinality: "1:N"
        semantic: "Manutenções aprovadas por este usuário (aprovador_id)"
      - target: "Alerta"
        cardinality: "1:N"
        semantic: "Alertas lidos/resolvidos por este usuário"
    invariants:
      - "username deve ser único"
      - "email deve ser único"
      - "password_hash não nulo"
      - "tentativas_login_falhas >= 0"
      - "Se bloqueado_ate preenchido, deve ser > now()"
    data_sensitivity: "confidential"

  - id: "ent-role"
    name: "Role"
    description: "Papel de acesso (perfil) que agrega permissões para controle RBAC (ex.: ADMIN, GESTOR_PATRIMONIO, TECNICO_MANUTENCAO, VISUALIZADOR)."
    table: "role"
    classification: "reference"
    lifecycle:
      creation: "Cadastro administrativo via createRole"
      archival: "Não se aplica (tabela de referência)"
      deletion: "Apenas se não houver Usuários vinculados; soft delete (ativo=false) preferido"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "nome"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "nome"
        type: "String(50)"
        required: true
      - name: "descricao"
        type: "String(200)"
        required: false
      - name: "ativo"
        type: "Boolean"
        required: true
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
    relationships:
      - target: "Permissao"
        cardinality: "N:N"
        semantic: "Permissões concedidas a este papel — via tabela role_permissao"
      - target: "Usuario"
        cardinality: "N:N"
        semantic: "Usuários que possuem este papel — via tabela usuario_role"
    invariants:
      - "nome deve ser único"
    data_sensitivity: "internal"

  - id: "ent-permissao"
    name: "Permissao"
    description: "Permissão atômica de acesso a um recurso/ação do sistema (ex.: ATIVO_CREATE, MANUTENCAO_APROVAR, ALERTA_RESOLVER, RELATORIO_CUSTO_TOTAL)."
    table: "permissao"
    classification: "reference"
    lifecycle:
      creation: "Cadastro administrativo via createPermission"
      archival: "Não se aplica"
      deletion: "Apenas se não houver Roles vinculadas"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "codigo"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "codigo"
        type: "String(100)"
        required: true
      - name: "descricao"
        type: "String(200)"
        required: false
      - name: "recurso"
        type: "String(50)"
        required: true
      - name: "acao"
        type: "String(50)"
        required: true
      - name: "ativo"
        type: "Boolean"
        required: true
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
    relationships:
      - target: "Role"
        cardinality: "N:N"
        semantic: "Roles que possuem esta permissão — via tabela role_permissao"
    invariants:
      - "codigo deve ser único"
      - "Par (recurso, acao) deve ser único"
    data_sensitivity: "internal"

  - id: "ent-funcionario"
    name: "Funcionario"
    description: "Cadastro de colaborador da empresa para vínculo com ativos (responsável), manutenções (técnico) e usuários (acesso ao sistema)."
    table: "funcionario"
    classification: "core"
    lifecycle:
      creation: "Cadastro via createFuncionario / createFuncionarioAndUsuario"
      archival: "Inativação (data_desligamento preenchida); retenção por 7 anos pós-desligamento"
      deletion: "Anonimização LGPD após período de retenção legal"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "matricula"
        type: "business"
      - name: "cpf"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "matricula"
        type: "String(30)"
        required: true
      - name: "cpf"
        type: "CPF"
        required: true
      - name: "nome_completo"
        type: "String(200)"
        required: true
      - name: "email_corporativo"
        type: "Email"
        required: true
      - name: "telefone"
        type: "String(20)"
        required: false
      - name: "cargo"
        type: "String(100)"
        required: false
      - name: "filial_id"
        type: "UUID"
        required: true
      - name: "departamento_id"
        type: "UUID"
        required: false
      - name: "data_admissao"
        type: "LocalDate"
        required: true
      - name: "data_desligamento"
        type: "LocalDate"
        required: false
      - name: "ativo"
        type: "Boolean"
        required: true
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
      - name: "created_by"
        type: "UUID"
        required: true
      - name: "updated_by"
        type: "UUID"
        required: true
    relationships:
      - target: "Filial"
        cardinality: "N:1"
        semantic: "Filial de lotação do funcionário"
      - target: "Departamento"
        cardinality: "N:1"
        semantic: "Departamento de lotação (opcional)"
      - target: "Usuario"
        cardinality: "1:1"
        semantic: "Usuário de sistema vinculado (opcional)"
      - target: "Ativo"
        cardinality: "1:N"
        semantic: "Ativos sob responsabilidade deste funcionário"
      - target: "Manutencao"
        cardinality: "1:N"
        semantic: "Manutenções executadas por este funcionário (como técnico)"
    invariants:
      - "matricula deve ser único"
      - "cpf deve ser único e válido (algoritmo CPF)"
      - "email_corporativo deve ser único"
      - "data_desligamento >= data_admissao (quando preenchida)"
      - "Se data_desligamento preenchida, ativo = false"
    data_sensitivity: "confidential"

  - id: "ent-filial"
    name: "Filial"
    description: "Unidade organizacional da empresa (sede, filiais, centros de distribuição) para segregação patrimonial e relatórios por unidade."
    table: "filial"
    classification: "reference"
    lifecycle:
      creation: "Cadastro administrativo via createFilial"
      archival: "Não se aplica"
      deletion: "Apenas se não houver Ativos/Funcionários vinculados; soft delete preferido"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "codigo"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "codigo"
        type: "String(20)"
        required: true
      - name: "nome"
        type: "String(150)"
        required: true
      - name: "endereco"
        type: "Text"
        required: false
      - name: "cidade"
        type: "String(100)"
        required: false
      - name: "estado"
        type: "String(2)"
        required: false
      - name: "cep"
        type: "String(10)"
        required: false
      - name: "ativo"
        type: "Boolean"
        required: true
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
    relationships:
      - target: "Ativo"
        cardinality: "1:N"
        semantic: "Ativos alocados nesta filial"
      - target: "Funcionario"
        cardinality: "1:N"
        semantic: "Funcionários lotados nesta filial"
      - target: "Departamento"
        cardinality: "1:N"
        semantic: "Departamentos pertencentes a esta filial"
    invariants:
      - "codigo deve ser único"
      - "nome deve ser único"
    data_sensitivity: "public"

  - id: "ent-departamento"
    name: "Departamento"
    description: "Divisão organizacional interna (TI, Facilities, Financeiro, etc.) para alocação de ativos e funcionários."
    table: "departamento"
    classification: "reference"
    lifecycle:
      creation: "Cadastro administrativo via createDepartamento"
      archival: "Não se aplica"
      deletion: "Apenas se não houver Ativos/Funcionários vinculados; soft delete preferido"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "codigo"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "codigo"
        type: "String(20)"
        required: true
      - name: "nome"
        type: "String(150)"
        required: true
      - name: "filial_id"
        type: "UUID"
        required: true
      - name: "gestor_id"
        type: "UUID"
        required: false
      - name: "ativo"
        type: "Boolean"
        required: true
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
    relationships:
      - target: "Filial"
        cardinality: "N:1"
        semantic: "Filial à qual o departamento pertence"
      - target: "Funcionario"
        cardinality: "1:N"
        semantic: "Funcionários lotados neste departamento (gestor_id referencia Funcionario)"
      - target: "Ativo"
        cardinality: "1:N"
        semantic: "Ativos alocados neste departamento"
    invariants:
      - "codigo deve ser único por filial"
      - "nome deve ser único por filial"
    data_sensitivity: "public"

  - id: "ent-localizacao"
    name: "Localizacao"
    description: "Local físico específico (prédio, andar, sala, rack) para rastreamento preciso de ativos."
    table: "localizacao"
    classification: "reference"
    lifecycle:
      creation: "Cadastro via createLocalizacao (testes) ou administrativo"
      archival: "Não se aplica"
      deletion: "Apenas se não houver Ativos vinculados"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "codigo"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "codigo"
        type: "String(30)"
        required: true
      - name: "nome"
        type: "String(150)"
        required: true
      - name: "filial_id"
        type: "UUID"
        required: true
      - name: "predio"
        type: "String(50)"
        required: false
      - name: "andar"
        type: "String(20)"
        required: false
      - name: "sala"
        type: "String(50)"
        required: false
      - name: "rack"
        type: "String(50)"
        required: false
      - name: "ativo"
        type: "Boolean"
        required: true
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
    relationships:
      - target: "Filial"
        cardinality: "N:1"
        semantic: "Filial onde a localização se encontra"
      - target: "Ativo"
        cardinality: "1:N"
        semantic: "Ativos fisicamente nesta localização"
    invariants:
      - "codigo deve ser único por filial"
    data_sensitivity: "public"

  - id: "ent-fornecedor"
    name: "Fornecedor"
    description: "Fornecedor de ativos, peças ou serviços de manutenção para gestão de contratos, garantias e custos."
    table: "fornecedor"
    classification: "reference"
    lifecycle:
      creation: "Cadastro via createFornecedor"
      archival: "Inativação (ativo=false) em vez de exclusão"
      deletion: "Apenas se não houver Ativos/Manutenções vinculados"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "cnpj"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "cnpj"
        type: "CNPJ"
        required: true
      - name: "razao_social"
        type: "String(200)"
        required: true
      - name: "nome_fantasia"
        type: "String(150)"
        required: false
      - name: "email_contato"
        type: "Email"
        required: false
      - name: "telefone_contato"
        type: "String(20)"
        required: false
      - name: "endereco"
        type: "Text"
        required: false
      - name: "ativo"
        type: "Boolean"
        required: true
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
    relationships:
      - target: "Ativo"
        cardinality: "1:N"
        semantic: "Ativos adquiridos deste fornecedor"
      - target: "Manutencao"
        cardinality: "1:N"
        semantic: "Manutenções executadas por este fornecedor"
    invariants:
      - "cnpj deve ser único e válido (algoritmo CNPJ)"
    data_sensitivity: "internal"

  - id: "ent-ativo-detalhe-hardware"
    name: "AtivoDetalheHardware"
    description: "Detalhes técnicos de hardware (componentes) para ativos do tipo HARDWARE: adaptadores de rede, discos, memórias, processadores."
    table: "ativo_detalhe_hardware"
    classification: "supporting"
    lifecycle:
      creation: "Criação automática via updateHealthCheck / coleta de inventário (agent)"
      archival: "Particionamento por ativo_id; remove em cascata quando Ativo é descartado"
      deletion: "Cascata com Ativo (ON DELETE CASCADE)"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "ativo_id"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "ativo_id"
        type: "UUID"
        required: true
      - name: "processador_modelo"
        type: "String(200)"
        required: false
      - name: "processador_nucleos"
        type: "Integer"
        required: false
      - name: "memoria_total_gb"
        type: "Integer"
        required: false
      - name: "memoria_tipo"
        type: "String(50)"
        required: false
      - name: "disco_total_gb"
        type: "Integer"
        required: false
      - name: "disco_tipo"
        type: "Enum (HDD, SSD, NVME, HIBRIDO)"
        required: false
      - name: "placa_mae_modelo"
        type: "String(200)"
        required: false
      - name: "bios_versao"
        type: "String(50)"
        required: false
      - name: "coletado_em"
        type: "Instant"
        required: true
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
    relationships:
      - target: "Ativo"
        cardinality: "N:1"
        semantic: "Ativo pai (1:1 lógico, mas modelado como N:1 para flexibilidade)"
      - target: "AdaptadorRede"
        cardinality: "1:N"
        semantic: "Adaptadores de rede do ativo"
      - target: "Disco"
        cardinality: "1:N"
        semantic: "Discos de armazenamento do ativo"
      - target: "Memoria"
        cardinality: "1:N"
        semantic: "Módulos de memória do ativo"
    invariants:
      - "ativo_id deve ser único (enforça 1:1 lógico)"
      - "coletado_em <= now()"
    data_sensitivity: "internal"

  - id: "ent-adaptador-rede"
    name: "AdaptadorRede"
    description: "Adaptador de interface de rede (NIC) inventariado no ativo de hardware."
    table: "adaptador_rede"
    classification: "supporting"
    lifecycle:
      creation: "Coleta automática via agent de inventário"
      archival: "Remove em cascata com AtivoDetalheHardware"
      deletion: "Cascata"
    identifiers:
      - name: "id"
        type: "technical"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "ativo_detalhe_hardware_id"
        type: "UUID"
        required: true
      - name: "nome"
        type: "String(100)"
        required: true
      - name: "endereco_mac"
        type: "MACAddress"
        required: true
      - name: "endereco_ip"
        type: "InetAddress"
        required: false
      - name: "velocidade_mbps"
        type: "Integer"
        required: false
      - name: "tipo"
        type: "Enum (ETHERNET, WIFI, BLUETOOTH, VIRTUAL, OUTRO)"
        required: true
      - name: "created_at"
        type: "Instant"
        required: true
    relationships:
      - target: "AtivoDetalheHardware"
        cardinality: "N:1"
        semantic: "Detalhe de hardware pai"
    invariants:
      - "endereco_mac deve ser único por ativo_detalhe_hardware_id"
      - "Formato MAC válido (XX:XX:XX:XX:XX:XX)"
    data_sensitivity: "internal"

  - id: "ent-disco"
    name: "Disco"
    description: "Disco de armazenamento (HDD/SSD/NVMe) inventariado no ativo de hardware."
    table: "disco"
    classification: "supporting"
    lifecycle:
      creation: "Coleta automática via agent de inventário"
      archival: "Remove em cascata com AtivoDetalheHardware"
      deletion: "Cascata"
    identifiers:
      - name: "id"
        type: "technical"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "ativo_detalhe_hardware_id"
        type: "UUID"
        required: true
      - name: "modelo"
        type: "String(100)"
        required: true
      - name: "numero_serie"
        type: "String(100)"
        required: false
      - name: "capacidade_gb"
        type: "Integer"
        required: true
      - name: "tipo"
        type: "Enum (HDD, SSD, NVME, HIBRIDO)"
        required: true
      - name: "interface"
        type: "String(30)"
        required: false
      - name: "saude_percentual"
        type: "Integer"
        required: false
      - name: "created_at"
        type: "Instant"
        required: true
    relationships:
      - target: "AtivoDetalheHardware"
        cardinality: "N:1"
        semantic: "Detalhe de hardware pai"
    invariants:
      - "capacidade_gb > 0"
      - "saude_percentual entre 0 e 100 (quando preenchido)"
    data_sensitivity: "internal"

  - id: "ent-memoria"
    name: "Memoria"
    description: "Módulo de memória RAM inventariado no ativo de hardware."
    table: "memoria"
    classification: "supporting"
    lifecycle:
      creation: "Coleta automática via agent de inventário"
      archival: "Remove em cascata com AtivoDetalheHardware"
      deletion: "Cascata"
    identifiers:
      - name: "id"
        type: "technical"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "ativo_detalhe_hardware_id"
        type: "UUID"
        required: true
      - name: "modelo"
        type: "String(100)"
        required: true
      - name: "capacidade_gb"
        type: "Integer"
        required: true
      - name: "tipo"
        type: "String(30)"
        required: false
      - name: "velocidade_mhz"
        type: "Integer"
        required: false
      - name: "slot"
        type: "String(20)"
        required: false
      - name: "created_at"
        type: "Instant"
        required: true
    relationships:
      - target: "AtivoDetalheHardware"
        cardinality: "N:1"
        semantic: "Detalhe de hardware pai"
    invariants:
      - "capacidade_gb > 0"
    data_sensitivity: "internal"

  - id: "ent-auditoria"
    name: "Auditoria"
    description: "Trilha de auditoria imutável (WORM) de todas as operações relevantes: criação/alteração/exclusão de ativos, manutenções, alertas, usuários, concessão/revogação de roles, login/logout, exportações."
    table: "auditoria"
    classification: "audit"
    lifecycle:
      creation: "Inserção automática via listeners JPA / interceptadores (doFilterInternal, preUpdate, onUpdate) — nunca via API direta"
      archival: "Particionamento por data_hora (mensal); cópia assíncrona para WORM storage (audit-worm-storage) para retenção 7 anos SOX"
      deletion: "Proibido no banco transacional; purga apenas no WORM storage após 7 anos com aprovação Legal"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "correlation_id"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "correlation_id"
        type: "UUID"
        required: true
      - name: "usuario_id"
        type: "UUID"
        required: false
      - name: "entidade"
        type: "String(100)"
        required: true
      - name: "entidade_id"
        type: "UUID"
        required: true
      - name: "acao"
        type: "Enum (CREATE, UPDATE, DELETE, LOGIN, LOGOUT, EXPORT, IMPORT, APPROVE, REJECT, READ)"
        required: true
      - name: "valores_anteriores"
        type: "JSONB"
        required: false
      - name: "valores_novos"
        type: "JSONB"
        required: false
      - name: "metadados"
        type: "JSONB"
        required: false
      - name: "ip_origem"
        type: "InetAddress"
        required: false
      - name: "user_agent"
        type: "String(500)"
        required: false
      - name: "data_hora"
        type: "Instant"
        required: true
      - name: "hash_encadeado"
        type: "String(64)"
        required: true
    relationships:
      - target: "Usuario"
        cardinality: "N:1"
        semantic: "Usuário que realizou a ação (opcional para ações de sistema)"
    invariants:
      - "Tabela é append-only (INSERT only) — UPDATE/DELETE proibidos por trigger/constraint"
      - "hash_encadeado = SHA256(hash_anterior || payload_atual) — garante imutabilidade encadeada"
      - "data_hora é monotonicamente crescente por correlation_id"
      - "Para UPDATE: valores_anteriores e valores_novos obrigatórios"
      - "Para CREATE: apenas valores_novos obrigatório"
      - "Para DELETE: apenas valores_anteriores obrigatório"
    data_sensitivity: "restricted"

  - id: "ent-usuario-role"
    name: "UsuarioRole"
    description: "Tabela de associação N:N entre Usuario e Role para RBAC."
    table: "usuario_role"
    classification: "supporting"
    lifecycle:
      creation: "Atribuição de papel via admin (hasPermission check)"
      archival: "Não se aplica"
      deletion: "Remoção de papel (revogação) — mantém histórico em Auditoria"
    identifiers:
      - name: "usuario_id + role_id"
        type: "business"
    attributes:
      - name: "usuario_id"
        type: "UUID"
        required: true
      - name: "role_id"
        type: "UUID"
        required: true
      - name: "concedido_por"
        type: "UUID"
        required: true
      - name: "concedido_em"
        type: "Instant"
        required: true
      - name: "expira_em"
        type: "Instant"
        required: false
    relationships:
      - target: "Usuario"
        cardinality: "N:1"
        semantic: "Usuário detentor do papel"
      - target: "Role"
        cardinality: "N:1"
        semantic: "Papel concedido"
    invariants:
      - "Par (usuario_id, role_id) único"
      - "Se expira_em preenchido, deve ser > concedido_em"
    data_sensitivity: "internal"

  - id: "ent-role-permissao"
    name: "RolePermissao"
    description: "Tabela de associação N:N entre Role e Permissao para composição de perfis de acesso."
    table: "role_permissao"
    classification: "supporting"
    lifecycle:
      creation: "Configuração de papel via admin (createRole + associação de permissões)"
      archival: "Não se aplica"
      deletion: "Remoção de permissão do papel — mantém histórico em Auditoria"
    identifiers:
      - name: "role_id + permissao_id"
        type: "business"
    attributes:
      - name: "role_id"
        type: "UUID"
        required: true
      - name: "permissao_id"
        type: "UUID"
        required: true
      - name: "concedido_por"
        type: "UUID"
        required: true
      - name: "concedido_em"
        type: "Instant"
        required: true
    relationships:
      - target: "Role"
        cardinality: "N:1"
        semantic: "Papel que recebe a permissão"
      - target: "Permissao"
        cardinality: "N:1"
        semantic: "Permissão concedida ao papel"
    invariants:
      - "Par (role_id, permissao_id) único"
    data_sensitivity: "internal"
```

## 3. Evidence & Assumptions
```yaml
assumptions:
  - id: "ASM-001"
    claim:
      object: "ativo.tag_patrimonial"
      interpretation: "Identificador de negócio único e obrigatório para rastreamento patrimonial; usado em etiquetas físicas e relatórios."
    confidence: 0.95
    evidence:
      - source: "db-context#L5 (domain terms: Ativo, tag_patrimonial implícito)"
      - source: "glossario#L68 (createAtivo: cadastro de novo ativo no patrimônio)"
    status: "validated"

  - id: "ASM-002"
    claim:
      object: "ativo.numero_serie"
      interpretation: "Número de série do fabricante; único por tipo de ativo; opcional pois nem todos ativos têm (ex.: móveis)."
    confidence: 0.85
    evidence:
      - source: "glossario#L68 (createAtivo: registrando suas informações essenciais para rastreamento)"
      - source: "db-context#L5 (domain: hardware, software, móveis)"
    status: "requires_validation"

  - id: "ASM-003"
    claim:
      object: "manutencao.numero_ordem"
      interpretation: "Identificador de negócio sequencial/formatado para rastreamento de ordens de serviço; único."
    confidence: 0.9
    evidence:
      - source: "glossario#L28 (iniciar: ação de dar início a uma ordem de manutenção)"
      - source: "glossario#L35 (concluir: ação de finalizar uma ordem de manutenção)"
      - source: "db-context#L5 (domain: ciclo de vida de manutenções preventiva/corretiva)"
    status: "validated"

  - id: "ASM-004"
    claim:
      object: "manutencao.custo_total"
      interpretation: "Campo derivado (custo_pecas + custo_mao_obra) armazenado para performance de agregação em custoTotalPorAtivo."
    confidence: 0.8
    evidence:
      - source: "glossario#L30 (custoTotalPorAtivo: somatório de todos os gastos de manutenção agrupados por ativo)"
      - source: "db-context#L10 (performance: custoTotalPorAtivo ≤ 2s na read-replica)"
    status: "requires_validation"

  - id: "ASM-005"
    claim:
      object: "alerta.tipo"
      interpretation: "Enum com valores baseados no AlertNotificationService.checkResourceUsageAlerts (complexidade 17) — uso de disco, memória, manutenção vencida, saúde degradada."
    confidence: 0.85
    evidence:
      - source: "db-context#L5 (AlertNotificationService.checkResourceUsageAlerts, complexidade ciclomática 17)"
      - source: "glossario#L58 (getRecentAlerts: recupera notificações/avisos mais recentes)"
      - source: "glossario#L59 (listarAlertas: recuperar relação de alertas gerados)"
    status: "requires_validation"

  - id: "ASM-006"
    claim:
      object: "usuario.setUsername (stub vazio)"
      interpretation: "Método setUsername em Usuario.java:86 tem corpo vazio — indica que username é imutável após criação (setado apenas no construtor/builder)."
    confidence: 0.95
    evidence:
      - source: "diagnóstico#L27 (função com corpo vazio: Usuario.java:86 setUsername)"
    status: "validated"

  - id: "ASM-007"
    claim:
      object: "ativo.uso_disco_percentual, memoria_total_gb, processador, sistema_operacional"
      interpretation: "Campos de telemetria de hardware preenchidos via updateHealthCheck / updateScalars (coleta de inventário por agent)."
    confidence: 0.9
    evidence:
      - source: "glossario#L85 (updateHealthCheck: atualiza status de saúde com dados de hardware e disco)"
      - source: "glossario#L86 (updateScalars: atualiza valores numéricos/textuais simples de ativo de hardware)"
      - source: "glossario#L57 (getHealthHistory: histórico de indicadores de saúde, uso de disco)"
    status: "validated"

  - id: "ASM-008"
    claim:
      object: "ativo_detalhe_hardware + adaptador_rede + disco + memoria"
      interpretation: "Modelo normalizado para componentes de hardware; deleteByAtivoDetalheHardwareId remove em lote componentes vinculados."
    confidence: 0.95
    evidence:
      - source: "glossario#L42 (deleteByAtivoDetalheHardwareId: remoção em lote de componentes de hardware vinculados a ID de detalhe de ativo)"
      - source: "glossario#L43 (findByAtivoDetalheHardwareId: busca componentes por ID de detalhe de hardware)"
    status: "validated"

  - id: "ASM-009"
    claim:
      object: "auditoria.hash_encadeado"
      interpretation: "Hash SHA-256 encadeado (hash_anterior || payload_atual) para imutabilidade WORM e conformidade SOX — conforme db-context §11."
    confidence: 0.95
    evidence:
      - source: "db-context#L11 (SOX: hash encadeado (hash anterior + payload))"
      - source: "db-context#L8 (audit-worm-storage: cópia imutável de Auditoria para retenção SOX 7 anos)"
    status: "validated"

  - id: "ASM-010"
    claim:
      object: "usuario.funcionario_id (1:1 opcional)"
      interpretation: "Vínculo opcional entre usuário de sistema e cadastro de funcionário; createFuncionarioAndUsuario cria ambos simultaneamente."
    confidence: 0.9
    evidence:
      - source: "glossario#L71 (createFuncionarioAndUsuario: cadastrar funcionário e simultaneamente criar seu acesso de usuário)"
      - source: "glossario#L72 (createUsuario: criação de usuário de teste para integração)"
    status: "validated"

  - id: "ASM-011"
    claim:
      object: "tipo_ativo.campos_tecnicos_obrigatorios (JSONB)"
      interpretation: "Metadado dinâmico definindo quais campos técnicos são obrigatórios por tipo (ex.: HARDWARE exige processador, memoria; SOFTWARE exige versão, licença)."
    confidence: 0.7
    evidence:
      - source: "db-context#L5 (domain: ativos hardware, software, móveis — características diferentes)"
      - source: "glossario#L69 (createTipoAtivo: definindo suas características e regras de depreciação)"
    status: "requires_validation"

  - id: "ASM-012"
    claim:
      object: "filial, departamento, localizacao — hierarquia organizacional"
      interpretation: "Filial → Departamento → Localização; Ativo referencia os três (filial obrigatório, departamento e localização opcionais)."
    confidence: 0.85
    evidence:
      - source: "glossario#L69 (createDepartamento, createFilial, createLocalizacao)"
      - source: "db-context#L5 (domain: gestão de patrimônio corporativo)"
    status: "requires_validation"

  - id: "ASM-013"
    claim:
      object: "fornecedor.cnpj"
      interpretation: "CNPJ válido e único; usado para validação fiscal e contratos de manutenção/aquisição."
    confidence: 0.95
    evidence:
      - source: "glossario#L70 (createFornecedor: cadastrar fornecedor para associar a ativos, alertas, manutenções)"
      - source: "db-context#L5 (domain: fornecedores)"
    status: "validated"

  - id: "ASM-014"
    claim:
      object: "manutencao.status transitions"
      interpretation: "Máquina de estados: SOLICITADA → APROVADA → EM_ANDAMENTO → CONCLUIDA; qualquer → CANCELADA; APROVADA → AGUARDANDO_PECAS → EM_ANDAMENTO."
    confidence: 0.8
    evidence:
      - source: "glossario#L26 (aprovar: autorizar solicitação de manutenção)"
      - source: "glossario#L28 (iniciar: dar início a ordem de manutenção)"
      - source: "glossario#L35 (concluir: finalizar ordem de manutenção)"
      - source: "glossario#L27 (cancelar: interromper e invalidar solicitação)"
    status: "requires_validation"

  - id: "ASM-015"
    claim:
      object: "alerta.status transitions"
      interpretation: "NAO_LIDO → LIDO → (EM_TRATAMENTO →) RESOLVIDO; ou NAO_LIDO → IGNORADO."
    confidence: 0.8
    evidence:
      - source: "glossario#L59 (markAsRead: registra visualização, atualiza status para 'lido')"
      - source: "glossario#L58 (listarAlertas: acompanhar situações de atenção)"
    status: "requires_validation"

  - id: "ASM-016"
    claim:
      object: "Motor de banco não definido"
      interpretation: "Todos os tipos (UUID, NUMERIC, JSONB, ENUM, INET, MACAddress) são lógicos; mapeamento físico depende de PostgreSQL vs Oracle vs SQL Server vs MySQL (Gap Crítico #1 no db-context)."
    confidence: 1.0
    evidence:
      - source: "db-context#L12 (Gaps Críticos: Motor de banco NÃO DEFINIDO)"
      - source: "stack JSON (motor_de_banco: 'nenhum motor de banco conhecido encontrado nas dependências')"
    status: "validated"

  - id: "ASM-017"
    claim:
      object: "ORM = JPA/Hibernate"
      interpretation: "Inferido da estrutura de pacotes model, repository, mapper no diagnóstico; db-context §3 confirma 'mapeadas via JPA/Hibernate — inferido da estrutura de pacotes'."
    confidence: 0.9
    evidence:
      - source: "db-context#L3 (JPA/Hibernate inferido de model, repository, mapper)"
      - source: "diagnóstico (pacotes: br.com.aegispatrimonio.model, repository, mapper)"
    status: "validated"

  - id: "ASM-018"
    claim:
      object: "custoTotalPorAtivo como agregação em read-replica"
      interpretation: "Query analítica offloadada para read-replica; target ≤ 2s; usa índices compostos em manutencao(ativo_id, data_conclusao, custo_total)."
    confidence: 0.85
    evidence:
      - source: "db-context#L10 (Performance: custoTotalPorAtivo ≤ 2s na read-replica)"
      - source: "db-context#L6 (Consumers: aegis-patrimonio-read-replica para offload analítico)"
      - source: "glossario#L30 (custoTotalPorAtivo: somatório de gastos de manutenção por ativo)"
    status: "requires_validation"

  - id: "ASM-019"
    claim:
      object: "ManutencaoSpecification.build (complexidade 14)"
      interpretation: "Query dinâmica de filtros de manutenção (por ativo, tipo, status, datas, fornecedor, técnico, faixa de custo) — requer índices compostos otimizados."
    confidence: 0.9
    evidence:
      - source: "diagnóstico#L8 (ManutencaoSpecification.java:26 build() complexidade 14)"
      - source: "db-context#L10 (Latência p95 query dinâmica ManutencaoSpecification ≤ 50ms)"
    status: "validated"

  - id: "ASM-020"
    claim:
      object: "AtivoMapper.toDTO (complexidade 14)"
      interpretation: "Mapeamento rico de Ativo para DTO inclui relacionamentos (tipo, filial, departamento, localização, fornecedor, responsável, detalhes hardware, última manutenção, alertas ativos)."
    confidence: 0.85
    evidence:
      - source: "diagnóstico#L8 (AtivoMapper.java:15 toDTO complexidade 14)"
      - source: "glossario#L83 (toDTO: transformação de dados internos em formato padronizado para comunicação)"
    status: "requires_validation"
```

**Regra de threshold:**
| Confiança | Ação |
| :--- | :--- |
| ≥ 0.95 | Pode ser incorporado automaticamente |
| 0.80 – 0.94 | Pode ser proposto, requer revisão leve |
| 0.60 – 0.79 | Requer revisão humana explícita |
| < 0.60 | Não deve virar decisão de schema |