# UI Style Guide — Aegis Patrimonio

> **Versão:** 1.0 · **Owner:** Design/Frontend Lead · **Status:** Draft  
> **Depende de:** `design-tokens.md`, `component-library.md`

---

## 1. Overview & Princípios de Design

O **Aegis Patrimonio** é uma aplicação web de gestão patrimonial enterprise (dashboard, tabelas densas, formulários complexos, modais, alertas de saúde) construída em **vanilla JavaScript (ES6 modules)** com **`@popperjs/core`** como única dependência de UI para posicionamento de tooltips, dropdowns e popovers. Não há framework de componentes (React, Vue, Svelte) nem biblioteca de UI (Material, Bootstrap, Tailwind).

**Princípios norteadores:**

| Princípio | Descrição |
| :--- | :--- |
| **Tokens como fonte única** | Nenhum valor visual (cor, espaçamento, tipografia, sombra, raio, duração, z-index) é hardcoded. Todos os componentes consomem **CSS Custom Properties** definidas em `design-tokens.md` (Seções 2–8). |
| **Clareza sobre densidade** | Tabelas usam densidade `compact` (`--table-font-size: var(--text-sm)`, padding `--space-2/--space-3`) para exibir muitos dados; formulários e leitura usam espaçamento `comfortable` (`--space-4/--space-6`). |
| **Dark mode first** | Tokens definem valores para **light** e **dark** (Seção 2.3). Troca de tema via classe `.dark` no `<html>` — persistida em `localStorage` + respeito a `prefers-color-scheme`. |
| **Acessibilidade nativa (WCAG 2.1 AA)** | Contraste ≥ 4.5:1 validado em todos os pares texto/fundo; focus visible obrigatório (`--shadow-focus`); reduced motion respeitado; status nunca comunicados apenas por cor (ícone + texto + cor). |
| **Consistência acima de customização pontual** | Componentes seguem especificação única em `component-library.md` (Seção 4). Variações só via props documentadas (variant, size, density). |
| **Performance em listas grandes** | `DataTable` prevê server-side pagination/sort/filter; skeleton loading; virtualização futura (Gap conhecido: DataGrid virtualizado). |

> **Rastreabilidade:** Stack confirmada pelo diagnóstico determinístico — 15 arquivos JS em `frontend/`, dependência única `@popperjs/core`, zero CSS framework, zero componentes UI existentes. <!-- source: diagnostico.md#L1-L30 -->

---

## 2. Voz Visual

* **Personalidade da marca:** Técnica, confiável, minimalista, orientada a dados. Interface "invisível" que prioriza legibilidade de tabelas, velocidade de leitura e clareza de status (ativo/manutenção/baixa/crítico).
* **Referências visuais (moodboard):** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Admin B2B modernos (Linear, GitHub, Vercel, Tailwind UI) — paleta slate/blue, tipografia Inter, ícones Lucide/Phosphor, elevação sutil (`--shadow-sm`/`--shadow-md`).
* **O que evitar:**
  - Gradientes decorativos sem função semântica
  - Cores fora da paleta de tokens (Seção 2.1/2.2)
  - Sombras pesadas (`--shadow-lg` apenas em modais/drawers/toasts)
  - Animações lentas (>300ms) exceto page transitions
  - Ícones sem label acessível ou tooltip
  - Hardcoded values em componentes (ex: `padding: 16px` → usar `var(--space-4)`)

---

## 3. Layout & Grid

### 3.1 Sistema de Grid
* **Container max-width:** `1280px` (`--bp-xl`) centrado com padding lateral `--space-6` (24px).
* **Sidebar fixa (desktop ≥ `--bp-lg` = 1024px):** `260px` expandida / `72px` colapsada (apenas ícones + tooltips).
* **Breakpoints (tokens):** `--bp-sm: 640px`, `--bp-md: 768px`, `--bp-lg: 1024px`, `--bp-xl: 1280px`, `--bp-2xl: 1536px` (Seção 5 de `design-tokens.md`).

### 3.2 Densidade de Informação
| Contexto | Densidade | Tokens-chave |
| :--- | :--- | :--- |
| **DataTable (listas de ativos, manutenções, alertas)** | `compact` | `--table-font-size: var(--text-sm)`, `--table-cell-padding-y: var(--space-2)`, `--table-cell-padding-x: var(--space-3)` |
| **Formulários (modais, drawers)** | `comfortable` | `--space-4` gap vertical, `--space-6` gap horizontal entre colunas, `--input-height: 2.5rem` |
| **Dashboard / Cards de resumo** | `comfortable` | `--space-5` gap entre cards, `--shadow-sm` elevação padrão |
| **Mobile (< `--bp-lg`)** | `stacked` | Sidebar vira drawer; formulários 1 coluna; tabelas com scroll horizontal |

### 3.3 Padrões de Página

| Tipo de Página | Estrutura | Exemplo (Fluxo) |
| :--- | :--- | :--- |
| **Lista/Tabela** | `Header` (título + ações) → `Toolbar` (filtros, busca, bulk actions) → `DataTable` (sticky header, pagination) → `Footer` (opcional) | Flow 3: Lista de Ativos; Flow 4: Lista de Manutenções; Flow 5: Alertas |
| **Detalhe/Formulário** | `Header` (breadcrumb + título + ações) → `Modal`/`Drawer`/`Page` com `FormLayout` (grid 1/2/3 colunas, labels top) → `Footer` (Cancelar/Salvar) | Flow 2: Login; Flow 3: Criar/Editar Ativo; Flow 4: Nova Manutenção |
| **Dashboard** | `Header` → `Grid de Cards` (2-3 colunas em `--bp-xl`, 1 coluna mobile) → `DataTable` resumida ou `Chart` (futuro) | Flow 1: Dashboard inicial; Flow 6: Relatórios TCO |
| **Empty State** | `EmptyState` component centralizado: ilustração + título + descrição + CTA primário | Flow 3: Nenhum ativo cadastrado; Flow 5: Nenhum alerta |

> **Componentes de layout:** `Sidebar`, `Header`, `FormLayout`, `Card`, `EmptyState` — especificados em `component-library.md` (Seções 4.10, 4.11, 4.8, 4.20). <!-- source: component-library.md#L400-L550 -->

---

## 4. Uso de Cor

### 4.1 Hierarquia Visual via Cor
| Camada | Tokens | Uso |
| :--- | :--- | :--- |
| **Primária (ações principais)** | `--color-accent-primary` (`#2563EB`), `--color-accent-hover` (`#1D4ED8`), `--color-accent-light` (`#DBEAFE`) | Botões primários, links, badges ativos, focus ring (`--shadow-focus`), row selected (`--table-row-selected-bg`) |
| **Secundária (UI neutra)** | `--color-bg-primary/secondary/tertiary`, `--color-border-light/medium`, `--color-text-primary/secondary/muted` | Fundos, bordas, textos, placeholders, divisores |
| **Semântica (status/feedback)** | `--color-success`, `--color-warning`, `--color-danger`, `--color-info` + respectivos `-bg` | Badges de status, toasts, alertas, validações, indicadores de saúde |

### 4.2 Regras de Contraste Mínimo
* **Todos os pares texto/fundo** validados ≥ **4.5:1** (texto normal) / **3:1** (large text ≥ 18px ou 14px bold) — Seção 10 de `design-tokens.md`.
* **Focus visible:** `--shadow-focus: 0 0 0 3px rgb(37 99 235 / 0.4)` obrigatório em **todos** elementos interativos (botões, links, inputs, rows clicáveis, tabs, dropdown items).
* **Color independence:** Status **nunca** comunicados apenas por cor — sempre com **ícone + texto + cor** (ex: Badge "Crítico" + ícone ⚠ + cor vermelha). Ver `Badge` variants em `component-library.md` Seção 4.8.

### 4.3 Uso de Cor Semântica — Onde Permitido / Proibido

| Componente / Contexto | Permitido | Proibido |
| :--- | :--- | :--- |
| **Badge (status de ativo/manutenção/alerta)** | Variantes semânticas: `ativo` (verde), `manutencao` (âmbar), `baixa` (neutro), `critico` (vermelho), `success`/`warning`/`info`/`neutral` genéricos | Cores customizadas fora das variantes |
| **Toast / Alert** | `variant: 'success' \| 'warning' \| 'danger' \| 'info'` → mapeia tokens semânticos + borda esquerda 4px | Usar cor primária para erro/sucesso |
| **Botão** | `variant: 'primary' \| 'secondary' \| 'danger' \| 'ghost'` — `danger` usa `--color-danger` para ações destrutivas (excluir, cancelar) | `variant: 'success'` (não existe — sucesso é feedback, não ação) |
| **Input/Select border** | `error` state → `--error-border-color` (`--color-danger`) + focus ring vermelho | Border vermelho sem `aria-invalid="true"` |
| **Tabela row** | `selected` → `--table-row-selected-bg` (`--color-accent-light`); `hover` → `--table-row-hover-bg` (`--color-bg-tertiary`) | Colorir row inteira por status (usar Badge na coluna Status) |
| **Texto corrido** | `--color-text-primary/secondary/muted` apenas | Texto colorido sem função semântica (ex: título em azul) |

> **Tokens de cor:** Seção 2 (Base, Semântica, Light/Dark) de `design-tokens.md`. <!-- source: design-tokens.md#L15-L80 -->

---

## 5. Tipografia em Contexto

| Elemento | Token de Tipografia | Cor | Peso | Uso |
| :--- | :--- | :--- | :--- | :--- |
| **H1 — Título de página / Dashboard principal** | `--text-2xl` (1.5rem / 24px, line-height 1.3) | `--color-text-primary` | `600` | `Dashboard`, `Relatórios`, `Auditoria` |
| **H2 — Título de seção / Modal / Card header** | `--text-xl` (1.25rem / 20px, line-height 1.4) | `--color-text-primary` | `600` | Modal title, Card header, Section em formulário |
| **H3 — Subtabela / Subseção** | `--text-lg` (1.125rem / 18px, line-height 1.5) | `--color-text-primary` | `500` | Subtítulos dentro de modais complexos |
| **Corpo principal / Parágrafos / Células de tabela** | `--text-base` (1rem / 16px, line-height 1.6) | `--color-text-primary` | `400` | Texto corrido, descrições, `DataTable` cells |
| **Label de formulário / Metadados de tabela / Badge text** | `--text-xs` (0.75rem / 12px, line-height 1.5) | `--color-text-secondary` (label) / `--color-text-primary` (badge) | `400` (label) / `600` (badge) | `Input` label, `DataTable` header, `Badge` |
| **Texto secundário / Helper / Timestamp / Descrição** | `--text-sm` (0.875rem / 14px, line-height 1.5) | `--color-text-secondary` | `400` | Helper text, tooltip, toast message, meta info |
| **Placeholder / Disabled / Muted** | `--text-sm` / `--text-base` | `--color-text-muted` (`#94A3B8` light / `#64748B` dark) | `400` | Input placeholder, disabled text, empty state description |
| **Código / IDs / Números de série / Datas ISO / Valores monetários alinhados** | `--font-mono` (`JetBrains Mono` stack) + `--text-sm` ou `--text-base` | `--color-text-primary` | `400` | `Ativo.tag`, `numeroSerie`, `dataAquisicao`, `valorAquisicao` em tabela |

> **Font families:** `--font-sans: Inter + system-ui stack` (UI geral); `--font-mono: JetBrains Mono + monospace stack` (dados técnicos). Seção 3 de `design-tokens.md`. <!-- source: design-tokens.md#L85-L115 -->

---

## 6. Padrões de Conteúdo (UX Writing)

### 6.1 Tom de Voz nos Textos de UI
* **Direto, técnico, sem jargão desnecessário.** Verbos no imperativo para ações. Frases curtas. Português do Brasil (pt-BR).
* **Exemplos:**
  - ✅ "Salvar ativo" (botão primário)
  - ✅ "Tag já cadastrada. Informe uma tag única." (erro de validação)
  - ❌ "Por favor, clique no botão abaixo para proceder com a gravação do ativo."

### 6.2 Padrões de Mensagens de Erro
**Estrutura:** `[O que aconteceu] + [Como resolver]` — exibido inline (Input/Select) ou em Toast/Alert (erros de servidor).

| Contexto | Padrão | Exemplo |
| :--- | :--- | :--- |
| **Validação de campo (inline)** | Curto, imperativo, sem "Erro:" | "Tag obrigatória", "Valor deve ser maior que zero", "Data não pode ser futura" |
| **Erro de API (Toast danger)** | Título: falha genérica; Mensagem: detalhe acionável | Título: "Erro ao salvar" / Mensagem: "Tag AT-2024-001 já existe. Use outra tag." |
| **Erro crítico (persistente)** | `persistent: true` + action "Tentar novamente" | Título: "Falha na conexão" / Mensagem: "Verifique sua rede e tente novamente." |

### 6.3 Padrões de Call-to-Action (Botões)
* **Verbo no imperativo**, sem reticências, sem "meu/minha".
* **Primário (uma por tela/modal):** "Salvar", "Criar", "Confirmar exclusão", "Aprovar", "Rejeitar".
* **Secundário:** "Cancelar", "Voltar", "Limpar filtros".
* **Danger:** "Excluir", "Baixar ativo", "Cancelar manutenção".
* **Ghost (toolbar):** Ícone + tooltip — label acessível obrigatório (`aria-label`).

### 6.4 Formatação de Números, Datas, Moeda
| Tipo | Formato | Exemplo | Componente |
| :--- | :--- | :--- | :--- |
| **Moeda (BRL)** | `new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })` | `R$ 12.345,67` | `DataTable` cell (align right, `--font-mono`), Input `type="number"` step="0.01" |
| **Data (curta)** | `DD/MM/AAAA` | `15/03/2024` | `DatePicker`, `DataTable` cell |
| **Data/hora (ISO)** | `YYYY-MM-DDTHH:mm` | `2024-03-15T14:30` | `Input type="datetime-local"`, auditoria |
| **Número inteiro (quantidade, vida útil)** | Sem separador de milhar | `42`, `10` | `Input type="number"` |
| **Tag / Código / Serial** | Maiúsculo, hífens conforme padrão | `AT-2024-001`, `SN-X7K9M2` | `--font-mono`, `--text-sm` |

---

## 7. Iconografia

* **Biblioteca de ícones:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] **Lucide** (SVG inline, tree-shakable, 24x24px viewBox) — usado em `Button`, `IconButton`, `Sidebar`, `Badge` (dot), `Toast`, `Tooltip`, `Select` (chevron), `DataTable` (sort arrows). Alternativa: Phosphor Icons.
* **Tamanhos padrão:**
  - `16px` (1rem) — `IconButton sm`, `Badge` dot, `Select` chevron, `Tooltip` icon
  - `20px` (1.25rem) — `IconButton md`, `Sidebar` nav icons, `Toast` icon (`--alert-icon-size`)
  - `24px` (1.5rem) — `IconButton lg`, `Header` actions, `Modal` close
* **Regras de uso:**
  - **Ícone sozinho SEMPRE requer** `aria-label` (IconButton) ou `Tooltip` (tabela/header).
  - **Ícone + label:** ícone leading (antes do texto) — `Button`, `Sidebar` item, `Dropdown` item.
  - **Ícone de estado:** `Badge` dot (8px), `Toast` icon (20px), `Alert` icon (20px), `EmptyState` illustration (64-96px).
  - **Cores de ícone:** Herdam `currentColor` (texto) — exceto `Toast`/`Alert`/`Badge` onde ícone usa cor semântica do variant.

> **Componentes que usam ícones:** `Button`, `IconButton`, `Sidebar`, `Header`, `DataTable` (sort, actions), `Select` (chevron), `DatePicker` (calendar), `Toast`, `Alert`, `Badge`, `Tooltip`, `Dropdown`, `Tabs`, `Breadcrumb`, `Pagination`, `EmptyState`, `Skeleton`. <!-- source: component-library.md#L50-L300 -->

---

## 8. Imagens & Mídia

* **Proporções aceitas:**
  - **Avatar:** 1:1 (quadrado) — tamanhos `xs: 24px`, `sm: 32px`, `md: 40px`, `lg: 56px` (token `--btn-height-*` equivalentes).
  - **Empty State illustration:** 4:3 ou 16:9 — max-width `320px`, centralizado.
  - **Logo (Sidebar/Header):** SVG preferido, altura `32px` (expandida) / `24px` (colapsada).
* **Tratamento de placeholder/loading:**
  - **Skeleton** component (`component-library.md` Seção 4.21) — variants: `text` (linhas), `card` (bloco), `table` (linhas de tabela), `avatar` (círculo).
  - **Animação:** `pulse` (padrão) ou `wave` — duração `--duration-slower` (500ms), respeita `prefers-reduced-motion`.
  - **Imagens reais (futuro):** `blur-up` (low-res → high-res) + `object-fit: cover` para avatars.
* **Empty States — Padrão Visual:**
  - Container centralizado (`display: flex`, `flex-direction: column`, `align-items: center`, `gap: var(--space-4)`, `padding: var(--space-12)`)
  - Ilustração SVG (64-96px, cor `--color-text-muted`)
  - Título `--text-lg` / `--color-text-primary` (ex: "Nenhum ativo encontrado")
  - Descrição `--text-sm` / `--color-text-secondary` (ex: "Cadastre o primeiro ativo ou ajuste os filtros.")
  - CTA primário (`Button variant="primary"`) quando ação óbvia existe.

> **Componentes:** `Avatar` (Seção 4.19), `EmptyState` (Seção 4.20), `Skeleton` (Seção 4.21) de `component-library.md`. <!-- source: component-library.md#L550-L650 -->

---

## 9. Padrões de Layout Responsivo

* **Abordagem:** **Mobile-first** para componentes (tokens em `rem`, breakpoints `min-width`), **Desktop-first** para layout de página (sidebar fixa em `--bp-lg+`).
* **Comportamento por breakpoint:**

| Breakpoint | Mudança de Layout |
| :--- | :--- |
| `< --bp-sm` (640px) | Formulários 1 coluna (`FormLayout`); `DataTable` scroll horizontal; `Sidebar` drawer (abre via hamburger); `Modal` full-screen (`size: 'xl'` → `max-width: 100%`, `margin: 0`, `border-radius: 0`); `Toast` bottom center, width `calc(100% - 2*var(--toast-offset))` |
| `--bp-sm` – `--bp-md` (640-768px) | `FormLayout` 1-2 colunas; `Sidebar` drawer; `DataTable` compact density |
| `--bp-md` – `--bp-lg` (768-1024px) | `Sidebar` colapsável (ícones + tooltip); `FormLayout` 2-3 colunas; `DataTable` confortável |
| `≥ --bp-lg` (1024px) | **Layout padrão:** Sidebar fixa expandida (260px) + Content; `FormLayout` 3 colunas; `DataTable` completo; `Modal` `size: 'md'/'lg'` centralizado |
| `≥ --bp-xl` (1280px) | Container max-width 1280px; Dashboard grid 2-3 colunas; `Modal` `size: 'xl'` (960px) |

* **O que colapsa/esconde em telas pequenas:**
  - Sidebar labels (colapsada) / Sidebar inteira (drawer mobile)
  - `DataTable` colunas menos críticas (via `column.hideOn: 'mobile'`) — scroll horizontal preferido
  - `Header` ações secundárias → `Dropdown` (Menu do usuário, Notificações, Tema)
  - `Breadcrumb` itens intermediários → truncados com `...` (tooltip no hover)

> **Tokens de breakpoint:** Seção 5 de `design-tokens.md`. Comportamento de `Sidebar` e `Modal` documentado em `component-library.md` Seções 4.10 e 4.6. <!-- source: design-tokens.md#L150-L170 --> <!-- source: component-library.md#L400-L480 -->

---

## 10. Checklist de Revisão Visual

| Item | Verificação | Referência |
| :--- | :--- | :--- |
| [ ] **Tokens only** | Nenhum valor hardcoded (cor, espaçamento, tipografia, sombra, raio, duração, z-index) — todos via `var(--token)` | `design-tokens.md` Seções 2–8 |
| [ ] **Componentes existem** | Todos os componentes usados estão especificados em `component-library.md` (Seção 4) | `component-library.md` Seção 3 (Inventário) |
| [ ] **Contraste validado** | Pares texto/fundo ≥ 4.5:1 (normal) / 3:1 (large) — light & dark | `design-tokens.md` Seção 10 |
| [ ] **Estados definidos** | Hover, Focus (`--shadow-focus`), Active, Disabled, Loading, Error para **todos** componentes interativos | `component-library.md` Seção 4 (cada componente → "Estados visuais") |
| [ ] **Responsivo testado** | Comportamento verificado nos 5 breakpoints (`--bp-sm` a `--bp-2xl`) | `design-tokens.md` Seção 5; `component-library.md` Seção 9 |
| [ ] **Acessibilidade** | `role`, `aria-*`, `tabindex`, focus trap (Modal/Drawer), `aria-live` (Toast/Alert), reduced motion | `design-tokens.md` Seção 10; `component-library.md` Seção 2 |
| [ ] **Dark mode** | Todos os tokens têm valor light/dark; troca via `.dark` no `<html>` funciona sem flash | `design-tokens.md` Seção 2.3 |
| [ ] **Ícones acessíveis** | Ícone sozinho → `aria-label` ou `Tooltip`; ícone decorativo → `aria-hidden="true"` | `component-library.md` Seção 7 (Iconografia) |
| [ ] **Status sem cor apenas** | Badge/Toast/Alert/Table row status → sempre ícone + texto + cor | `design-tokens.md` Seção 10; `component-library.md` Seção 4.8 |
| [ ] **Motion respeitado** | `@media (prefers-reduced-motion: reduce)` → durações `0.01ms` (exceto instant) | `design-tokens.md` Seção 6 |

---

## 11. Referências

* **Design Tokens (fonte única):** `design-tokens.md` — cores, tipografia, espaçamento, sombras, breakpoints, motion, z-index, tokens semânticos de componente, governança, acessibilidade. <!-- source: design-tokens.md -->
* **Component Library / Inventory:** `component-library.md` — 27 componentes planejados, API/props, estados visuais, tokens consumidos, exemplos de uso vanilla JS. <!-- source: component-library.md -->
* **Glossário Ubíquo:** `docs/glossario.md` — nomenclatura de domínio (Ativo, Manutenção, Filial, Departamento, Tipo de Ativo, Fornecedor, Localização, Tag, Baixa, Auditoria). <!-- source: glossario.md#L1-L75 -->
* **Business Requirements Document:** `docs/brd.md` (seções 3, 4, 6) — personas (ADMIN/USER), regras de negócio (BR-01 a BR-10), KPIs, fluxos que influenciam tokens/componentes. <!-- source: brd.md -->
* **Diagnóstico Determinístico:** `docs/diagnostico.md` — confirma stack vanilla JS + Popper.js only, 0 componentes UI existentes, 350 arquivos Java (backend). <!-- source: diagnostico.md -->
* **Popper.js Docs:** https://popper.js.org/docs/v2/ — posicionamento de `Tooltip`, `Dropdown`, `Select`, `DatePicker`. <!-- source: component-library.md#L700 -->
* **WCAG 2.1 AA Quick Reference:** https://www.w3.org/WAI/WCAG21/quickref/ — checklist aplicado a todos os componentes. <!-- source: design-tokens.md#L300 -->
* **Figma / Design Tool:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Link para arquivo Figma do Aegis Patrimonio (Design System library) — sincronizado via `tokens.json` + Style Dictionary. <!-- source: design-tokens.md#L310 -->

---

> **Próximos passos (action items consolidados):**
> 1. [ ] Validar paleta de cores e tipografia com stakeholder de Design/Brand
> 2. [ ] Criar `frontend/src/design-tokens.json` + configurar Style Dictionary (ou script custom) no `package.json`
> 3. [ ] Gerar `tokens.css` e importar no entry point da aplicação
> 4. [ ] Implementar componentes **core** (Button, Input, Select, Modal, Toast, Badge, IconButton)
> 5. [ ] Implementar `DataTable` (componente mais complexo — prioridade alta)
> 6. [ ] Implementar layout base: `Sidebar`, `Header`, `FormLayout`, `Card`, `EmptyState`, `Skeleton`
> 7. [ ] Implementar navegação/feedback: `Tooltip`, `Dropdown`, `Tabs`, `Breadcrumb`, `Pagination`, `Alert`
> 8. [ ] Adicionar testes de acessibilidade (axe-core) e regressão visual (Playwright) por componente
> 9. [ ] Documentar cada componente em `frontend/docs/components/` (ou Storybook se adotado futuramente)
> 10. [ ] Preencher gaps conhecidos: Wizard/Stepper, TreeSelect, DataGrid virtualizado, Chart/Sparkline, DateRangePicker