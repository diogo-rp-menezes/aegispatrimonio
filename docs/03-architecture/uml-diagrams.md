# UML & C4 Diagrams (Diagram as Code) — Aegis Patrimônio

> **Versão:** 1.0 · **Owner:** Arquitetura/Eng Lead · **Status:** Draft
> Este artefato segue a abordagem **Diagram as Code (DaC)**: os diagramas são texto versionado (Mermaid), nunca imagens estáticas coladas aqui. Toda alteração de arquitetura relevante deve atualizar este arquivo no mesmo PR que altera o código.

<!-- source: system-architecture.md#L1-L50 -->

## 1. Diagrama de Containers (C4 Model — Nível 2)

```mermaid
C4Container
    title Diagrama de Containers - Aegis Patrimônio

    Person(usuario, "Usuário Final", "Gestor, Operador, Auditor ou Admin acessando via navegador")
    Person(admin, "Administrador de Sistema", "Operações de infra, deploy, backup, rotação de segredos")

    System_Boundary(sistema, "Aegis Patrimônio - Monolito Modular") {
        Container(frontend, "Frontend (Static Assets)", "Vanilla JS ES2022 + @popperjs/core", "SPA servida como arquivos estáticos: login, dashboard, cadastro de ativos, solicitação de manutenção, visualização de alertas")
        Container(api, "Web/API Module", "Spring Boot 3.x (Spring MVC, Security, Doc)", "Endpoints REST: CRUD Ativos, Manutenções, Health Checks, Alertas, Autenticação JWT, Auditoria; serve frontend estático")
        Container(domain, "Domain Services", "Java Spring @Service", "Regras de negócio: AtivoService, ManutencaoService, AlertNotificationService, HealthCheckService, AuditoriaService")
        Container(scheduler, "Scheduler / Jobs", "Spring TaskScheduler / @Scheduled", "Jobs periódicos: checkResourceUsageAlerts (≤30s/12k ativos), updateHealthCheck")
        ContainerDb(db_primary, "Primary Database", "Motor relacional TBD (PostgreSQL/Oracle/SQL Server/MySQL)", "Persistência transacional: Ativos, Manutenções, HealthChecks, Usuários, Auditoria, Configurações; HikariCP 500 conn")
        ContainerDb(db_replica, "Read Replica", "Mesmo motor do primário (replicação nativa)", "Consultas analíticas (custoTotalPorAtivo), health checks de leitura, offload de relatórios")
        Container(worm, "WORM Audit Storage", "Object Storage S3/MinIO/GCS + Object Lock", "Logs de auditoria imutáveis (append-only) para operações de escrita - SOX 7 anos, LGPD")
    }

    System_Ext(idp, "Identity Provider (Futuro)", "Keycloak / Azure AD / Okta / AD FS", "Autenticação corporativa, MFA para ADMIN, integração AD/LDAP/OIDC")
    System_Ext(email, "Email / Notification Service", "SMTP / REST / Webhook", "Envio de alertas, aprovações, notificações")
    System_Ext(obs, "Observability Stack", "Prometheus/Grafana/Alertmanager/Loki/Tempo (a definir)", "Coleta logs JSON, métricas Prometheus, traces OpenTelemetry, alertas")
    System_Ext(vault, "Secret Manager", "HashiCorp Vault / AWS Secrets Manager", "Rotação de segredos a cada 90 dias (JWT keys, DB passwords, certs)")
    System_Ext(backup, "Backup / Restore Tool", "Native DB tools / pgBackRest / RMAN", "Snapshot diário + WAL/log shipping; runbook restore < 4h")

    Rel(usuario, frontend, "Acessa via HTTPS", "TLS 1.2+")
    Rel(frontend, api, "Requisições REST/JSON", "HTTPS/JSON")
    Rel(api, domain, "Chamadas in-process", "Java direto")
    Rel(domain, scheduler, "Agenda/Dispara jobs", "@Async / TaskScheduler")
    Rel(domain, db_primary, "Lê/Escreve (JDBC/HikariCP)", "Prepared Statements / SQL cru")
    Rel(domain, db_replica, "Lê (ReadOnly)", "JDBC ReadOnly")
    Rel(domain, worm, "Grava auditoria (assíncrono)", "S3 API + Object Lock")
    Rel(api, idp, "Autentica/Valida tokens", "OIDC / SAML 2.0 / LDAP")
    Rel(domain, email, "Envia notificações", "SMTP / REST / Webhook")
    Rel(api, obs, "Exporta métricas/logs/traces", "OTLP gRPC / Prometheus scrape / stdout JSON")
    Rel(api, vault, "Busca segredos em runtime", "HTTPS API")
    Rel(backup, db_primary, "Backup diário + WAL shipping", "Native protocol")
    Rel(admin, api, "Admin endpoints /actuator/*", "HTTPS (rede restrita)")
    Rel(admin, obs, "Dashboards / Alertas", "HTTPS")
    Rel(db_primary, db_replica, "Replicação nativa", "Sync/Async replication")
```

<!-- source: system-architecture.md#L51-L150 -->

## 2. Diagramas de Sequência (Fluxos Críticos)

### 2.1 Autenticação e Autorização (JWT + RBAC)

```mermaid
sequenceDiagram
    autonumber
    actor Usuario as Usuário Final
    participant Frontend as Frontend (Vanilla JS)
    participant API as Web/API Module (Spring Boot)
    participant Security as Spring Security Filter Chain
    participant IDP as Identity Provider (Futuro)
    participant DB as Primary Database
    participant Vault as Secret Manager

    Usuario->>Frontend: Insere credenciais (email/senha)
    Frontend->>API: POST /api/auth/login {email, password}
    API->>Security: AuthenticationManager.authenticate()
    Security->>DB: Busca Usuario + Roles (prepared statement)
    DB-->>Security: Usuario + Roles (ADMIN/GESTOR/OPERADOR/AUDITOR)
    alt Credenciais válidas
        Security->>Vault: Busca chave de assinatura JWT (rotação 90 dias)
        Vault-->>Security: Chave RS256/HS256
        Security->>Security: Gera Access Token (exp ≤ 1h) + Refresh Token (rotation + jti)
        Security-->>API: Authentication success
        API-->>Frontend: 200 OK {accessToken, refreshToken, roles}
        Frontend->>Frontend: Armazena tokens (memory/secure storage)
    else Credenciais inválidas
        Security-->>API: AuthenticationException
        API-->>Frontend: 401 Unauthorized
        Frontend-->>Usuario: Exibe erro de login
    end
    Note over Frontend,API: Requisições subsequentes incluem Authorization: Bearer <accessToken>
    Usuario->>Frontend: Acessa funcionalidade (ex: cadastrar ativo)
    Frontend->>API: GET/POST /api/ativos + Bearer token
    API->>Security: JwtAuthenticationFilter valida token (assinatura, exp, jti blocklist)
    alt Token válido + Role autorizada (@PreAuthorize)
        Security->>DB: Verifica permissões RBAC (BR-01)
        DB-->>Security: Permissão concedida
        Security-->>API: Acesso autorizado
        API->>Domain: AtivoService.criar()
        Domain->>DB: INSERT Ativo (prepared statement)
        Domain->>WORM: AuditoriaService.gravar(criar, payload completo) [async]
        DB-->>Domain: Ativo persistido
        Domain-->>API: AtivoDTO
        API-->>Frontend: 201 Created {ativo}
        Frontend-->>Usuario: Sucesso
    else Token inválido/expirado
        Security-->>API: 401 Unauthorized
        API-->>Frontend: 401
        Frontend->>Frontend: Tenta refresh token / redireciona login
    else Token válido + Role NÃO autorizada
        Security-->>API: 403 Forbidden
        API-->>Frontend: 403
        Frontend-->>Usuario: Acesso negado
    end
```

<!-- source: system-architecture.md#L151-L250; NFR-SEC02, BR-01 -->

### 2.2 Cadastro e Gestão de Ativo Patrimonial

```mermaid
sequenceDiagram
    autonumber
    actor Gestor as Gestor (role GESTOR/ADMIN)
    participant Frontend as Frontend
    participant API as Web/API Module
    participant AtivoSvc as AtivoService
    participant Mapper as AtivoMapper
    participant DB as Primary Database
    participant WORM as WORM Audit Storage
    participant Replica as Read Replica

    Gestor->>Frontend: Preenche formulário "Novo Ativo" (tag, descrição, valor, local, responsável)
    Frontend->>API: POST /api/ativos {tag, descricao, valorAquisicao, localId, responsavelId, ...}
    API->>AtivoSvc: criar(AtivoDTO)
    AtivoSvc->>AtivoSvc: Validações de negócio (tag única, valor > 0, local existe, responsável ativo)
    alt Validação falha
        AtivoSvc-->>API: ValidationException
        API-->>Frontend: 400 Bad Request {errors}
        Frontend-->>Gestor: Exibe erros de validação
    else Validação OK
        AtivoSvc->>DB: INSERT INTO ativo ... (prepared statement, generated keys)
        DB-->>AtivoSvc: AtivoEntity (id gerado)
        AtivoSvc->>Mapper: toDTO(AtivoEntity) [complexidade 14 - refatorar]
        Mapper-->>AtivoSvc: AtivoDTO enriquecido
        AtivoSvc->>WORM: AuditoriaService.gravarAsync(OPERACAO=CRIAR, ENTIDADE=ATIVO, PAYLOAD=AtivoDTO) [fire-and-forget]
        AtivoSvc-->>API: AtivoDTO
        API-->>Frontend: 201 Created {ativo}
        Frontend-->>Gestor: Sucesso - ativo criado
    end
    Note over Gestor,Replica: Consulta posterior (listagem/dashboard)
    Gestor->>Frontend: Acessa dashboard / lista ativos
    Frontend->>API: GET /api/ativos?page=0&size=20&sort=tag
    API->>AtivoSvc: listarPaginado(Pageable)
    AtivoSvc->>Replica: SELECT * FROM ativo ORDER BY tag LIMIT 20 OFFSET 0 (ReadOnly)
    Replica-->>AtivoSvc: List<AtivoEntity>
    AtivoSvc->>Mapper: toDTO(list) [batch mapping]
    Mapper-->>AtivoSvc: List<AtivoDTO>
    AtivoSvc-->>API: Page<AtivoDTO>
    API-->>Frontend: 200 OK {content, totalElements, ...}
    Frontend-->>Gestor: Renderiza tabela com @popperjs/core tooltips
```

<!-- source: system-architecture.md#L251-L350; AtivoMapper.toDTO complexity 14 -->

### 2.3 Solicitação e Aprovação de Manutenção

```mermaid
sequenceDiagram
    autonumber
    actor Operador as Operador (role OPERADOR)
    actor Gestor as Gestor (role GESTOR/ADMIN)
    participant Frontend as Frontend
    participant API as Web/API Module
    participant ManutSvc as ManutencaoService
    participant Spec as ManutencaoSpecification
    participant DB as Primary Database
    participant WORM as WORM Audit Storage
    participant Email as Email Service

    Operador->>Frontend: Clica "Solicitar Manutenção" no ativo
    Frontend->>API: POST /api/manutencoes {ativoId, tipo, descricao, prioridade, dataSolicitada}
    API->>ManutSvc: solicitar(ManutencaoDTO)
    ManutSvc->>DB: Verifica ativo existe e está ativo
    alt Ativo inválido
        ManutSvc-->>API: EntityNotFoundException
        API-->>Frontend: 404 Not Found
    else Ativo válido
        ManutSvc->>DB: INSERT INTO manutencao (status=SOLICITADA, solicitanteId=currentUser, ...)
        DB-->>ManutSvc: ManutencaoEntity (id gerado)
        ManutSvc->>WORM: AuditoriaService.gravarAsync(CRIAR, MANUTENCAO, payload)
        ManutSvc-->>API: ManutencaoDTO
        API-->>Frontend: 201 Created
        Frontend-->>Operador: Solicitação enviada
    end
    Note over Gestor,Email: Fluxo de aprovação (pode ser horas/dias depois)
    Gestor->>Frontend: Acessa "Manutenções Pendentes"
    Frontend->>API: GET /api/manutencoes?status=SOLICITADA
    API->>ManutSvc: listarPorStatus(SOLICITADA)
    ManutSvc->>Spec: build(filtros) [complexidade 14 - índices compostos críticos]
    Spec-->>ManutSvc: Specification<Manutencao>
    ManutSvc->>DB: SELECT * FROM manutencao WHERE status=SOLICITADA (usando spec)
    DB-->>ManutSvc: List<ManutencaoEntity>
    ManutSvc-->>API: List<ManutencaoDTO>
    API-->>Frontend: 200 OK
    Frontend-->>Gestor: Lista exibida
    Gestor->>Frontend: Clica "Aprovar" na manutenção #123
    Frontend->>API: PATCH /api/manutencoes/123/aprovar {aprovadorId, observacao}
    API->>ManutSvc: aprovar(123, aprovadorId, observacao)
    ManutSvc->>DB: UPDATE manutencao SET status=APROVADA, aprovadorId=?, dataAprovacao=NOW() WHERE id=123
    ManutSvc->>WORM: AuditoriaService.gravarAsync(APROVAR, MANUTENCAO, payload antes/depois)
    ManutSvc->>Email: Notifica solicitante + equipe técnica (template aprovação)
    Email-->>ManutSvc: Enviado (async)
    ManutSvc-->>API: ManutencaoDTO atualizada
    API-->>Frontend: 200 OK
    Frontend-->>Gestor: Aprovado com sucesso
```

<!-- source: system-architecture.md#L351-L450; ManutencaoSpecification.build complexity 14 -->

### 2.4 Job Crítico: Verificação de Alertas de Recursos (checkResourceUsageAlerts)

```mermaid
sequenceDiagram
    autonumber
    participant Scheduler as Spring TaskScheduler
    participant AlertSvc as AlertNotificationService
    participant DB as Primary Database
    participant Replica as Read Replica
    participant WORM as WORM Audit Storage
    participant Email as Email Service
    participant Obs as Observability (Metrics/Logs)

    Note over Scheduler,Obs: Executado a cada 30s (NFR-P04: ≤30s para 12k ativos)
    Scheduler->>AlertSvc: @Scheduled(fixedDelay=30000) checkResourceUsageAlerts()
    AlertSvc->>Obs: Inicia span OpenTelemetry "checkResourceUsageAlerts"
    AlertSvc->>Replica: SELECT a.id, a.tag, a.tipo, h.cpu_usage, h.mem_usage, h.disk_usage, h.timestamp\nFROM ativo a\nJOIN health_check h ON h.ativo_id = a.id\nWHERE h.timestamp > NOW() - INTERVAL '5 minutes'\nAND a.ativo = true\nORDER BY a.id [otimizado com índice composto (ativo_id, timestamp)]
    Replica-->>AlertSvc: List<HealthCheckRecente> (até 12k registros)
    alt Query > 5s (degradação)
        AlertSvc->>Obs: Métrica job.query.duration > 5s + log WARN
    end
    par Processamento em lote (paralelismo controlado)
        AlertSvc->>AlertSvc: Para cada ativo: avalia thresholds (CPU>80%, MEM>85%, DISK>90%)
        AlertSvc->>AlertSvc: Agrupa alertas por ativo + severidade (CRITICAL/WARNING/INFO)
        AlertSvc->>DB: INSERT INTO alerta (ativo_id, tipo, severidade, mensagem, timestamp) ON CONFLICT DO NOTHING
        AlertSvc->>WORM: AuditoriaService.gravarAsync(ALERTA_GERADO, ALERTA, payload) [batch async]
        AlertSvc->>Email: Envia notificações CRITICAL (async, circuit breaker Resilience4j)
    and Métricas de performance
        AlertSvc->>Obs: Registra métricas: job.duration, job.assets_processed, job.alerts_generated, job.errors
    end
    alt Falha no job (exception não tratada)
        AlertSvc->>Obs: Incrementa counter job.failure + log ERROR com stacktrace
        AlertSvc->>Obs: Alerta Alertmanager: "checkResourceUsageAlerts falhou"
    else Sucesso
        AlertSvc->>Obs: Finaliza span com status OK
    end
    Note over AlertSvc: Complexidade ciclomática 17 - REQUER REFACTORING (NFR-M02)\nDividir em: fetchHealthChecks, evaluateThresholds, persistAlerts, notifyCritical
```

<!-- source: system-architecture.md#L451-L550; AlertNotificationService.checkResourceUsageAlerts complexity 17, NFR-P04 -->

### 2.5 Health Check de Ativo e Atualização Periódica

```mermaid
sequenceDiagram
    autonumber
    participant Scheduler as Spring TaskScheduler
    participant HealthSvc as HealthCheckService
    participant DB as Primary Database
    participant Replica as Read Replica
    participant WORM as WORM Audit Storage
    participant Obs as Observability

    Note over Scheduler,Obs: Executado a cada 5min (configurável) - updateHealthCheck
    Scheduler->>HealthSvc: @Scheduled(fixedDelay=300000) updateHealthCheck()
    HealthSvc->>Replica: SELECT id, tag, tipo, endpoint_monitoramento FROM ativo WHERE ativo=true AND monitorado=true
    Replica-->>HealthSvc: List<AtivoMonitorado>
    par Para cada ativo (paralelo com semáforo max 50 concurrent)
        HealthSvc->>HealthSvc: Executa check específico por tipo (SNMP, SSH, HTTP, Ping, Agent)
        alt Check bem-sucedido
            HealthSvc->>DB: INSERT INTO health_check (ativo_id, cpu, mem, disk, status=UP, timestamp=NOW())
        else Check falhou / timeout
            HealthSvc->>DB: INSERT INTO health_check (ativo_id, status=DOWN, erro=?, timestamp=NOW())
            HealthSvc->>WORM: AuditoriaService.gravarAsync(HEALTH_CHECK_FALHA, HEALTH_CHECK, payload)
        end
    and Métricas agregadas
        HealthSvc->>Obs: Métricas: health_check.duration, health_check.up_count, health_check.down_count
    end
    HealthSvc->>Obs: Log estruturado JSON {timestamp, level=INFO, traceId, message="Health check cycle completed", assetsChecked=X, up=Y, down=Z}
    Note over HealthSvc: Indicador customizado em /actuator/health inclui:\n- DB connectivity\n- Scheduler last run status\n- Disk space\n- WORM storage accessibility
```

<!-- source: system-architecture.md#L551-L650; NFR-O02 -->

### 2.6 Consulta Analítica: Custo Total por Ativo (Read Replica Offload)

```mermaid
sequenceDiagram
    autonumber
    actor Auditor as Auditor (role AUDITOR)
    participant Frontend as Frontend
    participant API as Web/API Module
    participant AtivoSvc as AtivoService
    participant Cache as Cache L1 (Caffeine - Inferido)
    participant Replica as Read Replica
    participant WORM as WORM Audit Storage

    Auditor->>Frontend: Acessa relatório "Custo Total por Ativo"
    Frontend->>API: GET /api/relatorios/custo-total-por-ativo?ano=2024
    API->>AtivoSvc: gerarRelatorioCustoTotal(2024)
    AtivoSvc->>Cache: Get "custoTotalPorAtivo:2024"
    alt Cache HIT (TTL 5-15 min)
        Cache-->>AtivoSvc: Relatório pré-computado
        AtivoSvc-->>API: RelatórioDTO
        API-->>Frontend: 200 OK (rápido)
    else Cache MISS
        AtivoSvc->>Replica: SELECT a.id, a.tag, a.descricao,\n       COALESCE(SUM(m.custo),0) as custo_manutencoes,\n       COALESCE(SUM(aq.valor),0) as valor_aquisicao,\n       (COALESCE(SUM(m.custo),0) + COALESCE(SUM(aq.valor),0)) as custo_total\nFROM ativo a\nLEFT JOIN manutencao m ON m.ativo_id = a.id AND m.status IN ('CONCLUIDA','APROVADA') AND EXTRACT(YEAR FROM m.data_conclusao) = 2024\nLEFT JOIN aquisicao aq ON aq.ativo_id = a.id AND EXTRACT(YEAR FROM aq.data) = 2024\nGROUP BY a.id, a.tag, a.descricao\nORDER BY custo_total DESC
        Replica-->>AtivoSvc: ResultSet (pode ser lento - query pesada)
        AtivoSvc->>AtivoSvc: Constrói RelatórioDTO
        AtivoSvc->>Cache: Put "custoTotalPorAtivo:2024" (TTL 10 min)
        AtivoSvc->>WORM: AuditoriaService.gravarAsync(CONSULTA_RELATORIO, RELATORIO_CUSTO, {ano:2024, usuario:auditorId})
        AtivoSvc-->>API: RelatórioDTO
        API-->>Frontend: 200 OK
    end
    Frontend-->>Auditor: Renderiza tabela/gráfico (export CSV/PDF)
    Note over AtivoSvc,Replica: [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]\nCache L1 Caffeine assumido para custoTotalPorAtivo.\nSe read-replica insuficiente, avaliar Redis L2 distribuído.
```

<!-- source: system-architecture.md#L651-L750; NFR-S01, cache strategy inferred -->

## 3. Diagrama de Componentes (Visão Estática - Módulos Internos do Monolito)

```mermaid
graph TD
    subgraph "Presentation Layer"
        REST[REST Controllers\n@Valid + @PreAuthorize]
        Static[Static Resource Handler\n/frontend/**]
        Actuator[Actuator Endpoints\n/actuator/health, /actuator/prometheus]
    end

    subgraph "Security Layer"
        FilterChain[SecurityFilterChain\nJWT + RBAC]
        AuthMgr[AuthenticationManager]
        JwtUtil[JwtTokenProvider\nRS256/HS256 + jti blocklist]
    end

    subgraph "Application Services (Domain Layer)"
        AtivoSvc[AtivoService\nCRUD + validações]
        ManutSvc[ManutencaoService\nWorkflow: SOLICITADA→APROVADA→EM_ANDAMENTO→CONCLUIDA/CANCELADA]
        AlertSvc[AlertNotificationService\ncheckResourceUsageAlerts (refatorar)]
        HealthSvc[HealthCheckService\nupdateHealthCheck + custom indicators]
        AuditSvc[AuditoriaService\nAsync WORM write + LGPD anonymization]
        ConfigSvc[ConfiguracaoService\nCacheable lookups]
    end

    subgraph "Data Access Layer"
        AtivoRepo[AtivoRepository\nJpaRepository + @Query natives]
        ManutRepo[ManutencaoRepository\n+ ManutencaoSpecification]
        HealthRepo[HealthCheckRepository]
        UserRepo[UsuarioRepository\n+ Role/Permission queries]
        AuditRepo[AuditoriaLogRepository\nWrite-only to WORM]
        JdbcTpl[JdbcTemplate\nRaw SQL otimizado para relatórios]
    end

    subgraph "Mappers / Specifications"
        AtivoMap[AtivoMapper.toDTO\n(complexidade 14 - refatorar)]
        ManutSpec[ManutencaoSpecification.build\n(complexidade 14 - índices compostos)]
    end

    subgraph "Scheduler / Async"
        TaskScheduler[TaskScheduler\nThreadPool: web, scheduler, audit]
        AsyncExec[@Async Executors\nIsolamento bulkhead]
    end

    subgraph "External Integrations (Outbound)"
        IdPClient[IdP Client\nOIDC/SAML/LDAP]
        EmailClient[Email/Notification Client\nSMTP/REST + CircuitBreaker]
        WormClient[WORM S3 Client\nObject Lock + Retry/Backoff]
        ObsClient[OpenTelemetry SDK\nOTLP gRPC Exporter]
        VaultClient[Vault/Secrets Client\nRotation 90 dias]
    end

    REST --> FilterChain
    FilterChain --> AuthMgr
    AuthMgr --> JwtUtil
    JwtUtil --> VaultClient
    REST --> AtivoSvc
    REST --> ManutSvc
    REST --> AlertSvc
    REST --> HealthSvc
    REST --> AuditSvc
    Static --> Actuator
    AtivoSvc --> AtivoRepo
    AtivoSvc --> AtivoMap
    ManutSvc --> ManutRepo
    ManutSvc --> ManutSpec
    HealthSvc --> HealthRepo
    AuditSvc --> AuditRepo
    AuditSvc --> WormClient
    AtivoSvc --> JdbcTpl
    ManutSvc --> JdbcTpl
    AlertSvc --> TaskScheduler
    HealthSvc --> TaskScheduler
    TaskScheduler --> AsyncExec
    AtivoSvc --> ConfigSvc
    ManutSvc --> EmailClient
    AlertSvc --> EmailClient
    FilterChain --> IdPClient
    REST --> ObsClient
    AtivoSvc --> ObsClient
    ManutSvc --> ObsClient
```

<!-- source: system-architecture.md#L751-L850; diagnostic AST classes -->

## 4. Diagrama de Casos de Uso / Atividade (Fluxos de Negócio Principais)

```mermaid
flowchart TD
    Start([Início: Usuário autenticado]) --> Role{Role do Usuário}
    
    Role -->|ADMIN| AdminFlow[Gestão Completa\n- Usuários/Roles\n- Configurações sistema\n- Auditoria completa\n- Rotação segredos]
    Role -->|AUDITOR| AuditorFlow[Acesso Leitura + Relatórios\n- Dashboard executivo\n- Custo total por ativo\n- Trilha auditoria imutável\n- Export SOX/LGPD]
    Role -->|GESTOR| GestorFlow[Gestão Patrimonial\n- CRUD Ativos\n- Aprovar/Rejeitar Manutenções\n- Dashboards operacionais\n- Alertas críticos]
    Role -->|OPERADOR| OperadorFlow[Operação Diária\n- Solicitar Manutenção\n- Acompanhar próprias solicitações\n- Visualizar Health Checks\n- Receber Alertas]
    
    AdminFlow --> AuditLog[AuditoriaService.gravarAsync\nTodas operações escrita\nWORM Storage]
    AuditorFlow --> AuditLog
    GestorFlow --> AuditLog
    OperadorFlow --> AuditLog
    
    GestorFlow --> AtivoCRUD[(Ativo: Create/Read/Update/Delete)]
    GestorFlow --> ManutAprov[Manutenção: Aprovar/Rejeitar]
    OperadorFlow --> ManutSolic[Manutenção: Solicitar]
    OperadorFlow --> ManutAcomp[Manutenção: Acompanhar]
    
    AtivoCRUD --> DB[(Primary DB)]
    ManutAprov --> DB
    ManutSolic --> DB
    ManutAcomp --> Replica[(Read Replica)]
    
    Scheduler((Scheduler\n@Scheduled)) --> AlertJob[checkResourceUsageAlerts\n≤30s / 12k ativos]
    Scheduler --> HealthJob[updateHealthCheck\nA cada 5 min]
    
    AlertJob --> Replica
    AlertJob --> DB
    AlertJob --> WORM[(WORM Audit)]
    AlertJob --> Email[Email/Notification]
    
    HealthJob --> Replica
    HealthJob --> DB
    HealthJob --> WORM
    
    DB --> Replica[Replicação Nativa]
    DB --> Backup[Backup Diário + WAL\nRPO ≤ 24h / RTO ≤ 4h]
    
    AuditLog --> WORM
    WORM --> Compliance[SOX 7 anos / LGPD\nImutabilidade verificada]
    
    subgraph "Observabilidade Transversal"
        Obs[OpenTelemetry Java Agent\nLogs JSON + Métricas Prometheus + Traces OTLP]
        Alerting[Alertmanager\nP0: Latência p95>500ms, Erro>0.1%, Job falha, CPU>80%, Heap>85%, Disco>85%, Replica lag>60s]
    end
    
    REST[REST Controllers] --> Obs
    AtivoSvc[AtivoService] --> Obs
    ManutSvc[ManutencaoService] --> Obs
    AlertSvc[AlertNotificationService] --> Obs
    HealthSvc[HealthCheckService] --> Obs
    Scheduler --> Obs
    
    Alerting --> Pager[PagerDuty/Slack/Email\nOn-call rotation]
```

<!-- source: system-architecture.md#L851-L950; BRD roles, NFR-O04 -->

## 5. Diagrama de Estado: Ciclo de Vida de Manutenção

```mermaid
stateDiagram-v2
    [*] --> SOLICITADA: Operador solicita
    SOLICITADA --> APROVADA: Gestor aprova
    SOLICITADA --> CANCELADA: Operador/Gestor cancela
    SOLICITADA --> REJEITADA: Gestor rejeita
    
    APROVADA --> EM_ANDAMENTO: Técnico inicia
    APROVADA --> CANCELADA: Gestor cancela (antes de iniciar)
    
    EM_ANDAMENTO --> CONCLUIDA: Técnico finaliza + relatório
    EM_ANDAMENTO --> AGUARDANDO_PECAS: Peças necessárias
    EM_ANDAMENTO --> CANCELADA: Gestor cancela (excepcional)
    
    AGUARDANDO_PECAS --> EM_ANDAMENTO: Peças recebidas
    AGUARDANDO_PECAS --> CANCELADA: Cancelamento por obsolescência
    
    CONCLUIDA --> [*]: Fim do ciclo
    CANCELADA --> [*]: Fim do ciclo
    REJEITADA --> [*]: Fim do ciclo
    
    note right of SOLICITADA
        Auditoria: CRIAR (payload completo)
        Notificação: Gestores notificados
    end note
    
    note right of APROVADA
        Auditoria: APROVAR (antes/depois)
        Notificação: Solicitante + Equipe técnica
        Agendamento: Técnico alocado
    end note
    
    note right of EM_ANDAMENTO
        Auditoria: INICIAR
        Health Check: Monitoramento ativo
    end note
    
    note right of CONCLUIDA
        Auditoria: CONCLUIR (relatório + custo + peças)
        Custo: Atualiza custoTotalPorAtivo (invalida cache)
        Notificação: Solicitante + Gestor
    end note
    
    note right of CANCELADA
        Auditoria: CANCELAR (motivo obrigatório)
        Notificação: Partes envolvidas
    end note
```

<!-- source: system-architecture.md#L951-L1050; ManutencaoService workflow -->

## 6. Convenções de Manutenção

- **Fonte única da verdade:** este arquivo é gerado/atualizado pelo próprio fluxo de documentação — não editar diagramas fora deste artefato.
- **Sem binários:** nunca anexar `.png`/`.jpg`/`.drawio`; o diagrama é sempre o bloco Mermaid acima.
- **Atualização obrigatória:** qualquer PR que altere fluxos entre serviços, contratos de API ou topologia de containers deve atualizar a seção correspondente aqui.
- **Rastreabilidade:** cada diagrama referencia a seção do `system-architecture.md` de onde foi derivado via comentários `<!-- source: ... -->`.
- **Inferências marcadas:** todo elemento não explicitamente no código ou SAD carrega `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` com premissas documentadas.
- **Versionamento:** diagramas evoluem com o código; breaking changes na arquitetura exigem ADR correspondente.
- **Validação CI:** pipeline deve validar sintaxe Mermaid (`mermaid-cli` ou similar) em todo PR que toque este arquivo.