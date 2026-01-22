**Guia Definitivo de Arquitetura e Implementação do Aegis Patrimônio**.

Este documento será o seu manual de referência, garantindo que a
implementação *from scratch* siga rigorosamente os padrões de alta
qualidade que você estabeleceu.

**📘 GUIA DEFINITIVO DE ARQUITETURA E IMPLEMENTAÇÃO - AEGIS PATRIMÔNIO**

**1. VISÃO GERAL E PRINCÍPIOS DE IMPLEMENTAÇÃO**

**Missão:** Implementar um sistema de gestão patrimonial robusto,
escalável e seguro, focado em monitoramento automático de ativos e
controle de acesso granular, utilizando as melhores práticas
arquiteturais.

**Princípios de Implementação:**

1.  **Domain-Driven Design (DDD) First:** O design do software deve
    modelar o negócio.

2.  **Clean Architecture:** Separação de camadas para independência de
    frameworks e manutenibilidade.

3.  **Segurança por Design (NIST RBAC):** Controles de acesso granulares
    e auditáveis desde o início.

4.  **Qualidade (Quality Gates):** Cobertura de testes ≥80% e validação
    automática de segurança em cada *merge*.

**2. MODELO DE DOMÍNIO E USER STORIES DETALHADAS**

O design do domínio é a base da **FASE 1**.

**2.1 Agregados Core e Entidades**

O modelo é baseado em **Domain-Driven Design (DDD)**, utilizando
Agregados (Roots) para garantir a consistência transacional e **Value
Objects (VOs)** para imutabilidade e encapsulamento de regras.

| **Agregado (Root)** | **Entidade/VOs no Agregado**                 | **Regras de Negócio Críticas (RNC)**                                                                                                       |
|---------------------|----------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| **Patrimônio**      | Ativo (Root), Categoria, Localizacao         | RNC.1: Ativo.numeroSerie deve ser único na criação. RNC.2: Ativo.valorAtual mutável apenas por serviços de depreciação ou manutenção.      |
| **Operações**       | Manutencao (Root), Movimentacao              | RNC.3: Manutencao possui um Workflow de estados (Fase 4). RNC.4: Movimentacao é imutável, registra histórico completo (Audit Pattern).     |
| **Segurança**       | Usuario (Root), Grupo (Role), Permissao (VO) | RNC.5: Acesso baseado no modelo **NIST RBAC** (resource:action). RNC.6: Permissões são herdadas via hierarquia de Grupo (se implementada). |

**2.2 User Stories por Sprint (Guia de Implementação)**

O detalhamento das User Stories é a aplicação prática do design de
domínio em cada sprint.

| **Fase**   | **Sprint**    | **Foco Principal**       | **User Stories Chave**                                                                                                                                                                                                                                                                                    |
|------------|---------------|--------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **FASE 1** | **Sprint 1**  | RBAC e Infra Core        | **\[DDD/RBAC\]** Como Desenvolvedor, eu quero configurar o Agregado Usuario e suas entidades auxiliares (Grupo, Permissao). **\[Infra\]** Como Desenvolvedor, eu quero usar **Flyway** para versionar as tabelas base.                                                                                    |
|            | **Sprint 2**  | Domínio Core e Segurança | **\[DDD/Agregado\]** Como Gestor de Ativos, eu quero cadastrar e gerenciar o Agregado Ativo (CRUD). **\[Segurança\]** Como Desenvolvedor, eu quero proteger as APIs de AtivoController usando **@PreAuthorize com o modelo NIST RBAC** (\'ATIVO:READ\').                                                  |
|            | **Sprint 3**  | Depreciação e Auditoria  | **\[DDD/VO/Service\]** Como Sistema, eu quero usar o DepreciacaoService para aplicar o **Método Linear** de depreciação (RNC.7: Accounting Depreciation Standards) via @Scheduled. **\[DDD/Auditoria\]** Como Gestor, eu quero que o Agregado Movimentacao seja registrado a cada mudança de Localizacao. |
| **FASE 2** | **Sprint 4**  | Agente OSHI e Ingestão   | **\[Integração\]** Como Técnico de TI, eu quero que o Agente **OSHI** colete métricas e envie ao Backend via **Push Model** (RNC.10). **\[DDD/Metrics\]** Como Desenvolvedor, eu quero persistir a última coleta e o status do Ativo (Metric Collection Patterns).                                        |
|            | **Sprint 5**  | Alertas e Histórico      | **\[DDD/Alerta\]** Como Administrador, eu quero configurar limites de Alerta, que são verificados pelo AlertNotificationService (Alerting Patterns). **\[Arquitetura\]** Como Sistema, eu quero que o serviço de Health Check use o **Circuit Breaker Pattern** (RNC.15) para evitar falhas em cascata.   |
| **FASE 3** | **Sprint 6**  | Migração OAuth2          | **\[Segurança\]** Como Desenvolvedor, eu quero configurar o Spring Security como **Resource Server** (RNC.4) e integrar com um IdP OAuth2/OIDC. **\[Segurança\]** Como Sistema, eu quero provisionar um Usuario automaticamente (*JIT Provisioning*).                                                     |
|            | **Sprint 7**  | Mapeamento SSO           | **\[Segurança/RBAC\]** Como Administrador, eu quero mapear Grupos do IdP para Roles internas do sistema, para aplicar as permissões do **NIST RBAC**.                                                                                                                                                     |
| **FASE 4** | **Sprint 8**  | Workflow Manutenção      | **\[DDD/Service\]** Como Desenvolvedor, eu quero implementar o WorkflowAprovacaoService para o Agregado Manutencao (RNC.3). **\[Auditoria\]** Como Sistema, eu quero auditar todas as transições de estado (Audit Pattern).                                                                               |
|            | **Sprint 9**  | Workflow Movimentação    | **\[DDD/Regra\]** Como Sistema, eu quero bloquear a Movimentacao de um Ativo se ele estiver com status EM_MANUTENCAO (Lifecycle Management - RNC.12).                                                                                                                                                     |
| **FASE 5** | **Sprint 10** | Mobile Setup e QR        | **\[Mobile\]** Como Técnico em campo, eu quero escanear o QR Code de um ativo para consulta rápida (REST API Design - Nível 3). **\[Mobile\]** Configurar **WatermelonDB** para *Offline First*.                                                                                                          |
|            | **Sprint 11** | Sincronização Offline    | **\[Mobile/Sync\]** Como Técnico, eu quero registrar Manutencao offline e sincronizar automaticamente ao reestabelecer a conexão, com resolução de conflitos (Agent Communication Patterns).                                                                                                              |
| **FASE 6** | **Sprint 12** | Data Pipeline e IA       | **\[Data\]** Como Cientista de Dados, eu quero uma API para extrair dados históricos (Four Golden Signals - RNC.7) para treinar o modelo de ProbabilidadeDeFalha.                                                                                                                                         |
|            | **Sprint 13** | Integração Preditiva     | **\[DDD/Alerta\]** Como Sistema, eu quero que a previsão de IA atualize o status do Ativo para RISCO_PREDITIVO. **\[UI\]** Como Gestor, eu quero um **Dashboard de Risco** (RNC.8) baseado na previsão.                                                                                                   |
| **FASE 7** | **Sprint 14** | Performance/Hardening    | **\[Performance\]** Como Desenvolvedor, eu quero aplicar o **Cache-Aside Pattern** (RNC.13) com Redis nas buscas mais frequentes. **\[DB\]** Criar **Índices Otimizados** (RNC.14) nas colunas de busca (ex: idx_ativo_status).                                                                           |
|            | **Sprint 15** | Go-Live e Doc            | **\[Qualidade\]** Executar testes **E2E** e validar 100% dos *Quality Gates*. **\[Documentação\]** Finalizar manuais e documentação **OpenAPI**.                                                                                                                                                          |

**3. REFERÊNCIAS DE MODELOS E PADRÕES ARQUITETURAIS**

Esta seção detalha os padrões específicos que **devem ser seguidos** na
implementação.

**3.1 🏛️ REFERÊNCIAS ARQUITETURAIS**

| **Ref.** | **Modelo/Padrão**              | **Aplicação Chave no Aegis Patrimônio**                                                                                                                                        |
|----------|--------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **1.**   | **Domain-Driven Design (DDD)** | A lógica de negócio reside nos *Domain Services* (DepreciacaoService) e a consistência é mantida pelas *Aggregate Roots* (Ativo). *Value Objects* são imutáveis (Depreciacao). |
| **2.**   | **Clean Architecture**         | **Estrutura de Pacotes:** domain/ (Regras de negócio puras), application/ (Casos de uso), infrastructure/ (Implementações JPA/Integrações), presentation/ (REST Controllers).  |

**3.2 🔐 REFERÊNCIAS DE SEGURANÇA E RBAC**

| **Ref.** | **Modelo/Padrão**                | **Aplicação Chave no Aegis Patrimônio**                                                                                                                                               |
|----------|----------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **3.**   | **RBAC NIST Standard**           | O modelo de permissões será **Role-Based** (Usuario → Grupo → Permissao). Permissao é um VO definido por **Recurso** (Ex: ATIVO) e **Operação** (Ex: LER, CRIAR).                     |
| **4.**   | **Spring Security Architecture** | Uso de CustomPermissionEvaluator e @PreAuthorize para injetar o modelo NIST na verificação de acesso em tempo de execução. Migração para **OAuth2/OIDC** (Resource Server) na Fase 3. |

**3.3 💾 REFERÊNCIAS DE PERSISTÊNCIA**

| **Ref.** | **Modelo/Padrão**             | **Aplicação Chave no Aegis Patrimônio**                                                                                                                                                                |
|----------|-------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **5.**   | **JPA/Hibernate Patterns**    | **Soft Delete Pattern** (@SQLRestriction(\"ativo = true\")) na BaseEntity. **Audit Pattern** (@CreatedDate, @CreatedBy) para rastreabilidade de mudanças. Estratégia EnumType.STRING para StatusAtivo. |
| **6.**   | **Flyway Migration Patterns** | Migrations estritamente **versionadas e sequenciais** (V1\_\_\..., V2\_\_\...). Estrutura de scripts separada para RBAC, Patrimônio, Health Check, etc.                                                |

**3.4 📊 REFERÊNCIAS DE DASHBOARD E MÉTRICAS**

| **Ref.** | **Modelo/Padrão**              | **Aplicação Chave no Aegis Patrimônio**                                                                                                                   |
|----------|--------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| **7.**   | **Metric Collection Patterns** | Foco nas **Four Golden Signals** (Latência, Tráfego, Erros, Saturação) e no **RED Method** para monitoramento do Agente OSHI.                             |
| **8.**   | **Dashboard Design Patterns**  | Layout **Card-based** no topo para Resumo Executivo, seguido por Detalhes Operacionais (Gráficos) e funcionalidade **Drill-Down** para detalhes do ativo. |

**3.5 🔧 REFERÊNCIAS DE INTEGRAÇÃO**

| **Ref.** | **Modelo/Padrão**                | **Aplicação Chave no Aegis Patrimônio**                                                                                                                          |
|----------|----------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **9.**   | **REST API Design**              | APIs RESTful seguindo o **Richardson Maturity Model Nível 3 (HATEOAS)** (uso de RepresentationModel e *links* self, update, etc., na Fase 5 e posteriores).      |
| **10.**  | **Agent Communication Patterns** | Uso do **Push Model** para o Agente OSHI. Implementação do **Metric Buffer Pattern** (Fase 2) para processar lotes de dados de Health Check de forma assíncrona. |

**3.6 💰 REFERÊNCIAS DE DEPRECIAÇÃO CONTÁBIL E LIFECYCLE**

| **Ref.** | **Modelo/Padrão**                     | **Aplicação Chave no Aegis Patrimônio**                                                                                                              |
|----------|---------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| **11.**  | **Accounting Depreciation Standards** | O DepreciacaoService deve suportar o **Straight-Line Method** (Padrão) e ser extensível para **Declining Balance Method** (Strategy Pattern).        |
| **12.**  | **Asset Lifecycle Management**        | O StatusAtivo (Enum) deve cobrir todo o ciclo de vida: Aquisição (SOLICITADO), Operacional (OPERACIONAL, EM_MANUTENCAO), e Final (BAIXADO, VENDIDO). |

**3.7 🚀 REFERÊNCIAS DE PERFORMANCE E MONITORAMENTO**

| **Ref.** | **Modelo/Padrão**         | **Aplicação Chave no Aegis Patrimônio**                                                                                                       |
|----------|---------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| **13.**  | **Caching Strategies**    | Aplicação do **Cache-Aside Pattern** com @Cacheable (Spring Cache + Redis) para Agregados de leitura frequente.                               |
| **14.**  | **Database Optimization** | Criação de **Índices Otimizados** (idx_ativo_status, idx_health_check_computador_data) para consultas críticas.                               |
| **15.**  | **Health Check Patterns** | Uso do **Circuit Breaker Pattern** (Resilience4j) ao tentar consultar o status de um ativo (Fase 2), protegendo o sistema de ativos inativos. |
| **16.**  | **Alerting Patterns**     | Hierarquia de alertas (INFO, WARNING, CRITICAL) e lógica para prevenir **Alert Fatigue** (Fase 2).                                            |

**4. QUALITY GATES E CONFORMIDADE**

A conformidade com estes critérios é obrigatória para o fechamento de
cada sprint.

| **Critério**        | **Meta Obrigatória**                             | **Padrão/Ferramenta de Referência**              |
|---------------------|--------------------------------------------------|--------------------------------------------------|
| **Code Coverage**   | **≥80%** no Service Layer.                       | Jacoco / Checkpoint de Qualidade                 |
| **Security Scan**   | **0** vulnerabilidades Críticas ou Altas.        | SonarQube / Security Hardening                   |
| **Arquitetura**     | Conformidade com DDD e Clean Architecture.       | Revisão por Pares / Estrutura de Pacotes (RNC.2) |
| **Performance API** | Response Time **\< 2 segundos** (APIs críticas). | JMeter / Metric Collection Patterns              |
| **Autorização**     | 100% dos *Controllers* de negócio protegidos.    | @PreAuthorize + NIST RBAC (RNC.4)                |
