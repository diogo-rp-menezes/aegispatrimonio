# UI Style Guide — Aegis1

> **Versão:** 0.1 · **Owner:** Design/Frontend Lead · **Status:** Draft — *Tokens e componentes não implementados no codebase atual*  
> **Depende de:** `design-tokens.md`, `component-library.md`

---

## 1. Overview & Princípios de Design

O **Aegis1** é um sistema B2B de gestão de manutenção industrial/facilities. A identidade visual prioriza **clareza sobre densidade**, **funcionalidade sobre decoração** e **acessibilidade como requisito não negociável**.

### Princípios Norteadores

| Princípio | Descrição | Aplicação Prática |
| :--- | :--- | :--- |
| **Dados primeiro** | A interface serve a tabelas, listas e formulários densos; ornamentação compete com informação. | Cores semânticas apenas para estado (sucesso/erro/aviso), nunca decorativas. |
| **Consistência acima de customização** | Um token, um propósito. Variações pontuais criam dívida técnica e cognitiva. | Todos os valores visuais vêm de `design-tokens.md`; zero hardcoded. |
| **Dark mode nativo** | Técnicos em campo operam em ambientes com iluminação variável. | Tokens definem light/dark simultaneamente; troca via `[data-theme="dark"]`. |
| **Movimento funcional** | Animação só para feedback de estado ou transição de painel. | `prefers-reduced-motion` respeitado; durações em `design-tokens.md` §6. |
| **Acessibilidade por padrão** | WCAG AA mínimo; foco visível nunca removido. | `--shadow-focus` obrigatório em todos os interativos; contraste validado em CI. |

> **⚠️ Estado real:** O codebase atual (15 arquivos `.js`, vanilla JS, `@popperjs/core` apenas) **não possui CSS, design tokens, nem componentes**. Este guia especifica o alvo; a implementação começa com o checklist de `design-tokens.md` §9 e o inventário de `component-library.md` §3.

---

## 2. Voz Visual

* **Personalidade da marca:** Técnica, confiável, minimalista, sem ruído visual.
* **Referências visuais (moodboard):** *[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]* — Interfaces de sistemas ERP/industriais modernos (ex.: Linear, GitHub, Vercel Dashboard) — densidade alta, tipografia system-ui, cores restritas a acento azul + semânticas.
* **O que evitar:**
  - Gradientes decorativos, sombras pesadas, bordas arredondadas excessivas (`radius-lg` só em painéis flutuantes/modais).
  - Cores fora da paleta semântica (verde/âmbar/vermelho/azul apenas para estado).
  - Ícones sem label acessível ou tooltip.
  - Animações decorativas (spinners de página, transições de entrada de elementos estáticos).

---

## 3. Layout & Grid

### 3.1 Sistema de Grid

| Aspecto | Definição | Token/Origem |
| :--- | :--- | :--- |
| **Container máximo** | `1280px` (`xl` breakpoint) — centralizado com `margin-inline: auto` | `design-tokens.md` §5 (`xl: 1280px`) |
| **Grid base** | 4px (`space-1`) — todos os espaçamentos são múltiplos de 4px | `design-tokens.md` §4.1 |
| **Colunas** | 12 colunas fluidas (CSS Grid) para layouts de página; flexbox para componentes | `component-library.md` §2 (CSS Modules + tokens) |
| **Gutter padrão** | `space-6` (24px) entre colunas de layout; `space-4` (16px) entre cards/itens | `design-tokens.md` §4.1 |
| **Padding de página** | `space-6` (mobile) → `space-8` (tablet) → `space-10` (desktop) | `design-tokens.md` §5 breakpoints |

### 3.2 Densidade de Informação

| Contexto | Densidade | Implementação |
| :--- | :--- | :--- |
| **Tabelas/DataTable** | Compacta — `space-1` vertical, `space-2` horizontal; `text-sm`/`text-base` | `component-library.md` §4.3 `DataTable` |
| **Formulários** | Espaçada — `space-3` entre label+input, `space-6` entre seções | `component-library.md` §4.5 `Wizard/Stepper` |
| **Dashboards/Cards** | Média — `space-4` interno, `space-6` entre cards | `component-library.md` §3 `Card` |
| **Mobile** | Empilhada — single column, `space-4` entre blocos | `design-tokens.md` §5 `sm: 640px` |

### 3.3 Padrões de Página

| Tipo de Página | Estrutura | Exemplo (Fluxo) |
| :--- | :--- | :--- |
| **Lista/Tabela** | `Header (título + ações globais) → Toolbar (busca + filtros) → DataTable → Pagination` | Flow 1 (entidades), Flow 8 (ordens) |
| **Detalhe/Drill-down** | `Breadcrumb → Header (título + ações) → Tabs/Accordion (seções) → Conteúdo` | Flow 4 (detalhe ordem), Flow 7 (ativo → ordens) |
| **Formulário/Wizard** | `Header (título + fechar) → Stepper (progresso) → StepContent (form) → Footer (navegação)` | Flow 2 (criação ordem) |
| **Dashboard/Resumo** | `Grid de Cards (KPIs) → DataTable/Chart (detalhe) → Filtros laterais (Drawer mobile)` | Flow 3 (lista ordens), Flow 7 (custo por ativo) |
| **Modal/Overlay** | `Backdrop → Container (Header + Content + Footer) → Focus trap` | Flow 1, 2, 4, 5, 6 (confirmações, formulários) |

---

## 4. Uso de Cor

### 4.1 Hierarquia Visual via Cor

| Camada | Tokens | Uso |
| :--- | :--- | :--- |
| **Primária (Ação)** | `--color-accent`, `--color-accent-hover`, `--color-accent-light` | Botões primários, links, foco visível, badges "info" |
| **Semântica (Estado)** | `--color-success*`, `--color-warning*`, `--color-danger*`, `--color-info*` | Badges de estado (ordem, prioridade), toasts, alertas inline, validação |
| **Neutra (Estrutura)** | `--color-bg`, `--color-panel`, `--color-border`, `--color-border-strong` | Fundos, cards, divisores, inputs, bordas |
| **Texto** | `--color-text-primary`, `--color-text-secondary`, `--color-text-muted` | Hierarquia tipográfica (título → corpo → metadado) |

> **Regra:** Cor **nunca** é o único meio de transmitir informação. Badges usam ícone + texto; validação usa ícone + mensagem; gráficos usam padrão + cor.

### 4.2 Regras de Contraste Mínimo

* **Texto/UI:** WCAG AA (4.5:1 normal, 3:1 large) — validado em CI (`design-tokens.md` §9).
* **Não-texto (bordas, ícones, focus ring):** 3:1 contra fundo adjacente.
* **Modo dark:** Todos os pares revalidados; tokens dark em `design-tokens.md` §2.3.

### 4.3 Uso de Cor Semântica — Onde Permitido / Proibido

| Componente | Permitido | Proibido |
| :--- | :--- | :--- |
| **Button** | `variant="primary"` (accent), `variant="destructive"` (danger) | `variant="success"`/`"warning"` como ação principal |
| **Badge** | Todas as variantes semânticas (`success`, `warning`, `danger`, `info`, `default`, `outline`) | Cores customizadas fora da tabela §2.2 |
| **Toast/Alert** | Variante mapeada ao tipo (success/error/warning/info) | Misturar tipos no mesmo toast |
| **Input/Select** | `border-color: var(--color-danger)` + ícone erro no estado `error` | Fundo colorido no input (apenas borda + ícone) |
| **Tabela/Row** | Hover: `--color-accent-light`; Seleção: border-left accent + bg accent-light | Linhas zebradas com cores semânticas |
| **Gráficos/Charts** | Paleta categórica derivada dos tokens semânticos (ordem fixa) | Cores arbitrárias; mais de 6 categorias sem padrão |

---

## 5. Tipografia em Contexto

> **Fonte:** `system-ui` stack (zero dependências externas) — `design-tokens.md` §3.1  
> **Escala:** Major third (1.25), base 1rem = 16px — `design-tokens.md` §3.2

| Elemento | Token de Tipografia | Cor | Peso | Exemplo de Uso |
| :--- | :--- | :--- | :--- | :--- |
| **H1 — Título de página** | `text-2xl` (1.5rem/24px, lh 1.3) | `--color-text-primary` | `600` | `<h1 class="page-title">Ordens de Manutenção</h1>` |
| **H2 — Seção principal** | `text-xl` (1.25rem/20px, lh 1.4) | `--color-text-primary` | `600` | `<h2>Detalhes da Ordem #1234</h2>` |
| **H3 — Subseção / Card title** | `text-lg` (1.125rem/18px, lh 1.5) | `--color-text-primary` | `500` | `<h3 class="card-title">Materiais Utilizados</h3>` |
| **Corpo principal (tabelas, formulários, listas)** | `text-base` (1rem/16px, lh 1.6) | `--color-text-primary` | `400` | `<td>Troca de filtro hidráulico</td>` |
| **Texto secundário / Descrição** | `text-sm` (0.875rem/14px, lh 1.5) | `--color-text-secondary` | `400` | `<p class="help-text">Selecione o ativo vinculado à ordem.</p>` |
| **Label / Caption / Metadado** | `text-xs` (0.75rem/12px, lh 1.5) | `--color-text-muted` | `400` | `<span class="meta">Criado em 15/01/2025 por João</span>` |
| **Código / ID / Valor técnico** | `text-sm` + `--font-mono` | `--color-text-secondary` | `400` | `<code class="mono">ORD-2025-001234</code>` |
| **Botão (label)** | `text-sm` (md) / `text-base` (lg) | `white` (primary) / `--color-text-primary` (ghost) | `500` | `<Button>Iniciar Ordem</Button>` |
| **Badge** | `text-xs` (sm) / `text-sm` (md) | Cor da variante (semântica) | `500` | `<Badge variant="warning">Em Andamento</Badge>` |

> **Nota:** `--font-sans` para todo texto UI; `--font-mono` apenas para IDs, códigos, logs, valores técnicos (`design-tokens.md` §3.1).

---

## 6. Padrões de Conteúdo (UX Writing)

### 6.1 Tom de Voz nos Textos de UI

* **Direto e imperativo:** "Criar ordem", "Selecionar ativo", "Confirmar exclusão" — sem "por favor", "gostaria de".
* **Sem jargão desnecessário:** "Fornecedor" em vez de "Vendor"; "Técnico" em vez de "Assignee".
* **Contexto antes da ação:** "Esta ordem tem 3 custos vinculados. Excluir mesmo assim?" — não apenas "Excluir?".
* **Erro = o que aconteceu + como resolver:** "Falha ao salvar: campo 'Fornecedor' é obrigatório. Selecione um fornecedor na lista."

### 6.2 Padrões de Mensagens de Erro

| Tipo | Estrutura | Exemplo |
| :--- | :--- | :--- |
| **Validação inline** | `[Campo] + [regra violada] + [como corrigir]` | "Data de início: deve ser anterior à data de fim. Ajuste a data." |
| **Erro de API (toast)** | `[Ação] falhou: [motivo técnico resumido]. [Ação sugerida]` | "Falha ao criar ordem: fornecedor inativo. Ative o fornecedor ou escolha outro." |
| **Erro 5xx / Boundary** | "Ocorreu um erro inesperado. Tente novamente em instantes. Se persistir, contate suporte." | — |
| **Estado vazio (EmptyState)** | `[O que não há] + [por que] + [CTA se aplicável]` | "Nenhuma ordem encontrada. Ajuste os filtros ou crie a primeira ordem." |

### 6.3 Padrões de Call-to-Action

| Contexto | Padrão | Exemplo |
| :--- | :--- | :--- |
| **Ação primária (Button primary)** | Verbo no imperativo, sem reticências | "Criar ordem", "Iniciar", "Aprovar", "Concluir", "Salvar" |
| **Ação secundária (Button ghost/secondary)** | Verbo + objeto opcional | "Cancelar", "Voltar", "Limpar filtros" |
| **Ação destrutiva (Button destructive)** | "Excluir [objeto]", "Cancelar [objeto]", "Remover [objeto]" | "Excluir fornecedor", "Cancelar ordem" |
| **Link de navegação** | Substantivo/rótulo da página | "Ver detalhes", "Histórico de custos", "Configurações" |

### 6.4 Formatação de Números/Datas/Moeda

| Tipo | Formato (pt-BR) | Implementação |
| :--- | :--- | :--- |
| **Moeda (BRL)** | `R$ 1.234,56` (locale `pt-BR`, `currency: BRL`) | `Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })` |
| **Número inteiro** | `1.234` (separador de milhar) | `Intl.NumberFormat('pt-BR')` |
| **Número decimal** | `1.234,56` (2 casas) | `Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2 })` |
| **Data curta** | `15/01/2025` | `date.toLocaleDateString('pt-BR')` |
| **Data/hora** | `15/01/2025 14:30` | `date.toLocaleString('pt-BR', { hour: '2-digit', minute: '2-digit' })` |
| **Data relativa** | "há 2 dias", "em 3 horas" | Biblioteca leve (ex.: `date-fns/formatDistanceToNow`) |

---

## 7. Iconografia

* **Biblioteca de ícones:** *[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]* — **Lucide React** (tree-shakable, SVG, consistente com system-ui, ~400 ícones, MIT). Alternativa: `phosphor-icons` ou `heroicons` — decisão pendente Design Lead.
* **Tamanhos padrão (tokens):**

| Tamanho | Token | Valor (px) | Uso |
| :--- | :--- | :--- | :--- |
| `xs` | `text-xs` + `w-4 h-4` | `12×12` | Badges `size="sm"`, `IconButton sm`, inline em `text-xs` |
| `sm` | `text-sm` + `w-5 h-5` | `16×16` | **Padrão** — `Button`, `Input` prefix/suffix, `Badge md`, `Table` actions |
| `md` | `text-base` + `w-6 h-6` | `20×20` | `IconButton md`, `Modal` close, `Drawer` handle |
| `lg` | `text-lg` + `w-8 h-8` | `24×24` | `IconButton lg`, `EmptyState` ilustração, `LoadingOverlay` spinner |

* **Regras de uso:**
  - Ícone **sozinho** (sem texto visível) **sempre** requer `aria-label` ou `Tooltip` acessível.
  - Ícone **decorativo** (ao lado de label) recebe `aria-hidden="true"`.
  - Cor do ícone herda `currentColor` (segue cor do texto/pai) — **não** hardcodear `fill="#..."`.
  - Spinner de loading: `animate-spin` (CSS) + `currentColor` — tamanho `sm` em botões, `md` em overlay.

---

## 8. Imagens & Mídia

* **Proporções aceitas:**
  - Avatar: `1:1` (quadrado) — `component-library.md` §3 `Avatar`
  - Ilustração EmptyState: `4:3` ou `16:9` — max-width `320px` mobile, `480px` desktop
  - Anexos/Evidências (Flow 4): `16:9` preview; original sob download
* **Tratamento de placeholder/loading:**
  - **Skeleton** (`component-library.md` §3 `Skeleton`): `variant="rectangular"` para thumbnails, `variant="circular"` para avatars, `variant="image"` para placeholders de evidência.
  - **Blur-up:** Não aplicável (sem next/image ou similar); usar Skeleton + transição `opacity 0→1` (`duration-fast`).
* **Empty States — Padrão Visual (`component-library.md` §3 `EmptyState`):**

| Variante | Composição | Exemplo |
| :--- | :--- | :--- |
| `default` | Ilustração (svg, `text-muted`) + Título (`text-lg`, `text-primary`) + Descrição (`text-sm`, `text-secondary`) | "Nenhuma ordem encontrada" |
| `action` | Acima + `Button primary` (CTA principal) | "Criar primeira ordem" |
| `illustration` | Ilustração maior (`w-64`/`w-80`) + texto centralizado | Dashboard vazio, primeiro acesso |

---

## 9. Padrões de Layout Responsivo

* **Abordagem:** Mobile-first — `design-tokens.md` §5 breakpoints.
* **Comportamento por breakpoint:**

| Breakpoint | Largura | Mudanças de Layout |
| :--- | :--- | :--- |
| `< sm` (0–639px) | Mobile | Single column; `Drawer` para filtros/sidebar; `Modal fullscreen` para wizard; `DataTable` com scroll horizontal + `stickyHeader`; `Pagination` compacta; `FilterBar` colapsada em botão. |
| `sm` (640px+) | Mobile grande | Formulários 2 colunas (label + input); `Modal lg`; `DataTable` colunas essenciais visíveis. |
| `md` (768px+) | Tablet | Sidebar colapsável (off-canvas); `DataTable` colunas completas; `Wizard horizontal`; `Tabs` horizontais. |
| `lg` (1024px+) | Desktop pequeno | Sidebar fixa (260px); layout 2 colunas (lista + detalhe); `Modal lg`/`xl`; `DataTable` virtualized se >100 linhas. |
| `xl` (1280px+) | Desktop padrão | Container max-width 1280px; dashboard 3+ colunas; `FilterBar` inline expandida. |
| `2xl` (1536px+) | Ultra-wide | Layout estendido; painéis lado a lado (ex.: ordem + timeline + custos). |

* **O que colapsa/esconde em telas pequenas:**
  - Sidebar → `Drawer` (hamburger no header)
  - Colunas secundárias da `DataTable` → `expandable` row ou `DropdownMenu` "Colunas"
  - `FilterBar` inline → botão "Filtros" abre `Drawer`/`Modal`
  - `Breadcrumb` → `variant="collapsed"` (apenas "Início / Atual")
  - `Pagination` → `variant="compact"` (apenas anterior/próximo + página atual)

---

## 10. Checklist de Revisão Visual

* [ ] **Tokens only:** Nenhum valor hardcoded (hex, rgb, px, rem, shadow, font-size) — só `var(--token)` ou classes utilitárias mapeadas a tokens.
* [ ] **Componentes oficiais:** Todos os elementos UI usam componentes de `component-library.md` §3 (Button, Input, DataTable, Badge, Modal, etc.) — sem `<button>`/`<input>`/`<table>` nus.
* [ ] **Contraste validado:** Todos os pares texto/fundo, borda/fundo, focus ring/fundo passam WCAG AA (light + dark) — teste automatizado em CI.
* [ ] **Estados definidos:** Hover, focus (visível!), active, disabled, loading, error — documentados no Storybook para cada componente.
* [ ] **Responsivo testado:** Comportamento verificado nos 6 breakpoints (`sm`..`2xl`) — mobile-first, sem regressão desktop.
* [ ] **Acessibilidade:** Focus trap em Modal/Drawer; `aria-*` em componentes compostos; `prefers-reduced-motion` desativa transições; navegação por teclado completa.
* [ ] **Dark mode:** Todas as telas/estados renderizam corretamente com `[data-theme="dark"]` — sem "flash" de light mode.
* [ ] **Iconografia:** Ícones sem label têm `Tooltip` ou `aria-label`; decorativos têm `aria-hidden="true"`.
* [ ] **Content patterns:** Textos seguem §6 (tom, erros, CTAs, formatação pt-BR).
* [ ] **Performance:** `DataTable` virtualized >100 linhas; imagens lazy; bundle icons tree-shaken.

---

## 11. Referências

* **Design Tokens (fonte única de verdade):** `design-tokens.md` — cores, tipografia, espaçamento, raios, sombras, breakpoints, motion, z-index.
* **Component Library (inventário + specs):** `component-library.md` — 34 componentes (26 base + 8 domínio), props, estados, tokens consumidos, exemplos.
* **User Flows (requisitos de UI):** `user-flows.md` — 8 fluxos com estados de tela, métricas, edge cases.
* **Business Requirements Document:** `docs/brd.md` — regras BR-01 a BR-06, personas, glossário, guardrails (latência P95 < 800ms, taxa erro < 1%).
* **Diagnóstico Determinístico:** `server/analyze-pipeline.ts` — confirma ausência de CSS/design system no codebase atual.
* **Stack Tecnológica Verificada:** `package.json` — vanilla JS, `@popperjs/core` apenas; migração para React 18 + TS planejada (`component-library.md` §2).
* **Figma/Design Tool:** *[LINK NÃO DISPONÍVEL NO REPOSITÓRIO — REQUER ENTRADA HUMANA]*
* **Contrato de API (OpenAPI):** *[NÃO ENCONTRADO NO REPOSITÓRIO — DEPENDÊNCIA EXTERNA: TIME DE BACKEND]*

---

## 12. Revision History

| Versão | Data | Autor | Mudanças |
| :--- | :--- | :--- | :--- |
| 0.1 | 15/01/2025 | Pipeline (gerado) | Criação inicial baseada em `design-tokens.md` + `component-library.md` — **tokens e componentes não implementados**; todo conteúdo derivado dos artefatos-fonte com marcadores `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` onde aplicável. |