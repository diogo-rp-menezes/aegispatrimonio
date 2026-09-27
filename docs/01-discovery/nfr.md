# Non-Functional Requirements (NFR) — Aegis Patrimônio

> **Versão:** 1.0 · **Status:** Draft · **Owner:** Engenharia/Arquitetura

## 1. Performance
* **NFR-P01:** Tempo de resposta da API (endpoint `request` em `frontend/src/services/api.js`) ≤ 500ms no percentil 95 (p95) e ≤ 1.000ms no p99 sob carga nominal. <!-- source: BRD#4 (Guardrail Metrics) -->
* **NFR-P02:** Time to First Byte (TTFB) da aplicação web ≤ 200ms em rede local (latência de rede < 5ms). [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **NFR-P03:** Largest Contentful Paint (LCP) da interface principal ≤ 2,5s em conexão 4G (simulada) e ≤ 1,2s em rede corporativa. [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **NFR-P04:** Processamento do job `AlertNotificationService.checkResourceUsageAlerts` (complexidade ciclomática 17) concluído em ≤ 30s para base de 12.000 ativos. <!-- source: Diagnóstico#Funções com complexidade ciclomática alta; BRD#5 (In-Scope: Alertas) -->
* **Método de medição:** Testes de carga com k6/Gatling simulando 100 usuários concorrentes; APM via Spring Boot Actuator `/actuator/metrics/http.server.requests` e frontend `performance.mark`/`measure`. [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]

## 2. Escalabilidade
* **NFR-S01:** Suportar 400 usuários concorrentes (meta corporativa BRD#10 Fase 3) com degradação de throughput ≤ 10% em relação a 100 usuários, em instância única (vertical scaling). <!-- source: BRD#10 (Fase 3: 400+ usuários); Topologia: monolito -->
* **NFR-S02:** Throughput mínimo de 200 req/s (leituras) e 50 req/s (escritas transacionais) por instância da aplicação Java. [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **NFR-S03:** Banco de dados (motor não especificado — ver seção *O que falta verificar*) deve sustentar 500 conexões simultâneas com pool de conexões configurado (HikariCP padrão Spring Boot). [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Observação:** Não há suporte a auto-scaling horizontal, clustering ou sharding na stack atual (monolito Java + JS, sem Kubernetes/Orquestrador identificado). Escalabilidade é vertical (mais vCPU/RAM) ou read-replica para relatórios (BRD#8 Risco "Single point of failure").

## 3. Disponibilidade & Confiabilidade
* **NFR-A01:** Uptime mínimo de 99,5% (SLA contratual) / 99,9% (SLO interno) medido mensalmente, exceto janelas de manutenção programadas (máx. 4h/mês). [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **NFR-A02:** RTO (Recovery Time Objective) ≤ 4 horas para restauração completa (banco + aplicação) a partir de backup diário. <!-- source: BRD#8 (Mitigação: runbook de restore < 4h) -->
* **NFR-A03:** RPO (Recovery Point Objective) ≤ 24 horas (backup diário full + WAL/log shipping se suportado pelo motor de banco). [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **NFR-A04:** Eliminar single points of failure críticos: banco de dados deve ter réplica de leitura (read-replica) para relatórios `custoTotalPorAtivo` e health checks; armazenamento de logs de auditoria (WORM) em storage separado e replicado. <!-- source: BRD#8 (Risco Single point of failure; BR-02 Auditoria) -->

## 4. Segurança
* **NFR-SEC01:** Todos os dados sensíveis (tokens JWT, senhas, dados de ativos com informação sensível LGPD) criptografados em repouso (AES-256 no volume do banco) e em trânsito (TLS 1.2+ obrigatório, preferencialmente 1.3). <!-- source: BRD#7 (Premissa 7: LGPD/SOX); BRD#6 (BR-08 Sessão & Token) -->
* **NFR-SEC02:** Autenticação stateless via JWT (HS256 ou RS256) com expiração ≤ 1h e refresh token rotation; MFA obrigatório para papel `ADMIN` (integração futura com provedor OIDC/AD). <!-- source: BRD#7 (Premissa 5: JWT stateless); BRD#6 (BR-01 RBAC Estrito) -->
* **NFR-SEC03:** Conformidade com OWASP Top 10 (2021) — validação de entrada (BR-03), controle de acesso quebrado (BR-01), logging de falhas de autorização, proteção contra injeção SQL (uso de prepared statements / JPA criteria — stack usa SQL cru). [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **NFR-SEC04:** Rotação de segredos (chave JWT, credenciais de banco, chaves de criptografia) a cada 90 dias via cofre de segredos (ex.: HashiCorp Vault, AWS Secrets Manager — não identificado na stack, requer provisionamento). [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **NFR-SEC05:** Logs de auditoria imutáveis (append-only, storage WORM) para todas as operações de escrita listadas em BR-02 (`criar`, `atualizar`, `deletar`, `aprovar`, `cancelar`, `concluir`, `iniciar`) com `usuario_id`, `timestamp`, `entidade`, `entidade_id`, `acao`, `valores_anteriores`, `valores_novos`. <!-- source: BRD#6 (BR-02 Trilha de Auditoria Obrigatória) -->

## 5. Usabilidade & Acessibilidade
* **NFR-U01:** Conformidade com WCAG 2.1 nível AA em todos os fluxos críticos (login, cadastro de ativo, solicitação de manutenção, dashboard de alertas). [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **NFR-U02:** Suporte a leitores de tela (NVDA, JAWS) nos fluxos críticos; ordem de foco lógica, labels em inputs, ARIA landmarks. [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **NFR-U03:** Interface responsiva funcionando em viewports de 320px (mobile) a 1920px (desktop) sem perda de funcionalidade; componentes Popper.js (tooltips, dropdowns) testados em touch e mouse. <!-- source: Stack: @popperjs/core; BRD#5 (Out-of-Scope: app mobile nativo — apenas web responsiva) -->
* **NFR-U04:** Tempo de percepção de interação (INP) ≤ 200ms para ações principais (abrir modal, salvar formulário, filtrar tabela). [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]

## 6. Observabilidade
* **NFR-O01:** Logs estruturados em JSON (campos: `timestamp`, `level`, `traceId`, `spanId`, `service`, `message`, `context`) emitidos para stdout/arquivo; métricas expostas via Spring Boot Actuator `/actuator/prometheus` (JVM, HTTP, DB pool, cache). [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **NFR-O02:** Health checks em `/actuator/health` (liveness/readiness) verificando conectividade com banco, disco, e serviço de agendamento de health checks de ativos. <!-- source: BRD#5 (In-Scope: updateHealthCheck); BRD#7 (Premissa 6: agendador externo) -->
* **NFR-O03:** Tracing distribuído (OpenTelemetry) propagando `traceparent` em 100% das requisições HTTP de entrada e chamadas JDBC; instrumentação automática via Java agent. [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **NFR-O04:** Alertas configurados (Prometheus Alertmanager / Grafana) para: latência p95 > 500ms por 5min, taxa de erro 5xx > 0,1% por 2min, uso de CPU > 80% por 10min, uso de heap > 85% por 5min, falha no job `checkResourceUsageAlerts`. <!-- source: BRD#4 (Guardrail Metrics); Diagnóstico#AlertNotificationService complexity -->

## 7. Manutenibilidade & Qualidade de Código
* **NFR-M01:** Cobertura mínima de testes automatizados de 80% (linhas) para novo código; 60% para código legado (meta progressiva). Ferramentas: JaCoCo (Java), Vitest/Jest (JS). <!-- source: BRD#8 (Mitigação: testes unidade cobertura ≥ 80%) -->
* **NFR-M02:** Complexidade ciclomática máxima por método/função ≤ 10 (atualmente 5 métodos excedem: `request`=13, `run`=15, `toDTO`=14, `build`=14, `checkResourceUsageAlerts`=17). Refatoração obrigatória antes de Q2/2025. <!-- source: Diagnóstico#Funções com complexidade ciclomática alta -->
* **NFR-M03:** Documentação de API REST mantida via OpenAPI 3.0 (SpringDoc) atualizada a cada release; validação em pipeline CI de breaking changes. [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **NFR-M04:** Zero chamadas `console.*` residuais em produção (`api.js:36 error`, `api.js:99 debug`). Pipeline CI deve falhar se detectar. <!-- source: Diagnóstico#Chamadas console.* residuais -->
* **NFR-M05:** Zero métodos stub com corpo vazio (`Usuario.setUsername:86`). Pipeline CI deve falhar se detectar. <!-- source: Diagnóstico#Funções com corpo vazio (stub) -->

## 8. Compliance & Regulatório
* **NFR-C01:** LGPD — Implementar direito ao esquecimento (exclusão lógica + anonimização de dados pessoais em `Funcionario`, `Usuario`, logs de auditoria) via endpoint administrativo com registro de auditoria. <!-- source: BRD#7 (Premissa 7: LGPD/SOX); BRD#6 (BR-02) -->
* **NFR-C02:** SOX — Retenção de logs de auditoria (BR-02) e relatórios financeiros (`custoTotalPorAtivo`) por mínimo 7 anos em storage WORM com integridade verificada (hash SHA-256 periódico). <!-- source: BRD#7 (Premissa 7); BRD#6 (BR-06) -->
* **NFR-C03:** Classificação de dados: ativos com dados sensíveis (ex.: HD com dados pessoais) marcados com tag `LGPD_SENSITIVE`; acesso restrito a roles `ADMIN` e `AUDITOR`. [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]

## 9. Portabilidade & Compatibilidade
* **NFR-PO01:** Compatibilidade com as 2 últimas versões estáveis dos navegadores principais (Chrome, Edge, Firefox, Safari) em desktop; última versão em mobile (iOS Safari, Chrome Android). [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **NFR-PO02:** Deploy suportado em VM Linux (Ubuntu 22.04 LTS / RHEL 9) ou container Docker (imagem base Eclipse Temurin 17 JRE) com banco de dados relacional gerenciado ou self-hosted (motor a definir — ver *O que falta verificar*). <!-- source: Stack: Java 17+ implícito; BRD#7 (Premissa 1, 4) -->
* **NFR-PO03:** Frontend servido como arquivos estáticos pelo backend (Spring Boot `static` resources) ou CDN (Nginx/CloudFront) — build via Vite/Webpack (não configurado ainda). [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]

## 10. Custo (Cost Efficiency)
* **NFR-CO01:** Custo de infraestrutura por ativo gerenciado ≤ R$ 2,00/mês (meta guardrail BRD#4). Inclui compute, banco, storage, backup, monitoramento, rede. <!-- source: BRD#4 (Guardrail Metrics) -->
* **NFR-CO02:** Otimização de queries SQL (raw SQL / JPA nativo) para evitar full table scans em tabelas grandes (`Ativo`, `Manutencao`, `HealthCheck`); índices compostos alinhados a `ManutencaoSpecification.build` filtros. <!-- source: BRD#6 (BR-09); Stack: SQL cru/sem ORM -->
* **NFR-CO03:** Logs de auditoria (WORM) com política de tiering: hot (30 dias) em SSD, cold (7 anos) em object storage classe Archive/Glacier. [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]

## 11. Rastreabilidade
| ID | Requisito Relacionado (BRD) | Prioridade | Status de Validação |
| :--- | :--- | :--- | :--- |
| NFR-P01 | Guardrail: Latência P95 API ≤ 500ms | Must | Não validado |
| NFR-P04 | In-Scope: Alertas & Monitoramento (`checkResourceUsageAlerts`) | Must | Não validado |
| NFR-S01 | Fase 3 Corporativo: 400+ usuários | Must | Não validado |
| NFR-A02 | Risco: Runbook restore < 4h | Must | Não validado |
| NFR-A04 | Risco: Single point of failure | Must | Não validado |
| NFR-SEC01 | Premissa 7: LGPD/SOX; BR-08 Token | Must | Não validado |
| NFR-SEC02 | BR-01 RBAC Estrito; Premissa 5 JWT | Must | Não validado |
| NFR-SEC05 | BR-02 Trilha de Auditoria Obrigatória | Must | Não validado |
| NFR-M02 | Diagnóstico: Complexidade ciclomática alta (5 métodos) | Must | Não validado |
| NFR-M04 | Diagnóstico: Console.* residual | Should | Não validado |
| NFR-M05 | Diagnóstico: Stub vazio `setUsername` | Should | Não validado |
| NFR-C01 | Premissa 7: LGPD; BR-02 Auditoria | Must | Não validado |
| NFR-C02 | Premissa 7: SOX; BR-06 Custo Total | Must | Não validado |
| NFR-CO01 | Guardrail: Custo infra/ativo ≤ R$ 2,00/mês | Should | Não validado |

---

## O que falta verificar (Gaps de Informação)
1. **Motor de banco de dados:** Não identificado no `package.json` nem em arquivos de configuração (application.yml/properties não escaneados). NFRs de escalabilidade (NFR-S03), disponibilidade (NFR-A03), portabilidade (NFR-PO02) e custo (NFR-CO01) dependem desta definição (PostgreSQL, Oracle, SQL Server, MySQL?). **Ação:** Confirmar com DBA/Infra na Sprint 0.
2. **Estratégia de deploy e infraestrutura alvo:** VM única? Container Docker? Kubernetes? Cloud provider? Isso impacta NFR-A01, NFR-A04, NFR-PO02, NFR-CO01. **Ação:** Definir com DevOps/Infra.
3. **Provedor de identidade / MFA:** Integração com AD/LDAP/OIDC prevista (BRD#7 Dependências externas) mas não implementada. NFR-SEC02 depende desta decisão. **Ação:** Alinhar com Segurança da Informação.
4. **Ferramentas de observabilidade stack:** Prometheus/Grafana? Datadog? New Relic? ELK? NFR-O01, NFR-O03, NFR-O04 exigem escolha. **Ação:** Definir na Sprint 0.
5. **Política de retenção de dados específica por entidade:** BRD menciona 7 anos para SOX, mas LGPD pode exigir prazos diferentes por tipo de dado. NFR-C01, NFR-C02 precisam de matriz de retenção aprovada por Compliance. **Ação:** Workshop com Compliance/Legal.
6. **Orçamento de infraestrutura validado:** Estimativa BRD#9 (R$ 180k/ano) precisa cotação real para validar NFR-CO01. **Ação:** FinOps/Infra prover cotação.