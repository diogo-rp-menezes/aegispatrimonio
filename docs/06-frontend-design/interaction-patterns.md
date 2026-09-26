# Interaction & Motion Patterns — Aegis1

> **Versão:** 0.1 · **Owner:** Frontend Lead · **Status:** Draft  
> **Depende de:** `component-library.md`, `design-tokens.md` (não implementado)

## 1. Overview

Este documento define os padrões de microinteração, animação e feedback para o Aegis1. A varredura determinística do workspace confirma **zero componentes de UI implementados** no codebase atual — o projeto consiste em 15 arquivos `.js` vanilla (627 LOC) com apenas `@popperjs/core` como dependência de produção.  

Os padrões abaixo combinam:
- **Estado atual (vanilla JS + @popperjs/core):** capacidades reais de posicionamento e overlay disponíveis hoje
- **Estado alvo (componentes planejados em `component-library.md`):** 34 componentes especificados para migração futura — marcados como **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

> **Nota de rastreabilidade:** O `component-library.md` lista 26 componentes base/ui + 8 domínio com specs detalhadas de estados visuais, tokens consumidos e exemplos de uso. Este documento extrai e consolida os padrões de motion/interação implícitos nessas specs. <!-- source: component-library.md#3 -->

## 2. Princípios de Motion

| Princípio | Descrição | Implementação Atual | Implementação Planejada |
| :--- | :--- | :--- | :--- |
| **Propósito da animação** | Comunicar hierarquia, causalidade e estado — nunca decorativa | Não aplicável (sem componentes) | Obrigatório em todos os componentes base/ui |
| **Duração padrão** | `duration-fast` (150ms) para feedback direto; `duration-base` (250ms) para transições de painel | Não definido | Tokens em `design-tokens.md` §9 (checklist pendente) |
| **Easing padrão** | `easing-standard` (cubic-bezier(0.4, 0, 0.2, 1)) para entradas/saídas; `easing-emphasized` para destaque | Não definido | Tokens em `design-tokens.md` §9 (checklist pendente) |
| **`prefers-reduced-motion`** | Respeitado globalmente: animações desabilitadas ou reduzidas a 0ms | Não implementado | Obrigatório — `design-tokens.md` §5, `user-flows.md` §5 |
| **Performance** | Animações apenas em `transform`/`opacity` (GPU); evitar layout thrashing | Não aplicável | Regra de implementação para todos os componentes |

> **Gap crítico:** `design-tokens.md` não implementado — tokens de motion (`--duration-fast`, `--duration-base`, `--easing-standard`, `--easing-emphasized`) não existem no codebase. <!-- source: component-library.md#6 -->

## 3. Padrões por Tipo de Interação

### 3.1 Feedback de Ação (Clique/Toque)

| Ação | Feedback Visual Atual (@popperjs/core) | Feedback Visual Planejado (Component Library) | Duração | Componentes Afetados |
| :--- | :--- | :--- | :--- | :--- |
| **Clique em botão** | Nenhum (vanilla JS sem UI) | `transform: scale(0.98)` + `box-shadow: var(--shadow-sm)` no `:active`; spinner centralizado no `loading` | `duration-fast` (150ms) | `Button`, `IconButton` |
| **Toggle/Switch** | Nenhum | Thumb slide com `transform: translateX()`; mudança de cor de fundo | `duration-fast` | `Switch`, `Checkbox`, `RadioGroup` |
| **Drag & drop** | `@popperjs/core` pode posicionar overlay de preview | Ghost element com `opacity: 0.5` + `rotate(3deg)`; drop zone highlight | `duration-base` | `DataTable` (reorder), `OrderWizard` (reorder steps) |
| **Hover em linha de tabela** | Nenhum | `bg: var(--color-accent-light)` + `box-shadow: var(--shadow-sm)` | `duration-fast` | `DataTable`, `Table` |
| **Focus visível (teclado)** | Nenhum | `outline: none` + `box-shadow: inset 0 0 0 2px var(--color-accent)` | Imediato | **Todos os componentes interativos** |

> **Rastreabilidade:** Estados visuais detalhados por componente em `component-library.md` §4.1 (Button), §4.3 (DataTable), §4.4 (Badge). <!-- source: component-library.md#4.1 --> <!-- source: component-library.md#4.3 --> <!-- source: component-library.md#4.4 -->

### 3.2 Transições de Estado

| Transição | Comportamento Atual | Comportamento Planejado | Componentes Afetados |
| :--- | :--- | :--- | :--- |
| **Loading → Sucesso** | `console.log` em `api.js:49` | Skeleton → fade-in conteúdo (`opacity 0→1`, `translateY(8px)→0`); toast success auto-dismiss 3s | `DataTable`, `OrderCostForm`, `EntityCrudTable`, `Modal` (form) |
| **Loading → Erro** | `console.error` em `api.js:26` | Skeleton → inline error + toast error persistente; retry button no `EmptyState` | `DataTable`, `Modal`, `OrderWizard`, `OrderCostForm` |
| **Expandir/Colapsar (Accordion/Details)** | Nenhum | `max-height: 0 → scrollHeight` + `opacity 0→1`; ícone rotação 180° | `Accordion`, `DataTable` (expandable rows), `OrderTimeline` |
| **Abrir Modal/Drawer** | Nenhum (sem componentes) | Backdrop `opacity 0→1`; container `scale(0.95)→1` + `opacity 0→1`; focus trap | `Modal`, `Drawer` |
| **Fechar Modal/Drawer** | Nenhum | Reverso da entrada com `duration-fast`; focus return to trigger | `Modal`, `Drawer` |
| **Stepper/Wizard navegação** | Nenhum | Cross-fade conteúdo (`opacity 0→1`, `translateX(±16px)→0`); step indicator animação | `Wizard/Stepper` (OrderWizard) |
| **Toast entrada/saída** | Nenhum | Slide-in from bottom-right (`translateY(100%)→0`); slide-out + fade | `Toast` |

> **Rastreabilidade:** Transições documentadas nos exemplos de `Modal` (§4.2), `DataTable` (§4.3), `Wizard/Stepper` (§4.5) do `component-library.md`. <!-- source: component-library.md#4.2 --> <!-- source: component-library.md#4.3 --> <!-- source: component-library.md#4.5 -->

### 3.3 Navegação

| Padrão | Comportamento Atual | Comportamento Planejado |
| :--- | :--- | :--- |
| **Transição entre páginas** | Navegação nativa do browser (SPA não implementada) | **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** View transition API ou cross-fade 200ms entre rotas; scroll restoration |
| **Abertura de Modal/Dialog** | Nenhum | Backdrop fade-in 150ms; container scale+fade 250ms; focus trap; `Esc`/backdrop click para fechar |
| **Abertura de Drawer/Sidebar** | Nenhum | Slide from left/right (`translateX(-100%)→0`); backdrop em mobile; swipe-to-close em touch |
| **Toast/Notificação** | Nenhum | **Posição:** bottom-right (desktop), bottom-center (mobile)<br>**Duração:** success/info 3s auto-dismiss; warning/error persistent até dismiss<br>**Stack:** máx 3 simultâneos, gap 8px |
| **DropdownMenu/Popover (via @popperjs/core)** | **Disponível hoje** — `@popperjs/core` v2.11.8 instalado | Posicionamento automático (flip, shift, offset); animação `opacity 0→1` + `scale(0.95)→1` 150ms; click outside/esc para fechar |

> **Rastreabilidade:** `@popperjs/core` confirmado em `package.json` — único recurso de posicionamento/overlay disponível no codebase atual. <!-- source: package.json --> <!-- source: component-library.md#3 (Popover, DropdownMenu) -->

## 4. Estados Padrão de Componentes com Dados

### 4.1 Loading States

| Padrão | Quando Usar | Componentes com Skeleton Definido (Planejado) |
| :--- | :--- | :--- |
| **Skeleton (placeholder estrutural)** | Listagens, tabelas, cards, formulários complexos — quando estrutura conhecida | `DataTable` (3-5 linhas), `Card`, `OrderCostForm`, `AssetCostTable`, `EntityCrudTable` |
| **Spinner inline** | Ações pontuais (submit, save, delete) — botão ou área pequena | `Button` (`loading` prop), `IconButton`, `Modal` footer actions |
| **LoadingOverlay fullscreen** | Navegação entre rotas pesadas, autenticação | `LoadingOverlay` (variant `fullscreen`) |
| **Shimmer effect** | Opcional — apenas se `prefers-reduced-motion: no-preference` | Aplicado sobre skeletons via `::after` animation |

> **Rastreabilidade:** `component-library.md` §3 lista `Skeleton` e `LoadingOverlay` como componentes planejados; §4.3 (DataTable) e §4.6 (OrderCostForm) mostram uso em exemplos. <!-- source: component-library.md#3 --> <!-- source: component-library.md#4.3 --> <!-- source: component-library.md#4.6 -->

### 4.2 Empty States

| Estrutura Padrão | Tom de Voz | Componentes Afetados |
| :--- | :--- | :--- |
| **Ilustração/ícone** (48×48px, `var(--color-text-muted)`) + **Título** (`text-lg`, `font-medium`) + **Descrição** (`text-sm`, `var(--color-text-secondary)`) + **CTA opcional** (`Button` variant `primary`/`ghost`) | Neutro, orientado a ação — "Nenhum registro encontrado. [Criar primeiro]" | `DataTable`, `Table`, `AssetCostTable`, `EntityCrudTable`, `OrderTimeline`, `SearchInput` results |

> **Rastreabilidade:** `component-library.md` §3 lista `EmptyState` com variantes `default`, `action`, `illustration`; §4.3 (DataTable) mostra `emptyMessage` prop. <!-- source: component-library.md#3 --> <!-- source: component-library.md#4.3 -->

### 4.3 Error States

| Tipo | Padrão Visual | Comportamento | Componentes Afetados |
| :--- | :--- | :--- | :--- |
| **Erro de campo (inline)** | `Input`/`Select`/`Textarea` com `border: var(--color-danger)`; ícone `AlertCircle` 16px; mensagem `text-sm` `var(--color-danger)` abaixo do campo; `aria-describedby` linkado | Validação no `onBlur` + submit; mensagem `aria-live="polite"` | `Input`, `Select`, `Textarea`, `DatePicker`, `OrderWizard` steps, `OrderCostForm` |
| **Erro de página/bloqueante** | `ErrorBoundary` fallback: ilustração + título "Algo deu errado" + descrição técnica colapsável + `Button` "Tentar novamente" + `Button` "Contatar suporte" | Captura erros React não tratados; loga em serviço de erro (Sentry/etc.) | `ErrorBoundary` (root layout) |
| **Erro de rede/timeout** | Toast `variant="error"` persistent + `Button` "Tentar novamente" inline; para formulários: banner no topo do formulário | Retry automático 1x para GET idempotentes; exponential backoff para mutações | `Toast`, `DataTable` (toolbar), `Modal` (form), `OrderCostForm` |
| **Erro 409 (conflito — ex.: exclusão bloqueada)** | `Modal` variant `confirmation` com descrição detalhada + ações "Cancelar" / "Forçar exclusão" (destructive) | Backend retorna 409 com detalhes de dependências; frontend exibe modal contextual | `EntityCrudTable` (delete action), `Modal` |

> **Rastreabilidade:** `component-library.md` §4.1 (Button `disabled`/`loading`), §4.2 (Modal `confirmation` variant), §4.6 (OrderCostForm validação), §3 (`ErrorBoundary`, `Toast` variants). <!-- source: component-library.md#4.1 --> <!-- source: component-library.md#4.2 --> <!-- source: component-library.md#4.6 --> <!-- source: component-library.md#3 -->

## 5. Feedback do Sistema

| Tipo | Canal | Duração/Persistência | Acessibilidade |
| :--- | :--- | :--- | :--- |
| **Sucesso** | `Toast` (bottom-right) | Auto-dismiss 3s; `aria-live="polite"` | `role="status"` |
| **Aviso** | `Toast` (bottom-right) | Persistent até dismiss; `aria-live="polite"` | `role="status"` |
| **Erro** | `Toast` (bottom-right) + inline em formulários | Persistent até dismiss; `aria-live="assertive"` | `role="alert"` |
| **Confirmação destrutiva** | `Modal` variant `confirmation` (blocking) | Permanece até ação explícita; focus trap; `Esc` cancela | `role="alertdialog"`, `aria-modal="true"` |
| **Informação contextual** | `Tooltip` (hover/focus) / `Popover` (click) | Tooltip: show 200ms delay, hide 100ms; Popover: click outside/esc | `Tooltip`: `role="tooltip"`; `Popover`: `role="dialog"` |

> **Rastreabilidade:** `component-library.md` §3 lista `Toast` (variants success/error/warning/info, persistent/auto-dismiss), `Tooltip`, `Popover`, `Modal` (confirmation variant). <!-- source: component-library.md#3 -->

## 6. Acessibilidade de Interação

| Requisito | Status Atual | Implementação Planejada |
| :--- | :--- | :--- |
| **Foco visível em toda interação por teclado** | Não implementado | **Obrigatório:** `outline: none` + `box-shadow: inset 0 0 0 2px var(--color-accent)` em todos os componentes interativos; `focus-visible` polyfill se necessário |
| **`prefers-reduced-motion` respeitado** | Não implementado | **Obrigatório:** `@media (prefers-reduced-motion: reduce) { *, *::before, *::after { animation-duration: 0.01ms !important; transition-duration: 0.01ms !important; } }` em CSS global |
| **Anúncio de mudanças dinâmicas (aria-live)** | Não implementado | **Obrigatório:** `Toast` → `role="status"` (polite) / `role="alert"` (assertive); `DataTable` loading → `aria-busy="true"`; `Modal` abertura → focus management + `aria-modal="true"` |
| **Navegação por teclado completa** | Não implementado | **Obrigatório:** `Tab`/`Shift+Tab` ordem lógica; `Enter`/`Space` ativam botões; `Esc` fecha overlays; setas em `RadioGroup`, `Select`, `Tabs`, `DataTable` (linhas) |
| **Contraste WCAG AA** | Não verificado | **Obrigatório:** Tokens de cor em `design-tokens.md` validados para 4.5:1 (texto) / 3:1 (UI components); `Badge` variants usam cor + ícone/texto (não apenas cor) |

> **Rastreabilidade:** `component-library.md` §2 (Convenções Gerais → Acessibilidade), §4.4 (Badge: "não apenas cor, sempre com ícone/texto"), §4.2 (Modal: focus trap, `aria-modal`). <!-- source: component-library.md#2 --> <!-- source: component-library.md#4.4 --> <!-- source: component-library.md#4.2 -->

## 7. Anti-padrões (o que evitar)

| Anti-padrão | Por que evitar | Alternativa Correta |
| :--- | :--- | :--- |
| **Animações > 400ms em ações frequentes** (botões, toggles, hover) | Aumenta latência percebida; fadiga cognitiva | Máximo `duration-base` (250ms) para transições de painel; `duration-fast` (150ms) para feedback direto |
| **Loading sem feedback visual > 300ms** | Usuário assume falha; cliques duplicados | Skeleton imediato para estrutura conhecida; spinner inline para ações pontuais; `aria-busy="true"` |
| **Animação apenas em propriedades que disparam layout** (`width`, `height`, `top`, `left`, `margin`) | Force reflow/repaint; jank em mobile/low-end | Animar apenas `transform` (`translate`, `scale`) e `opacity`; `will-change` se necessário |
| **Toast empilhados ilimitados** | Poluição visual; perda de contexto | Máx 3 simultâneos; agrupar erros similares; `action` toast para ações críticas |
| **Confirmação destrutiva sem foco no botão "Cancelar"** | Risco de ação acidental por `Enter` | Focus inicial no botão menos destrutivo (`Cancelar`); `Esc` sempre cancela |
| **Cores semânticas apenas por `color`/`background`** (sem ícone/texto) | Inacessível para daltônicos; falha WCAG 1.4.1 | `Badge`/`Alert`/`Toast` sempre com ícone + label; `Border`/`Outline` variants para estados neutros |
| **Modais não modais (sem focus trap / backdrop)** | Teclado/screen reader "vazam" para background | `Modal` com `inert` no background (ou polyfill); focus trap loop; `aria-modal="true"` |

> **Rastreabilidade:** Baseado em specs de `component-library.md` §4 (Button active/loading, Modal focus trap, Badge variants, Toast stacking) e princípios WCAG/WAI-ARIA APG referenciados em §2. <!-- source: component-library.md#4.1 --> <!-- source: component-library.md#4.2 --> <!-- source: component-library.md#4.4 --> <!-- source: component-library.md#3 -->

## 8. Referências

* **Component Library / Inventory:** `component-library.md` — 34 componentes planejados com specs de estados visuais, tokens consumidos e exemplos de uso (fonte primária deste documento) <!-- source: component-library.md -->
* **Design Tokens:** `design-tokens.md` — **não implementado**; checklist §9 pendente (cores, tipografia, spacing, sombras, **motion**, z-index, breakpoints) <!-- source: component-library.md#1 --> <!-- source: component-library.md#6 -->
* **User Flows:** `user-flows.md` — 8 fluxos com estados de UI, métricas, edge cases (inferidos do BRD) <!-- source: component-library.md#1 -->
* **Business Requirements Document:** `docs/brd.md` — regras BR-01 a BR-06, guardrails (latência P95 < 800ms, taxa erro < 1%) <!-- source: component-library.md#7 -->
* **Diagnóstico Determinístico:** `server/analyze-pipeline.ts` — confirma: vanilla JS, `@popperjs/core` apenas, 3 `console.*` residuais, complexidade ciclomática 13 em `api.js:request` <!-- source: Diagnóstico geral do projeto -->
* **Stack Tecnológica Verificada:** `package.json` — dependências: `@popperjs/core@^2.11.8` apenas; sem framework UI, sem CSS, sem roteamento, sem testes <!-- source: Stack real e verificada -->
* **Protótipos Figma / Wireframes:** `[LINK NÃO DISPONÍVEL NO REPOSITÓRIO — REQUER ENTRADA HUMANA]` <!-- source: component-library.md#7 -->