# Component Library / Inventory — Aegis Patrimônio

> **Versão:** 1.0 · **Owner:** Frontend Lead · **Status:** Draft  
> **Baseado em:** Componentes vanilla JS proprietários (sem biblioteca UI externa — apenas `@popperjs/core` para positioning)  
> **Depende de:** [design-tokens.md](design-tokens.md), [user-flows.md](user-flows.md)

---

## 1. Overview

Este documento inventaria os **componentes de interface reutilizáveis** necessários para cobrir os 6 fluxos de usuário definidos em [user-flows.md](user-flows.md), consumindo exclusivamente os design tokens de [design-tokens.md](design-tokens.md).  

**Estado atual do codebase:** O diagnóstico determinístico (348 arquivos Java, 15 arquivos JS em `frontend/src/`) **não revelou nenhuma biblioteca de componentes, sistema de design, ou padrão de componentização**. Os 15 arquivos JS são serviços/utilitários (`api.js`, etc.) — não há arquivos de componente (`.js` com `customElements.define`, classes de UI, ou templates HTML reutilizáveis).  

**Estratégia proposta:** Implementar componentes como **Web Components nativos (Custom Elements)** ou **módulos JS vanilla com template literals**, sem dependência de framework (React/Vue/Svelte). Isso alinha com a stack real (JS vanilla + `@popperjs/core`) e evita build step complexo no MVP.  

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Toda a lista abaixo é uma **proposta inicial** derivada dos fluxos de usuário e tokens. Nenhum componente existe hoje. Requer decisão de arquitetura: Web Components vs. módulos JS + template literals vs. adoção de framework leve (ex: Petite-Vue, Alpine.js) antes de iniciar implementação.

---

## 2. Convenções Gerais

| Convenção | Definição |
| :--- | :--- |
| **Padrão de composição** | Web Components (Custom Elements) com `Shadow DOM` opcional (para isolamento de estilo) + `slot` para composição. Fallback: módulos ES com `render()` retornando HTML string + `attach()` para binding de eventos. |
| **Nomenclatura** | `aegis-*` prefixo (ex: `<aegis-button>`, `<aegis-data-table>`). Classes CSS internas: `.aegis-btn`, `.aegis-table`, etc. |
| **Local no repositório** | `frontend/src/components/` (a criar) — um arquivo por componente: `aegis-button.js`, `aegis-button.css`, `aegis-button.test.js` |
| **Tokens consumidos** | Via CSS Custom Properties (definidas em `:root` no `index.html` ou injetadas via JS no bootstrap). **Nenhum valor hardcoded** permitido. |
| **Acessibilidade** | Obrigatório: `role`, `aria-*`, `tabindex`, foco visível (`:focus-visible`), `aria-live` para estados dinâmicos. Testado com NVDA/VoiceOver. |
| **Variantes** | Controladas via **atributos HTML** (ex: `<aegis-button variant="primary" size="md">`) — não props JS. Reflete no CSS via `[variant="primary"]`. |
| **Estado** | Gerenciado externamente (pai passa dados via atributos/propriedades; componente emite `CustomEvent` para ações: `aegis-submit`, `aegis-delete`, `aegis-status-change`). |
| **Testes** | Unitários com Vitest + `@testing-library/web-components` (a configurar). Histórias no Storybook (opcional, avaliar custo). |

---

## 3. Inventário de Componentes

| Componente | Categoria | Variantes/Estados Principais | Status | Prioridade (Flows) | Localização Proposta |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `aegis-button` | Ação | `variant`: primary, secondary, ghost, destructive, outline<br>`size`: sm, md, lg<br>`loading`, `disabled` | **Proposto** | Todos (1–6) | `components/aegis-button/` |
| `aegis-input` | Formulário | `type`: text, email, password, number, search<br>`state`: default, error, disabled, readonly<br>`label`, `placeholder`, `helper-text`, `leading-icon`, `trailing-icon` | **Proposto** | 1, 2, 4, 6 | `components/aegis-input/` |
| `aegis-select` | Formulário | `multiple`, `searchable`, `clearable`<br>`options` via `<aegis-option>` slots<br>`state`: default, error, disabled | **Proposto** | 1, 2, 4 | `components/aegis-select/` |
| `aegis-date-picker` | Formulário | `mode`: single, range<br>`format`: DD/MM/YYYY<br>`min`, `max`, `disabled-dates` | **Proposto** | 1, 2, 5 | `components/aegis-date-picker/` |
| `aegis-textarea` | Formulário | `rows`, `resize`, `maxlength`, `counter`<br>`state`: default, error, disabled | **Proposto** | 1, 2, 4 | `components/aegis-textarea/` |
| `aegis-checkbox` | Formulário | `indeterminate`, `disabled`<br>Label via slot | **Proposto** | 1, 4 | `components/aegis-checkbox/` |
| `aegis-radio-group` | Formulário | `options` via `<aegis-radio>`<br>`orientation`: horizontal, vertical | **Proposto** | 1, 2, 4 | `components/aegis-radio-group/` |
| `aegis-switch` | Formulário | `checked`, `disabled`, `size` | **Proposto** | 4 (RBAC toggles) | `components/aegis-switch/` |
| `aegis-form` | Formulário | Wrapper com validação nativa (`reportValidity`), submissão via `CustomEvent`<br>Slots: `header`, `fields`, `actions` | **Proposto** | 1, 2, 4, 6 | `components/aegis-form/` |
| `aegis-modal` | Overlay | `size`: sm, md, lg, xl, full<br>`variant`: default, confirmation, form, alert<br>`dismissible`, `close-on-overlay-click`<br>Slots: `header`, `body`, `footer` | **Proposto** | 1, 2, 3, 4, 6 | `components/aegis-modal/` |
| `aegis-dropdown` | Overlay | `trigger`: click, hover<br>`placement` (via Popper.js): top, bottom, left, right + variations<br>Slots: `trigger`, `content` (`<aegis-dropdown-item>`) | **Proposto** | 1, 2, 4 (usa `@popperjs/core`) | `components/aegis-dropdown/` |
| `aegis-tooltip` | Overlay | `placement` (via Popper.js)<br>`delay`: show/hide<br>`variant`: default, info, warning, error<br>Slot: `trigger` | **Proposto** | 1, 2, 3, 5 (usa `@popperjs/core`) | `components/aegis-tooltip/` |
| `aegis-toast` | Feedback | `variant`: success, error, warning, info<br>`duration`: auto (5s), persistent<br>`action`: botão opcional (ex: "Desfazer")<br>Container: `<aegis-toast-container>` (portal) | **Proposto** | Todos (1–6) | `components/aegis-toast/` |
| `aegis-badge` | Feedback | `variant`: success, warning, danger, info, neutral<br>`size`: sm, md<br>`dot`: boolean (indicador circular) | **Proposto** | 1, 2, 3, 5 | `components/aegis-badge/` |
| `aegis-avatar` | Feedback | `src`, `alt`, `fallback` (iniciais)<br>`size`: xs, sm, md, lg, xl<br>`shape`: circle, square<br>`status`: online, offline, busy, away (badge overlay) | **Proposto** | 1, 4, 6 | `components/aegis-avatar/` |
| `aegis-data-table` | Exibição | `columns` (array de config: key, label, sortable, filterable, render)<br>`pagination`: client/server<br>`selection`: single, multiple, none<br>`sort`, `filter`, `row-click`, `row-expand`<br>`empty-state`, `loading-state`, `error-state`<br>`virtualized`: opcional para >1k linhas | **Proposto** | 1, 2, 3, 5 | `components/aegis-data-table/` |
| `aegis-card` | Layout | `variant`: default, outlined, elevated, interactive<br>`padding`: none, sm, md, lg<br>Slots: `header`, `media`, `body`, `footer`, `actions` | **Proposto** | 1, 3, 5 | `components/aegis-card/` |
| `aegis-sidebar` | Navegação | `collapsible`, `collapsed` (state)<br>`items`: array de { label, icon, href, badge, children }<br>`active-item` (rota atual)<br>Slot: `logo`, `user-menu` | **Proposto** | Todos (1–6) | `components/aegis-sidebar/` |
| `aegis-header` | Layout | `title`, `breadcrumbs`, `actions` (slot), `user-menu` (avatar + dropdown)<br>`sticky`, `elevated` | **Proposto** | Todos (1–6) | `components/aegis-header/` |
| `aegis-breadcrumb` | Navegação | `items`: array de { label, href, current }<br>`separator`: chevron, slash, arrow<br>`max-items` (colapso com ellipsis) | **Proposto** | 1, 2, 4, 5 | `components/aegis-breadcrumb/` |
| `aegis-tabs` | Navegação | `variant`: line, enclosed, soft<br>`orientation`: horizontal, vertical<br>`activation`: auto, manual<br>Slots: `tab-list`, `tab-panel` | **Proposto** | 4, 5 | `components/aegis-tabs/` |
| `aegis-empty-state` | Feedback | `illustration`: icon, illustration, none<br>`title`, `description`, `action` (slot para botão/link)<br>`size`: sm, md, lg | **Proposto** | 1, 2, 3, 5 | `components/aegis-empty-state/` |
| `aegis-loading` | Feedback | `variant`: spinner, skeleton, pulse, progress<br>`size`: sm, md, lg<br>`overlay`: boolean (full-screen vs inline) | **Proposto** | Todos (1–6) | `components/aegis-loading/` |
| `aegis-alert-banner` | Feedback | `variant`: info, success, warning, danger<br>`dismissible`, `action` (slot)<br>`persistent`: boolean | **Proposto** | 3, 6 | `components/aegis-alert-banner/` |
| `aegis-health-indicator` | Domínio | `status`: ok, warning, critical, unknown<br>`metric`: disk, memory, network, custom<br>`value`, `threshold`, `unit`<br>`trend`: up, down, stable (sparkline opcional) | **Proposto** | 3 | `components/aegis-health-indicator/` |
| `aegis-stat-card` | Domínio | `label`, `value`, `change` (%, trend), `icon`<br>`variant`: default, highlight, warning<br>`link` (opcional) | **Proposto** | 1, 3, 5 | `components/aegis-stat-card/` |
| `aegis-filter-bar` | Domínio | `filters`: array de { key, label, type: select/date/range/text, options }<br>`on-change` event<br>`collapsed` state (mobile) | **Proposto** | 1, 2, 3, 5 | `components/aegis-filter-bar/` |
| `aegis-export-button` | Ação | `formats`: csv, xlsx, pdf<br>`filename`, `loading` state<br>Integração com backend streaming | **Proposto** | 5 | `components/aegis-export-button/` |
| `aegis-audit-log-table` | Domínio | Extende `aegis-data-table` com colunas fixas: timestamp, user, entity, action, diff<br>`diff-viewer` (modal ao clicar) | **Proposto** | 5 | `components/aegis-audit-log-table/` |
| `aegis-login-form` | Auth | `fields`: email/username, password, remember-me<br>`forgot-password`, `sso-buttons` (slots)<br>`state`: default, loading, error, success<br>Validação cliente + integração `authInterceptor` | **Proposto** | 6 | `components/aegis-login-form/` |
| `aegis-protected-route` | Auth | Wrapper (não visual) — verifica token, roles, permissions<br>Redireciona login com `returnUrl`<br>Emite `aegis-auth-change` | **Proposto** | 6 | `components/aegis-protected-route/` |
| `aegis-confirm-dialog` | Overlay | Especialização de `aegis-modal` para confirmações perigosas<br>`variant`: delete, destructive, warning<br>`require-typing`: string para confirmar (ex: "EXCLUIR") | **Proposto** | 1, 2, 4 | `components/aegis-confirm-dialog/` |

**Total: 29 componentes propostos** — todos **Proposto** (não existem no codebase atual).

---

## 4. Especificação por Componente (Amostra Representativa)

> **Nota:** Por brevidade, detalho 6 componentes críticos. Os demais seguem o mesmo padrão — especificação completa deve ser feita em arquivos individuais (`components/aegis-*/README.md`) durante a implementação.

---

### 4.1 `aegis-button`

* **Propósito:** Ação primária/segundária/destrutiva em formulários, tabelas, modais, toolbars. Substitui `<button>` nativo com estilos consistentes, estados acessíveis e loading integrado.
* **Anatomia:** `<button class="aegis-btn">[<span class="aegis-btn__icon">][<span class="aegis-btn__label"><slot></slot></span>][<span class="aegis-btn__spinner">]</span>`
* **Props/Atributos:**

| Atributo | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `variant` | `primary \| secondary \| ghost \| destructive \| outline` | `primary` | Estilo visual — mapeia tokens `--btn-*-bg`, `--btn-*-hover`, `--btn-*-text` |
| `size` | `sm \| md \| lg` | `md` | Padding/tipografia — `--space-2/3/4`, `--text-sm/base/lg` |
| `disabled` | `boolean` | `false` | Desabilita + `aria-disabled="true"` + cursor not-allowed |
| `loading` | `boolean` | `false` | Mostra spinner, desabilita, mantém largura (evita layout shift) |
| `full-width` | `boolean` | `false` | `width: 100%` |
| `type` | `button \| submit \| reset` | `button` | Tipo nativo do button |
| `aria-pressed` | `boolean` | — | Para botões toggle (ex: favoritar) |

* **Estados visuais:**

| Estado | Comportamento Visual (Tokens) |
| :--- | :--- |
| Default | `background: var(--btn-{variant}-bg); color: var(--btn-{variant}-text); border-radius: var(--radius-md); box-shadow: var(--shadow-sm);` |
| Hover | `background: var(--btn-{variant}-hover);` (exceto `disabled`/`loading`) |
| Focus | `outline: none; box-shadow: var(--shadow-focus), var(--shadow-sm);` |
| Active/Pressed | `transform: scale(0.98);` + `background` mais escuro (10%) |
| Disabled | `opacity: 0.5; cursor: not-allowed; pointer-events: none;` |
| Loading | `color: transparent;` + spinner centralizado (`var(--color-text-primary)`) + `pointer-events: none;` |

* **Tokens consumidos:** `--btn-primary-bg`, `--btn-primary-hover`, `--btn-primary-text`, `--btn-secondary-bg`, `--btn-secondary-hover`, `--btn-danger-bg`, `--radius-md`, `--shadow-sm`, `--shadow-focus`, `--text-sm`, `--text-base`, `--text-lg`, `--space-2`, `--space-3`, `--space-4`, `--duration-fast`, `--color-text-primary`
* **Eventos emitidos:** `aegis-click` (detail: `{ originalEvent }`) — wrapper para não vazar `PointerEvent` nativo
* **Exemplo de uso:**
```html
<aegis-button variant="primary" size="md" loading="false">
  <svg slot="icon" class="aegis-icon" aria-hidden="true"><use href="#icon-plus"></use></svg>
  Novo Ativo
</aegis-button>

<aegis-button variant="destructive" size="sm" disabled>
  Excluir
</aegis-button>
```

---

### 4.2 `aegis-data-table`

* **Propósito:** Tabela de dados densa, acessível, com paginação, ordenação, filtros, seleção e estados vazios/carregamento/erro — coração dos Flows 1, 2, 3, 5.
* **Anatomia:** `<table class="aegis-table"><thead><tr><th><aegis-th>...</aegis-th></tr></thead><tbody><tr><td><slot name="cell-{key}"></slot></td></tr></tbody></table>` + `<aegis-pagination>` + `<aegis-toolbar>` (filtros, busca, ações em lote)
* **Props/Atributos (via propriedades JS no elemento):**

| Propriedade | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `columns` | `ColumnDef[]` | `[]` | `{ key, label, sortable, filterable, width, align, render: (row, cellValue) => string|Node }` |
| `data` | `any[]` | `[]` | Linhas de dados (objetos) |
| `key-field` | `string` | `'id'` | Campo único para `row-key` e seleção |
| `pagination` | `object` | `{ page: 1, pageSize: 20, total: 0, serverSide: false }` | Controle de paginação |
| `selection` | `'none' \| 'single' \| 'multiple'` | `'none'` | Habilita checkbox na primeira coluna |
| `selected-keys` | `Set<string\|number>` | `new Set()` | Linhas selecionadas (bidirecional) |
| `sort-by` | `string` | `null` | Coluna ordenada |
| `sort-dir` | `'asc' \| 'desc'` | `'asc'` | Direção |
| `loading` | `boolean` | `false` | Mostra skeleton rows |
| `error` | `string` | `null` | Mensagem de erro (estado error) |
| `empty-message` | `string` | `'Nenhum registro encontrado'` | Texto do empty state |
| `row-clickable` | `boolean` | `false` | `cursor: pointer` + emite `aegis-row-click` |
| `expandable` | `boolean` | `false` | Linha expansível (slot `expanded-content`) |

* **Estados visuais:**

| Estado | Comportamento |
| :--- | :--- |
| Loading | `tbody` mostra 5 skeleton rows (`aegis-loading variant="pulse"`) + `thead` visível |
| Empty | `tbody` com 1 row `colspan="100%"` + `<aegis-empty-state>` centralizado |
| Error | `tbody` com 1 row `colspan="100%"` + `<aegis-alert-banner variant="danger">` + botão "Tentar novamente" |
| Hover row | `background: var(--table-row-hover);` (exceto selecionada) |
| Selected row | `background: var(--color-accent-light);` + checkbox marcado |
| Focus cell | `outline: none; box-shadow: inset 0 0 0 2px var(--color-accent-primary);` (navegação por teclado) |

* **Tokens consumidos:** `--table-header-bg`, `--table-row-hover`, `--table-border`, `--color-border-light`, `--color-text-primary`, `--color-text-secondary`, `--color-text-muted`, `--color-accent-primary`, `--color-accent-light`, `--radius-none`, `--shadow-xs`, `--space-2`, `--space-3`, `--space-4`, `--text-sm`, `--text-base`, `--duration-fast`
* **Slots:** `header-actions` (toolbar), `cell-{key}` (render customizado por coluna), `expanded-content` (linha expandida), `pagination-info`, `page-size-select`
* **Eventos emitidos:** `aegis-sort` (`{ key, dir }`), `aegis-page-change` (`{ page, pageSize }`), `aegis-selection-change` (`{ keys: Set }`), `aegis-row-click` (`{ row, key }`), `aegis-filter-change` (`{ filters: Record<string, any> }`)
* **Exemplo de uso:**
```html
<aegis-data-table 
  key-field="id"
  selection="multiple"
  pagination='{"page":1,"pageSize":20,"total":0,"serverSide":true}'
  row-clickable
>
  <aegis-column key="tag" label="Tag" sortable filterable width="120"></aegis-column>
  <aegis-column key="nome" label="Nome" sortable filterable></aegis-column>
  <aegis-column key="tipo" label="Tipo" filterable render="renderTipo"></aegis-column>
  <aegis-column key="status" label="Status" filterable render="renderStatusBadge"></aegis-column>
  <aegis-column key="filial" label="Filial" sortable filterable></aegis-column>
  <aegis-column key="updatedAt" label="Atualizado" sortable align="right" render="renderDate"></aegis-column>
  
  <template slot="cell-status" data-row="{{row}}">
    <aegis-badge variant="{{row.statusVariant}}">{{row.statusLabel}}</aegis-badge>
  </template>
  
  <div slot="header-actions">
    <aegis-filter-bar filters="[[tableFilters]]" on-change="handleFilterChange"></aegis-filter-bar>
    <aegis-button variant="primary" on-click="openCreateModal">Novo Ativo</aegis-button>
  </div>
</aegis-data-table>
```

---

### 4.3 `aegis-modal`

* **Propósito:** Overlay acessível para formulários, confirmações, detalhes — usado em Flows 1, 2, 3, 4, 6. Gerencia foco trap, `Esc` para fechar, `aria-modal`, backdrop.
* **Anatomia:** `<div class="aegis-modal" role="dialog" aria-modal="true" aria-labelledby="modal-title"><div class="aegis-modal__backdrop"></div><div class="aegis-modal__container"><header><h2 id="modal-title"><slot name="header"></slot></h2><aegis-button variant="ghost" size="sm" aria-label="Fechar" on-click="close">×</aegis-button></header><div class="aegis-modal__body"><slot name="body"></slot></div><footer><slot name="footer"></slot></footer></div></div>`
* **Props/Atributos:**

| Atributo | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `open` | `boolean` | `false` | Controla visibilidade (reflete no `style.display`) |
| `size` | `sm \| md \| lg \| xl \| full` | `md` | `max-width`: 400px, 600px, 800px, 1000px, `calc(100% - 2rem)` |
| `variant` | `default \| confirmation \| form \| alert` | `default` | Ajusta padding, ícone, ações padrão |
| `dismissible` | `boolean` | `true` | `Esc`/backdrop click fecha |
| `close-on-overlay-click` | `boolean` | `true` | Click no backdrop fecha |
| `title` | `string` | — | Título (acessível via `aria-labelledby`) |
| `description` | `string` | — | Descrição (via `aria-describedby`) |

* **Estados visuais:**

| Estado | Comportamento |
| :--- | :--- |
| Entrada | `opacity: 0 → 1` + `transform: scale(0.95) → 1` (`--duration-base`, `--shadow-lg`) |
| Saída | `opacity: 1 → 0` + `transform: 1 → 0.95` (`--duration-fast`) |
| Backdrop | `background: rgba(0,0,0,0.4);` (`--color-bg-primary` com alpha) |
| Focus trap | Tab cicla dentro do modal; `focus` inicial no primeiro elemento focável ou botão fechar |

* **Tokens consumidos:** `--color-bg-primary`, `--color-bg-secondary`, `--color-border-light`, `--radius-lg`, `--shadow-lg`, `--shadow-modal-backdrop`, `--space-4`, `--space-6`, `--text-lg`, `--text-base`, `--duration-base`, `--duration-fast`, `--z-modal-backdrop`, `--z-modal`
* **Métodos JS:** `show()`, `hide()`, `toggle()`
* **Eventos emitidos:** `aegis-modal-open`, `aegis-modal-close`, `aegis-modal-confirm` (para variant=confirmation), `aegis-modal-submit` (para variant=form)
* **Exemplo de uso:**
```html
<aegis-modal id="modal-excluir" variant="confirmation" size="sm" title="Excluir Ativo" description="Esta ação não pode ser desfeita.">
  <span slot="header">Confirmar Exclusão</span>
  <p slot="body">Tem certeza que deseja excluir o ativo <strong>"Notebook Dell Latitude 7420"</strong>? Todos os dados de manutenção e health checks associados serão removidos permanentemente.</p>
  <div slot="footer" style="display:flex; gap:var(--space-3); justify-content:flex-end;">
    <aegis-button variant="secondary" on-click="closeModal">Cancelar</aegis-button>
    <aegis-button variant="destructive" on-click="confirmDelete">Excluir</aegis-button>
  </div>
</aegis-modal>

<script>
  const modal = document.getElementById('modal-excluir');
  modal.addEventListener('aegis-modal-confirm', () => { /* chamar API delete */ });
</script>
```

---

### 4.4 `aegis-dropdown` (usa `@popperjs/core`)

* **Propósito:** Menu de ações contextual (linha de tabela, header do usuário, filtros) — posicionamento robusto via Popper.js.
* **Anatomia:** `<aegis-dropdown><button slot="trigger" class="aegis-dropdown__trigger"><slot name="trigger"></slot></button><div class="aegis-dropdown__panel" role="menu"><slot name="content"></slot></div></aegis-dropdown>`
* **Props/Atributos:**

| Atributo | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `trigger` | `click \| hover` | `click` | Como abre |
| `placement` | `top \| bottom \| left \| right \| auto` | `bottom-start` | Posição via Popper |
| `offset` | `number` | `8` | Distância do trigger (px) |
| `flip` | `boolean` | `true` | Inverte se não cabe |
| `close-on-select` | `boolean` | `true` | Fecha ao clicar item |
| `disabled` | `boolean` | `false` | Desabilita trigger |

* **Slots:** `trigger` (botão/elemento que abre), `content` (`<aegis-dropdown-item>`, `<aegis-dropdown-divider>`, `<aegis-dropdown-header>`)
* **Tokens consumidos:** `--color-bg-primary`, `--color-border-light`, `--color-text-primary`, `--color-text-secondary`, `--radius-md`, `--shadow-md`, `--space-1`, `--space-2`, `--text-sm`, `--z-dropdown`, `--duration-fast`
* **Eventos:** `aegis-dropdown-open`, `aegis-dropdown-close`, `aegis-dropdown-select` (`{ value, item }`)
* **Exemplo de uso:**
```html
<aegis-dropdown placement="bottom-end" trigger="click">
  <aegis-button variant="ghost" size="sm" slot="trigger" aria-label="Ações do ativo">
    <svg class="aegis-icon"><use href="#icon-more-vertical"></use></svg>
  </aegis-button>
  <div slot="content" role="menu">
    <aegis-dropdown-item value="edit" role="menuitem">
      <svg class="aegis-icon" slot="icon"><use href="#icon-edit"></use></svg>
      Editar
    </aegis-dropdown-item>
    <aegis-dropdown-item value="maintenance" role="menuitem">
      <svg class="aegis-icon" slot="icon"><use href="#icon-wrench"></use></svg>
      Nova Manutenção
    </aegis-dropdown-item>
    <aegis-dropdown-divider></aegis-dropdown-divider>
    <aegis-dropdown-item value="delete" variant="destructive" role="menuitem">
      <svg class="aegis-icon" slot="icon"><use href="#icon-trash"></use></svg>
      Excluir
    </aegis-dropdown-item>
  </div>
</aegis-dropdown>
```

---

### 4.5 `aegis-toast` + `aegis-toast-container`

* **Propósito:** Feedback não-bloqueante para ações assíncronas (sucesso, erro, aviso) — usado em **todos os flows**.
* **Anatomia:** Container fixo (`position: fixed; z-index: var(--z-toast);`) + toasts individuais com `role="status"` (success/info) ou `role="alert"` (warning/danger).
* **API JS (imperativa, não declarativa):**
```js
import { toast } from './components/aegis-toast/aegis-toast.js';

toast.success('Ativo salvo com sucesso!', { duration: 5000, action: { label: 'Desfazer', onClick: () => undo() } });
toast.error('Falha ao conectar ao servidor', { persistent: true });
toast.warning('Disco acima de 85%', { duration: 10000 });
toast.info('Health check agendado para 02:00');
```
* **Tokens consumidos:** `--color-success`, `--color-success-light`, `--color-danger`, `--color-danger-light`, `--color-warning`, `--color-warning-light`, `--color-info`, `--color-info-light`, `--color-text-primary`, `--radius-md`, `--shadow-lg`, `--space-3`, `--space-4`, `--text-sm`, `--duration-base`, `--z-toast`
* **Estados:** Entrada (slide-in right + fade), saída (slide-out right + fade), progress bar (duração), hover (pausa timer)

---

### 4.6 `aegis-health-indicator` (Componente de Domínio)

* **Propósito:** Exibir status de saúde de hardware (disco, memória, rede) com threshold visual — core do Flow 3.
* **Anatomia:** `<div class="aegis-health"><div class="aegis-health__icon" aria-hidden="true"></div><div class="aegis-health__info"><span class="aegis-health__metric"></span><span class="aegis-health__value"></span></div><aegis-badge variant="{{statusVariant}}">{{statusLabel}}</aegis-badge></div>`
* **Props/Atributos:**

| Atributo | Tipo | Descrição |
| :--- | :--- | :--- |
| `metric` | `disk \| memory \| network \| cpu \| custom` | Tipo de métrica (define ícone padrão) |
| `status` | `ok \| warning \| critical \| unknown` | Status calculado |
| `value` | `number` | Valor atual (ex: 87) |
| `threshold` | `number` | Limite para warning (ex: 85) |
| `critical-threshold` | `number` | Limite para critical (ex: 95) |
| `unit` | `string` | Unidade: `%`, `GB`, `ms`, `Mbps` |
| `trend` | `up \| down \| stable` | Tendência (sparkline opcional) |
| `label` | `string` | Label customizado (ex: "Disco C:") |

* **Lógica de status (se não passado explicitamente):**
  - `value >= criticalThreshold` → `critical`
  - `value >= threshold` → `warning`
  - `value < threshold` → `ok`
* **Tokens consumidos:** `--health-ok`, `--health-warn`, `--health-critical`, `--color-text-primary`, `--color-text-secondary`, `--radius-md`, `--space-2`, `--space-3`, `--text-sm`, `--text-base`, `--duration-fast`
* **Exemplo de uso:**
```html
<aegis-health-indicator 
  metric="disk" 
  label="Disco C:" 
  value="87" 
  threshold="85" 
  critical-threshold="95" 
  unit="%"
  trend="up"
></aegis-health-indicator>
<!-- Renderiza: ícone disco + "Disco C:" + "87%" + badge "warning" (amarelo) + seta para cima sutil -->
```

---

## 5. Componentes em Depreciação

| Componente | Motivo | Substituído por | Prazo de Remoção |
| :--- | :--- | :--- | :--- |
| *Nenhum* | Não há componentes legados no codebase atual | — | — |

---

## 6. Gaps Conhecidos

| Componente Necessário | Flow Relacionado | Blocker / Decisão Pendente |
| :--- | :--- | :--- |
| `aegis-chart` / `aegis-sparkline` | 3, 5 | Gráficos de tendência de health check / relatórios. **Decisão:** biblioteca leve (uPlot, Chart.js, ApexCharts) vs. SVG nativo. Requer avaliação de bundle size. |
| `aegis-file-upload` | 1, 2 | Upload de anexos (notas fiscais, fotos de ativo, relatórios). Precisa de backend multipart + validação tipo/tamanho. |
| `aegis-user-picker` | 1, 2, 4 | Busca/seleção de usuários/funcionários com autocomplete (departamento, filial). Requer endpoint de busca paginada. |
| `aegis-asset-picker` | 2 | Seleção de ativo ao criar manutenção (modal com tabela + busca). Reutiliza `aegis-data-table` + `aegis-modal`. |
| `aegis-permission-matrix` | 4 | UI para RBAC (roles × permissions × recursos). Complexo — avaliar se MVP usa apenas select múltiplo simples. |
| `aegis-skeleton-*` | Todos | Skeletons específicos por componente (table row, card, stat card, form field). Pode ser gerado automaticamente via CSS. |
| `aegis-wizard` / `aegis-stepper` | 1, 2 | Fluxos multi-etapa (cadastro ativo em etapas, aprovação manutenção). Não identificado nos flows atuais — manter como gap futuro. |
| `aegis-color-picker` | 1 | Para personalização de tags/categorias de ativo. Não no MVP. |
| `aegis-rich-text-editor` | 2, 5 | Descrição rica de manutenção, observações de auditoria. **Decisão:** TipTap/ProseMirror (pesado) vs. textarea + markdown simples. |

---

## 7. Referências

* **Design Tokens:** [design-tokens.md](design-tokens.md) — fonte única de verdade para cores, espaçamento, tipografia, sombras, radius, motion, z-index.
* **User Flows:** [user-flows.md](user-flows.md) — 6 fluxos detalhados com estados de UI, métricas e edge cases que ditam a necessidade de cada componente.
* **Diagnóstico Determinístico:** 348 arquivos Java (backend), 15 arquivos JS (frontend/services), **zero componentes UI existentes**.
* **Stack Tecnológica Verificada:** JavaScript vanilla (ESM), `@popperjs/core` ^2.11.8 (única dependência UI), sem build tool configurado (Vite/Webpack necessário para tokens + componentes).
* **Acessibilidade:** WCAG 2.1 AA — todos os componentes devem passar axe-core + teste manual NVDA/VoiceOver.
* **Próximos Passos Imediatos:**
  1. Configurar `package.json` com `type: "module"`, scripts `dev`, `build`, `test` (Vite + Vitest)
  2. Criar `frontend/tokens.json` + build pipeline (Style Dictionary ou script custom) → CSS custom properties + JS tokens
  3. Implementar `aegis-button`, `aegis-input`, `aegis-modal`, `aegis-toast` (core) + `aegis-data-table` (crítico para Flows 1,2,3,5)
  4. Configurar Storybook (opcional) ou página de catálogo simples (`/components.html`)
  5. Adicionar ESLint + Stylelint + regra custom `no-hardcoded-tokens` no CI

---

## 8. Declaração de Limitações

> **Este inventário NÃO reflete componentes existentes.** O codebase atual **não possui nenhum componente de UI reutilizável** — apenas 15 arquivos JS de serviços (`api.js`, etc.).  
>   
> A lista de 29 componentes é **100% inferida** a partir dos user flows (que por sua vez são inferidos do BRD + diagnóstico) e dos design tokens (também inferidos).  
>   
> **Nenhuma decisão de arquitetura de frontend foi tomada** (Web Components vs. módulos JS vs. framework leve). A convenção "Web Components" na Seção 2 é uma **proposta**, não uma realidade.  
>   
> **Validação humana obrigatória** antes de qualquer implementação: priorização, escopo do MVP, escolha de tooling, adoção ou não de Shadow DOM, estratégia de SSR/SSG (se houver).