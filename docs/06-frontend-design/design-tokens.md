# Design Tokens — Aegis1

> **Versão:** 2.0 · **Owner:** Design/Frontend Lead · **Status:** Implemented
> **Base:** Frontend Vue 3 + Vite + Bootstrap 5 + PWA + TypeScript
> **Implementação:** CSS Custom Properties (`:root`) + SCSS Variables + Bootstrap 5 Sass Customization
> **Arquivos:** `frontend/src/styles/tokens.scss`, `frontend/src/styles/variables.scss`, `frontend/vite.config.ts` (CSS variables injection)

---

## 1. Overview

Este documento reflete os **Design Tokens implementados** no frontend Aegis1 (Vue 3 + Vite + Bootstrap 5 + PWA). Os tokens são a **fonte única da verdade** para todos os valores visuais primitivos: cores, tipografia, espaçamento, bordas, sombras, animações, breakpoints, z-index.

**Estrutura de Arquivos:**
```
frontend/src/styles/
├── tokens.scss          # Design Tokens puros (CSS Custom Properties)
├── variables.scss       # Variáveis SCSS derivadas + Bootstrap overrides
├── main.scss            # Import principal (tokens + variables + bootstrap)
└── components/          # Component-specific tokens (opcional)
```

**Build:** Vite injeta CSS Custom Properties no `:root` via `vite-plugin-css-injected-by-js` + PostCSS `postcss-custom-properties` (fallback para browsers sem suporte).

---

## 2. Color Tokens

### 2.1 Primitive Colors (Paleta Base)

```scss
// frontend/src/styles/tokens.scss
:root {
  // Neutros
  --color-neutral-0:   #FFFFFF;   // White
  --color-neutral-50:  #FAFAFA;   // Gray 50
  --color-neutral-100: #F5F5F5;   // Gray 100
  --color-neutral-200: #EEEEEE;   // Gray 200
  --color-neutral-300: #E0E0E0;   // Gray 300
  --color-neutral-400: #BDBDBD;   // Gray 400
  --color-neutral-500: #9E9E9E;   // Gray 500
  --color-neutral-600: #757575;   // Gray 600
  --color-neutral-700: #616161;   // Gray 700
  --color-neutral-800: #424242;   // Gray 800
  --color-neutral-900: #212121;   // Gray 900
  --color-neutral-950: #121212;   // Gray 950

  // Primária (Azul Aegis)
  --color-primary-50:  #E6F0FA;
  --color-primary-100: #CCE0F5;
  --color-primary-200: #99C2EB;
  --color-primary-300: #66A3E0;
  --color-primary-400: #3385D6;
  --color-primary-500: #0066CC;   // Primary Base
  --color-primary-600: #0052A3;
  --color-primary-700: #003D7A;
  --color-primary-800: #002952;
  --color-primary-900: #001429;

  // Sucesso (Verde)
  --color-success-50:  #E8F5E9;
  --color-success-100: #C8E6C9;
  --color-success-200: #A5D6A7;
  --color-success-300: #81C784;
  --color-success-400: #66BB6A;
  --color-success-500: #4CAF50;   // Success Base
  --color-success-600: #43A047;
  --color-success-700: #388E3C;
  --color-success-800: #2E7D32;
  --color-success-900: #1B5E20;

  // Alerta (Âmbar/Amarelo)
  --color-warning-50:  #FFF8E1;
  --color-warning-100: #FFECB3;
  --color-warning-200: #FFE082;
  --color-warning-300: #FFD54F;
  --color-warning-400: #FFCA28;
  --color-warning-500: #FFC107;   // Warning Base
  --color-warning-600: #FFB300;
  --color-warning-700: #FFA000;
  --color-warning-800: #FF8F00;
  --color-warning-900: #FF6F00;

  // Perigo (Vermelho)
  --color-danger-50:   #FDEDEC;
  --color-danger-100:  #F9BDBC;
  --color-danger-200:  #F6999A;
  --color-danger-300:  #F37678;
  --color-danger-400:  #EF5350;
  --color-danger-500:  #F44336;   // Danger Base
  --color-danger-600:  #E53935;
  --color-danger-700:  #D32F2F;
  --color-danger-800:  #C62828;
  --color-danger-900:  #B71C1C;

  // Info (Azul Claro)
  --color-info-50:   #E1F5FE;
  --color-info-100:  #B3E5FC;
  --color-info-200:  #81D4FA;
  --color-info-300:  #4FC3F7;
  --color-info-400:  #29B6F6;
  --color-info-500:  #03A9F4;   // Info Base
  --color-info-600:  #039BE5;
  --color-info-700:  #0288D1;
  --color-info-800:  #0277BD;
  --color-info-900:  #01579B;
}
```

### 2.2 Semantic Colors (Mapeamento para Uso)

```scss
:root {
  // Backgrounds
  --color-bg-primary:   var(--color-neutral-50);    // App background
  --color-bg-secondary: var(--color-neutral-0);     // Cards, modals, panels
  --color-bg-tertiary:  var(--color-neutral-100);   // Hover rows, disabled inputs
  --color-bg-inverse:   var(--color-neutral-900);   // Dark mode panels

  // Text
  --color-text-primary:   var(--color-neutral-900);   // Main text
  --color-text-secondary: var(--color-neutral-700);   // Subtext, descriptions
  --color-text-tertiary:  var(--color-neutral-500);   // Placeholders, metadata
  --color-text-inverse:   var(--color-neutral-0);     // Text on dark backgrounds
  --color-text-link:      var(--color-primary-600);   // Links
  --color-text-link-hover: var(--color-primary-700);  // Link hover

  // Borders
  --color-border-light:    var(--color-neutral-200);  // Default borders
  --color-border-medium:   var(--color-neutral-300);  // Focused/active borders
  --color-border-strong:   var(--color-neutral-400);  // Error/focus rings
  --color-border-focus:    var(--color-primary-500);  // Focus ring
  --color-border-error:    var(--color-danger-500);   // Error state borders

  // Semantic States
  --color-success:       var(--color-success-600);
  --color-success-bg:    var(--color-success-50);
  --color-success-border: var(--color-success-200);
  --color-success-text:  var(--color-success-800);

  --color-warning:       var(--color-warning-700);
  --color-warning-bg:    var(--color-warning-50);
  --color-warning-border: var(--color-warning-100);
  --color-warning-text:  var(--color-warning-900);

  --color-danger:        var(--color-danger-600);
  --color-danger-bg:     var(--color-danger-50);
  --color-danger-border: var(--color-danger-100);
  --color-danger-text:   var(--color-danger-800);

  --color-info:          var(--color-info-600);
  --color-info-bg:       var(--color-info-50);
  --color-info-border:   var(--color-info-100);
  --color-info-text:     var(--color-info-800);

  // Brand/Action
  --color-primary:       var(--color-primary-600);
  --color-primary-hover: var(--color-primary-700);
  --color-primary-active: var(--color-primary-800);
  --color-primary-bg:    var(--color-primary-50);
  --color-primary-border: var(--color-primary-200);
  --color-primary-text:  var(--color-primary-800);
}
```

### 2.3 Dark Mode (`.dark` class no `<html>`)

```scss
.dark {
  --color-bg-primary:   var(--color-neutral-950);
  --color-bg-secondary: var(--color-neutral-900);
  --color-bg-tertiary:  var(--color-neutral-800);
  --color-bg-inverse:   var(--color-neutral-50);

  --color-text-primary:   var(--color-neutral-50);
  --color-text-secondary: var(--color-neutral-300);
  --color-text-tertiary:  var(--color-neutral-500);
  --color-text-inverse:   var(--color-neutral-950);
  --color-text-link:      var(--color-primary-400);
  --color-text-link-hover: var(--color-primary-300);

  --color-border-light:    var(--color-neutral-700);
  --color-border-medium:   var(--color-neutral-600);
  --color-border-strong:   var(--color-neutral-500);
  --color-border-focus:    var(--color-primary-400);

  --color-success-bg:    var(--color-success-900);
  --color-success-border: var(--color-success-800);
  --color-success-text:  var(--color-success-100);

  --color-warning-bg:    var(--color-warning-900);
  --color-warning-border: var(--color-warning-800);
  --color-warning-text:  var(--color-warning-100);

  --color-danger-bg:     var(--color-danger-900);
  --color-danger-border: var(--color-danger-800);
  --color-danger-text:   var(--color-danger-100);

  --color-info-bg:       var(--color-info-900);
  --color-info-border:   var(--color-info-800);
  --color-info-text:     var(--color-info-100);

  --color-primary-bg:    var(--color-primary-900);
  --color-primary-border: var(--color-primary-800);
  --color-primary-text:  var(--color-primary-100);
}
```

---

## 3. Typography

### 3.1 Font Families

```scss
:root {
  --font-family-sans: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  --font-family-mono: ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, 'Liberation Mono', monospace;
  --font-family-display: var(--font-family-sans); // Reservado para futura fonte de marca
}
```

### 3.2 Type Scale (Base 1rem = 16px, Ratio 1.25)

```scss
:root {
  // Font Sizes
  --text-xs:   0.75rem;   // 12px
  --text-sm:   0.875rem;  // 14px
  --text-base: 1rem;      // 16px (Base)
  --text-lg:   1.125rem;  // 18px
  --text-xl:   1.25rem;   // 20px
  --text-2xl:  1.5rem;    // 24px
  --text-3xl:  1.875rem;  // 30px
  --text-4xl:  2.25rem;   // 36px

  // Line Heights
  --leading-none:   1;
  --leading-tight:  1.25;
  --leading-snug:   1.375;
  --leading-normal: 1.5;
  --leading-relaxed: 1.625;
  --leading-loose:   2;

  // Font Weights
  --font-light:    300;
  --font-normal:   400;
  --font-medium:   500;
  --font-semibold: 600;
  --font-bold:     700;
  --font-extrabold: 800;

  // Composite Tokens (Ready-to-use)
  --text-xs-normal:   var(--text-xs) / var(--leading-normal) var(--font-normal);
  --text-sm-normal:   var(--text-sm) / var(--leading-normal) var(--font-normal);
  --text-base-normal: var(--text-base) / var(--leading-relaxed) var(--font-normal);
  --text-lg-normal:   var(--text-lg) / var(--leading-normal) var(--font-medium);
  --text-xl-semibold: var(--text-xl) / var(--leading-snug) var(--font-semibold);
  --text-2xl-bold:    var(--text-2xl) / var(--leading-tight) var(--font-bold);
  --text-3xl-bold:    var(--text-3xl) / var(--leading-tight) var(--font-bold);
}
```

### 3.3 Bootstrap 5 Integration (SCSS Variables)

```scss
// frontend/src/styles/variables.scss
@use "sass:map";
@use "sass:color";

@forward "tokens" as *;

// Bootstrap 5 Sass Variables Override
$font-family-sans-serif: var(--font-family-sans) !default;
$font-family-monospace: var(--font-family-mono) !default;
$font-family-base: var(--font-family-sans) !default;

$headings-font-family: var(--font-family-display) !default;
$headings-font-weight: var(--font-semibold) !default;
$headings-line-height: var(--leading-tight) !default;
$headings-color: var(--color-text-primary) !default;

$font-size-root: 16px !default; // 1rem = 16px
$font-size-base: 1rem !default;
$font-size-sm: 0.875rem !default;
$font-size-lg: 1.25rem !default;

$line-height-base: var(--leading-normal) !default;
$line-height-sm: var(--leading-normal) !default;
$line-height-lg: var(--leading-normal) !default;

$body-font-family: var(--font-family-sans) !default;
$body-font-size: var(--text-base) !default;
$body-line-height: var(--leading-relaxed) !default;
$body-color: var(--color-text-primary) !default;

$link-color: var(--color-text-link) !default;
$link-hover-color: var(--color-text-link-hover) !default;
$link-decoration: none !default;
$link-hover-decoration: underline !default;
```

---

## 4. Spacing & Sizing

### 4.1 Spacing Scale (Base 4px / 0.25rem)

```scss
:root {
  --space-0:   0;
  --space-1:   0.25rem;  // 4px
  --space-2:   0.5rem;   // 8px
  --space-3:   0.75rem;  // 12px
  --space-4:   1rem;     // 16px
  --space-5:   1.25rem;  // 20px
  --space-6:   1.5rem;   // 24px
  --space-8:   2rem;     // 32px
  --space-10:  2.5rem;   // 40px
  --space-12:  3rem;     // 48px
  --space-16:  4rem;     // 64px
  --space-20:  5rem;     // 80px
  --space-24:  6rem;     // 96px
}
```

### 4.2 Component Spacing Conventions

```scss
:root {
  // Component Internal Padding
  --padding-btn-sm:   var(--space-1) var(--space-3);  // 4px 12px
  --padding-btn-md:   var(--space-2) var(--space-4);  // 8px 16px
  --padding-btn-lg:   var(--space-3) var(--space-6);  // 12px 24px
  --padding-input:    var(--space-2) var(--space-3);  // 8px 12px
  --padding-card:     var(--space-4) var(--space-6);  // 16px 24px
  --padding-modal:    var(--space-6) var(--space-8);  // 24px 32px
  --padding-page:     var(--space-6) var(--space-8);  // 24px 32px

  // Gaps
  --gap-inline-sm:  var(--space-1);  // 4px
  --gap-inline-md:  var(--space-2);  // 8px
  --gap-inline-lg:  var(--space-3);  // 12px
  --gap-stack-sm:   var(--space-2);  // 8px
  --gap-stack-md:   var(--space-4);  // 16px
  --gap-stack-lg:   var(--space-6);  // 24px
  --gap-section:    var(--space-8);  // 32px
}
```

### 4.3 Border Radius

```scss
:root {
  --radius-none:  0;
  --radius-sm:    0.125rem;  // 2px
  --radius-md:    0.25rem;   // 4px
  --radius-lg:    0.5rem;    // 8px
  --radius-xl:    0.75rem;   // 12px
  --radius-2xl:   1rem;      // 16px
  --radius-full:  9999px;    // Pill/Circle
  --radius-btn:   var(--radius-md);   // 4px (Bootstrap default)
  --radius-input: var(--radius-md);   // 4px
  --radius-card:  var(--radius-lg);   // 8px
  --radius-modal: var(--radius-xl);   // 12px
  --radius-badge: var(--radius-full); // Pill
}
```

### 4.4 Sizing (Width/Height)

```scss
:root {
  --size-xs:   1.5rem;   // 24px
  --size-sm:   2rem;     // 32px
  --size-md:   2.5rem;   // 40px
  --size-lg:   3rem;     // 48px
  --size-xl:   4rem;     // 64px
  --size-2xl:  5rem;     // 80px
  --size-icon-sm: 1rem;   // 16px
  --size-icon-md: 1.25rem; // 20px
  --size-icon-lg: 1.5rem;  // 24px
  --size-avatar-sm: 2rem;   // 32px
  --size-avatar-md: 2.5rem; // 40px
  --size-avatar-lg: 3.5rem; // 56px
}
```

---

## 5. Shadows & Elevation

```scss
:root {
  --shadow-none: none;
  --shadow-xs:   0 1px 2px 0 rgb(0 0 0 / 0.05);
  --shadow-sm:   0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1);
  --shadow-md:   0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
  --shadow-lg:   0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1);
  --shadow-xl:   0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1);
  --shadow-2xl:  0 25px 50px -12px rgb(0 0 0 / 0.25);
  --shadow-inner: inset 0 2px 4px 0 rgb(0 0 0 / 0.05);

  // Semantic Shadows
  --shadow-card:      var(--shadow-sm);
  --shadow-card-hover: var(--shadow-md);
  --shadow-dropdown:  var(--shadow-lg);
  --shadow-modal:     var(--shadow-xl);
  --shadow-toast:     var(--shadow-lg);
  --shadow-tooltip:   var(--shadow-md);
  --shadow-focus:     0 0 0 3px var(--color-primary-bg); // Focus ring
}
```

---

## 6. Transitions & Animation

```scss
:root {
  // Durations
  --duration-instant: 0ms;
  --duration-fast:    100ms;
  --duration-normal:  200ms;
  --duration-slow:    300ms;
  --duration-slower:  500ms;

  // Easings
  --ease-linear:       linear;
  --ease-in:           cubic-bezier(0.4, 0, 1, 1);
  --ease-out:          cubic-bezier(0, 0, 0.2, 1);
  --ease-in-out:       cubic-bezier(0.4, 0, 0.2, 1);
  --ease-spring:       cubic-bezier(0.34, 1.56, 0.64, 1);
  --ease-bounce:       cubic-bezier(0.68, -0.55, 0.265, 1.55);

  // Composite
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
}
```

---

## 7. Z-Index Scale

```scss
:root {
  --z-index-auto:     auto;
  --z-index-0:        0;
  --z-index-10:       10;
  --z-index-20:       20;
  --z-index-30:       30;
  --z-index-40:       40;
  --z-index-50:       50;

  // Semantic
  --z-index-dropdown:   1000;
  --z-index-sticky:     1020;
  --z-index-fixed:      1030;
  --z-index-modal-backdrop: 1040;
  --z-index-modal:      1050;
  --z-index-popover:    1060;
  --z-index-tooltip:    1070;
  --z-index-toast:      1080;
  --z-index-notification: 1090;
}
```

---

## 8. Breakpoints (Responsive)

```scss
:root {
  // Bootstrap 5 Breakpoints (Mobile First)
  --breakpoint-xs: 0;
  --breakpoint-sm: 576px;
  --breakpoint-md: 768px;
  --breakpoint-lg: 992px;
  --breakpoint-xl: 1200px;
  --breakpoint-xxl: 1400px;

  // Container Max Widths
  --container-sm:  540px;
  --container-md:  720px;
  --container-lg:  960px;
  --container-xl:  1140px;
  --container-xxl: 1320px;
  --container-fluid: 100%;
}
```

### 8.1 Bootstrap 5 SCSS Breakpoint Map

```scss
// frontend/src/styles/variables.scss
$grid-breakpoints: (
  xs: 0,
  sm: 576px,
  md: 768px,
  lg: 992px,
  xl: 1200px,
  xxl: 1400px
) !default;

$container-max-widths: (
  sm: 540px,
  md: 720px,
  lg: 960px,
  xl: 1140px,
  xxl: 1320px
) !default;
```

---

## 9. Component-Specific Tokens (Exemplos)

### 9.1 Button Tokens

```scss
:root {
  // Button Variants
  --btn-primary-bg:       var(--color-primary);
  --btn-primary-hover:    var(--color-primary-hover);
  --btn-primary-active:   var(--color-primary-active);
  --btn-primary-text:     var(--color-neutral-0);
  --btn-primary-border:   transparent;

  --btn-secondary-bg:     var(--color-neutral-100);
  --btn-secondary-hover:  var(--color-neutral-200);
  --btn-secondary-active: var(--color-neutral-300);
  --btn-secondary-text:   var(--color-text-primary);
  --btn-secondary-border: var(--color-border-light);

  --btn-success-bg:       var(--color-success);
  --btn-success-hover:    var(--color-success-700);
  --btn-success-text:     var(--color-neutral-0);

  --btn-danger-bg:        var(--color-danger);
  --btn-danger-hover:     var(--color-danger-700);
  --btn-danger-text:      var(--color-neutral-0);

  --btn-outline-primary-border: var(--color-primary);
  --btn-outline-primary-text:   var(--color-primary);
  --btn-outline-primary-hover-bg: var(--color-primary-bg);

  --btn-ghost-text:       var(--color-text-secondary);
  --btn-ghost-hover-bg:   var(--color-neutral-100);

  // Button Sizes
  --btn-padding-sm:   var(--padding-btn-sm);
  --btn-padding-md:   var(--padding-btn-md);
  --btn-padding-lg:   var(--padding-btn-lg);
  --btn-font-size-sm: var(--text-sm);
  --btn-font-size-md: var(--text-base);
  --btn-font-size-lg: var(--text-lg);
  --btn-gap-sm:       var(--gap-inline-sm);
  --btn-gap-md:       var(--gap-inline-md);
}
```

### 9.2 Form Input Tokens

```scss
:root {
  --input-bg:           var(--color-neutral-0);
  --input-border:       var(--color-border-light);
  --input-border-hover: var(--color-border-medium);
  --input-border-focus: var(--color-border-focus);
  --input-border-error: var(--color-border-error);
  --input-text:         var(--color-text-primary);
  --input-placeholder:  var(--color-text-tertiary);
  --input-label:        var(--color-text-secondary);
  --input-help-text:    var(--color-text-tertiary);
  --input-error-text:   var(--color-danger-text);
  --input-padding:      var(--padding-input);
  --input-radius:       var(--radius-input);
  --input-font-size:    var(--text-base);
  --input-line-height:  var(--leading-normal);
  --input-transition:   var(--transition-colors), var(--transition-shadow);
}
```

### 9.3 Table Tokens

```scss
:root {
  --table-header-bg:       var(--color-neutral-50);
  --table-header-text:     var(--color-text-secondary);
  --table-header-border:   var(--color-border-light);
  --table-row-hover-bg:    var(--color-neutral-100);
  --table-row-selected-bg: var(--color-primary-bg);
  --table-row-border:      var(--color-border-light);
  --table-cell-padding:    var(--space-3) var(--space-4);
  --table-font-size:       var(--text-sm);
  --table-sticky-header-shadow: var(--shadow-xs);
}
```

### 9.4 Badge/Status Tokens

```scss
:root {
  --badge-padding:       var(--space-1) var(--space-2);
  --badge-font-size:     var(--text-xs);
  --badge-font-weight:   var(--font-medium);
  --badge-radius:        var(--radius-badge);

  --badge-success-bg:    var(--color-success-bg);
  --badge-success-text:  var(--color-success-text);
  --badge-warning-bg:    var(--color-warning-bg);
  --badge-warning-text:  var(--color-warning-text);
  --badge-danger-bg:     var(--color-danger-bg);
  --badge-danger-text:   var(--color-danger-text);
  --badge-info-bg:       var(--color-info-bg);
  --badge-info-text:     var(--color-info-text);
  --badge-neutral-bg:    var(--color-neutral-100);
  --badge-neutral-text:  var(--color-text-secondary);
}
```

---

## 10. Accessibility Tokens (WCAG 2.1 AA)

```scss
:root {
  // Focus Ring (WCAG 2.4.7)
  --focus-ring-width:    2px;
  --focus-ring-offset:   2px;
  --focus-ring-color:    var(--color-primary);
  --focus-ring-style:    solid;
  --focus-ring:          var(--focus-ring-width) var(--focus-ring-style) var(--focus-ring-color);
  --focus-ring-offset-color: var(--color-bg-secondary);

  // Contrast Ratios (Minimum 4.5:1 normal text, 3:1 large text)
  // Validado via axe-core CI

  // Touch Targets (WCAG 2.5.5 - 44x44px minimum)
  --touch-target-min:    44px;
  --touch-target-comfortable: 48px;

  // Motion Reduction (prefers-reduced-motion)
  --duration-reduced:    0.01ms; // Effectively instant
}
```

---

## 11. Implementation & Usage

### 11.1 Import Order (main.ts / main.scss)

```scss
// frontend/src/styles/main.scss
@use "tokens" as *;           // 1. Design Tokens (CSS Custom Properties)
@use "variables" as *;        // 2. SCSS Variables + Bootstrap Overrides
@import "bootstrap/scss/bootstrap"; // 3. Bootstrap 5 (usa variáveis acima)
@import "./components";       // 4. Component overrides (opcional)
```

### 11.2 Usage in Vue Components

```vue
<!-- Exemplo: Button.vue -->
<script setup lang="ts">
// Tokens disponíveis via CSS Custom Properties globalmente
// Uso direto no template/style scoped
</script>

<template>
  <button 
    class="btn" 
    :class="[variantClass, sizeClass]"
    :style="customStyles"
  >
    <slot />
  </button>
</template>

<style scoped>
.btn {
  /* Uso direto de tokens CSS */
  padding: var(--btn-padding-md);
  font-size: var(--btn-font-size-md);
  font-weight: var(--font-medium);
  border-radius: var(--radius-btn);
  border: 1px solid var(--btn-border, transparent);
  background: var(--btn-bg, var(--color-primary));
  color: var(--btn-text, var(--color-neutral-0));
  gap: var(--btn-gap-md);
  transition: var(--transition-colors), var(--transition-shadow);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
}

.btn:hover { background: var(--btn-hover); }
.btn:active { background: var(--btn-active); }
.btn:focus-visible { 
  outline: none; 
  box-shadow: var(--focus-ring), var(--focus-ring-offset) var(--focus-ring-offset-color); 
}

/* Variant modifiers via CSS variables */
.btn--primary { --btn-bg: var(--btn-primary-bg); --btn-hover: var(--btn-primary-hover); --btn-text: var(--btn-primary-text); }
.btn--secondary { --btn-bg: var(--btn-secondary-bg); --btn-hover: var(--btn-secondary-hover); --btn-text: var(--btn-secondary-text); --btn-border: var(--btn-secondary-border); }
.btn--success { --btn-bg: var(--btn-success-bg); --btn-hover: var(--btn-success-hover); --btn-text: var(--btn-success-text); }
.btn--danger { --btn-bg: var(--btn-danger-bg); --btn-hover: var(--btn-danger-hover); --btn-text: var(--btn-danger-text); }
.btn--outline-primary { --btn-bg: transparent; --btn-border: var(--btn-outline-primary-border); --btn-text: var(--btn-outline-primary-text); --btn-hover-bg: var(--btn-outline-primary-hover-bg); }
.btn--ghost { --btn-bg: transparent; --btn-border: transparent; --btn-text: var(--btn-ghost-text); --btn-hover-bg: var(--btn-ghost-hover-bg); }

/* Size modifiers */
.btn--sm { padding: var(--btn-padding-sm); font-size: var(--btn-font-size-sm); gap: var(--btn-gap-sm); }
.btn--lg { padding: var(--btn-padding-lg); font-size: var(--btn-font-size-lg); gap: var(--btn-gap-lg); }
</style>
```

### 11.3 Dark Mode Toggle (Pinia Store + CSS Class)

```typescript
// frontend/src/stores/ui.ts
export const useUIStore = defineStore('ui', {
  state: () => ({
    darkMode: false,
  }),
  actions: {
    toggleDarkMode() {
      this.darkMode = !this.darkMode;
      document.documentElement.classList.toggle('dark', this.darkMode);
      localStorage.setItem('darkMode', String(this.darkMode));
    },
    initDarkMode() {
      const stored = localStorage.getItem('darkMode');
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      this.darkMode = stored ? JSON.parse(stored) : prefersDark;
      document.documentElement.classList.toggle('dark', this.darkMode);
    },
  },
});
```

---

## 12. Token Governance

| Processo | Frequência | Responsável |
| :--- | :--- | :--- |
| **Token Audit** | Trimestral | Design Lead + Frontend Lead |
| **New Token Request** | Sob demanda (PR template) | Designer → Frontend Lead approval |
| **Token Deprecation** | Anual | Design Lead + Frontend Lead |
| **Accessibility Audit** | Semestral | QA + Frontend Lead (axe-core) |
| **Dark Mode Validation** | Por release | QA + Frontend Lead |
| **Bundle Size Impact** | Every PR | CI (vite-bundle-analyzer) |

---

## 13. Rastreabilidade Tokens ↔ Artefatos

| Token Category | System Arch | UI Style Guide | Component Library | Accessibility | Component Code |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Colors** | §4 (Frontend) | `ui-style-guide.md#colors` | `component-library.md#button`, `#badge`, `#alert` | `accessibility-guidelines.md#contrast` | `Button.vue`, `Badge.vue`, `Alert.vue`, `tokens.scss` |
| **Typography** | §4 (Frontend) | `ui-style-guide.md#typography` | `component-library.md#text`, `#heading` | `accessibility-guidelines.md#text` | `Text.vue`, `Heading.vue`, `tokens.scss` |
| **Spacing** | §4 (Frontend) | `ui-style-guide.md#spacing` | `component-library.md#layout` | — | `Container.vue`, `Grid.vue`, `Stack.vue`, `tokens.scss` |
| **Shadows** | §4 (Frontend) | `ui-style-guide.md#elevation` | `component-library.md#card`, `#modal`, `#dropdown` | — | `Card.vue`, `Modal.vue`, `Dropdown.vue`, `tokens.scss` |
| **Animation** | §4 (Frontend) | `interaction-patterns.md#transitions` | `component-library.md#transitions` | `accessibility-guidelines.md#motion` | `Transition.vue`, `tokens.scss` |
| **Breakpoints** | §4 (Frontend) | `ui-style-guide.md#responsive` | `component-library.md#responsive` | — | `Grid.vue`, `Container.vue`, `variables.scss` |
| **Z-Index** | §4 (Frontend) | `ui-style-guide.md#layering` | `component-library.md#modal`, `#toast`, `#tooltip` | — | `Modal.vue`, `Toast.vue`, `Tooltip.vue`, `tokens.scss` |
| **Form/Input** | §4 (Frontend) | `ui-style-guide.md#forms` | `component-library.md#input`, `#select`, `#textarea` | `accessibility-guidelines.md#forms` | `Input.vue`, `Select.vue`, `FormField.vue`, `tokens.scss` |
| **Button** | §4 (Frontend) | `ui-style-guide.md#buttons` | `component-library.md#button` | `accessibility-guidelines.md#buttons` | `Button.vue`, `ButtonGroup.vue`, `tokens.scss` |
| **Table** | §4 (Frontend) | `ui-style-guide.md#tables` | `component-library.md#table` | `accessibility-guidelines.md#tables` | `Table.vue`, `DataTable.vue`, `tokens.scss` |

---

*Documento regenerado completamente com tokens implementados no frontend Vue 3 + Vite + Bootstrap 5 + PWA. Substitui versão 0.1 que indicava "não implementado".*