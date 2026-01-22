# Plano de Validação da Suíte de Testes

Este documento descreve a metodologia para a execução e verificação da suíte de testes do projeto Aegis Patrimônio, garantindo a saúde do código e a conformidade com as métricas de qualidade estabelecidas.

## 🎯 Objetivos Principais
- Executar a suíte de testes completa (unitários e integração) para estabelecer uma baseline de qualidade.
- Analisar e corrigir quaisquer falhas, inconsistências ou testes "flaky".
- Validar a cobertura de código em relação às metas definidas no `TEST_PLAN.md`.
- Assegurar que o ambiente de CI/CD está corretamente configurado para a execução dos testes.

---

## ⚙️ Fase 1: Preparação e Verificação do Ambiente de Testes

- [x] **1.1 Validar Configuração do Build e Testes**
    - **Agente Responsável:** `DEVOPS_ENGINEER`
    - **Tarefa:** Verificar se o `pom.xml` está configurado corretamente para executar todas as fases de teste (Surefire para unitários, Failsafe para integração) e para gerar o relatório de cobertura do JaCoCo.
    - **Critério de Aceite:** Confirmação de que o comando `mvn verify` executa todas as etapas de teste e gera o relatório do JaCoCo em `target/site/jacoco/html/index.html`.

- [x] **1.2 Validar Ferramentas de Qualidade Estática**
    - **Agente Responsável:** `QUALITY_ENGINEER`
    - **Tarefa:** Assegurar que as ferramentas de análise estática de código (como SonarQube, se aplicável, ou linters locais) estão configuradas e prontas para uso, com as regras alinhadas ao `DEVELOPMENT_MODELS.md`.
    - **Critério de Aceite:** Confirmação de que as regras de qualidade estão prontas para serem aplicadas no pipeline.

---

## 🔬 Fase 2: Execução e Análise dos Testes Unitários

- [x] **2.1 Executar Suíte de Testes Unitários**
    - **Agente Responsável:** `TEST_ENGINEER`
    - **Tarefa:** Executar todos os testes unitários do projeto.
    - **Critério de Aceite:** Relatório de execução indicando o número de testes passados, falhos e ignorados.
    - **Status:** **FALHA NA COMPILAÇÃO DOS TESTES** - `mvn clean verify` falhou na fase `testCompile` devido a erros de construtor em `DefaultHealthCheckCollectionsManagerTest.java`.

- [ ] **2.2 Analisar Cobertura de Código (JaCoCo)**
    - **Agente Responsável:** `QUALITY_ENGINEER`
    - **Tarefa:** Analisar o relatório gerado pelo JaCoCo e comparar a cobertura de código (linha e branch) com a meta de ≥ 80% para a camada de serviço, conforme definido no `TEST_PLAN.md`.
    - **Critério de Aceite:** Relatório de análise de cobertura, destacando áreas com baixa cobertura e confirmando se a meta foi atingida.

- [ ] **2.3 Corrigir Falhas de Testes Unitários (se houver)**
    - **Agentes Responsáveis:** `BACKEND_ENGINEER`, `TEST_ENGINEER`
    - **Tarefa:** Analisar a causa raiz de cada teste unitário que falhou, corrigir o bug no código de produção ou no próprio teste, e re-executar a Etapa 2.1 até que todos os testes passem.
    - **Critério de Aceite:** Todos os testes unitários passando (status GREEN).

---

## 🔄 Fase 3: Execução e Análise dos Testes de Integração

- [ ] **3.1 Executar Suíte de Testes de Integração**
    - **Agente Responsável:** `TEST_ENGINEER`
    - **Tarefa:** Executar todos os testes de integração do projeto (ex: `AtivoControllerIT`).
    - **Critério de Aceite:** Relatório de execução indicando o número de testes passados, falhos e ignorados.

- [ ] **3.2 Analisar e Corrigir Falhas de Testes de Integração (se houver)**
    - **Agentes Responsáveis:** `BACKEND_ENGINEER`, `SECURITY_ENGINEER`, `DATABASE_ARCHITECT`, `TEST_ENGINEER`
    - **Tarefa:** Realizar uma análise colaborativa das falhas. O `BACKEND_ENGINEER` investiga a lógica de negócio, o `SECURITY_ENGINEER` verifica falhas de autorização, o `DATABASE_ARCHITECT` analisa problemas de persistência, e o `TEST_ENGINEER` valida a correção dos testes.
    - **Critério de Aceite:** Todos os testes de integração passando (status GREEN).

---

## 📜 Fase 4: Relatório Final e Consolidação

- [ ] **4.1 Gerar Relatório de Qualidade da Suíte de Testes**
    - **Agente Responsável:** `QUALITY_ENGINEER`
    - **Tarefa:** Consolidar os resultados das Fases 2 e 3 em um relatório final, incluindo: número total de testes, status final, cobertura de código, e quaisquer observações sobre a saúde da suíte de testes.
    - **Critério de Aceite:** Documento de relatório de qualidade gerado e compartilhado.

- [ ] **4.2 Validação Final do Arquiteto**
    - **Agente Responsável:** `ARCHITECT`
    - **Tarefa:** Revisar o relatório de qualidade e declarar a suíte de testes como "saudável" e "confiável" para ser usada como quality gate no pipeline de CI/CD.
    - **Critério de Aceite:** Aprovação formal do `ARCHITECT`.

- [ ] **4.3 Atualizar `CONSOLIDACAO_PLAN.md`**
    - **Agente Responsável:** `ARCHITECT`
    - **Tarefa:** Marcar o item 6 ("Instruir a execução da suíte de testes") como concluído no `CONSOLIDACAO_PLAN.md`.
    - **Critério de Aceite:** `CONSOLIDACAO_PLAN.md` atualizado.

---

## ♻️ Diretrizes Contínuas (Manutenção e Boas Práticas)

Estas diretrizes devem ser seguidas em todas as fases e no desenvolvimento contínuo do projeto.

- **Manter a Disciplina na Execução:** Seguir este plano de ação rigorosamente, evitando desvios.
- **Revisar e Atualizar Documentação:** Manter `ARQUITETURA_PLAN.md`, `TEST_PLAN.md`, `REFACTORIZATION_PROPOSAL.md`, `DEVELOPMENT_MODELS.md`, `TEST_CHECK_STRATEGY.md`, `agents.md` e `rules.md` atualizados conforme o projeto evolui.
- **Execução Real de Testes:** SEMPRE executar os comandos de teste no ambiente real e fornecer os logs de saída para análise, evitando simulações.
- **Aplicar Princípios de Código:** Continuar aplicando os princípios SOLID, Clean Code e padrões de projeto recomendados em todo o novo código e refatorações.
- **Revisão de Código:** Implementar revisões de código regulares para garantir a aderência às diretrizes e a qualidade do código.
