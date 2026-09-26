# Database Security & Data Lifecycle — Aegis1 Frontend

> **Versão:** 1.0 · **Owner:** Frontend Team · **Status:** Active  
> **Nota importante:** Este repositório **não possui banco de dados próprio**. Conforme o *Database Schema Specification* ([[db-schema-spec]]) e o *Database Domain & Entity Model* ([[db-domain-model]]), o Aegis1 Frontend é uma SPA desacoplada servida via CDN; toda responsabilidade de persistência, segurança de dados em repouso, controle de acesso a nível de banco e ciclo de vida de dados reside no **backend externo (API REST/HTTPS)**, fora do escopo deste workspace. Este documento existe para satisfazer o catálogo de artefatos do projeto e registrar explicitamente essa fronteira de responsabilidade.

---

## 1. Roles & Permissions

```yaml
roles: []
```

> **Justificativa:** Não há banco de dados neste repositório, portanto não existem roles, usuários de banco ou permissões de objeto (GRANT/REVOKE) definidos aqui. O controle de acesso é realizado **exclusivamente no backend externo** via RBAC delegado (conforme *Security Policies* §6). O frontend apenas reflete permissões recebidas na UI (ex.: botões condicionais). <!-- source: docs/07-database/db-schema-spec.md#1 --> <!-- source: docs/07-database/db-domain-model.md#2 --> <!-- source: docs/06-security/security-policies.md#6 -->

---

## 2. Row-Level Security

```yaml
row_level_security: []
```

> **Justificativa:** Ausência de tabelas e entidades persistidas no frontend implica ausência de políticas RLS. A segregação de dados por tenant/usuário/papel é implementada no backend externo. <!-- source: docs/07-database/db-schema-spec.md#2 -->

---

## 3. Sensitive Data

```yaml
sensitive_data: []

secrets:
  prohibited_in_schema: true
```

> **Justificativa:** Nenhum campo de tabela existe neste repositório. A classificação e proteção de dados sensíveis (JWT, PII, evidências, dados de ordens) são descritas no *Security Policies* §5 e aplicadas no backend externo. O frontend **não persiste dados sensíveis em repouso** — o token JWT migra de `localStorage` para cookie `HttpOnly; Secure; SameSite=Strict` (NFR-SEC02 planejado), eliminando armazenamento local de credenciais. <!-- source: docs/07-database/db-schema-spec.md#1 --> <!-- source: docs/06-security/security-policies.md#5 -->

---

## 4. Data Lifecycle por Entidade

```yaml
data_lifecycle: []
```

> **Justificativa:** Não há entidades persistidas neste workspace. O ciclo de vida dos dados (criação, retenção, arquivamento, exclusão, anonimização) é gerido integralmente pelo backend externo. O frontend atua apenas como coletor/exibidor transitório: dados de formulário residem em memória durante a edição e são limpos ao navegar para fora (SPA routing); evidências (checklist/foto) são validadas client-side (tipo MIME, tamanho, nome) e enviadas via `FormData` multipart para o backend (conforme *Security Policies* §3). <!-- source: docs/07-database/db-schema-spec.md#1 --> <!-- source: docs/06-security/security-policies.md#3 --> <!-- source: docs/06-security/security-policies.md#5 -->

---

## 5. Seeds (Dev/Test)

```yaml
seed:
  environments: []
  purpose: "Não aplicável — ausência de banco de dados no repositório"
  rules:
    production_allowed: false
    contains_real_data: false
    secrets_allowed: false
  datasets: []
```

> **Justificativa:** Sem banco local, não há seeds, fixtures ou dados de teste persistidos. Testes de frontend utilizam mocks de API (MSW ou similar) e dados sintéticos em memória. Qualquer necessidade de seed para ambientes de homologação do backend é responsabilidade do time de backend. <!-- source: docs/07-database/db-schema-spec.md#1 --> <!-- source: docs/07-database/db-domain-model.md#1 -->

---

## 6. Fronteira de Responsabilidade & Referências Cruzadas

| Domínio | Responsável | Artefato de Referência |
| :--- | :--- | :--- |
| **Schema físico (tabelas, colunas, constraints, índices, views, functions, triggers)** | Backend Team | Documentação do serviço de backend correspondente (fora deste workspace) |
| **Roles, grants, RLS, políticas de acesso a nível de banco** | Backend Team / DBA | *Security Policies* §6 (modelo RBAC delegado) |
| **Classificação de dados, criptografia em repouso, mascaramento, retenção legal, LGPD Art. 46/48** | Backend Team / Security / Legal | *Security Policies* §5, §12 |
| **Ciclo de vida de dados (creation → active → archival → deletion/anonymize)** | Backend Team | *Security Policies* §5 (retenção & descarte) |
| **Seeds / dados de teste em ambientes não-prod** | Backend Team / QA | *Security Policies* §5 (anonimização em staging/homologação) |
| **Validação de upload de evidências (client-side)** | Frontend Team | *Security Policies* §3 (casos de teste obrigatórios) |
| **Sanitização de output / prevenção XSS no frontend** | Frontend Team | *Security Policies* §8 (DOMPurify, `textContent`, CSP) |
| **Gestão de segredos de aplicação (chaves JWT, DSN telemetria, credenciais CDN)** | Platform / DevOps | *Security Policies* §7 |
| **Logging de auditoria / telemetria sem vazamento de PII/tokens** | Frontend + Backend | *Security Policies* §10 |
| **Resposta a incidente de segurança (XSS, CDN compromise, token leak)** | Security / Frontend / Backend | *Security Policies* §11 |

---

## 7. Evidências de Rastreabilidade

| Item | Fonte | Observação |
| :--- | :--- | :--- |
| Ausência de banco no repositório | `docs/07-database/db-domain-model.md#1` | Domínio `aegis1-frontend` declara `entities: []` e descrição explícita de "NÃO possui banco de dados próprio" |
| Contrato de schema vazio intencional | `docs/07-database/db-schema-spec.md#1` | "Nenhuma tabela é declarada neste contrato porque **não há schema de banco neste repositório**" |
| Ponto único de integração de dados | `docs/07-database/db-domain-model.md#3 (ASM-001)` | `frontend/src/services/api.js` encapsula chamadas HTTP para backend externo |
| Nenhuma dependência de banco/ORM | `docs/07-database/db-domain-model.md#3 (ASM-003)` | `package.json` + varredura determinística: apenas `@popperjs/core` |
| Contexto de banco confirmado | `docs/07-database/db-manifest.md#1` | "Este repositório não possui banco de dados nem modelo de domínio persistente" |
| Classificação e proteção de dados sensíveis | `docs/06-security/security-policies.md#5` | Tabela de classificação (JWT, PII, ordens, evidências, trace-id) |
| Validação client-side de uploads | `docs/06-security/security-policies.md#3` | Tipo MIME, tamanho, sanitização de nome de arquivo |
| RBAC delegado ao backend | `docs/06-security/security-policies.md#6` | Frontend apenas reflete permissões recebidas |
| Criptografia em trânsito (TLS 1.2+) | `docs/06-security/security-policies.md#5` | NFR-SEC01, HSTS no CDN |
| Retenção & descarte de tokens/dados | `docs/06-security/security-policies.md#5` | TTL curto, rotação de refresh, limpeza em logout |
| Compliance LGPD / ISO 27001 / PCI DSS | `docs/06-security/security-policies.md#12` | Mapeamento de requisitos para controles implementados |

---

## 8. Ações Requeridas (Fora do Escopo Deste Repositório)

> As seguintes lacunas **não podem ser preenchidas neste documento** porque dependem do backend externo. Devem ser rastreadas no repositório/backlog do time de backend:

- [ ] Definição formal de roles de banco e matriz de permissões (GRANT/REVOKE)
- [ ] Implementação de Row-Level Security por tenant/filial/usuário
- [ ] Inventário completo de campos sensíveis com classificação (public/internal/confidential/restricted) e controles de proteção (encryption at rest, masking, tokenization)
- [ ] Políticas de retenção/arquivamento/exclusão por entidade com justificativa legal (LGPD, legislação setorial)
- [ ] Estratégia de seeds/dados sintéticos para ambientes de homologação/staging
- [ ] Plano de criptografia em repouso (chaves, rotação, KMS)
- [ ] Procedimento de anonimização/pseudonimização para ambientes não-prod
- [ ] Testes de penetração focados em camada de banco (SQLi, privilege escalation, data exfiltration)

---

> **Conclusão:** Este documento registra formalmente que **o Aegis1 Frontend não possui camada de persistência**. Toda segurança de banco de dados, controle de acesso a dados em repouso, ciclo de vida de dados e conformidade regulatória de armazenamento são responsabilidade do **backend externo**. O frontend limita-se a: (1) validar entradas client-side antes de enviar ao backend, (2) sanitizar outputs para prevenir XSS, (3) gerenciar tokens de sessão com migração planejada para cookies `HttpOnly`, (4) propagar `trace-id` para observabilidade, e (5) garantir TLS 1.2+ em todas as comunicações. Consulte a documentação do backend para os artefatos correspondentes de *Database Security & Data Lifecycle*.