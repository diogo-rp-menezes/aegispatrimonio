# Component Library / Inventory — Aegis1

> **Versão:** 0.1 · **Owner:** Frontend Lead · **Status:** Draft — *Nenhum componente implementado no codebase atual*  
> **Baseado em:** Nenhuma biblioteca base detectada (projeto vanilla JS + @popperjs/core apenas)  
> **Depende de:** `design-tokens.md` (tokens não implementados), `user-flows.md` (fluxos inferidos)

---

## 1. Overview

A varredura determinística do workspace (**15 arquivos `.js`**, 0 arquivos `.tsx/.jsx/.vue/.svelte`, 0 arquivos CSS/SCSS, 0 configurações de design system) **não encontrou nenhum componente de UI** no repositório atual.

O código existente limita-se a:
- `frontend/src/services/api.js` — camada de integração HTTP (`request`, `authInterceptor`, `handleResponse`, `handleApiError`)
- Demais arquivos `.js` (não inspecionados individualmente no diagnóstico) — presumivelmente utilitários, constants ou lógica de negócio pura

**Não há:**
- Framework de componentes (React, Vue, Svelte, Solid, Lit, etc.)
- Sistema de roteamento
- Pasta `components/` ou `ui/`
- Storybook, Chromatic ou catálogo visual
- Testes de componente (Vitest, Playwright component testing, etc.)

Este documento serve como **especificação do inventário mínimo necessário** para cobrir os 8 user flows documentados em `user-flows.md`, alinhado aos tokens propostos em `design-tokens.md`. Cada componente listado abaixo é **necessário, não existente**.

---

## 2. Convenções Gerais (Propostas)

| Aspecto | Decisão Proposta | Rastreabilidade |
| :--- | :--- | :--- |
| **Framework alvo** | **React 18 + TypeScript** (migração do vanilla JS atual) | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — Escolha baseada em ecossistema, tooling e compatibilidade com @popperjs/core |
| **Padrão de composição** | **Compound Components + CVA (class-variance-authority)** para variantes; slots via `children` / `Render Props` | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] |
| **Estilização** | **CSS Modules + CSS Custom Properties** (tokens) — zero runtime CSS-in-JS | Alinhado ao `design-tokens.md` (Fase 1: `tokens.css` nativo) |
| **Nomenclatura** | PascalCase para componentes (`Button`, `DataTable`); `use*` para hooks; `*.module.css` para estilos | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] |
| **Local no repositório** | `frontend/src/components/ui/` (componentes base) + `frontend/src/components/domain/` (componentes de negócio: `OrderCard`, `AssetCostTable`, etc.) | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] |
| **Acessibilidade** | **Obrigatório:** ARIA patterns (WAI-ARIA APG), focus management, `prefers-reduced-motion`, contraste WCAG AA | `design-tokens.md` §5, `user-flows.md` §5 |
| **Documentação** | Storybook 8 (CSF + MDX) — um story por variante/estado | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] |

---

## 3. Inventário de Componentes Necessários

> **Legenda Status:** `Planned` = especificado aqui, não iniciado; `In Progress` = em desenvolvimento; `Done` = implementado + testado + documentado no Storybook.  
> **Todos os itens abaixo estão `Planned`.**

| Componente | Categoria | Variantes Previstas | Status | Localização Proposta | Fluxos que Dependem |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Button** | Ação | `primary`, `secondary`, `ghost`, `destructive`, `outline`; `sm`, `md`, `lg`; `loading`, `disabled` | Planned | `components/ui/Button/` | Todos (ações primárias/secundárias) |
| **IconButton** | Ação | `ghost`, `outline`; `sm`, `md`, `lg`; `loading`, `disabled` | Planned | `components/ui/IconButton/` | Toolbar tabelas, ações de linha, close modais |
| **Input** | Formulário | `default`, `error`, `disabled`, `readonly`; `sm`, `md`, `lg` | Planned | `components/ui/Input/` | Flow 1, 2, 5, 6, 8 |
| **Textarea** | Formulário | `default`, `error`, `disabled`, `readonly`; `sm`, `md`, `lg` | Planned | `components/ui/Textarea/` | Flow 2 (descrição), 4 (obs. aprovação), 5 (obs. fechamento), 6 (motivo cancelamento) |
| **Select** | Formulário | `default`, `error`, `disabled`, `searchable`, `multi`; `sm`, `md`, `lg` | Planned | `components/ui/Select/` | Flow 1 (entidades), 2 (ativo, fornecedor, técnico), 8 (filtros) |
| **Checkbox** | Formulário | `default`, `error`, `disabled`, `indeterminate` | Planned | `components/ui/Checkbox/` | Flow 2 (checklists), 4 (evidências), 8 (filtros multi) |
| **RadioGroup** | Formulário | `default`, `error`, `disabled`, `inline` | Planned | `components/ui/RadioGroup/` | Flow 2 (prioridade, tipo ordem), 8 (filtros estado) |
| **Switch** | Formulário | `default`, `disabled` | Planned | `components/ui/Switch/` | Flow 8 (filtros booleanos), configurações |
| **DatePicker** | Formulário | `single`, `range`, `disabled`; com `Input` nativo + popover | Planned | `components/ui/DatePicker/` | Flow 2 (datas), 5 (período), 8 (filtros data) |
| **Modal** | Overlay | `default`, `confirmation`, `form`, `fullscreen`; `sm`, `md`, `lg`, `xl` | Planned | `components/ui/Modal/` | Flow 1 (CRUD), 2 (wizard), 3, 4, 5, 6 (confirmações) |
| **Drawer** | Overlay | `left`, `right`; `md`, `lg`, `full` | Planned | `components/ui/Drawer/` | Flow 2 (wizard lateral mobile), 4 (evidências), 7 (drill-down) |
| **Toast** | Feedback | `success`, `error`, `warning`, `info`; `persistent`, `auto-dismiss` | Planned | `components/ui/Toast/` | Todos (feedback global) |
| **Tooltip** | Feedback | `default`, `error`, `info`; `top`, `bottom`, `left`, `right` | Planned | `components/ui/Tooltip/` | Flow 4 (botão desabilitado), 8 (ícones info) |
| **Popover** | Overlay | `default`, `menu`; `top`, `bottom` | Planned | `components/ui/Popover/` | Flow 8 (ações de linha), @popperjs/core já disponível |
| **DropdownMenu** | Navegação | `default`, `checkbox`, `radio`, `separator` | Planned | `components/ui/DropdownMenu/` | Flow 1 (ações linha), 3, 4, 5, 8 (ações contexto) |
| **Tabs** | Navegação | `default`, `underline`, `pills`; `horizontal`, `vertical` | Planned | `components/ui/Tabs/` | Flow 4 (abas detalhe: Geral, Evidências, Custos, Timeline) |
| **Breadcrumb** | Navegação | `default`, `collapsed` | Planned | `components/ui/Breadcrumb/` | Flow 7 (drill-down ativo → ordens), 8 (detalhe ordem) |
| **Pagination** | Dados | `default`, `compact`, `show-size-changer` | Planned | `components/ui/Pagination/` | Flow 1, 8 (listagens paginadas) |
| **DataTable** | Dados | `sortable`, `filterable`, `selectable`, `expandable`, `sticky-header`, `virtualized` | Planned | `components/ui/DataTable/` | Flow 1, 3, 4, 7, 8 (tabelas mestras/ordens) |
| **Table** | Dados | `striped`, `bordered`, `hoverable`, `compact`, `responsive` | Planned | `components/ui/Table/` | Fallback simples para DataTable |
| **Badge** | Indicador | `default`, `success`, `warning`, `danger`, `info`, `outline`; `sm`, `md`, `dot` | Planned | `components/ui/Badge/` | Flow 3, 4, 5, 6, 7, 8 (estados: Aberta, Em Andamento, Aprovada, Concluída, Cancelada) |
| **Avatar** | Identidade | `image`, `initials`, `icon`; `xs`, `sm`, `md`, `lg`, `xl` | Planned | `components/ui/Avatar/` | Flow 3, 4, 5 (responsável técnico, aprovador) |
| **Card** | Layout | `default`, `outlined`, `elevated`, `interactive`; `padding: none, sm, md, lg` | Planned | `components/ui/Card/` | Flow 1 (cards entidade), 7 (cards custo ativo), 8 (cards resumo) |
| **Accordion** | Layout | `single`, `multiple`; `default`, `bordered` | Planned | `components/ui/Accordion/` | Flow 4 (seções evidências), 7 (drill-down colapsável) |
| **Divider** | Layout | `horizontal`, `vertical`; `dashed`, `solid` | Planned | `components/ui/Divider/` | Separação visual genérica |
| **Skeleton** | Feedback | `text`, `circular`, `rectangular`, `table-row`, `card` | Planned | `components/ui/Skeleton/` | Todos (estados loading) |
| **EmptyState** | Feedback | `default`, `action`, `illustration` | Planned | `components/ui/EmptyState/` | Flow 1, 3, 4, 5, 7, 8 (listas vazias) |
| **ErrorBoundary** | Feedback | `default`, `with-retry`, `fallback-ui` | Planned | `components/ui/ErrorBoundary/` | Todos (erros 5xx, boundary React) |
| **LoadingOverlay** | Feedback | `fullscreen`, `inline`, `skeleton` | Planned | `components/ui/LoadingOverlay/` | Flow 8 (overlay tabela), todos (submit actions) |
| **Wizard/Stepper** | Formulário | `horizontal`, `vertical`; `numbered`, `simple` | Planned | `components/domain/OrderWizard/` | Flow 2 (criação ordem 4 etapas) |
| **OrderTimeline** | Domínio | `vertical`, `horizontal`; `compact`, `detailed` | Planned | `components/domain/OrderTimeline/` | Flow 3, 4, 5, 6, 8 (histórico estados) |
| **OrderCostForm** | Domínio | `create`, `edit`, `view` | Planned | `components/domain/OrderCostForm/` | Flow 5 (formulário fechamento: horas, materiais, terceiros) |
| **AssetCostTable** | Domínio | `summary`, `detailed`, `drillable` | Planned | `components/domain/AssetCostTable/` | Flow 7 (custoTotalPorAtivo) |
| **EntityCrudTable** | Domínio | `departamentos`, `filiais`, `fornecedores`, `funcionarios` | Planned | `components/domain/EntityCrudTable/` | Flow 1 (CRUD 4 entidades) |
| **SearchInput** | Navegação | `default`, `with-filters`, `debounced` | Planned | `components/ui/SearchInput/` | Flow 8 (busca global + filtros) |
| **FilterBar** | Navegação | `inline`, `collapsible`, `sidebar` | Planned | `components/ui/FilterBar/` | Flow 8 (filtros avançados ordens) |

**Total: 34 componentes** (26 base/ui + 8 domínio)

---

## 4. Especificação por Componente (Amostra Representativa)

> **Nota:** A especificação completa de 34 componentes excederia o tamanho razoável deste documento. Abaixo, **6 componentes críticos** (Button, Modal, DataTable, Badge, Wizard/Stepper, OrderCostForm) são detalhados como referência de padrão. Os demais seguem a mesma estrutura.

---

### 4.1 Button

* **Propósito:** Ação principal da interface — submeter formulários, disparar transições de estado (Iniciar, Aprovar, Concluir, Cancelar), navegação destrutiva.
* **Anatomia:** `[IconLeading] + Label + [IconTrailing] + [LoadingSpinner]`
* **Props/API:**

| Prop | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `variant` | `'primary' \| 'secondary' \| 'ghost' \| 'destructive' \| 'outline'` | `'primary'` | Estilo visual semântico |
| `size` | `'sm' \| 'md' \| 'lg'` | `'md'` | Tamanho (padding, font-size, height) |
| `disabled` | `boolean` | `false` | Desabilita interação + `aria-disabled` |
| `loading` | `boolean` | `false` | Mostra spinner, desabilita, mantém largura |
| `fullWidth` | `boolean` | `false` | `width: 100%` |
| `iconLeft` | `ReactNode` | — | Ícone à esquerda do label |
| `iconRight` | `ReactNode` | — | Ícone à direita do label |
| `type` | `'button' \| 'submit' \| 'reset'` | `'button'` | Tipo nativo do `<button>` |
| `onClick` | `(e: React.MouseEvent<HTMLButtonElement>) => void` | — | Handler de clique |

* **Estados visuais:**

| Estado | Comportamento Visual (Tokens) |
| :--- | :--- |
| Default | `bg: var(--color-accent)`, `color: white`, `border: none`, `border-radius: var(--radius-md)`, `padding: var(--space-2) var(--space-4)`, `font: var(--text-sm) var(--font-sans)`, `box-shadow: var(--shadow-sm)` |
| Hover | `bg: var(--color-accent-hover)`, `box-shadow: var(--shadow-md)` |
| Focus | `outline: none`, `box-shadow: var(--shadow-focus), var(--shadow-md)` |
| Active/Pressed | `bg: var(--color-accent-hover)`, `transform: scale(0.98)`, `box-shadow: var(--shadow-sm)` |
| Disabled | `opacity: 0.5`, `cursor: not-allowed`, `box-shadow: none` |
| Loading | `cursor: wait`, label oculto (sr-only), spinner centralizado (`var(--color-accent-light)`), largura fixa |
| Error (destructive variant) | `bg: var(--color-danger)`, `hover: var(--color-danger-hover)`, `focus: var(--shadow-focus-danger)` |

* **Tokens consumidos:** `--color-accent`, `--color-accent-hover`, `--color-accent-light`, `--color-danger`, `--color-danger-hover`, `--color-danger-light`, `--color-text-primary`, `--color-border`, `--radius-md`, `--space-2`, `--space-4`, `--text-sm`, `--font-sans`, `--shadow-sm`, `--shadow-md`, `--shadow-focus`, `--duration-fast`, `--easing-standard`
* **Composição/Slots:** `children` (label), `iconLeft`, `iconRight` — aceita qualquer `ReactNode`
* **Exemplo de uso:**
```tsx
// Flow 3: Botão "Iniciar" na ordem Aberta
<Button
  variant="primary"
  size="md"
  iconLeft={<PlayIcon />}
  loading={isStarting}
  disabled={!canStart}
  onClick={handleStartOrder}
>
  Iniciar Ordem
</Button>

// Flow 6: Botão "Cancelar" (destrutivo)
<Button
  variant="destructive"
  size="sm"
  iconLeft={<XCircleIcon />}
  onClick={() => setShowCancelModal(true)}
>
  Cancelar
</Button>
```

---

### 4.2 Modal

* **Propósito:** Overlay modal para confirmações destrutivas, formulários complexos (CRUD entidades, wizard ordem), visualização de detalhes (evidências, custos).
* **Anatomia:** `Backdrop (Portal) → Container (role="dialog", aria-modal="true") → [Header: Title + Close] + Content + [Footer: Actions]`
* **Props/API:**

| Prop | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `open` | `boolean` | `false` | Controla visibilidade |
| `onClose` | `() => void` | — | Callback ao fechar (Esc, backdrop, botão) |
| `variant` | `'default' \| 'confirmation' \| 'form' \| 'fullscreen'` | `'default'` | Layout e comportamento |
| `size` | `'sm' \| 'md' \| 'lg' \| 'xl'` | `'md'` | Largura máxima do container |
| `title` | `string` | — | Título acessível (`aria-labelledby`) |
| `description` | `string` | — | Descrição acessível (`aria-describedby`) |
| `children` | `ReactNode` | — | Conteúdo do modal |
| `footer` | `ReactNode` | — | Ações fixadas no rodapé |
| `closeOnOverlayClick` | `boolean` | `true` | Fechar clicando no backdrop |
| `closeOnEscape` | `boolean` | `true` | Fechar com tecla Esc |
| `preventScroll` | `boolean` | `true` | `body { overflow: hidden }` enquanto aberto |

* **Estados visuais:**

| Estado | Comportamento Visual |
| :--- | :--- |
| Entrada (mount) | `opacity: 0 → 1`, `transform: scale(0.95) → 1`, `backdrop: opacity 0 → 1` — `var(--duration-base)`, `var(--easing-standard)` |
| Saída (unmount) | Reverso da entrada — `var(--duration-fast)` |
| Focus trap | `Tab`/`Shift+Tab` cicla dentro do modal; foco inicial no primeiro elemento focável ou `Close` |
| Scroll interno | `Content` com `max-height: calc(100vh - 200px)`, `overflow-y: auto` |

* **Tokens consumidos:** `--color-panel`, `--color-border`, `--color-text-primary`, `--radius-lg`, `--shadow-lg`, `--space-4`, `--space-6`, `--space-8`, `--text-xl`, `--text-base`, `--font-sans`, `--duration-base`, `--duration-fast`, `--easing-standard`, `--z-modal-backdrop`, `--z-modal`
* **Composição/Slots:** `children` (corpo), `footer` (ações), `title`/`description` (props para acessibilidade)
* **Exemplo de uso:**
```tsx
// Flow 1: Modal confirmação exclusão entidade (409 bloqueio)
<Modal
  open={showDeleteConfirm}
  onClose={() => setShowDeleteConfirm(false)}
  variant="confirmation"
  size="sm"
  title="Excluir Fornecedor"
  description="Esta ação não pode ser desfeita. 3 ordens estão vinculadas a este fornecedor."
  footer={
    <>
      <Button variant="ghost" onClick={() => setShowDeleteConfirm(false)}>Cancelar</Button>
      <Button variant="destructive" loading={isDeleting} onClick={confirmDelete}>
        Excluir mesmo assim
      </Button>
    </>
  }
>
  <p className="text-center text-lg">Tem certeza?</p>
</Modal>

// Flow 2: Wizard ordem em Modal fullscreen (mobile) / lg (desktop)
<Modal
  open={showOrderWizard}
  onClose={closeWizard}
  variant="form"
  size={isMobile ? 'fullscreen' : 'lg'}
  title="Nova Ordem de Manutenção"
  footer={<OrderWizardFooter currentStep={step} onNext={next} onBack={back} onSubmit={submit} />}
>
  <OrderWizardStep step={step} data={formData} onChange={setFormData} />
</Modal>
```

---

### 4.3 DataTable

* **Propósito:** Tabela de dados densa, ordenável, filtrável, paginada, com seleção de linhas e ações contextuais — usada em **todas as listagens** (entidades, ordens, custos).
* **Anatomia:** `Toolbar (busca, filtros, ações em lote) → Table (thead + tbody virtualizado) → Pagination`
* **Props/API:**

| Prop | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `columns` | `ColumnDef<T>[]` | — | Definição de colunas (header, accessor, cell renderer, sortable, filterable, width) |
| `data` | `T[]` | `[]` | Linhas de dados |
| `loading` | `boolean` | `false` | Mostra `Skeleton` rows |
| `emptyMessage` | `string` | `'Nenhum registro encontrado'` | Texto do `EmptyState` |
| `sortable` | `boolean` | `true` | Habilita ordenação por clique no header |
| `filterable` | `boolean` | `false` | Habilita filtros por coluna (header filter row) |
| `selectable` | `boolean` | `false` | Habilita checkbox de seleção + `onSelectionChange` |
| `selection` | `string[]` | `[]` | IDs selecionados |
| `onSelectionChange` | `(ids: string[]) => void` | — | Callback de mudança de seleção |
| `rowKey` | `string \| (row: T) => string` | `'id'` | Chave única da linha |
| `onRowClick` | `(row: T, event) => void` | — | Navega para detalhe (Flow 8) |
| `actions` | `RowAction<T>[]` | — | Ações por linha (Editar, Excluir, Iniciar, Aprovar, etc.) |
| `pagination` | `PaginationProps` | — | Configuração de paginação (server-side) |
| `virtualized` | `boolean` | `false` | Usa `@tanstack/react-virtual` para listas grandes |
| `stickyHeader` | `boolean` | `true` | Header fixo no scroll |
| `expandable` | `boolean` | `false` | Linha expansível para detalhe inline (Flow 7 drill-down) |
| `renderExpandedRow` | `(row: T) => ReactNode` | — | Renderizador da linha expandida |

* **Estados visuais:**

| Estado | Comportamento Visual |
| :--- | :--- |
| Loading | `Skeleton` rows (3-5 linhas) + `LoadingOverlay` na toolbar |
| Empty | `EmptyState` centralizado com ilustração + CTA "Criar primeiro" (se permissão) |
| Hover row | `bg: var(--color-accent-light)` (light) / `var(--color-accent-light-dark)` (dark) — `var(--shadow-sm)` |
| Selected row | `bg: var(--color-accent-light)`, border-left `3px solid var(--color-accent)` |
| Focus row (keyboard) | `outline: none`, `box-shadow: inset 0 0 0 2px var(--color-accent)` |
| Sorting | Header com `aria-sort="ascending/descending/none"`, ícone seta |
| Filtering | Input/select no header da coluna, debounce 300ms |

* **Tokens consumidos:** `--color-bg`, `--color-panel`, `--color-border`, `--color-border-strong`, `--color-text-primary`, `--color-text-secondary`, `--color-text-muted`, `--color-accent`, `--color-accent-light`, `--radius-sm`, `--radius-md`, `--space-1`, `--space-2`, `--space-3`, `--space-4`, `--text-xs`, `--text-sm`, `--text-base`, `--font-sans`, `--font-mono`, `--shadow-sm`, `--shadow-md`, `--duration-fast`, `--easing-standard`, `--z-dropdown`
* **Composição/Slots:** `columns[].cell` (render customizado), `actions` (array de `{ label, icon, onClick, variant, disabled, visible }`), `renderExpandedRow`
* **Exemplo de uso:**
```tsx
// Flow 8: Listagem de Ordens com filtros, seleção, ações contextuais
const columns: ColumnDef<Order>[] = [
  { accessor: 'id', header: 'ID', width: 80, sortable: true },
  { accessor: 'titulo', header: 'Título', cell: ({ row }) => <OrderTitleCell order={row.original} />, sortable: true },
  { accessor: 'ativo.tag', header: 'Ativo', width: 150, filterable: true },
  { accessor: 'estado', header: 'Estado', width: 140, cell: ({ row }) => <Badge variant={estadoToVariant(row.original.estado)}>{row.original.estado}</Badge> },
  { accessor: 'prioridade', header: 'Prioridade', width: 110, cell: ({ row }) => <PriorityBadge prioridade={row.original.prioridade} /> },
  { accessor: 'responsavel.nome', header: 'Responsável', width: 180 },
  { accessor: 'createdAt', header: 'Criada em', width: 160, cell: ({ row }) => formatDate(row.original.createdAt), sortable: true },
];

<DataTable
  columns={columns}
  data={orders}
  loading={isLoading}
  selectable
  selection={selectedIds}
  onSelectionChange={setSelectedIds}
  rowKey="id"
  onRowClick={(order) => navigate(`/ordens/${order.id}`)}
  actions={[
    { label: 'Iniciar', icon: <PlayIcon />, variant: 'primary', visible: (o) => o.estado === 'Aberta' && o.canStart, onClick: handleStart },
    { label: 'Aprovar', icon: <CheckCircleIcon />, variant: 'secondary', visible: (o) => o.estado === 'Em Andamento' && o.canApprove, onClick: handleApprove },
    { label: 'Concluir', icon: <FlagIcon />, variant: 'primary', visible: (o) => o.estado === 'Aprovada' && o.canComplete, onClick: handleComplete },
    { label: 'Cancelar', icon: <XCircleIcon />, variant: 'destructive', visible: (o) => o.estado !== 'Concluída' && o.canCancel, onClick: handleCancel },
    { label: 'Detalhes', icon: <EyeIcon />, variant: 'ghost', onClick: (o) => navigate(`/ordens/${o.id}`) },
  ]}
  pagination={{
    page,
    pageSize,
    total,
    onPageChange: setPage,
    onPageSizeChange: setPageSize,
    showSizeChanger: true,
  }}
  virtualized={orders.length > 100}
  stickyHeader
/>
```

---

### 4.4 Badge

* **Propósito:** Indicador visual de estado semântico (ordem, prioridade, evidência, custo) — **não apenas cor**, sempre com ícone/texto para acessibilidade.
* **Anatomia:** `[Icon] + Label` — `inline-flex`, `items-center`, `gap-1`
* **Props/API:**

| Prop | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `variant` | `'default' \| 'success' \| 'warning' \| 'danger' \| 'info' \| 'outline'` | `'default'` | Mapeia para cor semântica |
| `size` | `'sm' \| 'md' \| 'dot'` | `'md'` | `sm`: `text-xs`, `px-2 py-0.5`; `md`: `text-sm`, `px-2.5 py-0.5`; `dot`: apenas círculo 8px |
| `icon` | `ReactNode` | — | Ícone opcional à esquerda |
| `dot` | `boolean` | `false` | Se `true` + `size='dot'`: apenas indicador circular |
| `className` | `string` | — | Classes adicionais |

* **Estados visuais:**

| Variante | Light Mode | Dark Mode | Uso |
| :--- | :--- | :--- | :--- |
| `default` | `bg: var(--color-border)`, `color: var(--color-text-secondary)` | `bg: var(--color-border-dark)`, `color: var(--color-text-secondary-dark)` | Neutro (ex.: "Rascunho") |
| `success` | `bg: var(--color-success-light)`, `color: var(--color-success)` | `bg: var(--color-success-light-dark)`, `color: var(--color-success-dark)` | "Concluída", "Aprovada", "Ativo" |
| `warning` | `bg: var(--color-warning-light)`, `color: var(--color-warning)` | `bg: var(--color-warning-light-dark)`, `color: var(--color-warning-dark)` | "Em Andamento", "Pendente", "Atrasado" |
| `danger` | `bg: var(--color-danger-light)`, `color: var(--color-danger)` | `bg: var(--color-danger-light-dark)`, `color: var(--color-danger-dark)` | "Cancelada", "Crítica", "Bloqueada" |
| `info` | `bg: var(--color-info-light)`, `color: var(--color-info)` | `bg: var(--color-info-light-dark)`, `color: var(--color-info-dark)` | "Aberta", "Informativo", "Nova" |
| `outline` | `border: 1px solid var(--color-border-strong)`, `color: var(--color-text-primary)`, `bg: transparent` | `border: 1px solid var(--color-border-strong-dark)`, `color: var(--color-text-primary-dark)`, `bg: transparent` | Estado secundário, menos proeminente |

* **Tokens consumidos:** Todas as cores semânticas (`--color-success`, `--color-success-light`, etc.), `--color-border`, `--color-border-strong`, `--color-text-primary`, `--color-text-secondary`, `--radius-full`, `--space-1`, `--space-2`, `--text-xs`, `--text-sm`, `--font-sans`, `--duration-fast`
* **Composição/Slots:** `children` (label), `icon` (leading)
* **Exemplo de uso:**
```tsx
// Flow 3, 4, 5, 6, 8: Badge de estado da ordem
const estadoToVariant = (estado: OrderState): BadgeVariant => ({
  Aberta: 'info',
  'Em Andamento': 'warning',
  Aprovada: 'success',
  Concluída: 'success',
  Cancelada: 'danger',
}[estado]);

<Badge variant={estadoToVariant(order.estado)} icon={estadoToIcon(order.estado)}>
  {order.estado}
</Badge>

// Flow 7: Badge de custo alto (dot vermelho) na tabela de ativos
<Badge variant="danger" size="dot" aria-label="Custo acima do threshold" />
```

---

### 4.5 Wizard / Stepper (OrderWizard)

* **Propósito:** Formulário multi-etapa para criação de ordem (Flow 2) — 4 etapas: Dados Básicos → Ativo/Fornecedor → Responsável → Revisão.
* **Anatomia:** `StepperHeader (progresso) → StepContent (formulário da etapa) → StepperFooter (navegação)`
* **Props/API:**

| Prop | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `steps` | `WizardStep[]` | — | Array de `{ id, title, description, icon, component, validationSchema }` |
| `currentStep` | `number` | `0` | Índice da etapa ativa (controlado) |
| `onStepChange` | `(step: number) => void` | — | Callback de mudança de etapa |
| `onSubmit` | `(data: OrderFormData) => Promise<void>` | — | Submit final (etapa Revisão) |
| `onBack` | `() => void` | — | Voltar etapa anterior |
| `direction` | `'horizontal' \| 'vertical'` | `'horizontal'` | Layout do stepper header |
| `showStepNumbers` | `boolean` | `true` | Mostra números nos passos |

* **Estados visuais:**

| Estado | Comportamento Visual |
| :--- | :--- |
| Etapa atual | `border-color: var(--color-accent)`, `color: var(--color-accent)`, `font-weight: 600` |
| Etapa concluída | `bg: var(--color-success)`, `color: white`, ícone check |
| Etapa futura | `color: var(--color-text-muted)`, `border-color: var(--color-border)` |
| Etapa com erro | `border-color: var(--color-danger)`, `color: var(--color-danger)` |
| Transição conteúdo | `opacity 0→1`, `transform: translateY(8px)→0` — `var(--duration-base)` |
| Validação inline | `Input`/`Select` com `variant="error"`, mensagem `aria-live="polite"` |

* **Tokens consumidos:** `--color-accent`, `--color-accent-light`, `--color-success`, `--color-success-light`, `--color-danger`, `--color-danger-light`, `--color-border`, `--color-border-strong`, `--color-text-primary`, `--color-text-secondary`, `--color-text-muted`, `--radius-md`, `--radius-full`, `--space-3`, `--space-4`, `--space-6`, `--space-8`, `--text-sm`, `--text-base`, `--text-lg`, `--font-sans`, `--shadow-md`, `--duration-base`, `--easing-standard`
* **Composição/Slots:** Cada step recebe `component` (React component) com props `{ formData, errors, onChange, onBlur }`
* **Exemplo de uso:**
```tsx
// Flow 2: Wizard de criação de ordem
const wizardSteps: WizardStep<OrderFormData>[] = [
  {
    id: 'basicos',
    title: 'Dados Básicos',
    description: 'Título, descrição, prioridade e tipo da ordem',
    icon: <FileTextIcon />,
    component: BasicInfoStep,
    validationSchema: basicInfoSchema, // Zod/Yup
  },
  {
    id: 'ativo-fornecedor',
    title: 'Ativo / Fornecedor',
    description: 'Selecione o ativo e, se terceirizado, o fornecedor',
    icon: <BuildingIcon />,
    component: AssetSupplierStep,
    validationSchema: assetSupplierSchema,
  },
  {
    id: 'responsavel',
    title: 'Responsável',
    description: 'Técnico que executará a ordem',
    icon: <UserIcon />,
    component: ResponsibleStep,
    validationSchema: responsibleSchema,
  },
  {
    id: 'revisao',
    title: 'Revisão',
    description: 'Confirme os dados antes de criar',
    icon: <CheckCircleIcon />,
    component: ReviewStep,
    validationSchema: null, // Sem validação, apenas exibição
  },
];

<OrderWizard
  steps={wizardSteps}
  currentStep={currentStep}
  onStepChange={setCurrentStep}
  onSubmit={handleCreateOrder}
  direction={isMobile ? 'vertical' : 'horizontal'}
/>
```

---

### 4.6 OrderCostForm (Domínio)

* **Propósito:** Formulário de fechamento de ordem (Flow 5) — captura horas trabalhadas, materiais utilizados (quantidade + fornecedor), serviços terceiros, observações finais. Alimenta `custoTotalPorAtivo`.
* **Anatomia:** `Section: Mão de Obra → Section: Materiais (lista dinâmica) → Section: Serviços Terceiros (lista dinâmica) → Section: Observações → Actions`
* **Props/API:**

| Prop | Tipo | Default | Descrição |
| :--- | :--- | :--- | :--- |
| `initialData` | `OrderCostData` | — | Dados pré-preenchidos (edição) |
| `onSubmit` | `(data: OrderCostData) => Promise<void>` | — | Submit para `PATCH /api/ordens/{id}/concluir` |
| `onCancel` | `() => void` | — | Fecha formulário |
| `loading` | `boolean` | `false` | Estado de submissão |
| `readonly` | `boolean` | `false` | Modo visualização (ordem já concluída) |

* **Estados visuais:**

| Estado | Comportamento Visual |
| :--- | :--- |
| Loading submit | Botão "Concluir" com `loading=true`, formulário `disabled` |
| Validação erro | Campos com `variant="error"`, mensagem inline, `aria-describedby` |
| Linha material/terceiro | `Input` quantidade + `Select` fornecedor + `Input` valor unitário + `Button` remover (destructive, iconOnly) |
| Adicionar linha | `Button` ghost "Adicionar material/serviço" no final da lista |
| Total calculado | `Card` fixo no bottom (mobile) / sidebar (desktop) com soma em tempo real — `text-2xl`, `font-weight: 700`, `color: var(--color-text-primary)` |

* **Tokens consumidos:** `--color-accent`, `--color-accent-hover`, `--color-danger`, `--color-border`, `--color-text-primary`, `--color-text-secondary`, `--radius-md`, `--radius-sm`, `--space-3`, `--space-4`, `--space-6`, `--text-sm`, `--text-base`, `--text-lg`, `--text-2xl`, `--font-sans`, `--shadow-md`, `--duration-fast`
* **Composição/Slots:** `MaterialRow`, `ThirdPartyRow` componentes internos; `TotalCard` fixo
* **Exemplo de uso:**
```tsx
// Flow 5: Modal de conclusão com OrderCostForm
<Modal
  open={showCompleteModal}
  onClose={closeCompleteModal}
  variant="form"
  size="lg"
  title="Concluir Ordem #1234"
  footer={
    <>
      <Button variant="ghost" onClick={closeCompleteModal} disabled={isSubmitting}>
        Cancelar
      </Button>
      <Button variant="primary" loading={isSubmitting} onClick={handleSubmit}>
        Concluir e Registrar Custos
      </Button>
    </>
  }
>
  <OrderCostForm
    initialData={order.costData}
    onSubmit={submitCostData}
    loading={isSubmitting}
  />
</Modal>
```

---

## 5. Componentes em Depreciação

> **Não aplicável** — Nenhum componente existe no codebase atual para ser depreciado.

---

## 6. Gaps Conhecidos (O que Precisa Ser Construído)

| Gap | Descrição | Prioridade | Dependências |
| :--- | :--- | :--- | :--- |
| **Migração para React + TypeScript** | Codebase atual é vanilla JS (15 arquivos). Necessário setup: Vite, React 18, TS, ESLint, Prettier, Vitest, Storybook | **Crítica** | Decisão de arquitetura (SPA vs MPA), aprovação tech lead |
| **Design Tokens implementados** | `design-tokens.md` §9 checklist — `tokens.css` + substituição de valores hardcoded | **Crítica** | Design Lead sign-off nas cores/tipografia/spacing |
| **Sistema de roteamento** | React Router v6 ou TanStack Router — rotas para: `/login`, `/dashboard`, `/ordens`, `/ordens/:id`, `/ativos`, `/cadastros/:entidade`, `/configuracoes` | **Alta** | Definição de rotas + guards de permissão |
| **State management** | TanStack Query (server state) + Zustand/Jotai (client state: UI, auth, filtros) | **Alta** | Contratos de API (OpenAPI) do backend |
| **Camada de API tipada** | Gerar types + hooks a partir de OpenAPI (Orval/Hey API) — substituir `api.js` atual | **Alta** | Backend entrega spec OpenAPI |
| **Autenticação/Autorização** | `authInterceptor` já existe em `api.js` — precisa: login page, token storage, refresh, RBAC no frontend (permissões por persona) | **Alta** | Backend: endpoints auth, JWT structure, roles/permissions |
| **Componentes base (ui/)** | 26 componentes listados na §3 — priorizar: Button, Input, Select, Modal, DataTable, Badge, Toast, Tooltip, DropdownMenu, Skeleton, EmptyState | **Alta** | Tokens implementados, Storybook configurado |
| **Componentes de domínio (domain/)** | 8 componentes listados na §3 — OrderWizard, OrderTimeline, OrderCostForm, AssetCostTable, EntityCrudTable, SearchInput, FilterBar | **Alta** | Componentes base prontos, contratos de API definidos |
| **Acessibilidade (WCAG AA)** | Focus management, ARIA, contraste, `prefers-reduced-motion`, testes com screen reader | **Alta** | Tokens (cores), componentes base |
| **Testes** | Unit (Vitest + Testing Library), Integration (MSW), E2E (Playwright), Visual (Storybook + Chromatic) | **Média** | Componentes implementados |
| **Internacionalização (i18n)** | pt-BR inicial, estrutura para en/es — `react-i18next` ou `lingui` | **Baixa** | Requisito de produto |
| **PWA / Offline** | Service Worker (Workbox) para técnicos em campo — cache de ordens, sync posterior | **Baixa** | Requisito mobile/field |

---

## 7. Referências

* **Design Tokens:** `design-tokens.md` — fonte única de verdade para cores, tipografia, espaçamento, sombras, breakpoints, motion, z-index (ainda não implementados)
* **User Flows:** `user-flows.md` — 8 fluxos detalhados com estados de UI, métricas, edge cases (todos inferidos do BRD)
* **Business Requirements Document:** `docs/brd.md` — regras BR-01 a BR-06, personas, glossário, guardrails (latência P95 < 800ms, taxa erro < 1%)
* **Diagnóstico Determinístico:** `server/analyze-pipeline.ts` — confirma: vanilla JS, `@popperjs/core` apenas, 3 `console.*` residuais, complexidade ciclomática 13 em `api.js:request`
* **Stack Tecnológica Verificada:** `package.json` — sem framework UI, sem CSS, sem roteamento, sem testes
* **Contrato de API (OpenAPI):** `[NÃO ENCONTRADO NO REPOSITÓRIO — DEPENDÊNCIA EXTERNA: TIME DE BACKEND]`
* **Wireframes/Protótipos Figma:** `[LINK NÃO DISPONÍVEL NO REPOSITÓRIO — REQUER ENTRADA HUMANA]`

---

## 8. Revision History

| Versão | Data | Autor | Mudanças |
| :--- | :--- | :--- | :--- |
| 0.1 | 15/01/2025 | Pipeline (gerado) | Criação inicial baseada em diagnóstico real — **zero componentes no codebase**; inventário derivado de `user-flows.md` + `design-tokens.md`; todos os itens marcados `Planned` |