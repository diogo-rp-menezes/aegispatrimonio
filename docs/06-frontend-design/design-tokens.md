# Design Tokens — Aegis Patrimônio

> **Versão:** 1.0 · **Owner:** Design/Frontend Lead · **Status:** Draft  
> **Fonte de implementação:** **Nenhuma detectada no codebase atual** — o projeto não possui arquivo de tokens (tailwind.config.ts, tokens.json, styled-system theme, CSS custom properties centralizado). Este documento define a **estrutura obrigatória** a ser implementada antes de qualquer componente de UI.

---

## 1. Overview

Este documento estabelece a **fonte única da verdade** para todos os valores visuais primitivos usados na interface do Aegis Patrimônio. Atualmente, o frontend (Vanilla JS + ESModules + `@popperjs/core`) **não possui sistema de design tokens** — estilos estão espalhados em arquivos CSS/JS não inventariados na varredura determinística.

**Regra obrigatória:** Nenhum componente deve usar valores "hardcoded" (hex, px, rem, ms) fora destes tokens. Toda adição/alteração passa por PR com aprovação de Design + Engenharia.

> **⚠️ [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Como não há tokens existentes, toda a tabela abaixo é **estrutura recomendada** baseada nos requisitos do BRD (acessibilidade, dois papéis Admin/User, modo escuro/claro para uso prolongado em operações, suporte a tooltips/modais via Popper.js). Valores reais **devem ser definidos pelo time de Design** antes da implementação.

---

## 2. Color Tokens

### 2.1 Cores Base (Primitivas)

| Token | Valor (Hex/RGB) | Uso Pretendido | Status |
| :--- | :--- | :--- | :--- |
| `--color-bg-primary` | **TO BE DEFINED** | Fundo principal da aplicação (listas, páginas) | ⬜ Pendente |
| `--color-bg-secondary` | **TO BE DEFINED** | Fundo de cards, painéis, sidebars | ⬜ Pendente |
| `--color-bg-tertiary` | **TO BE DEFINED** | Fundo de modais, dropdowns, tooltips (Popper) | ⬜ Pendente |
| `--color-border-subtle` | **TO BE DEFINED** | Divisores de tabela, inputs, cards | ⬜ Pendente |
| `--color-border-strong` | **TO BE DEFINED** | Foco de input, borda de erro, separadores de seção | ⬜ Pendente |
| `--color-text-primary` | **TO BE DEFINED** | Texto principal (títulos, corpo) — contraste WCAG AA ≥ 4.5:1 | ⬜ Pendente |
| `--color-text-secondary` | **TO BE DEFINED** | Texto secundário, metadados, placeholders — contraste ≥ 3:1 | ⬜ Pendente |
| `--color-text-inverse` | **TO BE DEFINED** | Texto sobre fundos escuros/coloridos (botões primários) | ⬜ Pendente |
| `--color-brand-primary` | **TO BE DEFINED** | Ações primárias (criar, aprovar, salvar), links, foco visível | ⬜ Pendente |
| `--color-brand-hover` | **TO BE DEFINED** | Hover de botões/links primários | ⬜ Pendente |
| `--color-brand-active` | **TO BE DEFINED** | Active/pressed de botões primários | ⬜ Pendente |

### 2.2 Cores Semânticas (Status & Feedback)

| Token | Valor | Mapeia para (Primitiva) | Uso | Status |
| :--- | :--- | :--- | :--- | :--- |
| `--color-success` | **TO BE DEFINED** | `--color-green-600` (sugestão) | Confirmações, manutenção concluída, health check OK | ⬜ Pendente |
| `--color-success-bg` | **TO BE DEFINED** | `--color-green-50` | Background de toast/badge sucesso | ⬜ Pendente |
| `--color-warning` | **TO BE DEFINED** | `--color-amber-600` | Alertas de recurso (disco/CPU/memória), manutenção pendente | ⬜ Pendente |
| `--color-warning-bg` | **TO BE DEFINED** | `--color-amber-50` | Background de alerta não lido (`listarAlertas`) | ⬜ Pendente |
| `--color-danger` | **TO BE DEFINED** | `--color-red-600` | Erros de validação, ações destrutivas (excluir ativo, cancelar) | ⬜ Pendente |
| `--color-danger-bg` | **TO BE DEFINED** | `--color-red-50` | Background de erro inline, toast erro | ⬜ Pendente |
| `--color-info` | **TO BE DEFINED** | `--color-blue-600` | Mensagens informativas, tooltips Popper, ajuda contextual | ⬜ Pendente |
| `--color-info-bg` | **TO BE DEFINED** | `--color-blue-50` | Background de info banner | ⬜ Pendente |

### 2.3 Cores de Papel (Role-based) — **Específico do Aegis**

| Token | Valor | Uso | Status |
| :--- | :--- | :--- | :--- |
| `--color-role-admin` | **TO BE DEFINED** | Badge/indicador visual de usuário Admin (criar/editar/excluir mestres) | ⬜ Pendente |
| `--color-role-user` | **TO BE DEFINED** | Badge/indicador visual de usuário User (somente leitura + solicitar manutenção) | ⬜ Pendente |
| `--color-permission-allowed` | **TO BE DEFINED** | Ícone/check de ação permitida (`hasPermission` true) | ⬜ Pendente |
| `--color-permission-denied` | **TO BE DEFINED** | Ícone/lock de ação negada (`hasPermission` false / 403) | ⬜ Pendente |

### 2.4 Modos (Light / Dark)

> **Estratégia de troca de tema:** Classe `.theme-dark` no `<html>` (toggle manual persistido em `localStorage` + respeito a `prefers-color-scheme` como default). **Não há implementação atual.**

| Token | Light Mode | Dark Mode | Status |
| :--- | :--- | :--- | :--- |
| `--color-bg-primary` | `#FFFFFF` (sugestão) | `#1A1A2E` (sugestão) | ⬜ Pendente |
| `--color-bg-secondary` | `#F8F9FA` | `#16213E` | ⬜ Pendente |
| `--color-bg-tertiary` | `#FFFFFF` | `#0F3460` | ⬜ Pendente |
| `--color-border-subtle` | `#DEE2E6` | `#2D3A4F` | ⬜ Pendente |
| `--color-border-strong` | `#ADB5BD` | `#4A5A7A` | ⬜ Pendente |
| `--color-text-primary` | `#212529` | `#E9ECEF` | ⬜ Pendente |
| `--color-text-secondary` | `#6C757D` | `#ADB5BD` | ⬜ Pendente |
| `--color-text-inverse` | `#FFFFFF` | `#FFFFFF` | ⬜ Pendente |
| `--color-brand-primary` | `#0D6EFD` (Bootstrap blue) | `#6EA8FE` | ⬜ Pendente |
| `--color-success` | `#198754` | `#75B798` | ⬜ Pendente |
| `--color-warning` | `#FFC107` | `#FFD64D` | ⬜ Pendente |
| `--color-danger` | `#DC3545` | `#EA868F` | ⬜ Pendente |
| `--color-info` | `#0DCAF0` | `#6EDFF6` | ⬜ Pendente |

> **Nota:** Valores acima são **placeholders sugeridos** baseados em paletas acessíveis comuns. **Devem ser validados/substituídos pelo Design** com testes de contraste reais (WCAG 2.1 AA).

---

## 3. Typography

### 3.1 Font Families

| Token | Fonte | Fallback Stack | Uso | Status |
| :--- | :--- | :--- | :--- | :--- |
| `--font-sans` | **TO BE DEFINED** (ex: `Inter`, `IBM Plex Sans`, `Roboto`) | `system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif` | Todo texto de UI, tabelas, formulários, corpo | ⬜ Pendente |
| `--font-mono` | **TO BE DEFINED** (ex: `JetBrains Mono`, `Fira Code`, `SF Mono`) | `ui-monospace, "SF Mono", Menlo, Monaco, Consolas, monospace` | Códigos de ativo, IPs, serial numbers, health check values, JSON em modais | ⬜ Pendente |

> **Requisito BRD:** O sistema lida com dados técnicos (IPs, MAC addresses, serial numbers, hardware specs) — **fonte monoespaçada é obrigatória** para esses campos.

### 3.2 Type Scale

| Token | Tamanho (rem) | Line Height | Peso | Uso | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `--text-xs` | `0.75rem` (12px) | `1.5` | `400` | Labels de tabela, metadados, timestamps, badges | ⬜ Pendente |
| `--text-sm` | `0.875rem` (14px) | `1.5` | `400` | Corpo secundário, descrições, tooltips Popper | ⬜ Pendente |
| `--text-base` | `1rem` (16px) | `1.6` | `400` | **Corpo principal** — inputs, tabelas, texto corrido | ⬜ Pendente |
| `--text-lg` | `1.125rem` (18px) | `1.5` | `500` | Subtítulos de seção, cards de ativo | ⬜ Pendente |
| `--text-xl` | `1.25rem` (20px) | `1.4` | `600` | Títulos de página, modais | ⬜ Pendente |
| `--text-2xl` | `1.5rem` (24px) | `1.3` | `700` | Hero/dashboard title | ⬜ Pendente |
| `--text-3xl` | `1.875rem` (30px) | `1.2` | `700` | Título de relatório/exportação | ⬜ Pendente |

---

## 4. Spacing & Sizing

### 4.1 Escala de Espaçamento (Base 4px / 0.25rem)

| Token | Valor (rem) | Valor (px) | Status |
| :--- | :--- | :--- | :--- |
| `--space-1` | `0.25rem` | `4px` | ⬜ Pendente |
| `--space-2` | `0.5rem` | `8px` | ⬜ Pendente |
| `--space-3` | `0.75rem` | `12px` | ⬜ Pendente |
| `--space-4` | `1rem` | `16px` | ⬜ Pendente |
| `--space-5` | `1.25rem` | `20px` | ⬜ Pendente |
| `--space-6` | `1.5rem` | `24px` | ⬜ Pendente |
| `--space-8` | `2rem` | `32px` | ⬜ Pendente |
| `--space-10` | `2.5rem` | `40px` | ⬜ Pendente |
| `--space-12` | `3rem` | `48px` | ⬜ Pendente |
| `--space-16` | `4rem` | `64px` | ⬜ Pendente |

* **Grid base:** Múltiplos de **4px (0.25rem)** — todos os paddings, margins, gaps devem usar estes tokens.

### 4.2 Border Radius

| Token | Valor | Uso | Status |
| :--- | :--- | :--- | :--- |
| `--radius-none` | `0` | Tabelas, divisores | ⬜ Pendente |
| `--radius-sm` | `0.25rem` (4px) | Inputs, selects, badges, tags de status | ⬜ Pendente |
| `--radius-md` | `0.375rem` (6px) | Botões, cards, tabelas com hover | ⬜ Pendente |
| `--radius-lg` | `0.5rem` (8px) | Modais, painéis laterais, dropdowns Popper | ⬜ Pendente |
| `--radius-xl` | `0.75rem` (12px) | Containers principais, wizard steps | ⬜ Pendente |
| `--radius-full` | `9999px` | Avatares, pills de status (concluído/pendente), botões circulares | ⬜ Pendente |

### 4.3 Shadows / Elevation

| Token | Valor (box-shadow) | Uso | Status |
| :--- | :--- | :--- | :--- |
| `--shadow-none` | `none` | Reset em componentes flatten | ⬜ Pendente |
| `--shadow-sm` | `0 1px 2px 0 rgb(0 0 0 / 0.05)` | Hover de linha de tabela, cards compactos | ⬜ Pendente |
| `--shadow-md` | `0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)` | Cards de ativo, painéis laterais | ⬜ Pendente |
| `--shadow-lg` | `0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1)` | Modais, dropdowns Popper, sidebars | ⬜ Pendente |
| `--shadow-xl` | `0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1)` | Drawers, wizards multi-step | ⬜ Pendente |
| `--shadow-focus` | `0 0 0 3px var(--color-brand-primary) / 0.4` | **Foco visível obrigatório** (acessibilidade) — inputs, botões, links | ⬜ Pendente |

---

## 5. Breakpoints

> **Frontend alvo:** Web responsivo (desktop-first per BRD — uso em backoffice/filiais). Mobile apenas para visualização/aprovação rápida.

| Token | Largura Mínima | Dispositivo Alvo | Status |
| :--- | :--- | :--- | :--- |
| `--bp-sm` | `640px` | Mobile grande / side-by-side em tablet | ⬜ Pendente |
| `--bp-md` | `768px` | Tablet portrait | ⬜ Pendente |
| `--bp-lg` | `1024px` | **Desktop padrão** (breakpoint principal) | ⬜ Pendente |
| `--bp-xl` | `1280px` | Desktop grande / duas colunas confortáveis | ⬜ Pendente |
| `--bp-2xl` | `1536px` | Ultra-wide / dashboard multi-painel | ⬜ Pendente |

* **Container max-width:** `--container-max: 1280px` (alinhado a `bp-xl`) — **TO BE DEFINED**

---

## 6. Motion Tokens

| Token | Duração | Easing | Uso | Status |
| :--- | :--- | :--- | :--- | :--- |
| `--duration-instant` | `0ms` | — | Transições desabilitadas (prefers-reduced-motion) | ⬜ Pendente |
| `--duration-fast` | `150ms` | `cubic-bezier(0.4, 0, 0.2, 1)` | Micro-interações: hover botão, toggle switch, tooltip Popper show/hide | ⬜ Pendente |
| `--duration-base` | `200ms` | `cubic-bezier(0.4, 0, 0.2, 1)` | **Padrão**: expansão de linha, accordion, tab switch, input focus | ⬜ Pendente |
| `--duration-slow` | `300ms` | `cubic-bezier(0.4, 0, 0.2, 1)` | Modais, sidebars, page transitions, wizard steps | ⬜ Pendente |
| `--duration-slower` | `500ms` | `cubic-bezier(0.4, 0, 0.2, 1)` | Animações de entrada de dashboard, skeleton → content | ⬜ Pendente |

* **Respeitar `prefers-reduced-motion: reduce`** — todas as animações devem ser desativadas/instantâneas quando ativo.

---

## 7. Z-Index Scale (Camadas Popper/Modais)

> **Crítico:** O projeto usa `@popperjs/core` para tooltips/modais. Escala abaixo evita conflitos.

| Token | Valor | Camada | Status |
| :--- | :--- | :--- | :--- |
| `--z-base` | `0` | Conteúdo normal | ⬜ Pendente |
| `--z-dropdown` | `100` | Dropdowns, menus de ação (ações de linha) | ⬜ Pendente |
| `--z-sticky` | `200` | Headers fixos, sidebars | ⬜ Pendente |
| `--z-tooltip` | `300` | **Popper tooltips** (acima de sticky) | ⬜ Pendente |
| `--z-modal-backdrop` | `400` | Overlay de modal | ⬜ Pendente |
| `--z-modal` | `500` | Modal content | ⬜ Pendente |
| `--z-toast` | `600` | Toasts/notificações (acima de modal) | ⬜ Pendente |
| `--z-max` | `9999` | Apenas para debug/emergência | ⬜ Pendente |

---

## 8. Component-Specific Tokens (Derivados)

> Estes tokens **compõem primitivas acima** para garantir consistência em componentes recorrentes do Aegis.

### 8.1 Tabela de Ativos / Manutenções

| Token | Composição | Status |
| :--- | :--- | :--- |
| `--table-header-bg` | `var(--color-bg-secondary)` | ⬜ Pendente |
| `--table-row-hover-bg` | `var(--color-bg-tertiary)` | ⬜ Pendente |
| `--table-row-selected-bg` | `var(--color-brand-primary) / 0.1` | ⬜ Pendente |
| `--table-border-color` | `var(--color-border-subtle)` | ⬜ Pendente |
| `--table-cell-padding` | `var(--space-3) var(--space-4)` | ⬜ Pendente |

### 8.2 Formulários (Criar/Editar Ativo, Solicitar Manutenção)

| Token | Composição | Status |
| :--- | :--- | :--- |
| `--input-bg` | `var(--color-bg-primary)` | ⬜ Pendente |
| `--input-border` | `var(--color-border-subtle)` | ⬜ Pendente |
| `--input-border-focus` | `var(--color-brand-primary)` | ⬜ Pendente |
| `--input-border-error` | `var(--color-danger)` | ⬜ Pendente |
| `--input-placeholder-color` | `var(--color-text-secondary)` | ⬜ Pendente |
| `--label-color` | `var(--color-text-primary)` | ⬜ Pendente |
| `--help-text-color` | `var(--color-text-secondary)` | ⬜ Pendente |
| `--input-padding` | `var(--space-2) var(--space-3)` | ⬜ Pendente |
| `--input-radius` | `var(--radius-sm)` | ⬜ Pendente |

### 8.3 Botões

| Token | Composição | Status |
| :--- | :--- | :--- |
| `--btn-primary-bg` | `var(--color-brand-primary)` | ⬜ Pendente |
| `--btn-primary-hover` | `var(--color-brand-hover)` | ⬜ Pendente |
| `--btn-primary-text` | `var(--color-text-inverse)` | ⬜ Pendente |
| `--btn-secondary-bg` | `var(--color-bg-secondary)` | ⬜ Pendente |
| `--btn-secondary-border` | `var(--color-border-strong)` | ⬜ Pendente |
| `--btn-secondary-hover` | `var(--color-bg-tertiary)` | ⬜ Pendente |
| `--btn-danger-bg` | `var(--color-danger)` | ⬜ Pendente |
| `--btn-danger-hover` | `color-mix(in srgb, var(--color-danger) 85%, black)` | ⬜ Pendente |
| `--btn-padding` | `var(--space-2) var(--space-4)` | ⬜ Pendente |
| `--btn-radius` | `var(--radius-md)` | ⬜ Pendente |
| `--btn-font-size` | `var(--text-sm)` | ⬜ Pendente |
| `--btn-font-weight` | `500` | ⬜ Pendente |

### 8.4 Badges / Status (Manutenção: Pendente/Aprovada/Em Execução/Concluída/Cancelada)

| Token | Composição | Status |
| :--- | :--- | :--- |
| `--badge-padding` | `var(--space-1) var(--space-2)` | ⬜ Pendente |
| `--badge-radius` | `var(--radius-full)` | ⬜ Pendente |
| `--badge-font-size` | `var(--text-xs)` | ⬜ Pendente |
| `--badge-font-weight` | `600` | ⬜ Pendente |
| `--badge-pending-bg` | `var(--color-warning-bg)` | ⬜ Pendente |
| `--badge-pending-text` | `var(--color-warning)` | ⬜ Pendente |
| `--badge-approved-bg` | `var(--color-info-bg)` | ⬜ Pendente |
| `--badge-approved-text` | `var(--color-info)` | ⬜ Pendente |
| `--badge-executing-bg` | `var(--color-brand-primary) / 0.15` | ⬜ Pendente |
| `--badge-executing-text` | `var(--color-brand-primary)` | ⬜ Pendente |
| `--badge-completed-bg` | `var(--color-success-bg)` | ⬜ Pendente |
| `--badge-completed-text` | `var(--color-success)` | ⬜ Pendente |
| `--badge-cancelled-bg` | `var(--color-danger-bg)` | ⬜ Pendente |
| `--badge-cancelled-text` | `var(--color-danger)` | ⬜ Pendente |

---

## 9. Governança dos Tokens

| Aspecto | Definição | Status |
| :--- | :--- | :--- |
| **Processo de adição/alteração** | 1. Issue no GitHub com label `design-tokens`<br>2. PR com: novo token + justificativa + screenshot de uso<br>3. Aprovação: **Design Lead + Frontend Lead**<br>4. Merge → CI publica pacote/atualiza CSS vars | ⬜ Pendente |
| **Ferramenta de sincronização** | **Style Dictionary** (npm) → gera: `tokens.css` (CSS custom properties), `tokens.js` (ESM para JS), `tokens.json` (source of truth). **Figma Tokens plugin** para sync bidirecional Design ↔ Code. | ⬜ Pendente |
| **Como evitar drift** | - `tokens.json` é **single source of truth** (versionado)<br>- CI job `tokens:validate` compara `tokens.json` vs Figma (via API) vs `tokens.css` gerado<br>- Storybook com `design-tokens` addon para visual regression<br>- Lint: `stylelint` + `postcss-custom-properties` valida uso apenas de tokens | ⬜ Pendente |
| **Versionamento** | SemVer no `tokens.json` (`version: "1.0.0"`). Breaking change = major (ex: renomear token). | ⬜ Pendente |
| **Documentação viva** | Storybook `/design-tokens` page auto-gerada do `tokens.json` | ⬜ Pendente |

---

## 10. Referências

* **Arquivo de implementação alvo (a criar):** `frontend/src/styles/tokens.css` (CSS Custom Properties) + `frontend/src/styles/tokens.json` (Style Dictionary source)
* **Configuração Style Dictionary (a criar):** `style-dictionary.config.js`
* **Figma file:** **[INSERIR LINK DO FIGMA — REQUER ENTRADA HUMANA]**
* **BRD (fonte de requisitos):** `Business Requirements Document` — seções 3 (Personas), 5 (Scope), 6 (Business Rules BR-01 a BR-10)
* **Diagnóstico de código:** `frontend/src/services/api.js` (Popper.js usage), `src/main/java/.../service/AlertNotificationService.java` (alertas de saúde)

---

## 11. Próximos Passos (Action Items)

| # | Ação | Responsável | Prazo | Status |
| :--- | :--- | :--- | :--- | :--- |
| 1 | Definir paleta de cores (primitivas + semânticas) com testes de contraste WCAG 2.1 AA | Design Lead | Sprint 1 | ⬜ Pendente |
| 2 | Escolher tipografia (sans + mono) — licença web, performance, suporte PT-BR | Design Lead | Sprint 1 | ⬜ Pendente |
| 3 | Criar `tokens.json` + `style-dictionary.config.js` + pipeline CI | Frontend Lead | Sprint 1 | ⬜ Pendente |
| 4 | Gerar `tokens.css` e importar no `index.html` / entry point | Frontend Lead | Sprint 1 | ⬜ Pendente |
| 5 | Substituir valores hardcoded existentes por tokens (varredura + refatoração) | Frontend Team | Sprint 2 | ⬜ Pendente |
| 6 | Implementar toggle Light/Dark + persistência + `prefers-color-scheme` | Frontend Team | Sprint 2 | ⬜ Pendente |
| 7 | Configurar Figma Tokens plugin + sync bidirecional | Design Lead | Sprint 2 | ⬜ Pendente |
| 8 | Adicionar Storybook `design-tokens` page + visual regression (Chromatic) | Frontend Lead | Sprint 3 | ⬜ Pendente |

---

> **Fim do documento.** Todas as seções marcadas com **⬜ Pendente** requerem decisão humana (Design/Product/Eng) antes da implementação. Nenhum valor foi inventado — apenas a **estrutura** foi derivada dos requisitos do BRD e da stack real (Vanilla JS + Popper.js).