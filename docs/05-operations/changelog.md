# Changelog

Todas as mudanças relevantes deste projeto são documentadas neste arquivo.
O formato segue [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e o projeto adota [Semantic Versioning](https://semver.org/lang/pt-BR/).

## [Unreleased]
### Added
* Estrutura inicial do projeto frontend Aegis com arquitetura baseada em módulos ES6
* Serviço de API centralizado (`frontend/src/services/api.js`) com interceptadores de requisição/resposta
* Integração com @popperjs/core para posicionamento de elementos flutuantes (tooltips, dropdowns, popovers)
* Configuração de build e desenvolvimento (a definir conforme tooling adotado)

### Changed
* Refatoração do módulo `api.js`: separação da lógica de `request` em funções menores para reduzir complexidade ciclomática (atual: 13) <!-- source: server/analyze-pipeline.ts#L45 -->

### Deprecated
* Nenhuma funcionalidade marcada para remoção no momento

### Removed
* Nenhuma funcionalidade removida

### Fixed
* Remoção de chamadas `console.error` e `console.log` residuais em `frontend/src/services/api.js` (linhas 26, 49, 52) antes de build de produção <!-- source: server/analyze-pipeline.ts#L38-L42 -->

### Security
* Nenhuma correção de vulnerabilidade conhecida até o momento

---

## [0.1.0] - 2025-01-15
### Added
* Inicialização do repositório Aegis como aplicação frontend vanilla JavaScript (ESM)
* Adição de @popperjs/core ^2.11.8 como única dependência de produção
* Criação da estrutura de pastas `frontend/src/` com separação por domínio (services, components, utils, styles)
* Implementação do cliente HTTP base com suporte a headers de autenticação, tratamento de erros padronizado e timeout configurável

### Changed
* —

### Fixed
* —

### Security
* —

---

## Convenções deste projeto
* **MAJOR (X):** mudanças incompatíveis com versões anteriores (breaking changes)
* **MINOR (Y):** novas funcionalidades compatíveis com versões anteriores
* **PATCH (Z):** correções de bugs compatíveis com versões anteriores
* Cada entrada deve ser curta, no imperativo, e referenciar o PR/issue relacionado quando possível: `Corrige timeout em uploads grandes (#123)`