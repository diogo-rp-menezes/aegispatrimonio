# Risk Register — Aegis1

> **Owner:** Product Lead / Engineering Lead (Frontend) · **Última revisão:** 15/01/2025 · **Cadência de revisão:** Semanal

## 1. Matriz de Riscos

| ID | Categoria | Risco | Probabilidade | Impacto | Score (P×I) | Mitigação | Plano de Contingência | Dono | Status | Data Identificação |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| RISK-001 | Técnico | Função `request` em `frontend/src/services/api.js` com complexidade ciclomática 13 (linha 36) dificulta testes, manutenção e aumenta probabilidade de bugs de integração | Alta | Médio | 6 | Refatorar `request` em funções menores: `buildRequestConfig`, `executeWithRetry`, `parseResponse`, `handleTimeout`; adicionar testes unitários para cada parte | Se bug crítico surgir em produção, aplicar hotfix isolado na função afetada e priorizar refatoração na sprint seguinte | Tech Lead Frontend | Aberto | 15/01/2025 |
| RISK-002 | Segurança & Compliance | Três chamadas `console.*` residuais em `frontend/src/services/api.js` (linhas 26, 49, 52) podem vazar dados sensíveis (tokens, payloads de erro) no console do navegador em produção | Baixa | Alto | 3 | Configurar ESLint `no-console: error` no pipeline de CI/CD; remover chamadas antes do go-live; substituir por logger estruturado (ex.: Sentry) com sanitização | Se vazamento ocorrer, rotacionar tokens JWT imediatamente, auditar logs de acesso e comunicar Security Team | Tech Lead Frontend | Aberto | 15/01/2025 |
| RISK-003 | Técnico | Backend (fora deste repositório) não entrega endpoints compatíveis com contratos implícitos em `api.js` a tempo do cronograma | Média | Alto | 6 | Definir contrato OpenAPI 3.0 antecipado com time de Backend; implementar mock server (MSW) no frontend para desenvolvimento paralelo; validar contrato com testes de contrato (Pact) no CI | Ativar modo "mock-only" para demonstrações e treinamentos; priorizar endpoints críticos (ordens, custoTotalPorAtivo) | Product Lead / Backend Lead | Aberto | 15/01/2025 |
| RISK-004 | Segurança & Compliance | `authInterceptor` em `api.js` armazena/gerencia JWT em `localStorage` (presumido) — vulnerável a XSS; expiração de token pode causar logout em massa se refresh falhar | Média | Alto | 6 | Migrar para cookies `httpOnly` + `Secure` + `SameSite=Strict` gerenciados pelo backend; implementar testes automatizados de expiração/refresh (Cypress); adicionar fallback para tela de login limpa sem loop | Se falha de refresh em massa, desabilitar interceptor via feature flag, forçar logout global e comunicar usuários via canal de status | Tech Lead Frontend / Security Team | Aberto | 15/01/2025 |
| RISK-005 | Negócio | Cálculo de `custoTotalPorAtivo` divergente entre frontend (exibição) e backend (regra BR-04) gera decisões erradas de substituição/manutenção preventiva | Média | Médio | 4 | Contrato de API deve definir fórmula explícita (soma de custos de ordens *concluídas* por ativo); teste de contrato (Pact) validando payload de resposta; frontend trata valor como *read-only* | Se divergência detectada, exibir aviso "Valor em validação" e bloquear ações baseadas no custo até correção backend | Product Lead / Backend Lead | Aberto | 15/01/2025 |
| RISK-006 | Operacional | Dependência crítica do time de Backend para validações de regras de negócio (BR-01 a BR-05) — frontend apenas habilita/desabilita botões baseando-se em flags da API (`canApprove`, etc.) | Alta | Médio | 6 | Sessões semanais de alinhamento técnico (30 min); documentar flags esperadas por endpoint no contrato OpenAPI; frontend trata 409/400 via `handleApiError` com mensagens amigáveis | Se backend atrasar validações, frontend implementa validação *optimistic* local (apenas UI) com aviso "Validação final no servidor" | Tech Lead Frontend / Backend Lead | Aberto | 15/01/2025 |
| RISK-007 | Técnico | Ausência de scripts no `package.json` (build, test, lint, dev) impede automação de CI/CD e padronização de ambiente | Média | Médio | 4 | Adicionar scripts padrão: `dev` (Vite/serve), `build` (produção), `lint` (ESLint), `test` (Vitest/Jest), `test:ci` (cobertura); configurar GitHub Actions/GitLab CI | Execução manual temporária via `npx vite build` etc.; documentar passos no README até automação pronta | Tech Lead Frontend | Aberto | 15/01/2025 |
| RISK-008 | Operacional | Bus factor: conhecimento da camada `api.js` (interceptor, retry, error handling) concentrado em 1–2 desenvolvedores | Média | Médio | 4 | Code review obrigatório em `api.js`; documentar arquitetura do service layer no `docs/architecture/api-layer.md`; pair programming nas próximas 2 sprints | Se pessoa-chave sair, onboarding guiado pelo doc + testes existentes; priorizar refatoração (RISK-001) para reduzir complexidade | Engineering Lead | Aberto | 15/01/2025 |
| RISK-009 | Segurança & Compliance | Ausência de Content Security Policy (CSP) e headers de segurança no frontend hospedado estaticamente — risco de injeção de scripts via dependências comprometidas (@popperjs/core) | Baixa | Alto | 3 | Configurar CSP restritivo no CDN/servidor estático (`script-src 'self'`, `connect-src` apenas API backend); habilitar `Subresource Integrity` para `@popperjs/core`; auditoria `npm audit` no CI | Se vulnerabilidade crítica em dependência, aplicar patch via `npm override` ou migrar para alternativa; monitorar CVEs via Dependabot | DevOps / Security Team | Aberto | 15/01/2025 |
| RISK-010 | Negócio | Rollout gradual (piloto → expansão) falha se filial piloto não atingir ≥ 80% das ordens tramitando no sistema em 2 semanas | Média | Médio | 4 | Definir critérios de passagem claros e mensuráveis; acompanhamento diário nas 2 primeiras semanas; suporte dedicado (Slack/Teams) para usuários piloto | Estender piloto por +1 semana com ações corretivas (treinamento extra, ajustes UX); se persistir, reavaliar escopo ou cronograma | Product Lead / Change Manager | Aberto | 15/01/2025 |
| RISK-011 | Técnico | `handleApiError` em `api.js` pode não cobrir todos os códigos de erro HTTP retornados pelo backend (ex.: 409 para deleção com vínculos, 401/403 para auth) | Média | Médio | 4 | Mapear todos os códigos de erro esperados por endpoint no contrato OpenAPI; implementar testes de integração simulando cada erro; `handleApiError` deve ter `default` com log estruturado | Se erro não tratado surgir, exibir mensagem genérica "Erro inesperado — contate suporte" + ID de correlação; registrar incidente para cobertura posterior | Tech Lead Frontend | Aberto | 15/01/2025 |
| RISK-012 | Regulatório | Rastreabilidade documental para conformidade com NR-10/NR-12 depende de backend fornecer auditoria completa (quem aprovou, quando, evidências) — frontend apenas exibe | Baixa | Alto | 3 | Contrato de API deve incluir campos de auditoria (`approvedBy`, `approvedAt`, `evidenceRefs`) nas respostas de ordem; frontend exibe readonly na tela de detalhes | Se backend não entregar auditoria a tempo, manter controle paralelo em planilha assinada até release seguinte; registrar como débito técnico | Compliance / Backend Lead | Aberto | 15/01/2025 |

<!-- source: frontend/src/services/api.js#L26 -->
<!-- source: frontend/src/services/api.js#L36 -->
<!-- source: frontend/src/services/api.js#L49 -->
<!-- source: frontend/src/services/api.js#L52 -->
<!-- source: Business Requirements Document (brd)#section-8 -->
<!-- source: Business Requirements Document (brd)#section-6 -->
<!-- source: Business Requirements Document (brd)#section-7 -->
<!-- source: Business Requirements Document (brd)#section-10 -->

## 2. Critérios de Priorização
* **Probabilidade:** Alta (>60%) · Média (30-60%) · Baixa (<30%)
* **Impacto:** Alto (bloqueia release/dado sensível/perda financeira relevante) · Médio (degrada experiência) · Baixo (cosmético)
* **Score:** Probabilidade × Impacto (1-3 cada eixo) — riscos com score ≥ 6 exigem plano de mitigação ativo

## 3. Riscos por Categoria
### Técnicos
* **RISK-001** — Complexidade ciclomática alta em `request` (api.js:36)
* **RISK-003** — Backend não entrega endpoints compatíveis a tempo
* **RISK-007** — Ausência de scripts de automação no package.json
* **RISK-011** — Cobertura incompleta de códigos de erro em `handleApiError`

### Negócio
* **RISK-005** — Divergência no cálculo de `custoTotalPorAtivo`
* **RISK-006** — Dependência de validações de regras de negócio no backend
* **RISK-010** — Falha no critério de passagem do rollout piloto

### Segurança & Compliance
* **RISK-002** — Console logs residuais expõem dados sensíveis
* **RISK-004** — JWT em localStorage + refresh token frágil
* **RISK-009** — Ausência de CSP e SRI para dependências
* **RISK-012** — Rastreabilidade para NR-10/NR-12 dependente do backend

### Operacionais
* **RISK-008** — Bus factor na camada de serviços (api.js)

## 4. Riscos Aceitos (Accepted Risks)
| ID | Risco | Justificativa da Aceitação | Aprovado por |
| :--- | :--- | :--- | :--- |
| — | Nenhum risco aceito formalmente até esta revisão | Todos os riscos identificados possuem score ≥ 3 e exigem mitigação ou contingência ativa | — |

## 5. Histórico de Materialização
| ID | Data | O que aconteceu | Ação tomada | Lição aprendida |
| :--- | :--- | :--- | :--- | :--- |
| — | — | Nenhum risco materializado até a data de criação deste registro | — | — |