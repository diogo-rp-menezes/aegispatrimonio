# Operations Runbook & System Maintenance Guide — Aegis Patrimônio

> **Owner:** [SRE/DevOps/Eng On-call] · **Última revisão:** [DD/MM/AAAA]
> **Sistema de alerta:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — Nenhuma ferramenta de alerta detectada no diagnóstico (ausência de configurações PagerDuty, Opsgenie, Grafana Alerting, etc.)
> **Canal de incidentes:** [ENTRADA HUMANA NECESSÁRIA] — Não definido no projeto

## 1. System Overview

### Componentes Críticos
| Componente | Tecnologia | Localização | Status Operacional |
| :--- | :--- | :--- | :--- |
| **Backend API** | Java (333 arquivos em `src/main/java/br/com/aegispatrimonio`) | `src/main/java` | **Desconhecido** — Build system não detectado |
| **Frontend** | JavaScript (15 arquivos em `frontend/src`) | `frontend/src` | **Desconhecido** — Build scripts não detectados |
| **Data Seeder** | Java — `RealisticDataSeeder.java` | `src/main/java/br/com/aegispatrimonio/config/seeder/` | Complexidade ciclomática alta (15) — refatoração pendente (US-TECH-007) |
| **Alert Notification Service** | Java — `AlertNotificationService.java` | `src/main/java/br/com/aegispatrimonio/service/` | Complexidade ciclomática alta (17) em `checkResourceUsageAlerts` |

### Diagrama de Arquitetura
> **Referência:** `system-architecture.md` (não encontrado no diagnóstico — [ENTRADA HUMANA NECESSÁRIA])

### Dependências Externas Críticas
| Dependência | Tipo | Status de Detecção |
| :--- | :--- | :--- |
| **Banco de dados** | Relacional (PostgreSQL 15 assumido) | **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Diagnóstico: "nenhum motor de banco conhecido encontrado nas dependências" |
| **JWT Signing Key (RS256)** | Segurança/Autenticação | Citado no Test Strategy Seção 5 — não encontrado no código |
| **SMTP Server** | Notificações | Citado no Test Strategy Seção 5, US-MON-004 EX-4 — não configurado |
| **Storage WORM** | Auditoria/Compliance | Citado no Test Strategy Seção 5, US-ATIVO-004 EX-5 — não configurado |
| **@popperjs/core** | Frontend UI (tooltip/popover) | **Confirmado** — `package.json` dependency |

> **Gap crítico:** O diagnóstico não encontrou `pom.xml`, `build.gradle`, `Dockerfile`, `docker-compose.yml`, Kubernetes manifests, Terraform, ou qualquer artefato de infraestrutura. A arquitetura de deploy **não pode ser documentada** sem validação humana.

---

## 2. Monitoring & System Health Check

> **Estado atual:** **Nenhuma ferramenta de monitoramento/observabilidade detectada** no diagnóstico (ausência de Prometheus, Grafana, Datadog, New Relic, ELK, Loki, Jaeger, Zipkin, Micrometer, Spring Boot Actuator config).

| Métrica | Ferramenta | Threshold Saudável | Threshold de Alerta | Status |
| :--- | :--- | :--- | :--- | :--- |
| Latência API (p95) | [ENTRADA HUMANA NECESSÁRIA] | < 300 ms (listagem ativos) / < 800 ms (login) | > 800 ms / > 1.5 s | **Não instrumentado** |
| Taxa de erro 5xx | [ENTRADA HUMANA NECESSÁRIA] | < 0.1% (guardrail BRD citado no Test Strategy) | > 0.1% | **Não instrumentado** |
| Throughput health check ingest | [ENTRADA HUMANA NECESSÁRIA] | 11 req/s (Test Strategy Seção 5) | < 5 req/s | **Não instrumentado** |
| Saturação CPU | [ENTRADA HUMANA NECESSÁRIA] | < 70% | > 85% | **Não instrumentado** |
| Saturação Memória (Heap/Non-Heap) | [ENTRADA HUMANA NECESSÁRIA] | < 70% | > 85% | **Não instrumentado** |
| Conexões DB ativas/pool | [ENTRADA HUMANA NECESSÁRIA] | < 80% pool | > 90% pool | **Não instrumentado** — DB não confirmado |
| Fila de notificações/alertas | [ENTRADA HUMANA NECESSÁRIA] | — | Crescimento contínuo > 5 min | **Não instrumentado** — `AlertNotificationService` existe mas sem métricas expostas |

### Dashboards Principais
- **[ENTRADA HUMANA NECESSÁRIA]** — Nenhum dashboard configurado detectado

### Health Check Endpoint
- **Endpoint esperado:** `GET /actuator/health` ou equivalente Spring Boot **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
- **Status:** Não verificado — diagnóstico não confirma Spring Boot Actuator

### Métricas Específicas de Banco
> **Referência:** `db-performance-recovery.md` (não encontrado no diagnóstico — [ENTRADA HUMANA NECESSÁRIA])
> **Nota:** Banco de dados não confirmado no diagnóstico ("nenhum motor de banco conhecido")

---

## 3. On-Call & Escalation

> **Estado atual:** **Processo de on-call não definido** — não há documentação de rotação, ferramenta de alerta, ou runbooks de incidente.

| Nível | Papel | Tempo de Resposta Esperado | Contato | Status |
| :--- | :--- | :--- | :--- | :--- |
| **L1** | On-call Engineer | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] | **Não definido** |
| **L2** | Tech Lead / Senior Engineer | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] | **Não definido** |
| **L3** | Eng Manager / Incident Commander | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] | **Não definido** |

### Rotação de Plantão
- **Schedule:** [ENTRADA HUMANA NECESSÁRIA]
- **Handoff process:** [ENTRADA HUMANA NECESSÁRIA]
- **Escalação automática:** [ENTRADA HUMANA NECESSÁRIA] — depende de ferramenta de alerta

---

## 4. Failure Recovery Procedures

> **Limitação:** Sem infraestrutura, monitoramento, ou deploy pipeline definidos, os procedimentos abaixo são **esqueleto genérico** baseado nos componentes de código identificados. **Requer validação humana completa antes de uso operacional.**

### Incident Type 1: Backend API Indisponível / Retornando 5xx em Massa
* **Symptom:** 
  - Alerta de taxa de erro 5xx > 0.1% (se monitoramento existisse)
  - Usuários reportam falha em listagem de ativos, login, ou dashboard
  - Health check endpoint (se existente) retorna DOWN ou timeout
* **Possíveis Causas:**
  1. JVM OOM / GC thrashing (heap não monitorado)
  2. Pool de conexões DB esgotado (DB não confirmado)
  3. Deadlock em `AlertNotificationService.checkResourceUsageAlerts` (complexidade 17)
  4. Falha na validação de JWT (chave RS256 rotacionada/expirada — Test Strategy menciona rotação 90 dias)
  5. `RealisticDataSeeder` travado em startup (complexidade 15, execução síncrona?)
* **Diagnóstico:**
  1. Verificar logs da aplicação (`stdout`/`stderr` do processo Java) — **localização de logs não definida**
  2. Verificar uso de CPU/Memória do processo Java (`top`, `jstat`, `jcmd`) — **acesso ao host não documentado**
  3. Verificar conectividade com banco (se aplicável) — **connection string não documentada**
  4. Verificar se `RealisticDataSeeder` completou execução (logs de startup)
  5. Verificar expiração/rotação de chave JWT (se implementado)
* **Remediation:**
  1. **Reiniciar processo Java** — comando exato [ENTRADA HUMANA NECESSÁRIA] (systemd? docker? k8s? java -jar?)
  2. Se OOM: aumentar `-Xmx` / `-Xms` — **JVM args não documentados**
  3. Se pool DB esgotado: reiniciar DB / aumentar pool — **configuração pool não documentada**
  4. Se JWT: verificar rotação de chaves (90 dias per Test Strategy) — **keystore location não documentado**
* **Verificação pós-remediação:**
  - Health check endpoint retorna UP
  - Login funcional (P95 ≤ 800ms)
  - Listagem ativos funcional (P95 ≤ 300ms)
  - Health check ingest throughput ≥ 11 req/s
* **Severidade típica:** P0 (indisponibilidade total) / P1 (degradação severa)

### Incident Type 2: Frontend Não Carrega / Erros de Build em Runtime
* **Symptom:**
  - Página em branco, erros de console JS, falha ao chamar API backend
  - Erros de CORS, 404 em assets, ou falha de autenticação
* **Possíveis Causas:**
  1. Build do frontend não executado / bundle desatualizado
  2. Variável de ambiente `API_BASE_URL` incorreta ou ausente
  3. CORS mal configurado no backend
  4. `@popperjs/core` versão incompatível (única dependência declarada)
  5. `console.error`/`console.debug` residuais em `frontend/src/services/api.js:36,99` mascarando erros reais
* **Diagnóstico:**
  1. Abrir DevTools → Console / Network — identificar requests falhando
  2. Verificar `Network` → request para `/api/...` — status, CORS headers
  3. Verificar se `frontend/src/services/api.js` aponta para URL correta
  4. Verificar se build artifacts existem no servidor web / CDN
* **Remediation:**
  1. Rebuild frontend — **comando de build não detectado** (sem `package.json` scripts)
  2. Corrigir `API_BASE_URL` — **variável não documentada**
  3. Ajustar CORS no backend — **configuração não localizada**
  4. Remover `console.*` residuais (`api.js:36,99`)
* **Verificação pós-remediação:**
  - Página carrega sem erros de console
  - Login → dashboard navega corretamente
  - Requests API retornam 200 com payload esperado
* **Severidade típica:** P1 (frontend quebrado = usuários não conseguem operar)

### Incident Type 3: Alertas/Notificações Não Enviados
* **Symptom:**
  - Usuários não recebem e-mails de alerta de manutenção, vencimento, ou uso de recurso
  - `AlertNotificationService` logs mostram falhas (se logado)
* **Possíveis Causas:**
  1. Credenciais SMTP inválidas/expiradas
  2. `AlertNotificationService.checkResourceUsageAlerts` travado (complexidade 17, possível loop/infinite recursion)
  3. Fila de e-mails cheia / backend SMTP indisponível
  4. Template de e-mail com erro (NullPointer em `Usuario.setUsername` — stub vazio linha 86)
* **Diagnóstico:**
  1. Verificar logs de `AlertNotificationService` — **localização logs não definida**
  2. Testar conectividade SMTP (telnet/nc) — **host/porta SMTP não documentados**
  3. Verificar `Usuario.setUsername` stub — pode causar NPE ao montar e-mail
* **Remediation:**
  1. Corrigir `Usuario.setUsername` (implementar setter real)
  2. Refatorar `checkResourceUsageAlerts` (quebrar em métodos menores — complexidade 17)
  3. Atualizar credenciais SMTP — **secrets manager não definido**
  4. Reiniciar serviço de notificação (se separado) ou app completo
* **Verificação pós-remediação:**
  - E-mail de teste enviado com sucesso
  - Logs mostram processamento de alertas sem erros
* **Severidade típica:** P2 (funcionalidade de alerta degradada, core API operacional)

### Incident Type 4: Seed de Dados Falha / Dados Inconsistentes em Staging/Local
* **Symptom:**
  - `RealisticDataSeeder.run()` falha ou trava (complexidade 15)
  - Testes de integração/E2E falham por dados ausentes/incorretos
  - Dados anonimizados LGPD não aplicados corretamente
* **Possíveis Causas:**
  1. Lógica complexa no seeder com múltiplos caminhos condicionais
  2. Dependência de banco não disponível no momento do seed
  3. Constraints de FK/unique violadas por ordem de inserção
  4. Volume de dados "realista" muito grande para ambiente de teste
* **Diagnóstico:**
  1. Executar seeder isoladamente com logs verbosos
  2. Verificar constraints DB (FK, unique, not null) — **schema não documentado**
  3. Verificar se Flyway/Liquibase migrou schema antes do seed — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Remediation:**
  1. Refatorar `RealisticDataSeeder` (US-TECH-007) — quebrar em métodos menores, transações por entidade
  2. Executar seed em etapas ordenadas (usuários → ativos → manutenções → alertas)
  3. Usar dataset menor para CI (Testcontainers) — **Testcontainers module não confirmado**
* **Verificação pós-remediação:**
  - Seeder completa em < 60s (local) / < 30s (CI)
  - Testes de integração passam consistentemente
  - Dados sensíveis anonimizados (LGPD)
* **Severidade típica:** P2 (bloqueia testes/homologação, não afeta produção diretamente)

---

## 5. Backup & Rollback Protocol

> **Referência cruzada:** `db-performance-recovery.md` (não encontrado — [ENTRADA HUMANA NECESSÁRIA])
> **Nota:** Banco de dados não confirmado no diagnóstico. Esta seção cobre apenas rollback de aplicação/deploy.

### Rollback de Deploy
| Cenário | Procedimento | Tempo Alvo | Status |
| :--- | :--- | :--- | :--- |
| **Rollback JAR anterior** | [ENTRADA HUMANA NECESSÁRIA] — Ex: `systemctl restart aegis-backend` com JAR versionado, ou `kubectl rollout undo` | < 5 min (meta inferida) | **Não definido** — sem pipeline, sem artifact registry |
| **Rollback Frontend** | [ENTRADA HUMANA NECESSÁRIA] — Re-deploy bundle estático anterior (nginx/s3/cdn) | < 5 min | **Não definido** — sem build artifacts, sem CDN |
| **Feature Flag Kill Switch** | [ENTRADA HUMANA NECESSÁRIA] — Desligar feature flag problemática | < 1 min | **Não implementado** — Test Strategy não menciona feature flags |
| **Blue-Green Switch** | [ENTRADA HUMANA NECESSÁRIA] — Roteamento load balancer para versão anterior | < 2 min | **Não definido** — infra não documentada |

### Backup de Configuração/Infraestrutura (Fora do Banco)
| Item | Frequência | Retenção | Localização | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Secrets (JWT, SMTP, DB, Storage)** | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] — Secrets manager não definido | **Não configurado** |
| **Configuração de aplicação (application.yml/properties)** | A cada deploy | Últimas 10 versões | Git / ConfigMap / Vault | **Não verificado** |
| **Infraestrutura as Code (Terraform/Helm/Ansible)** | A cada mudança | Git history | Git repo | **Não detectado** — nenhum IaC encontrado |

---

## 6. Maintenance Windows

| Item | Definição | Status |
| :--- | :--- | :--- |
| **Janela padrão** | [ENTRADA HUMANA NECESSÁRIA] — Ex: domingos 02h-04h UTC | **Não definida** |
| **Comunicação prévia** | [ENTRADA HUMANA NECESSÁRIA] — Ex: aviso 48h via e-mail/Slack/statuspage | **Não definida** |
| **Aprovação de mudança** | [ENTRADA HUMANA NECESSÁRIA] — Change Advisory Board / Tech Lead | **Não definida** |
| **Rollback automático se falha** | [ENTRADA HUMANA NECESSÁRIA] | **Não implementado** |

---

## 7. Runbook de Rotina (Checklists Operacionais)

> **Nota:** Sem monitoramento, alerting, ou infraestrutura definidos, estes checklists são **aspiracionais** — requerem implementação de ferramentas antes de serem executáveis.

### Verificação Diária (Manual — até automação existir)
- [ ] Acessar dashboard de saúde (quando existir) — verificar latência, erro, saturação
- [ ] Verificar logs de erro nas últimas 24h — **localização logs não definida**
- [ ] Confirmar execução bem-sucedida de jobs agendados (seeder, alertas, relatórios) — **scheduler não detectado**
- [ ] Verificar espaço em disco / memória nos hosts — **acesso hosts não documentado**
- [ ] Validar certificados TLS (expiração > 30 dias) — **certificados não inventariados**

### Revisão Semanal
- [ ] Revisar alertas silenciados / em "firing" prolongado — **alertmanager não configurado**
- [ ] Verificar backups de configuração/secrets completos — **backup não configurado**
- [ ] Revisar métricas de negócio (logins, ativos cadastrados, manutenções) — **métricas não instrumentadas**
- [ ] Atualizar dependências com vulnerabilidades conhecidas (Semgrep/Dependabot) — **CI/CD não existe**

### Revisão Mensal
- [ ] Revisão de custos de infraestrutura — **infra não definida, custos não rastreados**
- [ ] Rotação de secrets (JWT keys 90 dias, SMTP, DB, Storage) — **política não implementada, secrets manager não definido**
- [ ] Teste de restore de backup (config/secrets) — **backup não configurado**
- [ ] Revisão de capacidade (CPU, RAM, disco, DB connections) — **capacity planning não feito**
- [ ] Atualização de dependências Java (SpotBugs/PMD/Checkstyle findings) — **build system não detectado**

---

## 8. Post-Incident Process

| Item | Definição | Status |
| :--- | :--- | :--- |
| **Postmortem obrigatório para** | Severidade P0 / P1 (conforme Test Strategy Seção 10 — Eng Lead responsible) | **Processo não formalizado** |
| **Template de postmortem** | [ENTRADA HUMANA NECESSÁRIA] — Sugestão: Linha do tempo, Impacto, Causa Raiz (5 Whys), Ações de Follow-up (com owner + prazo) | **Não definido** |
| **Prazo para publicar** | [ENTRADA HUMANA NECESSÁRIA] — Ex: 5 dias úteis após resolução | **Não definido** |
| **Revisão de ação corretiva** | [ENTRADA HUMANA NECESSÁRIA] — Sprint planning / backlog grooming | **Não definido** |
| **Banco de incidentes** | [ENTRADA HUMANA NECESSÁRIA] — Confluence, Notion, GitHub Issues, Jira | **Não definido** |

---

## 9. Contacts & Escalation Paths

> **Estado atual:** **Nenhum contato de emergência documentado** no código ou configuração.

| Sistema/Serviço | Responsável | Contato de Emergência | Backup | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Aplicação Backend (Java)** | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] | **Não definido** |
| **Aplicação Frontend (JS)** | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] | **Não definido** |
| **Banco de Dados** | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] | **Não definido** — DB não confirmado |
| **Infraestrutura / Cloud / VMs** | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] | **Não definido** |
| **Secrets / Certificados** | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] | **Não definido** |
| **Rede / DNS / Load Balancer** | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] | **Não definido** |
| **SMTP / E-mail Transacional** | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] | **Não definido** |
| **Storage WORM (Auditoria)** | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] | [ENTRADA HUMANA NECESSÁRIA] | **Não definido** |

---

## ⚠️ Resumo de Gaps Operacionais Críticos (Bloqueiam Runbook Executável)

| Gap | Impacto no Runbook | Ação Requerida |
| :--- | :--- | :--- |
| **Build system não detectado** (Maven/Gradle, package.json scripts) | Impede definir comandos de build, start, restart, rollback | Workshop: identificar build tool, documentar `pom.xml`/`build.gradle`/`package.json` |
| **Infraestrutura de deploy não definida** (VM, K8s, PaaS, serverless) | Impede procedimentos de start/stop/restart, rollback, scaling, logs | Decisão: alvo de deploy + provisionar IaC |
| **Banco de dados não confirmado** | Impede health checks, connection pool tuning, backup/restore, seed | Confirmar: PostgreSQL? Outro? Connection strings? Migrações (Flyway/Liquibase)? |
| **Containerização não detectada** (Dockerfile, imagem base) | Impede packaging consistente, deploy reprodutível, rollback via tag | Criar Dockerfile multi-stage (backend + frontend) |
| **CI/CD pipeline não existe** | Impede automação de test, build, deploy, quality gates | Implementar pipeline (GitHub Actions/GitLab CI per Test Strategy) |
| **Monitoramento/Observabilidade ausente** | Impede alertas, dashboards, diagnóstico, postmortem data | Implementar: Prometheus+Grafana ou Datadog/NewRelic + OpenTelemetry Java agent |
| **Secrets manager não definido** | Impede rotação segura, inject de config, rollback de secrets | Escolher: Vault, AWS Secrets Manager, GitHub/GitLab Secrets, Azure Key Vault |
| **Feature flags não implementadas** | Limita rollout seguro, kill switch, canary | Avaliar: LaunchDarkly, Unleash, FF4J, ou custom |
| **Processo de on-call não definido** | Sem rotação, sem escalação, sem SLA de resposta | Definir: ferramenta (PagerDuty/Opsgenie), rotação, runbooks por serviço |
| **Logs centralizados não configurados** | Diagnóstico de incidentes depende de SSH em hosts | Implementar: Loki, ELK, Datadog Logs, ou CloudWatch |

---

## 📋 Próximos Passos Recomendados (Pré-requisitos para Runbook Vivo)

1. **Workshop Técnico (2-4h):** Eng Lead + DevOps + DBA + Segurança + Tech Lead
   - Preencher todos os `[ENTRADA HUMANA NECESSÁRIA]` acima
   - Validar todos os `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`
   - Definir: build tool, DB, infra target, CI/CD tool, secrets manager, observabilidade stack

2. **Implementar Mínimo Operacional (Sprint 1-2):**
   - Dockerfile backend + frontend
   - `docker-compose.yml` para local/dev (app + DB + mock SMTP)
   - GitHub Actions/GitLab CI pipeline (stages do Deployment Plan)
   - Spring Boot Actuator + Micrometer + Prometheus endpoint
   - Grafana dashboards (RED metrics + JVM + DB pool)
   - Alertas básicos (P0: app down, 5xx > 0.1%, heap > 85%)

3. **Documentar Runbook Específico (Pós-Sprint 2):**
   - Substituir placeholders deste documento por comandos reais, URLs, contatos
   - Adicionar runbooks por componente (seeder, alertas, auth, ativos)
   - Testar procedimentos em staging (fire drill)

4. **Estabelecer Cadência:**
   - Revisão mensal do runbook (atualizar contatos, thresholds, procedimentos)
   - Fire drill trimestral (simular incidente P0, medir MTTR)
   - Postmortem de todo incidente P0/P1 dentro de 5 dias úteis

---

> **Fonte primária:** Diagnóstico determinístico do codebase (348 arquivos, 333 Java, 15 JS) + Deployment Plan (CI/CD) + Test Strategy (referenciado no Deployment Plan).
> **Rastreabilidade:** Itens marcados com `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` ou `[ENTRADA HUMANA NECESSÁRIA]` originam-se de gaps identificados no diagnóstico ou no Deployment Plan. Nenhuma tecnologia, framework, ou configuração fora do diagnóstico foi assumida como existente.