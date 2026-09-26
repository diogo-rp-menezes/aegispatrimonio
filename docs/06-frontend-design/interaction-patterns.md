# Interaction & Motion Patterns — Aegis1

> **Versão:** 2.0 · **Owner:** Frontend Lead · **Status:** Implemented
> **Base:** Component Library v2.0 + Design Tokens v2.0 + Accessibility Guidelines v2.0 + Vue 3 + Bootstrap 5 + PWA
> **Implementação:** CSS Transitions/Animations + Vue Transition/TransitionGroup + VueUse (useMotion, useTransition) + @popperjs/core (positioning)

---

## 1. Overview

Os **Interaction & Motion Patterns** definem o comportamento visual e responsivo de todos os componentes da biblioteca Aegis1. Baseiam-se nos **Design Tokens v2.0** (implementados) e seguem as **Accessibility Guidelines v2.0** (WCAG 2.1 AA).

**Princípios Fundamentais:**
- **Propósito:** Comunicar hierarquia, causalidade, estado — nunca decorativo
- **Performance:** Apenas `transform`/`opacity` (GPU); evitar layout thrashing
- **Acessibilidade:** `prefers-reduced-motion` respeitado globalmente (animações → 0.01ms)
- **Consistência:** Tokens de duração/easing centralizados em `tokens.scss` (Seção 6)
- **GPU-First:** Apenas `transform`, `opacity`, `filter` animados; `will-change` quando apropriado

---

## 2. Motion Tokens (Design Tokens v2.0 - Seção 6)

```scss
:root {
  // Durations
  --duration-instant: 0ms;
  --duration-fast:    100ms;    // Feedback direto (hover, click, focus)
  --duration-normal:  200ms;    // Transições padrão (modais, dropdowns, tabs)
  --duration-slow:    300ms;    // Transições complexas (modais grandes, drawers)
  --duration-slower:  500ms;    // Transições complexas + conteúdo (wizard steps)

  // Easings
  --ease-linear:       linear;
  --ease-in:           cubic-bezier(0.4, 0, 1, 1);
  --ease-out:          cubic-bezier(0, 0, 0.2, 1);      // Padrão saída
  --ease-in-out:       cubic-bezier(0.4, 0, 0.2, 1);    // Padrão entrada/saída
  --ease-spring:       cubic-bezier(0.34, 1.56, 0.64, 1); // Bounce sutil
  --ease-bounce:       cubic-bezier(0.68, -0.55, 0.265, 1.55); // Emphasized

  // Composite Transitions
  --transition-fast:    var(--duration-fast) var(--ease-out);
  --transition-normal:  var(--duration-normal) var(--ease-out);
  --transition-slow:    var(--duration-slow) var(--ease-in-out);
  --transition-spring:  var(--duration-slower) var(--ease-spring);

  // Specific
  --transition-colors:  var(--duration-fast) var(--ease-out);
  --transition-shadow:  var(--duration-normal) var(--ease-out);
  --transition-transform: var(--duration-normal) var(--ease-spring);
  --transition-opacity: var(--duration-fast) var(--ease-out);
  --transition-all:     var(--duration-normal) var(--ease-out);

  // Reduced Motion
  --duration-reduced: 0.01ms;
}
```

### 2.1 Reduced Motion (Global)

```scss
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: var(--duration-reduced) !important;
    animation-iteration-count: 1 !important;
    transition-duration: var(--duration-reduced) !important;
    scroll-behavior: auto !important;
  }
  
  // Disable Vue transitions
  .v-enter-active,
  .v-leave-active,
  .v-move {
    transition-duration: var(--duration-reduced) !important;
  }
}
```

---

## 3. Interaction Patterns por Categoria

### 3.1 Feedback de Ação (Click/Tap/Key)

| Interação | Comportamento Visual | Duração | Easing | Componentes |
| :--- | :--- | :--- | :--- | :--- |
| **Button Click** | `transform: scale(0.98)` + `box-shadow: var(--shadow-sm)` | `--duration-fast` | `--ease-out` | `Button`, `IconButton` |
| **Button Loading** | Spinner centralizado (`--duration-fast` spin) + `opacity: 0.8` | Contínuo | `--ease-linear` | `Button`, `IconButton` |
| **IconButton Hover** | `transform: scale(1.05)` + `background: var(--color-neutral-100)` | `--duration-fast` | `--ease-out` | `IconButton` |
| **Switch Toggle** | Thumb `translateX` + `background-color` transition | `--duration-fast` | `--ease-spring` | `Switch` |
| **Checkbox/Radio** | `transform: scale(0.9)` active + border-color transition | `--duration-fast` | `--ease-out` | `Checkbox`, `RadioGroup` |
| **Input Focus** | `border-color: var(--color-border-focus)` + `box-shadow: var(--shadow-focus)` | `--duration-fast` | `--ease-out` | `Input`, `Textarea`, `Select`, `DatePicker` |
| **Table Row Hover** | `background: var(--color-bg-tertiary)` + `box-shadow: var(--shadow-xs)` | `--duration-fast` | `--ease-out` | `DataTable`, `Table` |
| **Dropdown/Popover Open** | `opacity: 0→1` + `transform: scale(0.95)→1` + `opacity: 0→1` | `--duration-normal` | `--ease-out` | `Dropdown`, `Popover`, `Select`, `DatePicker` |
| **Modal/Drawer Open** | Backdrop `opacity: 0→1` + Container `scale(0.95)→1` + `opacity: 0→1` | `--duration-slow` | `--ease-out` | `Modal`, `Drawer` |
| **Toast Enter** | `translateY(100%)→0` + `opacity: 0→1` | `--duration-normal` | `--ease-spring` | `Toast` |
| **Toast Exit** | `translateY(0)→translateY(-100%)` + `opacity: 1→0` | `--duration-fast` | `--ease-in` | `Toast` |
| **Toast Stack** | `translateY` cascade (gap `--space-2`) | `--duration-fast` | `--ease-out` | `ToastContainer` |
| **Tabs Switch** | Cross-fade `opacity 0→1` + `translateX(±16px)→0` | `--duration-normal` | `--ease-in-out` | `Tabs` |
| **Accordion Expand** | `max-height: 0 → scrollHeight` + `opacity: 0→1` + icon `rotate(180deg)` | `--duration-normal` | `--ease-out` | `Accordion`, `DataTable` (expandable) |
| **Drawer Open** | `translateX(-100%)→0` (left) / `translateX(100%)→0` (right) | `--duration-slow` | `--ease-out` | `Drawer` |
| **Toast Stack Cascade** | Staggered `--duration-fast` (delay `--duration-fast` * index) | `--duration-fast` | `--ease-out` | `ToastContainer` |

### 3.2 Loading & State Transitions

| Transição | Comportamento | Duração | Componentes |
| :--- | :--- | :--- | :--- |
| **Skeleton → Content** | `opacity: 0→1` + `translateY(8px)→0` (staggered `--duration-fast` * index) | `--duration-normal` | `DataTable`, `Card`, `OrderCostForm`, `AssetCostTable` |
| **Loading → Error** | Skeleton fade-out → Inline error fade-in + Toast error | `--duration-normal` | `DataTable`, `Modal`, `OrderCostForm` |
| **Loading → Success** | Skeleton fade-out → Content fade-in + Toast success | `--duration-normal` | `DataTable`, `Modal`, `OrderCostForm` |
| **Button Loading** | Spinner `rotate(360deg)` infinite + `opacity: 0.8` | `--duration-fast` (spin) | `Button`, `IconButton` |
| **Page Transition** | Cross-fade `opacity 0→1` + `translateY(16px)→0` | `--duration-normal` | `RouterView` (Vue Router) |
| **Tab Switch** | Cross-fade `opacity 0→1` + `translateX(±16px)→0` | `--duration-normal` | `Tabs` |
| **Accordion Expand** | `max-height: 0 → scrollHeight` + `opacity: 0→1` + icon `rotate(180deg)` | `--duration-normal` | `Accordion`, `DataTable` (expandable) |
| **Drawer Open** | `translateX(-100%)→0` + backdrop `opacity: 0→1` | `--duration-slow` | `Drawer` |
| **Modal Open** | Backdrop `opacity: 0→1` + Container `scale(0.95)→1` + `opacity: 0→1` | `--duration-slow` | `Modal` |

### 3.3 Navigation Patterns

| Padrão | Comportamento | Duração | Implementação |
| :--- | :--- | :--- | :--- |
| **Route Transition** | Cross-fade `opacity 0→1` + `translateY(16px)→0` | `--duration-normal` | `<Transition name="page" mode="out-in">` |
| **Modal Open** | Backdrop `opacity: 0→1` + Container `scale(0.95)→1` + `opacity: 0→1` | `--duration-slow` | `<Transition name="modal">` + Focus Trap |
| **Drawer Open** | `translateX(-100%)→0` + backdrop `opacity: 0→1` | `--duration-slow` | `<Transition name="drawer">` |
| **Toast Enter** | `translateY(100%)→0` + `opacity: 0→1` | `--duration-normal` | `<TransitionGroup name="toast">` |
| **Toast Exit** | `translateY(0)→translateY(-100%)` + `opacity: 1→0` | `--duration-fast` | `<TransitionGroup name="toast">` |
| **Dropdown/Popover** | `opacity: 0→1` + `transform: scale(0.95)→1` | `--duration-normal` | `@popperjs/core` + `<Transition name="popover">` |
| **Tabs Switch** | Cross-fade `opacity 0→1` + `translateX(±16px)→0` | `--duration-normal` | `<TransitionGroup name="tabs">` |
| **Accordion** | `max-height: 0 → scrollHeight` + `opacity: 0→1` | `--duration-normal` | `<Transition name="accordion">` |
| **Page Load** | Skeleton → Content (staggered) | `--duration-normal` * index | `Skeleton` + `TransitionGroup` |

---

## 4. Component-Specific Patterns

### 4.1 Button & IconButton

```vue
<!-- Button.vue -->
<style scoped>
.btn {
  transition: var(--transition-colors), var(--transition-shadow), var(--transition-transform);
}

.btn:not(:disabled):not(.btn--loading):active {
  transform: scale(0.98);
  box-shadow: var(--shadow-sm);
  transition: var(--transition-transform);
}

.btn--loading .btn-spinner {
  animation: spin var(--duration-fast) var(--ease-linear) infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.btn:focus-visible {
  outline: none;
  box-shadow: var(--shadow-focus);
  transition: var(--transition-shadow);
}
</style>
```

### 4.2 Modal & Drawer (Focus Trap + Animation)

```vue
<!-- Modal.vue -->
<script setup lang="ts">
import { useFocusTrap } from '@vueuse/core';
import { onMounted, watch } from 'vue';

const contentRef = ref<HTMLElement>();
const { activate, deactivate } = useFocusTrap(contentRef, { 
  immediate: true,
  onDeactivate: () => emit('close') 
});

watch(open, (val) => {
  if (val) activate();
  else deactivate();
});
</script>

<template>
  <Transition name="modal" appear>
    <div v-if="open" class="modal-overlay" @click.self="close">
      <Transition name="modal-content">
        <div v-if="open" ref="contentRef" class="modal-content" role="dialog" aria-modal="true" :aria-labelledby="titleId" :aria-describedby="descId">
          <!-- Modal content -->
        </div>
      </Transition>
    </div>
  </Transition>
</template>

<style scoped>
.modal-overlay {
  position: fixed; inset: 0; z-index: var(--z-index-modal-backdrop);
  background: rgba(0, 0, 0, 0.5);
  display: flex; align-items: center; justify-content: center;
  padding: var(--space-4);
}

.modal-overlay-enter-active,
.modal-overlay-leave-active {
  transition: opacity var(--duration-slow) var(--ease-out);
}

.modal-overlay-enter-from,
.modal-overlay-leave-to {
  opacity: 0;
}

.modal-content {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-modal);
  box-shadow: var(--shadow-modal);
  max-width: 90vw; max-height: 90vh;
  overflow: hidden;
  display: flex; flex-direction: column;
}

.modal-content-enter-active,
.modal-content-leave-active {
  transition: transform var(--duration-slow) var(--ease-out), opacity var(--duration-slow) var(--ease-out);
}

.modal-content-enter-from,
.modal-content-leave-to {
  transform: scale(0.95);
  opacity: 0;
}
</style>
```

### 4.3 DataTable (Skeleton + Row Expansion + Sort/Filter)

```vue
<!-- DataTable.vue -->
<template>
  <div class="data-table-container">
    <!-- Toolbar -->
    <div class="data-table-toolbar">
      <GlobalSearch v-model="searchQuery" @search="onSearch" />
      <Button @click="refresh" :loading="loading" icon="RefreshIcon" />
    </div>

    <!-- Table with Skeleton -->
    <TransitionGroup name="table-rows" tag="tbody" class="data-table-body">
      <!-- Skeleton Rows -->
      <tr v-if="loading" v-for="i in 5" :key="`skeleton-${i}`" class="skeleton-row">
        <td v-for="col in columns" :key="col.key"><Skeleton variant="text" :width="col.width" /></td>
      </tr>

      <!-- Data Rows -->
      <tr 
        v-else 
        v-for="row in displayedData" 
        :key="row[rowKey]" 
        :class="['data-row', { 'data-row--selected': isSelected(row), 'data-row--expanded': expandedRows.has(row[rowKey]) }]"
        @click="toggleSelect(row)"
        @dblclick="$emit('row-click', row)"
      >
        <td v-for="col in columns" :key="col.key">
          <slot :name="col.key" :row="row" :value="row[col.key]">{{ row[col.key] }}</slot>
        </td>
      </tr>

      <!-- Expanded Row -->
      <tr v-if="expandedRows.has(row[rowKey])" :key="`expand-${row[rowKey]}`" class="expand-row">
        <td :colspan="columns.length">
          <Transition name="expand">
            <div v-show="expandedRows.has(row[rowKey])" class="expand-content">
              <slot name="expand" :row="row" />
            </div>
          </Transition>
        </td>
      </tr>

      <!-- Empty State -->
      <tr v-if="!loading && displayedData.length === 0">
        <td :colspan="columns.length"><EmptyState :icon="emptyIcon" :title="emptyTitle" :description="emptyDescription" :action="emptyAction" /></td>
      </tr>
    </TransitionGroup>

    <!-- Pagination -->
    <Pagination v-if="!loading && total > pageSize" v-model:modelValue="page" :total="total" :pageSize="pageSize" @update:modelValue="onPageChange" @update:pageSize="onPageSizeChange" />
  </div>
</template>

<style scoped>
.data-table-body {
  transition: opacity var(--duration-normal) var(--ease-out);
}

.skeleton-row .skeleton-cell {
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.table-rows-enter-active,
.table-rows-leave-active {
  transition: all var(--duration-normal) var(--ease-out);
}

.table-rows-move {
  transition: transform var(--duration-normal) var(--ease-spring);
}

.expand-row-enter-active,
.expand-row-leave-active {
  transition: all var(--duration-normal) var(--ease-out);
}

.expand-row-enter-from,
.expand-row-leave-to {
  max-height: 0;
  opacity: 0;
}
</style>
```

### 4.4 Toast Container (Stack + Auto-dismiss + Accessibility)

```vue
<!-- ToastContainer.vue -->
<template>
  <TransitionGroup 
    name="toast" 
    tag="div" 
    class="toast-container" 
    role="region" 
    aria-live="polite" 
    aria-atomic="true"
    aria-label="Notificações"
  >
    <Toast 
      v-for="toast in toasts" 
      :key="toast.id" 
      :toast="toast" 
      @dismiss="removeToast"
      @action="handleAction"
    />
  </TransitionGroup>
</template>

<style scoped>
.toast-container {
  position: fixed;
  bottom: var(--space-6);
  right: var(--space-6);
  z-index: var(--z-index-toast);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  max-width: 400px;
  pointer-events: none;
}

.toast-container > * {
  pointer-events: auto;
}

.toast-enter-active,
.toast-leave-active {
  transition: transform var(--duration-normal) var(--ease-spring), opacity var(--duration-normal) var(--ease-out);
}

.toast-enter-from {
  transform: translateY(100%);
  opacity: 0;
}

.toast-leave-to {
  transform: translateY(-100%);
  opacity: 0;
}

.toast-move {
  transition: transform var(--duration-fast) var(--ease-out);
}

/* Staggered entrance */
.toast-container > *:nth-child(1) { transition-delay: 0ms; }
.toast-container > *:nth-child(2) { transition-delay: 100ms; }
.toast-container > *:nth-child(3) { transition-delay: 200ms; }
.toast-container > *:nth-child(4) { transition-delay: 300ms; }
</style>
```

### 4.5 Tabs (Cross-fade + Keyboard Navigation)

```vue
<!-- Tabs.vue -->
<template>
  <div class="tabs" :class="{ 'tabs--vertical': vertical }">
    <div class="tabs-nav" role="tablist" :aria-orientation="vertical ? 'vertical' : 'horizontal'">
      <button
        v-for="(tab, index) in tabs" 
        :key="tab.key"
        :class="['tab-trigger', { 'tab-trigger--active': modelValue === tab.key, 'tab-trigger--disabled': tab.disabled }]"
        :role="vertical ? 'tab' : 'tab'"
        :aria-selected="modelValue === tab.key"
        :aria-controls="`panel-${tab.key}`"
        :id="`tab-${tab.key}`"
        :tabindex="modelValue === tab.key ? 0 : -1"
        :aria-disabled="tab.disabled"
        @click="() => !tab.disabled && updateModel(tab.key)"
        @keydown="onKeydown"
      >
        <component v-if="tab.icon" :is="tab.icon" class="tab-icon" aria-hidden="true" />
        <span class="tab-label">{{ tab.label }}</span>
        <span v-if="tab.badge" class="tab-badge">{{ tab.badge }}</span>
      </button>
    </div>

    <TransitionGroup name="tabs" tag="div" class="tabs-panels">
      <div 
        v-for="tab in tabs" 
        :key="tab.key" 
        :id="`panel-${tab.key}`" 
        :role="vertical ? 'tabpanel' : 'tabpanel'"
        :aria-labelledby="`tab-${tab.key}`"
        :hidden="modelValue !== tab.key"
        class="tab-panel"
      >
        <slot :name="tab.key" />
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.tabs-nav {
  display: flex;
  gap: var(--space-1);
  border-bottom: 1px solid var(--color-border-light);
  padding-bottom: var(--space-1);
}

.tab-trigger {
  background: transparent;
  border: none;
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--color-text-secondary);
  border-radius: var(--radius-md) var(--radius-md) 0 0;
  cursor: pointer;
  transition: var(--transition-colors);
  white-space: nowrap;
}

.tab-trigger:hover:not(.tab-trigger--disabled) {
  color: var(--color-text-primary);
  background: var(--color-bg-tertiary);
}

.tab-trigger--active {
  color: var(--color-primary);
  border-bottom: 2px solid var(--color-primary);
}

.tab-trigger:focus-visible {
  outline: none;
  box-shadow: var(--shadow-focus);
}

.tabs-panels {
  position: relative;
  min-height: 200px;
}

.tabs-panels-enter-active,
.tabs-panels-leave-active {
  transition: opacity var(--duration-normal) var(--ease-in-out), transform var(--duration-normal) var(--ease-in-out);
}

.tabs-panels-enter-from,
.tabs-panels-leave-to {
  opacity: 0;
  transform: translateX(16px);
}

.tabs-panels-leave-active {
  position: absolute;
  inset: 0;
}
</style>
```

---

## 5. Form Interaction Patterns

### 5.1 Validation Feedback

```vue
<!-- FormField.vue -->
<template>
  <div class="form-field" :class="{ 'form-field--error': error, 'form-field--disabled': disabled }">
    <label v-if="label" :for="forId" class="form-label">
      {{ label }}
      <span v-if="required" class="required-mark" aria-hidden="true">*</span>
      <Tooltip v-if="tooltip" :content="tooltip" placement="top">
        <HelpIcon class="form-tooltip-trigger" aria-hidden="true" />
      </Tooltip>
    </label>

    <div class="form-control-wrapper">
      <slot 
        :error="error" 
        :errorId="errorId" 
        :helpId="helpId" 
        :ariaInvalid="!!error"
        :ariaDescribedby="error ? errorId : (help ? helpId : undefined)"
      />
    </div>

    <div v-if="help && !error" :id="helpId" class="form-help">{{ help }}</div>
    <div v-if="error" :id="errorId" class="form-error" role="alert" aria-live="assertive">{{ error }}</div>
  </div>
</template>

<style scoped>
.form-field {
  transition: var(--transition-colors);
}

.form-field--error .form-control-wrapper > * {
  border-color: var(--color-border-error);
  box-shadow: 0 0 0 3px var(--color-danger-bg);
}

.form-field--error .form-control-wrapper > *:focus {
  box-shadow: 0 0 0 3px var(--color-danger-bg), var(--shadow-focus);
}

.form-control-wrapper > * {
  transition: var(--transition-colors), var(--transition-shadow);
}

.form-error {
  color: var(--color-danger-text);
  font-size: var(--text-sm);
  margin-top: var(--space-1);
  animation: slideDown var(--duration-fast) var(--ease-out);
}

.form-help {
  color: var(--color-text-tertiary);
  font-size: var(--text-sm);
  margin-top: var(--space-1);
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
```

### 5.2 Select/Dropdown (Keyboard + Search + Multi-select)

```vue
<!-- Select.vue -->
<template>
  <div class="select-wrapper" :class="{ 'select-wrapper--open': open, 'select-wrapper--focused': focused, 'select-wrapper--error': error, 'select-wrapper--disabled': disabled }">
    <div 
      class="select-trigger" 
      :class="{ 'select-trigger--multiple': multiple }"
      @click="toggleOpen"
      @keydown="onKeydown"
      :tabindex="disabled ? -1 : 0"
      :aria-expanded="open"
      :aria-haspopup="listbox"
      :aria-controls="listboxId"
      :aria-invalid="!!error"
      :aria-describedby="error ? errorId : (help ? helpId : undefined)"
      :tabindex="disabled ? -1 : 0"
      ref="triggerRef"
    >
      <div class="select-value" v-if="!multiple || !modelValue.length">
        <span v-if="!modelValue || (Array.isArray(modelValue) && !modelValue.length)" class="placeholder">{{ placeholder }}</span>
        <span v-else class="selected-text">{{ getDisplayValue(modelValue) }}</span>
      </div>
      <div v-if="multiple && modelValue.length" class="select-chips">
        <span v-for="val in modelValue" :key="val" class="select-chip">
          {{ getDisplayValue(val) }}
          <button type="button" @click.stop="removeValue(val)" aria-label="Remover">{{ CloseIcon }}</button>
        </span>
      </div>
      <ChevronDownIcon :class="{ 'rotated': open }" aria-hidden="true" />
    </div>

    <Transition name="select-dropdown" appear>
      <div v-if="open" :id="listboxId" role="listbox" :aria-multiselectable="multiple" class="select-dropdown" ref="dropdownRef">
        <div v-if="searchable" class="select-search">
          <input 
            type="text" 
            v-model="searchQuery" 
            placeholder="Buscar..."
            @keydown="onSearchKeydown"
            ref="searchInputRef"
            aria-label="Filtrar opções"
          />
        </div>
        <div class="select-options" role="presentation">
          <div 
            v-for="option in filteredOptions" 
            :key="option.value" 
            :role="multiple ? 'option' : 'option'"
            :aria-selected="isSelected(option.value)"
            :aria-disabled="option.disabled"
            :class="['select-option', { 'select-option--selected': isSelected(option.value), 'select-option--disabled': option.disabled, 'select-option--focused': focusedIndex === option.value }]"
            @click="selectOption(option)"
            @mousemove="focusedIndex = option.value"
          >
            <span v-if="multiple && isSelected(option.value)" class="option-check" aria-hidden="true">{{ CheckIcon }}</span>
            <span class="option-label">{{ option.label }}</span>
            <span v-if="option.description" class="option-description">{{ option.description }}</span>
          </div>
          <div v-if="filteredOptions.length === 0" class="select-empty">
            <EmptyState :icon="SearchIcon" :title="Nenhum resultado" :description="Tente ajustar sua busca" />
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.select-trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--padding-input);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-input);
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
  font-size: var(--text-base);
  cursor: pointer;
  transition: var(--transition-colors), var(--transition-shadow);
  min-height: var(--touch-target-comfortable);
}

.select-wrapper--focused .select-trigger,
.select-wrapper--open .select-trigger {
  border-color: var(--color-border-focus);
  box-shadow: var(--shadow-focus);
}

.select-wrapper--error .select-trigger {
  border-color: var(--color-border-error);
}

.select-wrapper--error .select-trigger:focus {
  box-shadow: 0 0 0 3px var(--color-danger-bg), var(--shadow-focus);
}

.select-dropdown {
  position: absolute;
  top: calc(100% + var(--space-1));
  left: 0;
  right: 0;
  z-index: var(--z-index-dropdown);
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-dropdown);
  overflow: hidden;
  max-height: 300px;
}

.select-dropdown-enter-active,
.select-dropdown-leave-active {
  transition: opacity var(--duration-normal) var(--ease-out), transform var(--duration-normal) var(--ease-out);
}

.select-dropdown-enter-from,
.select-dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.select-option {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  cursor: pointer;
  transition: var(--transition-colors);
}

.select-option:hover:not(.select-option--disabled) {
  background: var(--color-bg-tertiary);
}

.select-option--selected {
  background: var(--color-primary-bg);
  color: var(--color-primary-text);
}

.select-option--focused {
  background: var(--color-bg-tertiary);
}

.select-option--disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.select-search input {
  width: 100%;
  padding: var(--space-2) var(--space-3);
  border: none;
  border-bottom: 1px solid var(--color-border-light);
  background: transparent;
  font-size: var(--text-sm);
  outline: none;
}

.select-search input:focus {
  background: var(--color-bg-primary);
}
</style>
```

---

## 6. Mobile & Touch Patterns (PWA)

### 6.1 Touch Targets & Gestures

```scss
// Touch targets aplicados via Design Tokens
--touch-target-min: 44px;
--touch-target-comfortable: 48px;

// Aplicado em todos componentes interativos
.btn, .form-input, .select-trigger, .tab-trigger, .dropdown-trigger, .accordion-trigger {
  min-height: var(--touch-target-comfortable);
  min-width: var(--touch-target-comfortable);
}

// Swipe to close Drawer (mobile)
.drawer-content {
  touch-action: pan-y;
}

.drawer-content.swipe-close {
  transition: transform var(--duration-normal) var(--ease-out);
}
```

### 6.2 PWA Offline Patterns

```vue
<!-- QRScanner.vue (PWA Offline-First) -->
<template>
  <div class="qr-scanner" :class="{ 'qr-scanner--offline': isOffline }">
    <div class="qr-scanner-viewfinder" aria-hidden="true">
      <div class="viewfinder-frame" :class="{ 'viewfinder-frame--success': lastResult }">
        <slot name="frame" />
      </div>
      <div class="viewfinder-instruction" v-if="!lastResult">
        <slot name="instruction">
          <p>Posicione o QR Code do ativo dentro da moldura</p>
        </slot>
      </div>
    </div>

    <div class="qr-scanner-controls">
      <Button 
        variant="secondary" 
        icon="FlashlightIcon" 
        @click="toggleTorch" 
        :disabled="!supportsTorch"
        :aria-pressed="torchOn"
      >
        {{ torchOn ? 'Desligar lanterna' : 'Ligar lanterna' }}
      </Button>
      
      <Button 
        variant="primary" 
        :loading="scanning" 
        @click="startScan"
        :disabled="scanning || !hasPermission"
      >
        <span v-if="scanning">Escaneando...</span>
        <span v-else>Iniciar Escaneamento</span>
      </Button>
    </div>

    <!-- Offline Indicator -->
    <div v-if="isOffline" class="offline-banner" role="status" aria-live="polite">
      <WifiOffIcon aria-hidden="true" />
      <span>Você está offline. Escaneamentos serão salvos localmente e sincronizados ao reconectar.</span>
      <span class="pending-count" v-if="pendingCount > 0">{{ pendingCount }} pendente(s)</span>
    </div>

    <!-- Result Toast -->
    <Toast v-if="lastResult" type="success" :duration="5000" :action="{ label: 'Ver Ativo', onClick: () => navigateToAsset(lastResult) }">
      Ativo encontrado: {{ lastResult.tag }} — {{ lastResult.modelo }}
    </Toast>
  </div>
</template>

<style scoped>
.qr-scanner {
  position: relative;
  padding: var(--space-6);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-xl);
}

.qr-scanner--offline {
  border: 2px dashed var(--color-warning);
}

.viewfinder-frame {
  position: relative;
  width: 200px;
  height: 200px;
  margin: 0 auto var(--space-6);
  border: 2px solid var(--color-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.7);
}

.viewfinder-frame--success {
  border-color: var(--color-success);
  animation: pulseSuccess var(--duration-slow) var(--ease-spring) 3;
}

@keyframes pulseSuccess {
  0%, 100% { box-shadow: 0 0 0 0 var(--color-success); }
  50% { box-shadow: 0 0 0 8px var(--color-success-bg); }
}

.offline-banner {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  background: var(--color-warning-bg);
  border: 1px solid var(--color-warning-border);
  border-radius: var(--radius-md);
  color: var(--color-warning-text);
  font-size: var(--text-sm);
  margin-top: var(--space-4);
  animation: slideDown var(--duration-normal) var(--ease-out);
}

.pending-count {
  background: var(--color-warning);
  color: var(--color-neutral-0);
  padding: 2px var(--space-2);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
}
</style>
```

---

## 6. PWA Offline-First Patterns

### 6.1 Service Worker + IndexedDB + Background Sync

```typescript
// composables/useOffline.ts
export const useOffline = () => {
  const isOnline = ref(navigator.onLine);
  const pendingSync = ref(0);
  const db = useIndexedDB('aegis1-offline', {
    healthChecks: { keyPath: 'id', autoIncrement: true },
    pendingOrders: { keyPath: 'id', autoIncrement: true },
  });

  // Register Service Worker
  const registerSW = async () => {
    if ('serviceWorker' in navigator) {
      const registration = await navigator.serviceWorker.register('/sw.js', { scope: '/' });
      registration.addEventListener('updatefound', () => {
        const newWorker = registration.installing;
        newWorker?.addEventListener('statechange', () => {
          if (newWorker.state === 'installed' && navigator.serviceWorker.controller) {
            // New version available
            useToast().add({ type: 'info', message: 'Nova versão disponível. Clique para atualizar.', action: { label: 'Atualizar', onClick: () => newWorker.postMessage({ type: 'SKIP_WAITING' }) } });
          }
        });
      });
    }
  };

  // Background Sync
  const registerBackgroundSync = async (tag: string) => {
    if ('serviceWorker' in navigator && 'sync' in registration) {
      await registration.sync.register(tag);
      pendingSync.value++;
    }
  };

  // Save Health Check Offline
  const saveHealthCheckOffline = async (data: HealthCheckData) => {
    await db.healthChecks.add({ ...data, timestamp: Date.now(), synced: false });
    await registerBackgroundSync('health-check-sync');
  };

  // Sync when online
  const syncWhenOnline = async () => {
    if (!isOnline.value) return;
    
    const pending = await db.healthChecks.where('synced').equals(false).toArray();
    for (const item of pending) {
      try {
        await api.post('/health-check', item.data);
        await db.healthChecks.update(item.id, { synced: true });
        pendingSync.value--;
      } catch (e) {
        console.error('Sync failed:', e);
      }
    }
  };

  // Online/Offline listeners
  onMounted(() => {
    window.addEventListener('online', () => { isOnline.value = true; syncWhenOnline(); });
    window.addEventListener('offline', () => { isOnline.value = false; });
    registerSW();
  });

  return { isOnline, pendingSync, saveHealthCheckOffline, syncWhenOnline };
};
```

### 6.2 Background Sync Registration (Service Worker)

```javascript
// public/sw.js (Workbox via Vite PWA Plugin)
self.addEventListener('sync', (event) => {
  if (event.tag === 'health-check-sync') {
    event.waitFor(syncHealthChecks());
  }
});

async function syncHealthChecks() {
  const db = await openDB('aegis1-offline', 1);
  const pending = await db.getAllFromIndex('healthChecks', 'synced', false);
  
  for (const item of pending) {
    try {
      await fetch('/api/v1/health-check', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${getAccessToken()}` },
        body: JSON.stringify(item.data)
      });
      await db.put('healthChecks', { ...item, synced: true });
    } catch (e) {
      console.error('Sync failed:', e);
      // Will retry on next sync event
    }
  }
}
```

---

## 7. Accessibility Motion Patterns

### 7.1 Reduced Motion Implementation

```scss
// Global reduced motion (tokens.scss)
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: var(--duration-reduced) !important;
    animation-iteration-count: 1 !important;
    transition-duration: var(--duration-reduced) !important;
    scroll-behavior: auto !important;
  }
  
  // Vue Transitions
  .v-enter-active,
  .v-leave-active,
  .v-move {
    transition-duration: var(--duration-reduced) !important;
  }
  
  // Disable specific animations
  .spinner,
  .shimmer,
  .pulse {
    animation: none !important;
  }
  
  // Disable parallax/scroll animations
  .parallax,
  .scroll-animate {
    transform: none !important;
    animation: none !important;
  }
}
```

### 7.2 Focus Management Patterns

```typescript
// composables/useFocusTrap.ts (VueUse wrapper)
export const useFocusTrap = (element: Ref<HTMLElement | undefined>, options: FocusTrapOptions = {}) => {
  const { activate, deactivate, pause, unpause } = useFocusTrap(element, {
    immediate: options.immediate ?? true,
    onDeactivate: options.onDeactivate,
    fallbackFocus: options.fallbackFocus,
    escapeDeactivates: options.escapeDeactivates ?? true,
    clickOutsideDeactivates: options.clickOutsideDeactivates ?? true,
    returnFocus: options.returnFocus ?? true,
  });

  return { activate, deactivate, pause, unpause };
};

// Usage in Modal.vue
const contentRef = ref<HTMLElement>();
const { activate, deactivate } = useFocusTrap(contentRef, { 
  immediate: true,
  onDeactivate: () => emit('close'),
  returnFocus: true,
});

watch(open, (val) => {
  if (val) activate();
  else deactivate();
});
```

---

## 7. Performance Guidelines

| Regra | Implementação |
| :--- | :--- |
| **GPU-only properties** | Apenas `transform`, `opacity`, `filter` animados |
| **will-change** | Aplicado temporariamente durante animação (`will-change: transform, opacity`) |
| **Layout thrashing** | Evitado: ler layout → escrever layout em batch (requestAnimationFrame) |
| **Layout shift** | `aspect-ratio` em imagens; `min-height` em skeletons; `contain: layout` em containers |
| **Paint optimization** | `contain: paint` em modais/drawers; `content-visibility: auto` em listas longas |
| **Bundle size** | Code-splitting por rota (Vue Router lazy loading); Dynamic imports para componentes pesados (Chart, QRScanner, PdfViewer) |

---

## 8. Testing Motion Patterns

### 8.1 Unit Tests (Vitest + Vue Test Utils)

```typescript
// Button.test.ts
import { mount } from '@vue/test-utils';
import Button from './Button.vue';

describe('Button Motion', () => {
  it('applies active scale transform', async () => {
    const wrapper = mount(Button, { props: { variant: 'primary' } });
    const button = wrapper.find('button');
    
    await button.trigger('mousedown');
    expect(button.element.style.transform).toBe('scale(0.98)');
    
    await button.trigger('mouseup');
    expect(button.element.style.transform).toBe('');
  });

  it('shows spinner when loading', () => {
    const wrapper = mount(Button, { props: { loading: true } });
    expect(wrapper.find('.btn-spinner').exists()).toBe(true);
    expect(wrapper.find('.btn-spinner').element.style.animation).toContain('spin');
  });

  it('respects prefers-reduced-motion', async () => {
    // Mock matchMedia
    vi.stubGlobal('matchMedia', () => ({ matches: true, addEventListener: vi.fn(), removeEventListener: vi.fn() }));
    
    const wrapper = mount(Button, { props: { variant: 'primary' } });
    const button = wrapper.find('button');
    
    await button.trigger('mousedown');
    // Should not apply transform when reduced motion
    expect(button.element.style.transform).toBe('');
  });
});
```

### 8.2 E2E Tests (Cypress + axe-core)

```typescript
// motion.cy.ts
describe('Motion Patterns', () => {
  beforeEach(() => {
    cy.visit('/dashboard');
    cy.injectAxe();
  });

  it('should animate button click', () => {
    cy.get('button.btn--primary').first().click();
    cy.get('button.btn--primary').first().should('have.css', 'transform', 'matrix(0.98, 0, 0, 0.98, 0, 0)');
  });

  it('should respect prefers-reduced-motion', () => {
    cy.visit('/dashboard', { 
      onBeforeLoad: (win) => {
        cy.stub(win, 'matchMedia').returns({ matches: true, addEventListener: () => {}, removeEventListener: () => {} });
      }
    });
    
    cy.get('button.btn--primary').first().click();
    cy.get('button.btn--primary').first().should('not.have.css', 'transform', 'matrix(0.98, 0, 0, 0.98, 0, 0)');
  });

  it('should animate modal open/close', () => {
    cy.get('[data-cy="open-modal"]').click();
    cy.get('.modal-content').should('have.css', 'transform', 'matrix(1, 0, 0, 1, 0, 0)');
    cy.get('.modal-content').should('have.css', 'opacity', '1');
    
    cy.get('.modal-close').click();
    cy.get('.modal-overlay').should('not.exist');
  });

  it('should have no accessibility violations during animations', () => {
    cy.checkA11y();
  });
});
```

---

## 9. Rastreabilidade Motion ↔ Artefatos

| Pattern | Design Tokens | Component Library | Accessibility | Component Code | Testes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Button Click/Loading** | `tokens.scss` (Seção 6, 9.1) | `Button.vue`, `IconButton.vue` | `accessibility-guidelines.md#buttons` | `Button.vue` (style scoped) | Unit + E2E + A11y |
| **Modal/Drawer** | `tokens.scss` (shadows, z-index, durations) | `Modal.vue`, `Drawer.vue` | `accessibility-guidelines.md#modals` | `Modal.vue`, `Drawer.vue` | Unit + E2E + A11y |
| **Toast** | `tokens.scss` (feedback tokens) | `Toast.vue`, `ToastContainer.vue` | `accessibility-guidelines.md#toasts` | `Toast.vue`, `ToastContainer.vue` | Unit + E2E + A11y |
| **DataTable** | `tokens.scss` (table, skeleton) | `DataTable.vue` | `accessibility-guidelines.md#tables` | `DataTable.vue` | Unit + E2E + A11y |
| **Tabs** | `tokens.scss` (transitions) | `Tabs.vue` | `accessibility-guidelines.md#tabs` | `Tabs.vue` | Unit + E2E + A11y |
| **Form Validation** | `tokens.scss` (form tokens) | `FormField.vue`, `Input.vue` | `accessibility-guidelines.md#forms` | `FormField.vue` | Unit + E2E + A11y |
| **Select/Dropdown** | `tokens.scss` (dropdown tokens) | `Select.vue`, `Dropdown.vue` | `accessibility-guidelines.md#select` | `Select.vue`, `Dropdown.vue` | Unit + E2E + A11y |
| **QRScanner (PWA)** | `tokens.scss` (QR tokens) | `QRScanner.vue` | `accessibility-guidelines.md#qr` | `QRScanner.vue` | Unit + E2E + A11y |
| **Offline/Sync** | — | `useOffline.ts`, `sw.js` | `accessibility-guidelines.md#offline` | `useOffline.ts`, `public/sw.js` | Unit + E2E |
| **Reduced Motion** | `tokens.scss` (reduced motion) | Global CSS | `accessibility-guidelines.md#motion` | `@media (prefers-reduced-motion)` | Cypress + matchMedia stub |

---

*Documento regenerado completamente com padrões implementados no Vue 3 + Bootstrap 5 + PWA + Design Tokens v2.0. Substitui versão 0.1 que continha apenas especificações teóricas sem implementação.*