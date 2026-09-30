# Component Library / Inventory — Aegis Patrimônio

> **Versão:** 1.0 · **Owner:** Frontend Lead · **Status:** Draft
> **Baseado em:** Componentes Vue.js 3 + Bootstrap 5 + Pinia + Vite
> **Depende de:** `design-tokens.md` (tokens de cor, espaçamento, tipografia, motion, z-index), `user-flows.md` (fluxos que definem componentes necessários)

---

## 1. Overview

Este inventário documenta os **componentes de interface reutilizáveis** para o frontend do Aegis Patrimônio — uma aplicação **Vue.js 3 + Bootstrap 5 + Pinia + Vite** com arquitetura baseada em componentes (Single File Components `.vue`).

Os componentes são implementados como **Vue SFCs** que consomem **CSS Custom Properties** definidas em `design-tokens.md` e seguem as convenções de composição do Vue 3 (Composition API, `<script setup>`).

> **Estado atual (diagnóstico determinístico):** O workspace contém o frontend em `frontend/` com Vue.js 3, Bootstrap 5, Pinia, Vite. O build de produção está em `frontend/dist/`. Este inventário documenta os componentes existentes e planejados com base nos fluxos de usuário (`user-flows.md`) e tokens de design (`design-tokens.md`).

---

## 2. Convenções Gerais

| Convenção | Definição |
| :--- | :--- |
| **Padrão de composição** | Vue 3 SFCs com `<script setup>` + Composition API; props tipadas com `defineProps`, emits com `defineEmits` |
| **Nomenclatura** | `PascalCase` para componentes (ex: `AtivoCard.vue`, `DataTable.vue`); arquivos em `PascalCase.vue` |
| **Local no repositório** | `frontend/src/components/` — organizado por domínio (`ativos/`, `manutencoes/`, `ui/`, `layout/`) |
| **Estilização** | **CSS Custom Properties (tokens)** + Bootstrap 5 utility classes; scoped CSS nos SFCs; zero CSS-in-JS |
| **Acessibilidade** | Obrigatório: `role`, `aria-*`, `tabindex`, focus management, `aria-live` para toasts/alertas; WCAG 2.1 AA |
| **Tokens consumidos** | Referência direta a `design-tokens.md` — ex: `var(--color-brand-primary)`, `var(--space-4)`, `var(--duration-normal)` |
| **Estado global** | Pinia stores para auth, ativos, manutenções, alertas, UI (sidebar, theme) |
| **Roteamento** | Vue Router com lazy loading; guards de autenticação/autorização |

---

## 3. Estrutura de Pastas

```
frontend/src/components/
├── ui/                    # Componentes base (design system)
│   ├── Button.vue
│   ├── IconButton.vue
│   ├── Input.vue
│   ├── Select.vue
│   ├── Textarea.vue
│   ├── Checkbox.vue
│   ├── RadioGroup.vue
│   ├── Switch.vue
│   ├── Badge.vue
│   ├── Tooltip.vue
│   ├── Dropdown.vue
│   ├── Modal.vue
│   ├── Drawer.vue
│   ├── Toast.vue
│   ├── Alert.vue
│   ├── Tabs.vue
│   ├── Breadcrumb.vue
│   ├── Pagination.vue
│   ├── Avatar.vue
│   ├── Card.vue
│   ├── EmptyState.vue
│   ├── Skeleton.vue
│   ├── FileUpload.vue
│   └── DatePicker.vue
├── data-display/          # Componentes de exibição de dados
│   ├── DataTable.vue
│   ├── AtivoCard.vue
│   ├── AtivoDetail.vue
│   ├── ManutencaoCard.vue
│   ├── AlertaItem.vue
│   └── QRCodeLabel.vue
├── forms/                 # Componentes de formulário específicos do domínio
│   ├── AtivoForm.vue
│   ├── ManutencaoForm.vue
│   ├── FornecedorForm.vue
│   ├── PessoaForm.vue
│   ├── DepartamentoForm.vue
│   ├── FilialForm.vue
│   ├── TipoAtivoForm.vue
│   ├── LocalizacaoForm.vue
│   ├── RoleForm.vue
│   ├── PermissionForm.vue
│   └── UsuarioForm.vue
├── layout/                # Componentes de layout
│   ├── Header.vue
│   ├── Sidebar.vue
│   ├── Footer.vue
│   ├── PageLayout.vue
│   └── FormLayout.vue
└── index.ts               # Barrel export
```

---

## 4. Inventário de Componentes

### 4.1 Componentes Base (UI) — 21 componentes

| Componente | Arquivo | Descrição | Tokens Principais | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Button** | `ui/Button.vue` | Botão primário/secundário/danger/ghost com loading, disabled, icon | `--btn-*`, `--color-brand-*`, `--color-danger`, `--duration-fast` | ✅ Existente |
| **IconButton** | `ui/IconButton.vue` | Botão apenas ícone (toolbar, ações de tabela) | `--btn-*`, `--space-2`, `--radius-sm` | ✅ Existente |
| **Input** | `ui/Input.vue` | Input text/email/password/number/date com label, error, helper | `--input-*`, `--color-border-*`, `--color-danger`, `--shadow-focus` | ✅ Existente |
| **Select** | `ui/Select.vue` | Select nativo com options, searchable, multiple | `--input-*`, `--color-border-*`, `--radius-md` | ✅ Existente |
| **Textarea** | `ui/Textarea.vue` | Textarea com label, error, rows, maxLength | `--input-*`, `--color-border-*` | ✅ Existente |
| **Checkbox** | `ui/Checkbox.vue` | Checkbox com label, indeterminate | `--color-brand-primary`, `--radius-sm` | ✅ Existente |
| **RadioGroup** | `ui/RadioGroup.vue` | Grupo de radio buttons horizontal/vertical | `--color-brand-primary`, `--space-3` | ✅ Existente |
| **Switch** | `ui/Switch.vue` | Toggle switch para boolean | `--color-brand-primary`, `--radius-full` | ✅ Existente |
| **Badge** | `ui/Badge.vue` | Badge de status (ativo, manutenção, baixa, crítico, info, warning, success) | `--badge-*`, `--color-success/warning/danger/info` | ✅ Existente |
| **Tooltip** | `ui/Tooltip.vue` | Tooltip com Popper.js positioning | `--z-popover`, `--shadow-md`, `--duration-fast` | ✅ Existente |
| **Dropdown** | `ui/Dropdown.vue` | Dropdown menu com Popper.js | `--z-dropdown`, `--shadow-md`, `--space-2` | ✅ Existente |
| **Modal** | `ui/Modal.vue` | Modal com trap focus, footer actions, sizes | `--z-modal`, `--shadow-xl`, `--radius-lg`, `--duration-normal` | ✅ Existente |
| **Drawer** | `ui/Drawer.vue` | Drawer lateral (filtros, detalhes) | `--z-modal`, `--shadow-xl`, `--duration-slow` | ✅ Existente |
| **Toast** | `ui/Toast.vue` | Toast notification com variants, auto-dismiss | `--z-toast`, `--color-semantic-*`, `--duration-normal` | ✅ Existente |
| **Alert** | `ui/Alert.vue` | Alert inline dismissible | `--color-semantic-*`, `--radius-md` | ✅ Existente |
| **Tabs** | `ui/Tabs.vue` | Tabs line/enclosed | `--color-brand-primary`, `--border-strong` | ✅ Existente |
| **Breadcrumb** | `ui/Breadcrumb.vue` | Navegação hierárquica | `--text-sm`, `--color-text-secondary` | ✅ Existente |
| **Pagination** | `ui/Pagination.vue` | Paginação com page size changer | `--btn-*`, `--space-2` | ✅ Existente |
| **Avatar** | `ui/Avatar.vue` | Avatar com fallback iniciais, status | `--radius-full`, `--space-2` | ✅ Existente |
| **Card** | `ui/Card.vue` | Card container com header/footer/elevation | `--radius-md`, `--shadow-md`, `--space-4` | ✅ Existente |
| **EmptyState** | `ui/EmptyState.vue` | Estado vazio com ícone, título, ação | `--text-lg`, `--color-text-secondary` | ✅ Existente |

### 4.2 Componentes de Exibição de Dados — 6 componentes

| Componente | Arquivo | Descrição | Tokens Principais | Status |
| :--- | :--- | :--- | :--- | :--- |
| **DataTable** | `data-display/DataTable.vue` | Tabela genérica: columns, data, sortable, filterable, selectable, pagination, rowActions | `--table-*`, `--text-sm`, `--space-3/4` | ✅ Existente |
| **AtivoCard** | `data-display/AtivoCard.vue` | Card resumo de ativo (dashboard, lista) | `--card-*`, `--badge-*`, `--font-mono` | ✅ Existente |
| **AtivoDetail** | `data-display/AtivoDetail.vue` | Detalhe completo de ativo (modal/drawer) | `--font-mono`, `--space-4/6` | ✅ Existente |
| **ManutencaoCard** | `data-display/ManutencaoCard.vue` | Card de solicitação/ordem de manutenção | `--badge-*`, `--color-warning/danger` | ✅ Existente |
| **AlertaItem** | `data-display/AlertaItem.vue` | Item de alerta de recurso (disco, CPU, memória) | `--badge-*`, `--color-warning/danger` | ✅ Existente |
| **QRCodeLabel** | `data-display/QRCodeLabel.vue` | Etiqueta QR Code para impressão (PDF) | `--qrcode-*`, `--label-*`, `--font-mono` | ✅ Existente |

### 4.3 Componentes de Formulário (Domínio) — 11 componentes

| Componente | Arquivo | Entidade | Campos Principais | Status |
| :--- | :--- | :--- | :--- | :--- |
| **AtivoForm** | `forms/AtivoForm.vue` | Ativo | tipo, filial, departamento, localização, fornecedor, valor, data aquisição, número série, specs hardware | ✅ Existente |
| **ManutencaoForm** | `forms/ManutencaoForm.vue` | Solicitação Manutenção | ativo, descrição, prioridade, tipo, anexos | ✅ Existente |
| **FornecedorForm** | `forms/FornecedorForm.vue` | Fornecedor | nome, CNPJ, contato, email, telefone, endereço | ✅ Existente |
| **PessoaForm** | `forms/PessoaForm.vue` | Funcionário | nome, CPF, email, cargo, filial, departamento, usuário vinculado | ✅ Existente |
| **DepartamentoForm** | `forms/DepartamentoForm.vue` | Departamento | nome, descrição, filial pai | ✅ Existente |
| **FilialForm** | `forms/FilialForm.vue` | Filial | nome, código, endereço, responsável | ✅ Existente |
| **TipoAtivoForm** | `forms/TipoAtivoForm.vue` | Tipo Ativo | nome, ícone, vida útil, método depreciação | ✅ Existente |
| **LocalizacaoForm** | `forms/LocalizacaoForm.vue` | Localização | nome, filial, tipo, descrição, pai (hierarquia) | ✅ Existente |
| **RoleForm** | `forms/RoleForm.vue` | Role | nome, descrição, permissões | ✅ Existente |
| **PermissionForm** | `forms/PermissionForm.vue` | Permission | nome, recurso, ação, descrição | ✅ Existente |
| **UsuarioForm** | `forms/UsuarioForm.vue` | Usuário | username, email, senha, roles, funcionário vinculado | ✅ Existente |

### 4.4 Componentes de Layout — 5 componentes

| Componente | Arquivo | Descrição | Tokens Principais | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Header** | `layout/Header.vue` | Top bar: título, user menu, notificações, theme toggle | `--z-sticky`, `--shadow-sm`, `--space-4` | ✅ Existente |
| **Sidebar** | `layout/Sidebar.vue` | Menu lateral colapsível, navegação por módulo | `--z-sticky`, `--shadow-md`, `--space-4` | ✅ Existente |
| **Footer** | `layout/Footer.vue` | Rodapé com versão, links úteis | `--text-xs`, `--color-text-secondary` | ✅ Existente |
| **PageLayout** | `layout/PageLayout.vue` | Layout base: header + sidebar + main + footer | `--space-6/8` | ✅ Existente |
| **FormLayout** | `layout/FormLayout.vue` | Grid responsivo para formulários (1/2/3 colunas) | `--space-4/6`, breakpoints | ✅ Existente |

### 4.5 Componentes de Views/Páginas (Não reutilizáveis, mas documentados)

| View | Arquivo | Rota | Componentes Filhos Principais |
| :--- | :--- | :--- | :--- |
| **DashboardView** | `views/DashboardView.vue` | `/` | `AtivoCard`, `AlertaItem`, `DataTable` (resumo), charts |
| **AtivosView** | `views/AtivosView.vue` | `/ativos` | `DataTable`, `AtivoForm` (modal), `AtivoDetail` (drawer), `QRCodeLabel` |
| **AtivoDetailView** | `views/AtivoDetailView.vue` | `/ativos/:id` | `AtivoDetail`, `ManutencaoCard` (histórico) |
| **ManutencoesView** | `views/ManutencoesView.vue` | `/manutencoes` | `DataTable`, `ManutencaoForm` (modal), `ManutencaoCard` |
| **FornecedoresView** | `views/FornecedoresView.vue` | `/fornecedores` | `DataTable`, `FornecedorForm` (modal) |
| **PessoasView** | `views/PessoasView.vue` | `/pessoas` | `DataTable`, `PessoaForm` (modal) |
| **DepartamentosView** | `views/DepartamentosView.vue` | `/departamentos` | `DataTable`, `DepartamentoForm` (modal) |
| **FiliaisView** | `views/FiliaisView.vue` | `/filiais` | `DataTable`, `FilialForm` (modal) |
| **TiposAtivoView** | `views/TiposAtivoView.vue` | `/tipos-ativo` | `DataTable`, `TipoAtivoForm` (modal) |
| **LocalizacoesView** | `views/LocalizacoesView.vue` | `/localizacoes` | `DataTable`, `LocalizacaoForm` (modal) |
| **RolesView** | `views/RolesView.vue` | `/roles` | `DataTable`, `RoleForm` (modal) |
| **PermissionsView** | `views/PermissionsView.vue` | `/permissions` | `DataTable`, `PermissionForm` (modal) |
| **UsuariosView** | `views/UsuariosView.vue` | `/usuarios` | `DataTable`, `UsuarioForm` (modal) |
| **SystemHealthView** | `views/SystemHealthView.vue` | `/health` | `AlertaItem`, charts preditivos |
| **ScannerView** | `views/ScannerView.vue` | `/scanner` | Camera QR/barcode, `AtivoDetail` |
| **DetailView** | `views/DetailView.vue` | `/detail/:type/:id` | Genérico para detalhes |

---

## 5. Mapeamento Fluxos de Usuário → Componentes

| Fluxo (user-flows.md) | Componentes Envolvidos |
| :--- | :--- |
| **Flow 1: Login/Autenticação** | `Input` (email/password), `Button` (submit), `Alert` (erro), `Toast` (sucesso) |
| **Flow 2: CRUD Entidades Mestres (Admin)** | `DataTable`, `*Form` (modal), `Button`, `Badge` (status), `Toast`, `Modal`, `Pagination` |
| **Flow 3: Cadastro de Ativo** | `AtivoForm` (modal), `Select` (filial, depto, local, tipo, fornecedor), `Input` (valor, série), `Button`, `Toast` |
| **Flow 4: Solicitação Manutenção (User)** | `ManutencaoForm` (modal), `Select` (ativo filtrado por filial/user), `Textarea`, `FileUpload`, `Button`, `Toast` |
| **Flow 5: Aprovação Manutenção (Admin)** | `DataTable` (filtro status=PENDENTE), `ManutencaoCard`, `Button` (aprovar/cancelar), `Modal` (confirmação), `Toast` |
| **Flow 6: Execução Manutenção (Técnico)** | `ManutencaoCard`, `Button` (iniciar/concluir), `Textarea` (relatório), `Toast` |
| **Flow 7: Consulta Ativos + QR Code** | `DataTable` (filtros avançados), `AtivoCard`, `AtivoDetail` (drawer), `QRCodeLabel` (modal), `Button` (imprimir) |
| **Flow 8: Dashboard + Alertas Preditivos** | `AtivoCard` (KPIs), `AlertaItem` (lista), `Chart` (Chart.js), `Skeleton` (loading) |
| **Flow 9: Health Check Scanner** | `ScannerView` (camera), `AtivoDetail` (modal), `Toast` (resultado) |
| **Flow 10: Admin Usuários/Roles/Permissions** | `DataTable`, `UsuarioForm`/`RoleForm`/`PermissionForm`, `Switch` (ativo/inativo), `Checkbox` (permissões) |

---

## 6. API de Componentes Base (Exemplos)

### 6.1 Button.vue

```vue
<!-- frontend/src/components/ui/Button.vue -->
<script setup lang="ts">
import { tokens } from '@/utils/tokens';

interface Props {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  loading?: boolean;
  disabled?: boolean;
  iconOnly?: boolean;
  type?: 'button' | 'submit' | 'reset';
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  loading: false,
  disabled: false,
  iconOnly: false,
  type: 'button',
});

defineEmits<{ click: [event: MouseEvent] }>();
</script>

<template>
  <button
    :type="props.type"
    :disabled="props.disabled || props.loading"
    :class="[
      'btn',
      `btn-${props.variant}`,
      `btn-${props.size}`,
      { 'btn-icon-only': props.iconOnly, 'is-loading': props.loading }
    ]"
    :style="buttonStyle"
    @click="$emit('click', $event)"
  >
    <span v-if="props.loading" class="spinner" aria-hidden="true"></span>
    <slot v-else />
  </button>
</template>

<style scoped>
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  border: none;
  border-radius: var(--radius-sm);
  font-family: var(--font-sans);
  font-size: var(--text-sm);
  font-weight: 500;
  cursor: pointer;
  transition: background-color var(--duration-fast) var(--ease-standard),
              box-shadow var(--duration-fast) var(--ease-standard);
}
.btn:disabled { cursor: not-allowed; opacity: 0.6; }
.btn:focus-visible { outline: none; box-shadow: var(--shadow-focus); }

.btn-primary { background: var(--color-brand-primary); color: var(--color-text-inverse); }
.btn-primary:hover:not(:disabled) { background: var(--color-brand-hover); }
.btn-secondary { background: var(--color-bg-secondary); color: var(--color-text-primary); border: 1px solid var(--color-border-subtle); }
.btn-secondary:hover:not(:disabled) { background: var(--color-bg-tertiary); }
.btn-danger { background: var(--color-danger); color: var(--color-text-inverse); }
.btn-danger:hover:not(:disabled) { background: var(--color-danger) / 0.9; }
.btn-ghost { background: transparent; color: var(--color-text-primary); }
.btn-ghost:hover:not(:disabled) { background: var(--color-bg-secondary); }

.btn-sm { height: 2rem; padding: 0 var(--space-3); }
.btn-md { height: 2.5rem; padding: 0 var(--space-4); }
.btn-lg { height: 3rem; padding: 0 var(--space-5); }
.btn-icon-only { padding: 0; width: 2.5rem; }

.spinner {
  width: 1em; height: 1em;
  border: 2px solid currentColor;
  border-right-color: transparent;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
```

### 6.2 DataTable.vue (Genérico)

```vue
<!-- frontend/src/components/data-display/DataTable.vue -->
<script setup lang="ts">
import { tokens } from '@/utils/tokens';
import { h } from 'vue';
import Button from '@/components/ui/Button.vue';
import Badge from '@/components/ui/Badge.vue';
import Pagination from '@/components/ui/Pagination.vue';

interface Column<T> {
  key: string;
  header: string;
  sortable?: boolean;
  filterable?: boolean;
  render?: (row: T) => VNode | string;
  width?: string;
}

interface Props {
  columns: Column<any>[];
  data: any[];
  sortable?: boolean;
  filterable?: boolean;
  selectable?: boolean;
  pagination?: boolean;
  rowActions?: Array<{ label: string; variant: 'primary' | 'danger' | 'ghost'; icon?: string; action: (row: any) => void }>;
  emptyState?: { icon: string; title: string; description: string; action?: { label: string; onClick: () => void } };
  loading?: boolean;
  density?: 'compact' | 'comfortable';
}

const props = withDefaults(defineProps<Props>(), {
  sortable: true,
  filterable: false,
  selectable: false,
  pagination: true,
  density: 'comfortable',
});

defineEmits<{ sort: [key: string, direction: 'asc' | 'desc']; selectionChange: [rows: any[]]; pageChange: [page: number, pageSize: number] }>();

// ... lógica de sort, filter, pagination, selection
</script>

<template>
  <div class="data-table" :style="tableStyle">
    <div class="table-wrapper" v-if="!props.loading">
      <table :style="tableStyle">
        <thead>
          <tr>
            <th v-for="col in props.columns" :key="col.key" :style="{ width: col.width }" @click="col.sortable && handleSort(col.key)">
              {{ col.header }}
              <span v-if="col.sortable" class="sort-icon" :class="sortIconClass(col.key)"></span>
            </th>
            <th v-if="props.rowActions" style="width: 120px">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in displayedData" :key="row.id" :class="{ selected: isSelected(row) }">
            <td v-for="col in props.columns" :key="col.key">
              <span v-if="col.render" v-html="col.render(row)" />
              <span v-else>{{ row[col.key] }}</span>
            </td>
            <td v-if="props.rowActions">
              <div class="actions">
                <Button
                  v-for="action in props.rowActions"
                  :key="action.label"
                  :variant="action.variant"
                  size="sm"
                  icon-only
                  @click="action.action(row)"
                >
                  <i :class="action.icon" />
                </Button>
              </div>
            </td>
          </tr>
          <tr v-if="displayedData.length === 0 && !props.loading">
            <td :colspan="props.columns.length + (props.rowActions ? 1 : 0)">
              <EmptyState v-bind="props.emptyState" />
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <Skeleton v-else variant="table" :rows="10" />
    <Pagination v-if="props.pagination" :total="filteredData.length" @page-change="$emit('pageChange', $event)" />
  </div>
</template>

<style scoped>
.data-table { overflow-x: auto; }
.table-wrapper { min-width: 600px; }
table { width: 100%; border-collapse: collapse; font-size: var(--text-sm); }
th, td { padding: var(--space-3) var(--space-4); text-align: left; border-bottom: 1px solid var(--color-border-subtle); }
th { background: var(--color-bg-secondary); font-weight: 600; color: var(--color-text-primary); white-space: nowrap; }
th:hover { background: var(--color-bg-tertiary); cursor: pointer; }
tr:hover td { background: var(--color-bg-tertiary); }
tr.selected td { background: var(--color-brand-primary) / 0.1; }
.actions { display: flex; gap: var(--space-2); }
</style>
```

### 6.3 Badge.vue (Status Específicos do Domínio)

```vue
<!-- frontend/src/components/ui/Badge.vue -->
<script setup lang="ts">
import { tokens } from '@/utils/tokens';

type Variant = 'ativo' | 'manutencao' | 'baixa' | 'critico' | 'info' | 'warning' | 'success' | 'neutral';

interface Props {
  variant: Variant;
  dot?: boolean;
  removable?: boolean;
  label?: string;
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'neutral',
  dot: false,
  removable: false,
});

defineEmits<{ remove: [] }>();

const variantClasses: Record<Variant, string> = {
  ativo: 'badge-ativo',
  manutencao: 'badge-manutencao',
  baixa: 'badge-baixa',
  critico: 'badge-critico',
  info: 'badge-info',
  warning: 'badge-warning',
  success: 'badge-success',
  neutral: 'badge-neutral',
};
</script>

<template>
  <span :class="['badge', variantClasses[props.variant]]" :style="badgeStyle">
    <span v-if="props.dot" class="badge-dot" aria-hidden="true"></span>
    <slot>{{ props.label || props.variant }}</slot>
    <button v-if="props.removable" class="badge-remove" @click="$emit('remove')" aria-label="Remover">
      <i class="bi bi-x" />
    </button>
  </span>
</template>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--badge-padding);
  border-radius: var(--badge-radius);
  font-size: var(--badge-font-size);
  font-weight: var(--badge-font-weight);
  white-space: nowrap;
}
.badge-dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }
.badge-remove { display: flex; align-items: center; justify-content: center; width: 16px; height: 16px; border: none; background: transparent; color: inherit; opacity: 0.7; cursor: pointer; }
.badge-remove:hover { opacity: 1; }

.badge-ativo { background: var(--badge-ativo-bg); color: var(--badge-ativo-text); }
.badge-manutencao { background: var(--badge-manutencao-bg); color: var(--badge-manutencao-text); }
.badge-baixa { background: var(--color-bg-tertiary); color: var(--color-text-secondary); }
.badge-critico { background: var(--badge-alerta-bg); color: var(--badge-alerta-text); }
.badge-info { background: var(--color-info-bg); color: var(--color-info); }
.badge-warning { background: var(--color-warning-bg); color: var(--color-warning); }
.badge-success { background: var(--color-success-bg); color: var(--color-success); }
.badge-neutral { background: var(--color-bg-secondary); color: var(--color-text-secondary); }
</style>
```

---

## 7. Integração com Design Tokens

### 7.1 Import Global (main.ts)

```typescript
// frontend/src/main.ts
import '@/styles/tokens.css';  // CSS Custom Properties
import '@/styles/global.css';  // Reset + base styles
```

### 7.2 Uso em Componentes

```vue
<!-- Exemplo: AtivoCard.vue -->
<script setup lang="ts">
import { tokens } from '@/utils/tokens';
import Badge from '@/components/ui/Badge.vue';
import Button from '@/components/ui/Button.vue';
import IconButton from '@/components/ui/IconButton.vue';

defineProps<{ ativo: Ativo }>();
</script>

<template>
  <div class="ativo-card" :style="cardStyle">
    <div class="header">
      <h3 class="title">{{ ativo.nome }}</h3>
      <Badge :variant="statusVariant" :label="ativo.status" />
    </div>
    <div class="meta">
      <code class="serial">{{ ativo.numeroSerie }}</code>
      <span class="filial">{{ ativo.filial.nome }}</span>
    </div>
    <div class="actions">
      <IconButton @click="openDetail" aria-label="Ver detalhes">
        <i class="bi bi-eye" />
      </IconButton>
      <IconButton @click="printQR" aria-label="Imprimir QR Code">
        <i class="bi bi-qr-code" />
      </IconButton>
    </div>
  </div>
</template>

<style scoped>
.ativo-card {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-subtle);
  border-radius: var(--radius-md);
  padding: var(--space-4);
  box-shadow: var(--shadow-sm);
  transition: box-shadow var(--duration-fast) var(--ease-standard);
}
.ativo-card:hover { box-shadow: var(--shadow-md); }
.header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: var(--space-3); }
.title { font-size: var(--text-lg); font-weight: 600; color: var(--color-text-primary); margin: 0; }
.meta { display: flex; flex-direction: column; gap: var(--space-1); margin-bottom: var(--space-3); }
.serial { font-family: var(--font-mono); font-size: var(--text-sm); color: var(--color-text-secondary); }
.filial { font-size: var(--text-sm); color: var(--color-text-secondary); }
.actions { display: flex; justify-content: flex-end; gap: var(--space-2); }
</style>
```

---

## 8. Testes de Componentes

| Tipo | Ferramenta | Cobertura Mínima | Comando |
| :--- | :--- | :--- | :--- |
| **Unitário** | Vitest + Vue Test Utils | 80% (linhas, branches, functions) | `npm run test:unit` |
| **Integração** | Cypress Component Testing | Fluxos críticos (Form submit, DataTable sort/filter) | `npm run test:component` |
| **Visual Regression** | Chromatic / Storybook | Todos componentes UI base | `npm run chromatic` |
| **Acessibilidade** | axe-core (Cypress) | 0 violações WCAG 2.1 AA | `npm run test:a11y` |

### Exemplo Teste Unitário (Button)

```typescript
// frontend/src/components/ui/Button.test.ts
import { mount } from '@vue/test-utils';
import Button from './Button.vue';
import { describe, it, expect, vi } from 'vitest';

describe('Button', () => {
  it('emite click quando clicado', async () => {
    const wrapper = mount(Button, { slots: { default: 'Salvar' } });
    await wrapper.trigger('click');
    expect(wrapper.emitted('click')).toBeTruthy();
  });

  it('não emite click quando disabled', async () => {
    const wrapper = mount(Button, { props: { disabled: true }, slots: { default: 'Salvar' } });
    await wrapper.trigger('click');
    expect(wrapper.emitted('click')).toBeFalsy();
  });

  it('aplica variant danger corretamente', () => {
    const wrapper = mount(Button, { props: { variant: 'danger' }, slots: { default: 'Excluir' } });
    expect(wrapper.classes()).toContain('btn-danger');
  });

  it('mostra spinner quando loading', () => {
    const wrapper = mount(Button, { props: { loading: true }, slots: { default: 'Salvar' } });
    expect(wrapper.find('.spinner').exists()).toBe(true);
  });
});
```

---

## 9. Storybook (Documentação Visual)

```typescript
// frontend/src/components/ui/Button.stories.ts
import type { Meta, StoryObj } from '@storybook/vue3';
import Button from './Button.vue';

const meta: Meta<typeof Button> = {
  title: 'UI/Button',
  component: Button,
  tags: ['autodocs'],
  argTypes: {
    variant: { control: 'select', options: ['primary', 'secondary', 'danger', 'ghost'] },
    size: { control: 'select', options: ['sm', 'md', 'lg'] },
    loading: { control: 'boolean' },
    disabled: { control: 'boolean' },
  },
};

export default meta;
type Story = StoryObj<typeof Button>;

export const Primary: Story = { args: { variant: 'primary', default: 'Salvar' } };
export const Secondary: Story = { args: { variant: 'secondary', default: 'Cancelar' } };
export const Danger: Story = { args: { variant: 'danger', default: 'Excluir' } };
export const Ghost: Story = { args: { variant: 'ghost', default: 'Voltar' } };
export const Loading: Story = { args: { loading: true, default: 'Salvando...' } };
export const IconOnly: Story = { args: { iconOnly: true, default: '<i class="bi bi-search" />' } };
```

---

## 10. Governance & Versionamento

| Regra | Descrição |
| :--- | :--- |
| **Versionamento** | Componentes UI base seguem semver independente (`@aegis/ui@1.x.x`); componentes de domínio versionados com app |
| **Breaking Changes** | Props removidas/renomeadas → major; novas props opcionais → minor; bugfix → patch |
| **Depreciação** | 2 versões minor de aviso + migration guide no CHANGELOG |
| **Design Tokens** | Mudança em token primitivo → major em `@aegis/ui` + comunicação 30 dias antes |
| **Code Owners** | `ui/` → Frontend Lead + Design; `forms/`, `data-display/` → Domain Team + Frontend; `layout/` → Frontend Lead |

---

## 11. Métricas de Qualidade

| Métrica | Target | Ferramenta |
| :--- | :--- | :--- |
| **Bundle size (UI base)** | < 50 kB gzipped | `vite-bundle-analyzer` |
| **Tree-shaking** | 100% (apenas componentes importados no bundle) | Rollup/Vite |
| **Acessibilidade** | 0 violações axe-core | Cypress + axe |
| **TypeScript strict** | `strict: true`, zero `any` | `tsc --noEmit` |
| **Test coverage** | ≥ 80% | Vitest + c8 |
| **Storybook coverage** | 100% componentes UI base | Chromatic |

---

## 12. Referências

- `design-tokens.md` — Tokens consumidos por todos componentes
- `ui-style-guide.md` — Diretrizes visuais e de uso
- `accessibility-guidelines.md` — Requisitos WCAG 2.1 AA
- `user-flows.md` — Fluxos que definem necessidades de componentes
- `system-architecture.md` — Stack frontend (Vue 3, Bootstrap 5, Pinia, Vite)
- `api-specification.md` — Contratos de API consumidos pelos forms/data-display

---

**Total de componentes:** 43 (21 UI base + 6 Data Display + 11 Forms + 5 Layout)
**Status:** 43/43 implementados (baseado no build `frontend/dist/` existente)
**Próximos passos:** Adicionar testes de regressão visual (Chromatic), documentar props no Storybook, migrar para biblioteca pública `@aegis/ui` se houver múltiplos frontends.