# Changelog — AegisPatrimonio

Todas as mudanças relevantes deste projeto são documentadas neste arquivo.
O formato segue [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e o projeto adota [Semantic Versioning](https://semver.org/lang/pt-BR/).

> **Proveniência:** documento gerado a partir da varredura determinística do workspace (AST via `server/analyze-pipeline.ts`), sem exploração livre de arquivos. O nome do projeto deriva do pacote raiz `br.com.aegispatrimonio` (o `package.json` não possui campo `name` preenchido). Não havia artefatos-fonte upstream (`deployment-plan`, `roadmap`) disponíveis para cross-referência de tags de release, sprints e marcos; datas, numeração de versão e PRs/issues inferidos carregam o rótulo **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**.

## [Unreleased]

> Nenhuma alteração concluída foi registrada desde a baseline [0.1.0] até o fechamento deste documento. As pendências técnicas conhecidas do snapshot atual estão rastreadas ao final desta seção, na ausência do artefato `roadmap`.

### Added
* Nenhuma funcionalidade nova concluída registrada desde a baseline [0.1.0].

### Changed
* Nenhuma alteração de comportamento registrada.

### Deprecated
* Nenhuma funcionalidade marcada para remoção.

### Removed
* Nenhuma funcionalidade removida.

### Fixed
* Nenhuma correção de bug concluída registrada.

### Security
* Nenhuma correção de segurança registrada.

### Pendências técnicas conhecidas (varredura AST — rastreadas para a próxima release)
> Os achados abaixo são fatos reais da varredura determinística do codebase. O enquadramento como pendências da próxima release e a priorização são **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**. Ao concluir cada item, mova a entrada para a subseção padrão correspondente, no modo imperativo e com a referência do PR/issue.

**Complexidade ciclomática alta — refatoração (12 funções identificadas na varredura; as 5 de maior severidade abaixo):**
* `AlertNotificationService.checkResourceUsageAlerts` — `src/main/java/br/com/aegispatrimonio/service/AlertNotificationService.java:96`; complexidade 17, a maior do codebase; quebrar em métodos menores.
* `RealisticDataSeeder.run` — `src/main/java/br/com/aegispatrimonio/config/seeder/RealisticDataSeeder.java:34`; complexidade 15; quebrar em métodos menores.
* `AtivoMapper.toDTO` — `src/main/java/br/com/aegispatrimonio/mapper/AtivoMapper.java:15`; complexidade 14; quebrar em métodos menores.
* `ManutencaoSpecification.build` — `src/main/java/br/com/aegispatrimonio/repository/ManutencaoSpecification.java:26`; complexidade 14; quebrar em métodos menores.
* `request` — `frontend/src/services/api.js:54`; complexidade 13; quebrar em funções menores.

**Higiene de código:**
* Remover chamada `console.error(...)` residual — `frontend/src/services/api.js:44`.
* Remover chamada `console.debug(...)` residual — `frontend/src/services/api.js:107`.
* Implementar o corpo do método `Usuario.setUsername` — `src/main/java/br/com/aegispatrimonio/model/Usuario.java:86`; atualmente com corpo vazio (stub), chamadas ao método não produzem efeito.

**Performance:**
* Avaliar o TODO (perf) em `AtivoService` — `src/main/java/br/com/aegispatrimonio/service/AtivoService.java:119`; o caminho carrega até 1000 candidatos (id+nome) e faz ranking; definir estratégia de paginação/ranking antes da próxima release.

---

## [0.1.0] - 2025-06-01 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]

> Baseline inicial inferida. Premissas adotadas: (1) o workspace não expõe tags, histórico de commits nem campo `version` preenchido — adotou-se a numeração 0.1.0 (pré-1.0, conforme SemVer) para a entrega inicial da base de código; (2) a data é hipotética — substituir pela data real do commit/tag de baseline; (3) a versão exata de introdução de cada módulo não é rastreável, por isso toda a base atual é atribuída a esta baseline. Escopo do snapshot: 350 arquivos de código (335 `.java`, 15 `.js`), 1.268 funções, 344 classes e 27.537 linhas de código (LOC).

### Added
* Adiciona estrutura backend da aplicação web em `src/main/java/br/com/aegispatrimonio` — 335 arquivos Java cobrindo modelos, serviços, mappers, repositórios, especificações de consulta, seeders e configuração.
* Adiciona serviço de domínio `AtivoService` com operações de gestão de ativos, incluindo caminho de listagem com ranking de candidatos.
* Adiciona serviço `AlertNotificationService` com verificação e notificação de alertas, incluindo alertas de uso de recursos (`checkResourceUsageAlerts`).
* Adiciona especificação `ManutencaoSpecification` para construção dinâmica de filtros de consulta de manutenções (método `build`).
* Adiciona mapper `AtivoMapper` com conversão de entidades de ativos para DTOs (método `toDTO`).
* Adiciona seeder `RealisticDataSeeder` para carga de dados realistas no ambiente (`config/seeder`).
* Adiciona modelo `Usuario` para representação de usuários do sistema.
* Adiciona frontend em `frontend/` com 15 arquivos JavaScript, incluindo o cliente de API `frontend/src/services/api.js` responsável pela comunicação com o backend.
* Adiciona dependência de produção `@popperjs/core@^2.11.8`.

### Changed
* N/A — release inicial.

### Deprecated
* N/A — release inicial.

### Removed
* N/A — release inicial.

### Fixed
* N/A — release inicial.

### Security
* N/A — release inicial.

---

## Lacunas de informação e itens a verificar
* Nenhuma versão lançada, tag ou histórico de commits foi encontrado na varredura do workspace — validar a baseline [0.1.0] contra o repositório antes de publicar este histórico.
* `package.json` sem campos `name`/`version` e sem scripts definidos — preencher antes da primeira release publicada.
* Nenhum motor de banco de dados nem ORM/query builder declarados nas dependências — confirmar a estratégia de persistência antes de documentar mudanças de schema em versões futuras.
* Artefatos `deployment-plan` e `roadmap` indisponíveis no catálogo de dependências do projeto — ao ficarem disponíveis, cross-referenciar tags de release e marcos de sprint nas entradas correspondentes.
* A seção **Security** está vazia por ausência de achados na varredura — não reflete auditoria de segurança concluída.

---

## Convenções deste projeto
* **MAJOR (X):** mudanças incompatíveis com versões anteriores (breaking changes)
* **MINOR (Y):** novas funcionalidades compatíveis com versões anteriores
* **PATCH (Z):** correções de bugs compatíveis com versões anteriores
* Cada entrada deve ser curta, no imperativo, e referenciar o PR/issue relacionado quando possível: `Corrige timeout em uploads grandes (#123)`
* Enquanto o artefato `roadmap` não estiver disponível, pendências técnicas identificadas pela varredura AST (`server/analyze-pipeline.ts`) são rastreadas em [Unreleased]; quando disponível, cross-referenciar sprints e marcos de versão.
* Estratégias de release e tags devem ser cross-referenciadas com o artefato `deployment-plan` quando disponível no catálogo.
* Datas, versões, hashes de commit e PRs inferidos devem carregar o rótulo [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] com as premissas explícitas.