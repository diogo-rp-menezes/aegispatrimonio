# Risk Register — Aegis Patrimônio

> **Owner:** Engineering Lead (Backend) · **Última revisão:** 15/01/2025 · **Cadência de revisão:** Quinzenal

## 1. Matriz de Riscos

| ID | Categoria | Risco | Probabilidade | Impacto | Score (P×I) | Mitigação | Plano de Contingência | Dono | Status | Data Identificação |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| RISK-001 | Técnico | Banco de dados não definido / ausência de ORM nas dependências — projeto usa SQL cru sem migrações versionadas | Alta | Alto | 9 | Definir motor (PostgreSQL recomendado) e estratégia de migração (Flyway/Liquibase) na Sprint 0; adotar JPA/Hibernate ou jOOQ para type-safety | Se decisão atrasar: prototipar com H2 em memória para validar modelo de domínio; documentar dívida técnica para migração posterior | Tech Lead Backend | Aberto | 15/01/2025 |
| RISK-002 | Técnico | Complexidade ciclomática alta em 5 serviços críticos: `AlertNotificationService.checkResourceUsageAlerts` (17), `api.js request` (13), `ManutencaoSpecification.build` (14), `AtivoMapper.toDTO` (14), `RealisticDataSeeder.run` (15) | Alta | Médio | 6 | Refatorar em métodos menores + testes de unidade (cobertura ≥ 80%) antes de Q2; code review obrigatório com foco em complexidade; adicionar lint rule `max-complexity: 10` | Se refatoração não couber no sprint: isolar métodos complexos atrás de interfaces; criar testes de caracterização antes de mexer; monitorar taxa de erro 5xx em `AlertNotificationService` < 0,1% | Tech Lead Backend / Tech Lead Frontend | Aberto | 15/01/2025 |
| RISK-003 | Segurança & Compliance | Ausência de entidade de Auditoria/Log imutável — BR-02 exige trilha para `criar`, `atualizar`, `deletar`, `aprovar`, `cancelar`, `concluir`, `iniciar` mas não existe tabela `audit_log` no domínio | Média | Alto | 6 | Criar tabela `audit_log` (append-only, índice por `entidade_id` + `timestamp`); popular via `@PrePersist`/`@PreUpdate`/`@PreRemove` ou interceptor JDBC; garantir armazenamento WORM para SOX/LGPD | Se implementação atrasar: habilitar log de auditoria no banco (pgaudit / Oracle AUDIT) como medida temporária; documentar gap para auditoria | Tech Lead Backend / Security Lead | Aberto | 15/01/2025 |
| RISK-004 | Técnico | Método `setUsername` em `Usuario.java:86` tem corpo vazio (stub) — pode quebrar `createFuncionarioAndUsuario` / `createUserAndToken` e fluxos de autenticação | Baixa | Médio | 3 | Corrigir implementação; adicionar teste de regressão; validar se afeta criação de usuários e geração de token | Se descoberto em produção: hotfix imediato; rollback para versão anterior se impacto crítico; adicionar teste de integração para fluxo completo de criação de usuário | Backend Developer | Aberto | 15/01/2025 |
| RISK-005 | Operacional | Chamadas `console.error` e `console.debug` residuais em `frontend/src/services/api.js:36,99` — vazam informações sensíveis em produção | Baixa | Baixo | 1 | Remover antes de produção; configurar logger estruturado (pino/winston no backend, console.log apenas em dev com variável de ambiente) | Se esquecido no deploy: feature flag para desabilitar logs de debug em prod; monitorar logs de aplicação por padrões de `console.*` | Frontend Developer | Aberto | 15/01/2025 |
| RISK-006 | Operacional | Single point of failure: monolito Java sem HA, sem read-replica, sem auto-scaling — RTO/RPO não documentados | Média | Alto | 6 | Documentar RTO/RPO; backup diário do banco; runbook de restore < 4h; avaliar read-replica para relatórios (`custoTotalPorAtivo`, health history) | Se instância cair: restaurar do backup mais recente; comunicar SLA de 4h para stakeholders; ativar runbook de disaster recovery | DevOps / Infra Lead | Aberto | 15/01/2025 |
| RISK-007 | Técnico | Escopo de alertas preditivos limitado a thresholds estáticos (disco > 85%, memória > 90%, latência rede > 100ms) — sem ML/anomalia detection | Média | Médio | 4 | Roadmap Q4: avaliar ML simples (isolation forest) sobre `getHealthHistory`; manter thresholds configuráveis por tipo de ativo via config externa | Se thresholds gerarem falsos positivos/negativos: tornar thresholds ajustáveis por filial/tipo de ativo via admin UI; adicionar feedback loop "marcar como falso positivo" | Tech Lead Backend | Aberto | 15/01/2025 |
| RISK-008 | Operacional | Dependência de agendador externo para `updateHealthCheck` — endpoint não idempotente, sem contrato documentado para scheduler (cron, Airflow, Temporal) | Média | Médio | 4 | Implementar `updateHealthCheck` como endpoint idempotente (chave: `ativo_id` + `timestamp_coleta`); documentar contrato OpenAPI para scheduler; adicionar validação de payload duplicado | Se scheduler falhar: endpoint manual via admin UI para re-executar health check; alerta de "health check desatualizado > 24h" no dashboard | Backend Developer | Aberto | 15/01/2025 |
| RISK-009 | Técnico | Frontend sem build/test pipeline visível — 15 arquivos JS sem Vite/Webpack, Vitest/Jest, ESLint, CI configurados no repositório | Média | Médio | 4 | Configurar Vite + Vitest + ESLint + GitHub Actions/GitLab CI na Sprint 0; adicionar stage de build no pipeline; gate de qualidade (cobertura ≥ 70%, zero erros ESLint) | Se pipeline não estiver pronto: build manual documentado; checklist de deploy com validação manual de console errors; priorizar na Sprint 1 | Frontend Developer / DevOps | Aberto | 15/01/2025 |
| RISK-010 | Negócio | Migração de dados legados (planilhas dispersas, ~60% cobertura) para sistema unificado — risco de duplicidade, perda de histórico, mapeamento incorreto de campos | Alta | Alto | 9 | Sprint 0: inventário de fontes, template CSV padronizado, script de importação idempotente com validação (BR-03, BR-04); fase piloto em 1 filial (500 ativos) antes de expansão | Se migração falhar no piloto: rollback para planilhas; estender piloto 2 semanas; contratar especialista em migração de dados patrimoniais | Product Owner / Data Engineer | Aberto | 15/01/2025 |
| RISK-011 | Segurança & Compliance | RBAC não testado em cenários de escala — regras `*_comUser_deveRetornarForbidden` validadas apenas em testes unitários; sem testes de carga/penetração | Média | Alto | 6 | Adicionar testes de integração para matriz de permissões (ADMIN vs USER vs roles customizadas); agendar pentest antes de go-live corporativo (Q4); implementar rate limiting em endpoints sensíveis | Se vulnerabilidade encontrada em produção: WAF rule temporária; hotfix de autorização; rotação de segredos/JWT keys | Security Lead / QA | Aberto | 15/01/2025 |
| RISK-012 | Técnico | `ManutencaoSpecification.build` (complexidade 14) encapsula filtros compostos — risco de quebra de clientes ao adicionar novos filtros (status, filial, departamento, tipo, período, prioridade) | Média | Médio | 4 | Cobrir com testes de contrato (Pact ou Spring Cloud Contract); versionar API de busca; usar padrão Specification extensível sem breaking changes | Se breaking change necessário: manter v1/v2 paralelos por 2 sprints; comunicar consumidores (frontend, relatórios) com antecedência | Backend Developer | Aberto | 15/01/2025 |
| RISK-013 | Operacional | Bus factor: conhecimento concentrado em 3 backend (Java), 2 frontend (JS) — ausência de documentação arquitetural (ADR), runbooks, onboarding guide | Média | Médio | 4 | Criar ADRs para decisões-chave (auth, health check, auditoria, RBAC); runbooks de deploy/restore/health check; guia de onboarding para novos devs (target: < 1 dia para primeiro PR) | Se pessoa-chave sair: pair programming obrigatório nas 2 primeiras semanas; documentação viva no Confluence; knowledge sharing sessions semanais | Engineering Manager | Aberto | 15/01/2025 |
| RISK-014 | Regulatório | LGPD/SOX: logs de auditoria imutáveis (WORM) requeridos mas storage WORM não provisionado; retenção de logs não definida | Média | Alto | 6 | Provisionar storage WORM (S3 Object Lock / Azure Immutable Blob / on-prem WORM) na Sprint 0; definir política de retenção (mínimo 7 anos para SOX); validar com Compliance | Se storage WORM não estiver pronto: gravar logs em tabela append-only com trigger de bloqueio `DELETE/UPDATE`; auditoria manual temporária | DevOps / Compliance Lead | Aberto | 15/01/2025 |
| RISK-015 | Negócio | Cronograma agressivo: piloto Q2/2025 (3 meses) com escopo completo (RBAC, health checks, relatórios, migração, auditoria) — risco de corte de qualidade | Alta | Médio | 6 | Priorizar MVP: RBAC + CRUD Ativos + Auditoria básica + Health Check manual; adiar relatórios avançados e alertas preditivos para Fase 2; definir "Definition of Done" rigorosa | Se atrasar: reduzir escopo do piloto (menos filiais, menos ativos); estender piloto 4 semanas; comunicar stakeholders com antecedência | Product Owner / Engineering Manager | Aberto | 15/01/2025 |

## 2. Critérios de Priorização
* **Probabilidade:** Alta (>60%) · Média (30-60%) · Baixa (<30%)
* **Impacto:** Alto (bloqueia release/dado sensível/perda financeira relevante) · Médio (degrada experiência/atraso não crítico) · Baixo (cosmético/workaround simples)
* **Score:** Probabilidade × Impacto (Alta=3, Média=2, Baixa=1) — riscos com score ≥ 6 exigem plano de mitigação ativo e revisão quinzenal

## 3. Riscos por Categoria
### Técnicos
* RISK-001: Banco de dados não definido / ausência de ORM
* RISK-002: Complexidade ciclomática alta em 5 serviços críticos
* RISK-004: `setUsername` stub vazio em `Usuario.java:86`
* RISK-007: Alertas preditivos limitados a thresholds estáticos
* RISK-008: Dependência de agendador externo para health checks
* RISK-009: Frontend sem build/test pipeline
* RISK-012: `ManutencaoSpecification.build` risco de breaking changes

### Negócio
* RISK-010: Migração de dados legados (planilhas dispersas)
* RISK-015: Cronograma agressivo para piloto Q2/2025

### Segurança & Compliance
* RISK-003: Ausência de entidade de Auditoria/Log imutável
* RISK-011: RBAC não testado em cenários de escala/penetração
* RISK-014: LGPD/SOX - storage WORM não provisionado

### Operacionais
* RISK-005: Console.* residual em `api.js`
* RISK-006: Single point of failure (monolito sem HA)
* RISK-013: Bus factor / ausência de documentação arquitetural

## 4. Riscos Aceitos (Accepted Risks)
| ID | Risco | Justificativa da Aceitação | Aprovado por |
| :--- | :--- | :--- | :--- |
| RISK-005 | Console.* residual em `api.js` | Baixo impacto (apenas logs de debug/error em console do navegador); correção trivial na Sprint 0; não bloqueia funcionalidade | [Pendente — aguardando Tech Lead Frontend] |
| RISK-007 | Alertas preditivos limitados a thresholds estáticos | Fora do escopo do MVP (Q2/Q3); roadmap Q4 prevê avaliação de ML; thresholds configuráveis atendem necessidade imediata de manutenção baseada em condição | [Pendente — aguardando Product Owner] |

## 5. Histórico de Materialização
| ID | Data | O que aconteceu | Ação tomada | Lição aprendida |
| :--- | :--- | :--- | :--- | :--- |
| — | — | Nenhum risco materializado até a data — registro iniciado em 15/01/2025 | — | — |