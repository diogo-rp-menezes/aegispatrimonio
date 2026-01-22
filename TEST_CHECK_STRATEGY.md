# Estratégia de Checagem de Testes — Aegis Patrimônio

Este documento descreve a metodologia para a execução e verificação da suíte de testes, garantindo que o processo seja claro, sequencial e controlado. O objetivo é assegurar a qualidade do código e a estabilidade da aplicação após cada ciclo de desenvolvimento ou refatoração.

## 1. Princípios da Estratégia
- **Sequencialidade:** A execução dos testes deve seguir uma ordem lógica, começando pelos testes mais granulares e avançando para os mais abrangentes.
- **Controle:** Cada etapa da checagem deve ter critérios claros de sucesso/falha.
- **Rastreabilidade:** Os resultados devem ser facilmente rastreáveis aos requisitos e às alterações de código.
- **Automação:** Priorizar a automação da execução e da verificação sempre que possível.

## 2. Metodologia de Checagem

A checagem da suíte de testes será dividida em etapas, com dependências claras entre elas.

### 2.1 Etapa 1: Testes Unitários (Unit Tests)
- **Objetivo:** Validar o comportamento de unidades isoladas de código (classes, métodos, funções) sem dependências externas (ou com dependências mockadas).
- **Execução:** Rodar todos os testes localizados em `src/test/java/br/com/aegispatrimonio/service/**`, `src/test/java/br/com/aegispatrimonio/mapper/**`, `src/test/java/br/com/aegispatrimonio/config/**`, etc.
- **Ferramenta:** JUnit 5.
- **Critério de Sucesso:**
    - Todos os testes unitários devem passar (status GREEN).
    - A cobertura de código (linha e branch) para a camada de serviço deve ser ≥ 80% (conforme `TEST_PLAN.md`).
- **Ação em Caso de Falha:** Interromper o processo, analisar as falhas, corrigir os bugs e/ou os testes, e reiniciar a Etapa 1.

### 2.2 Etapa 2: Testes de Integração de Componentes (Component Integration Tests)
- **Objetivo:** Validar a interação entre componentes internos da aplicação (ex: Controller com Service, Service com Repository, Mappers com DTOs e Entidades).
- **Execução:** Rodar testes que utilizam `@SpringBootTest` com `MockMvc` (para Controllers) ou `@DataJpaTest` (para Repositórios), localizados em `src/test/java/br/com/aegispatrimonio/controller/**`, `src/test/java/br/com/aegispatrimonio/repository/**`, etc.
- **Ferramenta:** JUnit 5, Spring Boot Test, MockMvc, Testcontainers (se aplicável).
- **Critério de Sucesso:**
    - Todos os testes de integração de componentes devem passar (status GREEN).
    - Cenários críticos de RBAC, validação e tratamento de erros devem ser validados (conforme `TEST_PLAN.md`).
- **Ação em Caso de Falha:** Interromper o processo, analisar as falhas, corrigir os bugs e/ou os testes, e reiniciar a Etapa 1 (se a falha for em unidade) ou Etapa 2.

### 2.3 Etapa 3: Testes de Integração de Sistema (System Integration Tests / End-to-End)
- **Objetivo:** Validar o fluxo completo da aplicação, incluindo a interação com bancos de dados reais (ou Testcontainers), filas, caches e outros serviços externos (se houver).
- **Execução:** Rodar testes que simulam o comportamento do usuário final através da API, verificando a funcionalidade de ponta a ponta. (Ex: `AtivoControllerIT`)
- **Ferramenta:** JUnit 5, Spring Boot Test, MockMvc, Testcontainers.
- **Critério de Sucesso:**
    - Todos os testes de integração de sistema devem passar (status GREEN).
    - Os cenários de negócio mais importantes e os edge cases devem ser validados (conforme `TEST_PLAN.md`).
- **Ação em Caso de Falha:** Interromper o processo, analisar as falhas, corrigir os bugs e/ou os testes, e reiniciar a Etapa 1, 2 ou 3 conforme a raiz do problema.

## 3. Relatório e Ações Pós-Checagem
- **Relatório:** Gerar um relatório consolidado da execução dos testes, incluindo tempo de execução, número de testes passados/falhos/ignorados e cobertura de código.
- **Análise de Falhas:** Qualquer falha deve ser investigada imediatamente. Priorizar a correção de bugs sobre a desabilitação de testes.
- **Revisão de Cobertura:** Periodicamente, revisar a cobertura de código para garantir que novas funcionalidades e refatorações estejam adequadamente testadas.
- **Documentação:** Manter o `TEST_PLAN.md` e este `TEST_CHECK_STRATEGY.md` atualizados para refletir a evolução da suíte de testes e da metodologia.

## 4. Ferramentas Sugeridas
- **Build Tool:** Maven (para execução de testes e geração de relatórios).
- **Cobertura de Código:** JaCoCo (integrado ao Maven, gera relatórios de cobertura).
- **Ambiente de Teste:** Testcontainers (para bancos de dados e serviços externos em testes de integração).

---

Esta estratégia garante um processo de validação rigoroso e metódico, essencial para a entrega contínua de software de alta qualidade.