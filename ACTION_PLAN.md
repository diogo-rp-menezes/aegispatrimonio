# Plano de Ação - Refatoração e Melhoria Contínua

Este documento serve como um checklist para guiar a implementação das refatorações e melhorias identificadas na análise holística do projeto. Cada item deve ser marcado como concluído (`[x]`) após sua finalização e verificação.

## 🎯 Objetivos Principais
- Aumentar a testabilidade e manutenibilidade do código.
- Reduzir acoplamento e promover a separação de responsabilidades.
- Garantir a aderência aos princípios SOLID e de Clean Code.
- Fortalecer a segurança e a observabilidade da aplicação.

---

## 🚀 Fase 1: Refatoração do HealthCheckService (Alta Prioridade)

Esta fase foca na modularização do `HealthCheckService` conforme detalhado em `REFACTORIZATION_PROPOSAL.md`.

- [x] **1.1 Introduzir `CurrentUserProvider`**
    - [x] Criar a interface `CurrentUserProvider`.
    - [x] Implementar `SecurityContextCurrentUserProvider`.
    - [x] Migrar `HealthCheckService` para injetar e usar `CurrentUserProvider`.
    - [x] Ajustar testes unitários existentes para mockar `CurrentUserProvider`.

- [x] **1.2 Extrair `HealthCheckAuthorizationPolicy`**
    - [x] Criar a interface `HealthCheckAuthorizationPolicy`.
    - [x] Implementar `DefaultHealthCheckAuthorizationPolicy` com a lógica de autorização.
    - [x] Migrar `HealthCheckService` para injetar e usar `HealthCheckAuthorizationPolicy`.
    - [x] Criar testes unitários para `DefaultHealthCheckAuthorizationPolicy`.

- [x] **1.3 Extrair `HealthCheckUpdater`**
    - [x] Criar a interface `HealthCheckUpdater`.
    - [x] Implementar `DefaultHealthCheckUpdater` com a lógica de atualização de campos escalares.
    - [x] Migrar `HealthCheckService` para injetar e usar `HealthCheckUpdater`.
    - [x] Criar testes unitários para `DefaultHealthCheckUpdater`.

- [x] **1.4 Extrair `HealthCheckCollectionsManager`**
    - [x] Criar a interface `HealthCheckCollectionsManager`.
    - [x] Implementar `DefaultHealthCheckCollectionsManager` com a lógica de gerenciamento de coleções (limpeza e recriação).
    - [x] Migrar `HealthCheckService` para injetar e usar `HealthCheckCollectionsManager`.
    - [x] Criar testes unitários para `DefaultHealthCheckCollectionsManager`.

- [x] **1.5 Refatorar `HealthCheckService` para Orquestração**
    - [x] Simplificar o método `updateHealthCheck` para orquestrar os novos componentes.
    - [x] Extrair métodos privados auxiliares (`createDetailsFor`, `ensureMapsIdIntegrity`) se necessário.
    - [x] Garantir que `HealthCheckService` esteja enxuto e focado na coordenação.

- [x] **1.6 Padronizar Logging**
    - [x] Remover todas as ocorrências de `System.out.println` no projeto.
    - [x] Substituir por logging via SLF4J (ex: `org.slf4j.Logger`).

---

## 🛠️ Fase 2: Melhorias no AtivoController e Contratos de Serviço

Esta fase visa aprimorar a interface dos controllers e services.

- [x] **2.1 Introduzir Interfaces para Services**
    - [x] Criar a interface `IAtivoService`.
    - [x] Criar a interface `IHealthCheckService`.
    - [x] Migrar `AtivoController` e outros controllers para depender das interfaces (ex: `IAtivoService`) em vez das implementações concretas.

- [x] **2.2 Encapsular Filtros e Paginação no AtivoController**
    - [x] Criar o `record` `AtivoQueryParams` para encapsular os parâmetros de filtro.
    - [x] Atualizar o método `listarTodos` em `AtivoController` para usar `@Valid AtivoQueryParams`.
    - [x] Implementar validação e limite de paginação (ex: `PageRequest.of(..., Math.min(100, pageable.getPageNumber()), ...)`) no controller.

---

## ✅ Fase 3: Cobertura de Testes e Qualidade

Foco em garantir a robustez e a verificação das funcionalidades.

- [x] **3.1 Criar Novos Testes Unitários**
    - [x] Desenvolver testes unitários para os novos componentes criados na Fase 1 (ex: `SecurityContextCurrentUserProviderTest`, `HealthCheckAuthorizationPolicyTest`, etc.).

- [x] **3.2 Garantir Cobertura de Testes Críticos**
    - [x] Revisar e completar a cobertura de testes de integração e unidade conforme o `TEST_PLAN.md`, especialmente para cenários de autorização (RBAC), validação e regras de negócio.
    - [x] Assegurar que os testes existentes (`AtivoControllerIT`) permaneçam verdes após as refatorações.

- [x] **3.3 Revisar e Padronizar `@PreAuthorize`**
    - [x] Verificar a aplicação correta de `@PreAuthorize` em todos os endpoints sensíveis, conforme `ARQUITETURA_PLAN.md` e `TEST_PLAN.md`.

- [x] **3.4 Padronizar Tratamento de Erros**
    - [x] Confirmar o mapeamento completo de exceções de negócio (`EntityNotFoundException`, `AccessDeniedException`, Bean Validation, conflitos) para os códigos HTTP apropriados (400, 403, 404, 409) via `ApplicationControllerAdvice`.
    - [x] Garantir mensagens de erro consistentes e amigáveis.

---

## 📊 Fase 4: Observabilidade e Documentação

Garantir que a aplicação seja monitorável e bem documentada.

- [x] **4.1 Implementar Logging de Auditoria**
    - [x] Adicionar logs de auditoria em pontos críticos (ex: criação/atualização/exclusão de ativos, tentativas de acesso negado).
    - [x] Mascarar dados sensíveis nos logs.

- [x] **4.2 Configurar Métricas Básicas**
    - [x] Integrar uma solução de métricas (ex: Micrometer com Prometheus/Grafana) para monitorar performance e saúde da aplicação.

- [x] **4.3 Documentar APIs com OpenAPI/Swagger**
    - [x] Assegurar que a documentação Swagger reflita corretamente todos os endpoints, seus DTOs, códigos de status esperados e requisitos de segurança (roles).

---

## ♻️ Diretrizes Contínuas (Manutenção e Boas Práticas)

Estas diretrizes devem ser seguidas em todas as fases e no desenvolvimento contínuo do projeto.

- [x] **Manter a Disciplina na Execução:** Seguir este plano de ação rigorosamente, evitando desvios.
- [x] **Revisar e Atualizar Documentação:** Manter `ARQUITETURA_PLAN.md`, `TEST_PLAN.md`, `REFACTORIZATION_PROPOSAL.md`, e `rules.md` atualizados conforme o projeto evolui.
- [x] **Aplicar Princípios de Código:** Continuar aplicando os princípios SOLID, Clean Code e padrões de projeto recomendados em todo o novo código e refatorações.
- [x] **Revisão de Código:** Implementar revisões de código regulares para garantir a aderência às diretrizes e a qualidade do código.
