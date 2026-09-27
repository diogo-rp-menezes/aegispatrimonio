# Changelog

Todas as mudanças relevantes deste projeto são documentadas neste arquivo.
O formato segue [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e o projeto adota [Semantic Versioning](https://semver.org/lang/pt-BR/).

## [Unreleased]
### Added
* Inicialização do projeto Aegis Patrimônio com estrutura base Java (Spring Boot) e frontend JavaScript
* Configuração de dependência `@popperjs/core` para componentes de UI
* Estrutura de pacotes `br.com.aegispatrimonio` com camadas: model, repository, service, mapper, config, controller
* Seeders de dados realistas para ambiente de desenvolvimento (`RealisticDataSeeder`)
* Especificações de consulta dinâmica para manutenções (`ManutencaoSpecification`)
* Serviço de notificação de alertas de uso de recursos (`AlertNotificationService`)
* Mapeadores de entidade para DTO (`AtivoMapper`)
* Cliente HTTP centralizado no frontend (`frontend/src/services/api.js`) com interceptadores de erro e autenticação

### Changed
* Refatoração pendente: quebrar métodos com complexidade ciclomática alta em `RealisticDataSeeder.run`, `AtivoMapper.toDTO`, `ManutencaoSpecification.build`, `AlertNotificationService.checkResourceUsageAlerts` e `api.js request`
* Remoção pendente de chamadas `console.error` e `console.debug` residuais em `frontend/src/services/api.js`
* Correção pendente: implementar corpo do método `Usuario.setUsername` (atualmente stub vazio)

### Deprecated
* Nenhuma funcionalidade marcada para remoção futura no momento

### Removed
* Nenhuma funcionalidade removida nesta versão

### Fixed
* Nenhuma correção de bug registrada ainda

### Security
* Nenhuma correção relacionada a vulnerabilidade registrada ainda

---

## [0.1.0] - 2025-01-15
### Added
* Estrutura inicial do repositório com código-fonte Java (333 arquivos) e frontend (15 arquivos)
* Configuração base do projeto sem scripts de build definidos em `package.json`
* Modelo de domínio para gestão de patrimônio (ativos, manutenções, usuários, alertas)

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