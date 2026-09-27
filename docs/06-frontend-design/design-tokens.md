# Design Tokens — Aegis Patrimônio

> **Versão:** 1.0 · **Owner:** Design/Frontend Lead · **Status:** Draft  
> **Fonte de implementação:** *Nenhuma implementação de tokens encontrada no codebase atual* — este documento propõe a estrutura inicial baseada nos requisitos do BRD e na stack tecnológica (JavaScript vanilla + @popperjs/core). Todos os valores abaixo são **hipóteses iniciais** e requerem validação humana.

---

## 1. Overview

Este documento define a **fonte única da verdade** para todos os valores visuais primitivos (cores, tipografia, espaçamento, bordas, sombras, breakpoints, motion) usados na interface do **Aegis Patrimônio**.  

**Estado atual:** O diagnóstico determinístico do repositório (348 arquivos Java, 15 arquivos JS) **não revelou nenhum arquivo de design tokens** (sem `tailwind.config.ts`, `tokens.json`, `theme.js`, `styled-system`, CSS custom properties centralizadas, ou plugin Figma Tokens). O frontend usa JavaScript vanilla com `@popperjs/core` apenas para posicionamento de tooltips/dropdowns.  

**Objetivo:** Estabelecer um sistema de tokens antes que valores "hardcoded" proliferem nos 15 arquivos JS e eventuais arquivos CSS/HTML. Nenhum componente deve usar valores literais fora destes tokens após a aprovação deste documento.

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Todos os valores, nomes de tokens e decisões abaixo são propostas baseadas em boas práticas para sistemas enterprise de gestão de ativos (dashboard denso, tabelas, formulários, modais, alertas de saúde de hardware). Não há nenhuma decisão de design pré-existente no repositório para derivar.

---

## 2. Color Tokens

### 2.1 Cores Base (Primitivas)

| Token | Valor (Hex) | Valor (RGB) | Uso Pretendido |
| :--- | :--- | :--- | :--- |
| `--color-bg-primary` | `#FFFFFF` | `255, 255, 255` | Fundo principal da aplicação (light mode) |
| `--color-bg-secondary` | `#F8F9FA` | `248, 249, 250` | Fundo de cards, painéis, sidebars |
| `--color-bg-tertiary` | `#E9ECEF` | `233, 236, 239` | Fundo de inputs desabilitados, hover sutil |
| `--color-border-light` | `#DEE2E6` | `222, 226, 230` | Bordas padrão, divisores de tabela |
| `--color-border-medium` | `#CED4DA` | `206, 212, 218` | Bordas de inputs focados, cards selecionados |
| `--color-text-primary` | `#212529` | `33, 37, 41` | Texto principal (títulos, corpo) |
| `--color-text-secondary` | `#495057` | `73, 80, 87` | Texto secundário, labels, metadados |
| `--color-text-muted` | `#6C757D` | `108, 117, 125` | Placeholders, texto desabilitado, timestamps |
| `--color-accent-primary` | `#2563EB` | `37, 99, 235` | Ações primárias (botões, links, foco) |
| `--color-accent-hover` | `#1D4ED8` | `29, 78, 216` | Hover de ações primárias |
| `--color-accent-light` | `#DBEAFE` | `219, 234, 254` | Background de badges/info primários |

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Paleta baseada em tons neutros azul-cinza (slate/blue) típicos de sistemas B2B enterprise. Nenhuma identidade visual corporativa foi encontrada no repositório.

### 2.2 Cores Semânticas

| Token | Valor (Hex) | Mapeia para (Primitiva) | Uso |
| :--- | :--- | :--- | :--- |
| `--color-success` | `#059669` | `--color-green-600` | Confirmações, status "Concluído", health check OK |
| `--color-success-light` | `#D1FAE5` | `--color-green-100` | Background de badges sucesso, toast sucesso |
| `--color-warning` | `#D97706` | `--color-amber-600` | Alertas de atenção, manutenção pendente, disco > 85% |
| `--color-warning-light` | `#FEF3C7` | `--color-amber-100` | Background de badges aviso, toast aviso |
| `--color-danger` | `#DC2626` | `--color-red-600` | Erros, ações destrutivas (excluir ativo), health check crítico |
| `--color-danger-light` | `#FEE2E2` | `--color-red-100` | Background de badges erro, toast erro, inline errors |
| `--color-info` | `#0891B2` | `--color-cyan-600` | Mensagens informativas, health check "info", tooltips |
| `--color-info-light` | `#CFFAFE` | `--color-cyan-100` | Background de badges info |

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Cores semânticas mapeadas para primitivas de uma escala estendida (green/amber/red/cyan 50–900) que **não está definida aqui**. Requer definição da escala completa ou adoção de paleta existente (ex: Tailwind, Radix, shadcn).

### 2.3 Modos (Light / Dark)

| Token | Light Mode | Dark Mode |
| :--- | :--- | :--- |
| `--color-bg-primary` | `#FFFFFF` | `#111827` |
| `--color-bg-secondary` | `#F8F9FA` | `#1F2937` |
| `--color-bg-tertiary` | `#E9ECEF` | `#374151` |
| `--color-border-light` | `#DEE2E6` | `#4B5563` |
| `--color-border-medium` | `#CED4DA` | `#6B7280` |
| `--color-text-primary` | `#212529` | `#F9FAFB` |
| `--color-text-secondary` | `#495057` | `#E5E7EB` |
| `--color-text-muted` | `#6C757D` | `#9CA3AF` |
| `--color-accent-primary` | `#2563EB` | `#3B82F6` |
| `--color-accent-hover` | `#1D4ED8` | `#60A5FA` |
| `--color-accent-light` | `#DBEAFE` | `#1E3A5F` |
| `--color-success` | `#059669` | `#10B981` |
| `--color-warning` | `#D97706` | `#F59E0B` |
| `--color-danger` | `#DC2626` | `#EF4444` |
| `--color-info` | `#0891B2` | `#06B6D4` |

* **Estratégia de troca de tema:** Classe `.dark` no `:root` (ou `html`) + `prefers-color-scheme` como fallback + toggle manual persistido em `localStorage`. Implementação via CSS custom properties no `:root` e `:root.dark`.

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Valores de dark mode são estimativas baseadas em contraste WCAG AA. Não há preferência de tema no BRD. Requer decisão de produto: dark mode é MVP ou fase 2?

---

## 3. Typography

### 3.1 Font Families

| Token | Fonte | Fallback Stack | Uso |
| :--- | :--- | :--- | :--- |
| `--font-sans` | `'Inter', system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif` | System UI stack | Todo o texto da UI: corpo, títulos, botões, inputs, tabelas |
| `--font-mono` | `'JetBrains Mono', 'Fira Code', 'SF Mono', Menlo, Monaco, Consolas, monospace` | Monospace system | Código, IDs de ativos, números de série, IPs, métricas técnicas, logs |

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — `Inter` é padrão moderno para dashboards enterprise (legibilidade em tabelas densas). `JetBrains Mono` para dados técnicos. Nenhuma fonte corporativa foi especificada. Requer verificação de licença (Inter: SIL OFL — ok; JetBrains Mono: SIL OFL — ok) e decisão se self-hosted ou CDN.

### 3.2 Type Scale

| Token | Tamanho (rem) | Tamanho (px @16px) | Line Height | Peso | Uso |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `--text-xs` | `0.75rem` | `12px` | `1.5` | `400` | Labels de inputs, metadados de tabela, badges, timestamps |
| `--text-sm` | `0.875rem` | `14px` | `1.5` | `400` | Corpo secundário, descrições, texto de ajuda, tooltips |
| `--text-base` | `1rem` | `16px` | `1.6` | `400` | **Corpo principal** (padrão), inputs, selects, texto de cards |
| `--text-lg` | `1.125rem` | `18px` | `1.5` | `500` | Subtítulos de seção, headers de cards, títulos de modais |
| `--text-xl` | `1.25rem` | `20px` | `1.4` | `600` | Títulos de página (H1), headers de dashboard |
| `--text-2xl` | `1.5rem` | `24px` | `1.3` | `600` | Hero, título de relatório impresso |
| `--text-3xl` | `1.875rem` | `30px` | `1.2` | `700` | Título de landing/empty state grande |

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Escala baseada em ratio 1.125 (major second) com base 16px. Line heights ajustados para densidade de dashboard. Requer validação com design system real ou designer.

---

## 4. Spacing & Sizing

### 4.1 Escala de Espaçamento (base 4px / 0.25rem)

| Token | Valor (rem) | Valor (px @16px) |
| :--- | :--- | :--- |
| `--space-0` | `0` | `0` |
| `--space-1` | `0.25rem` | `4px` |
| `--space-2` | `0.5rem` | `8px` |
| `--space-3` | `0.75rem` | `12px` |
| `--space-4` | `1rem` | `16px` |
| `--space-5` | `1.25rem` | `20px` |
| `--space-6` | `1.5rem` | `24px` |
| `--space-8` | `2rem` | `32px` |
| `--space-10` | `2.5rem` | `40px` |
| `--space-12` | `3rem` | `48px` |
| `--space-16` | `4rem` | `64px` |

* **Grid base:** Múltiplos de **4px** (0.25rem). Todos os paddings, margins, gaps, heights devem usar exclusivamente estes tokens.

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Escala padrão 4px compatível com a maioria dos design systems. Requer confirmação se o layout usa grid de 8px (então `--space-2` como unidade base).

### 4.2 Border Radius

| Token | Valor (rem) | Valor (px) | Uso |
| :--- | :--- | :--- | :--- |
| `--radius-none` | `0` | `0` | Tabelas, divisores, elementos flush |
| `--radius-sm` | `0.25rem` | `4px` | Inputs, selects, badges, tags, botões pequenos |
| `--radius-md` | `0.375rem` | `6px` | **Padrão**: Cards, botões padrão, dropdowns, modais |
| `--radius-lg` | `0.5rem` | `8px` | Modais grandes, painéis laterais, containers principais |
| `--radius-xl` | `0.75rem` | `12px` | Cards de destaque, empty states |
| `--radius-full` | `9999px` | `9999px` | Avatares, pills, badges circulares, indicadores de status |

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Valores alinhados com shadcn/ui / Radix. `--radius-md` (6px) como default para consistência com Popper.js dropdowns.

### 4.3 Shadows / Elevation

| Token | Valor (box-shadow) | Uso |
| :--- | :--- | :--- |
| `--shadow-none` | `none` | Reset, elementos flush |
| `--shadow-xs` | `0 1px 2px 0 rgb(0 0 0 / 0.05)` | Hover sutil em linhas de tabela, cards compactos |
| `--shadow-sm` | `0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1)` | **Padrão**: Cards, botões, dropdowns (Popper), inputs focados |
| `--shadow-md` | `0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)` | Modais, sidebars, painéis flutuantes |
| `--shadow-lg` | `0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1)` | Modais grandes, drawers, toasts empilhados |
| `--shadow-focus` | `0 0 0 3px var(--color-accent-light)` | **Focus ring** acessível (não usar `outline: none` sem isto) |

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Sombras escalonadas para hierarquia z-index. `--shadow-focus` usa `--color-accent-light` para consistência de marca. Requer teste de contraste em dark mode.

---

## 5. Breakpoints

| Token | Largura Mínima | Dispositivo Alvo | Uso no Aegis Patrimônio |
| :--- | :--- | :--- | :--- |
| `--bp-sm` | `640px` | Mobile grande / landscape | Stack de cards, formulários em coluna única, navegação bottom-tab |
| `--bp-md` | `768px` | Tablet portrait | Sidebar colapsável, tabelas com scroll horizontal, grid 2 colunas |
| `--bp-lg` | `1024px` | Tablet landscape / Desktop pequeno | **Layout principal**: sidebar fixa + conteúdo, tabelas completas, grid 3 colunas |
| `--bp-xl` | `1280px` | Desktop padrão | Dashboard com 4+ widgets, tabelas densas, painel lateral duplo |
| `--bp-2xl` | `1536px` | Desktop grande / Ultra-wide | Layout fluido max-width 1400px, side-by-side master-detail |

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Breakpoints alinhados com Tailwind v3 padrão. O BRD menciona "web responsiva" mas não define breakpoints. Requer decisão: mobile-first ou desktop-first? (Mobile-first recomendado para usuários de campo).

---

## 6. Motion Tokens

| Token | Duração | Easing (cubic-bezier) | Uso |
| :--- | :--- | :--- | :--- |
| `--duration-instant` | `0ms` | `linear` | Toggles de boolean, checkbox, radio (sem animação) |
| `--duration-fast` | `100ms` | `cubic-bezier(0.4, 0, 0.2, 1)` | **Micro-interações**: hover de botão, focus ring, tooltip show/hide (Popper), badge pulse |
| `--duration-base` | `200ms` | `cubic-bezier(0.4, 0, 0.2, 1)` | **Padrão**: Transições de cor, background, border, transform (scale), dropdown slide, modal fade+scale |
| `--duration-slow` | `300ms` | `cubic-bezier(0.4, 0, 0.2, 1)` | Modais grandes, sidebars, page transitions (se SPA), accordion expand/collapse |
| `--duration-slower` | `500ms` | `cubic-bezier(0.4, 0, 0.2, 1)` | Empty state appear, skeleton → content, tour/guia passo a passo |

* **Reduced motion:** Respeitar `@media (prefers-reduced-motion: reduce)` — todas as durations viram `0.01ms` (quase instantâneo) mantendo o estado final.

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Curva `cubic-bezier(0.4, 0, 0.2, 1)` = Material Design "standard easing". Durações conservadoras para app enterprise (não "divertido"). Requer validação com UX: usuários de manutenção em campo preferem velocidade sobre animação.

---

## 7. Z-Index Scale

| Token | Valor | Uso |
| :--- | :--- | :--- |
| `--z-base` | `0` | Conteúdo padrão |
| `--z-dropdown` | `100` | Dropdowns (Popper.js), selects customizados |
| `--z-sticky` | `200` | Headers fixos, sidebars |
| `--z-modal-backdrop` | `400` | Overlay de modais, drawers |
| `--z-modal` | `500` | Modais, dialogs, confirmations |
| `--z-popover` | `600` | Popovers, tooltips complexos, date pickers |
| `--z-toast` | `700` | Toasts, notificações, snackbars |
| `--z-tooltip` | `800` | Tooltips simples (Popper) — acima de tudo |

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Escala baseada em conflitos típicos: Popper.js tooltips/dropdowns vs modais vs toasts. Requer auditoria do `z-index` atual nos 15 arquivos JS/CSS.

---

## 8. Component-Level Tokens (Semânticos)

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Tokens compostos que mapeiam primitivas para componentes específicos do domínio (ativos, manutenção, health checks). Evitam decisões ad-hoc em cada componente.

| Token | Composição | Uso |
| :--- | :--- | :--- |
| `--btn-primary-bg` | `var(--color-accent-primary)` | Botão primário (Criar ativo, Iniciar manutenção, Aprovar) |
| `--btn-primary-hover` | `var(--color-accent-hover)` | Hover botão primário |
| `--btn-primary-text` | `#FFFFFF` | Texto botão primário |
| `--btn-secondary-bg` | `var(--color-bg-tertiary)` | Botão secundário (Cancelar, Voltar) |
| `--btn-secondary-hover` | `var(--color-border-medium)` | Hover botão secundário |
| `--btn-danger-bg` | `var(--color-danger)` | Botão destrutivo (Excluir ativo, Baixar patrimônio) |
| `--input-bg` | `var(--color-bg-primary)` | Background de inputs, selects, textareas |
| `--input-border` | `var(--color-border-light)` | Borda padrão de inputs |
| `--input-border-focus` | `var(--color-accent-primary)` | Borda focada + focus ring |
| `--input-placeholder` | `var(--color-text-muted)` | Placeholder text |
| `--table-header-bg` | `var(--color-bg-secondary)` | Cabeçalho de tabelas de ativos/manutenções |
| `--table-row-hover` | `var(--color-bg-tertiary)` | Hover em linhas de tabela |
| `--table-border` | `var(--color-border-light)` | Divisores de tabela |
| `--badge-success-bg` | `var(--color-success-light)` | Badge "Concluído", "Saudável", "Em dia" |
| `--badge-success-text` | `var(--color-success)` | Texto badge sucesso |
| `--badge-warning-bg` | `var(--color-warning-light)` | Badge "Pendente", "Atrasado", "Disco > 85%" |
| `--badge-warning-text` | `var(--color-warning)` | Texto badge aviso |
| `--badge-danger-bg` | `var(--color-danger-light)` | Badge "Crítico", "Falha", "Vencido" |
| `--badge-danger-text` | `var(--color-danger)` | Texto badge perigo |
| `--badge-info-bg` | `var(--color-info-light)` | Badge "Em andamento", "Aguardando aprovação" |
| `--badge-info-text` | `var(--color-info)` | Texto badge info |
| `--card-bg` | `var(--color-bg-secondary)` | Background de cards de ativo, widget de dashboard |
| `--card-border` | `var(--color-border-light)` | Borda de cards |
| `--card-shadow` | `var(--shadow-sm)` | Sombra de cards |
| `--sidebar-bg` | `var(--color-bg-secondary)` | Sidebar de navegação |
| `--sidebar-item-hover` | `var(--color-bg-tertiary)` | Hover item de menu |
| `--sidebar-item-active` | `var(--color-accent-light)` | Item ativo (rota atual) |
| `--health-ok` | `var(--color-success)` | Indicador health check OK (disco < 85%, mem < 90%) |
| `--health-warn` | `var(--color-warning)` | Indicador health check atenção |
| `--health-critical` | `var(--color-danger)` | Indicador health check crítico |

---

## 9. Governança dos Tokens

### 9.1 Processo de Adição/Alteração de Token

1. **Proposta:** Issue/PR com justificativa (novo componente, mudança de marca, acessibilidade, novo tema).
2. **Revisão:** Aprovação obrigatória de **Design Lead** + **Frontend Lead**.
3. **Implementação:** Atualização no arquivo fonte único (`tokens.css` ou `tokens.json` + build).
4. **Distribuição:** Build gera CSS custom properties + JSON para consumo JS (ex: `import tokens from '@aegis/tokens'`).
5. **Migração:** Substituição de valores hardcoded nos 15 arquivos JS + eventuais CSS — **um PR por componente** para rastreabilidade.

### 9.2 Ferramenta de Sincronização

| Etapa | Ferramenta Proposta | Status |
| :--- | :--- | :--- |
| **Fonte única** | `tokens.json` (JSON estruturado: nome, valor, descrição, tipo) | **A definir** — não existe hoje |
| **Build → CSS** | Style Dictionary / custom script Node | **A definir** |
| **Build → JS/TS** | Style Dictionary → `tokens.d.ts` + `tokens.js` (ESM) | **A definir** |
| **Design → Code** | Figma Tokens Plugin (se Figma usado) → `tokens.json` | **A definir** — não há link Figma no repositório |
| **Documentação** | Storybook + design-tokens addon / auto-gerado do JSON | **A definir** |

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Stack atual não tem build step visível (sem `package.json` scripts no frontend, sem Vite/Webpack detectado). **Pré-requisito:** configurar tooling de build (Vite recomendado para JS vanilla) antes de implementar tokens.

### 9.3 Como Evitar Drift entre Design e Código

- **CI Gate:** Job que compara `tokens.json` com valores extraídos do CSS buildado (falha se divergência).
- **Lint rule:** `no-hardcoded-colors` / `no-hardcoded-spacing` no ESLint (custom rule ou `stylelint` para CSS).
- **Design Review:** Checklist em PR template: "Novos valores visuais usam tokens existentes?" / "Novos tokens documentados?".
- **Audit trimestral:** Script que varre `grep -r "#[0-9a-fA-F]\{3,8\}" frontend/src --include="*.js" --include="*.css"` e reporta valores não-tokenizados.

---

## 10. Referências e Próximos Passos

| Item | Status | Ação Necessária |
| :--- | :--- | :--- |
| **Arquivo fonte `tokens.json`** | ❌ Não existe | Criar na raiz do frontend (`frontend/tokens.json`) |
| **Build pipeline (Vite + Style Dictionary)** | ❌ Não configurado | Sprint 0: configurar `package.json` scripts, `vite.config.js`, `build-tokens.js` |
| **CSS custom properties injection** | ❌ Não implementado | Injetar `:root { ... }` no `index.html` ou entry point JS |
| **ESLint / Stylelint rules** | ❌ Não configurado | Adicionar `stylelint`, `stylelint-config-standard`, custom rule para tokens |
| **Figma / Design tool link** | ❌ Não referenciado | **Entrada humana:** link do arquivo Figma ou confirmação de que design será feito no código |
| **Dark mode implementation** | ❌ Não iniciado | Decisão de produto: MVP ou fase 2? Se MVP, implementar toggle + persistência |
| **Acessibilidade (contraste)** | ❌ Não validado | Auditar todas as combinações semânticas (WCAG AA 4.5:1 texto, 3:1 UI) |
| **Documentação viva (Storybook)** | ❌ Não configurado | Avaliar custo/benefício para 15 arquivos JS — pode ser overkill inicial |

---

## 11. Declaração de Limitações e Gaps

> **Este documento NÃO reflete um design system existente.** O codebase atual (diagnóstico determinístico de 348 arquivos Java + 15 JS) **não contém nenhum artefato de design tokens, CSS framework, ou variáveis CSS centralizadas**.  
>   
> Todos os valores, nomes, escalas e decisões acima são **hipóteses iniciais (INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA)** baseadas em:
> - Requisitos do BRD (dashboard denso, tabelas de ativos, formulários de manutenção, alertas de health check, RBAC)
> - Stack tecnológica real: JavaScript vanilla + `@popperjs/core` (apenas para positioning)
> - Boas práticas de design systems enterprise (shadcn/ui, Radix, Tailwind, Material Design tokens)
>   
> **Nenhuma cor corporativa, fonte licenciada, logo, ou guideline de marca foi encontrada no repositório.**  
>   
> **Próximo passo obrigatório:** Sessão de alinhamento com Product Design / Frontend Lead / PO para validar, ajustar ou descartar esta proposta antes de qualquer implementação.

---

## 12. Revision History

| Versão | Data | Autor | Mudanças |
| :--- | :--- | :--- | :--- |
| 1.0 | 15/01/2025 | IA Assistida (Design Tokens Generator) | Criação inicial baseada em diagnóstico determinístico (zero tokens encontrados) e BRD v1.0. **Todas as seções marcadas como [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**. |