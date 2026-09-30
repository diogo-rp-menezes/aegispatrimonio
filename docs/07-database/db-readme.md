# aegis_patrimonio — Database README

> **Versão:** 1.0 · **Owner:** backend-team · **Status:** Active

## 1. Purpose
Banco de dados relacional PostgreSQL 15+ que suporta o domínio de gestão de patrimônio (ativos, manutenções, alertas, inventário de hardware, auditoria e controle de acesso RBAC). Centraliza o ciclo de vida de bens patrimoniais — da aquisição à baixa — com rastreabilidade total, depreciação contábil, monitoramento preditivo de hardware e conformidade LGPD/SOX. Ver [[db-manifest]] e [[db-domain-model]].

## 2. Architecture
- **Modelo:** Relacional, single-tenant, consistência forte (ACID)
- **Engine:** PostgreSQL 15+ (compatibilidade `>=15 <17`)
- **Particionamento nativo:** `health_check_disco` (RANGE mensal, retenção 13 meses) e `auditoria` (RANGE anual, retenção 5 anos online + 5 anos cold)
- **Fonte da verdade:** Migrações versionadas em `src/main/resources/db/migration` (Flyway/Liquibase — ver [[db-migration-spec]])
- **Contrato físico:** [[db-schema-spec]] (20 tabelas, 5 views, 6 functions, 20 triggers, 39 índices, 106 constraints)
- **Segurança:** RLS por filial + RBAC granular (ADMIN, USER, GESTOR_PATRIMONIO, TECNICO_MANUTENCAO, AUDITOR) — ver [[db-security-lifecycle]]
- **Dados sensíveis:** Classificação `restricted` (CPF, senha, auditoria) e `confidential` (CNPJ, valores, PII) com criptografia em repouso (TDE) + column-level encryption (AES-256-GCM via AttributeConverter + Vault) — ver [[db-security-lifecycle#3-sensitive-data]]

## 3. Domains & Core Entities
| Entidade | Tabela | Classificação | Descrição Resumida |
| :--- | :--- | :--- | :--- |
| Ativo | `ativo` | core | Bem/item de valor rastreável (tag, valor, depreciação, status, localização, responsável) |
| TipoAtivo | `tipo_ativo` | reference | Catálogo de categorias com taxa de depreciação e vida útil padrão |
| Localizacao | `localizacao` | reference | Local físico/lógico (sala, andar, estoque) vinculado a filial |
| Departamento | `departamento` | reference | Unidade organizacional com centro de custo |
| Filial | `filial` | reference | Estabelecimento da organização (CNPJ, razão social, endereço) |
| Fornecedor | `fornecedor` | reference | Entidade externa de aquisição/manutenção |
| Funcionario | `funcionario` | core | Colaborador (CPF, matrícula, cargo, lotação) — custodiante de ativos |
| Usuario | `usuario` | core | Credencial de acesso (ADMIN/USER) vinculada 1:1 a funcionário |
| Manutencao | `manutencao` | transactional | Ordem de serviço (preventiva/corretiva) com fluxo de aprovação e custos |
| Alerta | `alerta` | transactional | Notificações de risco (disco crítico, manutenção vencida, garantia, depreciação) |
| AtivoDetalheHardware | `ativo_detalhe_hardware` | supporting | Especificações TI (CPU, RAM, OS) — 1:1 com ativo de categoria TI |
| Memoria | `memoria` | supporting | Pentes de RAM instalados |
| Disco | `disco` | supporting | Discos/volumes com saúde SMART e uso |
| AdaptadorRede | `adaptador_rede` | supporting | Interfaces de rede (MAC, IP, tipo, velocidade) |
| HealthCheckDisco | `health_check_disco` | audit | Métricas periódicas de disco (particionado, alta volumetria) |
| Auditoria | `auditoria` | audit | Trilha imutável de alterações (insert-only, particionado, LGPD/SOX) |
| Papel | `papel` | reference | Roles granulares RBAC (GESTOR_PATRIMONIO, TECNICO_MANUTENCAO, AUDITOR) |
| Permissao | `permissao` | reference | Autorizações recurso:ação |
| PapelPermissao | `papel_permissao` | reference | Junção N:M papel↔permissao |
| UsuarioPapel | `usuario_papel` | reference | Junção N:M usuario↔papel |

Ver [[db-domain-model]] para modelo conceitual completo e [[db-schema-spec#1-tables]] para contrato físico detalhado.

## 4. Source of Truth
Definido em [[db-manifest]]:
- **ID:** `aegis-patrimonio-db`
- **Nome físico:** `aegis_patrimonio`
- **Versão atual:** `0.1.0`
- **Localização das migrações:** `src/main/resources/db/migration`
- **Artefatos de documentação:** `docs/07-database/`
- **Política de migração:** `controlled` — mudanças destrutivas exigem aprovação humana explícita
- **Drift detection:** Habilitado (requer motor determinístico próprio — ver seção 11)
- **Schema validation:** Habilitado

## 5. Migration Strategy
Resumo da política em [[db-migration-spec]]:
- **Ferramenta:** Flyway ou Liquibase (confirmar — ver checklist item 2)
- **Estratégia:** Expand → Migrate → Contract (offline deployment para v1.0)
- **Versionamento:** Uma pasta/migration por mudança, referenciando `db-migration-*.md`
- **Rollback:** Possível via `DROP` em ordem reversa de dependência (requer downtime)
- **Aprovação obrigatória:** Sim — criação de schema completo com particionamento, triggers LGPD e funções críticas
- **Validação pós-deploy:** 9 checks automatizados (contagem objetos, FKs, triggers, particionamento, views, índices BRIN, performance baseline) — ver [[db-migration-spec#5-verification]]

## 6. Environments
| Environment | Purpose | Configuração Específica |
| :--- | :--- | :--- |
| Development | Desenvolvimento local | Seed `baseline_minimal` (dados sintéticos determinísticos) — ver [[db-security-lifecycle#5-seeds]] |
| Test | Validação automatizada (CI) | Seed `realistic_full` (~500 ativos) + `compliance_audit` para testes de retenção/anônimização |
| Staging | Pré-produção (espelho de prod) | Dados anonimizados de produção + seeds de compliance; particionamento e jobs de retenção ativos |
| Production | Produção | Dados reais; TDE habilitado; Vault para chaves; pg_partman/jobs de retenção configurados; WORM backup para auditoria |

## 7. Security
Resumo de [[db-security-lifecycle]]:

### Roles & RBAC
- **ADMIN:** Acesso total a todas as tabelas (incl. `filial`, `usuario`, `papel`, `permissao`, `auditoria`)
- **USER:** Operacional restrito à própria filial (via `app.current_filial_id` no JWT)
- **GESTOR_PATRIMONIO:** USER + escrita em ativos/manutenções/fornecedores/localizações/departamentos da filial
- **TECNICO_MANUTENCAO:** Foco em execução de OS (manutencao UPDATE, ativo SELECT, alerta SELECT/UPDATE)
- **AUDITOR:** Leitura de `auditoria`, `alerta`, `ativo`, `manutencao`, `funcionario`, views de depreciação e saúde de disco

### Row-Level Security (RLS)
Habilitada em 13 tabelas com políticas baseadas em `current_setting('app.current_filial_id')` e `current_setting('app.current_user_id')`/`app.current_role`. Tabelas globais (`tipo_ativo`, `fornecedor`) usam `using: true` com controle via RBAC na camada de aplicação. Ver [[db-security-lifecycle#2-row-level-security]].

### Dados Sensíveis
- **Restricted (PII LGPD):** `funcionario` (CPF, nome, email, telefone, cargo, datas), `usuario` (username, password_hash bcrypt cost=12, role), `auditoria` (valores_anteriores/novos JSONB, ip_origem, usuario_id) — column-level encryption AES-256-GCM + Vault key rotation 90d
- **Confidential:** `filial` (CNPJ, razão social, endereço), `fornecedor` (CNPJ, dados comerciais), `ativo` (tag, valor, número de série), `manutencao` (custos) — TDE + RLS filial
- **Secrets:** Proibidos no schema — gerenciados via HashiCorp Vault / AWS Secrets Manager / Azure Key Vault

### Data Lifecycle por Entidade
| Entidade | Retenção | Método Fim de Vida | Base Legal |
| :--- | :--- | :--- | :--- |
| Filial | Permanente | Soft delete (`ativa=false`) | Obrigação societária/fiscal |
| Funcionario | 20 anos pós-demissão | Anonimização (CPF→hash, nome→FUNCIONARIO_ANON) | CLT Art. 41 + LGPD Art. 16 |
| Usuario | 5 anos pós-último login | Hard delete | SOX / investigação incidentes |
| Ativo | 7 anos pós-baixa | Anonimização (valor→0, tag→BAIXADO_{id}) | Contabilidade / SOX |
| Manutencao | 10 anos pós-conclusão | Anonimização (custos→0, observações→NULL) | Garantias / SOX |
| Alerta | 2 anos pós-leitura | Hard delete | Tendências / MTTR |
| HealthCheckDisco | 13 meses (rolling) | Drop partition automático | Monitoramento operacional |
| Auditoria | 5 anos online + 5 anos cold (WORM) | Destruição certificada | LGPD Art. 16 / SOX |

Ver [[db-security-lifecycle#4-data-lifecycle]] para detalhes completos por entidade.

## 8. Backup, Recovery & Observability
> **Nota:** O artefato `db-performance-recovery` referenciado no template não foi localizado no diagnóstico. Abaixo, consolido o que é inferível dos artefatos-fonte disponíveis.

### Backup
- **Frequência:** Diário (base) + WAL contínuo (PITR)
- **Retenção:** 30 dias online + 1 ano cold (S3/GCS com Object Lock para auditoria)
- **Teste de restore:** Mensal em staging (validar integridade referencial + particionamento)

### Recovery
- **RPO:** < 5 min (WAL streaming + replication slot)
- **RTO:** < 30 min (standby promovido + aplicação reconecta)
- **Procedimento:** Documentado em runbook operacional (fora do escopo deste README)

### Observabilidade
- **Métricas-chave:** Tamanho de tabelas/partições, taxa de crescimento `health_check_disco`/`auditoria`, latência P95 queries críticas (listagem ativos por filial/status, histórico manutenção, health check disco), taxa de erro triggers auditoria
- **Alertas:** Partição `health_check_disco` próxima do limite (12 meses), partição `auditoria` próxima do limite (4 anos), falha de trigger `fn_auditoria_trigger`, drift de schema detectado
- **Logs:** `log_statement = 'ddl'` + `log_min_duration_statement = 1000` + `pg_audit` para acesso a tabelas `restricted`/`confidential`

## 9. Change Management
Processo definido em [[db-migration-spec]] e [[db-manifest#governance]]:

1. **Proposta:** Issue/PR com `db-migration-<id>.md` descrevendo mudança (DDL, DML, rollback, impacto)
2. **Análise de impacto:** Verificar FKs, índices, views, functions, triggers, RLS, particionamento, seeds
3. **Aprovação:** Obrigatória para mudanças destrutivas (`DROP`, `TRUNCATE`, `ALTER COLUMN TYPE` destrutivo) — Tech Lead + DBA + Security Officer
4. **Validação:** Aplicar em staging idêntico a prod → rodar checks de [[db-migration-spec#5-verification]] → testes de integração
5. **Deploy:** Janela de manutenção (offline para DDL pesado) ou online se backward-compatible (Expand phase apenas)
6. **Pós-deploy:** Verificar métricas, validar aplicação, atualizar documentação (`db-schema-spec`, `db-readme.md`)

**Nunca** edite SQL gerado em `database/schema/` diretamente — edite o contrato [[db-schema-spec]] e regenere.

## 10. Directory Structure
```text
database/
├── dbac.yaml                    # Database as Code config (entrypoint)
├── model/                       # Modelo canônico (derivado de db-domain-model.md)
├── schema/                      # SQL gerado a partir de db-schema-spec.md — NÃO EDITAR MANUALMENTE
├── migrations/                  # Uma pasta por migration, referenciando db-migration-*.md
│   └── MIG-20250115-001/       # Migration inicial (ver db-migration-spec)
├── seeds/                       # Dados sintéticos por ambiente (ver db-security-lifecycle#5)
│   ├── baseline_minimal/
│   ├── realistic_full/
│   └── compliance_audit/
└── tests/                       # Testes de schema (constraints, triggers, views, performance)
```

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

## 12. Checklist de Validação Humana (pendente)
| Item | Descrição | Responsável | Status |
|------|-----------|-------------|--------|
| 1 | Confirmar motor de banco (PostgreSQL 15+?) | Tech Lead / DBA | ⬜ Pendente |
| 2 | Confirmar ferramenta de migração (Flyway/Liquibase?) | Tech Lead | ⬜ Pendente |
| 3 | Validar validação completa de CPF/CNPJ (banco vs app) | Backend Team | ⬜ Pendente |
| 4 | Decidir: Enum nativo PG vs CHECK constraint | Backend Team / DBA | ⬜ Pendente |
| 5 | Aprovar estratégia de particionamento e retenção | DBA / Compliance | ⬜ Pendente |
| 6 | Definir criptografia/proteção dados LGPD (restricted/confidential) | Security / Legal | ⬜ Pendente |
| 7 | Implementar sequence `seq_numero_os` e reset anual | Backend Team | ⬜ Pendente |
| 8 | Implementar middleware para `app.current_user_id` / `app.client_ip` | Backend Team | ⬜ Pendente |
| 9 | Revisar códigos de `tipo_ativo` considerados "TI" em `fn_verificar_ativo_ti` | Product Owner / Domain Expert | ⬜ Pendente |
| 10 | Validar fluxo de status de manutenção com stakeholders | Product Owner | ⬜ Pendente |

Ver [[db-schema-spec#10-checklist-de-validacao-humana]] para detalhes.

---

<!-- source: db-manifest -->
<!-- source: db-schema-spec -->
<!-- source: db-security-lifecycle -->
<!-- source: db-migration-spec -->