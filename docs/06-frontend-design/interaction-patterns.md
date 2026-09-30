# Interaction & Motion Patterns — Aegis Patrimonio

> **Versão:** 1.0 · **Owner:** Frontend Lead · **Status:** Draft  
> **Depende de:** `component-library.md`, `design-tokens.md`

## 1. Overview

Este documento define os **padrões de microinteração, animação e feedback** que garantem consistência de comportamento entre todos os componentes e telas do Aegis Patrimonio. Como a aplicação é **vanilla JavaScript (ES6 modules)** sem framework de UI, cada componente implementa sua própria lógica de animação consumindo **CSS Custom Properties** definidas em `design-tokens.md` — não há camada de abstração de motion (ex: Framer Motion, Animate.css).

Os padrões abaixo derivam diretamente das **especificações de componentes** em `component-library.md` (seção 4) e dos **tokens de motion** em `design-tokens.md`. **Nenhum componente UI existe no codebase atual** (diagnóstico: 0 arquivos de componente em `frontend/src/components/`); este documento serve como especificação para implementação.

> **Princípio norteador:** Animação **comunica hierarquia, causalidade e estado** — nunca é puramente decorativa. Cada movimento tem propósito funcional (feedback, orientação espacial, indicação de progresso).

---

## 2. Princípios de Motion

| Princípio | Definição | Aplicação no Projeto |
| :--- | :--- | :--- |
| **Propósito funcional** | Toda animação responde a uma ação do usuário ou mudança de estado do sistema | Button press (confirma clique), Modal slideUp (origem espacial), Toast slideIn (entrada não-bloqueante), Sidebar collapse (mudança de layout) |
| **Duração baseada em distância/complexidade** | Movimentos curtos/simples = rápido; longos/complexos = mais lento | `--duration-instant` (50ms): press, hover; `--duration-fast` (150ms): tooltip, dropdown, focus ring; `--duration-base` (250ms): modal, drawer, toast, sidebar; `--duration-slow` (350ms): transições de página (futuro) |
| **Easing consistente** | `ease-out` para entradas (natural), `ease-in` para saídas (rápido), `ease-in-out` para transições de estado | Definido em tokens: `--ease-out`, `--ease-in`, `--ease-in-out` |
| **Respeito a `prefers-reduced-motion`** | **Obrigatório** — todas as animações CSS usam `@media (prefers-reduced-motion: reduce)` para desativar/acelerar | Implementado via `design-tokens.md` (ver seção Motion Tokens) |
| **Performance first** | Apenas propriedades `transform` e `opacity` animadas (GPU); nada de `width`, `height`, `top`, `left` | Sidebars usam `transform: translateX`; modais usam `transform: translateY`; tooltips usam `transform: translateY` + `opacity` |
| **Sem animações em loop contínuo** | Exceto spinners de loading e skeleton pulse/wave — ambos pausáveis via `prefers-reduced-motion` | `Skeleton` component: `animation: pulse var(--duration-slow) ease-in-out infinite` → `animation: none` em reduced motion |

> **Tokens de motion (fonte única):** `design-tokens.md` <!-- source: design-tokens.md#motion-tokens --> define: `--duration-instant: 50ms`, `--duration-fast: 150ms`, `--duration-base: 250ms`, `--duration-slow: 350ms`, `--ease-out: cubic-bezier(0.25, 0.46, 0.45, 0.94)`, `--ease-in: cubic-bezier(0.55, 0.06, 0.68, 0.19)`, `--ease-in-out: cubic-bezier(0.42, 0, 0.58, 1)`.

---

## 3. Padrões por Tipo de Interação

### 3.1 Feedback de Ação (Clique/Toque/Teclado)

| Ação | Componente(s) | Feedback Visual | Duração | Token Usado |
| :--- | :--- | :--- | :--- | :--- |
| **Clique em Button** | `Button`, `IconButton` | `transform: scale(0.98)` no `mousedown` → `scale(1)` no `mouseup`/`mouseleave` | 50ms | `--duration-instant`, `--ease-out` |
| **Hover em Button** | `Button`, `IconButton` | `background-color` transition (variant-specific) | 150ms | `--duration-fast`, `--ease-out` |
| **Focus visível (teclado)** | **Todos os interativos** | `box-shadow: var(--shadow-focus)` (3px outline acessível) — **não** `outline: none` | 0ms (instant) | `--shadow-focus` |
| **Toggle Switch** | `Switch` | Thumb `transform: translateX(0 → 20px)` + `background-color` do track | 150ms | `--duration-fast`, `--ease-in-out` |
| **Checkbox/Radio** | `Checkbox`, `RadioGroup` | `transform: scale(0.8 → 1.1 → 1)` no check + `border-color`/`background` | 150ms | `--duration-fast`, `--ease-out` |
| **Select/Dropdown abrir** | `Select`, `Dropdown`, `DatePicker` | Dropdown `opacity: 0 → 1` + `transform: translateY(-4px → 0)` (Popper.js `flip`/`shift`) | 150ms | `--duration-fast`, `--ease-out` |
| **Tab change** | `Tabs` | Indicator `transform: translateX` suave entre abas | 150ms | `--duration-fast`, `--ease-in-out` |
| **Drag & drop (row reorder — futuro)** | `DataTable` (planejado) | Row `opacity: 0.5` + `box-shadow: var(--shadow-lg)` + `transform: rotate(2deg)` | 150ms | `--duration-fast` |

> **Nota:** `Button.loading` exibe spinner CSS (`@keyframes spin { to { transform: rotate(360deg) } }`) com `animation: spin var(--duration-base) linear infinite` — **pausado** em `prefers-reduced-motion`.

### 3.2 Transições de Estado

| Transição | Comportamento Detalhado | Componentes Afetados | Duração |
| :--- | :--- | :--- | :--- |
| **Idle → Loading** | Botão: label `opacity: 0`, spinner `opacity: 1` + `transform: scale(1)`; Input/Select: skeleton shimmer no placeholder; DataTable: `Skeleton` rows (3-5) com `animation: pulse` | `Button`, `Input`, `Select`, `DataTable`, `FormLayout` | 150ms (entrada skeleton) |
| **Loading → Sucesso** | Botão: spinner `opacity: 0`, check icon `scale(0 → 1)` + `opacity: 1` → após 500ms `hide()`; Toast: `slideInRight` + progress bar | `Button`, `Toast`, `Modal` (fecha) | 250ms + 500ms hold |
| **Loading → Erro** | Botão: spinner `opacity: 0`, label `opacity: 1` + `shake` (`translateX: -4px, 4px, -4px, 0`); Input: `border-color: var(--error-border-color)` + `shake`; Toast `variant="danger"` persistente | `Button`, `Input`, `Select`, `FormLayout`, `Toast` | 250ms (shake: 300ms) |
| **Expandir/Colapsar (Sidebar)** | `width: 260px ↔ 72px` + labels `opacity: 1 ↔ 0` + `pointer-events`; submenu `max-height: 0 ↔ auto` | `Sidebar`, `NavItem` (submenu) | 250ms |
| **Abrir/Fechar Modal** | Backdrop: `opacity: 0 → 1`; Container: `translateY(20px → 0)` + `opacity: 0 → 1` (abrir) / `translateY(0 → -20px)` + `opacity: 1 → 0` (fechar) | `Modal`, `Drawer` | 250ms |
| **Toast entrada/saída** | Entrada: `translateX(100% → 0)` + `opacity: 0 → 1`; Saída: `translateX(0 → 100%)` + `opacity: 1 → 0`; Progress bar: `width: 100% → 0` linear | `Toast` | 250ms (entrada/saída), `duration` prop (progress) |
| **Tooltip show/hide** | `opacity: 0 → 1` + `translateY(-4px → 0)` (top placement) / `translateY(4px → 0)` (bottom) | `Tooltip` | 150ms |
| **Accordion (submenu/FAQ)** | `max-height: 0 → scrollHeight` + `opacity: 0 → 1` do conteúdo | `Sidebar` (submenu), `Accordion` (futuro) | 250ms |

### 3.3 Navegação

| Padrão | Comportamento | Duração | Tokens |
| :--- | :--- | :--- | :--- |
| **Transição entre páginas (SPA)** | *Não implementado ainda* — planejado: `main` content `opacity: 1 → 0` → troca rota → `opacity: 0 → 1` + `translateY(8px → 0)` | 250ms (planejado) | `--duration-base`, `--ease-in-out` |
| **Abertura de Modal/Dialog** | Backdrop fade-in + container slide-up (ver 3.2) | 250ms | `--duration-base`, `--ease-out` |
| **Abertura de Drawer (Sidebar mobile)** | Backdrop fade-in + `translateX(-100% → 0)` | 250ms | `--duration-base`, `--ease-out` |
| **Toast/Notificação** | Stack no `bottom-right` (`var(--toast-offset)`); entrada `slideInRight`; auto-dismiss com progress bar linear; hover pausa progress; click fora/btn fecha `slideOutRight` | Entrada: 250ms; Exibição: `duration` prop (default 5000ms); Saída: 150ms | `--duration-base`, `--duration-fast`, `--toast-duration` |
| **Breadcrumb navigation** | Sem animação — navegação instantânea | 0ms | — |
| **Pagination change** | `DataTable` rows `opacity: 1 → 0` → troca dados → `opacity: 0 → 1` (stagger 30ms/row opcional) | 150ms + stagger | `--duration-fast` |

---

## 4. Estados Padrão de Componentes com Dados

### 4.1 Loading States

| Componente | Padrão de Loading | Quando Usar Skeleton vs Spinner |
| :--- | :--- | :--- |
| **Button** | Spinner interno (substitui label) — `loading` prop | Ações pontuais (submit, save, delete) — **spinner** |
| **Input / Select / Textarea** | Skeleton shimmer no placeholder (`background: linear-gradient(90deg, var(--color-bg-tertiary) 25%, var(--color-bg-secondary) 50%, var(--color-bg-tertiary) 75%)` + `animation: shimmer 1.5s infinite`) | Carregamento de dados assíncronos (ex: `Select.loadOptions`) — **skeleton** |
| **DataTable** | 3-5 `Skeleton` rows (`variant="table"`) com `animation: pulse var(--duration-slow) ease-in-out infinite` | Listas server-side (paginação, filtros, sort) — **skeleton rows** |
| **Modal / Drawer** | Spinner centralizado no body se conteúdo carrega assincronamente | Formulários que buscam dados antes de abrir (raro) — **spinner** |
| **Card / Avatar / Badge** | `Skeleton` `variant="card" \| "avatar" \| "text"` | Listas de cards, grids, tabelas com células ricas — **skeleton** |
| **FormLayout** | Cada field mostra seu próprio skeleton (Input/Select) | Formulários grandes com dados pré-carregados — **skeleton por campo** |

> **Regra:** **Skeleton** para "estrutura conhecida, dados pendentes" (preserva layout, evita layout shift). **Spinner** para "ação em andamento, duração incerta" (botões, ações pontuais). **Nunca** ambos simultâneos no mesmo componente.

### 4.2 Empty States

**Estrutura padrão (componente `EmptyState`):**
```html
<div class="empty-state" role="status" aria-live="polite">
  <div class="empty-state__icon" aria-hidden="true"><!-- SVG ilustração --></div>
  <h3 class="empty-state__title">Título contextual</h3>
  <p class="empty-state__description">Descrição acionável — o que o usuário pode fazer</p>
  <div class="empty-state__action"><!-- Button primário opcional --></div>
</div>
```

| Contexto | Título | Descrição | CTA (Button) |
| :--- | :--- | :--- | :--- |
| **DataTable sem resultados (filtros ativos)** | "Nenhum ativo encontrado" | "Ajuste os filtros ou limpe a busca para ver todos os ativos." | "Limpar filtros" (secondary) |
| **DataTable vazio (primeiro acesso)** | "Nenhum ativo cadastrado" | "Comece cadastrando o primeiro ativo do patrimônio." | "Novo Ativo" (primary) |
| **Select sem opções** | "Nenhuma opção disponível" | "Cadastre uma filial/departamento/tipo antes de selecionar." | Link para cadastro (ghost) |
| **Busca global sem resultados** | "Nenhum resultado para 'termo'" | "Verifique a grafia ou tente termos mais genéricos." | — |
| **Erro de carregamento (retry)** | "Não foi possível carregar" | "Verifique sua conexão e tente novamente." | "Tentar novamente" (primary) |

**Tom de voz:** Direto, útil, sem jargão técnico — alinhado a `ui-style-guide.md` (seção Voice & Tone) <!-- source: ui-style-guide.md#voice-tone -->. **Sempre** oferece próximo passo acionável.

### 4.3 Error States

| Tipo | Padrão Visual | Comportamento | Componentes |
| :--- | :--- | :--- | :--- |
| **Erro de campo (inline)** | `Input`/`Select`/`Textarea`: `border-color: var(--error-border-color)` + `box-shadow: 0 0 0 3px rgb(220 38 38 / 0.15)` no focus; `error-text` abaixo com `color: var(--error-text-color)`, `role="alert"`, `aria-live="polite"` | Mostra no `blur` (validação) ou após submit falho; limpa no `input`/`change` | `Input`, `Select`, `Textarea`, `DatePicker`, `FileUpload` |
| **Erro de formulário (submit)** | `FormLayout`: scroll suave para primeiro campo com erro + `focus()`; toast `variant="danger"` global + erros inline | `FormLayout.onSubmit` captura `validationErrors` do backend e mapeia para fields | `FormLayout`, `Modal` (formulários) |
| **Erro de página/bloqueante** | Tela dedicada (`ErrorPage`): ilustração + título "Ops, algo deu errado" + mensagem + `Button` "Tentar novamente" / "Voltar ao dashboard" | Renderizada no `main` quando rota falha criticamente (404, 500, auth) | `App` (router) |
| **Erro de rede/timeout** | Toast `variant="danger"` **persistente** (`persistent: true`) com `action: { label: 'Tentar novamente', onClick: retryFn }` | Não auto-dismiss; usuário decide retry ou ignorar | `Toast`, `api.js` (interceptor) |
| **Erro de validação assíncrona** (ex: tag duplicada) | Campo mostra erro inline **após** resposta do backend; `asyncValidator` em `FormField` | Debounce 300ms no `blur`/`change`; loading sutil no campo | `Input` (tag, email), `FormLayout` |

> **Tokens de erro:** `--error-border-color: var(--color-danger)`, `--error-text-color: var(--color-danger)`, `--error-bg: var(--color-danger-bg)` (definidos em `design-tokens.md` <!-- source: design-tokens.md#semantic-colors -->).

---

## 5. Feedback do Sistema

| Tipo | Canal | Duração/Persistência | Gatilhos Comuns |
| :--- | :--- | :--- | :--- |
| **Sucesso** | `Toast` (bottom-right, `variant="success"`) | Auto-dismiss 5000ms (configurável); progress bar linear | Criação/edição/exclusão de ativo, manutenção, usuário; importação CSV; geração de relatório |
| **Aviso** | `Toast` (`variant="warning"`) ou `Alert` inline (em formulário) | Toast: 7000ms; Alert: persistente até correção | Validação de negócio (ex: "Ativo em manutenção não pode ser baixado"), licença próxima do vencimento |
| **Erro** | `Toast` (`variant="danger"`, `persistent: true`) + `action: { label: 'Tentar novamente' }` | **Não auto-dismiss**; exige ação do usuário | Falha de API (5xx, network), validação de unicidade (tag duplicada), permissão negada |
| **Confirmação destrutiva** | `Modal` (`size="sm"`, `title="Confirmar exclusão"`, `footerActions: [Cancelar (secondary), Excluir (danger)]`) | Persistente até decisão; `trapFocus: true`; ESC/backdrop = cancelar | Excluir ativo, manutenção, usuário, filial; baixar ativo; cancelar manutenção em andamento |
| **Informação contextual** | `Tooltip` (hover/focus), `Popover` (click), `Alert` banner (topo da página) | Tooltip: delay 200ms show/hide; Alert: dismissible opcional | Ícones de ajuda, truncamento de texto em tabela, avisos de sistema (manutenção programada) |
| **Progresso de longa duração** | `Toast` com `duration: 0` (persistente) + `progress` indeterminado ou `Button.loading` | Até conclusão; toast mostra "Processando... 45%" se progresso conhecido | Importação CSV (>100 linhas), geração de relatório PDF, backup/restore |

> **ToastManager** (singleton) expõe `toast.success()`, `toast.warning()`, `toast.danger()`, `toast.info()` — usado em **todos os fluxos** (Flow 2–7) <!-- source: component-library.md#4.7-toast -->.

---

## 6. Acessibilidade de Interação

| Requisito | Implementação | Verificação |
| :--- | :--- | :--- |
| **Foco visível em toda interação por teclado** | **Todos** os componentes interativos (`Button`, `Input`, `Select`, `Modal`, `Sidebar`, `Tabs`, `Dropdown`, `Tooltip` trigger) têm `box-shadow: var(--shadow-focus)` no `:focus-visible` — **nunca** `outline: none` sem substituição | Teste manual: `Tab`/`Shift+Tab` em todas as telas; axe-core `focus-visible` |
| **`prefers-reduced-motion` respeitado** | `design-tokens.md` define `@media (prefers-reduced-motion: reduce) { *, *::before, *::after { animation-duration: 0.01ms !important; transition-duration: 0.01ms !important; } }` — aplicado globalmente no `tokens.css` | Teste: OS settings → Accessibility → Reduce motion → todas animações instantâneas |
| **Anúncio de mudanças dinâmicas (screen readers)** | `Toast`: `role="status"` (success/info) ou `role="alert"` (warning/danger) + `aria-live="polite"`/`assertive`; `Modal`: `role="dialog"`, `aria-modal="true"`, `aria-labelledby="modal-title"`; `DataTable` sort/filter: `aria-live="polite"` no contador de resultados; `EmptyState`: `role="status"`, `aria-live="polite"` | Teste: NVDA/JAWS/VoiceOver — anúncios corretos sem duplicação |
| **Gerenciamento de foco em modais/drawers** | `Modal.open()`: salva `document.activeElement`, foca primeiro elemento focável interno, `trapFocus` loop; `Modal.close()`: restaura foco salvo | Teste: `Tab` dentro do modal não escapa; `Esc` fecha e foco retorna ao trigger |
| **Contraste em estados** | Todos os estados (hover, focus, active, error, disabled) mantêm **WCAG 2.1 AA** (4.5:1 texto, 3:1 UI components) — validado em `design-tokens.md` | Ferramenta: axe-core, Lighthouse, manual color contrast analyzer |
| **Navegação por setas em componentes compostos** | `Select`/`Dropdown`/`DatePicker`/`Tabs`/`RadioGroup`: `ArrowUp/Down/Left/Right`, `Home/End`, `Enter/Space`, `Esc` seguem WAI-ARIA Authoring Practices | Teste: teclado apenas — todos os padrões ARIA funcionais |

---

## 7. Anti-padrões (o que evitar)

| Anti-padrão | Por que Evita | Alternativa Correta |
| :--- | :--- | :--- |
| **Animações > 400ms em ações frequentes** (botões, toggles, hover) | Gera latência percebida, cansaço cognitivo | Máximo `--duration-base` (250ms) para transições de estado; `--duration-fast` (150ms) para micro-feedback |
| **Loading sem feedback visual > 300ms** | Usuário acha que travou; clica novamente (double submit) | Skeleton imediato (0ms) para listas; spinner em botão no `mousedown` se ação > 200ms |
| **Animação de `width`/`height`/`top`/`left`** | Force layout/reflow — jank em mobile/baixo desempenho | Apenas `transform` + `opacity`; `max-height` para accordion (única exceção aceita) |
| **Auto-dismiss de erro crítico** | Usuário perde informação de falha; não sabe como recuperar | `Toast.persistent: true` + action "Tentar novamente" para erros de rede/validação |
| **Mudança de conteúdo sem anúncio (aria-live)** | Screen reader não informa atualização (ex: filtro aplicado, total de resultados) | `aria-live="polite"` em contadores, toasts, empty states |
| **Focus trap ausente em modal/drawer** | Teclado "vaza" para background; usuário perde contexto | `Modal.trapFocus: true` obrigatório; `Drawer` idem |
| **Tooltip em elemento sem `tabindex` (ex: `span`, `div`)** | Usuário teclado nunca vê o tooltip | Tooltip só em elementos focáveis nativamente (`button`, `a`, `input`) ou com `tabindex="0"` + `role="button"` |
| **Skeleton com layout diferente do conteúdo real** | Layout shift (CLS) quando dados chegam | Skeleton **espelha exatamente** estrutura final (mesmo grid, mesmos tamanhos) |
| **Múltiplos toasts simultâneos sem stack** | Poluição visual; usuário não lê nenhum | `ToastManager` faz stack vertical (`gap: var(--space-2)`), max 3 visíveis, fila os demais |

---

## 8. Referências

* **Component Library / Inventory:** `component-library.md` (seções 3, 4) — especificação completa de 27 componentes com animações definidas por componente <!-- source: component-library.md#4 -->
* **Design Tokens:** `design-tokens.md` — tokens de motion (`--duration-*`, `--ease-*`), cores semânticas, z-index, breakpoints, sombras <!-- source: design-tokens.md -->
* **User Flows:** `user-flows.md` — define quando cada padrão se aplica (Flow 1–7) <!-- source: user-flows.md -->
* **UI Style Guide:** `ui-style-guide.md` — voice & tone para empty/error states <!-- source: ui-style-guide.md#voice-tone -->
* **Glossário Ubíquo:** `docs/glossario.md` — nomenclatura de domínio nos textos de feedback <!-- source: glossario.md#L1-L75 -->
* **WCAG 2.1 AA Quick Reference:** https://www.w3.org/WAI/WCAG21/quickref/ — checklist aplicado a todos os padrões
* **WAI-ARIA Authoring Practices 1.2:** https://www.w3.org/WAI/ARIA/apg/ — padrões de teclado para `Select`, `Tabs`, `Modal`, `Dropdown`, `DatePicker`
* **Popper.js v2 Docs:** https://popper.js.org/docs/v2/ — posicionamento e estratégias `flip`/`shift`/`offset` usadas em `Tooltip`, `Select`, `Dropdown`, `DatePicker`
* **Diagnóstico Determinístico:** `docs/diagnostico.md` — confirma stack vanilla JS + Popper.js only, 0 componentes implementados <!-- source: diagnostico.md -->

---

> **Próximos passos (action items):**
> 1. [ ] Criar `frontend/src/styles/tokens.css` com todos os CSS Custom Properties (incluindo motion tokens)
> 2. [ ] Implementar `prefers-reduced-motion` media query global no `tokens.css`
> 3. [ ] Definir keyframes globais: `spin`, `pulse`, `shimmer`, `slideInRight`, `slideOutRight`, `slideUp`, `fadeIn`
> 4. [ ] Implementar `Button` com press scale + loading spinner + focus ring (validação visual dos tokens)
> 5. [ ] Implementar `Modal` com backdrop fade + container slideUp + focus trap + ESC handling
> 6. [ ] Implementar `ToastManager` singleton com stack, progress bar, `aria-live` correto
> 7. [ ] Implementar `Sidebar` collapse/expand com `transform` + label opacity + submenu max-height
> 8. [ ] Adicionar testes de acessibilidade (axe-core + Playwright) no CI para cada componente
> 9. [ ] Documentar padrões de motion em `frontend/docs/motion/` com exemplos de código + vídeos de referência