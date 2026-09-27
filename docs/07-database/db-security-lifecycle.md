# Database Security & Data Lifecycle — aegis_patrimonio

> **Versão:** 1.0 · **Owner:** Aegis Patrimônio — Engenharia de Segurança de Dados · **Status:** Draft

## 1. Roles & Permissions
```yaml
roles:
  - name: "ADMIN"
    purpose: "Acesso total ao sistema: gestão de usuários, roles, permissões, configurações, auditoria e operações LGPD (direito ao esquecimento)"
    permissions:
      tables:
        - table: "usuario"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "role"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "permissao"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "usuario_role"
          operations: ["SELECT", "INSERT", "DELETE"]
        - table: "role_permissao"
          operations: ["SELECT", "INSERT", "DELETE"]
        - table: "auditoria"
          operations: ["SELECT"]
        - table: "ativo"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "manutencao"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "alerta"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "funcionario"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "filial"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "departamento"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "localizacao"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "fornecedor"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "tipo_ativo"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "ativo_detalhe_hardware"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "adaptador_rede"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "disco"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "memoria"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
    source_refs:
      - "<!-- source: security-policies#L180-L210 -->"
      - "<!-- source: db-schema-spec#L1100-L1150 -->"

  - name: "AUDITOR"
    purpose: "Acesso somente leitura a dados de auditoria, ativos, manutenções, alertas e relatórios para fins de compliance (SOX, LGPD)"
    permissions:
      tables:
        - table: "auditoria"
          operations: ["SELECT"]
        - table: "ativo"
          operations: ["SELECT"]
        - table: "manutencao"
          operations: ["SELECT"]
        - table: "alerta"
          operations: ["SELECT"]
        - table: "funcionario"
          operations: ["SELECT"]
        - table: "filial"
          operations: ["SELECT"]
        - table: "departamento"
          operations: ["SELECT"]
        - table: "localizacao"
          operations: ["SELECT"]
        - table: "fornecedor"
          operations: ["SELECT"]
        - table: "tipo_ativo"
          operations: ["SELECT"]
        - table: "ativo_detalhe_hardware"
          operations: ["SELECT"]
        - table: "adaptador_rede"
          operations: ["SELECT"]
        - table: "disco"
          operations: ["SELECT"]
        - table: "memoria"
          operations: ["SELECT"]
        - table: "usuario"
          operations: ["SELECT"]
        - table: "role"
          operations: ["SELECT"]
        - table: "permissao"
          operations: ["SELECT"]
        - table: "usuario_role"
          operations: ["SELECT"]
        - table: "role_permissao"
          operations: ["SELECT"]
    source_refs:
      - "<!-- source: security-policies#L180-L210 -->"

  - name: "GESTOR"
    purpose: "Gestão patrimonial: CRUD de ativos, manutenções (incluir aprovar/cancelar/concluir/iniciar), health checks, alertas, configurações de leitura"
    permissions:
      tables:
        - table: "ativo"
          operations: ["SELECT", "INSERT", "UPDATE"]
        - table: "manutencao"
          operations: ["SELECT", "INSERT", "UPDATE"]
        - table: "alerta"
          operations: ["SELECT", "UPDATE"]
        - table: "funcionario"
          operations: ["SELECT"]
        - table: "filial"
          operations: ["SELECT"]
        - table: "departamento"
          operations: ["SELECT"]
        - table: "localizacao"
          operations: ["SELECT"]
        - table: "fornecedor"
          operations: ["SELECT"]
        - table: "tipo_ativo"
          operations: ["SELECT"]
        - table: "ativo_detalhe_hardware"
          operations: ["SELECT", "INSERT", "UPDATE"]
        - table: "adaptador_rede"
          operations: ["SELECT", "INSERT", "UPDATE"]
        - table: "disco"
          operations: ["SELECT", "INSERT", "UPDATE"]
        - table: "memoria"
          operations: ["SELECT", "INSERT", "UPDATE"]
        - table: "auditoria"
          operations: ["SELECT"]
    source_refs:
      - "<!-- source: security-policies#L180-L210 -->"

  - name: "OPERADOR"
    purpose: "Operação diária: leitura de ativos próprios, criação de solicitações de manutenção, leitura de alertas próprios, health checks de ativos sob responsabilidade"
    permissions:
      tables:
        - table: "ativo"
          operations: ["SELECT"]
        - table: "manutencao"
          operations: ["SELECT", "INSERT"]
        - table: "alerta"
          operations: ["SELECT", "UPDATE"]
        - table: "funcionario"
          operations: ["SELECT"]
        - table: "filial"
          operations: ["SELECT"]
        - table: "departamento"
          operations: ["SELECT"]
        - table: "localizacao"
          operations: ["SELECT"]
        - table: "tipo_ativo"
          operations: ["SELECT"]
        - table: "ativo_detalhe_hardware"
          operations: ["SELECT"]
        - table: "adaptador_rede"
          operations: ["SELECT"]
        - table: "disco"
          operations: ["SELECT"]
        - table: "memoria"
          operations: ["SELECT"]
    source_refs:
      - "<!-- source: security-policies#L180-L210 -->"

  - name: "SYSTEM_SCHEDULER"
    purpose: "Identidade técnica para jobs agendados (checkResourceUsageAlerts, updateHealthCheck) — escrita em alerta, ativo (health check), auditoria"
    permissions:
      tables:
        - table: "alerta"
          operations: ["INSERT", "UPDATE"]
        - table: "ativo"
          operations: ["UPDATE"]
        - table: "auditoria"
          operations: ["INSERT"]
        - table: "manutencao"
          operations: ["SELECT"]
    source_refs:
      - "<!-- source: security-policies#L45-L55 -->"
      - "<!-- source: db-schema-spec#L1300-L1350 -->"
```

## 2. Row-Level Security
```yaml
row_level_security:
  - table: "ativo"
    enabled: true
    policies:
      - name: "rls_ativo_filial"
        operation: "ALL"
        using: "filial_id IN (SELECT filial_id FROM funcionario WHERE id = current_setting('app.current_user_funcionario_id')::uuid)"
        description: "Usuário vê apenas ativos da sua filial (via funcionário vinculado ao usuário)"
        source_refs:
          - "<!-- source: security-policies#L60-L75 -->"
          - "<!-- source: db-schema-spec#L15-L25 -->"
      - name: "rls_ativo_departamento"
        operation: "ALL"
        using: "departamento_id IN (SELECT departamento_id FROM funcionario WHERE id = current_setting('app.current_user_funcionario_id')::uuid)"
        description: "Usuário vê apenas ativos do seu departamento (quando preenchido)"
        source_refs:
          - "<!-- source: db-schema-spec#L30-L35 -->"

  - table: "manutencao"
    enabled: true
    policies:
      - name: "rls_manutencao_filial"
        operation: "ALL"
        using: "ativo_id IN (SELECT id FROM ativo WHERE filial_id IN (SELECT filial_id FROM funcionario WHERE id = current_setting('app.current_user_funcionario_id')::uuid))"
        description: "Usuário vê apenas manutenções de ativos da sua filial"
        source_refs:
          - "<!-- source: db-schema-spec#L200-L210 -->"
      - name: "rls_manutencao_tecnico"
        operation: "SELECT"
        using: "tecnico_responsavel_id = current_setting('app.current_user_funcionario_id')::uuid"
        description: "Técnico vê apenas manutenções atribuídas a ele"
        source_refs:
          - "<!-- source: db-schema-spec#L220-L225 -->"

  - table: "alerta"
    enabled: true
    policies:
      - name: "rls_alerta_filial"
        operation: "ALL"
        using: "ativo_id IS NULL OR ativo_id IN (SELECT id FROM ativo WHERE filial_id IN (SELECT filial_id FROM funcionario WHERE id = current_setting('app.current_user_funcionario_id')::uuid))"
        description: "Usuário vê alertas globais (ativo_id NULL) ou de ativos da sua filial"
        source_refs:
          - "<!-- source: db-schema-spec#L300-L310 -->"

  - table: "funcionario"
    enabled: true
    policies:
      - name: "rls_funcionario_filial"
        operation: "ALL"
        using: "filial_id IN (SELECT filial_id FROM funcionario WHERE id = current_setting('app.current_user_funcionario_id')::uuid)"
        description: "Usuário vê apenas funcionários da sua filial"
        source_refs:
          - "<!-- source: db-schema-spec#L500-L510 -->"

  - table: "departamento"
    enabled: true
    policies:
      - name: "rls_departamento_filial"
        operation: "ALL"
        using: "filial_id IN (SELECT filial_id FROM funcionario WHERE id = current_setting('app.current_user_funcionario_id')::uuid)"
        description: "Usuário vê apenas departamentos da sua filial"
        source_refs:
          - "<!-- source: db-schema-spec#L550-L560 -->"

  - table: "localizacao"
    enabled: true
    policies:
      - name: "rls_localizacao_filial"
        operation: "ALL"
        using: "filial_id IN (SELECT filial_id FROM funcionario WHERE id = current_setting('app.current_user_funcionario_id')::uuid)"
        description: "Usuário vê apenas localizações da sua filial"
        source_refs:
          - "<!-- source: db-schema-spec#L600-L610 -->"

  - table: "auditoria"
    enabled: true
    policies:
      - name: "rls_auditoria_admin_auditor"
        operation: "SELECT"
        using: "current_setting('app.current_user_role') IN ('ADMIN','AUDITOR')"
        description: "Apenas ADMIN e AUDITOR podem ler auditoria (SOX/LGPD)"
        source_refs:
          - "<!-- source: security-policies#L180-L210 -->"
          - "<!-- source: db-schema-spec#L800-L820 -->"
```

## 3. Sensitive Data
```yaml
sensitive_data:
  - field: "usuario.password_hash"
    classification: "restricted"
    protection: "encryption"
    details: "Hash bcrypt/Argon2 — nunca logado, nunca exposto em API. Armazenado apenas hash + salt."
    source_refs:
      - "<!-- source: db-schema-spec#L400-L410 -->"
      - "<!-- source: security-policies#L250-L260 -->"

  - field: "usuario.email"
    classification: "confidential"
    protection: "restricted-access"
    details: "PII — mascarado em logs não-auditoria (ex.: u***@d***.com). Acesso restrito a ADMIN/AUDITOR/GESTOR (own)."
    source_refs:
      - "<!-- source: db-schema-spec#L405-L415 -->"
      - "<!-- source: security-policies#L280-L290 -->"

  - field: "usuario.username"
    classification: "confidential"
    protection: "restricted-access"
    details: "Identificador de login — imutável após criação (stub setUsername vazio)."
    source_refs:
      - "<!-- source: db-schema-spec#L405-L415 -->"
      - "<!-- source: security-policies#L230-L240 -->"

  - field: "usuario.tentativas_login_falhas"
    classification: "confidential"
    protection: "restricted-access"
    details: "Contador de segurança — usado para lockout."
    source_refs:
      - "<!-- source: db-schema-spec#L425-L430 -->"

  - field: "usuario.bloqueado_ate"
    classification: "confidential"
    protection: "restricted-access"
    details: "Timestamp de bloqueio por força bruta."
    source_refs:
      - "<!-- source: db-schema-spec#L430-L435 -->"

  - field: "funcionario.cpf"
    classification: "restricted"
    protection: "encryption|masking"
    details: "PII sensível LGPD — criptografado em repouso (AES-256 TDE), mascarado em logs (ex.: ***.****.***-**). Validação algorítmica via function validar_cpf."
    source_refs:
      - "<!-- source: db-schema-spec#L520-L530 -->"
      - "<!-- source: security-policies#L120-L140 -->"
      - "<!-- source: db-schema-spec#L1400-L1410 -->"

  - field: "funcionario.email_corporativo"
    classification: "confidential"
    protection: "restricted-access|masking"
    details: "PII — mascarado em logs não-auditoria."
    source_refs:
      - "<!-- source: db-schema-spec#L530-L535 -->"
      - "<!-- source: security-policies#L280-L290 -->"

  - field: "funcionario.telefone"
    classification: "confidential"
    protection: "restricted-access"
    details: "PII — acesso restrito."
    source_refs:
      - "<!-- source: db-schema-spec#L535-L540 -->"

  - field: "funcionario.data_admissao"
    classification: "confidential"
    protection: "restricted-access"
    details: "Data sensível para compliance trabalhista."
    source_refs:
      - "<!-- source: db-schema-spec#L545-L550 -->"

  - field: "funcionario.data_desligamento"
    classification: "confidential"
    protection: "restricted-access"
    details: "Data sensível — dispara anonimização LGPD quando preenchida."
    source_refs:
      - "<!-- source: db-schema-spec#L550-L555 -->"

  - field: "ativo.valor_aquisicao"
    classification: "confidential"
    protection: "restricted-access"
    details: "Valor financeiro do ativo — agregado em custoTotalPorAtivo (read-replica)."
    source_refs:
      - "<!-- source: db-schema-spec#L45-L50 -->"
      - "<!-- source: security-policies#L120-L140 -->"

  - field: "manutencao.custo_pecas"
    classification: "confidential"
    protection: "restricted-access"
    details: "Custo financeiro — parte de custo_total."
    source_refs:
      - "<!-- source: db-schema-spec#L230-L235 -->"

  - field: "manutencao.custo_mao_obra"
    classification: "confidential"
    protection: "restricted-access"
    details: "Custo financeiro — parte de custo_total."
    source_refs:
      - "<!-- source: db-schema-spec#L235-L240 -->"

  - field: "manutencao.custo_total"
    classification: "confidential"
    protection: "restricted-access"
    details: "Custo total derivado (custo_pecas + custo_mao_obra) — usado em vw_custo_manutencao_por_ativo (materialized view)."
    source_refs:
      - "<!-- source: db-schema-spec#L240-L245 -->"
      - "<!-- source: db-schema-spec#L1200-L1220 -->"

  - field: "fornecedor.cnpj"
    classification: "internal"
    protection: "restricted-access|masking"
    details: "Identificador fiscal — validação algorítmica via function validar_cnpj. Mascarado em logs (ex.: **.***.***/****-**)."
    source_refs:
      - "<!-- source: db-schema-spec#L650-L660 -->"
      - "<!-- source: db-schema-spec#L1410-L1420 -->"

  - field: "auditoria.valores_anteriores"
    classification: "restricted"
    protection: "encryption|restricted-access"
    details: "Snapshot completo pré-alteração — contém PII e dados sensíveis. Armazenado em WORM (Object Lock Compliance Mode). Acesso apenas ADMIN/AUDITOR."
    source_refs:
      - "<!-- source: db-schema-spec#L850-L860 -->"
      - "<!-- source: security-policies#L120-L140 -->"
      - "<!-- source: security-policies#L350-L370 -->"

  - field: "auditoria.valores_novos"
    classification: "restricted"
    protection: "encryption|restricted-access"
    details: "Snapshot completo pós-alteração — contém PII e dados sensíveis. Armazenado em WORM."
    source_refs:
      - "<!-- source: db-schema-spec#L860-L870 -->"
      - "<!-- source: security-policies#L350-L370 -->"

  - field: "auditoria.metadados"
    classification: "restricted"
    protection: "encryption|restricted-access"
    details: "Metadados de contexto (IP, user-agent, traceId) — WORM."
    source_refs:
      - "<!-- source: db-schema-spec#L870-L880 -->"

  - field: "auditoria.ip_origem"
    classification: "restricted"
    protection: "restricted-access"
    details: "IP do ator — PII para LGPD."
    source_refs:
      - "<!-- source: db-schema-spec#L880-L885 -->"

  - field: "auditoria.user_agent"
    classification: "restricted"
    protection: "restricted-access"
    details: "User-Agent — pode identificar dispositivo/pessoa."
    source_refs:
      - "<!-- source: db-schema-spec#L885-L890 -->"

  - field: "auditoria.hash_encadeado"
    classification: "restricted"
    protection: "integrity"
    details: "SHA-256 encadeado para imutabilidade WORM — verificação de integridade periódica."
    source_refs:
      - "<!-- source: db-schema-spec#L890-L895 -->"
      - "<!-- source: security-policies#L370-L380 -->"

  - field: "tipo_ativo.campos_tecnicos_obrigatorios"
    classification: "internal"
    protection: "restricted-access"
    details: "JSONB com schema de campos obrigatórios por tipo — não sensível, mas interno."
    source_refs:
      - "<!-- source: db-schema-spec#L100-L110 -->"

  - field: "alerta.metadados"
    classification: "internal"
    protection: "restricted-access"
    details: "JSONB com contexto do alerta (thresholds, valores medidos) — interno."
    source_refs:
      - "<!-- source: db-schema-spec#L340-L350 -->"

secrets:
  prohibited_in_schema: true
  details: "Nenhuma credencial, segredo, chave de API, senha, token JWT, chave de criptografia ou certificado deve constar em defaults, seeds, comentários ou metadados do schema. Segredos gerenciados exclusivamente via HashiCorp Vault / AWS Secrets Manager (NFR-SEC04)."
  source_refs:
    - "<!-- source: security-policies#L300-L330 -->"
```

## 4. Data Lifecycle por Entidade
```yaml
data_lifecycle:
  - entity: "Ativo"
    creation:
      event: "Cadastro de novo bem patrimonial via API POST /ativos ou importação em lote"
      owner: "AtivoService.createAtivo / AtivoImportService"
    active_duration: "Vida útil do ativo (definida por tipo_ativo.vida_util_padrao_meses ou ativo.vida_util_meses) — tipicamente 24-120 meses"
    archival:
      condition: "status IN ('DESCARTADO','VENDIDO') E data_conclusao_manutencao_mais_recente > 12 meses"
      destination: "Partição histórica da tabela ativo (mesma tabela, índice por data_aquisicao) — não move fisicamente; RLS mantém acesso"
    retention:
      duration: "7 anos após descarte/venda (SOX Seção 404/802 — trilha de auditoria imutável exige rastreabilidade do ativo)"
      rationale: "SOX exige retenção de registros financeiros/patrimoniais por 7 anos; LGPD permite retenção por obrigação legal (Art. 16)"
    deletion:
      condition: "Nunca — hard delete proibido. Anonimização de campos sensíveis (valor_aquisicao, localização) após 7 anos via job LGPD, mantendo PK e FKs para integridade referencial da auditoria."
      method: "anonymize"
    legal_retention_required: true
    source_refs:
      - "<!-- source: db-schema-spec#L15-L80 -->"
      - "<!-- source: security-policies#L350-L370 -->"
      - "<!-- source: security-policies#L420-L440 -->"

  - entity: "Manutencao"
    creation:
      event: "Abertura de ordem de serviço (solicitação, preventiva agendada, preditiva) via API POST /manutencoes"
      owner: "ManutencaoService.createManutencao / Scheduler.preventiva"
    active_duration: "Da abertura (SOLICITADA) até conclusão (CONCLUIDA) ou cancelamento (CANCELADA) — tipicamente dias a semanas"
    archival:
      condition: "status IN ('CONCLUIDA','CANCELADA') E data_conclusao > 24 meses"
      destination: "Partição histórica por data_abertura (índice INDEX-013) — read-replica para consultas analíticas (custoTotalPorAtivo)"
    retention:
      duration: "7 anos após conclusão/cancelamento (SOX — custos, aprovações, execução)"
      rationale: "Registros financeiros de manutenção (custos, aprovações) sujeitos a SOX; LGPD Art. 16 permite retenção por obrigação legal"
    deletion:
      condition: "Nunca — hard delete proibido. Anonimização de custo_pecas, custo_mao_obra, observacoes após 7 anos."
      method: "anonymize"
    legal_retention_required: true
    source_refs:
      - "<!-- source: db-schema-spec#L180-L250 -->"
      - "<!-- source: security-policies#L350-L370 -->"
      - "<!-- source: db-schema-spec#L1200-L1220 -->"

  - entity: "Alerta"
    creation:
      event: "Geração automática (checkResourceUsageAlerts job) ou manual via API POST /alertas"
      owner: "AlertNotificationService.checkResourceUsageAlerts / AlertaService.createAlerta"
    active_duration: "Da criação até resolução (RESOLVIDO) ou ignorado (IGNORADO) — tipicamente horas a dias"
    archival:
      condition: "status IN ('RESOLVIDO','IGNORADO') E data_resolucao > 90 dias"
      destination: "Partição histórica por data_criacao — mantido na mesma tabela com índice INDEX-015 para dashboard"
    retention:
      duration: "2 anos após resolução/ignorado (requisito operacional + LGPD Art. 16 — logs de segurança)"
      rationale: "Alertas de segurança (USO_DISCO_CRITICO, SAUDE_DEGRADADA) são logs de segurança; retenção 2 anos alinhada a SIEM"
    deletion:
      condition: "Hard delete permitido após 2 anos resolvido/ignorado via job de purga (não afeta auditoria)."
      method: "hard"
    legal_retention_required: false
    source_refs:
      - "<!-- source: db-schema-spec#L280-L350 -->"
      - "<!-- source: security-policies#L350-L370 -->"
      - "<!-- source: db-schema-spec#L1150-L1160 -->"

  - entity: "Usuario"
    creation:
      event: "Cadastro de usuário via API POST /usuarios (admin) ou convite via IdP (futuro)"
      owner: "UsuarioService.createUsuario / IdP Sync Job"
    active_duration: "Enquanto ativo = true — desativação por desligamento ou revogação de acesso"
    archival:
      condition: "ativo = false E ultimo_login > 12 meses"
      destination: "Mantido na tabela com ativo=false — RLS impede acesso operacional"
    retention:
      duration: "7 anos após desativação (SOX — trilha de auditoria referencia usuario_id; LGPD direito ao esquecimento via endpoint admin anonimiza)"
      rationale: "Auditoria WORM referencia usuario_id (FK SET NULL) — anonimização LGPD substitui PII por hash irreversível, mantendo FK para integridade"
    deletion:
      condition: "Apenas via endpoint admin 'direito ao esquecimento' (LGPD Art. 18) — anonimiza PII (username, email, nome_completo, password_hash), mantém id para FK auditoria."
      method: "anonymize"
    legal_retention_required: true
    source_refs:
      - "<!-- source: db-schema-spec#L380-L440 -->"
      - "<!-- source: security-policies#L230-L250 -->"
      - "<!-- source: security-policies#L420-L440 -->"

  - entity: "Funcionario"
    creation:
      event: "Cadastro de colaborador via API POST /funcionarios (RH/ADMIN)"
      owner: "FuncionarioService.createFuncionario"
    active_duration: "Da admissão (data_admissao) até desligamento (data_desligamento) — vínculo empregatício"
    archival:
      condition: "data_desligamento IS NOT NULL E data_desligamento > 12 meses"
      destination: "Mantido na tabela com ativo=false — RLS por filial"
    retention:
      duration: "7 anos após desligamento (SOX — responsabilidade patrimonial; LGPD — obrigação legal trabalhista/previdenciária)"
      rationale: "Funcionário é custodiante de ativos (ativo.funcionario_responsavel_id) e executor de manutenções — rastreabilidade patrimonial exige retenção"
    deletion:
      condition: "Nunca — hard delete proibido. Anonimização LGPD (CPF, email, telefone, nome) via endpoint admin após 7 anos, mantendo matricula (hash) para FKs."
      method: "anonymize"
    legal_retention_required: true
    source_refs:
      - "<!-- source: db-schema-spec#L480-L560 -->"
      - "<!-- source: security-policies#L420-L440 -->"

  - entity: "Auditoria"
    creation:
      event: "Trigger BEFORE INSERT em auditoria (trg_auditoria_insert_hash_chain) — toda operação BR-02 (criar, atualizar, deletar, aprovar, cancelar, concluir, iniciar, login, logout, export, import)"
      owner: "AuditoriaService.log / Triggers automáticos"
    active_duration: "Imutável — append-only (WORM). Nunca atualizado ou deletado."
    archival:
      condition: "data_hora > 12 meses"
      destination: "Cópia assíncrona para WORM Object Storage (S3 Object Lock Compliance Mode / Azure Immutable Blob) — particionamento mensal (INDEX-033)"
    retention:
      duration: "7 anos imutáveis (SOX Seção 802 — destruição/alteração de registros de auditoria é crime federal; LGPD Art. 16 — obrigação legal)"
      rationale: "SOX exige retenção de audit trails por 7 anos; Object Lock Compliance Mode impede deleção antes do retention period"
    deletion:
      condition: "Nunca — hard delete tecnicamente impossível (WORM Compliance Mode). Cripto-shredding (destruição de chaves de criptografia) após 7 anos para tornar dados irrecuperáveis."
      method: "crypto-shredding"
    legal_retention_required: true
    source_refs:
      - "<!-- source: db-schema-spec#L800-L900 -->"
      - "<!-- source: security-policies#L350-L380 -->"
      - "<!-- source: security-policies#L420-L440 -->"
      - "<!-- source: db-schema-spec#L1300-L1320 -->"

  - entity: "AtivoDetalheHardware"
    creation:
      event: "Coleta de inventário de hardware via agente/script (updateHealthCheck job) — upsert em ativo_detalhe_hardware"
      owner: "HardwareInventoryService.collect / Scheduler.updateHealthCheck"
    active_duration: "Enquanto ativo.status != 'DESCARTADO' — atualizado a cada coleta (coletado_em)"
    archival:
      condition: "ativo_id REFERENCES ativo WHERE status = 'DESCARTADO' E coletado_em > 12 meses"
      destination: "Partição histórica — mantido para histórico de configuração"
    retention:
      duration: "7 anos após descarte do ativo pai (SOX — configuração de hardware afeta valor residual e depreciação)"
      rationale: "Detalhes de hardware (processador, memória, discos) compõem base de cálculo de depreciação e valor residual"
    deletion:
      condition: "Cascade delete do ativo pai (ON DELETE CASCADE) — mas ativo nunca é hard deleted, apenas anonimizado. Detalhe mantido."
      method: "anonymize"
    legal_retention_required: true
    source_refs:
      - "<!-- source: db-schema-spec#L680-L730 -->"
      - "<!-- source: db-schema-spec#L1300-L1320 -->"

  - entity: "AdaptadorRede"
    creation:
      event: "Coleta de inventário de NICs via agente — insert em adaptador_rede (ON DELETE CASCADE do pai)"
      owner: "HardwareInventoryService.collect"
    active_duration: "Enquanto ativo_detalhe_hardware pai existe"
    archival:
      condition: "ativo_detalhe_hardware_id REFERENCES ativo_detalhe_hardware WHERE ativo_id REFERENCES ativo(status='DESCARTADO')"
      destination: "Partição histórica com pai"
    retention:
      duration: "7 anos (mesmo ciclo do ativo pai)"
      rationale: "Endereços MAC/IP compõem inventário de rede para compliance de segurança"
    deletion:
      condition: "Cascade delete do ativo_detalhe_hardware pai"
      method: "cascade"
    legal_retention_required: true
    source_refs:
      - "<!-- source: db-schema-spec#L740-L780 -->"
      - "<!-- source: db-schema-spec#L1300-L1320 -->"

  - entity: "Disco"
    creation:
      event: "Coleta de inventário de discos via agente — insert em disco (ON DELETE CASCADE)"
      owner: "HardwareInventoryService.collect"
    active_duration: "Enquanto ativo_detalhe_hardware pai existe — saude_percentual monitorado para alertas"
    archival:
      condition: "ativo_detalhe_hardware_id REFERENCES ativo_detalhe_hardware WHERE ativo_id REFERENCES ativo(status='DESCARTADO')"
      destination: "Partição histórica com pai"
    retention:
      duration: "7 anos (mesmo ciclo do ativo pai)"
      rationale: "Saúde do disco (saude_percentual) e capacidade compõem base de depreciação e alertas preditivos"
    deletion:
      condition: "Cascade delete do ativo_detalhe_hardware pai"
      method: "cascade"
    legal_retention_required: true
    source_refs:
      - "<!-- source: db-schema-spec#L790-L830 -->"
      - "<!-- source: db-schema-spec#L1300-L1320 -->"

  - entity: "Memoria"
    creation:
      event: "Coleta de inventário de módulos RAM via agente — insert em memoria (ON DELETE CASCADE)"
      owner: "HardwareInventoryService.collect"
    active_duration: "Enquanto ativo_detalhe_hardware pai existe"
    archival:
      condition: "ativo_detalhe_hardware_id REFERENCES ativo_detalhe_hardware WHERE ativo_id REFERENCES ativo(status='DESCARTADO')"
      destination: "Partição histórica com pai"
    retention:
      duration: "7 anos (mesmo ciclo do ativo pai)"
      rationale: "Capacidade e tipo de memória compõem especificação técnica do ativo para depreciação"
    deletion:
      condition: "Cascade delete do ativo_detalhe_hardware pai"
      method: "cascade"
    legal_retention_required: true
    source_refs:
      - "<!-- source: db-schema-spec#L840-L880 -->"
      - "<!-- source: db-schema-spec#L1300-L1320 -->"

  - entity: "TipoAtivo"
    creation:
      event: "Cadastro de categoria de ativo via API POST /tipos-ativo (ADMIN)"
      owner: "TipoAtivoService.createTipoAtivo"
    active_duration: "Indefinido — referência estática, ativo=true/false para desativação lógica"
    archival:
      condition: "ativo = false E NOT EXISTS (SELECT 1 FROM ativo WHERE tipo_ativo_id = tipo_ativo.id)"
      destination: "Mantido na tabela — referência histórica para ativos já cadastrados"
    retention:
      duration: "Permanente — tabela de referência (reference classification)"
      rationale: "Tipos de ativo são catálogo imutável para integridade referencial histórica"
    deletion:
      condition: "Nunca — soft delete apenas (ativo=false). Hard delete proibido por FK restrict em ativo.tipo_ativo_id."
      method: "soft"
    legal_retention_required: false
    source_refs:
      - "<!-- source: db-schema-spec#L85-L130 -->"
      - "<!-- source: db-schema-spec#L1050-L1060 -->"

  - entity: "Filial"
    creation:
      event: "Cadastro de unidade organizacional via API POST /filiais (ADMIN)"
      owner: "FilialService.createFilial"
    active_duration: "Indefinido — ativo=true/false"
    archival:
      condition: "ativo = false E NOT EXISTS (SELECT 1 FROM ativo WHERE filial_id = filial.id) E NOT EXISTS (SELECT 1 FROM funcionario WHERE filial_id = filial.id)"
      destination: "Mantido na tabela — referência histórica"
    retention:
      duration: "Permanente — tabela de referência"
      rationale: "Filiais compõem hierarquia organizacional para relatórios históricos e auditoria"
    deletion:
      condition: "Nunca — soft delete apenas. Hard delete proibido por FK restrict."
      method: "soft"
    legal_retention_required: false
    source_refs:
      - "<!-- source: db-schema-spec#L570-L610 -->"
      - "<!-- source: db-schema-spec#L1050-L1060 -->"

  - entity: "Departamento"
    creation:
      event: "Cadastro de divisão interna via API POST /departamentos (ADMIN/GESTOR)"
      owner: "DepartamentoService.createDepartamento"
    active_duration: "Indefinido — ativo=true/false"
    archival:
      condition: "ativo = false E NOT EXISTS (SELECT 1 FROM ativo WHERE departamento_id = departamento.id) E NOT EXISTS (SELECT 1 FROM funcionario WHERE departamento_id = departamento.id)"
      destination: "Mantido na tabela — referência histórica"
    retention:
      duration: "Permanente — tabela de referência"
      rationale: "Departamentos compõem hierarquia para alocação histórica de ativos e funcionários"
    deletion:
      condition: "Nunca — soft delete apenas. Hard delete proibido por FK restrict/set_null."
      method: "soft"
    legal_retention_required: false
    source_refs:
      - "<!-- source: db-schema-spec#L620-L670 -->"
      - "<!-- source: db-schema-spec#L1050-L1060 -->"

  - entity: "Localizacao"
    creation:
      event: "Cadastro de local físico via API POST /localizacoes (ADMIN/GESTOR)"
      owner: "LocalizacaoService.createLocalizacao"
    active_duration: "Indefinido — ativo=true/false"
    archival:
      condition: "ativo = false E NOT EXISTS (SELECT 1 FROM ativo WHERE localizacao_id = localizacao.id)"
      destination: "Mantido na tabela — referência histórica"
    retention:
      duration: "Permanente — tabela de referência"
      rationale: "Localizações (prédio, andar, sala, rack) necessárias para rastreabilidade física histórica"
    deletion:
      condition: "Nunca — soft delete apenas. Hard delete proibido por FK set_null."
      method: "soft"
    legal_retention_required: false
    source_refs:
      - "<!-- source: db-schema-spec#L680-L730 -->"
      - "<!-- source: db-schema-spec#L1050-L1060 -->"

  - entity: "Fornecedor"
    creation:
      event: "Cadastro de fornecedor via API POST /fornecedores (ADMIN/GESTOR)"
      owner: "FornecedorService.createFornecedor"
    active_duration: "Indefinido — ativo=true/false"
    archival:
      condition: "ativo = false E NOT EXISTS (SELECT 1 FROM ativo WHERE fornecedor_id = fornecedor.id) E NOT EXISTS (SELECT 1 FROM manutencao WHERE fornecedor_id = fornecedor.id)"
      destination: "Mantido na tabela — referência histórica"
    retention:
      duration: "7 anos após inativação sem referências (SOX — contratos e custos de manutenção/ativos)"
      rationale: "Fornecedores vinculados a aquisições (ativo.fornecedor_id) e manutenções (manutencao.fornecedor_id) — retenção financeira"
    deletion:
      condition: "Apenas se sem referências — anonimização de CNPJ, razao_social, contato após 7 anos."
      method: "anonymize"
    legal_retention_required: true
    source_refs:
      - "<!-- source: db-schema-spec#L740-L790 -->"
      - "<!-- source: security-policies#L350-L370 -->"

  - entity: "Role"
    creation:
      event: "Cadastro de papel de acesso via API POST /roles (ADMIN)"
      owner: "RoleService.createRole"
    active_duration: "Indefinido — ativo=true/false"
    archival:
      condition: "ativo = false E NOT EXISTS (SELECT 1 FROM usuario_role WHERE role_id = role.id) E NOT EXISTS (SELECT 1 FROM role_permissao WHERE role_id = role.id)"
      destination: "Mantido na tabela — referência histórica"
    retention:
      duration: "Permanente — catálogo de roles para auditoria de concessões passadas"
      rationale: "Auditoria referencia role.nome em concessões/revogações (usuario_role, role_permissao)"
    deletion:
      condition: "Nunca — soft delete apenas."
      method: "soft"
    legal_retention_required: false
    source_refs:
      - "<!-- source: db-schema-spec#L450-L480 -->"
      - "<!-- source: db-schema-spec#L1050-L1060 -->"

  - entity: "Permissao"
    creation:
      event: "Cadastro de permissão atômica via API POST /permissoes (ADMIN)"
      owner: "PermissaoService.createPermissao"
    active_duration: "Indefinido — ativo=true/false"
    archival:
      condition: "ativo = false E NOT EXISTS (SELECT 1 FROM role_permissao WHERE permissao_id = permissao.id)"
      destination: "Mantido na tabela — referência histórica"
    retention:
      duration: "Permanente — catálogo de permissões para auditoria de concessões passadas"
      rationale: "Auditoria referencia permissao.codigo em concessões/revogações"
    deletion:
      condition: "Nunca — soft delete apenas."
      method: "soft"
    legal_retention_required: false
    source_refs:
      - "<!-- source: db-schema-spec#L490-L530 -->"
      - "<!-- source: db-schema-spec#L1050-L1060 -->"

  - entity: "UsuarioRole"
    creation:
      event: "Concessão de role a usuário via API POST /usuarios/{id}/roles (ADMIN) — trigger trg_usuario_role_auditoria registra em auditoria"
      owner: "UsuarioRoleService.grantRole"
    active_duration: "Até revogação (DELETE) ou expiração (expira_em)"
    archival:
      condition: "expira_em IS NOT NULL AND expira_em < CURRENT_TIMESTAMP"
      destination: "Mantido na tabela — histórico de concessões para auditoria"
    retention:
      duration: "7 anos após revogação/expiração (SOX — segregação de duties, acesso a dados financeiros)"
      rationale: "Histórico de quem teve qual role quando é essencial para SOX 404"
    deletion:
      condition: "Nunca — hard delete proibido. Trigger de auditoria registra REVOKE."
      method: "soft"
    legal_retention_required: true
    source_refs:
      - "<!-- source: db-schema-spec#L900-L920 -->"
      - "<!-- source: db-schema-spec#L1350-L1360 -->"
      - "<!-- source: security-policies#L350-L370 -->"

  - entity: "RolePermissao"
    creation:
      event: "Concessão de permissão a role via API POST /roles/{id}/permissoes (ADMIN) — trigger trg_role_permissao_auditoria registra em auditoria"
      owner: "RolePermissaoService.grantPermissao"
    active_duration: "Até revogação (DELETE)"
    archival:
      condition: "N/A — mantido ativo enquanto role existe"
      destination: "Mantido na tabela — histórico de composição de roles"
    retention:
      duration: "7 anos após revogação (SOX — composição de perfis de acesso a dados financeiros)"
      rationale: "Mudanças em role_permissao alteram escopo de acesso — trilha obrigatória SOX"
    deletion:
      condition: "Nunca — hard delete proibido. Trigger de auditoria registra REVOKE."
      method: "soft"
    legal_retention_required: true
    source_refs:
      - "<!-- source: db-schema-spec#L930-L950 -->"
      - "<!-- source: db-schema-spec#L1360-L1370 -->"
      - "<!-- source: security-policies#L350-L370 -->"
```

## 5. Seeds (Dev/Test)
```yaml
seed:
  environments: ["development", "test"]
  purpose: "Dataset determinístico para testes de integração, validação de RLS, performance de queries (ManutencaoSpecification.build, AtivoMapper.toDTO) e desenvolvimento de frontend"
  rules:
    production_allowed: false
    contains_real_data: false
    secrets_allowed: false
    pii_handling: "generated-fake"  # CPF/CNPJ válidos algoritmicamente mas fictícios; emails @example.com; nomes aleatórios
  datasets:
    - name: "core_reference_data"
      deterministic: true
      description: "Dados de referência mínimos para subir a aplicação: filiais, departamentos, localizações, tipos de ativo, roles, permissões"
      tables:
        - "filial"
        - "departamento"
        - "localizacao"
        - "tipo_ativo"
        - "role"
        - "permissao"
        - "fornecedor"
      source_refs:
        - "<!-- source: db-schema-spec#L1450-L1460 -->"
        - "<!-- source: diagnostic#L15-L20 -->"

    - name: "rbac_baseline"
      deterministic: true
      description: "Matriz RBAC baseline (ADMIN, AUDITOR, GESTOR, OPERADOR) com permissões mapeadas conforme security-policies §6"
      tables:
        - "role_permissao"
        - "usuario_role"
      source_refs:
        - "<!-- source: security-policies#L180-L210 -->"

    - name: "patrimonio_sample"
      deterministic: true
      description: "Amostra de 100 ativos (20 por tipo: HARDWARE, SOFTWARE, MOVEL, EQUIPAMENTO, VEICULO) distribuídos em 3 filiais, 5 departamentos, 10 localizações, 15 funcionários, 5 usuários com roles variadas"
      tables:
        - "funcionario"
        - "usuario"
        - "ativo"
        - "ativo_detalhe_hardware"
        - "adaptador_rede"
        - "disco"
        - "memoria"
      source_refs:
        - "<!-- source: diagnostic#L15-L20 -->"
        - "<!-- source: db-schema-spec#L1450-L1460 -->"

    - name: "manutencao_sample"
      deterministic: true
      description: "50 ordens de manutenção (preventiva/corretiva/preditiva/melhoria) em diversos status, com custos, fornecedores, técnicos, aprovadores — para testar ManutencaoSpecification.build (complexidade 14) e vw_custo_manutencao_por_ativo"
      tables:
        - "manutencao"
      source_refs:
        - "<!-- source: diagnostic#L15-L20 -->"
        - "<!-- source: db-schema-spec#L1150-L1170 -->"

    - name: "alerta_sample"
      deterministic: true
      description: "30 alertas de tipos variados (USO_DISCO_CRITICO, MANUTENCAO_VENCIDA, SAUDE_DEGRADADA, etc.) com severidades INFO/WARNING/CRITICAL, status NAO_LIDO/LIDO/RESOLVIDO — para testar listarAlertas e vw_alerta_ativo"
      tables:
        - "alerta"
      source_refs:
        - "<!-- source: db-schema-spec#L1150-L1160 -->"
        - "<!-- source: security-policies#L45-L55 -->"

    - name: "auditoria_sample"
      deterministic: true
      description: "200 entradas de auditoria simulando operações BR-02 (CREATE/UPDATE/DELETE/APPROVE/REJECT/LOGIN/EXPORT) com hash_encadeado válido, correlation_id, payloads JSON — para testar integridade WORM e rastreamento"
      tables:
        - "auditoria"
      source_refs:
        - "<!-- source: db-schema-spec#L1300-L1320 -->"
        - "<!-- source: security-policies#L370-L380 -->"

    - name: "lgpd_edge_cases"
      deterministic: true
      description: "Casos de borda LGPD: funcionário desligado (data_desligamento preenchida, ativo=false), usuário anonimizado (endpoint direito ao esquecimento), ativo descartado, alerta ignorado > 90 dias — para validar triggers de anonimização e jobs de purga"
      tables:
        - "funcionario"
        - "usuario"
        - "ativo"
        - "alerta"
      source_refs:
        - "<!-- source: security-policies#L420-L440 -->"
        - "<!-- source: db-schema-spec#L1400-L1420 -->"
```