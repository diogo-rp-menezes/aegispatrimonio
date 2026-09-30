# Database Context — aegis_patrimonio

> **Versão:** 1.0 · **Owner:** backend-team · **Status:** Active

## 1. Purpose
Persistência transacional do domínio de **gestão de patrimônio/ativos** (cadastro, rastreamento, manutenção, alertas e auditoria de bens) para o serviço `aegis-patrimonio-backend`. O banco é a *source of truth* única e forte para todas as entidades de negócio do sistema.

## 2. System
Sistema consumidor principal: **aegis-patrimonio-backend** (Java/Spring Boot, localizado em `src/main/java/br/com/aegispatrimonio`).  
Referência arquitetural: [[system-architecture]] (conforme manifesto, seções de *ownership*, *governance* e *environments*).

## 3. Responsibilities
- Armazenar e versionar (via migrações controladas) o esquema relacional completo do domínio de patrimônio.
- Garantir consistência forte (ACID) para operações de criação, movimentação, manutenção e baixa de ativos.
- Suportar consultas analíticas e operacionais do backend (relatórios, dashboards, alertas de uso/manutenção).
- Servir como base para *seeding* de dados realistas em ambientes de desenvolvimento/teste (`RealisticDataSeeder`).
- Aplicar políticas de governança: aprovação humana para mudanças destrutivas, detecção de *drift*, validação de esquema.

## 4. Non-Responsibilities
- **Cache de sessão / tokens** — fora do escopo (não há menção a Redis ou similar no manifesto).
- **Armazenamento de arquivos/binários** (fotos de ativos, manuais, anexos) — o manifesto não prevê *blob storage* no PostgreSQL.
- **Fila de mensageria / event sourcing** — não há tabelas de *outbox* ou *event store* declaradas.
- **Dados de autenticação/autorização** (usuários, roles, OAuth) — o modelo `Usuario` existe no código (`src/main/java/br/com/aegispatrimonio/model/Usuario.java`), mas o manifesto não detalha se a tabela de usuários vive neste banco ou em serviço de identidade separado. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** Assumimos que usuários de aplicação ficam aqui; validar com o time.

## 5. Domain
Domínio de negócio: **Gestão de Patrimônio Organizacional** (ativos, localizações, responsáveis, manutenções, alertas, depreciação, auditoria).  
Termos-chave: [[glossario]] (não fornecido no contexto — recomenda-se criar/referenciar glossário do projeto).

## 6. Consumers
| Consumer | Tipo | Responsabilidade |
| :--- | :--- | :--- |
| `aegis-patrimonio-backend` | API (REST) | CRUD de ativos, manutenções, alertas, relatórios; executa migrações na inicialização. |
| `frontend` (JS) | API (via backend) | Consome dados via chamadas HTTP (`frontend/src/services/api.js`); não acessa o banco diretamente. |
| `RealisticDataSeeder` | Batch (inicialização) | Popula dados de teste/desenvolvimento em ambiente `development` e `test`. |
| Ferramentas de migração (Flyway/Liquibase — inferido) | Batch | Aplicam scripts versionados em `src/main/resources/db/migration` em todos os ambientes. |

> **Nota:** O manifesto não lista consumidores analíticos (BI, Data Lake) separados; assumimos que, se existirem, leem via *replica* ou *CDC* — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**.

## 7. Data Ownership
| Conjunto de Dados | Owner (Time/Serviço) | Observação |
| :--- | :--- | :--- |
| Esquema completo (`public` / schema default) | `backend-team` / `aegis-patrimonio-backend` | Dono único; mudanças via PR + aprovação (política `controlled`). |
| Dados de *seeding* (dev/test) | `backend-team` | Gerados por `RealisticDataSeeder`; não existem em `staging`/`production`. |
| Migrações (DDL/DML versionado) | `backend-team` | Local: `src/main/resources/db/migration`; *source of truth* declarativo. |

## 8. Consistency Requirements
- **Forte (transacional)** para todas as operações de negócio: criação/movimentação/baixa de ativos, ordens de manutenção, alertas, auditoria.  
  *Justificativa:* manifesto declara `consistency: "strong"` e `architecture.model: "relational"`.
- **Eventual** não se aplica neste banco (single-tenancy, sem réplicas de leitura declaradas).

## 9. Availability Requirements
- **Ambientes:** `development`, `test`, `staging`, `production` (manifesto).  
- **SLA de produção:** não explicitado no manifesto. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** Recomenda-se ≥ 99,9% (≤ 8,76 h/ano de indisponibilidade planejada + não planejada) para alinhar com criticidade de gestão patrimonial.  
- **RPO/RTO:** não definidos no manifesto — necessários para plano de *backup/restore* e *disaster recovery* (ver [[db-performance-recovery]]).

## 10. Performance Requirements
- **SLOs de latência/throughput:** não quantificados no manifesto.  
- **Referência cruzada:** detalhamento esperado em [[db-performance-recovery]] (artefato previsto em `docs/07-database/`).  
- **Observabilidade:** manifesto exige `drift_detection: true` e `schema_validation: true`; métricas de *query latency*, *connection pool saturation* e *migration duration* devem ser instrumentadas.

## 11. Compliance Requirements
- **LGPD (Lei Geral de Proteção de Dados — Brasil):** provável, pois o projeto é brasileiro (`br.com.aegispatrimonio`) e lida com dados de responsáveis por ativos (pessoas físicas). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**  
- **PCI-DSS, HIPAA, SOX:** não aplicáveis ao domínio de patrimônio genérico, salvo se a organização atuar em setor regulado (bancário, saúde, capital aberto) — **validar com *compliance* da organização**.  
- **Auditoria:** manifesto prevê `governance.drift_detection` e `schema_validation`; recomenda-se *audit log* nativo (p. ex. `pgaudit`) para rastreamento de DML sensível.

## 12. Lifecycle
1. **Criação:** instância PostgreSQL 15+ provisionada por IaC (não descrito no manifesto — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**).  
2. **Inicialização:** migrações em `src/main/resources/db/migration` aplicadas automaticamente na subida do backend (política `controlled`).  
3. **Utilização:** operação contínua nos 4 ambientes; *seeding* apenas em `development`/`test`.  
4. **Evolução:** novas migrações versionadas (nunca destrutivas sem aprovação); *drift detection* contínuo.  
5. **Arquivamento:** não definido — sugerir política de *partitioning* por ano para tabelas de auditoria/histórico e *cold storage* para backups > 1 ano.  
6. **Eliminação:** *decommission* do banco só após confirmação de migração para nova versão/arquitetura e retenção legal cumprida; exige aprovação humana (política `destructive_changes: "approval-required"`).