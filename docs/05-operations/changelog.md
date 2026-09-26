# Changelog

Todas as mudanças relevantes deste projeto são documentadas neste arquivo.
O formato segue [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e o projeto adota [Semantic Versioning](https://semver.org/lang/pt-BR/).

## [Unreleased]
### Added
* Estrutura inicial do frontend com 15 módulos JavaScript
* Integração com @popperjs/core ^2.11.8 para posicionamento de elementos (tooltips, dropdowns, popovers)
* Camada de serviços de API em `frontend/src/services/api.js` com função `request` para chamadas HTTP

### Changed
* —

### Deprecated
* —

### Removed
* —

### Fixed
* —

### Security
* —

---

## [0.1.0] - 2025-01-15
### Added
* Inicialização do projeto Aegis1 (frontend)
* Configuração de dependência de produção: @popperjs/core ^2.11.8
* Módulos de frontend organizados em `frontend/src/`

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

---

## Observações técnicas conhecidas (pendentes de resolução)
* [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Remover chamadas `console.error` (linha 26) e `console.log` (linhas 49, 52) em `frontend/src/services/api.js` antes de deploy em produção
* [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Refatorar função `request` em `frontend/src/services/api.js:36` (complexidade ciclomática 13) em funções menores para melhorar manutenibilidade
* [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Definir `name`, `version` e `scripts` no `package.json` (atualmente ausentes)