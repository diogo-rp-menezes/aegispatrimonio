# Aegis1 Frontend — Database README

> **Versão:** 1.0 · **Owner:** Frontend Team · **Status:** Active

## 1. Purpose

Este documento descreve a **ausência intencional de banco de dados** no repositório Aegis1 Frontend. Conforme o *Database Manifest* ([[db-manifest]]), o Aegis1 Frontend é uma **SPA estática servida via CDN** sem camada de persistência própria. Toda responsabilidade de armazenamento, regras de negócio de dados, schema, migrações, segurança em repouso e ciclo de vida de dados reside no **backend externo (API REST/HTTPS)**, fora do escopo deste workspace. Este README existe para satisfazer o catálogo de artefatos do projeto e registrar explicitamente essa fronteira arquitetural. <!-- source: docs/07-database/db-manifest.md#1 -->

## 2. Architecture

| Atributo | Valor |
| :--- | :--- |
| **Modelo** | Nenhum (frontend-only, sem persistência local) |
| **Engine** | N/A — não há motor de banco neste repositório |
| **Tenancy** | N/A — multi-tenancy gerenciado no backend externo |
| **Consistência** | N/A — consistência de dados é responsabilidade do backend |
| **ORM / Query Builder** | Nenhum — varredura determinística confirma apenas `@popperjs/core` como dependência | <!-- source: docs/07-database/db-schema-spec.md#1 --> <!-- source: package.json -->

O frontend consome dados exclusivamente via **HTTP/JSON** através do módulo `frontend/src/services/api.js` (ponto único de integração, ASM-001). <!-- source: docs/07-database/db-domain-model.md#3 (ASM-001) -->

## 3. Domains & Core Entities

**Nenhuma entidade persistida é declarada neste repositório.** O *Database Domain & Entity Model* ([[db-domain-model]]) define o domínio `aegis1-frontend` com `entities: []` e descrição explícita: *"NÃO possui banco de dados próprio"*. <!-- source: docs/07-database/db-domain-model.md#2 -->

Todas as entidades de negócio (usuários, ordens, checklists, evidências, etc.) existem **apenas no backend externo**. O frontend manipula DTOs transitórios em memória durante a edição de formulários e exibe dados recebidos via API.

## 4. Source of Truth

| Aspecto | Definição |
| :--- | :--- |
| **Tipo** | Nenhum (não há schema, migrations, models ou seeds neste repositório) |
| **Localização** | N/A — a fonte da verdade do schema físico reside na documentação do backend externo |
| **Artefatos locais** | Apenas `docs/07-database/db-manifest.md` e `docs/07-database/db-readme.md` (este documento) | <!-- source: docs/07-database/db-manifest.md#1 -->

## 5. Migration Strategy

**Não se aplica.** Conforme o *Migration Specification & Plan* ([[db-migration-spec]], MIG-20250115-001):

- Não há objetos de banco para migrar (tabelas, índices, constraints, functions, triggers)
- Não há dados persistidos localmente para transformar
- A estratégia de deployment é `"none"` e rollback é `"Não aplicável"`
- Qualquer mudança de schema físico deve ser realizada no repositório do **backend externo** <!-- source: docs/07-database/db-migration-spec.md#1 -->

## 6. Environments

| Environment | Purpose |
| :--- | :--- |
| **Development** | Desenvolvimento local da SPA (mock API opcional via MSW/jest) |
| **Test** | Testes automatizados da SPA com mocks de API (MSW) |
| **Staging** | Pré-produção da SPA contra backend de staging |
| **Production** | Produção da SPA contra backend de produção | <!-- source: docs/07-database/db-manifest.md#1 -->

> **Nota:** Nenhum ambiente provisiona banco de dados local. A configuração de banco (se houver) é responsabilidade do backend em cada ambiente.

## 7. Security

Conforme o *Database Security & Data Lifecycle* ([[db-security-lifecycle]]):

| Domínio | Responsabilidade | Observação |
| :--- | :--- | :--- |
| **Roles & Permissions (GRANT/REVOKE)** | Backend Team | Frontend apenas reflete permissões recebidas na UI (botões condicionais) |
| **Row-Level Security** | Backend Team | Ausência de tabelas no frontend implica ausência de RLS local |
| **Sensitive Data Classification** | Backend Team / Security / Legal | Frontend não persiste dados sensíveis em repouso; JWT migra para cookie `HttpOnly; Secure; SameSite=Strict` (NFR-SEC02) |
| **Data Lifecycle (retenção, arquivamento, exclusão)** | Backend Team | Frontend atua apenas como coletor/exibidor transitório |
| **Seeds / Test Data** | Backend Team / QA | Testes de frontend usam mocks em memória (MSW) |
| **Client-side Upload Validation** | Frontend Team | Tipo MIME, tamanho, sanitização de nome — *Security Policies* §3 |
| **Output Sanitization / XSS Prevention** | Frontend Team | DOMPurify, `textContent`, CSP — *Security Policies* §8 |
| **Secrets Management** | Platform / DevOps | Chaves JWT, DSN telemetria, credenciais CDN — *Security Policies* §7 |
| **Audit Logging / Telemetry** | Frontend + Backend | Propagação de `trace-id`, sem vazamento de PII/tokens — *Security Policies* §10 | <!-- source: docs/07-database/db-security-lifecycle.md#6 -->

**Fronteira crítica:** O frontend **não implementa** criptografia em repouso, mascaramento, tokenização, anonimização ou controles de acesso a nível de banco — todos são responsabilidade do backend externo.

## 8. Backup, Recovery & Observability

| Capacidade | Status | Responsável |
| :--- | :--- | :--- |
| **Backup de schema/dados** | Não aplicável (sem banco local) | Backend Team |
| **Point-in-time Recovery** | Não aplicável | Backend Team |
| **Replica / Standby** | Não aplicável | Backend Team |
| **Métricas de performance de queries** | Não aplicável | Backend Team |
| **Observabilidade de conexão/pool** | Não aplicável | Backend Team |
| **Frontend Telemetry** | `trace-id` propagado em headers HTTP; logs estruturados sem PII/tokens | Frontend Team | <!-- source: docs/06-security/security-policies.md#10 -->

## 9. Change Management

Como não há schema de banco neste repositório, **não existe processo local de mudança de schema**. O fluxo para alterações que impactam o contrato de dados é:

1. **Backend Team** propõe mudança no schema físico (via seus próprios artefatos: `db-schema-spec.md`, `db-migration-spec.md`, etc.)
2. **Backend Team** implementa e migra o banco em ambientes controlados (dev → staging → prod)
3. **Frontend Team** é notificado da mudança no contrato da API (breaking vs. non-breaking)
4. **Frontend Team** adapta DTOs, validações client-side e UI conforme necessário em `frontend/src/services/api.js` e componentes
5. **Deploy coordenado** (se breaking change) ou deploy independente (se backward-compatible)

> **Referência:** *Migration Specification & Plan* ([[db-migration-spec]]) registra que a compatibilidade do contrato de API é verificada no passo "Application Compatibility" do plano operacional. <!-- source: docs/07-database/db-migration-spec.md#2 -->

## 10. Directory Structure

```text
docs/
└── 07-database/
    ├── db-manifest.md           # Manifesto Database as Code (este repositório não tem banco)
    ├── db-domain-model.md       # Modelo de domínio (entities: [])
    ├── db-schema-spec.md        # Especificação de schema (intencionalmente vazia)
    ├── db-security-lifecycle.md # Segurança & ciclo de vida (fronteira de responsabilidade)
    ├── db-migration-spec.md     # Spec de migração (Not Applicable)
    └── db-readme.md             # Este arquivo
```

> **Não existe** diretório `database/` na raiz do workspace, nem subpastas `model/`, `schema/`, `migrations/`, `seeds/` ou `tests/` relacionadas a banco de dados. A varredura determinística confirma: 15 arquivos `.js` apenas em `frontend/`. <!-- source: Diagnóstico determinístico do codebase -->

## 11. Limitações Atuais do Sistema (importante)

Este conjunto de artefatos cobre a camada de **contrato e documentação versionada** (Database as Code). Os itens abaixo, sugeridos na análise original, dependem de um **motor determinístico** — código de introspecção, parsing SQL, execução em banco efêmero e comparação de estado — que **não existe ainda** neste projeto e não é gerado por templates:

| Capacidade | Status |
| :--- | :--- |
| Introspecção de banco existente (brownfield) | Não implementado |
| Schema Diff / Drift Report contra banco vivo | Não implementado |
| Execução de migration em banco efêmero + verificação automática | Não implementado |
| Compatibility Matrix automática app↔schema | Não implementado |
| Changelog e Validation Report gerados automaticamente | Não implementado |

Se quiser, esses pontos podem virar uma frente de trabalho separada (conectores de banco + engine de validação), fora do escopo do sistema de templates/artefatos atual.

---

## 12. Referências Cruzadas (Rastreabilidade)

| Artefato | Seção Relevante | Descrição |
| :--- | :--- | :--- |
| `docs/07-database/db-manifest.md` | #1 | Declaração formal de ausência de banco |
| `docs/07-database/db-domain-model.md` | #1, #2, #3 | Domínio `aegis1-frontend` com `entities: []`; ASM-001 (api.js); ASM-003 (zero deps de banco) |
| `docs/07-database/db-schema-spec.md` | #1–#8 | Contrato físico vazio intencional; evidências de rastreabilidade |
| `docs/07-database/db-security-lifecycle.md` | #1–#8 | Fronteira de responsabilidade; matriz de domínios; ações requeridas no backend |
| `docs/07-database/db-migration-spec.md` | #1–#5 | MIG-20250115-001 (Not Applicable); plano operacional; risk assessment |
| `docs/06-security/security-policies.md` | #3, #5, #6, #7, #8, #10, #11, #12 | Validação uploads, classificação dados, RBAC, secrets, XSS, telemetria, incident response, compliance |
| `package.json` | — | Dependência única: `@popperjs/core` (sem banco/ORM) |
| `frontend/src/services/api.js` | — | Ponto único de integração HTTP (ASM-001) |

---

> **Conclusão:** Este README documenta a **decisão arquitetural deliberada** de manter o Aegis1 Frontend como uma SPA sem persistência local. Qualquer necessidade relacionada a schema físico, migrações, segurança de dados em repouso, backup/recovery, ou conformidade regulatória de armazenamento deve ser endereçada na documentação e no codebase do **backend externo**.