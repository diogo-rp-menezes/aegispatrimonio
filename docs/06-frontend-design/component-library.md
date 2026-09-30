# Component Library / Inventory — Aegis Patrimonio

> **Versão:** 1.0 · **Owner:** Frontend Lead · **Status:** Draft  
> **Baseado em:** Componentes vanilla JavaScript (ES6+) + `@popperjs/core` para posicionamento  
> **Depende de:** `design-tokens.md` (tokens de cor, espaçamento, tipografia, motion, z-index), `user-flows.md` (fluxos que definem componentes necessários)

---

## 1. Overview

Este inventário documenta os **componentes de interface reutilizáveis** para o frontend do Aegis Patrimonio — uma aplicação **vanilla JavaScript (ES6 modules)** sem framework de componentes (React, Vue, Svelte) ou biblioteca de UI (shadcn/ui, Material, Bootstrap). O único dependência de UI é **`@popperjs/core`** para posicionamento de tooltips, dropdowns e popovers.

Os componentes são implementados como **funções factory / classes ES6** que criam e gerenciam elementos DOM, consomem **CSS Custom Properties** definidas em `design-tokens.md` e seguem o padrão de **Web Components leves** (sem Shadow DOM obrigatório) para compatibilidade com a stack atual.

> **Estado atual (diagnóstico determinístico):** O workspace contém **15 arquivos `.js` em `frontend/src/`** — majoritariamente services (`api.js`, `auth.js`) e utilitários. **Nenhum arquivo de componente UI foi detectado** na varredura AST. Este inventário define a **especificação a ser implementada** com base nos fluxos de usuário (`user-flows.md`) e tokens de design (`design-tokens.md`).

---

## 2. Convenções Gerais

| Convenção | Definição |
| :--- | :--- |
| **Padrão de composição** | Funções factory `createComponentName(config)` que retornam objeto com `{ element, destroy, update, show, hide }` + event emitters via `CustomEvent` |
| **Nomenclatura** | `create[PascalCase]` para factories (ex: `createButton`, `createDataTable`); arquivos em `kebab-case` (`button.js`, `data-table.js`) |
| **Local no repositório** | `frontend/src/components/` (a criar) — um arquivo por componente + `index.js` para barrel export |
| **Estilização** | **Apenas CSS Custom Properties** (tokens) — zero CSS-in-JS, zero utility classes. Classes CSS BEM-style opcionais para estados (`is-loading`, `is-disabled`, `has-error`) |
| **Acessibilidade** | Obrigatório: `role`, `aria-*`, `tabindex`, focus management, `aria-live` para toasts/alertas |
| **Tokens consumidos** | Referência direta a `design-tokens.md` — ex: `var(--color-accent-primary)`, `var(--space-4)`, `var(--duration-base)` |
| **Popper.js** | Usado **apenas** para: `Tooltip`, `Dropdown`, `Popover`, `Select` (positioning strategy `flip`, `shift`, `offset`) |

---

## 3. Inventário de Componentes

| Componente | Categoria | Variantes / Configurações | Status | Localização (planejada) |
| :--- | :--- | :--- | :--- | :--- |
| **Button** | Ação | `variant: 'primary' \| 'secondary' \| 'danger' \| 'ghost'`, `size: 'sm' \| 'md' \| 'lg'`, `loading`, `disabled`, `iconOnly` | **Planejado** | `frontend/src/components/button.js` |
| **IconButton** | Ação | `variant`, `size`, `aria-label` obrigatório | **Planejado** | `frontend/src/components/icon-button.js` |
| **Input** | Formulário | `type: 'text' \| 'email' \| 'password' \| 'number' \| 'date'`, `label`, `error`, `disabled`, `required`, `helperText` | **Planejado** | `frontend/src/components/input.js` |
| **Select** | Formulário | `options[]`, `placeholder`, `searchable`, `multiple`, `error`, `disabled` (usa Popper para dropdown) | **Planejado** | `frontend/src/components/select.js` |
| **Textarea** | Formulário | `label`, `error`, `disabled`, `rows`, `maxLength` | **Planejado** | `frontend/src/components/textarea.js` |
| **Checkbox** | Formulário | `label`, `indeterminate`, `disabled` | **Planejado** | `frontend/src/components/checkbox.js` |
| **RadioGroup** | Formulário | `options[]`, `orientation: 'horizontal' \| 'vertical'`, `disabled` | **Planejado** | `frontend/src/components/radio-group.js` |
| **Switch** | Formulário | `label`, `checked`, `disabled` | **Planejado** | `frontend/src/components/switch.js` |
| **DataTable** | Exibição | `columns[]`, `data[]`, `sortable`, `filterable`, `selectable`, `pagination`, `rowActions[]`, `emptyState`, `loading`, `density: 'compact' \| 'comfortable'` | **Planejado** | `frontend/src/components/data-table.js` |
| **Modal** | Overlay | `size: 'sm' \| 'md' \| 'lg' \| 'xl'`, `title`, `closable`, `footerActions[]`, `trapFocus` | **Planejado** | `frontend/src/components/modal.js` |
| **Drawer** | Overlay | `position: 'left' \| 'right'`, `size`, `closable` | **Planejado** | `frontend/src/components/drawer.js` |
| **Toast** | Feedback | `variant: 'success' \| 'warning' \| 'danger' \| 'info'`, `title`, `message`, `duration`, `action`, `persistent` | **Planejado** | `frontend/src/components/toast.js` |
| **Alert** | Feedback | `variant`, `title`, `message`, `dismissible`, `icon` | **Planejado** | `frontend/src/components/alert.js` |
| **Badge** | Status | `variant: 'ativo' \| 'manutencao' \| 'baixa' \| 'critico' \| 'info' \| 'warning' \| 'success' \| 'neutral'`, `dot`, `removable` | **Planejado** | `frontend/src/components/badge.js` |
| **Tooltip** | Feedback | `content`, `placement`, `delay`, `interactive` (usa Popper) | **Planejado** | `frontend/src/components/tooltip.js` |
| **Dropdown** | Navegação | `trigger`, `items[]`, `align`, `divider` (usa Popper) | **Planejado** | `frontend/src/components/dropdown.js` |
| **Tabs** | Navegação | `tabs[]`, `variant: 'line' \| 'enclosed'`, `orientation` | **Planejado** | `frontend/src/components/tabs.js` |
| **Breadcrumb** | Navegação | `items[]`, `separator`, `currentPage` | **Planejado** | `frontend/src/components/breadcrumb.js` |
| **Pagination** | Navegação | `total`, `page`, `pageSize`, `onChange`, `showSizeChanger` | **Planejado** | `frontend/src/components/pagination.js` |
| **Avatar** | Exibição | `src`, `alt`, `fallback` (iniciais), `size: 'xs' \| 'sm' \| 'md' \| 'lg'`, `status` | **Planejado** | `frontend/src/components/avatar.js` |
| **Card** | Layout | `header`, `footer`, `hoverable`, `bordered`, `elevation` | **Planejado** | `frontend/src/components/card.js` |
| **Sidebar** | Layout | `collapsible`, `items[]`, `activeItem`, `logo`, `userMenu` | **Planejado** | `frontend/src/components/sidebar.js` |
| **Header** | Layout | `title`, `actions[]`, `userMenu`, `notificationBell`, `themeToggle` | **Planejado** | `frontend/src/components/header.js` |
| **FormLayout** | Layout | `fields[]`, `colCount: 1 \| 2 \| 3`, `labelPosition: 'top' \| 'left'` | **Planejado** | `frontend/src/components/form-layout.js` |
| **EmptyState** | Feedback | `icon`, `title`, `description`, `action` | **Planejado** | `frontend/src/components/empty-state.js` |
| **Skeleton** | Feedback | `variant: 'text' \| 'card' \| 'table' \| 'avatar'`, `animation: 'pulse' \| 'wave'` | **Planejado** | `frontend/src/components/skeleton.js` |
| **FileUpload** | Formulário | `accept`, `maxSize`, `multiple`, `dragDrop`, `preview` | **Planejado** | `frontend/src/components/file-upload.js` |
| **DatePicker** | Formulário | `range`, `format`, `disabledDates`, `minDate`, `maxDate` (usa Popper) | **Planejado** | `frontend/src/components/date-picker.js` |

> **Total:** 27 componentes planejados. **0 implementados** (diagnóstico não detectou componentes UI no codebase atual).

---

## 4. Especificação por Componente

> **Convenção de documentação:** Cada seção abaixo segue o template do item 4 do template. Tokens referenciam `design-tokens.md` via `var(--token-name)`. Exemplos de uso mostram **vanilla JS** (sem JSX/TSX).

---

### 4.1 Button

* **Propósito:** Ação principal/secundária/destrutiva em formulários, tabelas, modais, toolbars. Usado em **todos os fluxos** (Flow 2–7).
* **Anatomia:** `<button>` nativo + `span` para label + `slot` opcional para ícone (leading/trailing).

#### Props/API

| Prop | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `variant` | `'primary' \| 'secondary' \| 'danger' \| 'ghost'` | `'primary'` | Estilo visual — mapeia tokens de cor/borda |
| `size` | `'sm' \| 'md' \| 'lg'` | `'md'` | Altura/padding — `--btn-height-*`, `--btn-padding-x-*` |
| `disabled` | `boolean` | `false` | Desabilita interação + estilo `--color-text-muted` |
| `loading` | `boolean` | `false` | Exibe spinner (CSS animation) + desabilita |
| `icon` | `HTMLElement \| string` | `undefined` | Ícone leading (antes do label) |
| `iconTrailing` | `HTMLElement \| string` | `undefined` | Ícone trailing (após label) |
| `fullWidth` | `boolean` | `false` | `width: 100%` |
| `type` | `'button' \| 'submit' \| 'reset'` | `'button'` | Atributo nativo |
| `onClick` | `(event: MouseEvent) => void` | `undefined` | Handler de clique |

#### Estados visuais

| Estado | Comportamento Visual |
| :--- | :--- |
| **Default** | `background: var(--btn-primary-bg)`, `color: var(--btn-primary-text)`, `border: none`, `border-radius: var(--btn-radius)` |
| **Hover** | `background: var(--btn-primary-hover)` (primary) / `background: var(--btn-secondary-hover)` (secondary) |
| **Focus** | `box-shadow: var(--shadow-focus)` (outline acessível) |
| **Active/Pressed** | `transform: scale(0.98)`, `transition: var(--duration-instant)` |
| **Disabled** | `opacity: 0.5`, `cursor: not-allowed`, `pointer-events: none` |
| **Loading** | `position: relative`, label oculto, spinner centralizado (`animation: spin var(--duration-base) linear infinite`) |
| **Ghost variant** | `background: transparent`, `color: var(--color-accent-primary)`, `border: 1px solid transparent` → hover: `background: var(--color-accent-light)` |

#### Tokens consumidos

`--btn-height-*`, `--btn-padding-x-*`, `--btn-font-size`, `--btn-font-weight`, `--btn-radius`, `--btn-primary-bg`, `--btn-primary-hover`, `--btn-primary-text`, `--btn-secondary-bg`, `--btn-secondary-hover`, `--btn-secondary-text`, `--btn-danger-bg`, `--btn-danger-hover`, `--color-text-muted`, `--shadow-focus`, `--duration-instant`, `--duration-base`, `--radius-md`

#### Composição/Slots

```js
// Uso: createButton({ variant: 'primary', label: 'Salvar', onClick: handleSave })
// Com ícone: createButton({ variant: 'secondary', label: 'Exportar', icon: downloadIconSvg })
```

#### Exemplo de uso

```js
import { createButton } from './components/button.js';

const btnSalvar = createButton({
  variant: 'primary',
  size: 'md',
  label: 'Salvar ativo',
  icon: '<svg class="icon" aria-hidden="true">...</svg>',
  onClick: async (e) => {
    e.target.disabled = true; // loading gerenciado externamente
    await salvarAtivo(formData);
    btnSalvar.update({ loading: false });
  }
});

// Inserir no DOM
document.querySelector('#form-actions').appendChild(btnSalvar.element);
```

---

### 4.2 IconButton

* **Propósito:** Ações apenas com ícone (toolbar de tabela, header, ações de linha). **Obrigatório `aria-label`** para acessibilidade.
* **Anatomia:** `<button>` + `span.icon` + `span.sr-only` (label para screen readers).

#### Props/API

| Prop | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `variant` | `'primary' \| 'secondary' \| 'danger' \| 'ghost'` | `'ghost'` | Estilo — ghost padrão para toolbar |
| `size` | `'sm' \| 'md' \| 'lg'` | `'md'` | `--btn-height-*` |
| `ariaLabel` | `string` | **obrigatório** | Label acessível (ex: "Editar ativo", "Excluir manutenção") |
| `icon` | `HTMLElement \| string` | **obrigatório** | SVG ou elemento de ícone |
| `disabled` | `boolean` | `false` | — |
| `loading` | `boolean` | `false` | Spinner substitui ícone |
| `onClick` | `(event) => void` | `undefined` | — |

#### Estados visuais

| Estado | Comportamento Visual |
| :--- | :--- |
| **Default (ghost)** | `background: transparent`, `color: var(--color-text-secondary)`, `border: none` |
| **Hover** | `background: var(--color-bg-tertiary)`, `color: var(--color-text-primary)` |
| **Focus** | `box-shadow: var(--shadow-focus)` |
| **Disabled** | `opacity: 0.4`, `cursor: not-allowed` |

#### Tokens consumidos

`--btn-height-*`, `--btn-padding-x-*` (padding igual em X/Y para quadrado), `--color-text-secondary`, `--color-text-primary`, `--color-bg-tertiary`, `--shadow-focus`, `--duration-fast`

#### Exemplo de uso

```js
import { createIconButton } from './components/icon-button.js';

const btnEditar = createIconButton({
  variant: 'ghost',
  size: 'sm',
  ariaLabel: 'Editar ativo',
  icon: editIconSvg,
  onClick: () => abrirModalEdicao(ativoId)
});

const btnExcluir = createIconButton({
  variant: 'danger',
  size: 'sm',
  ariaLabel: 'Excluir ativo',
  icon: trashIconSvg,
  onClick: () => confirmarExclusao(ativoId)
});

// Em DataTable rowActions:
rowActions: [
  { component: btnEditar.element },
  { component: btnExcluir.element }
]
```

---

### 4.3 Input

* **Propósito:** Campo de texto único — usado em **todos os formulários** (Flow 2, 3, 4, 7).
* **Anatomia:** `<label>` + `<input>` + `<span.helper-text>` + `<span.error-text>` (condicional).

#### Props/API

| Prop | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `type` | `'text' \| 'email' \| 'password' \| 'number' \| 'date' \| 'tel' \| 'url'` | `'text'` | Tipo nativo |
| `label` | `string` | `undefined` | Label associado via `for/id` |
| `placeholder` | `string` | `''` | Placeholder nativo |
| `value` | `string` | `''` | Valor controlado |
| `error` | `string \| null` | `null` | Mensagem de erro — adiciona `aria-invalid="true"` |
| `helperText` | `string` | `undefined` | Texto auxiliar abaixo do input |
| `disabled` | `boolean` | `false` | — |
| `required` | `boolean` | `false` | Adiciona `aria-required="true"` + asterisco no label |
| `maxLength` | `number` | `undefined` | Atributo nativo + contador opcional |
| `autocomplete` | `string` | `undefined` | Atributo nativo (ex: `organization`, `email`) |
| `onChange` | `(value: string, event: Event) => void` | `undefined` | Handler de mudança |
| `onBlur` | `(event: FocusEvent) => void` | `undefined` | Handler de blur (validação on blur) |

#### Estados visuais

| Estado | Comportamento Visual |
| :--- | :--- |
| **Default** | `border: 1px solid var(--input-border)`, `background: var(--input-bg)`, `height: var(--input-height)`, `padding: 0 var(--input-padding-x)`, `border-radius: var(--radius-sm)` |
| **Hover** | `border-color: var(--input-border-hover)` |
| **Focus** | `border-color: var(--input-border-focus)`, `box-shadow: var(--shadow-focus)`, `outline: none` |
| **Disabled** | `background: var(--color-bg-tertiary)`, `color: var(--color-text-muted)`, `cursor: not-allowed` |
| **Error** | `border-color: var(--error-border-color)`, focus: `box-shadow: 0 0 0 3px rgb(220 38 38 / 0.4)` |
| **Label** | `font-size: var(--label-font-size)`, `font-weight: var(--label-font-weight)`, `color: var(--color-text-primary)`, `margin-bottom: var(--label-gap)` |
| **Helper text** | `font-size: var(--text-xs)`, `color: var(--color-text-muted)` |
| **Error text** | `font-size: var(--text-xs)`, `color: var(--error-text-color)`, `display: flex`, `align-items: center`, `gap: var(--space-1)` |

#### Tokens consumidos

`--input-height`, `--input-padding-x`, `--input-bg`, `--input-border`, `--input-border-hover`, `--input-border-focus`, `--input-placeholder-color`, `--label-font-size`, `--label-font-weight`, `--label-gap`, `--error-text-color`, `--error-border-color`, `--radius-sm`, `--space-1`, `--space-2`, `--space-3`, `--text-xs`, `--color-text-primary`, `--color-text-muted`, `--color-bg-tertiary`, `--shadow-focus`, `--duration-base`

#### Exemplo de uso

```js
import { createInput } from './components/input.js';

const inputTag = createInput({
  type: 'text',
  label: 'Tag do Ativo *',
  placeholder: 'Ex: AT-2024-001',
  required: true,
  maxLength: 50,
  helperText: 'Código único de identificação do ativo',
  onChange: (value) => formData.tag = value,
  onBlur: (e) => validarTagUnica(e.target.value)
});

// Em FormLayout:
formLayout.addField(inputTag);
```

---

### 4.4 Select

* **Propósito:** Seleção única/múltipla com busca — usado para **FKs** (filial, departamento, tipo ativo, fornecedor, localização) nos formulários de ativos/manutenções (Flow 3, 4).
* **Anatomia:** `Input` (trigger) + `Popper` (dropdown) + `ul[role="listbox"]` + `li[role="option"]`.

#### Props/API

| Prop | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `options` | `Array<{ value: string, label: string, disabled?: boolean }>` | `[]` | Opções — pode ser carregado assincronamente |
| `placeholder` | `string` | `'Selecione...'` | Texto quando vazio |
| `value` | `string \| string[]` | `''` | Valor(es) selecionado(s) |
| `multiple` | `boolean` | `false` | Modo múltiplo (chips + checkbox no dropdown) |
| `searchable` | `boolean` | `true` | Input de busca no dropdown |
| `error` | `string \| null` | `null` | — |
| `disabled` | `boolean` | `false` | — |
| `label` | `string` | `undefined` | Label do campo |
| `helperText` | `string` | `undefined` | — |
| `onChange` | `(value: string \| string[]) => void` | `undefined` | — |
| `loadOptions` | `(query: string) => Promise<Option[]>` | `undefined` | Lazy loading para listas grandes (>50 itens) |

#### Estados visuais

| Estado | Comportamento Visual |
| :--- | :--- |
| **Trigger (default)** | Mesmo estilo de `Input` + `background-image: url("data:image/svg+xml,<chevron-down>")` |
| **Open** | `border-color: var(--input-border-focus)`, `box-shadow: var(--shadow-focus)`, chevron rotacionado 180° |
| **Dropdown (Popper)** | `background: var(--color-panel)`, `border: 1px solid var(--color-border-light)`, `border-radius: var(--radius-md)`, `box-shadow: var(--shadow-md)`, `max-height: 280px`, `overflow-y: auto` |
| **Option hover/selected** | `background: var(--color-accent-light)`, `color: var(--color-accent-primary)` |
| **Option disabled** | `color: var(--color-text-muted)`, `cursor: not-allowed` |
| **Search input** | `padding: var(--space-2) var(--space-3)`, `border-bottom: 1px solid var(--color-border-light)` |
| **Multi-select chips** | `display: inline-flex`, `gap: var(--space-1)`, `background: var(--color-accent-light)`, `border-radius: var(--radius-full)`, `padding: 2px var(--space-2)`, botão remover com `aria-label="Remover {label}"` |

#### Tokens consumidos

Todos de `Input` + `--color-panel`, `--color-border-light`, `--shadow-md`, `--color-accent-light`, `--color-accent-primary`, `--color-text-muted`, `--radius-md`, `--radius-full`, `--space-1`, `--space-2`, `--space-3`, `--duration-base` (animação dropdown)

#### Exemplo de uso

```js
import { createSelect } from './components/select.js';

const selectFilial = createSelect({
  label: 'Filial *',
  placeholder: 'Selecione a filial',
  required: true,
  options: [], // carregado via API
  loadOptions: async (query) => {
    const response = await api.get('/filiais', { params: { q: query, limit: 50 } });
    return response.data.map(f => ({ value: f.id, label: f.nome }));
  },
  onChange: (value) => formData.filialId = value
});
```

---

### 4.5 DataTable

* **Propósito:** Tabela de dados densa, paginada, ordenável, filtrável — **componente central** do Aegis Patrimonio (Flow 3: lista de ativos, Flow 4: lista de manutenções, Flow 5: lista de alertas, Flow 6: relatórios).
* **Anatomia:** `table` + `thead` (sticky) + `tbody` + `tfoot` (pagination) + `caption` (acessibilidade) + toolbar opcional (filtros, busca, ações em lote).

#### Props/API

| Prop | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `columns` | `Column[]` | **obrigatório** | Definição de colunas (ver abaixo) |
| `data` | `any[]` | `[]` | Dados da página atual |
| `total` | `number` | `0` | Total de registros (para paginação server-side) |
| `page` | `number` | `1` | Página atual |
| `pageSize` | `number` | `20` | Registros por página |
| `sortable` | `boolean` | `true` | Habilita ordenação por clique no header |
| `filterable` | `boolean` | `false` | Habilita filtros por coluna (header com input/select) |
| `selectable` | `boolean` | `false` | Checkbox na primeira coluna + header "select all" |
| `rowKey` | `string` | `'id'` | Campo único para `row-id` e seleção |
| `rowActions` | `RowAction[]` | `[]` | Ações por linha (IconButton array) |
| `emptyState` | `EmptyStateConfig` | `undefined` | Configuração de estado vazio |
| `loading` | `boolean` | `false` | Exibe skeleton rows |
| `density` | `'compact' \| 'comfortable'` | `'compact'` | Padding das células |
| `stickyHeader` | `boolean` | `true` | Header fixo no scroll |
| `onSort` | `(column: string, direction: 'asc' \| 'desc') => void` | `undefined` | — |
| `onFilter` | `(filters: Record<string, any>) => void` | `undefined` | — |
| `onPageChange` | `(page: number, pageSize: number) => void` | `undefined` | — |
| `onSelectionChange` | `(selectedIds: string[]) => void` | `undefined` | — |
| `onRowClick` | `(rowData: any, event: MouseEvent) => void` | `undefined` | Navega para detalhe (Flow 3) |

#### Column Definition

```ts
interface Column {
  key: string;           // campo no objeto data
  label: string;         // header
  width?: string;        // ex: '120px', '15%'
  minWidth?: string;     // para colunas flexíveis
  align?: 'left' | 'center' | 'right';
  sortable?: boolean;    // override global
  filterable?: boolean;  // override global
  render?: (value: any, row: any, index: number) => HTMLElement | string; // custom renderer
  className?: string;    // classe CSS na célula
}
```

#### RowAction Definition

```ts
interface RowAction {
  component: HTMLElement; // IconButton.element
  show?: (row: any) => boolean; // condicional por role/estado
}
```

#### Estados visuais

| Estado | Comportamento Visual |
| :--- | :--- |
| **Table** | `width: 100%`, `border-collapse: collapse`, `font-size: var(--table-font-size)` |
| **Header** | `background: var(--table-header-bg)`, `color: var(--table-header-text)`, `font-weight: 600`, `position: sticky`, `top: 0`, `z-index: var(--z-sticky)` |
| **Cell** | `padding: var(--table-cell-padding-y) var(--table-cell-padding-x)`, `border-bottom: 1px solid var(--table-border-color)`, `vertical-align: middle` |
| **Row hover** | `background: var(--table-row-hover-bg)` |
| **Row selected** | `background: var(--table-row-selected-bg)` |
| **Sortable header** | `cursor: pointer`, `user-select: none`, ícone seta (asc/desc/none) |
| **Loading** | `tbody` exibe `Skeleton` rows (3-5 linhas) |
| **Empty** | `td[colspan="N"]` com `EmptyState` centralizado |
| **Pagination** | `display: flex`, `justify-content: space-between`, `align-items: center`, `padding: var(--space-3)`, `border-top: 1px solid var(--table-border-color)` |

#### Tokens consumidos

`--table-header-bg`, `--table-header-text`, `--table-row-hover-bg`, `--table-row-selected-bg`, `--table-border-color`, `--table-cell-padding-y`, `--table-cell-padding-x`, `--table-font-size`, `--text-sm`, `--color-text-secondary`, `--color-bg-tertiary`, `--color-accent-light`, `--color-border-light`, `--z-sticky`, `--shadow-xs`, `--space-2`, `--space-3`, `--duration-fast`

#### Exemplo de uso (Flow 3 - Lista de Ativos)

```js
import { createDataTable } from './components/data-table.js';
import { createIconButton } from './components/icon-button.js';

const columns = [
  { key: 'tag', label: 'Tag', width: '120px', sortable: true },
  { key: 'nome', label: 'Nome', minWidth: '200px', sortable: true },
  { key: 'tipo', label: 'Tipo', width: '150px', sortable: true,
    render: (v) => `<span class="badge badge--info">${v}</span>` },
  { key: 'filial', label: 'Filial', width: '180px', sortable: true },
  { key: 'departamento', label: 'Departamento', width: '180px' },
  { key: 'status', label: 'Status', width: '130px', sortable: true,
    render: (v) => createBadge({ variant: v.toLowerCase() }).element.outerHTML },
  { key: 'actions', label: '', width: '100px', align: 'center',
    render: (_, row) => {
      const btnVer = createIconButton({ ariaLabel: `Ver detalhes de ${row.nome}`, icon: eyeIcon, onClick: () => router.navigate(`/ativos/${row.id}`) });
      const btnEditar = createIconButton({ ariaLabel: `Editar ${row.nome}`, icon: editIcon, onClick: () => abrirModalEdicao(row.id), show: (r) => isAdmin });
      const btnExcluir = createIconButton({ ariaLabel: `Excluir ${row.nome}`, icon: trashIcon, variant: 'danger', onClick: () => confirmarExclusao(row.id), show: (r) => isAdmin });
      return `<div class="action-group">${btnVer.element.outerHTML}${btnEditar.element.outerHTML}${btnExcluir.element.outerHTML}</div>`;
    }
  }
];

const table = createDataTable({
  columns,
  data: [],
  total: 0,
  page: 1,
  pageSize: 20,
  sortable: true,
  filterable: true,
  selectable: true,
  rowKey: 'id',
  density: 'compact',
  stickyHeader: true,
  emptyState: { title: 'Nenhum ativo encontrado', description: 'Cadastre o primeiro ativo ou ajuste os filtros.', action: { label: 'Novo Ativo', onClick: () => abrirModalCriacao() } },
  onSort: (col, dir) => carregarAtivos({ sort: col, order: dir }),
  onFilter: (filters) => carregarAtivos({ filters }),
  onPageChange: (page, size) => carregarAtivos({ page, pageSize: size }),
  onSelectionChange: (ids) => toolbarBulkActions.update({ visible: ids.length > 0, count: ids.length }),
  onRowClick: (row) => router.navigate(`/ativos/${row.id}`)
});

// Carregamento inicial
async function carregarAtivos(params) {
  table.update({ loading: true });
  const { data, total } = await api.get('/ativos', { params });
  table.update({ data, total, loading: false });
}
```

---

### 4.6 Modal

* **Propósito:** Diálogo modal para criação/edição de entidades (Flow 2, 3, 4), confirmações destrutivas, formulários complexos.
* **Anatomia:** `div[role="dialog"]` + `div.modal-backdrop` + `div.modal-container` + `header` + `main` + `footer`.

#### Props/API

| Prop | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `size` | `'sm' \| 'md' \| 'lg' \| 'xl'` | `'md'` | `max-width`: sm=400px, md=560px, lg=720px, xl=960px |
| `title` | `string` | `''` | Título no header (aria-labelledby) |
| `closable` | `boolean` | `true` | Botão fechar (X) + ESC + click backdrop |
| `footerActions` | `ButtonConfig[]` | `[]` | Botões no footer (ex: Cancelar, Salvar) |
| `content` | `HTMLElement \| string` | `''` | Conteúdo do body (formulário, texto, etc.) |
| `trapFocus` | `boolean` | `true` | Focus trap dentro do modal |
| `onClose` | `() => void` | `undefined` | Callback ao fechar |
| `onOpen` | `() => void` | `undefined` | Callback ao abrir |

#### Estados visuais

| Estado | Comportamento Visual |
| :--- | :--- |
| **Backdrop** | `position: fixed`, `inset: 0`, `background: rgba(0,0,0,0.5)`, `z-index: var(--z-modal-backdrop)`, `animation: fadeIn var(--duration-base)` |
| **Container** | `background: var(--color-panel)`, `border-radius: var(--radius-lg)`, `box-shadow: var(--shadow-lg)`, `z-index: var(--z-modal)`, `max-height: 90vh`, `display: flex`, `flex-direction: column`, `animation: slideUp var(--duration-base)` |
| **Header** | `padding: var(--space-4) var(--space-6)`, `border-bottom: 1px solid var(--color-border-light)`, `display: flex`, `justify-content: space-between`, `align-items: center` |
| **Body** | `padding: var(--space-6)`, `overflow-y: auto`, `flex: 1` |
| **Footer** | `padding: var(--space-4) var(--space-6)`, `border-top: 1px solid var(--color-border-light)`, `display: flex`, `justify-content: flex-end`, `gap: var(--space-3)` |
| **Close button** | `IconButton` ghost, `aria-label="Fechar"` |

#### Tokens consumidos

`--color-panel`, `--color-border-light`, `--radius-lg`, `--shadow-lg`, `--z-modal-backdrop`, `--z-modal`, `--space-3`, `--space-4`, `--space-6`, `--duration-base`, `--color-text-primary`, `--color-text-secondary`

#### Exemplo de uso (Flow 3 - Criação de Ativo)

```js
import { createModal } from './components/modal.js';
import { createButton } from './components/button.js';
import { createFormLayout } from './components/form-layout.js';

const modalCriarAtivo = createModal({
  size: 'lg',
  title: 'Novo Ativo',
  closable: true,
  content: formLayout.element, // FormLayout com todos os campos
  footerActions: [
    createButton({ variant: 'secondary', label: 'Cancelar', onClick: () => modalCriarAtivo.hide() }),
    createButton({ variant: 'primary', label: 'Criar Ativo', loading: false, onClick: async () => {
      const btn = footerActions[1];
      btn.update({ loading: true });
      try {
        await api.post('/ativos', formLayout.getValues());
        toast.success('Ativo criado com sucesso');
        modalCriarAtivo.hide();
        table.refresh();
      } catch (err) {
        formLayout.setErrors(err.response.data.errors);
      } finally {
        btn.update({ loading: false });
      }
    }})
  ],
  onClose: () => formLayout.reset()
});

// Abrir
document.querySelector('#btn-novo-ativo').addEventListener('click', () => {
  formLayout.reset();
  modalCriarAtivo.show();
});
```

---

### 4.7 Toast

* **Propósito:** Notificações globais não-bloqueantes (sucesso, erro, aviso, info) — usado em **todos os fluxos** para feedback de ações assíncronas.
* **Anatomia:** `div[role="alert" \| "status"]` + `icon` + `div.content` (title + message) + `button.close` (opcional) + `div.progress` (auto-dismiss).

#### Props/API

| Prop | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `variant` | `'success' \| 'warning' \| 'danger' \| 'info'` | `'info'` | Define cor/ícone |
| `title` | `string` | `''` | Título em negrito |
| `message` | `string` | `''` | Mensagem detalhada |
| `duration` | `number` | `5000` | ms para auto-dismiss (0 = persistente) |
| `persistent` | `boolean` | `false` | Não auto-dismiss (erros críticos) |
| `action` | `{ label: string, onClick: () => void }` | `undefined` | Botão de ação (ex: "Desfazer") |
| `onDismiss` | `() => void` | `undefined` | Callback ao fechar |

#### Estados visuais

| Estado | Comportamento Visual |
| :--- | :--- |
| **Container (toast stack)** | `position: fixed`, `bottom: var(--toast-offset)`, `right: var(--toast-offset)`, `z-index: var(--z-toast)`, `display: flex`, `flex-direction: column`, `gap: var(--space-2)` |
| **Toast** | `display: flex`, `gap: var(--alert-gap)`, `padding: var(--alert-padding)`, `border-radius: var(--alert-radius)`, `box-shadow: var(--shadow-lg)`, `min-width: 320px`, `max-width: 480px`, `animation: slideInRight var(--duration-base)` |
| **Success** | `background: var(--color-success-bg)`, `border-left: 4px solid var(--color-success)`, `color: var(--color-success)` |
| **Warning** | `background: var(--color-warning-bg)`, `border-left: 4px solid var(--color-warning)`, `color: var(--color-warning)` |
| **Danger** | `background: var(--color-danger-bg)`, `border-left: 4px solid var(--color-danger)`, `color: var(--color-danger)` |
| **Info** | `background: var(--color-info-bg)`, `border-left: 4px solid var(--color-info)`, `color: var(--color-info)` |
| **Icon** | `width: var(--alert-icon-size)`, `height: var(--alert-icon-size)`, `flex-shrink: 0` |
| **Progress bar** | `position: absolute`, `bottom: 0`, `left: 0`, `height: 3px`, `background: currentColor`, `opacity: 0.3`, `animation: progress var(--toast-duration) linear forwards` |
| **Exit** | `animation: slideOutRight var(--duration-fast)` |

#### Tokens consumidos

`--toast-offset`, `--z-toast`, `--alert-padding`, `--alert-radius`, `--alert-gap`, `--alert-icon-size`, `--toast-duration`, `--color-success-bg`, `--color-success`, `--color-warning-bg`, `--color-warning`, `--color-danger-bg`, `--color-danger`, `--color-info-bg`, `--color-info`, `--shadow-lg`, `--space-2`, `--duration-base`, `--duration-fast`

#### Exemplo de uso (Singleton ToastManager)

```js
import { createToast } from './components/toast.js';

// ToastManager global (singleton)
const toast = {
  success: (title, message, opts = {}) => createToast({ variant: 'success', title, message, ...opts }).show(),
  warning: (title, message, opts = {}) => createToast({ variant: 'warning', title, message, ...opts }).show(),
  danger: (title, message, opts = {}) => createToast({ variant: 'danger', title, message, persistent: true, ...opts }).show(),
  info: (title, message, opts = {}) => createToast({ variant: 'info', title, message, ...opts }).show()
};

// Uso em qualquer lugar
toast.success('Ativo criado', 'O ativo AT-2024-001 foi cadastrado com sucesso.');
toast.danger('Erro ao salvar', 'Tag já cadastrada. Informe uma tag única.', { 
  action: { label: 'Tentar novamente', onClick: () => modalCriarAtivo.show() } 
});
```

---

### 4.8 Badge

* **Propósito:** Indicador de status visual para ativos, manutenções, alertas (Flow 3, 4, 5). **Nunca comunica status apenas por cor** — sempre com texto + ícone opcional.
* **Anatomia:** `span.badge` + `span.dot` (opcional) + `span.text`.

#### Props/API

| Prop | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `variant` | `'ativo' \| 'manutencao' \| 'baixa' \| 'critico' \| 'info' \| 'warning' \| 'success' \| 'neutral'` | `'neutral'` | Mapeia tokens semânticos |
| `label` | `string` | `''` | Texto exibido |
| `dot` | `boolean` | `false` | Indicador circular antes do label |
| `removable` | `boolean` | `false` | Botão fechar (para chips em multi-select) |
| `onRemove` | `() => void` | `undefined` | Handler de remoção |

#### Estados visuais

| Variante | Background | Texto | Uso |
| :--- | :--- | :--- | :--- |
| `ativo` | `var(--badge-status-ativo-bg)` | `var(--badge-status-ativo-text)` | Ativo operacional |
| `manutencao` | `var(--badge-status-manutencao-bg)` | `var(--badge-status-manutencao-text)` | Em manutenção / Aguardando aprovação |
| `baixa` | `var(--badge-status-baixa-bg)` | `var(--badge-status-baixa-text)` | Baixado / Inativo |
| `critico` | `var(--badge-status-alerta-critico-bg)` | `var(--badge-status-alerta-critico-text)` | Alerta disco/saúde crítico |
| `success` | `var(--color-success-bg)` | `var(--color-success)` | Genérico sucesso |
| `warning` | `var(--color-warning-bg)` | `var(--color-warning)` | Genérico aviso |
| `info` | `var(--color-info-bg)` | `var(--color-info)` | Genérico info |
| `neutral` | `var(--color-bg-tertiary)` | `var(--color-text-secondary)` | Neutro |

#### Tokens consumidos

`--badge-padding-x`, `--badge-padding-y`, `--badge-font-size`, `--badge-font-weight`, `--badge-radius`, `--badge-status-*-bg`, `--badge-status-*-text`, `--color-success-bg`, `--color-success`, `--color-warning-bg`, `--color-warning`, `--color-info-bg`, `--color-info`, `--color-bg-tertiary`, `--color-text-secondary`, `--radius-full`, `--text-xs`, `--space-2`

#### Exemplo de uso

```js
import { createBadge } from './components/badge.js';

// Em DataTable render de status
render: (status) => {
  const variantMap = {
    'ATIVO': 'ativo',
    'EM_MANUTENCAO': 'manutencao',
    'AGUARDANDO_APROVACAO': 'manutencao',
    'BAIXADO': 'baixa',
    'CANCELADO': 'neutral'
  };
  return createBadge({ variant: variantMap[status] || 'neutral', label: status, dot: true }).element.outerHTML;
}
```

---

### 4.9 Tooltip

* **Propósito:** Texto contextual em hover/focus — ícones de ação, headers de tabela truncados, badges. **Usa `@popperjs/core`** para posicionamento.
* **Anatomia:** `div[role="tooltip"]` + `div.arrow` + `div.content`.

#### Props/API

| Prop | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `content` | `string \| HTMLElement` | **obrigatório** | Conteúdo do tooltip |
| `placement` | `'top' \| 'bottom' \| 'left' \| 'right'` | `'top'` | Posição relativa ao trigger |
| `trigger` | `'hover' \| 'focus' \| 'click'` | `'hover'` | Evento que abre |
| `delay` | `number` | `200` | ms para mostrar/esconder |
| `interactive` | `boolean` | `false` | Permite hover no próprio tooltip |
| `offset` | `number` | `8` | Distância do trigger (px) |

#### Estados visuais

| Estado | Comportamento Visual |
| :--- | :--- |
| **Default** | `background: var(--color-text-primary)`, `color: var(--color-text-inverse)`, `padding: var(--space-1) var(--space-2)`, `border-radius: var(--radius-sm)`, `font-size: var(--text-xs)`, `white-space: nowrap`, `box-shadow: var(--shadow-md)`, `z-index: var(--z-tooltip)` |
| **Arrow** | `::before` com `border-color: var(--color-text-primary) transparent` |
| **Animation** | `opacity: 0 → 1`, `transform: translateY(-4px) → 0`, `transition: var(--duration-fast)` |

#### Tokens consumidos

`--color-text-primary`, `--color-text-inverse`, `--space-1`, `--space-2`, `--radius-sm`, `--text-xs`, `--shadow-md`, `--z-tooltip`, `--duration-fast`

#### Exemplo de uso

```js
import { createTooltip } from './components/tooltip.js';

// Em IconButton da tabela
const btnEditar = createIconButton({ ariaLabel: 'Editar ativo', icon: editIcon });
const tooltipEditar = createTooltip({
  trigger: btnEditar.element,
  content: 'Editar ativo',
  placement: 'top',
  delay: 150
});

// Cleanup
btnEditar.element.addEventListener('remove', () => tooltipEditar.destroy());
```

---

### 4.10 Sidebar

* **Propósito:** Navegação principal lateral — **componente de layout** presente em todas as telas autenticadas (Flow 1–7). Responsivo: fixa em `≥ --bp-lg`, drawer em `< --bp-lg`.
* **Anatomia:** `aside[role="navigation"]` + `nav` + `ul` + `li` + `a`/`button` + `div.user-menu` (bottom).

#### Props/API

| Prop | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `items` | `NavItem[]` | `[]` | Itens de navegação (ver abaixo) |
| `collapsible` | `boolean` | `true` | Permite colapsar para ícones apenas |
| `collapsed` | `boolean` | `false` | Estado inicial |
| `activeItem` | `string` | `undefined` | Key do item ativo |
| `logo` | `{ src: string, alt: string, href: string }` | `undefined` | Logo no topo |
| `userMenu` | `UserMenuConfig` | `undefined` | Menu do usuário no rodapé |
| `onNavigate` | `(href: string) => void` | `undefined` | Handler de navegação (SPA router) |
| `onToggleCollapse` | `(collapsed: boolean) => void` | `undefined` | Persiste em localStorage |

#### NavItem

```ts
interface NavItem {
  key: string;           // ex: 'ativos', 'manutencoes'
  label: string;         // ex: 'Ativos'
  icon: HTMLElement;     // SVG icon
  href?: string;         // rota (se não tiver children)
  children?: NavItem[];  // submenu (accordion)
  badge?: string | number; // contador (ex: alertas não lidos)
  roles?: ('ADMIN' | 'USER')[]; // controle de visibilidade por role
}
```

#### Estados visuais

| Estado | Comportamento Visual |
| :--- | :--- |
| **Sidebar expandida** | `width: 260px`, `transition: width var(--duration-base)` |
| **Sidebar colapsada** | `width: 72px`, labels ocultos (`opacity: 0`, `pointer-events: none`), tooltips nos ícones |
| **Item ativo** | `background: var(--color-accent-light)`, `color: var(--color-accent-primary)`, `border-right: 3px solid var(--color-accent-primary)` |
| **Item hover** | `background: var(--color-bg-tertiary)` |
| **Submenu aberto** | `max-height: 0 → auto`, `transition: max-height var(--duration-base)` |
| **Mobile (drawer)** | `position: fixed`, `left: 0`, `top: 0`, `height: 100vh`, `z-index: var(--z-sidebar)`, `transform: translateX(-100%) → 0`, backdrop `var(--z-modal-backdrop)` |

#### Tokens consumidos

`--color-bg-secondary`, `--color-bg-tertiary`, `--color-accent-light`, `--color-accent-primary`, `--color-text-primary`, `--color-text-secondary`, `--color-text-muted`, `--space-3`, `--space-4`, `--space-6`, `--radius-md`, `--shadow-md`, `--z-sidebar`, `--z-modal-backdrop`, `--duration-base`, `--bp-lg`

#### Exemplo de uso

```js
import { createSidebar } from './components/sidebar.js';

const navItems = [
  { key: 'dashboard', label: 'Dashboard', icon: homeIcon, href: '/dashboard', roles: ['ADMIN', 'USER'] },
  { key: 'ativos', label: 'Ativos', icon: boxIcon, href: '/ativos', roles: ['ADMIN', 'USER'] },
  { key: 'manutencoes', label: 'Manutenções', icon: toolIcon, href: '/manutencoes', badge: 3, roles: ['ADMIN', 'USER'] },
  { key: 'alertas', label: 'Alertas', icon: bellIcon, href: '/alertas', badge: 7, roles: ['ADMIN', 'USER'] },
  { key: 'cadastros', label: 'Cadastros', icon: databaseIcon, children: [
    { key: 'filiais', label: 'Filiais', href: '/cadastros/filiais', roles: ['ADMIN'] },
    { key: 'departamentos', label: 'Departamentos', href: '/cadastros/departamentos', roles: ['ADMIN'] },
    { key: 'tipos-ativo', label: 'Tipos de Ativo', href: '/cadastros/tipos-ativo', roles: ['ADMIN'] },
    { key: 'fornecedores', label: 'Fornecedores', href: '/cadastros/fornecedores', roles: ['ADMIN'] },
    { key: 'localizacoes', label: 'Localizações', href: '/cadastros/localizacoes', roles: ['ADMIN'] }
  ], roles: ['ADMIN'] },
  { key: 'relatorios', label: 'Relatórios', icon: chartIcon, href: '/relatorios', roles: ['ADMIN'] },
  { key: 'auditoria', label: 'Auditoria', icon: shieldIcon, href: '/auditoria', roles: ['ADMIN'] },
  { key: 'usuarios', label: 'Usuários', icon: usersIcon, href: '/usuarios', roles: ['ADMIN'] }
];

const sidebar = createSidebar({
  items: navItems,
  collapsible: true,
  collapsed: localStorage.getItem('sidebarCollapsed') === 'true',
  logo: { src: '/logo.svg', alt: 'Aegis Patrimonio', href: '/dashboard' },
  userMenu: {
    name: currentUser.nome,
    email: currentUser.email,
    avatar: currentUser.avatarUrl,
    items: [
      { label: 'Meu Perfil', icon: userIcon, onClick: () => router.navigate('/perfil') },
      { label: 'Preferências', icon: settingsIcon, onClick: () => router.navigate('/preferencias') },
      { type: 'divider' },
      { label: 'Sair', icon: logoutIcon, variant: 'danger', onClick: () => auth.logout() }
    ]
  },
  onNavigate: (href) => router.navigate(href),
  onToggleCollapse: (collapsed) => localStorage.setItem('sidebarCollapsed', collapsed)
});

// Render
document.querySelector('#app-layout').prepend(sidebar.element);
```

---

### 4.11 FormLayout

* **Propósito:** Layout de formulário responsivo (grid 1/2/3 colunas) com labels alinhados — usado em **todos os modais de criação/edição** (Flow 2, 3, 4, 7).
* **Anatomia:** `form` + `div.form-grid` + `div.form-field` (label + input + helper/error) + `div.form-actions` (footer).

#### Props/API

| Prop | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `fields` | `FormField[]` | `[]` | Array de campos (ver abaixo) |
| `colCount` | `1 \| 2 \| 3` | `2` | Colunas no desktop (≥ `--bp-lg`) |
| `labelPosition` | `'top' \| 'left'` | `'top'` | Label acima ou à esquerda do input |
| `onSubmit` | `(data: Record<string, any>) => Promise<void>` | `undefined` | Handler de submit |
| `submitButton` | `ButtonConfig` | `undefined` | Config do botão submit |
| `cancelButton` | `ButtonConfig` | `undefined` | Config do botão cancelar |

#### FormField

```ts
interface FormField {
  key: string;                    // nome do campo no objeto de dados
  component: HTMLElement;         // Input.element, Select.element, etc.
  span?: number;                  // col-span (1 a colCount)
  required?: boolean;             // asterisco no label
  validator?: (value: any) => string | null; // validação síncrona
  asyncValidator?: (value: any) => Promise<string | null>; // validação assíncrona
}
```

#### Estados visuais

| Estado | Comportamento Visual |
| :--- | :--- |
| **Grid** | `display: grid`, `grid-template-columns: repeat(var(--col-count), 1fr)`, `gap: var(--space-4) var(--space-6)` |
| **Responsive** | `@media (max-width: 768px) { grid-template-columns: 1fr }` |
| **Field** | `display: flex`, `flex-direction: column`, `gap: var(--label-gap)` |
| **Label** | `font-size: var(--label-font-size)`, `font-weight: var(--label-font-weight)` |
| **Error state** | Input com `aria-invalid="true"`, error text com `role="alert"` |
| **Actions** | `display: flex`, `justify-content: flex-end`, `gap: var(--space-3)`, `padding-top: var(--space-4)`, `border-top: 1px solid var(--color-border-light)`, `margin-top: var(--space-6)` |

#### Tokens consumidos

`--space-3`, `--space-4`, `--space-6`, `--label-font-size`, `--label-font-weight`, `--label-gap`, `--color-border-light`, `--color-text-primary`, `--color-text-secondary`, `--color-text-muted`, `--duration-base`

#### Exemplo de uso (Flow 3 - Formulário de Ativo)

```js
import { createFormLayout } from './components/form-layout.js';
import { createInput } from './components/input.js';
import { createSelect } from './components/select.js';
import { createDatePicker } from './components/date-picker.js';
import { createTextarea } from './components/textarea.js';

const fields = [
  { key: 'tag', component: createInput({ label: 'Tag *', required: true, maxLength: 50 }).element, span: 1, required: true },
  { key: 'nome', component: createInput({ label: 'Nome *', required: true, maxLength: 200 }).element, span: 2, required: true },
  { key: 'descricao', component: createTextarea({ label: 'Descrição', rows: 3 }).element, span: 3 },
  { key: 'tipoId', component: createSelect({ label: 'Tipo de Ativo *', required: true, loadOptions: loadTiposAtivo }).element, span: 1, required: true },
  { key: 'filialId', component: createSelect({ label: 'Filial *', required: true, loadOptions: loadFiliais }).element, span: 1, required: true },
  { key: 'departamentoId', component: createSelect({ label: 'Departamento *', required: true, loadOptions: loadDepartamentos }).element, span: 1, required: true },
  { key: 'localizacaoId', component: createSelect({ label: 'Localização', loadOptions: loadLocalizacoes }).element, span: 1 },
  { key: 'fornecedorId', component: createSelect({ label: 'Fornecedor', loadOptions: loadFornecedores }).element, span: 1 },
  { key: 'dataAquisicao', component: createDatePicker({ label: 'Data de Aquisição *', required: true, maxDate: new Date() }).element, span: 1, required: true },
  { key: 'valorAquisicao', component: createInput({ label: 'Valor de Aquisição (R$)', type: 'number', step: '0.01', min: 0 }).element, span: 1 },
  { key: 'vidaUtilAnos', component: createInput({ label: 'Vida Útil (anos)', type: 'number', min: 1, max: 50 }).element, span: 1 },
  { key: 'status', component: createSelect({ label: 'Status *', required: true, options: [
    { value: 'ATIVO', label: 'Ativo' },
    { value: 'EM_MANUTENCAO', label: 'Em Manutenção' },
    { value: 'AGUARDANDO_APROVACAO', label: 'Aguardando Aprovação' },
    { value: 'BAIXADO', label: 'Baixado' }
  ]}).element, span: 1, required: true }
];

const formLayout = createFormLayout({
  fields,
  colCount: 3,
  labelPosition: 'top',
  submitButton: { variant: 'primary', label: 'Salvar', loading: false },
  cancelButton: { variant: 'secondary', label: 'Cancelar' },
  onSubmit: async (data) => {
    if (editingId) {
      await api.put(`/ativos/${editingId}`, data);
      toast.success('Ativo atualizado');
    } else {
      await api.post('/ativos', data);
      toast.success('Ativo criado');
    }
    modalCriarAtivo.hide();
    table.refresh();
  }
});
```

---

## 5. Componentes em Depreciação

| Componente | Motivo | Substituído por | Prazo de Remoção |
| :--- | :--- | :--- | :--- |
| *Nenhum* | Primeira versão do design system — não há componentes legados | — | — |

---

## 6. Gaps Conhecidos

| Componente Necessário | Contexto / Fluxo | Prioridade | Observações |
| :--- | :--- | :--- | :--- |
| **Wizard / Stepper** | Flow 3 (Criação de Ativo com hardware em abas), Flow 4 (Fluxo de manutenção multi-etapas) | Alta | Necessário para formulários complexos em etapas |
| **TreeSelect** | Flow 2 (Hierarquia de filiais/departamentos/localizações) | Média | Seleção em árvore com busca |
| **DataGrid (virtualizado)** | Flow 3 (Lista de ativos > 10k registros) | Média | DataTable atual não virtualiza — performance em listas grandes |
| **Chart / Sparkline** | Flow 5 (Histórico de saúde de disco), Flow 6 (Dashboards TCO) | Média | Gráficos simples (linha, barra, área) — considerar `uPlot` ou `Chart.js` leve |
| **RichTextEditor** | Flow 4 (Descrição detalhada de manutenção com formatação) | Baixa | Apenas se requisito de negócio exigir |
| **Calendar / DateRangePicker** | Flow 6 (Filtros de período em relatórios) | Média | Atualmente só `DatePicker` single |
| **Command Palette (Cmd+K)** | Transversal — navegação rápida, ações globais | Baixa | Melhoria de UX para power users (ADMIN) |
| **Tour / Onboarding** | Flow 1 (Primeiro acesso) | Baixa | Guias interativos para novos usuários |

---

## 7. Referências

* **Design Tokens:** `design-tokens.md` (este repositório) — fonte única de verdade para cores, espaçamento, tipografia, motion, z-index, breakpoints, tokens semânticos de componente
* **User Flows:** `user-flows.md` (este repositório) — define quais componentes são necessários em cada fluxo e seus estados
* **Glossário Ubíquo:** `docs/glossario.md` <!-- source: glossario#L1-L75 --> — nomenclatura de domínio (Ativo, Manutenção, Filial, etc.)
* **Business Requirements Document:** `docs/brd.md` (seções 3, 4, 6) — regras de negócio (BR-01 a BR-10), KPIs, personas
* **Diagnóstico Determinístico:** `docs/diagnostico.md` — confirma stack vanilla JS + Popper.js only, 0 componentes UI existentes
* **Popper.js Docs:** https://popper.js.org/docs/v2/ — posicionamento de Tooltip, Dropdown, Select, DatePicker
* **WCAG 2.1 AA:** https://www.w3.org/WAI/WCAG21/quickref/ — checklist de acessibilidade aplicado a todos os componentes

---

> **Próximos passos (action items):**
> 1. [ ] Criar diretório `frontend/src/components/` e `index.js` barrel export
> 2. [ ] Implementar `Button`, `Input`, `Select`, `Modal`, `Toast` (core para formulários)
> 3. [ ] Implementar `DataTable` (componente mais complexo — prioridade alta)
> 4. [ ] Implementar `Sidebar`, `Header`, `FormLayout` (layout base da aplicação)
> 5. [ ] Implementar `Badge`, `Tooltip`, `IconButton`, `Alert`, `EmptyState`, `Skeleton`
> 6. [ ] Configurar CSS Custom Properties globais (`tokens.css`) importado no entry point
> 7. [ ] Adicionar testes de acessibilidade (axe-core) e visuais (Playwright) por componente
> 8. [ ] Documentar cada componente com exemplos em `frontend/docs/components/` (ou Storybook se adotado futuramente)