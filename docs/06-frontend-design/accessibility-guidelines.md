# Accessibility Guidelines — Aegis1

> **Versão:** 2.0 · **Owner:** Frontend/QA Lead · **Status:** Implemented
> **Nível-alvo:** WCAG 2.1 AA · **Base:** Design Tokens v2.0 + NFR v2.0 + Vue 3 + Bootstrap 5 + PWA
> **Validação:** axe-core (Cypress E2E) + Lighthouse CI + Manual Testing

---

## 1. Overview

O Aegis1 é um sistema B2B de gestão patrimonial e manutenção utilizado por:
- **Gestor de Manutenção/Patrônio** (desktop) — Dashboards, relatórios, configurações
- **Técnico de Campo** (mobile/PWA) — Health Check, QR Scanner, Ordens offline-first
- **Aprovador/Supervisor** (desktop) — Aprovação com evidências
- **Administrador** (desktop) — RBAC, Multi-tenancy, Auditoria, LGPD

**Acessibilidade não é opcional:** Técnicos operam em ambientes adversos (ruído, luz forte, luvas, limitações motoras temporárias); Gestores podem ter baixa visão ou depender de navegação por teclado.

**Conformidade Obrigatória:** WCAG 2.1 Nível AA (todas diretrizes A + AA) + Lei Brasileira de Inclusão (Lei 13.146/2015) + Decreto 10.098/2019 + LGPD Art. 9.

---

## 2. Contraste de Cor (WCAG 1.4.3, 1.4.11)

### 2.1 Tokens Validados (Design Tokens v2.0)

| Combinação | Contexto | Ratio Light | Ratio Dark | Status |
| :--- | :--- | :---: | :---: | :--- |
| `--color-text-primary` / `--color-bg-primary` | Texto principal | **12.6:1** | **12.6:1** | ✅ AAA |
| `--color-text-secondary` / `--color-bg-primary` | Texto secundário | **7.0:1** | **7.0:1** | ✅ AAA |
| `--color-text-tertiary` / `--color-bg-primary` | Placeholders, metadata | **4.5:1** | **4.5:1** | ✅ AA |
| `--color-accent` / `--color-bg-primary` | Links, botões primários | **5.9:1** | **5.9:1** | ✅ AA |
| `--color-border-light` / `--color-bg-primary` | Bordas UI (inputs, cards) | **3.2:1** | **3.2:1** | ✅ AA (UI) |
| `--color-border-focus` / `--color-bg-primary` | Focus ring | **4.5:1** | **4.5:1** | ✅ AA |
| `--color-success` / `--color-success-bg` | Badge "Concluída" | **5.2:1** | **5.2:1** | ✅ AA |
| `--color-warning` / `--color-warning-bg` | Badge "Em andamento" | **4.5:1** | **4.5:1** | ✅ AA (limite) |
| `--color-danger` / `--color-danger-bg` | Badge "Cancelada", erros | **5.5:1** | **5.5:1** | ✅ AA |
| `--color-info` / `--color-info-bg` | Badge "Aberta", tooltips | **5.1:1** | **5.1:1** | ✅ AA |

> **Validação Automatizada:** `axe-core` (Cypress) + Lighthouse CI em every PR. Falha se qualquer violação `critical`/`serious`.

### 2.2 Regras de Uso

- **Texto normal (≥ 16px):** Mínimo 4.5:1
- **Texto grande (≥ 18.5px bold / 24px normal):** Mínimo 3:1
- **Elementos UI (bordas, ícones, focus):** Mínimo 3:1
- **Modo Dark:** Mesmos ratios (tokens ajustados automaticamente via `.dark` class)

---

## 3. Navegação por Teclado (WCAG 2.1.1, 2.4.3, 2.4.7)

### 3.1 Regras Obrigatórias

| Regra | Implementação |
| :--- | :--- |
| **Ordem de tabulação lógica** | Ordem visual (topo→baixo, esquerda→direita); `tabindex` apenas 0 ou -1; nunca > 0 |
| **Foco visível** | `:focus-visible { outline: none; box-shadow: var(--shadow-focus); }` — **Nunca** `outline: none` sem `:focus-visible` |
| **Skip Link** | Primeiro elemento focável: `<a href="#main-content" class="skip-link">Pular para conteúdo principal</a>` (visível no foco) |
| **Trap de foco em modais** | `focus-trap` (VueUse `useFocusTrap`) — foco cicla dentro do modal; `Esc` fecha + restaura foco no gatilho |
| **Roving tabindex** | Em componentes compostos (tabs, menu, select, date picker): `tabindex="0"` no ativo, `-1` nos outros; setas navegam |

### 3.2 Atalhos de Teclado Globais

| Atalho | Ação | Escopo |
| :--- | :--- | :--- |
| `Tab` / `Shift+Tab` | Navegar elementos focáveis | Global |
| `Enter` / `Espaço` | Ativar botão, link, checkbox, radio | Global |
| `Esc` | Fechar modal, drawer, dropdown, toast | Global |
| `Setas` | Navegar dentro de componentes compostos | Componente |
| `Home` / `End` | Primeiro/último item | Tabelas, listas |
| `Ctrl/Cmd + K` | Busca global (Command Palette) | Global |
| `Ctrl/Cmd + /` | Ajuda / Atalhos | Global |

---

## 4. Suporte a Screen Readers (WCAG 1.3.1, 4.1.2)

### 4.1 HTML Semântico Obrigatório

```html
<!-- Landmarks -->
<header role="banner">        <!-- Header global -->
<nav role="navigation">       <!-- Navegação principal -->
<main role="main" id="main-content">  <!-- Conteúdo principal (target do skip link) -->
<aside role="complementary">  <!-- Sidebar / drawer -->
<footer role="contentinfo">   <!-- Footer -->

<!-- Headings Hierárquicos -->
<h1>Título da Página</h1>      <!-- Único por tela -->
<h2>Seção Principal</h2>
<h3>Subseção</h3>

<!-- Listas -->
<nav aria-label="Navegação principal">
  <ul>...</ul>
</nav>

<!-- Tabelas Acessíveis -->
<table>
  <caption>Ordens de Serviço — 12 itens, filtradas por "Aberta"</caption>
  <thead>
    <tr>
      <th scope="col">Número</th>
      <th scope="col">Ativo</th>
      <th scope="col">Status</th>
    </tr>
  </thead>
  <tbody>...</tbody>
</table>
```

### 4.2 Labels e Descrições

| Elemento | Regra | Exemplo Aegis1 |
| :--- | :--- | :--- |
| **Input** | `<label for="id">` visível + `id` no input | `<label for="ativo-tag">Tag do Ativo</label><input id="ativo-tag">` |
| **Ícone sem texto** | `aria-label` descritivo | `<button aria-label="Escanear QR Code do ativo"><QRCodeIcon /></button>` |
| **Ícone decorativo** | `aria-hidden="true"` + `focusable="false"` | `<IconCheck aria-hidden="true" focusable="false" />` |
| **Live Region** | `aria-live="polite"` para toasts; `aria-live="assertive"` para alertas críticos | `<div id="toast-container" aria-live="polite" aria-atomic="true">` |
| **Status/Estado** | `aria-pressed`, `aria-expanded`, `aria-selected`, `aria-disabled`, `aria-invalid` | `<button aria-pressed="true" aria-expanded="false">` |
| **Descrição Complexa** | `aria-describedby="id-da-descricao"` | `<input aria-describedby="help-ativo-tag" id="ativo-tag"><span id="help-ativo-tag">Código único do ativo (ex: NB-001)</span>` |

### 4.3 ARIA Patterns Implementados

| Componente | Pattern | Atributos-Chave |
| :--- | :--- | :--- |
| **Modal/Dialog** | `role="dialog"` + `aria-modal="true"` + `aria-labelledby` + `aria-describedby` | Focus trap, `Esc` close, focus restore |
| **Dropdown/Select** | `role="combobox"` + `aria-controls` + `aria-expanded` + `aria-activedescendant` | Roving tabindex, type-ahead |
| **Tabs** | `role="tablist"` + `role="tab"` + `aria-selected` + `aria-controls` + `role="tabpanel"` + `aria-labelledby` | Roving tabindex, keyboard navigation |
| **Menu** | `role="menu"` + `role="menuitem"` + `aria-orientation` | Roving tabindex, `Esc` close |
| **Toast/Alert** | `role="status"` (polite) / `role="alert"` (assertive) + `aria-live` + `aria-atomic` | Auto-dismiss, focus management |
| **Progress/Loading** | `role="progressbar"` + `aria-valuemin` + `aria-valuemax` + `aria-valuenow` + `aria-label` | Determinate/indeterminate |
| **Tree/Navigation** | `role="tree"` + `role="treeitem"` + `aria-expanded` + `aria-level` | Keyboard navigation |

---

## 5. Formulários Acessíveis (WCAG 3.3.1, 3.3.2, 3.3.3)

### 5.1 Estrutura Base

```vue
<FormField label="Tag do Ativo" for="ativo-tag" required>
  <template #help>
    Código único do ativo (ex: NB-001)
  </template>
  <template #error>
    Tag já cadastrada. Escolha outra.
  </template>
  <input 
    id="ativo-tag" 
    type="text" 
    v-model="form.tag" 
    :aria-invalid="!!errors.tag"
    :aria-describedby="errors.tag ? 'error-tag' : 'help-tag'"
    :aria-describedby="!errors.tag ? 'help-tag' : undefined"
  />
  <span id="help-tag" class="form-help">Código único do ativo (ex: NB-001)</span>
  <span id="error-tag" class="form-error" role="alert" v-if="errors.tag">{{ errors.tag }}</span>
</FormField>
```

### 5.2 Regras de Validação

| Regra | Implementação |
| :--- | :--- |
| **Required** | `required` + `aria-required="true"` + `aria-invalid="true"` no erro |
| **Error Message** | `role="alert"` + `aria-live="assertive"` + `aria-describedby` no input |
| **Success** | `aria-invalid="false"` (implícito) + ícone visual + `aria-describedby` opcional |
| **Formato** | `type="email"`, `type="tel"`, `pattern`, `inputmode` apropriados |
| **Autocomplete** | `autocomplete="email"`, `"tel"`, `"name"`, `"organization"`, `"street-address"` |

---

## 6. Touch & Mobile (WCAG 2.5.1, 2.5.5, 2.5.8)

### 6.1 Touch Targets (Mínimo 44×44px / Confortável 48×48px)

```scss
// Design Tokens aplicados
--touch-target-min: 44px;
--touch-target-comfortable: 48px;

// Aplicado em:
.btn { min-height: var(--touch-target-comfortable); min-width: var(--touch-target-comfortable); }
.form-input { min-height: var(--touch-target-comfortable); }
.dropdown-trigger { min-height: var(--touch-target-comfortable); }
.tab-trigger { min-height: var(--touch-target-comfortable); }
```

### 6.2 Gestos e Orientação

- **Nenhum gesto complexo obrigatório** (pinch, swipe, drag) — alternativas por teclado/botão
- **Orientação:** Portrait + Landscape suportados (responsivo)
- **Pull-to-refresh:** Suportado em listas (PWA) + botão "Atualizar" acessível

---

## 7. Motion & Animation (WCAG 2.3.3)

### 7.1 Respeito a `prefers-reduced-motion`

```scss
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }
}
```

### 7.2 Tokens de Duração Reduzida

```scss
:root {
  --duration-reduced: 0.01ms; // Effectively instant
}

// Aplicado automaticamente via @media (prefers-reduced-motion: reduce)
```

---

## 8. Internacionalização & Localização (WCAG 3.1.1, 3.1.2)

### 8.1 Idioma da Página

```html
<html lang="pt-BR">
```

### 8.2 Mudança de Idioma

```html
<!-- Se houver conteúdo em outro idioma -->
<span lang="en">Maintenance Order</span>
```

### 8.3 Formatação Localizada

```typescript
// Composables para formatação
const formatDate = (date: Date) => new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short' }).format(date);
const formatCurrency = (cents: number) => new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(cents / 100);
const formatNumber = (num: number) => new Intl.NumberFormat('pt-BR').format(num);
```

---

## 9. Error Handling & Feedback (WCAG 3.3.1, 3.3.3, 4.1.3)

### 9.1 Toast/Notification System

```vue
<!-- Toast Container (Live Region) -->
<div id="toast-container" aria-live="polite" aria-atomic="true" class="toast-container">
  <Toast v-for="toast in toasts" :key="toast.id" :toast="toast" />
</div>

<!-- Toast Component -->
<Toast :toast="toast" role="status" :aria-live="toast.type === 'error' ? 'assertive' : 'polite'" aria-atomic="true">
  <div :class="['toast', `toast--${toast.type}`]">
    <Icon :name="toast.icon" aria-hidden="true" />
    <span>{{ toast.message }}</span>
    <button @click="dismiss" aria-label="Fechar notificação">×</button>
  </div>
</Toast>
```

### 9.2 Inline Errors

```vue
<FormField :error="errors.email" :error-id="'error-email'">
  <input 
    :aria-invalid="!!errors.email" 
    :aria-describedby="errors.email ? 'error-email' : 'help-email'"
  />
  <span id="help-email" class="form-help">Seu email corporativo</span>
  <span id="error-email" class="form-error" role="alert" v-if="errors.email">{{ errors.email }}</span>
</FormField>
```

---

## 10. Testing & Validation

### 10.1 Automatizado (CI/CD)

| Ferramenta | Escopo | Gate |
| :--- | :--- | :--- |
| **axe-core (Cypress)** | Todas páginas E2E | Fail se violações `critical`/`serious` |
| **Lighthouse CI** | Build preview | Score A11y ≥ 95 |
| **axe-core (Storybook)** | Componentes isolados | Zero violações |
| **ESLint a11y plugin** | Código estático | `vuejs/accessibility` rules |

### 10.2 Testes Manuais (Checklist por Release)

| Teste | Ferramenta | Frequência |
| :--- | :--- | :--- |
| **Navegação só teclado** | Teclado only | Every Release |
| **Screen Reader (NVDA/JAWS/VoiceOver)** | NVDA (Win), VoiceOver (Mac/iOS) | Critical Flows |
| **Zoom 200%** | Browser zoom | Every Release |
| **High Contrast Mode** | OS High Contrast | Every Release |
| **Mobile Touch** | Device real (Android/iOS) | Every Release |
| **PWA Offline** | Chrome DevTools → Application | Every Release |

---

## 11. Component-Specific Guidelines

### 11.1 Data Tables

```vue
<table>
  <caption>Ordens de Serviço — {{ total }} itens</caption>
  <thead>
    <tr>
      <th scope="col"><ButtonIcon @click="sort('numero')" aria-label="Ordenar por Número">#</ButtonIcon></th>
      <th scope="col"><ButtonIcon @click="sort('ativo')" aria-label="Ordenar por Ativo">Ativo</ButtonIcon></th>
      <th scope="col"><ButtonIcon @click="sort('status')" aria-label="Ordenar por Status">Status</ButtonIcon></th>
    </tr>
  </thead>
  <tbody>
    <tr v-for="ordem in ordens" :key="ordem.id" :aria-selected="selectedIds.includes(ordem.id)">
      <td>{{ ordem.numero }}</td>
      <td>{{ ordem.ativo.tag }}</td>
      <td><Badge :variant="ordem.status">{{ ordem.status }}</Badge></td>
    </tr>
  </tbody>
</table>
```

### 11.2 Modals/Dialogs

```vue
<Teleport to="body">
  <Transition name="modal">
    <div v-if="open" class="modal-overlay" @click.self="close" role="dialog" aria-modal="true" :aria-labelledby="titleId" :aria-describedby="descId">
      <div class="modal-content" ref="contentRef">
        <header class="modal-header">
          <h2 id="titleId">{{ title }}</h2>
          <button @click="close" aria-label="Fechar modal" class="close-btn">×</button>
        </header>
        <div id="descId" class="modal-body"><slot /></div>
        <footer class="modal-footer"><slot name="footer" /></footer>
      </div>
    </div>
  </Transition>
</Teleport>
```

### 11.3 QR Scanner (PWA Mobile)

```vue
<QRScanner 
  @decode="onDecode" 
  aria-label="Escanear QR Code do ativo. Posicione o código dentro da moldura."
  :aria-busy="scanning"
>
  <template #instruction>
    <p>Posicione o QR Code do ativo dentro da área verde</p>
    <p class="sr-only">A câmera está ativa. Movimente o dispositivo até ouvir o bip de confirmação.</p>
  </template>
</QRScanner>
```

---

## 12. Validation Checklist (Definition of Done por Componente)

| Critério | Validação |
| :--- | :--- |
| **Contraste** | axe-core + Lighthouse ≥ 95 |
| **Teclado** | Navegação completa só teclado; focus visível; skip link; trap modal |
| **Screen Reader** | NVDA/VoiceOver: labels, landmarks, headings, live regions, ARIA |
| **Touch** | Targets ≥ 48px; gestos alternativos; orientação ambos |
| **Motion** | `prefers-reduced-motion` respeitado; animações essenciais apenas |
| **Zoom 200%** | Layout não quebra; texto legível; não horizontal scroll |
| **High Contrast** | Windows High Contrast / macOS Increase Contrast funcional |
| **Idioma** | `lang="pt-BR"`; mudanças de idioma marcadas; formatação localizada |
| **Erros** | Inline `role="alert"`; toast `aria-live`; `aria-invalid` + `aria-describedby` |
| **Formulários** | Labels associados; `autocomplete`; `aria-required`; `aria-invalid` |

---

## 13. Rastreabilidade Acessibilidade ↔ Artefatos

| Guideline | Design Tokens | UI Style Guide | Component Library | Interaction Patterns | Testes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Contraste** | `tokens.scss` (Seção 2) | `ui-style-guide.md#colors` | `Button.vue`, `Badge.vue`, `Input.vue` | — | axe-core, Lighthouse |
| **Teclado** | `tokens.scss` (focus ring) | `ui-style-guide.md#focus` | `Modal.vue`, `Dropdown.vue`, `Tabs.vue` | `interaction-patterns.md#keyboard` | Cypress keyboard nav |
| **Screen Reader** | — | `ui-style-guide.md#semantics` | `Modal.vue`, `Table.vue`, `Select.vue` | `interaction-patterns.md#aria` | NVDA/VoiceOver manual |
| **Touch/Mobile** | `tokens.scss` (touch targets) | `ui-style-guide.md#touch` | `Button.vue`, `QRScanner.vue` | `interaction-patterns.md#touch` | Device real testing |
| **Motion** | `tokens.scss` (reduced motion) | `interaction-patterns.md#animation` | `Transition.vue`, `Toast.vue` | `interaction-patterns.md#reduced-motion` | `prefers-reduced-motion` test |
| **Formulários** | `tokens.scss` (form tokens) | `ui-style-guide.md#forms` | `FormField.vue`, `Input.vue`, `Select.vue` | `interaction-patterns.md#forms` | axe-core forms |
| **Zoom/High Contrast** | `tokens.scss` (relative units) | `ui-style-guide.md#zoom` | All components | — | Manual 200% zoom test |

---

*Documento regenerado completamente com base em Design Tokens v2.0 (implementados) + Vue 3 + Bootstrap 5 + PWA + WCAG 2.1 AA. Substitui versão 1.0 que continha apenas especificações teóricas sem implementação.*