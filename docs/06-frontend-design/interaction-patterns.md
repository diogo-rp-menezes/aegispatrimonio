# Interaction & Motion Patterns — Aegis Patrimônio

> **Versão:** 1.0 · **Owner:** Frontend Lead · **Status:** Draft  
> **Depende de:** [component-library.md](component-library.md), [design-tokens.md](design-tokens.md)

---

## 1. Overview

Este documento define os **padrões de microinteração, animação e feedback** que garantem consistência de comportamento entre todos os componentes e telas do Aegis Patrimônio.  

**Contexto crítico:** O diagnóstico determinístico (348 arquivos Java, 15 arquivos JS em `frontend/src/`) **não revelou nenhum componente de UI, sistema de design, ou padrão de interação existente**. Os 15 arquivos JS são serviços/utilitários (`api.js`, etc.).  

**Base de trabalho:** Os padrões abaixo são derivados **exclusivamente** dos 29 componentes propostos em [component-library.md](component-library.md) (Seção 3 — Inventário) e dos design tokens inferidos em [design-tokens.md](design-tokens.md). **Nenhum padrão foi extraído de código real** — todos são propostas iniciais que requerem validação humana antes de implementação.

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Todo este documento é uma **proposta inicial** baseada em componentes que não existem e tokens que não foram implementados. Requer decisão de arquitetura (Web Components vs. módulos JS + template literals vs. framework leve) e aprovação de design antes de qualquer implementação.

---

## 2. Princípios de Motion

| Princípio | Definição | Aplicação Prática |
| :--- | :--- | :--- |
| **Propósito da animação** | Comunicar hierarquia, causalidade e estado — **nunca decorativa** | Botão: `scale(0.98)` no press confirma ação; Modal: `scale(0.95)→1` + fade indica entrada em novo contexto; Toast: slide-in right indica chegada de informação nova |
| **Duração e easing padrão** | Referenciar tokens de [design-tokens.md](design-tokens.md) — **valores hardcoded proibidos** | `--duration-fast: 150ms` (micro-feedback: hover, press, badge appear)<br>`--duration-base: 250ms` (transições de estado: modal, dropdown, toast, expand/collapse)<br>`--duration-slow: 350ms` (transições complexas: sidebar collapse, page transition)<br>`--easing-standard: cubic-bezier(0.4, 0, 0.2, 1)` (padrão Material)<br>`--easing-emphasized: cubic-bezier(0.4, 0, 0, 1)` (saídas rápidas)<br>`--easing-decelerated: cubic-bezier(0, 0, 0.2, 1)` (entradas suaves) |
| **Quando NÃO animar** | Respeitar `prefers-reduced-motion` + evitar em ações de alta frequência | **Desativar:** transições de modal/drawer/toast, skeleton pulse, spinner rotation, expand/collapse animado<br>**Manter (instantâneo):** focus ring, disabled opacity, loading spinner (sem rotação), tooltip show/hide<br>**Alta frequência (>10/s):** table row hover, button hover, badge status change — usar `transition: none` ou `0ms` |
| **Performance** | 60fps garantido — apenas `transform` e `opacity` animados; `will-change` usado com parcimônia | Modal/dropdown: `will-change: transform, opacity` durante animação; remover após `transitionend`<br>Sidebar: `will-change: transform` apenas enquanto `collapsed` state muda<br>**Nunca** animar `width`, `height`, `top`, `left`, `margin`, `padding`, `border-radius` |
| **Consistência cross-component** | Mesmo token = mesmo comportamento em todos os componentes | `aegis-modal`, `aegis-dropdown`, `aegis-toast`, `aegis-sidebar` usam `--duration-base` + `--easing-standard` para entrada/saída<br>`aegis-button`, `aegis-switch`, `aegis-checkbox` usam `--duration-fast` + `--easing-emphasized` para press/toggle |

---

## 3. Padrões por Tipo de Interação

### 3.1 Feedback de Ação (Clique/Toque)

| Ação | Componente(s) | Feedback Visual | Duração | Token(s) |
| :--- | :--- | :--- | :--- | :--- |
| **Clique em botão** (`aegis-button`) | `aegis-button` | `transform: scale(0.98)` no `active` + ripple opcional (apenas `variant="primary"`) | `--duration-fast` (150ms) | `--duration-fast`, `--easing-emphasized` |
| **Toggle/Switch** (`aegis-switch`, `aegis-checkbox`) | `aegis-switch`, `aegis-checkbox` | Thumb slide (`translateX`) + background color cross-fade + checkmark draw (checkbox) | `--duration-fast` (150ms) | `--duration-fast`, `--easing-standard` |
| **Seleção de linha** (`aegis-data-table`) | `aegis-data-table` | Background `--color-accent-light` imediato (sem animação) + checkbox fade-in | `0ms` (background) / `--duration-fast` (checkbox) | `--color-accent-light`, `--duration-fast` |
| **Drag & drop** (futuro) | `aegis-data-table` (row reorder), `aegis-file-upload` | Ghost element com `opacity: 0.5` + `rotate(3deg)` + drop zone highlight `--color-accent-primary` border | `--duration-base` (250ms) | `--duration-base`, `--color-accent-primary` |
| **Hover em ação de tabela** (dropdown trigger) | `aegis-dropdown` (trigger em `aegis-data-table` row) | Background `--table-row-hover` na row + icon color `--color-text-primary` | `--duration-fast` (150ms) | `--table-row-hover`, `--duration-fast` |

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Ripple effect em botão primary é opcional; avaliar custo de implementação vs. valor UX. Drag & drop não está nos flows atuais — mantido como padrão futuro.

### 3.2 Transições de Estado

| Transição | Comportamento Detalhado | Componentes Afetados | Duração | Tokens |
| :--- | :--- | :--- | :--- | :--- |
| **Loading → Sucesso** (form submit, save) | 1. Button `loading=true`: spinner aparece, label `opacity: 0`<br>2. Sucesso: button `loading=false`, emite `aegis-toast.success`<br>3. Modal (se aberto): `aegis-modal.hide()` com fade+scale out | `aegis-button`, `aegis-form`, `aegis-modal`, `aegis-toast` | Button: `--duration-fast`<br>Toast: `--duration-base`<br>Modal: `--duration-base` | `--duration-fast`, `--duration-base`, `--easing-standard` |
| **Loading → Erro** (API failure) | 1. Button `loading=false`, `aria-invalid="true"`, border `--color-danger`<br>2. Inline error message slide-down (`max-height: 0 → auto`)<br>3. `aegis-toast.error` persistente (sem auto-dismiss)<br>4. Shake sutil no container (`translateX: -4px → 4px → 0`) | `aegis-button`, `aegis-input`, `aegis-form`, `aegis-toast`, `aegis-modal` | Inline error: `--duration-base`<br>Shake: `--duration-fast` × 3 | `--duration-base`, `--duration-fast`, `--color-danger`, `--easing-standard`, `--easing-emphasized` |
| **Expandir/Colapsar** (sidebar, table row, filter bar) | **Sidebar:** `transform: translateX(0 → -280px)` + overlay fade<br>**Table row:** `max-height: 0 → auto` + `opacity: 0 → 1` (content)<br>**Filter bar:** `height: auto → 0` + `opacity: 1 → 0` + `margin-bottom` collapse | `aegis-sidebar`, `aegis-data-table` (expandable), `aegis-filter-bar` | Sidebar: `--duration-base`<br>Table row: `--duration-fast`<br>Filter bar: `--duration-base` | `--duration-base`, `--duration-fast`, `--easing-standard`, `--easing-decelerated` |
| **Abertura/Fechar Modal** | **Abrir:** backdrop `opacity: 0 → 0.4` + container `scale(0.95) → 1` + `opacity: 0 → 1`<br>**Fechar:** reverso com `--duration-fast`<br>**Focus trap:** primeiro elemento focável recebe focus ao abrir | `aegis-modal`, `aegis-confirm-dialog` | `--duration-base` (abrir)<br>`--duration-fast` (fechar) | `--duration-base`, `--duration-fast`, `--easing-standard`, `--easing-emphasized`, `--shadow-modal-backdrop` |
| **Abertura/Fechar Dropdown** | **Abrir:** panel `opacity: 0 → 1` + `translateY(-4px → 0)` (Popper.js placement)<br>**Fechar:** `opacity: 1 → 0` + `translateY(0 → -4px)`<br>**Flip:** animação suave se Popper inverte placement | `aegis-dropdown`, `aegis-select`, `aegis-date-picker` | `--duration-fast` (150ms) | `--duration-fast`, `--easing-decelerated`, `--shadow-md` |
| **Toast Entrada/Saída** | **Entrada:** `translateX(100%) → 0` + `opacity: 0 → 1` + progress bar `width: 100% → 0`<br>**Saída:** `translateX(0 → 100%)` + `opacity: 1 → 0`<br>**Hover:** pausa progress bar + `box-shadow` elevado | `aegis-toast`, `aegis-toast-container` | Entrada: `--duration-base`<br>Saída: `--duration-fast`<br>Progress: `duration` prop (default 5s) | `--duration-base`, `--duration-fast`, `--easing-standard`, `--easing-emphasized` |

### 3.3 Navegação

| Padrão | Comportamento | Duração | Tokens |
| :--- | :--- | :--- | :--- |
| **Transição entre páginas** (SPA navigation) | **Não implementado no MVP** — navegação full-page reload (backend Java renderiza). Se SPA no futuro: `opacity: 0 → 1` no `<main>` + scroll to top instantâneo. | `--duration-base` (futuro) | `--duration-base`, `--easing-standard` |
| **Abertura de modal/dialog** | Ver "Abertura/Fechar Modal" acima. `aegis-confirm-dialog` adiciona `require-typing` input focus delay. | `--duration-base` | `--duration-base`, `--easing-standard` |
| **Abertura de drawer/sidebar** | **Desktop:** sidebar `translateX(0 → -280px)` + content `margin-left: 280px → 0` + overlay fade<br>**Mobile:** sidebar `translateX(-100% → 0)` + overlay `opacity: 0 → 0.4` + body `overflow: hidden` | `--duration-base` (250ms) | `--duration-base`, `--easing-standard`, `--z-sidebar`, `--z-overlay` |
| **Toast/notificação** | Container fixo `bottom-right` (desktop) / `bottom-center` (mobile)<br>Stack: novo toast entra acima, existentes `translateY(-8px)`<br>Max 3 simultâneos; 4º substitui o mais antigo (fade out) | Entrada: `--duration-base`<br>Stack shift: `--duration-fast` | `--duration-base`, `--duration-fast`, `--z-toast`, `--space-3`, `--space-4` |

---

## 4. Estados Padrão de Componentes com Dados

### 4.1 Loading States

| Componente | Padrão de Loading | Quando Usar Skeleton vs Spinner |
| :--- | :--- | :--- |
| `aegis-data-table` | **Skeleton rows** (5 linhas) — mantém `thead` visível, `tbody` mostra `<aegis-loading variant="pulse">` em cada cell | **Skeleton:** lista com estrutura conhecida (colunas fixas). **Spinner:** ação pontual (export, delete, single fetch) |
| `aegis-card` / `aegis-stat-card` | **Skeleton card** — `aegis-loading variant="pulse"` no header, body, footer | **Skeleton:** dashboard com múltiplos cards carregando em paralelo |
| `aegis-form` | **Spinner inline** no botão submit (`aegis-button loading`) + `fieldset disabled` | **Spinner:** submissão única. **Skeleton:** não aplicável |
| `aegis-modal` (form) | **Spinner no botão submit** + `aria-busy="true"` no modal | **Spinner:** ação dentro do modal |
| `aegis-dropdown` / `aegis-select` | **Spinner no trigger** (substituí icon) + `aria-busy="true"` | **Spinner:** options carregadas sob demanda (searchable select) |
| `aegis-health-indicator` | **Pulse no badge** + `--color-text-muted` no value | **Pulse:** polling de health check (intervalo >5s) |

**Componentes com skeleton definido (proposto):** `aegis-data-table`, `aegis-card`, `aegis-stat-card`, `aegis-filter-bar`, `aegis-audit-log-table`, `aegis-empty-state` (skeleton do próprio empty state)

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Skeletons específicos por componente podem ser gerados automaticamente via CSS (utility `.aegis-skeleton-{component}`) em vez de componentes dedicados. Avaliar trade-off manutenção vs. consistência.

### 4.2 Empty States

**Estrutura padrão (todos os componentes):**
```html
<div class="aegis-empty-state" role="status">
  <div class="aegis-empty-state__illustration" aria-hidden="true">
    <!-- SVG illustration ou ícone tokenizado -->
  </div>
  <h3 class="aegis-empty-state__title">Título contextual</h3>
  <p class="aegis-empty-state__description">Descrição acionável — o que o usuário pode fazer</p>
  <slot name="action"><!-- CTA opcional: botão, link --></slot>
</div>
```

| Componente | Ilustração | Título Padrão | Descrição Padrão | CTA Sugerido |
| :--- | :--- | :--- | :--- | :--- |
| `aegis-data-table` | `inbox` / `search-off` | "Nenhum ativo encontrado" | "Tente ajustar os filtros ou cadastre o primeiro ativo." | `<aegis-button variant="primary">Novo Ativo</aegis-button>` |
| `aegis-card` (grid) | `box` | "Nenhum item" | "Não há itens para exibir no momento." | — |
| `aegis-filter-bar` | `filter-off` | "Nenhum filtro ativo" | "Todos os registros estão sendo exibidos." | `<aegis-button variant="ghost" size="sm">Limpar filtros</aegis-button>` |
| `aegis-audit-log-table` | `history` | "Sem registros de auditoria" | "As alterações aparecerão aqui automaticamente." | — |
| `aegis-login-form` | `lock` | "Acesso restrito" | "Faça login para acessar o sistema." | Formulário de login (não CTA separado) |

**Tom de voz:** Direto, acionável, sem jargão técnico. Ver [ui-style-guide.md](ui-style-guide.md) (não gerado — gap conhecido).

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Ilustrações SVG não existem. Decisão pendente: ícones do sistema (lucide/heroicons) vs. ilustrações customizadas. MVP pode usar apenas ícones.

### 4.3 Error States

| Tipo | Comportamento Visual | Componente(s) | Acessibilidade |
| :--- | :--- | :--- | :--- |
| **Erro de campo (inline)** | `border: 1px solid var(--color-danger)` + `aria-invalid="true"` + `aria-describedby="field-error-id"`<br>Mensagem: `var(--color-danger)` + `var(--text-sm)` + ícone `alert-circle` (16px) abaixo do input<br>Animação: `max-height: 0 → auto` + `opacity: 0 → 1` (`--duration-base`) | `aegis-input`, `aegis-select`, `aegis-textarea`, `aegis-date-picker`, `aegis-checkbox`, `aegis-radio-group` | `role="alert"` na mensagem (polite), `aria-live="polite"` no container de erros do form |
| **Erro de página/bloqueante** | Tela cheia: `<aegis-alert-banner variant="danger" persistent>` no topo + `aegis-empty-state` com ícone `alert-triangle`<br>Botão "Tentar novamente" (`aegis-button variant="primary"`) recarrega dados | `aegis-data-table`, `aegis-card` (grid), `aegis-stat-card` | `role="alert"` no banner, focus no botão "Tentar novamente" ao renderizar |
| **Erro de rede/timeout** | `aegis-toast.error` persistente (`persistent: true`) + botão "Tentar novamente" no toast<br>Se erro crítico (auth): redirect para login com `returnUrl` | `aegis-toast`, `aegis-protected-route`, `aegis-login-form` | `role="alert"` no toast, `aria-live="assertive"` |
| **Erro de validação de formulário (submit)** | Scroll suave para primeiro campo inválido (`scrollIntoView({ behavior: 'smooth', block: 'center' })`) + focus no campo + shake no container (`--duration-fast` × 3) | `aegis-form`, `aegis-modal` (variant="form") | `aria-invalid` nos campos, focus management obrigatório |

**Retry Pattern (erro de rede):**
1. Toast persistente com botão "Tentar novamente"
2. Click → `loading=true` no botão original (se identificável) ou novo toast `info` "Tentando novamente..."
3. Sucesso → toast `success` "Recuperado automaticamente"
4. Falha novamente → toast `error` persistente + link "Reportar problema" (mailto/sentry)

---

## 5. Feedback do Sistema

| Tipo | Canal | Duração/Persistência | Comportamento Adicional |
| :--- | :--- | :--- | :--- |
| **Sucesso** (create, update, delete, export) | `aegis-toast.success` (bottom-right) | Auto-dismiss 5s (`--duration-toast: 5000ms`) | Progress bar visual; hover pausa timer; action "Desfazer" opcional (ex: delete) |
| **Aviso** (threshold health, sync pendente) | `aegis-toast.warning` + `aegis-badge` no item relacionado | 10s (health) / persistente até ação (sync) | Health: badge `warning` no `aegis-health-indicator` + toast; Sync: banner `aegis-alert-banner variant="warning"` no header |
| **Erro** (API 5xx, validação, permissão) | `aegis-toast.error` (persistente) + inline field error (se aplicável) | Persistente até dismiss ou retry | 5xx: "Falha no servidor. Tentar novamente?" + botão retry; 403: "Sem permissão" + link "Solicitar acesso"; Validação: inline + focus management |
| **Confirmação destrutiva** (delete, purge, revoke) | `aegis-confirm-dialog` (variant="destructive") + `require-typing` opcional | Modal bloqueante — sem auto-dismiss | `require-typing="EXCLUIR"` habilita botão destroy apenas após digitação exata; focus no input typing; `Esc` cancela |

**Tokens de feedback (referência [design-tokens.md](design-tokens.md)):**
- `--color-success`, `--color-success-light`, `--color-danger`, `--color-danger-light`, `--color-warning`, `--color-warning-light`, `--color-info`, `--color-info-light`
- `--duration-toast: 5000ms` (proposto — não está no tokens atual)
- `--z-toast: 1000`, `--z-modal: 900`, `--z-dropdown: 800`, `--z-overlay: 700`, `--z-sidebar: 600`

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — `--duration-toast` e z-index scale não existem no design-tokens.md atual. Precisam ser adicionados.

---

## 6. Acessibilidade de Interação

| Requisito | Implementação | Componentes Afetados | Validação |
| :--- | :--- | :--- | :--- |
| **Foco visível em toda interação por teclado** | `:focus-visible { outline: 2px solid var(--color-accent-primary); outline-offset: 2px; }` — **nunca** `outline: none` sem substituição | **Todos** (botões, inputs, links, rows clicáveis, tabs, dropdown items) | axe-core + teste manual Tab/Shift+Tab |
| **`prefers-reduced-motion` respeitado** | `@media (prefers-reduced-motion: reduce) { *, *::before, *::after { animation-duration: 0.01ms !important; transition-duration: 0.01ms !important; } }` + JS `matchMedia` para desativar `will-change` e animações JS | Modal, dropdown, toast, sidebar, table row expand, button press, switch toggle | Teste: System Preferences → Accessibility → Reduce motion |
| **Anúncio de mudanças dinâmicas para screen readers** | `aria-live="polite"`: toasts success/info, contadores, status badges<br>`aria-live="assertive"`: toasts error/warning, validação inline, confirmação destrutiva<br>`aria-atomic="true"` em containers de toast/banner | `aegis-toast`, `aegis-alert-banner`, `aegis-badge`, `aegis-health-indicator`, `aegis-form` (error summary) | NVDA (Windows), VoiceOver (macOS/iOS), JAWS (Windows) |
| **Navegação por teclado em componentes compostos** | **Modal:** Tab trap, `Esc` fecha, focus restore ao fechar<br>**Dropdown:** `Esc`/`Tab`/`Shift+Tab` fecha, `ArrowUp/Down` navega items, `Enter`/`Space` seleciona<br>**Table:** `ArrowUp/Down` navega rows, `Space` seleciona, `Enter` abre detalhes (se `row-clickable`)<br>**Tabs:** `ArrowLeft/Right` navega tabs, `Home`/`End` first/last, `Tab` entra no painel | `aegis-modal`, `aegis-dropdown`, `aegis-select`, `aegis-data-table`, `aegis-tabs`, `aegis-sidebar` | Teste completo teclado-only em cada componente |
| **Contraste em estados animados** | Cores de hover/focus/active/loading mantêm **WCAG AA 4.5:1** contra background<br>Loading spinner: `var(--color-text-primary)` sobre `var(--btn-{variant}-bg)` — validar contraste | `aegis-button`, `aegis-switch`, `aegis-input`, `aegis-badge`, `aegis-toast` | axe-core + verificação manual em modo high contrast |

---

## 7. Anti-padrões (o que evitar)

| Anti-padrão | Por que Evita | Alternativa Correta |
| :--- | :--- | :--- |
| **Animações > 400ms em ações frequentes** (button hover, table row hover, tooltip show) | Percebido como lag; acumula delay em uso intenso | Máximo `--duration-fast` (150ms) para micro-interações; `0ms` para hover de alta frequência |
| **Loading sem feedback visual acima de 300ms** | Usuário acha que travou; clica novamente (double submit) | Skeleton imediato (0ms) para listas; spinner no botão para ações pontuais; `aria-busy="true"` sempre |
| **Animação de `width`/`height`/`top`/`left`** | Force layout/reflow → jank, não 60fps | Apenas `transform` + `opacity`; `max-height` para collapse (aceitável se raro) |
| **Toast sem `aria-live` ou com `role="status"` para erro** | Screen reader não anuncia erro crítico | `role="alert"` + `aria-live="assertive"` para error/warning; `role="status"` + `aria-live="polite"` para success/info |
| **Focus trap ausente em modal/drawer** | Teclado "vaza" para background; usuário perde contexto | `inert` no background (polyfill se necessário) + focus cycle interno + restore ao fechar |
| **`outline: none` sem `:focus-visible` replacement** | Usuário teclado não vê onde está | Sempre `:focus-visible` com outline visível (2px, offset 2px, cor accent) |
| **Animação contínua sem `prefers-reduced-motion` check** (spinner infinite, pulse skeleton) | Causa náusea/distração em usuários vestibulares | `@media (prefers-reduced-motion: reduce) { .aegis-loading--pulse { animation: none; opacity: 0.6; } .aegis-spinner { animation: none; } }` |
| **Mudança de layout causada por animação** (button loading muda largura, toast empurra conteúdo) | Layout shift → cliques acidentais, desorientação | Button loading: `width` fixa (min-width do label) + `color: transparent`; Toast: container fixed, não afeta layout |

---

## 8. Referências

* **Component Library / Inventory:** [component-library.md](component-library.md) — 29 componentes propostos com anatomia, slots, eventos e tokens consumidos (Seção 4 — Especificação por Componente)
* **Design Tokens:** [design-tokens.md](design-tokens.md) — fonte única de verdade para `--duration-*`, `--easing-*`, `--color-*`, `--shadow-*`, `--radius-*`, `--space-*`, `--z-*`, `--text-*` **(arquivo inferido — não implementado)**
* **User Flows:** [user-flows.md](user-flows.md) — 6 fluxos detalhados que ditam quando cada padrão se aplica
* **Diagnóstico Determinístico:** 348 arquivos Java (backend), 15 arquivos JS (frontend/services), **zero componentes UI existentes**
* **Stack Tecnológica Verificada:** JavaScript vanilla (ESM), `@popperjs/core` ^2.11.8 (única dependência UI), **sem build tool configurado** (Vite/Webpack necessário para tokens + componentes)
* **Acessibilidade:** WCAG 2.1 AA — todos os padrões devem passar axe-core + teste manual NVDA/VoiceOver
* **Próximos Passos Imediatos (para tornar isto implementável):**
  1. **Criar `design-tokens.md` real** com valores numéricos para todos os tokens referenciados acima (`--duration-fast: 150ms`, `--easing-standard: cubic-bezier(0.4, 0, 0.2, 1)`, etc.)
  2. **Configurar `package.json`** com `type: "module"`, scripts `dev`, `build`, `test` (Vite + Vitest)
  3. **Implementar pipeline de tokens** (Style Dictionary ou script custom) → CSS custom properties + JS tokens module
  4. **Implementar componentes core** (`aegis-button`, `aegis-input`, `aegis-modal`, `aegis-toast`, `aegis-data-table`) **aplicando estes padrões**
  5. **Adicionar testes de regressão visual** (Chromatic/Playwright) + CI com `prefers-reduced-motion` simulation

---

## 9. Declaração de Limitações

> **Este documento NÃO reflete padrões implementados.** O codebase atual **não possui nenhum componente de UI, sistema de design, ou animação** — apenas 15 arquivos JS de serviços (`api.js`, etc.).  
>   
> Os padrões acima são **100% inferidos** a partir dos componentes propostos em [component-library.md](component-library.md) (que por sua vez são inferidos dos user flows + BRD) e dos design tokens inferidos em [design-tokens.md](design-tokens.md).  
>   
> **Nenhuma decisão de arquitetura de frontend foi tomada** (Web Components vs. módulos JS vs. framework leve). A convenção "Web Components" no component-library é uma **proposta**, não uma realidade.  
>   
> **Validação humana obrigatória** antes de qualquer implementação: aprovação de durações/easings, escolha de tooling, estratégia de SSR/SSG (se houver), priorização de componentes do MVP.