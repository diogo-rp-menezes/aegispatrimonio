# Risk Register — Aegis Patrimônio

> **Owner:** Tech Lead / Product Owner · **Última revisão:** 15/01/2025 · **Cadência de revisão:** Quinzenal

## 1. Matriz de Riscos

| ID | Categoria | Risco | Probabilidade | Impacto | Score (P×I) | Mitigação | Plano de Contingência | Dono | Status | Data Identificação |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| RISK-001 | Técnico | Complexidade ciclomática alta em `AlertNotificationService.checkResourceUsageAlerts` (17) — dificulta testes, manutenção e evolução de alertas preditivos | Alta | Alto | 9 | Refatorar em métodos menores (< 10 linhas cada); adicionar testes de unidade com cobertura ≥ 90 %; SonarGate quality gate | Se bug crítico em produção: hotfix isolado no método `checkResourceUsageAlerts` + rollback de deploy anterior | Tech Lead Backend | Aberto | 15/01/2025 |
| RISK-002 | Técnico | Complexidade ciclomática alta em `ManutencaoSpecification.build` (14) — consultas compostas frágeis a mudanças de filtro/paginação | Alta | Médio | 6 | Extrair builders de critério por domínio (filial, status, data); testar com Property-Based Testing | Desativar filtros compostos temporariamente; fallback para query simples paginada | Tech Lead Backend | Aberto | 15/01/2025 |
| RISK-003 | Técnico | Complexidade ciclomática alta em `AtivoMapper.toDTO` (14) — mapeamento bidirecional propenso a regressões de serialização | Alta | Médio | 6 | Substituir por MapStruct com validação de nulos; testes de contrato `toEntity_deveRetornarNullParaDTONulo` e `toEntity_deveMapearDTOparaEntidade` | Serialização manual via DTOs simples em endpoint crítico; remover mapper temporariamente | Tech Lead Backend | Aberto | 15/01/2025 |
| RISK-004 | Técnico | Complexidade ciclomática alta em `RealisticDataSeeder.run` (15) — seed de dados realistas acoplado a lógica de negócio | Média | Baixo | 3 | Mover para scripts Flyway/Liquibase separados; usar factories de teste (Instancio/DataFaker) | Desabilitar seeder em produção; executar apenas em ambiente de homologação | Tech Lead Backend | Aberto | 15/01/2025 |
| RISK-005 | Técnico | Complexidade ciclomática alta em `frontend/src/services/api.js:request` (13) — interceptor HTTP centralizado com múltiplas responsabilidades | Alta | Médio | 6 | Separar: (1) auth interceptor, (2) error handler, (3) retry/backoff, (4) logging; ESLint `max-lines-per-function` | Desativar retry/logging em produção via feature flag; manter apenas auth + error handling | Tech Lead Frontend | Aberto | 15/01/2025 |
| RISK-006 | Segurança | Método `setUsername` em `Usuario.java:86` com corpo vazio (stub) — possível brecha de atualização de credencial sem validação | Média | Alto | 6 | Implementar validação de unicidade, formato e auditoria (`preUpdate`/`onUpdate`); teste de contrato cobrindo username | Bloquear endpoint de atualização de usuário via WAF rule até correção; auditoria manual de contas | Tech Lead Backend / Security | Aberto | 15/01/2025 |
| RISK-007 | Segurança | Chamadas `console.error` (`api.js:44`) e `console.debug` (`api.js:107`) residuais — vazamento de stack traces e dados sensíveis em produção | Baixa | Médio | 2 | Remover antes de homologação; regra ESLint `no-console` em CI; substituir por logger estruturado (pino/winston) | Hotfix de remoção imediata se detectado em produção; rotação de segredos expostos | Tech Lead Frontend | Aberto | 15/01/2025 |
| RISK-008 | Técnico | Ausência de ORM/migrações versionadas (nenhum driver/ORM detectado no `package.json`) — drift de schema e falhas silenciosas | Alta | Alto | 9 | Adotar Flyway/Liquibase imediatamente; CI gate para migrações reversíveis; baseline de schema atual | Rollback manual via backup de banco; script de correção de drift executado por DBA | Tech Lead Backend / DBA | Aberto | 15/01/2025 |
| RISK-009 | Operacional | Single point of failure (monolito sem réplicas) — indisponibilidade total em deploy ou falha de JVM | Média | Alto | 6 | Health endpoint (`/actuator/health`); blue-green deploy com Docker Compose; runbook de restart < 5 min | Failover manual para VM standby com banco replicado; SLA de recuperação 30 min | DevOps / Infra | Aberto | 15/01/2025 |
| RISK-010 | Técnico | Falta de paginação obrigatória em `listarTodos` / `findAll` — risco de OOM em bases > 10k ativos | Média | Médio | 4 | Impor `Pageable` em todos os repositórios Spring Data; default page size 50, max 200; teste de carga com 50k registros | Patch emergencial adicionando `LIMIT 200` nativo via `@Query`; monitoramento de heap via Micrometer | Tech Lead Backend | Aberto | 15/01/2025 |
| RISK-011 | Segurança & Compliance | Segregação de duties incompleta — glossário não mostra regra "aprovador ≠ solicitante ≠ executor" | Média | Alto | 6 | Implementar validação `hasPermission` contextual por fluxo; auditoria de conflitos de papel em `Role`/`Permission` | Controle compensatório: aprovação manual via e-mail com assinatura digital; log de exceção | Tech Lead Backend / Compliance | Aberto | 15/01/2025 |
| RISK-012 | Negócio | Dependência de banco de dados relacional externo não versionado (A1 no BRD) — bloqueia todos CRUDs e health checks | Alta | Alto | 9 | Confirmar com DBA/Infra: engine, versão, connection pool, migrações; provisionar instância dedicada homologação | Ambiente de desenvolvimento com H2/Testcontainers; mock de repositórios para frontend | DBA / Infra | Aberto | 15/01/2025 |
| RISK-013 | Operacional | Dependência de pessoa-chave (bus factor) — conhecimento concentrado em poucos devs sobre regras `ManutencaoSpecification`, `AlertNotificationService` | Média | Alto | 6 | Documentação técnica viva (ADRs); pair programming obrigatório; rotação de code ownership a cada sprint | Runbook detalhado para cada serviço crítico; onboarding guiado para novo dev | Tech Lead / PM | Aberto | 15/01/2025 |
| RISK-014 | Técnico | TODO de performance em `AtivoService.java:119` — carrega até 1000 candidatos (id+nome) e faz ranking em memória | Média | Médio | 4 | Mover ranking para query nativa com window function; paginação server-side; cache Redis para consultas frequentes | Limitar a 200 candidatos via `LIMIT`; desabilitar ranking se latência > 500 ms | Tech Lead Backend | Aberto | 15/01/2025 |
| RISK-015 | Regulatório | Logs de auditoria imutáveis insuficientes — `preUpdate`/`onUpdate` cobrem apenas timestamps, falta log de ação/antes/depois (A5 no BRD) | Média | Alto | 6 | Tabela de auditoria dedicada (ação, entidade, antes, depois, usuário, timestamp) ou CDC (Debezium) | Exportação manual de logs de aplicação + banco para auditoria; assinatura digital de trilha | Compliance / Tech Lead Backend | Aberto | 15/01/2025 |
| RISK-016 | Negócio | Integração AD/LDAP para sincronia de `Usuario`/`Funcionario` não confirmada — cadastro manual propenso a erros | Média | Médio | 4 | Definir contrato SCIM/LDAP com Identity Team; sincronismo agendado + webhook de mudanças | Processo manual com checklist de validação; script de reconciliação semanal | Identity Team / Tech Lead Backend | Aberto | 15/01/2025 |
| RISK-017 | Operacional | Certificado TLS + domínio corporativo não provisionados — impede homologação/produção | Baixa | Alto | 3 | Solicitar com 30 dias de antecedência; usar Let's Encrypt staging para homologação | Homologação em HTTP com aviso de segurança; deploy em subdomínio temporário | SecOps / NetOps | Aberto | 15/01/2025 |
| RISK-018 | Técnico | Autenticação JWT stateless sem refresh token observado — expiração curta causa UX ruim; longa aumenta risco de token roubado | Média | Médio | 4 | Definir política: access token 15 min + refresh token 7 dias com rotação; armazenamento httpOnly cookie | Fallback para sessão server-side (Redis) se refresh token falhar; logout forçado em mudança de senha | Tech Lead Backend / Security | Aberto | 15/01/2025 |
| RISK-019 | Negócio | Mudança de prioridade estratégica no meio do projeto (ex: diretoria solicita módulo f... | Média | Alto | 6 | Alinhamento quinzenal com PO; backlog priorizado com MoSCoW; buffer de 20% capacidade para mudanças não planejadas | Replanejamento de sprint com trade-offs explícitos; comunicação proativa a stakeholders | Product Owner / Tech Lead | Aberto | 15/01/2025 |

## 2. Resumo por Categoria

| Categoria | Qtd Riscos | Score Médio | Riscos Críticos (Score ≥ 9) |
| :--- | :---: | :---: | :---: |
| Técnico | 7 | 5.9 | RISK-001, RISK-008 |
| Segurança | 3 | 4.7 | — |
| Operacional | 3 | 5.0 | — |
| Negócio | 4 | 5.8 | RISK-012 |
| Regulatório | 1 | 6.0 | — |
| Segurança & Compliance | 1 | 6.0 | — |

## 3. Top 5 Riscos por Score

1. **RISK-001** (9) — Complexidade `AlertNotificationService` — Refatoração urgente + testes
2. **RISK-008** (9) — Ausência Flyway/Liquibase — Adoção imediata + CI gate
3. **RISK-012** (9) — Dependência DB externo não versionado — Confirmação DBA/Infra urgente
4. **RISK-002** (6) — Complexidade `ManutencaoSpecification` — Extrair builders + PBT
5. **RISK-003** (6) — Complexidade `AtivoMapper` — Migrar para MapStruct + testes contrato

## 4. Ações Imediatas (Próximas 2 Semanas)

| Ação | Risco Relacionado | Responsável | Prazo |
| :--- | :--- | :--- | :--- |
| Adotar Flyway/Liquibase + baseline schema | RISK-008 | Tech Lead Backend / DBA | 29/01/2025 |
| Refatorar `AlertNotificationService.checkResourceUsageAlerts` | RISK-001 | Tech Lead Backend | 29/01/2025 |
| Confirmar engine/versão/pool DB com DBA/Infra | RISK-012 | DBA / Infra | 22/01/2025 |
| Implementar validação `hasPermission` contextual | RISK-011 | Tech Lead Backend / Compliance | 29/01/2025 |
| Remover `console.error`/`console.debug` residuais | RISK-007 | Tech Lead Frontend | 22/01/2025 |

## 5. Métricas de Acompanhamento

- **Total de riscos abertos:** 19
- **Riscos com Score ≥ 9:** 3 (16%)
- **Riscos com Score 6-8:** 9 (47%)
- **Riscos com Score ≤ 5:** 7 (37%)
- **Média de idade dos riscos abertos:** 0 dias (todos identificados em 15/01/2025)
- **Próxima revisão:** 29/01/2025 (quinzenal)