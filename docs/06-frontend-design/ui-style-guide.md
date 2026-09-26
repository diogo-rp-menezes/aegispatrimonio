# UI Style Guide — Aegis1

> **Versão:** 2.0 · **Owner:** Design/Frontend Lead · **Status:** Implemented
> **Base:** Design Tokens v2.0 (Implementados) + Component Library v2.0 + Accessibility Guidelines v2.0 + Vue 3 + Bootstrap 5 + PWA
> **Status:** Tokens implementados em `frontend/src/styles/tokens.scss` + `variables.scss`; Componentes em `frontend/src/components/`

---

## 1. Overview & Princípios de Design

O **Aegis1** é um sistema B2B de gestão patrimonial e manutenção industrial/facilities. A identidade visual prioriza **clareza sobre densidade**, **funcionalidade sobre decoração** e **acessibilidade como requisito não negociável (WCAG 2.1 AA)**.

### Princípios Norteadores (Implementados)

| Princípio | Descrição | Implementação |
| :--- | :--- | :--- |
| **Dados primeiro** | Interface serve tabelas, listas, formulários densos; ornamentação compete com informação | Cores semânticas apenas para estado; zero hardcoded values |
| **Consistência via Tokens** | Um token, um propósito. Variações pontuais criam dívida técnica | Todos valores visuais via `tokens.scss` + `variables.scss`; zero hardcoded |
| **Dark Mode Nativo** | Técnicos operam em iluminação variável | Tokens definem light/dark simultaneamente; troca via `.dark` class no `<html>` |
| **Movimento Funcional** | Animação só para feedback/transição | `prefers-reduced-motion` respeitado; tokens em `tokens.scss` §6 |
| **Acessibilidade por Padrão** | WCAG 2.1 AA mínimo; foco visível nunca removido | `--shadow-focus` obrigatório; contraste validado em CI (axe-core + Lighthouse) |

---

## 2. Voz Visual & Brand

### 2.1 Personalidade
- **Técnica, confiável, minimalista, sem ruído visual**
- **Referências:** Interfaces ERP/industriais modernas (Linear, GitHub, Vercel Dashboard) — densidade alta, tipografia system-ui, cores restritas a acento azul + semânticas

### 2.2 O que Evitar
- Gradientes decorativos, sombras pesadas, bordas arredondadas excessivas (`radius-lg` só em painéis flutuantes/modais)
- Cores fora da paleta semântica (verde/âmbar/vermelho/azul apenas para estado)
- Ícones sem label acessível ou tooltip
- Animações decorativas (spinners de página, transições de entrada de elementos estáticos)

---

## 3. Layout & Grid System

### 3.1 Grid System (Bootstrap 5 + CSS Grid)

| Aspecto | Definição | Token/Origem |
| :--- | :--- | :--- |
| **Container Max Width** | `1320px` (xxl) / `1140px` (xl) / `960px` (lg) / `720px` (md) / `540px` (sm) | `tokens.scss` §8 + Bootstrap `$container-max-widths` |
| **Grid Base** | 4px (`--space-1`) — todos espaçamentos múltiplos de 4px | `tokens.scss` §4.1 |
| **Colunas** | 12 colunas fluidas (Bootstrap Grid) + CSS Grid para layouts complexos | Bootstrap 5 Grid + CSS Grid |
| **Gutter Padrão** | `--space-6` (24px) entre colunas layout; `--space-4` (16px) entre cards/itens | `tokens.scss` §4.1 |
| **Padding Página** | `--space-6` (mobile) → `--space-8` (tablet) → `--space-10` (desktop) | `tokens.scss` §4.2 + Breakpoints |

### 3.2 Breakpoints (Mobile First)

```scss
// tokens.scss §8 + Bootstrap $grid-breakpoints
--breakpoint-xs: 0;
--breakpoint-sm: 576px;   // ≥ 576px (mobile landscape / small tablet)
--breakpoint-md: 768px;   // ≥ 768px (tablet)
--breakpoint-lg: 992px;   // ≥ 992px (desktop)
--breakpoint-xl: 1200px;  // ≥ 1200px (large desktop)
--breakpoint-xxl: 1400px; // ≥ 1400px (ultrawide)
```

### 3.3 Container & Spacing Patterns

| Contexto | Container | Padding | Gap |
| :--- | :--- | :--- | :--- |
| **Página Principal** | `.container-fluid` (full) ou `.container-xxl` (max) | `--space-6` (mobile) → `--space-8` (md) → `--space-10` (lg+) | `--space-6` entre seções |
| **Dashboard** | `.container-xxl` | `--space-6` → `--space-8` | `--space-6` entre cards KPI; `--space-4` entre widgets |
| **Tabelas/DataTable** | Full width (`.container-fluid`) | `--space-4` horizontal | `--space-1` vertical (rows); `--space-2` horizontal (cells) |
| **Formulários** | `.container-md` (max 720px) | `--space-6` vertical | `--space-3` label→input; `--space-6` entre seções |
| **Modais/Drawers** | Max-width por size (`sm`: 400px, `md`: 600px, `lg`: 800px, `xl`: 1000px, `full`: 90vw) | `--space-6` → `--space-8` | `--space-4` entre seções |
| **Mobile** | Full width (100vw) | `--space-4` → `--space-6` | `--space-4` entre blocos |

### 3.4 Padrões de Página (Page Patterns)

| Tipo | Estrutura | Exemplo |
| :--- | :--- | :--- |
| **Lista/Tabela** | `Header (título + ações) → Toolbar (busca + filtros) → DataTable → Pagination` | Ativos, Ordens, Cadastros |
| **Detalhe/Drill-down** | `Breadcrumb → Header (título + ações) → Tabs/Accordion (seções) → Conteúdo` | Detalhe Ativo, Detalhe Ordem |
| **Formulário/Wizard** | `Header (título + fechar) → Stepper (progresso) → StepContent (form) → Footer (navegação)` | Criar Ordem, Criar Ativo |
| **Dashboard/Resumo** | `Grid KPIs → Charts/Tables → Filtros laterais (Drawer mobile)` | Dashboard Principal, Preditiva |
| **Modal/Overlay** | `Backdrop → Container (Header + Content + Footer) → Focus trap` | Confirmações, Formulários, Evidências |
| **Detalhe Mobile (PWA)** | `Header fixo → Conteúdo scroll → Actions fixas bottom` | Health Check, QR Scanner |

---

## 4. Color System (Design Tokens v2.0 - Seção 2)

### 4.1 Hierarquia Visual via Cor

| Camada | Tokens | Uso |
| :--- | :--- | :--- |
| **Primária (Ação)** | `--color-primary*` (50-900) | Botões primários, links, focus ring, badges info |
| **Semântica (Estado)** | `--color-success*`, `--color-warning*`, `--color-danger*`, `--color-info*` | Badges estado (ordem, prioridade), toasts, alertas inline, validação |
| **Neutra (Estrutura)** | `--color-neutral-*` (0-950) | Fundos, cards, divisores, inputs, bordas, texto |
| **Texto** | `--color-text-*` | Hierarquia tipográfica (primário → secundário → terciário) |

### 4.2 Semantic Color Mapping (Implementado)

| Componente | Variante | Tokens Usados |
| :--- | :--- | :--- |
| **Button** | `primary` | `--btn-primary-bg: var(--color-primary)`, `--btn-primary-hover: var(--color-primary-hover)`, `--btn-primary-text: var(--color-neutral-0)` |
| | `secondary` | `--btn-secondary-bg: var(--color-neutral-100)`, `--btn-secondary-hover: var(--color-neutral-200)`, `--btn-secondary-text: var(--color-text-primary)` |
| | `success` | `--btn-success-bg: var(--color-success)`, `--btn-success-hover: var(--color-success-700)` |
| | `danger` | `--btn-danger-bg: var(--color-danger)`, `--btn-danger-hover: var(--color-danger-700)` |
| | `outline` | `--btn-outline-primary-border: var(--color-primary)`, `--btn-outline-primary-text: var(--color-primary)`, `--btn-outline-primary-hover-bg: var(--color-primary-bg)` |
| | `ghost` | `--btn-ghost-text: var(--color-text-secondary)`, `--btn-ghost-hover-bg: var(--color-neutral-100)` |
| **Badge** | `success` | `--badge-success-bg: var(--color-success-bg)`, `--badge-success-text: var(--color-success-text)` |
| | `warning` | `--badge-warning-bg: var(--color-warning-bg)`, `--badge-warning-text: var(--color-warning-text)` |
| | `danger` | `--badge-danger-bg: var(--color-danger-bg)`, `--badge-danger-text: var(--color-danger-text)` |
| | `info` | `--badge-info-bg: var(--color-info-bg)`, `--badge-info-text: var(--color-info-text)` |
| **Toast/Alert** | `success` | `bg: var(--color-success-bg)`, `border: var(--color-success-border)`, `text: var(--color-success-text)` |
| | `error` | `bg: var(--color-danger-bg)`, `border: var(--color-danger-border)`, `text: var(--color-danger-text)` |
| | `warning` | `bg: var(--color-warning-bg)`, `border: var(--color-warning-border)`, `text: var(--color-warning-text)` |
| | `info` | `bg: var(--color-info-bg)`, `border: var(--color-info-border)`, `text: var(--color-info-text)` |
| **Input/Select** | `default` | `border: var(--color-border-light)`, `bg: var(--color-bg-secondary)` |
| | `focus` | `border: var(--color-border-focus)`, `box-shadow: var(--shadow-focus)` |
| | `error` | `border: var(--color-border-error)`, `box-shadow: 0 0 0 3px var(--color-danger-bg)` |
| **Table/Row** | `hover` | `bg: var(--color-bg-tertiary)`, `box-shadow: var(--shadow-xs)` |
| | `selected` | `border-left: 3px solid var(--color-primary)`, `bg: var(--color-primary-bg)` |
| **Focus Ring** | Global | `box-shadow: var(--shadow-focus)` = `0 0 0 3px var(--color-primary-bg)` |

### 4.3 Dark Mode (`.dark` class)

```css
/* Automático via .dark class no <html> */
.dark {
  --color-bg-primary: var(--color-neutral-950);
  --color-bg-secondary: var(--color-neutral-900);
  --color-bg-tertiary: var(--color-neutral-800);
  --color-text-primary: var(--color-neutral-50);
  --color-text-secondary: var(--color-neutral-300);
  --color-text-tertiary: var(--color-neutral-500);
  --color-border-light: var(--color-neutral-700);
  --color-border-medium: var(--color-neutral-600);
  --color-border-strong: var(--color-neutral-500);
  --color-border-focus: var(--color-primary-400);
  /* Semantic colors auto-adjusted via token mapping */
}
```

### 4.4 Contraste Validado (WCAG 2.1 AA)

| Combinação | Light Ratio | Dark Ratio | Status |
| :--- | :---: | :---: | :--- |
| Text Primary / BG Primary | 12.6:1 | 12.6:1 | ✅ AAA |
| Text Secondary / BG Primary | 7.0:1 | 7.0:1 | ✅ AAA |
| Text Tertiary / BG Primary | 4.5:1 | 4.5:1 | ✅ AA |
| Primary / BG Primary | 5.9:1 | 5.9:1 | ✅ AA |
| Border Light / BG Primary | 3.2:1 | 3.2:1 | ✅ AA (UI) |
| Focus Ring / BG Primary | 4.5:1 | 4.5:1 | ✅ AA |
| Success Badge | 5.2:1 | 5.2:1 | ✅ AA |
| Warning Badge | 4.5:1 | 4.5:1 | ✅ AA (limite) |
| Danger Badge | 5.5:1 | 5.5:1 | ✅ AA |
| Info Badge | 5.1:1 | 5.1:1 | ✅ AA |

> **Validação CI:** axe-core + Lighthouse CI ≥ 95 score a11y

---

## 5. Typography (Design Tokens v2.0 - Seção 3)

### 5.1 Font Stack (System UI - Zero Dependencies)

```scss
--font-family-sans: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
--font-family-mono: ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, 'Liberation Mono', monospace;
```

### 5.2 Type Scale (Base 1rem = 16px, Ratio 1.25)

| Token | Size | Line Height | Weight | Uso |
| :--- | :--- | :--- | :--- | :--- |
| `--text-xs` | 0.75rem (12px) | 1.5 | 400 | Labels inputs, metadata, badges |
| `--text-sm` | 0.875rem (14px) | 1.5 | 400 | Texto secundário, descrições, tooltips |
| `--text-base` | 1rem (16px) | 1.6 | 400 | **Corpo principal** (tabelas, formulários, listas) |
| `--text-lg` | 1.125rem (18px) | 1.5 | 500 | Subtítulos seção, cards resumo |
| `--text-xl` | 1.25rem (20px) | 1.4 | 600 | Títulos página, headers modal |
| `--text-2xl` | 1.5rem (24px) | 1.3 | 600 | Título principal (hero), dashboards |
| `--text-3xl` | 1.875rem (30px) | 1.2 | 700 | Reservado (landing/empty states) |

### 5.3 Composite Tokens (Ready-to-use)

```scss
--text-xs-normal:   var(--text-xs) / var(--leading-normal) var(--font-normal);
--text-sm-normal:   var(--text-sm) / var(--leading-normal) var(--font-normal);
--text-base-normal: var(--text-base) / var(--leading-relaxed) var(--font-normal);
--text-lg-normal:   var(--text-lg) / var(--leading-normal) var(--font-medium);
--text-xl-semibold: var(--text-xl) / var(--leading-snug) var(--font-semibold);
--text-2xl-bold:    var(--text-2xl) / var(--leading-tight) var(--font-bold);
--text-3xl-bold:    var(--text-3xl) / var(--leading-tight) var(--font-bold);
```

### 5.4 Font Weights

```scss
--font-light: 300;
--font-normal: 400;
--font-medium: 500;
--font-semibold: 600;
--font-bold: 700;
--font-extrabold: 800;
```

### 5.5 Monospace (Códigos, IDs, Valores Técnicos)

```scss
--font-family-mono: ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, 'Liberation Mono', monospace;
--text-mono-base: var(--text-base) var(--font-family-mono);
--text-mono-sm: var(--text-sm) var(--font-family-mono);
```

---

## 6. Spacing & Sizing (Design Tokens v2.0 - Seção 4)

### 6.1 Spacing Scale (Base 4px / 0.25rem)

```scss
--space-0: 0;
--space-1: 0.25rem;  // 4px
--space-2: 0.5rem;   // 8px
--space-3: 0.75rem;  // 12px
--space-4: 1rem;     // 16px
--space-5: 1.25rem;  // 20px
--space-6: 1.5rem;   // 24px
--space-8: 2rem;     // 32px
--space-10: 2.5rem;  // 40px
--space-12: 3rem;    // 48px
--space-16: 4rem;    // 64px
--space-20: 5rem;    // 80px
--space-24: 6rem;    // 96px
```

### 6.2 Component Spacing Conventions

| Uso | Token | Valor |
| :--- | :--- | :--- |
| **Button Padding** | `--padding-btn-sm` | `--space-1 --space-3` (4px 12px) |
| | `--padding-btn-md` | `--space-2 --space-4` (8px 16px) |
| | `--padding-btn-lg` | `--space-3 --space-6` (12px 24px) |
| **Input Padding** | `--padding-input` | `--space-2 --space-3` (8px 12px) |
| **Card Padding** | `--padding-card` | `--space-4 --space-6` (16px 24px) |
| **Modal Padding** | `--padding-modal` | `--space-6 --space-8` (24px 32px) |
| **Page Padding** | `--padding-page` | `--space-6 --space-8` (24px 32px) |
| **Inline Gaps** | `--gap-inline-sm` | `--space-1` (4px) |
| | `--gap-inline-md` | `--space-2` (8px) |
| | `--gap-inline-lg` | `--space-3` (12px) |
| **Stack Gaps** | `--gap-stack-sm` | `--space-2` (8px) |
| | `--gap-stack-md` | `--space-4` (16px) |
| | `--gap-stack-lg` | `--space-6` (24px) |
| **Section Gap** | `--gap-section` | `--space-8` (32px) |

### 6.3 Border Radius

```scss
--radius-none: 0;
--radius-sm: 0.125rem;   // 2px
--radius-md: 0.25rem;    // 4px (Bootstrap default)
--radius-lg: 0.5rem;     // 8px
--radius-xl: 0.75rem;    // 12px
--radius-2xl: 1rem;      // 16px
--radius-full: 9999px;   // Pill/Circle
--radius-btn: var(--radius-md);      // 4px
--radius-input: var(--radius-md);    // 4px
--radius-card: var(--radius-lg);     // 8px
--radius-modal: var(--radius-xl);    // 12px
--radius-badge: var(--radius-full);  // Pill
```

### 6.4 Sizing (Width/Height)

```scss
--size-xs: 1.5rem;    // 24px
--size-sm: 2rem;      // 32px
--size-md: 2.5rem;    // 40px
--size-lg: 3rem;      // 48px
--size-xl: 4rem;      // 64px
--size-2xl: 5rem;     // 80px
--size-icon-sm: 1rem;     // 16px
--size-icon-md: 1.25rem;  // 20px
--size-icon-lg: 1.5rem;   // 24px
--size-avatar-sm: 2rem;   // 32px
--size-avatar-md: 2.5rem; // 40px
--size-avatar-lg: 3.5rem; // 56px
```

---

## 7. Shadows & Elevation (Design Tokens v2.0 - Seção 5)

```scss
--shadow-none: none;
--shadow-xs: 0 1px 2px 0 rgb(0 0 0 / 0.05);
--shadow-sm: 0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1);
--shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
--shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1);
--shadow-xl: 0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1);
--shadow-2xl: 0 25px 50px -12px rgb(0 0 0 / 0.25);
--shadow-inner: inset 0 2px 4px 0 rgb(0 0 0 / 0.05);

// Semantic
--shadow-card: var(--shadow-sm);
--shadow-card-hover: var(--shadow-md);
--shadow-dropdown: var(--shadow-lg);
--shadow-modal: var(--shadow-xl);
--shadow-toast: var(--shadow-lg);
--shadow-tooltip: var(--shadow-md);
--shadow-focus: 0 0 0 3px var(--color-primary-bg); // Focus ring (WCAG)
```

---

## 8. Component Visual Specs (Resumo)

### 8.1 Buttons

| Variant | Background | Hover | Text | Border | Usage |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Primary** | `--color-primary` | `--color-primary-hover` | White | None | Ação principal (Criar, Salvar, Confirmar) |
| **Secondary** | `--color-neutral-100` | `--color-neutral-200` | `--color-text-primary` | `--color-border-light` | Ação secundária (Cancelar, Voltar) |
| **Success** | `--color-success` | `--color-success-700` | White | None | Ação positiva (Concluir, Aprovar) |
| **Danger** | `--color-danger` | `--color-danger-700` | White | None | Ação destrutiva (Excluir, Cancelar ordem) |
| **Outline Primary** | Transparent | `--color-primary-bg` | `--color-primary` | `--color-primary` | Ação alternativa (Filtrar, Exportar) |
| **Ghost** | Transparent | `--color-neutral-100` | `--color-text-secondary` | None | Ação terciária (Editar inline, Ver mais) |

**Sizes:** `sm` (24px h), `md` (32px h), `lg` (40px h) — `--padding-btn-*` + `--btn-font-size-*`

### 8.2 Form Inputs

| State | Border | Background | Shadow | Text |
| :--- | :--- | :--- | :--- | :--- |
| **Default** | `--color-border-light` | `--color-bg-secondary` | None | `--color-text-primary` |
| **Hover** | `--color-border-medium` | `--color-bg-secondary` | None | `--color-text-primary` |
| **Focus** | `--color-border-focus` | `--color-bg-secondary` | `--shadow-focus` | `--color-text-primary` |
| **Error** | `--color-border-error` | `--color-bg-secondary` | `0 0 0 3px var(--color-danger-bg)` | `--color-text-primary` |
| **Disabled** | `--color-border-light` | `--color-bg-tertiary` | None | `--color-text-tertiary` |
| **Readonly** | `--color-border-light` | `--color-bg-tertiary` | None | `--color-text-secondary` |

### 8.3 Badges (Status)

| Variant | Background | Text | Border | Usage |
| :--- | :--- | :--- | :--- | :--- |
| **Success** | `--color-success-bg` | `--color-success-text` | `--color-success-border` | Concluída, Ativo, OK |
| **Warning** | `--color-warning-bg` | `--color-warning-text` | `--color-warning-border` | Em Andamento, Atenção |
| **Danger** | `--color-danger-bg` | `--color-danger-text` | `--color-danger-border` | Cancelada, Erro, Crítico |
| **Info** | `--color-info-bg` | `--color-info-text` | `--color-info-border` | Aberta, Aguardando, Info |
| **Default** | `--color-neutral-100` | `--color-text-secondary` | `--color-border-light` | Neutro, Pendente |
| **Outline** | Transparent | Semantic text | Semantic border | Alternativa sutil |

**Sizes:** `sm` (20px h), `md` (24px h), `dot` (8px dot) — `--badge-padding` + `--badge-font-size`

### 8.4 Tables/DataTable

| Element | Spec |
| :--- | :--- |
| **Header** | BG: `--color-neutral-50` / Dark: `--color-neutral-800`; Text: `--color-text-secondary`; Border bottom: `--color-border-light` |
| **Row Hover** | BG: `--color-bg-tertiary`; Shadow: `--shadow-xs` |
| **Row Selected** | Left border: `3px solid --color-primary`; BG: `--color-primary-bg` |
| **Row Border** | `--color-border-light` |
| **Cell Padding** | `--space-3 --space-4` (12px 16px) |
| **Font Size** | `--text-sm` (14px) |
| **Sticky Header** | Shadow: `--shadow-xs` |
| **Expandable Row** | Animation: `max-height 0→scrollHeight` + `opacity 0→1` (`--duration-normal`, `--ease-out`) |

### 8.5 Modals & Drawers

| Size | Max Width | Padding | Border Radius | Shadow |
| :--- | :--- | :--- | :--- | :--- |
| `sm` | 400px | `--space-6` | `--radius-xl` | `--shadow-modal` |
| `md` | 600px | `--space-6 --space-8` | `--radius-xl` | `--shadow-modal` |
| `lg` | 800px | `--space-6 --space-8` | `--radius-xl` | `--shadow-modal` |
| `xl` | 1000px | `--space-6 --space-8` | `--radius-xl` | `--shadow-modal` |
| `full` | 90vw / 90vh | `--space-6 --space-8` | `--radius-xl` | `--shadow-modal` |

**Animation:** Backdrop `opacity 0→1` + Container `scale(0.95)→1` + `opacity 0→1` (`--duration-slow`, `--ease-out`)

### 8.6 Toasts

| Type | Background | Border | Text | Icon | Duration |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Success** | `--color-success-bg` | `--color-success-border` | `--color-success-text` | CheckCircle | 3s auto-dismiss |
| **Error** | `--color-danger-bg` | `--color-danger-border` | `--color-danger-text` | XCircle | Persistent (manual dismiss) |
| **Warning** | `--color-warning-bg` | `--color-warning-border` | `--color-warning-text` | AlertTriangle | 5s auto-dismiss |
| **Info** | `--color-info-bg` | `--color-info-border` | `--color-info-text` | Info | 4s auto-dismiss |

**Animation:** Enter `translateY(100%)→0` + `opacity 0→1` (`--duration-normal`, `--ease-spring`); Exit `translateY(0)→translateY(-100%)` + `opacity 1→0` (`--duration-fast`, `--ease-in`); Stack gap `--space-2`; Max 4 visible.

---

## 9. Iconography

### 9.1 Icon System
- **Library:** Lucide Vue Next (tree-shakable, SVG, 24x24 viewBox)
- **Sizes:** `--size-icon-sm` (16px), `--size-icon-md` (20px), `--size-icon-lg` (24px)
- **Color:** `currentColor` (herda cor do texto) ou semantic color tokens
- **Accessibility:** `aria-hidden="true"` em ícones decorativos; `aria-label` em ícones funcionais

### 9.2 Icon Mapping (Principais)

| Ação/Estado | Ícone Lucide | Uso |
| :--- | :--- | :--- |
| **Criar/Novo** | `Plus` | Botões "Novo", "Adicionar" |
| **Editar** | `Edit2` | Ações de edição |
| **Excluir** | `Trash2` | Ações destrutivas |
| **Visualizar** | `Eye` | Detalhes, visualização |
| **Baixar/Download** | `Download` | Export, PDF, QR Code |
| **Imprimir** | `Printer` | Impressão termo, etiquetas |
| **Buscar** | `Search` | Busca global, filtros |
| **Filtrar** | `Filter` | Filtros avançados |
| **Ordenar** | `ArrowUpDown` | Ordenação tabelas |
| **Expandir/Colapsar** | `ChevronDown` / `ChevronUp` | Accordion, DataTable expand |
| **Configurações** | `Settings` | Admin, preferências |
| **Usuário/Perfil** | `User` | Menu usuário, avatar |
| **Notificações** | `Bell` | Header notifications |
| **QR Code** | `QrCode` | Gerar/visualizar QR |
| **Escanear** | `Camera` | QR Scanner (PWA) |
| **Lanterna** | `Flashlight` | QR Scanner torch |
| **Sincronizar** | `RefreshCw` | Sync offline, refresh data |
| **Config/Engrenagem** | `Settings2` | Configurações avançadas |
| **Ajuda/Info** | `HelpCircle` | Tooltips, help text |
| **Alerta/Warning** | `AlertTriangle` | Warning badges, toasts |
| **Sucesso** | `CheckCircle2` | Success badges, toasts |
| **Erro** | `XCircle` | Error badges, toasts |
| **Fechar** | `X` | Modais, toasts, chips |
| **Chevron** | `ChevronDown/Up/Left/Right` | Dropdowns, accordion, navegação |
| **Mais/Menu** | `MoreHorizontal/Vertical` | Dropdown menus, ações de linha |
| **Refresh/Sync** | `RefreshCw` | Pull-to-refresh, sync offline |
| **Home** | `Home` | Breadcrumb, navegação |
| **Link Externo** | `ExternalLink` | Links externos, QR Code público |

---

## 10. Imagery & Illustrations

### 10.1 Empty States
- **Style:** Line art minimalista, cor `--color-text-tertiary` / `--color-neutral-400`
- **Sizes:** `--size-2xl` (80px) para ilustração principal
- **Tone:** Amigável, técnico, acionável (CTA claro)

### 10.2 Loading Skeletons
- **Colors:** `--color-neutral-100` (light) / `--color-neutral-800` (dark) base + `--color-neutral-200` / `--color-neutral-700` highlight
- **Animation:** Shimmer `--duration-slower` (1.5s) infinite
- **Variants:** `text` (linhas), `circular` (avatars), `rectangular` (cards), `table-row` (tabelas)

---

## 11. Responsive Behavior

### 10.1 Container Behavior

| Breakpoint | Container | Page Padding | Sidebar |
| :--- | :--- | :--- | :--- |
| **xs (< 576px)** | 100% (fluid) | `--space-4` | Drawer (hamburger) |
| **sm (≥ 576px)** | 540px | `--space-4` | Drawer |
| **md (≥ 768px)** | 720px | `--space-6` | Collapsible (280px) |
| **lg (≥ 992px)** | 960px | `--space-6` | Fixed (280px) |
| **xl (≥ 1200px)** | 1140px | `--space-8` | Fixed (280px) |
| **xxl (≥ 1400px)** | 1320px | `--space-10` | Fixed (320px) |

### 10.2 Component Adaptations

| Component | Mobile (< md) | Desktop (≥ md) |
| :--- | :--- | :--- |
| **Sidebar** | Drawer (hamburger) | Fixed left (280px) |
| **DataTable** | Horizontal scroll + sticky first col | Full width, all columns |
| **Modal** | Fullscreen (`size: full`) | Size `md`/`lg` centered |
| **Drawer** | Fullscreen (bottom sheet) | Side panel (280px) |
| **Tabs** | Scrollable horizontal | All visible |
| **Pagination** | Compact (prev/next + page) | Full (first/prev/page/next/last + size changer) |
| **Toolbar** | Stacked (search → filters → actions) | Horizontal (search | filters | actions) |
| **Form** | Single column, full width inputs | Multi-column (grid) where appropriate |
| **Charts** | Full width, simplified | Full width, detailed |
| **QR Scanner** | Fullscreen camera view | Modal centered (600px) |

---

## 11. Print Styles

```css
@media print {
  .no-print { display: none !important; } /* Header, Sidebar, Footer, Buttons, Toasts */
  .print-only { display: block !important; }
  
  .modal, .drawer, .toast-container, .dropdown { display: none !important; }
  
  .page-container { padding: 0; max-width: none; }
  .data-table { font-size: 12px; }
  .data-table th, .data-table td { padding: 4px 8px; }
  .badge { border: 1px solid currentColor; background: transparent !important; color: inherit !important; }
  a { text-decoration: none; color: inherit; }
  .page-break-inside-avoid { page-break-inside: avoid; }
}
```

---

## 12. Rastreabilidade Style Guide ↔ Artefatos

| Seção | Design Tokens | Component Library | Accessibility | Interaction Patterns | Component Code |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Layout/Grid** | `tokens.scss` §4, §8 | `Container.vue`, `Grid.vue`, `PageContainer.vue` | — | `interaction-patterns.md#layout` | `AppLayout.vue`, `PageContainer.vue` |
| **Colors** | `tokens.scss` §2 | All components | `accessibility-guidelines.md#contrast` | `interaction-patterns.md#colors` | All `.vue` (scoped styles) |
| **Typography** | `tokens.scss` §3 | `Text.vue`, `Heading.vue`, `Button.vue` | `accessibility-guidelines.md#typography` | — | `Text.vue`, `Heading.vue` |
| **Spacing** | `tokens.scss` §4 | All components | — | `interaction-patterns.md#spacing` | All `.vue` |
| **Shadows/Elevation** | `tokens.scss` §5 | `Card.vue`, `Modal.vue`, `Dropdown.vue`, `Toast.vue` | — | `interaction-patterns.md#elevation` | `Card.vue`, `Modal.vue`, `Dropdown.vue` |
| **Buttons** | `tokens.scss` §9.1 | `Button.vue`, `IconButton.vue` | `accessibility-guidelines.md#buttons` | `interaction-patterns.md#button` | `Button.vue`, `IconButton.vue` |
| **Forms** | `tokens.scss` §9.2 | `Input.vue`, `Select.vue`, `FormField.vue` | `accessibility-guidelines.md#forms` | `interaction-patterns.md#forms` | `Input.vue`, `Select.vue`, `FormField.vue` |
| **Tables** | `tokens.scss` §8.4 | `Table.vue`, `DataTable.vue` | `accessibility-guidelines.md#tables` | `interaction-patterns.md#tables` | `Table.vue`, `DataTable.vue` |
| **Modals/Drawers** | `tokens.scss` §8.5 | `Modal.vue`, `Drawer.vue` | `accessibility-guidelines.md#modals` | `interaction-patterns.md#modals` | `Modal.vue`, `Drawer.vue` |
| **Toasts** | `tokens.scss` §8.6 | `Toast.vue`, `ToastContainer.vue` | `accessibility-guidelines.md#toasts` | `interaction-patterns.md#toasts` | `Toast.vue`, `ToastContainer.vue` |
| **Responsive** | `tokens.scss` §8 | All components | `accessibility-guidelines.md#mobile` | `interaction-patterns.md#responsive` | `AppLayout.vue`, `PageContainer.vue` |
| **Print** | — | `PrintStyles.css` | — | — | `PrintStyles.css` |

---

*Documento regenerado completamente com base em Design Tokens v2.0 (implementados) + Component Library v2.0 + Vue 3 + Bootstrap 5 + PWA. Substitui versão 0.1 que continha apenas especificações teóricas sem implementação.*