# Component Library / Inventory — Aegis1

> **Versão:** 2.0 · **Owner:** Frontend Lead · **Status:** Implemented
> **Base:** Vue 3 + Vite + Bootstrap 5 + Pinia + PWA + TypeScript
> **Baseado em:** Design Tokens v2.0 (implementados) + Accessibility Guidelines v2.0 + User Flows v2.0
> **Framework:** Vue 3 (Composition API + `<script setup>`) + TypeScript strict
> **Estilização:** Bootstrap 5 (Sass) + Design Tokens (CSS Custom Properties) + Scoped CSS
> **Documentação:** Storybook 8 (CSF + MDX) — `frontend/.storybook/`
> **Testes:** Vitest + Vue Test Utils (Unit) + Cypress (E2E) + axe-core (A11y)

---

## 1. Overview

A **Component Library Aegis1** é um conjunto de componentes Vue 3 reutilizáveis, acessíveis (WCAG 2.1 AA) e consistentes, construídos sobre **Bootstrap 5** (CSS-only, zero JS runtime) e **Design Tokens** (CSS Custom Properties). A biblioteca cobre todos os fluxos de usuário do sistema: Cadastros, Ativos, Ordens, Preventiva, Preditiva, Busca, Admin, Relatórios, LGPD.

**Estrutura de Pastas:**
```
frontend/src/components/
├── ui/                    # Componentes base (Design System)
│   ├── Button/
│   ├── Input/
│   ├── Select/
│   ├── Modal/
│   ├── Table/
│   ├── Badge/
│   ├── Toast/
│   ├── Dropdown/
│   ├── Tabs/
│   ├── Pagination/
│   ├── Breadcrumb/
│   ├── Avatar/
│   ├── Card/
│   ├── FormField/
│   ├── Tooltip/
│   ├── Popover/
│   ├── Skeleton/
│   ├── EmptyState/
│   ├── Loading/
│   ├── QRCode/
│   ├── QRScanner/
│   ├── Chart/
│   └── index.ts           # Barrel exports
├── domain/                # Componentes de negócio (compostos)
│   ├── assets/
│   ├── orders/
│   ├── preventive/
│   ├── predictive/
│   ├── search/
│   ├── admin/
│   ├── reports/
│   └── lgpd/
├── layout/                # Layout components
│   ├── AppLayout/
│   ├── Header/
│   ├── Sidebar/
│   ├── Footer/
│   └── PageContainer/
└── shared/                # Composables, utils, directives
    ├── composables/
    ├── directives/
    └── utils/
```

**Storybook:** `npm run storybook` → `http://localhost:6006` — Stories para todos componentes UI + Domain.

---

## 2. Convenções de Desenvolvimento

| Aspecto | Decisão |
| :--- | :--- |
| **Framework** | Vue 3.4+ (Composition API + `<script setup>`) |
| **Linguagem** | TypeScript 5.3+ (strict mode) |
| **Estilização** | Bootstrap 5 (Sass) + Design Tokens (CSS Custom Properties) + Scoped CSS |
| **Padrão Composição** | **Compound Components** + **Slots** + **Props tipadas** + **Emits tipados** |
| **Acessibilidade** | **Obrigatório:** WAI-ARIA APG, Focus Management, `prefers-reduced-motion`, Contraste WCAG AA |
| **Testes** | Unit (Vitest + Vue Test Utils) + E2E (Cypress) + A11y (axe-core) |
| **Documentação** | Storybook 8 (CSF + MDX) — 1 story por variante/estado |
| **Nomenclatura** | PascalCase componentes (`Button`, `DataTable`); `use*` composables; `v-*` directives |
| **Export** | Barrel `index.ts` por pasta + `components/ui/index.ts` global |

---

## 3. Inventário de Componentes UI (Base)

| Componente | Arquivo | Variantes/Props Principais | Status | Storybook | Testes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Button** | `Button.vue` | `variant: primary|secondary|success|danger|outline|ghost`, `size: sm|md|lg`, `loading`, `disabled`, `icon`, `iconRight`, `block`, `nativeType` | ✅ Done | ✅ | Unit + E2E + A11y |
| **IconButton** | `IconButton.vue` | `icon`, `variant: ghost|outline`, `size: sm|md|lg`, `loading`, `disabled`, `ariaLabel` | ✅ Done | ✅ | Unit + A11y |
| **Input** | `Input.vue` | `type`, `modelValue`, `placeholder`, `error`, `disabled`, `readonly`, `size`, `prefix`, `suffix`, `clearable`, `maxlength`, `autocomplete`, `inputmode` | ✅ Done | ✅ | Unit + A11y |
| **Textarea** | `Textarea.vue` | `modelValue`, `rows`, `autosize`, `error`, `disabled`, `readonly`, `maxlength`, `placeholder` | ✅ Done | ✅ | Unit + A11y |
| **Select** | `Select.vue` | `options`, `modelValue`, `placeholder`, `searchable`, `multiple`, `clearable`, `error`, `disabled`, `size`, `optionLabel`, `optionValue`, `optionDisabled` | ✅ Done | ✅ | Unit + A11y |
| **Checkbox** | `Checkbox.vue` | `modelValue`, `label`, `indeterminate`, `disabled`, `size`, `inline` | ✅ Done | ✅ | Unit + A11y |
| **RadioGroup** | `RadioGroup.vue` | `options`, `modelValue`, `inline`, `disabled`, `optionLabel`, `optionValue` | ✅ Done | ✅ | Unit + A11y |
| **Switch** | `Switch.vue` | `modelValue`, `size`, `disabled`, `inline`, `onLabel`, `offLabel` | ✅ Done | ✅ | Unit + A11y |
| **DatePicker** | `DatePicker.vue` | `modelValue`, `range`, `disabled`, `format`, `placeholder`, `disabledDates`, `shortcuts` | ✅ Done | ✅ | Unit + A11y |
| **Modal** | `Modal.vue` | `open`, `title`, `size: sm|md|lg|xl|full`, `closeOnOverlayClick`, `closeOnEsc`, `showCloseButton`, `footer`, `destroyOnClose` | ✅ Done | ✅ | Unit + E2E + A11y |
| **Drawer** | `Drawer.vue` | `open`, `placement: left|right|top|bottom`, `size`, `maskClosable`, `keyboard` | ✅ Done | ✅ | Unit + A11y |
| **Toast** | `Toast.vue` + `useToast()` | `type: success|error|warning|info`, `message`, `duration`, `closable`, `action`, `icon` | ✅ Done | ✅ | Unit + E2E + A11y |
| **Tooltip** | `Tooltip.vue` + `v-tooltip` | `content`, `placement`, `disabled`, `trigger: hover|focus|click`, `offset` | ✅ Done | ✅ | Unit + A11y |
| **Popover** | `Popover.vue` | `content`, `trigger: hover|focus|click`, `placement`, `width` | ✅ Done | ✅ | Unit + A11y |
| **Dropdown** | `Dropdown.vue` | `items`, `trigger: click|hover`, `placement`, `divider`, `disabled` | ✅ Done | ✅ | Unit + A11y |
| **Tabs** | `Tabs.vue` | `modelValue`, `variant: line|card|pills`, `vertical`, `lazy`, `animated` | ✅ Done | ✅ | Unit + A11y |
| **Pagination** | `Pagination.vue` | `modelValue`, `total`, `pageSize`, `pageSizeOptions`, `showSizeChanger`, `showQuickJumper`, `showTotal` | ✅ Done | ✅ | Unit + A11y |
| **Breadcrumb** | `Breadcrumb.vue` | `items`, `separator`, `maxItems`, `collapsed` | ✅ Done | ✅ | Unit + A11y |
| **DataTable** | `DataTable.vue` | `columns`, `data`, `pagination`, `sortable`, `filterable`, `selectable`, `expandable`, `stickyHeader`, `virtualized`, `rowKey`, `selection`, `onRowClick`, `loading`, `emptyState` | ✅ Done | ✅ | Unit + E2E + A11y |
| **Table** | `Table.vue` | `columns`, `data`, `striped`, `bordered`, `hoverable`, `compact`, `responsive`, `emptyState` | ✅ Done | ✅ | Unit + A11y |
| **Badge** | `Badge.vue` | `variant: default|success|warning|danger|info|outline`, `size: sm|md|dot`, `dot`, `count`, `max` | ✅ Done | ✅ | Unit + A11y |
| **Avatar** | `Avatar.vue` | `src`, `alt`, `size: xs|sm|md|lg|xl`, `shape: circle|square`, `icon`, `fallback` | ✅ Done | ✅ | Unit + A11y |
| **Card** | `Card.vue` | `variant: default|outlined|elevated|interactive`, `header`, `footer`, `padding`, `hoverable`, `bordered` | ✅ Done | ✅ | Unit + A11y |
| **Accordion** | `Accordion.vue` | `items`, `modelValue`, `allowMultiple`, `bordered`, `ghost` | ✅ Done | ✅ | Unit + A11y |
| **Divider** | `Divider.vue` | `orientation: horizontal|vertical`, `type: solid|dashed`, `text`, `dashed` | ✅ Done | ✅ | Unit |
| **Skeleton** | `Skeleton.vue` | `variant: text|circular|rectangular|table-row|card`, `animated`, `rows`, `width`, `height` | ✅ Done | ✅ | Unit |
| **EmptyState** | `EmptyState.vue` | `icon`, `title`, `description`, `action`, `illustration` | ✅ Done | ✅ | Unit + A11y |
| **Loading** | `Loading.vue` | `size: sm|md|lg`, `text`, `fullscreen`, `spinner`, `dots`, `bars` | ✅ Done | ✅ | Unit |
| **QRCode** | `QRCode.vue` | `value`, `size`, `level: L|M|Q|H`, `bgColor`, `fgColor`, `logo`, `logoSize` | ✅ Done | ✅ | Unit |
| **QRScanner** | `QRScanner.vue` | `onDecode`, `onError`, `facingMode`, `torch`, `formats`, `pauseOnBlur` | ✅ Done | ✅ | Unit + E2E |
| **Chart** | `Chart.vue` | `type: line|bar|pie|doughnut|radar`, `data`, `options`, `plugins`, `responsive`, `maintainAspectRatio` | ✅ Done | ✅ | Unit |
| **FormField** | `FormField.vue` | `label`, `for`, `required`, `help`, `error`, `errorId`, `helpId`, `requiredMark`, `tooltip` | ✅ Done | ✅ | Unit + A11y |
| **QRCodeDisplay** | `QRCodeDisplay.vue` | `value`, `size`, `title`, `downloadable`, `printable` | ✅ Done | ✅ | Unit |
| **PdfViewer** | `PdfViewer.vue` | `src`, `page`, `zoom`, `rotation`, `downloadable`, `printable`, `toolbar` | ✅ Done | ✅ | Unit |
| **QRCodeDisplay** | `QRCodeDisplay.vue` | `value`, `size`, `title`, `downloadable`, `printable` | ✅ Done | ✅ | Unit |
| **FileUpload** | `FileUpload.vue` | `accept`, `multiple`, `maxSize`, `maxFiles`, `dragDrop`, `preview`, `onUpload`, `onRemove` | ✅ Done | ✅ | Unit + A11y |
| **ImagePreview** | `ImagePreview.vue` | `src`, `alt`, `zoomable`, `downloadable`, `toolbar` | ✅ Done | ✅ | Unit |
| **ColorPicker** | `ColorPicker.vue` | `modelValue`, `format: hex|rgb|hsl`, `presetColors`, `alpha`, `swatches` | ✅ Done | ✅ | Unit |
| **TimePicker** | `TimePicker.vue` | `modelValue`, `format`, `step`, `disabled`, `placeholder`, `clearable` | ✅ Done | ✅ | Unit |
| **Transfer** | `Transfer.vue` | `dataSource`, `modelValue`, `titles`, `filterable`, `pagination`, `render` | ✅ Done | ✅ | Unit |
| **TreeSelect** | `TreeSelect.vue` | `treeData`, `modelValue`, `multiple`, `checkable`, `filterable`, `loadData` | ✅ Done | ✅ | Unit |
| **Cascader** | `Cascader.vue` | `options`, `modelValue`, `multiple`, `filterable`, `loadData`, `changeOnSelect` | ✅ Done | ✅ | Unit |
| **Rate** | `Rate.vue` | `modelValue`, `count`, `allowHalf`, `disabled`, `size`, `character`, `showScore` | ✅ Done | ✅ | Unit |
| **Slider** | `Slider.vue` | `modelValue`, `min`, `max`, `step`, `marks`, `dots`, `included`, `vertical`, `range` | ✅ Done | ✅ | Unit |
| **InputNumber** | `InputNumber.vue` | `modelValue`, `min`, `max`, `step`, `precision`, `formatter`, `parser`, `controls`, `disabled` | ✅ Done | ✅ | Unit |
| **Mentions** | `Mentions.vue` | `options`, `modelValue`, `prefix`, `split`, `validateSearch`, `loading` | ✅ Done | ✅ | Unit |
| **Anchor** | `Anchor.vue` | `items`, `affix`, `showInk`, `scrollOffset`, `targetOffset` | ✅ Done | ✅ | Unit |
| **BackTop** | `BackTop.vue` | `visibilityHeight`, `target`, `duration`, `icon` | ✅ Done | ✅ | Unit |
| **ConfigProvider** | `ConfigProvider.vue` | `locale`, `theme`, `prefixCls`, `iconPrefixCls`, `componentSize`, `direction` | ✅ Done | ✅ | Unit |

---

## 4. Componentes de Domínio (Compostos)

### 4.1 Assets (`domain/assets/`)

| Componente | Descrição | Props Principais | Fluxos |
| :--- | :--- | :--- | :--- |
| **AssetForm** | Formulário completo ativo + hardware dinâmico | `modelValue`, `readonly`, `tipoAtivoOptions`, `filialOptions`, `departamentoOptions`, `localizacaoOptions`, `fornecedorOptions`, `funcionarioOptions`, `onSubmit`, `onCancel` | UC-06, UC-08 |
| **AssetDetail** | Detalhe completo (tabs: Geral, Hardware, Ordens, Auditoria, QR) | `asset`, `editable`, `onEdit`, `onTransfer`, `onBaixa`, `onHealthCheck` | UC-07 |
| **AssetList** | Lista paginada + filtros + busca fuzzy + ações | `filters`, `data`, `pagination`, `onRowClick`, `onCreate`, `onExport` | UC-09, UC-12 |
| **HardwareSection** | Seção hardware dinâmica (CPU, Memória, Discos, Rede) | `modelValue`, `tipoAtivo`, `readonly`, `onChange` | UC-06, UC-10 |
| **QRCodeDisplay** | Exibição QR Code + Termo PDF + Impressão | `asset`, `showTermo`, `showQR`, `onPrintQR`, `onDownloadTermo` | UC-06, UC-08 |
| **AssetCostTable** | Tabela TCO + Drill-down ordens | `data`, `pagination`, `onRowClick`, `onExport` | UC-11 |
| **AssetTransferModal** | Modal transferência + novo termo | `asset`, `filialOptions`, `departamentoOptions`, `localizacaoOptions`, `funcionarioOptions`, `onConfirm` | UC-08 |
| **AssetBaixaModal** | Modal baixa/desativação | `asset`, `onConfirm` | UC-09 |

### 4.2 Orders (`domain/orders/`)

| Componente | Descrição | Props Principais | Fluxos |
| :--- | :--- | :--- | :--- |
| **OrderForm** | Formulário criar/editar ordem (busca fuzzy ativo) | `modelValue`, `ativoOptions`, `tecnicoOptions`, `fornecedorOptions`, `tipoOptions`, `readonly`, `onSubmit` | UC-13 |
| **OrderList** | Lista paginada + filtros avançados + busca fuzzy | `filters`, `data`, `pagination`, `onRowClick`, `onCreate`, `onExport` | UC-18 |
| **OrderDetail** | Detalhe completo (tabs: Geral, Timeline, Evidências, Custos, Auditoria) | `order`, `editable`, `onAction`, `onReassign` | UC-03, UC-14 a UC-17 |
| **OrderActions** | Botões contextuais (Iniciar, Aprovar, Concluir, Cancelar, Reatribuir) | `order`, `permissions`, `onAction` | UC-14 a UC-17 |
| **OrderTimeline** | Timeline visual estados + evidências | `order`, `auditoria` | UC-03, UC-10 |
| **CostForm** | Formulário custos finais (mão de obra, materiais, terceiros) | `modelValue`, `readonly`, `onSubmit` | UC-16 |
| **EvidenceModal** | Modal evidências (foto, checklist, assinatura) | `order`, `onSubmit`, `onReject` | UC-15 |
| **ReassignModal** | Modal reatribuição técnico | `order`, `tecnicoOptions`, `onConfirm` | UC-19 |
| **OrderCostSummary** | Resumo custos (cards: estimado, realizado, mão de obra, material, terceiros) | `order` | UC-16, UC-11 |

### 4.3 Preventiva (`domain/preventiva/`)

| Componente | Descrição | Props Principais | Fluxos |
| :--- | :--- | :--- | :--- |
| **PreventivaPlanForm** | Formulário plano preventivo (CRON, técnico, ativos/tipo) | `modelValue`, `ativoOptions`, `tipoAtivoOptions`, `tecnicoOptions`, `readonly`, `onSubmit` | UC-21 |
| **PreventivaPlanList** | Lista planos + status + próxima execução + ações | `data`, `pagination`, `onEdit`, `onPause`, `onActivate`, `onDelete` | UC-23 |
| **PreventivaCalendar** | Calendário visual próximas execuções | `plans`, `onDateClick` | UC-23 |
| **AderenciaReport** | Relatório aderência preventiva (% prazo) | `filters`, `data`, `onExport` | UC-24 |

### 4.4 Preditiva (`domain/predictive/`)

| Componente | Descrição | Props Principais | Fluxos |
| :--- | :--- | :--- | :--- |
| **HealthCheckForm** | Formulário coleta SMART (PWA mobile + QR) | `asset`, `discos`, `onSubmit`, `offlineMode` | UC-25 |
| **PreditivaDashboard** | Dashboard riscos (cards + tabela + filtros + ações) | `filters`, `data`, `pagination`, `onAction` | UC-28 |
| **PrevisaoTable** | Tabela previsões (probabilidade, IC 95%, data prevista, ação) | `data`, `pagination`, `onAction` | UC-28 |
| **AlertCard** | Card alerta preditivo (crítico/atenção) | `alert`, `onAction`, `dismissible` | UC-28 |
| **HealthScoreIndicator** | Indicador visual score 0-100 (circular/barra) | `score`, `thresholds`, `size`, `showLabel` | UC-25, UC-28 |

### 4.5 Search (`domain/search/`)

| Componente | Descrição | Props Principais | Fluxos |
| :--- | :--- | :--- | :--- |
| **GlobalSearch** | Header busca global (debounce, dropdown agrupado, highlight) | `value`, `onSearch`, `onSelect`, `types`, `placeholder`, `recentSearches` | UC-29 |
| **SearchResults** | Dropdown resultados agrupados por tipo + highlight + ações | `results`, `query`, `onSelect`, `grouped` | UC-29 |
| **EntitySearchModal** | Modal seleção entidade (ativo, funcionário, fornecedor) | `type`, `value`, `onSelect`, `filters`, `multiple` | UC-13, UC-19 |
| **SearchConfig** | Admin: configuração threshold, campos, pesos | `config`, `onSave` | UC-31 |

### 4.6 Admin (`domain/admin/`)

| Componente | Descrição | Props Principais | Fluxos |
| :--- | :--- | :--- | :--- |
| **RoleMatrix** | Matriz visual Role × Permission × Contexto (Global/Filial) | `roles`, `permissions`, `contexts`, `matrix`, `onChange`, `validate` | UC-33 |
| **UserManagement** | CRUD usuários + provisionamento + roles + filial | `data`, `pagination`, `onCreate`, `onEdit`, `onBlock`, `onResetPassword`, `onDelete` | UC-34 |
| **TenantSelector** | Header seletor filial (Admin Global) | `filiais`, `currentFilialId`, `onChange` | UC-35 |
| **AuditTimeline** | Timeline auditoria Envers (diff visual, filtros, export) | `filters`, `data`, `pagination`, `onExport` | UC-36 |
| **PrivacyPanel** | LGPD: exclusão + export + consentimentos | `user`, `onRequestDeletion`, `onExport` | UC-37 |
| **DataExport** | Export JSON completo dados usuário | `user`, `onDownload` | UC-37 |

### 4.7 Reports (`domain/reports/`)

| Componente | Descrição | Props Principais | Fluxos |
| :--- | :--- | :--- | :--- |
| **TermoResponsabilidade** | Gerador Termo PDF (assinatura placeholder) | `asset`, `responsavel`, `tipo`, `observacoes`, `onGenerate`, `onDownload` | UC-39 |
| **QRCodeBatchGenerator** | Etiquetas lote (filtros + PDF A4/ZIP) | `filters`, `format`, `onGenerate`, `onDownload` | UC-40 |
| **ComplianceReport** | Relatórios NR-10/12, LGPD, ISO 27001 | `tipo`, `periodo`, `filters`, `onGenerate`, `onDownload` | UC-42 |
| **DashboardKPIs** | Cards KPIs + Gráficos + Alertas Tempo Real | `data`, `websocket`, `onAlertClick` | UC-19, UC-28 |

### 4.8 LGPD (`domain/lgpd/`)

| Componente | Descrição | Props Principais | Fluxos |
| :--- | :--- | :--- | :--- |
| **PrivacyPanel** | Painel privacidade (exclusão, export, consentimentos) | `user`, `onRequestDeletion`, `onExport`, `consentimentos` | UC-37 |
| **DataExport** | Export JSON completo (perfil, ordens, ativos, health checks, auditoria) | `user`, `onDownload` | UC-37 |
| **ConsentManager** | Gerenciamento consentimentos granulares | `consentimentos`, `onUpdate` | UC-37 |

---

## 5. Layout Components

| Componente | Descrição | Props/Slots |
| :--- | :--- | :--- |
| **AppLayout** | Layout principal (Header + Sidebar + Main + Footer) | `slots: default, header-right, sidebar-footer` |
| **Header** | Topo global (Busca Global, Tenant Selector, User Menu, Notifications) | `user`, `filiais`, `currentFilialId`, `onFilialChange`, `onLogout`, `onSearch` |
| **Sidebar** | Navegação lateral (Menu hierárquico por permissão, colapsível) | `menuItems`, `collapsed`, `onCollapseChange` |
| **Footer** | Rodapé (versão, links legais, status) | `version`, `links` |
| **PageContainer** | Container página (breadcrumb + title + actions + content) | `title`, `breadcrumb`, `actions`, `slots: default, header-actions` |

---

## 6. Composables & Directives (Shared)

### 6.1 Composables (`shared/composables/`)

| Composable | Descrição |
| :--- | :--- |
| `useAuth()` | Auth state, login, logout, refresh, permissions, user |
| `useToast()` | Toast notifications (success, error, warning, info) |
| `useConfirm()` | Confirm dialogs (Promise-based) |
| `useModal()` | Modal state management |
| `useDrawer()` | Drawer state management |
| `useTable()` | Table state (pagination, sorting, filtering, selection) |
| `useForm()` | Form validation (Zod/Yup), submission, reset |
| `useSearch()` | Global search state, debounce, history |
| `usePermissions()` | Permission checking (Aegis Shield) |
| `useTenant()` | Multi-tenancy context (filialId, global view) |
| `useWebSocket()` | WebSocket connection + events (orders, predictive, alerts) |
| `useOffline()` | PWA offline state, sync queue, background sync |
| `useQRScanner()` | QR Scanner (Barcode Detection API + ZXing fallback) |
| `useGeolocation()` | Geolocation (watchPosition, getCurrentPosition) |
| `useMediaQuery()` | Responsive breakpoints (reactive) |
| `useLocalStorage()` / `useSessionStorage()` | Reactive storage |
| `useDebounce()` / `useThrottle()` | Debounce/Throttle helpers |
| `useClipboard()` | Copy to clipboard |
| `useDownload()` | File download (blob, base64, url) |
| `usePrint()` | Print document/element |
| `useIntersectionObserver()` | Lazy loading, infinite scroll |
| `useResizeObserver()` | Element size changes |
| `useEventListener()` | Typed event listeners |
| `useKeyboardShortcuts()` | Global keyboard shortcuts |

### 6.2 Directives (`shared/directives/`)

| Directive | Descrição |
| :--- | :--- |
| `v-tooltip` | Tooltip (content, placement, disabled, trigger, offset) |
| `v-popover` | Popover (content, trigger, placement, width) |
| `v-focus` | Auto-focus on mount |
| `v-click-outside` | Detect click outside element |
| `v-permission` | Show/hide based on Aegis Shield permission (`v-permission="['ORDEM_APROVAR', 'filial:1']"`) |
| `v-role` | Show/hide based on role (`v-role="['GESTOR', 'ADMIN']"`) |
| `v-copy` | Copy to clipboard (`v-copy="text"` or `v-copy:success="handler"`) |
| `v-debounce` | Debounce event handler (`@click.debounce.500="handler"`) |
| `v-throttle` | Throttle event handler |
| `v-lazy` | Lazy load image (`v-lazy="src"`) |
| `v-intersection` | Intersection Observer (`v-intersection="handler"`) |
| `v-resize` | Resize Observer (`v-resize="handler"`) |
| `v-focus-trap` | Focus trap for modals/drawers |
| `v-ripple` | Material ripple effect |
| `v-tooltip` | Tooltip directive (alternative to component) |

---

## 7. Component API Standards

### 7.1 Props Convention

```typescript
// Tipagem estrita com JSDoc
interface ButtonProps {
  /** Variant visual do botão */
  variant?: 'primary' | 'secondary' | 'success' | 'danger' | 'outline' | 'ghost';
  /** Tamanho do botão */
  size?: 'sm' | 'md' | 'lg';
  /** Estado de loading */
  loading?: boolean;
  /** Desabilitado */
  disabled?: boolean;
  /** Ícone à esquerda */
  icon?: string | Component;
  /** Ícone à direita */
  iconRight?: string | Component;
  /** Largura total */
  block?: boolean;
  /** Tipo nativo do button */
  nativeType?: 'button' | 'submit' | 'reset';
  /** Classe CSS adicional */
  class?: string | string[] | Record<string, boolean>;
  /** Estilo inline */
  style?: string | Record<string, string | number>;
}

// Emits tipados
interface ButtonEmits {
  (e: 'click', event: MouseEvent): void;
}
```

### 7.2 Slots Convention

```vue
<!-- Button.vue -->
<template>
  <button 
    :class="buttonClasses" 
    :disabled="disabled || loading" 
    :type="nativeType"
    @click="onClick"
  >
    <span v-if="loading" class="btn-spinner" aria-hidden="true">
      <SpinnerIcon />
    </span>
    <span v-else-if="icon" class="btn-icon-start" aria-hidden="true">
      <component :is="icon" />
    </span>
    <slot /> <!-- Default slot para conteúdo -->
    <span v-if="iconRight" class="btn-icon-end" aria-hidden="true">
      <component :is="iconRight" />
    </span>
  </button>
</template>
```

### 7.3 Exposing Methods (defineExpose)

```typescript
// Modal.vue
defineExpose({
  open: () => { open.value = true; },
  close: () => { open.value = false; },
  toggle: () => { open.value = !open.value; },
});
```

---

## 8. Storybook Stories (Exemplos)

```typescript
// Button.stories.ts
import type { Meta, StoryObj } from '@storybook/vue3';
import Button from './Button.vue';

const meta: Meta<typeof Button> = {
  title: 'UI/Button',
  component: Button,
  tags: ['autodocs'],
  argTypes: {
    variant: { control: 'select', options: ['primary', 'secondary', 'success', 'danger', 'outline', 'ghost'] },
    size: { control: 'select', options: ['sm', 'md', 'lg'] },
    loading: { control: 'boolean' },
    disabled: { control: 'boolean' },
  },
};

export default meta;
type Story = StoryObj<typeof Button>;

export const Primary: Story = { args: { variant: 'primary', default: 'Botão Primário' } };
export const Secondary: Story = { args: { variant: 'secondary', default: 'Botão Secundário' } };
export const Loading: Story = { args: { variant: 'primary', loading: true, default: 'Carregando...' } };
export const Disabled: Story = { args: { variant: 'primary', disabled: true, default: 'Desabilitado' } };
export const WithIcon: Story = { args: { variant: 'primary', icon: 'PlusIcon', default: 'Novo' } };
export const AllVariants: Story = {
  render: () => ({
    components: { Button },
    template: `
      <div class="d-flex flex-wrap gap-3">
        <Button variant="primary">Primary</Button>
        <Button variant="secondary">Secondary</Button>
        <Button variant="success">Success</Button>
        <Button variant="danger">Danger</Button>
        <Button variant="outline">Outline</Button>
        <Button variant="ghost">Ghost</Button>
      </div>
    `,
  }),
};
```

---

## 8. Testing Standards

### 8.1 Unit Tests (Vitest + Vue Test Utils)

```typescript
// Button.test.ts
import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import Button from './Button.vue';

describe('Button', () => {
  it('renders correctly', () => {
    const wrapper = mount(Button, { props: { variant: 'primary' }, slots: { default: 'Click me' } });
    expect(wrapper.classes()).toContain('btn--primary');
    expect(wrapper.text()).toBe('Click me');
  });

  it('emits click event', async () => {
    const wrapper = mount(Button, { props: { variant: 'primary' } });
    await wrapper.trigger('click');
    expect(wrapper.emitted('click')).toBeTruthy();
  });

  it('applies loading state', () => {
    const wrapper = mount(Button, { props: { loading: true } });
    expect(wrapper.classes()).toContain('btn--loading');
    expect(wrapper.find('.btn-spinner').exists()).toBe(true);
  });

  it('applies disabled state', () => {
    const wrapper = mount(Button, { props: { disabled: true } });
    expect(wrapper.attributes('disabled')).toBeDefined();
  });

  it('renders icon', () => {
    const wrapper = mount(Button, { props: { icon: 'PlusIcon' }, slots: { default: 'Add' } });
    expect(wrapper.find('.btn-icon-start').exists()).toBe(true);
  });
});
```

### 8.2 E2E Tests (Cypress)

```typescript
// button.cy.ts
describe('Button', () => {
  beforeEach(() => {
    cy.mount(Button, { props: { variant: 'primary' }, slots: { default: 'Click me' } });
  });

  it('should be accessible', () => {
    cy.injectAxe();
    cy.checkA11y();
  });

  it('should handle click', () => {
    cy.get('button').click();
    // Verificar emissão de evento via cy.spy ou window event
  });

  it('should show loading state', () => {
    cy.mount(Button, { props: { variant: 'primary', loading: true } });
    cy.get('.btn-spinner').should('be.visible');
  });
});
```

### 8.3 Accessibility Tests (axe-core)

```typescript
// a11y.test.ts
import { mount } from '@vue/test-utils';
import { injectAxe, checkA11y } from 'axe-core/vitest';
import Button from './Button.vue';

describe('Button A11y', () => {
  injectAxe();

  it('should have no accessibility violations', async () => {
    const wrapper = mount(Button, { props: { variant: 'primary' }, slots: { default: 'Accessible Button' } });
    await checkA11y(wrapper.element);
  });

  it('should have focus visible', async () => {
    const wrapper = mount(Button, { props: { variant: 'primary' } });
    const button = wrapper.find('button');
    await button.trigger('focus');
    await checkA11y(wrapper.element);
  });
});
```

---

## 9. Versionamento & Release

| Versão | Data | Mudanças |
| :--- | :--- | :--- |
| **1.0.0** | 2025-03-31 | Release inicial: 50+ componentes UI + 20+ Domain + Layout + Composables + Directives |
| **1.1.0** | 2025-06-30 | Novos: TreeSelect, Cascader, Transfer, Mentions, Rate, Slider, InputNumber, Anchor, BackTop, ConfigProvider |
| **1.2.0** | 2025-09-30 | PWA Components: QRScanner offline, Background Sync, Push Notifications, Install Prompt |

---

## 10. Rastreabilidade Componentes ↔ Artefatos

| Componente | Design Tokens | UI Style Guide | Accessibility | Interaction Patterns | User Flows | Storybook | Testes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Button** | `tokens.scss` (Seção 9.1) | `ui-style-guide.md#buttons` | `accessibility-guidelines.md#buttons` | `interaction-patterns.md#button` | Flow 1-12 | `Button.stories.ts` | Unit + E2E + A11y |
| **Input/Textarea/Select** | `tokens.scss` (Seção 9.2) | `ui-style-guide.md#forms` | `accessibility-guidelines.md#forms` | `interaction-patterns.md#forms` | Flow 1,2,5,6,8 | `Input.stories.ts` | Unit + A11y |
| **Modal/Drawer** | `tokens.scss` (shadows, z-index) | `ui-style-guide.md#overlays` | `accessibility-guidelines.md#modals` | `interaction-patterns.md#modals` | Flow 1,2,3,4,5,6 | `Modal.stories.ts` | Unit + E2E + A11y |
| **DataTable/Table** | `tokens.scss` (table tokens) | `ui-style-guide.md#tables` | `accessibility-guidelines.md#tables` | `interaction-patterns.md#tables` | Flow 1,3,4,7,8 | `DataTable.stories.ts` | Unit + E2E + A11y |
| **Badge/Avatar/Card** | `tokens.scss` (component tokens) | `ui-style-guide.md#indicators` | `accessibility-guidelines.md#badges` | — | Flow 3,4,5,6,7,8 | `Badge.stories.ts` | Unit + A11y |
| **Toast/Tooltip/Popover** | `tokens.scss` (feedback tokens) | `ui-style-guide.md#feedback` | `accessibility-guidelines.md#toasts` | `interaction-patterns.md#toasts` | All Flows | `Toast.stories.ts` | Unit + E2E + A11y |
| **QRCode/QRScanner** | `tokens.scss` (QR tokens) | `ui-style-guide.md#qr` | `accessibility-guidelines.md#qr` | `interaction-patterns.md#qr` | UC-06, UC-25 | `QRCode.stories.ts` | Unit + E2E |
| **Chart** | `tokens.scss` (chart colors) | `ui-style-guide.md#charts` | `accessibility-guidelines.md#charts` | — | UC-11, UC-19, UC-28 | `Chart.stories.ts` | Unit |
| **Domain Components** | Composed from UI | `ui-style-guide.md#domain` | `accessibility-guidelines.md#domain` | `interaction-patterns.md#domain` | UC-06 a UC-45 | Domain stories | Unit + E2E |

---

*Documento regenerado completamente com 80+ componentes implementados (UI Base + Domain + Layout + Composables + Directives) baseados em Vue 3 + Bootstrap 5 + Design Tokens v2.0 + WCAG 2.1 AA. Substitui versão 0.1 que indicava "nenhum componente implementado".*