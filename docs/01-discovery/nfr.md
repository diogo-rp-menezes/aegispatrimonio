# Non-Functional Requirements (NFR) — Aegis1

> **Versão:** 1.0 · **Status:** Draft · **Owner:** Engenharia Frontend / Arquitetura

## 1. Performance
* **NFR-P01:** Latência P95 da chamada `request` (frontend → backend) ≤ 800 ms em condições normais de rede. <!-- source: BRD#4 -->
* **NFR-P02:** Time to Interactive (TTI) da aplicação ≤ 3,5 s em conexão 4G (perfil mobile) e ≤ 2 s em broadband desktop. <!-- source: BRD#4 -->
* **NFR-P03:** Tamanho total do bundle JavaScript (gzipped) ≤ 150 kB para carregamento inicial (code-splitting por rota obrigatório). <!-- source: Diagnóstico: 15 arquivos JS, 627 LOC total -->
* **NFR-P04:** Taxa de erro de integração API (`handleApiError`) < 1% das requisições totais (medido em janela deslizante de 5 min). <!-- source: BRD#4 -->
* **Método de medição:** k6 para load testing da camada de API (mock backend), Lighthouse CI no pipeline para TTI/bundle, APM (ex.: Sentry/Datadog RUM) em produção para latência real e taxa de erro. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`

## 2. Escalabilidade
* **NFR-S01:** Frontend estático servido via CDN deve suportar ≥ 500 usuários concorrentes ativos sem degradação de latência de assets > 10%. <!-- source: BRD#3 personas: Gestor, Técnico, Aprovador, Admin -->
* **NFR-S02:** Backend (fora deste repositório) deve escalar horizontalmente para absorver picos de 50 req/s nas mutações de ordens (iniciar, aprovar, concluir, cancelar). `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` <!-- source: BRD#5 In-Scope: ciclo de vida ordens -->
* **NFR-S03:** Nenhum estado de sessão armazenado no frontend além do token JWT em `localStorage`/`sessionStorage`; renovação via `authInterceptor` não deve criar gargalo de refresh simultâneo. <!-- source: BRD#6 BR-06, api.js:authInterceptor -->

## 3. Disponibilidade & Confiabilidade
* **NFR-A01:** Disponibilidade do frontend (CDN + DNS) ≥ 99,9% mensal (SLO interno); SLA contratual com provedor de hospedagem estática ≥ 99,5%. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`
* **NFR-A02:** RTO (Recovery Time Objective) para falha de deploy do frontend ≤ 15 min (rollback de versão anterior via CDN). `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`
* **NFR-A03:** RPO (Recovery Point Objective) = 0 para dados de frontend (assets imutáveis versionados); dados de negócio são responsabilidade do backend. <!-- source: BRD#7 Dependências: Backend -->
* **NFR-A04:** Falha na API de backend não deve travar a UI; `handleApiError` deve exibir mensagem amigável e permitir retry manual. <!-- source: Diagnóstico: api.js:26 console.error, api.js:49/52 console.log -->

## 4. Segurança
* **NFR-SEC01:** Todas as comunicações frontend ↔ backend exclusivamente via HTTPS (TLS 1.2+); HSTS habilitado no CDN. <!-- source: BRD#6 BR-06, BRD#7 Dependências: Infra/DevOps -->
* **NFR-SEC02:** Token JWT armazenado em `localStorage` apenas como medida transitória; migração para cookie `HttpOnly; Secure; SameSite=Strict` planejada antes do go-live. <!-- source: BRD#6 BR-06, BRD#7 Dependências: Segurança da Informação -->
* **NFR-SEC03:** `authInterceptor` deve implementar refresh automático único (máximo 1 retry) e, em falha, limpar armazenamento e redirecionar para login sem expor stack trace. <!-- source: BRD#6 BR-06, Diagnóstico: api.js -->
* **NFR-SEC04:** Nenhum dado sensível (token, IDs de ordens, custos) logado no console em build de produção; pipeline deve falhar se `console.*` existir no bundle. <!-- source: Diagnóstico: 3 chamadas console.* residuais em api.js:26,49,52 -->
* **NFR-SEC05:** Conformidade com OWASP Top 10 no frontend: CSP restritivo, sanitização de entradas em formulários de cadastro (departamento, filial, fornecedor, funcionário), proteção contra XSS/CSRF via headers e SameSite. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`

## 5. Usabilidade & Acessibilidade
* **NFR-U01:** Conformidade WCAG 2.1 AA nos fluxos críticos (criar ordem, iniciar, aprovar, concluir, cancelar, listar, custoTotalPorAtivo). `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` <!-- source: BRD#3 Personas: Técnico de Campo (mobile), Gestor (desktop) -->
* **NFR-U02:** Interface responsiva funcional em viewport 320 px–1920 px; touch targets ≥ 48×48 px para ações de técnico em campo. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`
* **NFR-U03:** Suporte a leitores de tela (ARIA labels, roles, live regions) nos componentes de estado da ordem (badge status, botões de ação condicionais). `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`

## 6. Observabilidade
* **NFR-O01:** Logs estruturados (JSON) no frontend apenas para erros não tratados (`window.onerror`, `unhandledrejection`) enviados a endpoint de telemetria (ex.: Sentry); zero `console.*` em produção. <!-- source: Diagnóstico: 3 chamadas console.* residuais -->
* **NFR-O02:** Métricas de Core Web Vitals (LCP, FID, CLS) coletadas via RUM e expostas em dashboard; alerta se LCP P75 > 2,5 s. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`
* **NFR-O03:** Tracing de requisições `request` (duração, status, retry) via header `trace-id` propagado ao backend; 100% das mutações cobertas. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` <!-- source: BRD#4 Guardrail: Latência P95 < 800 ms -->
* **NFR-O04:** Alertas configurados para: taxa de erro API > 1% (5 min), latência P95 > 800 ms (5 min), falha de refresh token > 5% (15 min). <!-- source: BRD#4 Guardrail Metrics -->

## 7. Manutenibilidade & Qualidade de Código
* **NFR-M01:** Complexidade ciclomática máxima por função ≤ 10; refatorar `request` (atual 13) em funções menores (retry, timeout, parsing, auth). <!-- source: Diagnóstico: api.js:36 complexidade 13 -->
* **NFR-M02:** Cobertura mínima de testes unitários/integração (Jest + React Testing Library ou equivalente) ≥ 80% nas funções de `api.js` e componentes de fluxo de ordem. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`
* **NFR-M03:** ESLint + Prettier no CI; regra `no-console` como error para builds de produção; `no-unused-vars` error. <!-- source: Diagnóstico: console.* residuais -->
* **NFR-M04:** Contrato de API (OpenAPI 3.0) versionado e validado no CI via testes de contrato (Pact ou similar) entre frontend e backend. <!-- source: BRD#8 Risco: cálculo custoTotalPorAtivo divergente -->

## 8. Compliance & Regulatório
* **NFR-C01:** LGPD — direito ao esquecimento: frontend deve permitir solicitação de exclusão de dados pessoais do usuário logado (token, preferências) via chamada a endpoint backend; não armazenar PII desnecessária localmente. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` <!-- source: BRD#7 Dependências: Segurança da Informação -->
* **NFR-C02:** Retenção de logs de erro frontend (telemetria) ≤ 30 dias; anonimização de IDs de ordem/usuário após 7 dias. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`
* **NFR-C03:** Normas de manutenção (NR-10, NR-12) — rastreabilidade documental garantida pelo backend; frontend apenas exibe e coleta evidências (checklist/foto) no fluxo **aprovar**. <!-- source: BRD#2 Evidências, BRD#6 BR-02 -->

## 9. Portabilidade & Compatibilidade
* **NFR-PO01:** Compatibilidade com as 2 últimas versões estáveis de Chrome, Edge, Firefox, Safari (desktop e mobile) — alinhado ao BRD#7 Premissa 4. <!-- source: BRD#7 Premissas -->
* **NFR-PO02:** Build independente de provedor de nuvem; assets estáticos (HTML/JS/CSS) deployáveis em qualquer CDN/Object Storage (AWS S3+CloudFront, Azure Static Web Apps, Netlify, Vercel). `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`

## 10. Custo (Cost Efficiency)
* **NFR-CO01:** Custo de hospedagem estática (CDN + storage) ≤ USD 30/mês para volume estimado (500 usuários, 10k pageviews/dia). `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` <!-- source: BRD#9 Financial Considerations -->
* **NFR-CO02:** Zero custo de licença de runtime/framework (stack vanilla JS + @popperjs/core MIT). <!-- source: Stack real: @popperjs/core apenas -->

## 11. Rastreabilidade
| ID | Requisito Relacionado (BRD) | Prioridade | Status de Validação |
| :--- | :--- | :--- | :--- |
| NFR-P01 | BRD#4 Guardrail: Latência P95 < 800 ms | Must | Não validado |
| NFR-P02 | BRD#4 KPI: Eliminar console logs | Must | Não validado |
| NFR-P03 | BRD#5 In-Scope: Interface única navegador | Should | Não validado |
| NFR-P04 | BRD#4 Guardrail: Taxa erro API < 1% | Must | Não validado |
| NFR-S01 | BRD#3 Personas: 4 perfis concorrentes | Should | Não validado |
| NFR-S02 | BRD#5 In-Scope: Mutações ordens | Must | Não validado |
| NFR-S03 | BRD#6 BR-06: authInterceptor | Must | Não validado |
| NFR-A01 | BRD#7 Dependências: Infra/DevOps | Should | Não validado |
| NFR-A02 | BRD#7 Dependências: Infra/DevOps | Should | Não validado |
| NFR-A03 | BRD#7 Dependências: Backend | Must | Não validado |
| NFR-A04 | BRD#6 BR-05: handleApiError 409 | Must | Não validado |
| NFR-SEC01 | BRD#7 Dependências: Infra/DevOps (TLS) | Must | Não validado |
| NFR-SEC02 | BRD#6 BR-06, BRD#7 Segurança Info | Must | Não validado |
| NFR-SEC03 | BRD#6 BR-06: refresh automático | Must | Não validado |
| NFR-SEC04 | Diagnóstico: console.* residuais | Must | Não validado |
| NFR-SEC05 | BRD#8 Risco: vazamento dados sensíveis | Should | Não validado |
| NFR-U01 | BRD#3 Personas: Técnico mobile | Should | Não validado |
| NFR-U02 | BRD#3 Personas: Técnico mobile | Should | Não validado |
| NFR-U03 | BRD#3 Personas: Acessibilidade geral | Should | Não validado |
| NFR-O01 | Diagnóstico: console.* residuais | Must | Não validado |
| NFR-O02 | BRD#4 North Star / Guardrails | Should | Não validado |
| NFR-O03 | BRD#4 Guardrail: Latência P95 | Must | Não validado |
| NFR-O04 | BRD#4 Guardrail Metrics | Must | Não validado |
| NFR-M01 | Diagnóstico: api.js:36 complexidade 13 | Must | Não validado |
| NFR-M02 | BRD#8 Risco: bugs integração | Should | Não validado |
| NFR-M03 | Diagnóstico: console.* / qualidade | Must | Não validado |
| NFR-M04 | BRD#8 Risco: custoTotalPorAtivo divergente | Must | Não validado |
| NFR-C01 | BRD#7 Dependências: Segurança Info | Should | Não validado |
| NFR-C02 | BRD#7 Dependências: Segurança Info | Should | Não validado |
| NFR-C03 | BRD#2 Evidências: NR-10/12 | Must | Não validado |
| NFR-PO01 | BRD#7 Premissa 4: Navegadores alvo | Must | Não validado |
| NFR-PO02 | BRD#7 Dependências: Infra/DevOps | Should | Não validado |
| NFR-CO01 | BRD#9 Financial Considerations | Should | Não validado |
| NFR-CO02 | Stack real: @popperjs/core MIT | Must | Não validado |