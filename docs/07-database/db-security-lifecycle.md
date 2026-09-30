# Database Security & Data Lifecycle — aegis_patrimonio

> **Versão:** 1.0 · **Owner:** backend-team / security-team · **Status:** Draft

## 1. Roles & Permissions
```yaml
roles:
  - name: "ADMIN"
    purpose: "Acesso administrativo total ao sistema — gestão de filiais, tipos de ativo, usuários, papéis, permissões e dados mestres de todas as filiais."
    source: "security-policies#6-authentication-authorization + db-schema-spec#ent-papel + db-schema-spec#ent-usuario"
    permissions:
      tables:
        - table: "filial"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "departamento"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "tipo_ativo"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "localizacao"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "fornecedor"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "usuario"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "papel"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "permissao"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "papel_permissao"
          operations: ["SELECT", "INSERT", "DELETE"]
        - table: "usuario_papel"
          operations: ["SELECT", "INSERT", "DELETE"]
        - table: "ativo"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "manutencao"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "alerta"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "funcionario"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "ativo_detalhe_hardware"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "memoria"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "disco"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "adaptador_rede"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "health_check_disco"
          operations: ["SELECT"]
        - table: "auditoria"
          operations: ["SELECT"]

  - name: "USER"
    purpose: "Usuário padrão — acesso operacional restrito à própria filial (filial_id do JWT). Pode criar/atualizar manutenções e alertas, visualizar ativos/funcionários da filial, consultar dados de referência."
    source: "security-policies#6-authentication-authorization + db-schema-spec#ent-usuario"
    permissions:
      tables:
        - table: "filial"
          operations: ["SELECT"]
        - table: "departamento"
          operations: ["SELECT"]
        - table: "tipo_ativo"
          operations: ["SELECT"]
        - table: "localizacao"
          operations: ["SELECT"]
        - table: "fornecedor"
          operations: ["SELECT"]
        - table: "ativo"
          operations: ["SELECT", "INSERT", "UPDATE"]
        - table: "manutencao"
          operations: ["SELECT", "INSERT", "UPDATE"]
        - table: "alerta"
          operations: ["SELECT", "INSERT", "UPDATE"]
        - table: "funcionario"
          operations: ["SELECT"]
        - table: "ativo_detalhe_hardware"
          operations: ["SELECT", "INSERT", "UPDATE"]
        - table: "memoria"
          operations: ["SELECT", "INSERT", "UPDATE"]
        - table: "disco"
          operations: ["SELECT", "INSERT", "UPDATE"]
        - table: "adaptador_rede"
          operations: ["SELECT", "INSERT", "UPDATE"]
        - table: "health_check_disco"
          operations: ["SELECT"]
        - table: "auditoria"
          operations: ["SELECT"]

  - name: "GESTOR_PATRIMONIO"
    purpose: "Papel granular (RBAC) para gestão avançada de ativos — herda permissões de USER + operações de escrita em ativos e manutenções da filial."
    source: "db-schema-spec#ent-papel + db-schema-spec#ent-permissao + db-schema-spec#ent-papel_permissao"
    permissions:
      tables:
        - table: "ativo"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "manutencao"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "alerta"
          operations: ["SELECT", "INSERT", "UPDATE", "DELETE"]
        - table: "fornecedor"
          operations: ["SELECT", "INSERT", "UPDATE"]
        - table: "localizacao"
          operations: ["SELECT", "INSERT", "UPDATE"]
        - table: "departamento"
          operations: ["SELECT", "INSERT", "UPDATE"]

  - name: "TECNICO_MANUTENCAO"
    purpose: "Papel granular para técnicos de manutenção — foco em execução e atualização de ordens de serviço."
    source: "db-schema-spec#ent-papel + db-schema-spec#ent-permissao + db-schema-spec#ent-papel_permissao"
    permissions:
      tables:
        - table: "manutencao"
          operations: ["SELECT", "UPDATE"]
        - table: "ativo"
          operations: ["SELECT"]
        - table: "alerta"
          operations: ["SELECT", "UPDATE"]
        - table: "fornecedor"
          operations: ["SELECT"]

  - name: "AUDITOR"
    purpose: "Papel granular para auditoria — leitura de trilhas de auditoria, alertas e relatórios de conformidade."
    source: "db-schema-spec#ent-papel + db-schema-spec#ent-permissao + db-schema-spec#ent-papel_permissao"
    permissions:
      tables:
        - table: "auditoria"
          operations: ["SELECT"]
        - table: "alerta"
          operations: ["SELECT"]
        - table: "ativo"
          operations: ["SELECT"]
        - table: "manutencao"
          operations: ["SELECT"]
        - table: "funcionario"
          operations: ["SELECT"]
        - table: "vw_depreciacao_ativo"
          operations: ["SELECT"]
        - table: "vw_disco_saude_atual"
          operations: ["SELECT"]
```

## 2. Row-Level Security
```yaml
row_level_security:
  - table: "ativo"
    enabled: true
    source: "security-policies#2-context-isolation-boundary + db-schema-spec#ent-ativo"
    policies:
      - name: "rls_ativo_filial_isolation"
        operation: "ALL"
        using: "filial_id = current_setting('app.current_filial_id')::UUID"
        comment: "Isolamento multi-filial: usuário só acessa ativos da própria filial (claim filial_id no JWT). ADMIN bypass via policy separada ou role bypass."

  - table: "manutencao"
    enabled: true
    source: "security-policies#2-context-isolation-boundary + db-schema-spec#ent-manutencao"
    policies:
      - name: "rls_manutencao_filial_isolation"
        operation: "ALL"
        using: "ativo_id IN (SELECT id FROM ativo WHERE filial_id = current_setting('app.current_filial_id')::UUID)"
        comment: "Acesso a manutenções apenas dos ativos da filial do usuário."

  - table: "funcionario"
    enabled: true
    source: "security-policies#2-context-isolation-boundary + db-schema-spec#ent-funcionario"
    policies:
      - name: "rls_funcionario_filial_isolation"
        operation: "ALL"
        using: "filial_id = current_setting('app.current_filial_id')::UUID"
        comment: "Funcionários visíveis apenas da própria filial."

  - table: "departamento"
    enabled: true
    source: "security-policies#2-context-isolation-boundary + db-schema-spec#ent-departamento"
    policies:
      - name: "rls_departamento_filial_isolation"
        operation: "ALL"
        using: "filial_id = current_setting('app.current_filial_id')::UUID"

  - table: "localizacao"
    enabled: true
    source: "security-policies#2-context-isolation-boundary + db-schema-spec#ent-localizacao"
    policies:
      - name: "rls_localizacao_filial_isolation"
        operation: "ALL"
        using: "filial_id = current_setting('app.current_filial_id')::UUID"

  - table: "fornecedor"
    enabled: true
    source: "security-policies#2-context-isolation-boundary + db-schema-spec#ent-fornecedor"
    policies:
      - name: "rls_fornecedor_filial_isolation"
        operation: "ALL"
        using: "true"  -- Fornecedores são globais (não têm filial_id), mas acesso controlado via RBAC na camada de aplicação
        comment: "Fornecedor não possui filial_id; isolamento feito na camada de serviço (RBAC). RLS mantida para consistência."

  - table: "tipo_ativo"
    enabled: true
    source: "security-policies#2-context-isolation-boundary + db-schema-spec#ent-tipo-ativo"
    policies:
      - name: "rls_tipo_ativo_global"
        operation: "ALL"
        using: "true"  -- Tipos de ativo são globais (catálogo compartilhado)
        comment: "Catálogo global; escrita restrita a ADMIN via RBAC."

  - table: "filial"
    enabled: true
    source: "security-policies#2-context-isolation-boundary + db-schema-spec#ent-filial"
    policies:
      - name: "rls_filial_admin_only"
        operation: "ALL"
        using: "current_setting('app.current_role', true) = 'ADMIN'"
        comment: "Apenas ADMIN acessa tabela de filiais (dados confidenciais: CNPJ, endereço)."

  - table: "usuario"
    enabled: true
    source: "security-policies#2-context-isolation-boundary + db-schema-spec#ent-usuario"
    policies:
      - name: "rls_usuario_self_or_admin"
        operation: "ALL"
        using: "id = current_setting('app.current_user_id')::UUID OR current_setting('app.current_role', true) = 'ADMIN'"
        comment: "Usuário vê apenas próprio registro; ADMIN vê todos."

  - table: "alerta"
    enabled: true
    source: "security-policies#2-context-isolation-boundary + db-schema-spec#ent-alerta"
    policies:
      - name: "rls_alerta_filial_via_ativo"
        operation: "ALL"
        using: "ativo_id IS NULL OR ativo_id IN (SELECT id FROM ativo WHERE filial_id = current_setting('app.current_filial_id')::UUID) OR usuario_id = current_setting('app.current_user_id')::UUID"
        comment: "Alertas globais (ativo_id NULL) + alertas de ativos da filial + alertas destinados ao próprio usuário."

  - table: "ativo_detalhe_hardware"
    enabled: true
    source: "security-policies#2-context-isolation-boundary + db-schema-spec#ent-ativo-detalhe-hardware"
    policies:
      - name: "rls_ativo_detalhe_hardware_filial"
        operation: "ALL"
        using: "ativo_id IN (SELECT id FROM ativo WHERE filial_id = current_setting('app.current_filial_id')::UUID)"

  - table: "memoria"
    enabled: true
    source: "security-policies#2-context-isolation-boundary + db-schema-spec#ent-memoria"
    policies:
      - name: "rls_memoria_filial_via_hardware"
        operation: "ALL"
        using: "ativo_detalhe_hardware_id IN (SELECT id FROM ativo_detalhe_hardware WHERE ativo_id IN (SELECT id FROM ativo WHERE filial_id = current_setting('app.current_filial_id')::UUID))"

  - table: "disco"
    enabled: true
    source: "security-policies#2-context-isolation-boundary + db-schema-spec#ent-disco"
    policies:
      - name: "rls_disco_filial_via_hardware"
        operation: "ALL"
        using: "ativo_detalhe_hardware_id IN (SELECT id FROM ativo_detalhe_hardware WHERE ativo_id IN (SELECT id FROM ativo WHERE filial_id = current_setting('app.current_filial_id')::UUID))"

  - table: "adaptador_rede"
    enabled: true
    source: "security-policies#2-context-isolation-boundary + db-schema-spec#ent-adaptador-rede"
    policies:
      - name: "rls_adaptador_rede_filial_via_hardware"
        operation: "ALL"
        using: "ativo_detalhe_hardware_id IN (SELECT id FROM ativo_detalhe_hardware WHERE ativo_id IN (SELECT id FROM ativo WHERE filial_id = current_setting('app.current_filial_id')::UUID))"

  - table: "health_check_disco"
    enabled: true
    source: "security-policies#2-context-isolation-boundary + db-schema-spec#ent-health-check-disco"
    policies:
      - name: "rls_health_check_filial_via_disco"
        operation: "SELECT"
        using: "disco_id IN (SELECT id FROM disco WHERE ativo_detalhe_hardware_id IN (SELECT id FROM ativo_detalhe_hardware WHERE ativo_id IN (SELECT id FROM ativo WHERE filial_id = current_setting('app.current_filial_id')::UUID)))"
        comment: "Apenas leitura (tabela de auditoria/monitoramento)."

  - table: "auditoria"
    enabled: true
    source: "security-policies#2-context-isolation-boundary + db-schema-spec#ent-auditoria + security-policies#10-logging-auditability"
    policies:
      - name: "rls_auditoria_admin_auditor"
        operation: "SELECT"
        using: "current_setting('app.current_role', true) IN ('ADMIN', 'AUDITOR')"
        comment: "Acesso restrito a ADMIN e papel AUDITOR (RBAC). Dados sensíveis: quem fez o quê, quando, antes/depois."
```

## 3. Sensitive Data
```yaml
sensitive_data:
  - field: "filial.cnpj"
    classification: "confidential"
    protection: "encryption-at-rest (TDE managed DB) + restricted-access (RLS ADMIN only) + masking in logs"
    source: "db-schema-spec#ent-filial + security-policies#5-data-protection-rules"

  - field: "filial.razao_social"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS ADMIN only)"

  - field: "filial.nome_fantasia"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS ADMIN only)"

  - field: "filial.endereco"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS ADMIN only)"

  - field: "filial.telefone"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS ADMIN only)"

  - field: "filial.email"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS ADMIN only)"

  - field: "fornecedor.cnpj"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RBAC) + masking in logs"

  - field: "fornecedor.razao_social"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RBAC)"

  - field: "fornecedor.nome_fantasia"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RBAC)"

  - field: "fornecedor.endereco"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RBAC)"

  - field: "fornecedor.telefone"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RBAC)"

  - field: "fornecedor.email"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RBAC)"

  - field: "fornecedor.contato_comercial"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RBAC)"

  - field: "ativo.tag_patrimonio"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS filial)"

  - field: "ativo.nome"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS filial)"

  - field: "ativo.descricao"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS filial)"

  - field: "ativo.valor_aquisicao"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS filial)"

  - field: "ativo.data_aquisicao"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS filial)"

  - field: "ativo.numero_serie"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS filial)"

  - field: "ativo.fornecedor_id"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS filial)"

  - field: "ativo.filial_id"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS filial)"

  - field: "funcionario.matricula"
    classification: "restricted"
    protection: "AES-256-GCM column-level encryption (AttributeConverter) + key in Vault + restricted-access (RLS filial) + masking in logs (****)"
    source: "db-schema-spec#ent-funcionario + security-policies#5-data-protection-rules"

  - field: "funcionario.cpf"
    classification: "restricted"
    protection: "AES-256-GCM column-level encryption (AttributeConverter) + key in Vault (rotation 90d) + restricted-access (RLS filial) + masking in logs (***.***.***-**)"
    source: "db-schema-spec#ent-funcionario + security-policies#5-data-protection-rules"

  - field: "funcionario.nome_completo"
    classification: "restricted"
    protection: "AES-256-GCM column-level encryption (AttributeConverter) + key in Vault + restricted-access (RLS filial)"

  - field: "funcionario.email_corporativo"
    classification: "restricted"
    protection: "AES-256-GCM column-level encryption (AttributeConverter) + key in Vault + restricted-access (RLS filial) + masking in logs (****@example.com)"

  - field: "funcionario.telefone"
    classification: "restricted"
    protection: "AES-256-GCM column-level encryption (AttributeConverter) + key in Vault + restricted-access (RLS filial) + masking in logs"

  - field: "funcionario.cargo"
    classification: "restricted"
    protection: "AES-256-GCM column-level encryption (AttributeConverter) + key in Vault + restricted-access (RLS filial)"

  - field: "funcionario.data_admissao"
    classification: "restricted"
    protection: "AES-256-GCM column-level encryption (AttributeConverter) + key in Vault + restricted-access (RLS filial)"

  - field: "funcionario.data_demissao"
    classification: "restricted"
    protection: "AES-256-GCM column-level encryption (AttributeConverter) + key in Vault + restricted-access (RLS filial)"

  - field: "funcionario.departamento_id"
    classification: "internal"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS filial)"

  - field: "funcionario.filial_id"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS filial)"

  - field: "usuario.username"
    classification: "restricted"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS self/ADMIN) + never logged"

  - field: "usuario.password_hash"
    classification: "restricted"
    protection: "bcrypt cost=12 (irreversível) + never logged + never returned in queries"
    source: "db-schema-spec#ent-usuario + security-policies#6-authentication-authorization"

  - field: "usuario.role"
    classification: "restricted"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS self/ADMIN)"

  - field: "usuario.ultimo_login"
    classification: "restricted"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS self/ADMIN)"

  - field: "usuario.funcionario_id"
    classification: "restricted"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS self/ADMIN)"

  - field: "manutencao.custo_estimado"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS filial)"

  - field: "manutencao.custo_real"
    classification: "confidential"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS filial)"

  - field: "auditoria.valores_anteriores"
    classification: "restricted"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS ADMIN/AUDITOR only) + immutable (insert-only) + WORM backup"
    source: "db-schema-spec#ent-auditoria + security-policies#5-data-protection-rules"

  - field: "auditoria.valores_novos"
    classification: "restricted"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS ADMIN/AUDITOR only) + immutable (insert-only) + WORM backup"

  - field: "auditoria.ip_origem"
    classification: "restricted"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS ADMIN/AUDITOR only) + masking in logs"

  - field: "auditoria.usuario_id"
    classification: "restricted"
    protection: "encryption-at-rest (TDE) + restricted-access (RLS ADMIN/AUDITOR only)"

secrets:
  prohibited_in_schema: true
  comment: "Nenhuma credencial, chave de criptografia, segredo de JWT, senha de banco ou token de API deve aparecer em DDL, seeds, comentários ou migrações. Todos gerenciados via HashiCorp Vault / AWS Secrets Manager / Azure Key Vault (security-policies#7-secrets-management)."
  source: "security-policies#7-secrets-management"
```

## 4. Data Lifecycle por Entidade
```yaml
data_lifecycle:
  - entity: "Filial"
    creation: { event: "Cadastro inicial da unidade de negócio (matriz/filial)", owner: "ADMIN via API /api/filiais" }
    active_duration: "Indeterminada (enquanto a unidade existir)"
    archival: { condition: "filial.ativa = false", destination: "Mesma tabela (soft delete via coluna ativa=false)" }
    retention: { duration: "Permanente (dados cadastrais de pessoa jurídica)", rationale: "Obrigação legal/contábil — CNPJ, razão social, endereço devem ser mantidos indefinidamente para compliance fiscal e societário." }
    deletion: { condition: "Nunca (exclusão física proibida)", method: "soft" }
    legal_retention_required: true

  - entity: "Departamento"
    creation: { event: "Criação de unidade organizacional interna", owner: "ADMIN/GESTOR_PATRIMONIO via API /api/departamentos" }
    active_duration: "Indeterminada (enquanto o departamento existir)"
    archival: { condition: "departamento.ativo = false", destination: "Mesma tabela (soft delete)" }
    retention: { duration: "Permanente (estrutura organizacional histórica)", rationale: "Rastreabilidade de centros de custo e alocação de ativos ao longo do tempo." }
    deletion: { condition: "Nunca (exclusão física proibida)", method: "soft" }
    legal_retention_required: false

  - entity: "TipoAtivo"
    creation: { event: "Cadastro de categoria de ativo com regras de depreciação", owner: "ADMIN via API /api/tipos-ativos" }
    active_duration: "Indeterminada (catálogo de referência)"
    archival: { condition: "tipo_ativo.ativo = false", destination: "Mesma tabela (soft delete)" }
    retention: { duration: "Permanente (histórico de depreciação depende do tipo)", rationale: "Cálculo de depreciação de ativos existentes requer taxa e vida útil do tipo no momento da aquisição." }
    deletion: { condition: "Nunca se houver ativos referenciando (FK RESTRICT)", method: "soft" }
    legal_retention_required: false

  - entity: "Localizacao"
    creation: { event: "Cadastro de local físico/lógico (sala, andar, estoque)", owner: "ADMIN/GESTOR_PATRIMONIO via API /api/localizacoes" }
    active_duration: "Indeterminada"
    archival: { condition: "localizacao.ativo = false", destination: "Mesma tabela (soft delete)" }
    retention: { duration: "Permanente (histórico de alocação de ativos)", rationale: "Inventários passados e auditoria de localização exigem referência histórica." }
    deletion: { condition: "Nunca se houver ativos alocados (FK RESTRICT)", method: "soft" }
    legal_retention_required: false

  - entity: "Fornecedor"
    creation: { event: "Cadastro de fornecedor de ativos/serviços", owner: "ADMIN/GESTOR_PATRIMONIO via API /api/fornecedores" }
    active_duration: "Indeterminada (enquanto relação comercial existir)"
    archival: { condition: "fornecedor.ativo = false", destination: "Mesma tabela (soft delete)" }
    retention: { duration: "10 anos após inativação", rationale: "Compliance fiscal/contratual — notas fiscais, contratos, garantias." }
    deletion: { condition: "Após 10 anos inativo e sem manutenções/ativos vinculados", method: "anonymize (CNPJ→hash, razão_social→'FORNECEDOR_ANON_{id}')" }
    legal_retention_required: true

  - entity: "Funcionario"
    creation: { event: "Admissão / cadastro no sistema", owner: "RH / ADMIN via API /api/funcionarios" }
    active_duration: "Vínculo empregatício (data_admissao até data_demissao)"
    archival: { condition: "funcionario.ativo = false AND data_demissao IS NOT NULL", destination: "Mesma tabela (soft delete) + anonimização PII após período legal" }
    retention: { duration: "20 anos após demissão (CLT Art. 41 + LGPD)", rationale: "Obrigações trabalhistas, previdenciárias e LGPD Art. 16." }
    deletion: { condition: "Após 20 anos da demissão", method: "anonymize (CPF→hash irreversível, email/telefone→null, nome→'FUNCIONARIO_ANON_{id}')" }
    legal_retention_required: true

  - entity: "Usuario"
    creation: { event: "Criação de credencial de acesso vinculada a funcionário", owner: "ADMIN via API /api/usuarios" }
    active_duration: "Enquanto funcionário ativo + 90 dias pós-desligamento"
    archival: { condition: "usuario.ativo = false", destination: "Mesma tabela (soft delete)" }
    retention: { duration: "5 anos após último login ou desligamento", rationale: "Auditoria de acesso (SOX) e investigação de incidentes." }
    deletion: { condition: "Após 5 anos inativo", method: "hard (DELETE) — sem PII direta, apenas hash de senha e username" }
    legal_retention_required: true

  - entity: "Ativo"
    creation: { event: "Aquisição/registro de bem patrimonial", owner: "GESTOR_PATRIMONIO / ADMIN via API /api/ativos" }
    active_duration: "Vida útil do ativo (anos) + período de guarda contábil"
    archival: { condition: "ativo.status = 'BAIXADO'", destination: "Mesma tabela (status BAIXADO mantém registro para histórico)" }
    retention: { duration: "7 anos após baixa (SOX / legislação contábil)", rationale: "Demonstrações contábeis, depreciação, alienação, seguros." }
    deletion: { condition: "Após 7 anos da baixa e sem manutenções/alertas pendentes", method: "anonymize (valor_aquisicao→0, tag_patrimonio→'BAIXADO_{id}', fornecedor_id→NULL)" }
    legal_retention_required: true

  - entity: "Manutencao"
    creation: { event: "Abertura de ordem de serviço (solicitação)", owner: "USER/TECNICO_MANUTENCAO via API /api/manutencoes" }
    active_duration: "Do status SOLICITADA até CONCLUIDA ou CANCELADA"
    archival: { condition: "manutencao.status IN ('CONCLUIDA', 'CANCELADA') AND data_conclusao < (CURRENT_DATE - INTERVAL '1 year')", destination: "Mesma tabela (registros históricos mantidos para SLA/garantia)" }
    retention: { duration: "10 anos após conclusão", rationale: "Garantias de serviço, contratos de manutenção, compliance SOX." }
    deletion: { condition: "Após 10 anos", method: "anonymize (custo_real/estimado→0, observacoes→NULL, fornecedor_id→NULL)" }
    legal_retention_required: true

  - entity: "Alerta"
    creation: { event: "Geração automática pelo sistema (jobs, health checks, regras de negócio)", owner: "Sistema (AlertNotificationService, jobs agendados)" }
    active_duration: "Até ser lido/resolvido + 90 dias"
    archival: { condition: "alerta.lido = true AND data_leitura < (CURRENT_DATE - INTERVAL '90 days')", destination: "Mesma tabela (mantido para métricas)" }
    retention: { duration: "2 anos após leitura", rationale: "Análise de tendências, MTTR, compliance de resposta a incidentes." }
    deletion: { condition: "Após 2 anos", method: "hard (DELETE) — dados operacionais, sem PII direta" }
    legal_retention_required: false

  - entity: "AtivoDetalheHardware"
    creation: { event: "Inventário de hardware (coleta via agente/ferramenta)", owner: "Sistema (agente de inventário) / TECNICO_MANUTENCAO" }
    active_duration: "Enquanto ativo associado estiver ATIVO"
    archival: { condition: "ativo_id referenciado tem status != 'ATIVO'", destination: "Mesma tabela (cascata com ativo)" }
    retention: { duration: "Mesmo ciclo de vida do ativo (7 anos pós-baixa)", rationale: "Rastreabilidade de configuração para depreciação, suporte, segurança." }
    deletion: { condition: "Cascata com ativo (ON DELETE CASCADE)", method: "hard (CASCADE)" }
    legal_retention_required: false

  - entity: "Memoria"
    creation: { event: "Registro de pente de memória no inventário", owner: "Sistema (agente) / TECNICO_MANUTENCAO" }
    active_duration: "Enquanto ativo_detalhe_hardware associado existir"
    archival: { condition: "Cascata com ativo_detalhe_hardware", destination: "Mesma tabela" }
    retention: { duration: "Mesmo ciclo do ativo pai", rationale: "Especificação técnica para suporte/upgrade." }
    deletion: { condition: "Cascata (ON DELETE CASCADE)", method: "hard (CASCADE)" }
    legal_retention_required: false

  - entity: "Disco"
    creation: { event: "Registro de disco/volume no inventário", owner: "Sistema (agente) / TECNICO_MANUTENCAO" }
    active_duration: "Enquanto ativo_detalhe_hardware associado existir"
    archival: { condition: "Cascata com ativo_detalhe_hardware", destination: "Mesma tabela" }
    retention: { duration: "Mesmo ciclo do ativo pai + health checks (13 meses particionados)", rationale: "Histórico de saúde SMART e uso para análise preditiva." }
    deletion: { condition: "Cascata (ON DELETE CASCADE)", method: "hard (CASCADE)" }
    legal_retention_required: false

  - entity: "AdaptadorRede"
    creation: { event: "Registro de interface de rede no inventário", owner: "Sistema (agente) / TECNICO_MANUTENCAO" }
    active_duration: "Enquanto ativo_detalhe_hardware associado existir"
    archival: { condition: "Cascata com ativo_detalhe_hardware", destination: "Mesma tabela" }
    retention: { duration: "Mesmo ciclo do ativo pai", rationale: "Inventário de rede, MAC address para DHCP/NAC." }
    deletion: { condition: "Cascata (ON DELETE CASCADE)", method: "hard (CASCADE)" }
    legal_retention_required: false

  - entity: "HealthCheckDisco"
    creation: { event: "Coleta periódica de métricas de disco (job agendado/agente)", owner: "Sistema (job de monitoramento)" }
    active_duration: "13 meses (particionamento mensal com retenção automática)"
    archival: { condition: "coletado_em < (CURRENT_DATE - INTERVAL '13 months')", destination: "Partição antiga detach/drop automático (pg_partman ou job)" }
    retention: { duration: "13 meses (rolling window)", rationale: "Monitoramento operacional, detecção de degradação, capacidade de trend analysis." }
    deletion: { condition: "Automático via particionamento RANGE (drop partition > 13 meses)", method: "hard (DROP PARTITION)" }
    legal_retention_required: false
    partitioning: { strategy: "RANGE", column: "coletado_em", interval: "1 month", retention: "13 months" }
    source: "db-schema-spec#ent-health-check-disco"

  - entity: "Auditoria"
    creation: { event: "Trigger AFTER INSERT/UPDATE/DELETE em tabelas sensíveis (fn_auditoria_trigger)", owner: "Sistema (trigger automático)" }
    active_duration: "5 anos (particionamento anual com retenção)"
    archival: { condition: "created_at < (CURRENT_DATE - INTERVAL '5 years')", destination: "Partição antiga detach → armazenamento WORM (S3/GCS Object Lock) criptografado KMS" }
    retention: { duration: "5 anos online + 5 anos cold (total 10 anos LGPD Art. 16 / SOX)", rationale: "Trilha imutável de alterações para compliance, investigação forense, LGPD." }
    deletion: { condition: "Após 10 anos totais", method: "hard (DROP PARTITION + destruição certificada WORM)" }
    legal_retention_required: true
    partitioning: { strategy: "RANGE", column: "created_at", interval: "1 year", retention: "5 years online" }
    source: "db-schema-spec#ent-auditoria + security-policies#5-data-protection-rules"

  - entity: "Papel"
    creation: { event: "Criação de role granular (RBAC)", owner: "ADMIN via API /api/papeis" }
    active_duration: "Indeterminada (catálogo de autorização)"
    archival: { condition: "N/A (não há coluna ativo)", destination: "N/A" }
    retention: { duration: "Permanente", rationale: "Histórico de permissões para auditoria de acesso." }
    deletion: { condition: "Nunca se houver usuario_papel ou papel_permissao referenciando (FK RESTRICT/CASCADE)", method: "soft (marcar inativo se coluna adicionada futuramente)" }
    legal_retention_required: false

  - entity: "Permissao"
    creation: { event: "Cadastro de permissão recurso:ação", owner: "ADMIN via API /api/permissoes" }
    active_duration: "Indeterminada (catálogo de autorização)"
    archival: { condition: "N/A", destination: "N/A" }
    retention: { duration: "Permanente", rationale: "Definição de escopo de acesso para auditoria." }
    deletion: { condition: "Nunca se houver papel_permissao referenciando", method: "soft" }
    legal_retention_required: false
```

## 5. Seeds (Dev/Test)
```yaml
seed:
  environments: ["development", "test"]
  purpose: "Dados sintéticos determinísticos para desenvolvimento local, testes de integração, homologação e demonstrações — **nunca** dados reais de produção."
  source: "diagnóstico#RealisticDataSeeder.java + security-policies#5-data-protection-rules (anonimização)"
  rules:
    production_allowed: false
    contains_real_data: false
    secrets_allowed: false
    pii_handling: "CPF/CNPJ gerados algoritmicamente com dígitos verificadores válidos; emails/telefones sintéticos (user{N}@example.com, (11) 9{N}00-0000); nomes de funcionários aleatórios (Faker PT-BR); valores de ativos com ruído gaussiano ±10%."
    jwt_keys: "Chaves distintas por ambiente (Vault path secret/data/aegis/{env}/jwt-key) — security-policies#5-data-protection-rules"
    encryption_keys: "Chaves AES-256-GCM distintas por ambiente (Vault path secret/data/aegis/{env}/encryption-key)"
  datasets:
    - name: "baseline_minimal"
      deterministic: true
      description: "Conjunto mínimo para subir a aplicação: 1 filial (matriz), 2 departamentos, 3 tipos de ativo (NOTEBOOK, DESKTOP, MOVEL), 2 localizações, 1 fornecedor, 3 funcionários (1 ADMIN, 2 USER), 2 usuários, 5 ativos, 2 manutenções, 3 alertas."
      tables_seeded: ["filial", "departamento", "tipo_ativo", "localizacao", "fornecedor", "funcionario", "usuario", "papel", "permissao", "papel_permissao", "usuario_papel", "ativo", "manutencao", "alerta"]
      row_counts:
        filial: 1
        departamento: 2
        tipo_ativo: 3
        localizacao: 2
        fornecedor: 1
        funcionario: 3
        usuario: 2
        papel: 4
        permissao: 12
        papel_permissao: 16
        usuario_papel: 3
        ativo: 5
        manutencao: 2
        alerta: 3

    - name: "realistic_full"
      deterministic: true
      description: "Cenário realista para testes de performance, UI e relatórios — ~500 ativos, 50 funcionários, 10 filiais, histórico de 2 anos."
      source: "diagnóstico#RealisticDataSeeder.java:34 (complexidade ciclomática 15 — considerar refatoração em métodos menores)"
      tables_seeded: ["filial", "departamento", "tipo_ativo", "localizacao", "fornecedor", "funcionario", "usuario", "papel", "permissao", "papel_permissao", "usuario_papel", "ativo", "ativo_detalhe_hardware", "memoria", "disco", "adaptador_rede", "manutencao", "alerta", "health_check_disco"]
      row_counts:
        filial: 10
        departamento: 25
        tipo_ativo: 15
        localizacao: 40
        fornecedor: 20
        funcionario: 50
        usuario: 45
        papel: 6
        permissao: 24
        papel_permissao: 48
        usuario_papel: 60
        ativo: 500
        ativo_detalhe_hardware: 180
        memoria: 360
        disco: 250
        adaptador_rede: 200
        manutencao: 300
        alerta: 150
        health_check_disco: 12000  # ~10 checks/disco/mês × 12 meses

    - name: "compliance_audit"
      deterministic: true
      description: "Dataset focado em validação de trilha de auditoria, LGPD, retenção e anonimização — inclui funcionários demitidos, ativos baixados, manutenções antigas, partições de health_check_disco e auditoria com 5 anos."
      tables_seeded: ["filial", "departamento", "funcionario", "usuario", "ativo", "manutencao", "auditoria", "health_check_disco"]
      special_cases:
        - "funcionario com data_demissao > 20 anos (elegível para anonimização)"
        - "ativo com status BAIXADO > 7 anos (elegível para anonimização)"
        - "auditoria com created_at > 5 anos (partição cold)"
        - "health_check_disco com coletado_em > 13 meses (partição expirada)"
      row_counts:
        filial: 3
        departamento: 5
        funcionario: 20
        usuario: 15
        ativo: 100
        manutencao: 80
        auditoria: 5000
        health_check_disco: 15000
```