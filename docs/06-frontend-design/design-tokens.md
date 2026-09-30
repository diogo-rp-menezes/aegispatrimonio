# Design Tokens — Aegis Patrimônio

> **Versão:** 1.0 · **Owner:** Design/Frontend Lead · **Status:** Draft
> **Fonte de implementação:** **Nenhuma detectada no codebase atual** — o projeto não possui arquivo de tokens (tailwind.config.ts, tokens.json, styled-system theme, CSS custom properties centralizado). Este documento define a **estrutura obrigatória** a ser implementada antes de qualquer componente de UI.

---

## 1. Overview

Este documento estabelece a **fonte única da verdade** para todos os valores visuais primitivos usados na interface do Aegis Patrimônio. Atualmente, o frontend (Vue.js 3 + Bootstrap 5 + Pinia + Vite) **não possui sistema de design tokens** — estilos estão espalhados em arquivos CSS/JS não inventariados na varredura determinística.

**Regra obrigatória:** Nenhum componente deve usar valores "hardcoded" (hex, px, rem, ms) fora destes tokens. Toda adição/alteração passa por PR com aprovação de Design + Engenharia.

> **⚠️ [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Como não há tokens existentes, toda a tabela abaixo é **estrutura recomendada** baseada nos requisitos do BRD (acessibilidade, dois papéis Admin/User, modo escuro/claro para uso prolongado em operações, suporte a tooltips/modais via Popper.js). Valores reais **devem ser definidos pelo time de Design** antes da implementação.

---

## 2. Color Tokens

### 2.1 Cores Base (Primitivas)

| Token | Valor (Hex/RGB) | Uso Pretendido | Status |
| :--- | :--- | :--- | :--- |
| `--color-bg-primary` | **TO BE DEFINED** | Fundo principal da aplicação (listas, páginas) | ⬜ Pendente |
| `--color-bg-secondary` | **TO BE DEFINED** | Fundo de cards, painéis, sidebars | ⬜ Pendente |
| `--color-bg-tertiary` | **TO BE DEFINED** | Fundo de modais, dropdowns, tooltips (Popper) | ⬜ Pendente |
| `--color-border-subtle` | **TO BE DEFINED** | Divisores de tabela, inputs, cards | ⬜ Pendente |
| `--color-border-strong` | **TO BE DEFINED** | Foco de input, borda de erro, separadores de seção | ⬜ Pendente |
| `--color-text-primary` | **TO BE DEFINED** | Texto principal (títulos, corpo) — contraste WCAG AA ≥ 4.5:1 | ⬜ Pendente |
| `--color-text-secondary` | **TO BE DEFINED** | Texto secundário, metadados, placeholders — contraste ≥ 3:1 | ⬜ Pendente |
| `--color-text-inverse` | **TO BE DEFINED** | Texto sobre fundos escuros/coloridos (botões primários) | ⬜ Pendente |
| `--color-brand-primary` | **TO BE DEFINED** | Ações primárias (criar, aprovar, salvar), links, foco visível | ⬜ Pendente |
| `--color-brand-hover` | **TO BE DEFINED** | Hover de botões/links primários | ⬜ Pendente |
| `--color-brand-active` | **TO BE DEFINED** | Active/pressed de botões primários | ⬜ Pendente |

### 2.2 Cores Semânticas (Status & Feedback)

| Token | Valor | Mapeia para (Primitiva) | Uso | Status |
| :--- | :--- | :--- | :--- | :--- |
| `--color-success` | **TO BE DEFINED** | `--color-green-600` (sugestão) | Confirmações, manutenção concluída, health check OK | ⬜ Pendente |
| `--color-success-bg` | **TO BE DEFINED** | `--color-green-50` | Background de toast/badge sucesso | ⬜ Pendente |
| `--color-warning` | **TO BE DEFINED** | `--color-amber-600` | Alertas de recurso (disco/CPU/memória), manutenção pendente | ⬜ Pendente |
| `--color-warning-bg` | **TO BE DEFINED** | `--color-amber-50` | Background de alerta não lido (`listarAlertas`) | ⬜ Pendente |
| `--color-danger` | **TO BE DEFINED** | `--color-red-600` | Erros de validação, ações destrutivas (excluir ativo, cancelar) | ⬜ Pendente |
| `--color-danger-bg` | **TO BE DEFINED** | `--color-red-50` | Background de erro inline, toast erro | ⬜ Pendente |
| `--color-info` | **TO BE DEFINED** | `--color-blue-600` | Mensagens informativas, tooltips Popper, ajuda contextual | ⬜ Pendente |
| `--color-info-bg` | **TO BE DEFINED** | `--color-blue-50` | Background de info banner | ⬜ Pendente |

### 2.3 Cores de Papel (Role-based) — **Específico do Aegis**

| Token | Valor | Uso | Status |
| :--- | :--- | :--- | :--- |
| `--color-role-admin` | **TO BE DEFINED** | Badge/indicador visual de usuário Admin (criar/editar/excluir mestres) | ⬜ Pendente |
| `--color-role-user` | **TO BE DEFINED** | Badge/indicador visual de usuário User (somente leitura + solicitar manutenção) | ⬜ Pendente |
| `--color-permission-allowed` | **TO BE DEFINED** | Ícone/check de ação permitida (`hasPermission` true) | ⬜ Pendente |
| `--color-permission-denied` | **TO BE DEFINED** | Ícone/lock de ação negada (`hasPermission` false / 403) | ⬜ Pendente |

### 2.4 Modos (Light / Dark)

> **Estratégia de troca de tema:** Classe `.theme-dark` no `<html>` (toggle manual persistido em `localStorage` + respeito a `prefers-color-scheme` como default). **Não há implementação atual.**

| Token | Light Mode | Dark Mode | Status |
| :--- | :--- | :--- | :--- |
| `--color-bg-primary` | `#FFFFFF` (sugestão) | `#1A1A2E` (sugestão) | ⬜ Pendente |
| `--color-bg-secondary` | `#F8F9FA` | `#16213E` | ⬜ Pendente |
| `--color-bg-tertiary` | `#FFFFFF` | `#0F3460` | ⬜ Pendente |
| `--color-border-subtle` | `#DEE2E6` | `#2D3A4F` | ⬜ Pendente |
| `--color-border-strong` | `#ADB5BD` | `#4A5A7A` | ⬜ Pendente |
| `--color-text-primary` | `#212529` | `#E9ECEF` | ⬜ Pendente |
| `--color-text-secondary` | `#6C757D` | `#ADB5BD` | ⬜ Pendente |
| `--color-text-inverse` | `#FFFFFF` | `#FFFFFF` | ⬜ Pendente |
| `--color-brand-primary` | `#0D6EFD` (Bootstrap blue) | `#6EA8FE` | ⬜ Pendente |
| `--color-success` | `#198754` | `#75B798` | ⬜ Pendente |
| `--color-warning` | `#FFC107` | `#FFD64D` | ⬜ Pendente |
| `--color-danger` | `#DC3545` | `#EA868F` | ⬜ Pendente |
| `--color-info` | `#0DCAF0` | `#6EDFF6` | ⬜ Pendente |

> **Nota:** Valores acima são **placeholders sugeridos** baseados em paletas acessíveis comuns. **Devem ser validados/substituídos pelo Design** com testes de contraste reais (WCAG 2.1 AA).

---

## 3. Typography

### 3.1 Font Families

| Token | Fonte | Fallback Stack | Uso | Status |
| :--- | :--- | :--- | :--- | :--- |
| `--font-sans` | **TO BE DEFINED** (ex: `Inter`, `IBM Plex Sans`, `Roboto`) | `system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif` | Todo texto de UI, tabelas, formulários, corpo | ⬜ Pendente |
| `--font-mono` | **TO BE DEFINED** (ex: `JetBrains Mono`, `Fira Code`, `SF Mono`) | `ui-monospace, "SF Mono", Menlo, Monaco, Consolas, monospace` | Códigos de ativo, IPs, serial numbers, health check values, JSON em modais | ⬜ Pendente |

> **Requisito BRD:** O sistema lida com dados técnicos (IPs, MAC addresses, serial numbers, hardware specs) — **fonte monoespaçada é obrigatória** para esses campos.

### 3.2 Type Scale

| Token | Tamanho (rem) | Line Height | Peso | Uso | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `--text-xs` | `0.75rem` (12px) | `1.5` | `400` | Labels de tabela, metadados, timestamps, badges | ⬜ Pendente |
| `--text-sm` | `0.875rem` (14px) | `1.5` | `400` | Corpo secundário, descrições, tooltips Popper | ⬜ Pendente |
| `--text-base` | `1rem` (16px) | `1.6` | `400` | **Corpo principal** — inputs, tabelas, texto corrido | ⬜ Pendente |
| `--text-lg` | `1.125rem` (18px) | `1.5` | `500` | Subtítulos de seção, cards de ativo | ⬜ Pendente |
| `--text-xl` | `1.25rem` (20px) | `1.4` | `600` | Títulos de página, modais | ⬜ Pendente |
| `--text-2xl` | `1.5rem` (24px) | `1.3` | `700` | Hero/dashboard title | ⬜ Pendente |
| `--text-3xl` | `1.875rem` (30px) | `1.2` | `700` | Título de relatório/exportação | ⬜ Pendente |

---

## 4. Spacing & Sizing

### 4.1 Escala de Espaçamento (Base 4px / 0.25rem)

| Token | Valor (rem) | Valor (px) | Status |
| :--- | :--- | :--- | :--- |
| `--space-1` | `0.25rem` | `4px` | ⬜ Pendente |
| `--space-2` | `0.5rem` | `8px` | ⬜ Pendente |
| `--space-3` | `0.75rem` | `12px` | ⬜ Pendente |
| `--space-4` | `1rem` | `16px` | ⬜ Pendente |
| `--space-5` | `1.25rem` | `20px` | ⬜ Pendente |
| `--space-6` | `1.5rem` | `24px` | ⬜ Pendente |
| `--space-8` | `2rem` | `32px` | ⬜ Pendente |
| `--space-10` | `2.5rem` | `40px` | ⬜ Pendente |
| `--space-12` | `3rem` | `48px` | ⬜ Pendente |
| `--space-16` | `4rem` | `64px` | ⬜ Pendente |

### 4.2 Border Radius

| Token | Valor | Uso | Status |
| :--- | :--- | :--- | :--- |
| `--radius-none` | `0` | Elementos quadrados (badges, tags) | ⬜ Pendente |
| `--radius-sm` | `0.25rem` (4px) | Inputs, botões, badges | ⬜ Pendente |
| `--radius-md` | `0.375rem` (6px) | Cards, modais, dropdowns | ⬜ Pendente |
| `--radius-lg` | `0.5rem` (8px) | Modais grandes, painéis | ⬜ Pendente |
| `--radius-xl` | `0.75rem` (12px) | Containers principais | ⬜ Pendente |
| `--radius-full` | `9999px` | Pills, avatares, badges circulares | ⬜ Pendente |

### 4.3 Breakpoints (Responsivo — Mobile-first)

| Token | Valor | Uso | Status |
| :--- | :--- | :--- | :--- |
| `--bp-sm` | `640px` | Tablet portrait / mobile landscape | ⬜ Pendente |
| `--bp-md` | `768px` | Tablet landscape | ⬜ Pendente |
| `--bp-lg` | `1024px` | Desktop pequeno | ⬜ Pendente |
| `--bp-xl` | `1280px` | Desktop padrão | ⬜ Pendente |
| `--bp-2xl` | `1536px` | Desktop grande / wide | ⬜ Pendente |

> **Requisito BRD (NFR-08):** Interface responsiva desktop/tablet, **mobile-first para solicitações** de manutenção.

---

## 5. Shadows & Elevation

| Token | Valor (CSS box-shadow) | Uso | Status |
| :--- | :--- | :--- | :--- |
| `--shadow-none` | `none` | Reset | ⬜ Pendente |
| `--shadow-sm` | `0 1px 2px 0 rgb(0 0 0 / 0.05)` | Cards sutis, inputs focus | ⬜ Pendente |
| `--shadow-md` | `0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)` | Cards padrão, dropdowns | ⬜ Pendente |
| `--shadow-lg` | `0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1)` | Modais, sidebars | ⬜ Pendente |
| `--shadow-xl` | `0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1)` | Modais full-screen, drawers | ⬜ Pendente |
| `--shadow-focus` | `0 0 0 3px var(--color-brand-primary) / 0.4` | Focus ring acessível (WCAG) | ⬜ Pendente |

---

## 6. Motion & Transitions

| Token | Valor | Uso | Status |
| :--- | :--- | :--- | :--- |
| `--duration-fast` | `100ms` | Hover, focus, micro-interações | ⬜ Pendente |
| `--duration-normal` | `200ms` | Transições padrão (modais, dropdowns, tabs) | ⬜ Pendente |
| `--duration-slow` | `300ms` | Animações complexas (sidebars, drawers) | ⬜ Pendente |
| `--ease-standard` | `cubic-bezier(0.4, 0, 0.2, 1)` | Padrão Material/Google | ⬜ Pendente |
| `--ease-emphasized` | `cubic-bezier(0.2, 0, 0, 1)` | Entrada de modais, toasts | ⬜ Pendente |
| `--ease-decelerate` | `cubic-bezier(0, 0, 0.2, 1)` | Saída de modais, toasts | ⬜ Pendente |

> **Requisito:** Respeitar `prefers-reduced-motion: reduce` — desabilitar animações não essenciais.

---

## 7. Z-Index Scale

| Token | Valor | Uso | Status |
| :--- | :--- | :--- | :--- |
| `--z-base` | `0` | Conteúdo normal | ⬜ Pendente |
| `--z-dropdown` | `100` | Dropdowns, selects | ⬜ Pendente |
| `--z-sticky` | `200` | Headers fixos, sidebars | ⬜ Pendente |
| `--z-modal-backdrop` | `400` | Overlay de modais | ⬜ Pendente |
| `--z-modal` | `500` | Modais, drawers | ⬜ Pendente |
| `--z-popover` | `600` | Popovers, tooltips (Popper.js) | ⬜ Pendente |
| `--z-toast` | `700` | Toasts, notificações | ⬜ Pendente |
| `--z-tooltip` | `800` | Tooltips nativos | ⬜ Pendente |

---

## 8. Component-Specific Tokens (Exemplos)

> **Nota:** Tokens específicos de componente derivam dos primitivos acima. Exemplos para componentes críticos do Aegis:

### 8.1 Tabela de Ativos (Lista principal)

| Token | Derivação | Status |
| :--- | :--- | :--- |
| `--table-header-bg` | `var(--color-bg-secondary)` | ⬜ Pendente |
| `--table-row-hover-bg` | `var(--color-bg-tertiary)` | ⬜ Pendente |
| `--table-row-striped-bg` | `var(--color-bg-secondary) / 0.5` | ⬜ Pendente |
| `--table-border-color` | `var(--color-border-subtle)` | ⬜ Pendente |
| `--table-cell-padding` | `var(--space-3) var(--space-4)` | ⬜ Pendente |
| `--table-font-size` | `var(--text-sm)` | ⬜ Pendente |

### 8.2 Formulários (Cadastro Ativo, Manutenção, Entidades)

| Token | Derivação | Status |
| :--- | :--- | :--- |
| `--input-height` | `2.5rem` (40px) | ⬜ Pendente |
| `--input-padding` | `0 var(--space-3)` | ⬜ Pendente |
| `--input-border-color` | `var(--color-border-subtle)` | ⬜ Pendente |
| `--input-border-focus` | `var(--color-brand-primary)` | ⬜ Pendente |
| `--input-error-border` | `var(--color-danger)` | ⬜ Pendente |
| `--input-bg` | `var(--color-bg-primary)` | ⬜ Pendente |
| `--label-font-size` | `var(--text-sm)` | ⬜ Pendente |
| `--label-font-weight` | `500` | ⬜ Pendente |
| `--help-text-font-size` | `var(--text-xs)` | ⬜ Pendente |

### 8.3 Botões (Primário, Secundário, Perigo, Ghost)

| Token | Derivação | Status |
| :--- | :--- | :--- |
| `--btn-height` | `2.5rem` (40px) | ⬜ Pendente |
| `--btn-padding-x` | `var(--space-4)` | ⬜ Pendente |
| `--btn-font-size` | `var(--text-sm)` | ⬜ Pendente |
| `--btn-font-weight` | `500` | ⬜ Pendente |
| `--btn-primary-bg` | `var(--color-brand-primary)` | ⬜ Pendente |
| `--btn-primary-hover` | `var(--color-brand-hover)` | ⬜ Pendente |
| `--btn-primary-text` | `var(--color-text-inverse)` | ⬜ Pendente |
| `--btn-danger-bg` | `var(--color-danger)` | ⬜ Pendente |
| `--btn-danger-hover` | `var(--color-danger) / 0.9` | ⬜ Pendente |
| `--btn-ghost-bg` | `transparent` | ⬜ Pendente |
| `--btn-ghost-hover` | `var(--color-bg-secondary)` | ⬜ Pendente |

### 8.4 Badges de Status (Ativo, Manutenção, Alerta)

| Token | Derivação | Status |
| :--- | :--- | :--- |
| `--badge-padding` | `var(--space-1) var(--space-2)` | ⬜ Pendente |
| `--badge-font-size` | `var(--text-xs)` | ⬜ Pendente |
| `--badge-font-weight` | `600` | ⬜ Pendente |
| `--badge-radius` | `var(--radius-full)` | ⬜ Pendente |
| `--badge-ativo-bg` | `var(--color-success-bg)` | ⬜ Pendente |
| `--badge-ativo-text` | `var(--color-success)` | ⬜ Pendente |
| `--badge-manutencao-bg` | `var(--color-warning-bg)` | ⬜ Pendente |
| `--badge-manutencao-text` | `var(--color-warning)` | ⬜ Pendente |
| `--badge-alerta-bg` | `var(--color-danger-bg)` | ⬜ Pendente |
| `--badge-alerta-text` | `var(--color-danger)` | ⬜ Pendente |

### 8.5 QR Code / Etiquetas (Geração de PDF)

| Token | Derivação | Status |
| :--- | :--- | :--- |
| `--qrcode-size` | `2rem` (32px) | ⬜ Pendente |
| `--qrcode-quiet-zone` | `var(--space-2)` | ⬜ Pendente |
| `--label-font-family` | `var(--font-mono)` | ⬜ Pendente |
| `--label-font-size` | `var(--text-xs)` | ⬜ Pendente |

---

## 9. Implementation Guide

### 9.1 Estrutura de Arquivos Recomendada

```
frontend/
├── src/
│   ├── styles/
│   │   ├── tokens.css          # CSS Custom Properties (esta definição)
│   │   ├── tokens.json         # Export JSON para consumo JS/TS (Style Dictionary)
│   │   ├── global.css          # Reset + import tokens.css
│   │   └── themes/
│   │       ├── light.css       # Overrides light mode
│   │       └── dark.css        # Overrides dark mode
│   ├── utils/
│   │   └── tokens.ts           # Type-safe accessors (ex: tokens.color.brand.primary)
│   └── components/
│       └── ...                 # Componentes usam var(--token) ou tokens.color.xxx
```

### 9.2 CSS Custom Properties (tokens.css)

```css
/* frontend/src/styles/tokens.css */
:root {
  /* Color - Primitive */
  --color-bg-primary: #FFFFFF;
  --color-bg-secondary: #F8F9FA;
  --color-bg-tertiary: #FFFFFF;
  --color-border-subtle: #DEE2E6;
  --color-border-strong: #ADB5BD;
  --color-text-primary: #212529;
  --color-text-secondary: #6C757D;
  --color-text-inverse: #FFFFFF;
  --color-brand-primary: #0D6EFD;
  --color-brand-hover: #0B5ED7;
  --color-brand-active: #0A58CA;

  /* Color - Semantic */
  --color-success: #198754;
  --color-success-bg: #D1E7DD;
  --color-warning: #FFC107;
  --color-warning-bg: #FFF3CD;
  --color-danger: #DC3545;
  --color-danger-bg: #F8D7DA;
  --color-info: #0DCAF0;
  --color-info-bg: #CFEFFE;

  /* Color - Role-based */
  --color-role-admin: #6F42C1;
  --color-role-user: #0D6EFD;
  --color-permission-allowed: #198754;
  --color-permission-denied: #DC3545;

  /* Typography */
  --font-sans: "Inter", system-ui, -apple-system, sans-serif;
  --font-mono: "JetBrains Mono", ui-monospace, monospace;

  --text-xs: 0.75rem;
  --text-sm: 0.875rem;
  --text-base: 1rem;
  --text-lg: 1.125rem;
  --text-xl: 1.25rem;
  --text-2xl: 1.5rem;
  --text-3xl: 1.875rem;

  /* Spacing */
  --space-1: 0.25rem;
  --space-2: 0.5rem;
  --space-3: 0.75rem;
  --space-4: 1rem;
  --space-5: 1.25rem;
  --space-6: 1.5rem;
  --space-8: 2rem;
  --space-10: 2.5rem;
  --space-12: 3rem;
  --space-16: 4rem;

  /* Border Radius */
  --radius-none: 0;
  --radius-sm: 0.25rem;
  --radius-md: 0.375rem;
  --radius-lg: 0.5rem;
  --radius-xl: 0.75rem;
  --radius-full: 9999px;

  /* Shadows */
  --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
  --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
  --shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1);
  --shadow-xl: 0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1);
  --shadow-focus: 0 0 0 3px var(--color-brand-primary) / 0.4;

  /* Motion */
  --duration-fast: 100ms;
  --duration-normal: 200ms;
  --duration-slow: 300ms;
  --ease-standard: cubic-bezier(0.4, 0, 0.2, 1);
  --ease-emphasized: cubic-bezier(0.2, 0, 0, 1);
  --ease-decelerate: cubic-bezier(0, 0, 0.2, 1);

  /* Z-Index */
  --z-base: 0;
  --z-dropdown: 100;
  --z-sticky: 200;
  --z-modal-backdrop: 400;
  --z-modal: 500;
  --z-popover: 600;
  --z-toast: 700;
  --z-tooltip: 800;
}

/* Dark mode overrides */
.theme-dark {
  --color-bg-primary: #1A1A2E;
  --color-bg-secondary: #16213E;
  --color-bg-tertiary: #0F3460;
  --color-border-subtle: #2D3A4F;
  --color-border-strong: #4A5A7A;
  --color-text-primary: #E9ECEF;
  --color-text-secondary: #ADB5BD;
  --color-brand-primary: #6EA8FE;
  --color-brand-hover: #8BB4FE;
  --color-brand-active: #4D94FE;
  --color-success: #75B798;
  --color-warning: #FFD64D;
  --color-danger: #EA868F;
  --color-info: #6EDFF6;
}
```

### 9.3 TypeScript Accessors (tokens.ts)

```typescript
// frontend/src/utils/tokens.ts
export const tokens = {
  color: {
    bg: {
      primary: 'var(--color-bg-primary)',
      secondary: 'var(--color-bg-secondary)',
      tertiary: 'var(--color-bg-tertiary)',
    },
    border: {
      subtle: 'var(--color-border-subtle)',
      strong: 'var(--color-border-strong)',
    },
    text: {
      primary: 'var(--color-text-primary)',
      secondary: 'var(--color-text-secondary)',
      inverse: 'var(--color-text-inverse)',
    },
    brand: {
      primary: 'var(--color-brand-primary)',
      hover: 'var(--color-brand-hover)',
      active: 'var(--color-brand-active)',
    },
    semantic: {
      success: 'var(--color-success)',
      successBg: 'var(--color-success-bg)',
      warning: 'var(--color-warning)',
      warningBg: 'var(--color-warning-bg)',
      danger: 'var(--color-danger)',
      dangerBg: 'var(--color-danger-bg)',
      info: 'var(--color-info)',
      infoBg: 'var(--color-info-bg)',
    },
    role: {
      admin: 'var(--color-role-admin)',
      user: 'var(--color-role-user)',
      permissionAllowed: 'var(--color-permission-allowed)',
      permissionDenied: 'var(--color-permission-denied)',
    },
  },
  font: {
    sans: 'var(--font-sans)',
    mono: 'var(--font-mono)',
  },
  text: {
    xs: 'var(--text-xs)',
    sm: 'var(--text-sm)',
    base: 'var(--text-base)',
    lg: 'var(--text-lg)',
    xl: 'var(--text-xl)',
    '2xl': 'var(--text-2xl)',
    '3xl': 'var(--text-3xl)',
  },
  space: {
    1: 'var(--space-1)',
    2: 'var(--space-2)',
    3: 'var(--space-3)',
    4: 'var(--space-4)',
    5: 'var(--space-5)',
    6: 'var(--space-6)',
    8: 'var(--space-8)',
    10: 'var(--space-10)',
    12: 'var(--space-12)',
    16: 'var(--space-16)',
  },
  radius: {
    none: 'var(--radius-none)',
    sm: 'var(--radius-sm)',
    md: 'var(--radius-md)',
    lg: 'var(--radius-lg)',
    xl: 'var(--radius-xl)',
    full: 'var(--radius-full)',
  },
  shadow: {
    sm: 'var(--shadow-sm)',
    md: 'var(--shadow-md)',
    lg: 'var(--shadow-lg)',
    xl: 'var(--shadow-xl)',
    focus: 'var(--shadow-focus)',
  },
  duration: {
    fast: 'var(--duration-fast)',
    normal: 'var(--duration-normal)',
    slow: 'var(--duration-slow)',
  },
  ease: {
    standard: 'var(--ease-standard)',
    emphasized: 'var(--ease-emphasized)',
    decelerate: 'var(--ease-decelerate)',
  },
  zIndex: {
    base: 'var(--z-base)',
    dropdown: 'var(--z-dropdown)',
    sticky: 'var(--z-sticky)',
    modalBackdrop: 'var(--z-modal-backdrop)',
    modal: 'var(--z-modal)',
    popover: 'var(--z-popover)',
    toast: 'var(--z-toast)',
    tooltip: 'var(--z-tooltip)',
  },
} as const;

export type Tokens = typeof tokens;
```

### 9.4 Uso em Componentes Vue

```vue
<!-- Exemplo: AtivoCard.vue -->
<script setup lang="ts">
import { tokens } from '@/utils/tokens';
defineProps<{ ativo: Ativo }>();
</script>

<template>
  <div class="ativo-card" :style="cardStyle">
    <span class="badge" :style="badgeStyle">{{ ativo.status }}</span>
    <code class="serial" :style="serialStyle">{{ ativo.numeroSerie }}</code>
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
.ativo-card:hover {
  box-shadow: var(--shadow-md);
}
.badge {
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: 600;
}
.serial {
  font-family: var(--font-mono);
  font-size: var(--text-sm);
}
</style>
```

---

## 10. Governance & Change Process

| Etapa | Descrição | Responsável |
| :--- | :--- | :--- |
| **Proposta** | Novo token ou alteração via PR com justificativa (acessibilidade, novo componente, feedback usuário) | Qualquer dev/designer |
| **Revisão Design** | Validação visual, contraste WCAG 2.1 AA, consistência com sistema | Design Lead |
| **Revisão Engenharia** | Impacto em componentes existentes, breaking changes, migração | Frontend Lead |
| **Aprovação** | Ambos aprovam → merge para `main` | Design + Eng Lead |
| **Distribuição** | Atualização de `tokens.json` + `tokens.css` + `tokens.ts` em release patch | CI/CD automatizado |
| **Documentação** | Changelog em `CHANGELOG.md` + atualização deste documento | Autor do PR |

> **Regra:** Tokens **primitivos** (cores base, espaçamento, tipografia) são **imutáveis após v1.0** — apenas adição. Tokens **semânticos/componentes** podem evoluir com versionamento semântico.

---

## 11. Accessibility Checklist (Tokens)

- [ ] Todos pares de cor texto/fundo ≥ **4.5:1** (WCAG AA) — texto normal
- [ ] Todos pares de cor texto/fundo ≥ **3:1** (WCAG AA) — texto grande (≥ 18px ou 14px bold)
- [ ] Focus ring visível (`--shadow-focus`) em **todos** elementos interativos
- [ ] Estados hover/active/focus/disabled distintos para botões, inputs, links
- [ ] Modo escuro testado com `prefers-color-scheme: dark` + toggle manual
- [ ] `prefers-reduced-motion: reduce` desabilita `--duration-*` não essenciais
- [ ] Fonte monoespaçada (`--font-mono`) para dados técnicos (IP, MAC, serial, JSON)
- [ ] Tamanho mínimo de toque **44×44px** (mobile) — `--btn-height` ≥ 44px

---

## 12. Referências

- `ui-style-guide.md` — Diretrizes de uso dos tokens em componentes
- `component-library.md` — Inventário de componentes que consomem tokens
- `accessibility-guidelines.md` — Requisitos WCAG 2.1 AA detalhados
- `system-architecture.md` §3 — Frontend stack (Vue 3, Bootstrap 5, Vite)
- `BRD` NFR-08 — Usabilidade responsiva, mobile-first para solicitações
- `ADR-001` — Monolito único: frontend servido estaticamente pelo Spring Boot

---

**Próximos passos:**
1. **Design Team** define valores reais para todos tokens "TO BE DEFINED"
2. **Frontend Lead** implementa `tokens.css` + `tokens.ts` + integração no Vite
3. **Engenharia** refatora componentes existentes para usar tokens (eliminar hardcoded values)
4. **CI Gate** adiciona verificação: `grep -r "#[0-9A-Fa-f]\{3,8\}" frontend/src --include="*.vue" --include="*.css" --include="*.ts"` deve retornar vazio (exceto tokens.css)