# Database Context — Aegis1 Frontend (Sem Banco de Dados Próprio)

> **Versão:** 1.0 · **Owner:** Frontend Team · **Status:** Active

## 1. Purpose
Este repositório **não possui banco de dados**. O propósito deste documento é registrar explicitamente a ausência intencional de persistência local e documentar que toda a responsabilidade de dados reside no backend externo (API REST/HTTPS), fora do escopo deste codebase. O frontend é uma SPA estática servida via CDN que apenas consome a API.

## 2. System
- **Sistema:** Aegis1 Frontend (SPA)
- **Arquitetura referenciada:** [[system-architecture]] — camada de apresentação (frontend) desacoplada do backend de persistência
- **Integração:** Consome `frontend/src/services/api.js` para comunicação com backend externo

## 3. Responsibilities
- Renderização da interface de usuário (SPA)
- Orquestração de chamadas HTTP para o backend externo via `api.js`
- Gerenciamento de estado transitório em memória (React state, context, etc.)
- Validação de entrada no lado do cliente (UX), **não** substitui validação de negócio no backend

## 4. Non-Responsibilities
- Persistência de qualquer entidade de domínio (usuários, transações, configurações, logs, etc.)
- Execução de migrações, seeds, DDL ou DML
- Garantia de integridade referencial, constraints, triggers ou stored procedures
- Backup, restore, point-in-time recovery ou arquivamento de dados
- Conformidade de dados (LGPD, PCI-DSS, HIPAA) — responsabilidade do backend
- Cache de sessão ou tokens de longa duração (se houver, fica no backend ou storage do navegador, não em banco neste repo)

## 5. Domain
- **Domínio coberto:** Nenhum — o frontend não define modelo de domínio persistente
- **Referência:** [[glossario]] — termos de negócio são definidos e versionados no backend

## 6. Consumers
| Consumer | Tipo | Responsabilidade |
| :--- | :--- | :--- |
| Backend externo (API) | API REST/HTTPS | Dono exclusivo da persistência, regras de negócio, consistência e ciclo de vida dos dados |
| Navegador do usuário final | SPA Runtime | Executa código frontend; armazena apenas estado efêmero (localStorage/sessionState se aplicável) |

## 7. Data Ownership
- **Frontend Team:** Dono da experiência de usuário, contratos de API (OpenAPI/Swagger consumidos), validações de UI
- **Backend Team (fora deste workspace):** Dono exclusivo de todos os dados persistidos, schema, migrações, políticas de retenção, compliance e SLA de disponibilidade dos dados

## 8. Consistency Requirements
- **Neste repositório:** Não se aplica — não há estado persistente
- **No backend (referência):** Consistência forte (transacional) para operações de negócio; eventual para caches/denormalizações — detalhes na documentação do backend

## 9. Availability Requirements
- **Frontend (SPA/CDN):** SLA de disponibilidade do ativo estático (ex.: 99.9% via CDN)
- **Dados (backend):** SLA definido pelo time de backend — fora do escopo deste repositório

## 10. Performance Requirements
- **Frontend:** Tempo de carregamento inicial < 3s (Core Web Vitals), latência de chamadas API < 500ms p95 (depende do backend)
- **Backend:** SLOs de latência/throughput documentados em [[db-performance-recovery]] do repositório de backend

## 11. Compliance Requirements
- **Neste repositório:** Nenhum dado sensível persistido — apenas trânsito via HTTPS
- **Backend:** LGPD, PCI-DSS, HIPAA ou outros conforme aplicável — responsabilidade do time de backend

## 12. Lifecycle
- **Criação:** Build da SPA (`npm run build` ou equivalente) → artefatos estáticos (HTML/JS/CSS)
- **Utilização:** Servidos via CDN; chamadas de runtime para API externa
- **Arquivamento:** Versões antigas do build mantidas no CDN/bucket por política de rollback
- **Eliminação:** Remoção de builds obsoletos do CDN; **nenhum dado de banco a eliminar neste repo**

---

> **Nota de rastreabilidade:** Este documento espelha o `Database Manifest` (db-manifest) deste repositório. Para o modelo de dados real (entidades, relacionamentos, constraints, DDL, migrações), consultar a documentação do backend (fora deste workspace). <!-- source: docs/07-database/db-manifest.md -->