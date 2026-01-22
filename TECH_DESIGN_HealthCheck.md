# TECH DESIGN — Fase 2: HealthCheck Automatizado

Meta
- Data/Hora: 2025-10-24 00:38
- Versão: 1.0 (inicial)
- Escopo: Coleta automática de métricas de hardware (CPU, memória, disco, rede) usando OSHI, persistência histórica e alertas básicos.

1. Arquitetura
- Componentes
  - OSHIHealthCheckCollector (componente): encapsula integração com biblioteca OSHI para coletar snapshots do host.
  - IHealthCheckService (interface): orquestra coleta, persistência, consulta e alertas.
  - HealthCheckServiceImpl (service): implementação principal, transacional onde necessário.
  - HealthCheckHistory (entidade JPA): registro histórico por coleta.
  - HealthCheckHistoryRepository (JPA): acesso aos dados históricos.
  - HealthCheckScheduler (@Scheduled): agenda coleta a cada 12 horas (configurável via properties).
  - HealthCheckAlertService (component): aplica regras simples de alerta (limiares configuráveis).
  - HealthCheckDTOs (records): request/response para APIs.
  - HealthCheckController (opcional nesta fase): endpoints de consulta de histórico e estado atual.
- Fluxo Principal (coleta)
  1) Scheduler dispara coleta → Service chama Collector (OSHI) 
  2) Service normaliza e valida dados → persiste HealthCheckHistory 
  3) Service aciona AlertService com métricas → gera eventos/logs (futuro: enviar para notificações)
- Observabilidade
  - Métricas Micrometer: 
    - counter aegis_healthcheck_collect_total{status="success|fail"}
    - gauge aegis_healthcheck_cpu_usage (last snapshot)
    - gauge aegis_healthcheck_mem_free_percent (last snapshot)
    - timer aegis_healthcheck_collect_timer
  - Logs estruturados SLF4J (sem dados sensíveis).

2. Decisões Técnicas
- Biblioteca: OSHI (oshi-core). Justificativa: sem dependência nativa, cross-platform.
- Frequência: 12 horas (config: aegis.healthcheck.collect.cron, default: "0 0 0/12 * * *").
- Persistência: HealthCheckHistory com índices por createdAt e hostId (se multi-host no futuro). TTL/retention via job de limpeza (futuro).
- Escopo inicial: CPU usage (%), memória livre (%), disco livre por volume (%), rede (bytes tx/rx por interface). Expandir conforme necessidade.
- Configuração thresholds: application.yml → aegis.healthcheck.thresholds: cpuHigh=0.90, memLow=0.10, diskLow=0.10.

3. Modelo de Dados (proposta)
- Entidade HealthCheckHistory
  - id (BIGINT, PK)
  - createdAt (TIMESTAMP)
  - host (VARCHAR 128, default: hostname)
  - cpuUsage (DECIMAL(5,4))
  - memFreePercent (DECIMAL(5,4))
  - disks (JSON/text) — lista com mount, total, freePercent
  - nets (JSON/text) — lista com iface, bytesTx, bytesRx
  - indices: idx_hch_createdAt, idx_hch_host_createdAt
- DTOs
  - HealthCheckSnapshotDTO(cpuUsage, memFreePercent, List<DiskDTO>, List<NetDTO>, createdAt)
  - DiskDTO(mount, totalBytes, freeBytes, freePercent)
  - NetDTO(interfaceName, bytesTx, bytesRx)

4. Endpoints (mínimo viável)
- GET /healthcheck/last → snapshot mais recente
- GET /healthcheck/history?from&to&page → histórico paginado
- GET /healthcheck/alerts/recent → últimos eventos de alerta (MVP: derivar de logs até entidade própria)
- Segurança: @PreAuthorize("hasAnyRole('ADMIN','USER')"), futura granularidade.

5. Segurança
- Não coletar/processar dados sensíveis (somente métricas de hardware).
- Rate limit para endpoints de consulta (via filter/futuro gateway).
- Erros tratados no ApplicationControllerAdvice; sem dados internos em mensagens.

6. Testes
- Unitários (≥80% serviço/collector/alertas):
  - HealthCheckServiceImpl: cenários de coleta, persistência, erros do collector.
  - HealthCheckAlertService: limiares e geração de flags.
  - OSHIHealthCheckCollector: mock de OSHI; validar mapeamento.
- Integração (MockMvc):
  - GET /healthcheck/last: 200 com payload correto.
  - GET /healthcheck/history: filtros from/to e paginação.
- TestContainers (opcional): validar persistência.

7. Métricas e Observabilidade (Micrometer)
- Registrar counters, gauges e timer conforme seção 1.
- Expor via /actuator/prometheus (já habilitado no projeto se aplicável).

8. Backlog Detalhado (para Checklist)
- Collector: classe + mapeamento OSHI
- Service + Interface: orquestração e validações
- Scheduler: @Scheduled + config externa
- Entidade + Repository: HealthCheckHistory + índices Flyway
- AlertService: regras básicas
- Endpoints GET: last, history, alerts (MVP)
- Métricas Micrometer: counters/gauges/timer
- Testes unitários + integração
- Documentação OpenAPI

9. Riscos e Mitigações
- Carga de escrita excessiva: reduzir frequência ou amostrar; compactação/retention.
- Compatibilidade OSHI: validar em ambiente alvo; fallback parcial se métrica indisponível.
- Crescimento de tabela: índices adequados e política de retenção.

10. Próximos Passos
- Validar este design com PO/Arquitetura.
- Aprovar backlog e iniciar Sprint 4.
