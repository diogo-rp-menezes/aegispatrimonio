# UI Style Guide — Aegis Patrimônio

> **Versão:** 1.0 · **Owner:** Design/Frontend Lead · **Status:** Draft  
> **Depende de:** [design-tokens.md](design-tokens.md), [component-library.md](component-library.md)

---

## 1. Overview & Princípios de Design

O **Aegis Patrimônio** é um sistema enterprise de gestão de ativos de TI e facilities (cadastro, manutenção, health checks, auditoria, RBAC). A interface serve **operadores de campo, gestores de TI, aprovadores e administradores** em contextos de alta densidade informacional (tabelas com centenas de linhas, dashboards de métricas, formulários de múltiplas etapas).

**Princípios norteadores:**

| Princípio | Descrição | Aplicação Prática |
| :--- | :--- | :--- |
| **Clareza sobre densidade** | Informação densa não significa visual ruidoso. Hierarquia tipográfica, espaçamento consistente e cor semântica guiam o olhar. | Tabelas usam `--space-2`/`--space-3` entre linhas; headers fixos (`--z-sticky`); zebra sutil via `--table-row-hover`. |
| **Ação evidente, consequência clara** | Botões primários (`--btn-primary-bg`) indicam a ação principal da tela; destrutivos (`--btn-danger-bg`) exigem confirmação modal (`aegis-confirm-dialog`). | Nunca mais de 1 botão `primary` visível por viewport; `destructive` sempre abre `aegis-modal variant="confirmation"`. |
| **Feedback imediato e não-bloqueante** | Operações assíncronas (salvar, excluir, health check) retornam `aegis-toast` (success/error/warning/info) — nunca `alert()` nativo. | `toast.success()` para criações; `toast.error()` com ação "Tentar novamente" para falhas de rede. |
| **Acessibilidade como requisito, não opcional** | Todos os componentes do [component-library.md](component-library.md) implementam `role`, `aria-*`, `:focus-visible`, `tabindex`, `aria-live`. Testados com NVDA/VoiceOver. | Focus ring obrigatório (`--shadow-focus`); `prefers-reduced-motion` respeitado (`--duration-*` → `0.01ms`). |
| **Consistência acima de customização pontual** | **Zero valores hardcoded**. Todo valor visual vem de [design-tokens.md](design-tokens.md). Novos tokens passam por governança (Seção 9 do design-tokens). | Lint `no-hardcoded-colors`/`no-hardcoded-spacing` no CI; PR template exige checklist de tokens. |
| **Dark mode como cidadão de primeira classe** | Tokens definem light/dark (Seção 2.3 do design-tokens). Troca via classe `.dark` no `:root` + `localStorage` + `prefers-color-scheme`. | **Decisão pendente:** MVP ou Fase 2? (Ver design-tokens.md §10). Se MVP, implementar toggle no `aegis-header`. |

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Princípios derivados dos user flows (dashboard denso, tabelas, formulários, modais, alertas de hardware) e da stack JS vanilla + `@popperjs/core`. Nenhum design system ou guideline de marca existe no repositório.

---

## 2. Voz Visual

* **Personalidade da marca:** Técnica, confiável, minimalista, funcional. Sem ornamentação decorativa. A cor existe para **comunicar estado** (sucesso/aviso/erro/info) e **hierarquia de ação** (primário/secundário/destrutivo), não para "identidade visual".
* **Referências visuais (moodboard):**  
  - shadcn/ui / Radix UI (componentes acessíveis, tokens CSS, composição via slots)  
  - GitHub / Linear / Vercel dashboards (densidade controlada, tipografia Inter, sidebar colapsável)  
  - Tabelas enterprise (Jira, Datadog, AWS Console) — ordenação, filtros, seleção múltipla, virtualização
* **O que evitar:**  
  - Gradientes decorativos, sombras "flutuantes" excessivas, bordas arredondadas grandes (`--radius-xl`+``) em componentes densos  
  - Cores fora da paleta semântica (Seção 2.2 do design-tokens) — ex: roxo, rosa, laranja não definidos  
  - Ícones sem label acessível ou tooltip (`aegis-tooltip`)  
  - Animações "divertidas" (`--duration-slower`+``) em fluxos de alta frequência (tabelas, formulários)  
  - Breakpoints não definidos em [design-tokens.md §5](design-tokens.md#5-breakpoints)

---

## 3. Layout & Grid

### 3.1 Sistema de Grid
* **Base:** CSS Grid + Flexbox (sem framework CSS). Container principal: `max-width: 1400px` (`--bp-2xl` menos `--space-8` lateral) centrado com `margin-inline: auto`.
* **Colunas:** 12 colunas conceituais (não classes utilitárias) — implementadas via `grid-template-columns: repeat(12, 1fr)` no layout de página; componentes internos usam `gap: var(--space-4)` a `var(--space-6)`.
* **Sidebar fixa (desktop ≥ `--bp-lg`):** `width: 260px` (expandida) / `72px` (colapsada) — `aegis-sidebar` com `position: sticky; top: 0; height: 100vh; z-index: var(--z-sticky)`.
* **Header fixo:** `height: 56px` (`--space-14` aproximado) — `aegis-header` com `position: sticky; top: 0; z-index: var(--z-sticky); background: var(--color-bg-primary); border-bottom: 1px solid var(--color-border-light)`.

### 3.2 Densidade de Informação
| Contexto | Densidade | Tokens de Espaçamento | Exemplo |
| :--- | :--- | :--- | :--- |
| **Dashboard / Listas densas** | Compacta | `--space-2` (8px) gap vertical, `--space-3` (12px) horizontal | `aegis-data-table` rows, `aegis-stat-card` grid |
| **Formulários / Detalhes** | Confortável | `--space-4` (16px) gap vertical, `--space-6` (24px) entre seções | `aegis-form`, `aegis-modal variant="form"` |
| **Leitura / Empty states** | Espaçada | `--space-8` (32px) a `--space-12` (48px) | `aegis-empty-state`, `aegis-card variant="elevated"` |

### 3.3 Padrões de Página

| Tipo de Página | Estrutura | Componentes-Chave |
| :--- | :--- | :--- |
| **Lista/Tabela (Ativos, Manutenções, Auditoria)** | `aegis-header` + `aegis-sidebar` + `<main class="aegis-page"><aegis-breadcrumb>` + `aegis-data-table` (com `slot="header-actions"` para `aegis-filter-bar` + `aegis-button primary`) + `aegis-pagination` | `aegis-data-table`, `aegis-filter-bar`, `aegis-button`, `aegis-dropdown` (ações por linha), `aegis-badge` (status) |
| **Detalhe / Formulário (Criar/Editar Ativo, Manutenção)** | `aegis-header` + `aegis-sidebar` + `<main><aegis-breadcrumb>` + `aegis-card variant="outlined"` (ou `elevated`) com `aegis-form` interno (`aegis-input`, `aegis-select`, `aegis-date-picker`, `aegis-textarea`, `aegis-switch`) + `slot="footer"` com `aegis-button secondary` (Cancelar) + `primary` (Salvar) | `aegis-form`, `aegis-card`, `aegis-input`, `aegis-select`, `aegis-date-picker`, `aegis-button` |
| **Dashboard (Visão Geral, Health Checks)** | `aegis-header` + `aegis-sidebar` + `<main class="aegis-dashboard"><aegis-stat-card>` grid (4 colunas ≥ `--bp-xl`, 2 colunas `--bp-md`, 1 coluna `< --bp-sm`) + `aegis-card` com `aegis-health-indicator` list + `aegis-data-table` resumido (últimos 5 alertas) | `aegis-stat-card`, `aegis-health-indicator`, `aegis-card`, `aegis-data-table` |
| **Login / Auth** | Centralizado vertical/horizontal, `max-width: 400px`, sem sidebar/header. `aegis-card variant="elevated"` + `aegis-login-form` | `aegis-login-form`, `aegis-card`, `aegis-button primary full-width` |
| **Modal / Overlay** | Portal no `body` (ou `aegis-toast-container` para toasts). `aegis-modal` gerencia focus trap, backdrop, `Esc`. `aegis-dropdown`/`aegis-tooltip` usam `@popperjs/core` com `z-index: var(--z-dropdown)`/`var(--z-tooltip)`. | `aegis-modal`, `aegis-dropdown`, `aegis-tooltip`, `aegis-toast`, `aegis-confirm-dialog` |

---

## 4. Uso de Cor

### 4.1 Hierarquia Visual via Cor
| Camada | Tokens | Regra |
| :--- | :--- | :--- |
| **Ação Primária** | `--btn-primary-bg` (`--color-accent-primary`), `--btn-primary-hover`, `--btn-primary-text` | **Exatamente 1 botão `primary` por viewport** (ex: "Salvar", "Criar", "Confirmar"). |
| **Ação Secundária** | `--btn-secondary-bg` (`--color-bg-tertiary`), `--btn-secondary-hover` (`--color-border-medium`) | "Cancelar", "Voltar", "Limpar filtros". Pode haver múltiplos. |
| **Ação Destrutiva** | `--btn-danger-bg` (`--color-danger`), `--btn-danger-hover` (10% mais escuro) | **Sempre** abre `aegis-confirm-dialog variant="delete"` — nunca ação direta. |
| **Estado Sucesso** | `--color-success`, `--color-success-light`, `--badge-success-bg/text` | Badges "Concluído", "Saudável", toasts success, health OK. |
| **Estado Aviso** | `--color-warning`, `--color-warning-light`, `--badge-warning-bg/text` | Badges "Pendente", "Atrasado", "Disco > 85%", toasts warning. |
| **Estado Erro/Crítico** | `--color-danger`, `--color-danger-light`, `--badge-danger-bg/text` | Badges "Crítico", "Falha", "Vencido", inline errors, toasts error. |
| **Estado Info** | `--color-info`, `--color-info-light`, `--badge-info-bg/text` | Badges "Em andamento", "Aguardando aprovação", toasts info, tooltips. |
| **Texto/Conteúdo** | `--color-text-primary`, `--color-text-secondary`, `--color-text-muted` | Hierarquia: primary (títulos, corpo), secondary (labels, metadados), muted (placeholders, timestamps, disabled). |
| **Superfícies** | `--color-bg-primary`, `--color-bg-secondary`, `--color-bg-tertiary`, `--color-border-light`, `--color-border-medium` | Camadas: página (primary), cards/sidebar (secondary), inputs disabled/hover sutil (tertiary). Bordas: light (padrão), medium (focus/selecionado). |

### 4.2 Regras de Contraste Mínimo
* **Texto:** WCAG AA 4.5:1 (normal) / 3:1 (large text ≥ 18px ou 14px bold) — validar todas as combinações `text-*` sobre `bg-*` em light **e** dark mode.
* **Elementos de UI (bordas, ícones, focus rings):** WCAG AA 3:1 — `--color-border-light` sobre `bg-primary`, `--shadow-focus` (`--color-accent-light` 3px) sobre qualquer background.
* **Badges/Indicadores de status:** Texto do badge (`--badge-*-text`) sobre background do badge (`--badge-*-bg`) **deve** passar 4.5:1.
* **Validação:** Script CI `npm run a11y:contrast` (axe-core + paleta de tokens) — falha se qualquer combinação semântica falhar.

### 4.3 Uso de Cor Semântica — Onde Permitido / Proibido
| Componente / Contexto | Permitido | Proibido |
| :--- | :--- | :--- |
| `aegis-button` | `variant="primary|secondary|destructive|ghost|outline"` (mapeia tokens semânticos) | `style="background: #2563EB"` ou qualquer hex literal |
| `aegis-badge` | `variant="success|warning|danger|info|neutral"` (tokens `--badge-*-bg/text`) | `variant="custom"` com cor arbitrária |
| `aegis-health-indicator` | `status="ok|warning|critical|unknown"` (tokens `--health-ok/warn/critical`) | Cores customizadas por métrica |
| `aegis-alert-banner` / `aegis-toast` | `variant="success|warning|danger|info"` | Cores fora da paleta semântica |
| **Gráficos / Sparklines** (futuro `aegis-chart`) | Paleta categórica derivada dos tokens semânticos (sucesso=verde, aviso=âmbar, perigo=vermelho, info=ciano) | Paletas arbitrárias (ex: Category10, Tableau) sem mapeamento semântico |
| **Texto decorativo / Ilustrações** | `--color-text-muted` para linhas decorativas, divisores sutis | Cores de marca não definidas nos tokens |

---

## 5. Tipografia em Contexto

| Elemento | Token de Tipografia | Cor | Peso | Exemplo de Uso |
| :--- | :--- | :--- | :--- | :--- |
| **H1 — Título de Página** | `--text-xl` (1.25rem / 20px) | `--color-text-primary` | `600` | `<h1 class="aegis-page__title">Ativos de TI</h1>` |
| **H2 — Seção / Card Header** | `--text-lg` (1.125rem / 18px) | `--color-text-primary` | `500` | `<h2 class="aegis-card__header">Filtros Avançados</h2>` |
| **H3 — Subseção / Modal Title** | `--text-base` (1rem / 16px) | `--color-text-primary` | `600` | `<h3 id="modal-title" class="aegis-modal__title">Excluir Ativo</h3>` |
| **Corpo Principal (p, td, li, label)** | `--text-base` (1rem / 16px) | `--color-text-primary` | `400` | `<p class="aegis-text">Descrição do ativo...</p>` |
| **Corpo Secundário / Ajuda / Tooltip** | `--text-sm` (0.875rem / 14px) | `--color-text-secondary` | `400` | `<span class="aegis-input__helper">Máx. 255 caracteres</span>` |
| **Label de Input / Select / Checkbox** | `--text-sm` (0.875rem / 14px) | `--color-text-primary` | `500` | `<label class="aegis-label">Nome do Ativo</label>` |
| **Caption / Metadados / Timestamp / Badge Text** | `--text-xs` (0.75rem / 12px) | `--color-text-muted` | `400` | `<span class="aegis-table__meta">Atualizado há 2h</span>` |
| **Código / ID / Serial / IP / Métrica Técnica** | `--text-sm` + `--font-mono` | `--color-text-primary` | `400` | `<code class="aegis-code">NB-2024-001234</code>` |
| **Botão (Label)** | `--text-sm` (sm), `--text-base` (md), `--text-lg` (lg) | Conforme `--btn-*-text` | `500` | `<aegis-button size="md">Salvar</aegis-button>` |
| **Tabela Header** | `--text-xs` (0.75rem / 12px) | `--color-text-secondary` | `600` | `<th class="aegis-th">Tag do Ativo</th>` |
| **Empty State Title** | `--text-lg` (1.125rem / 18px) | `--color-text-primary` | `500` | `<h3 class="aegis-empty__title">Nenhum ativo encontrado</h3>` |
| **Empty State Description** | `--text-base` (1rem / 16px) | `--color-text-secondary` | `400` | `<p class="aegis-empty__desc">Comece cadastrando seu primeiro ativo.</p>` |

> **Fonte:** `--font-sans` (`Inter` + system fallback) para todo texto UI; `--font-mono` (`JetBrains Mono` + fallback) para dados técnicos. Ver [design-tokens.md §3](design-tokens.md#3-typography).

---

## 6. Padrões de Conteúdo (UX Writing)

### 6.1 Tom de Voz nos Textos de UI
* **Direto, imperativo, sem jargão desnecessário.**  
  - ✅ "Cadastrar ativo" — ❌ "Clique aqui para iniciar o processo de cadastro de um novo ativo"  
  - ✅ "Disco acima de 85%" — ❌ "O sistema detectou que o armazenamento está próximo da capacidade máxima"
* **Objetivo, informativo, sem culpa do usuário.**  
  - ✅ "Email inválido. Verifique o formato." — ❌ "Você digitou um email errado."
* **Consistência de terminologia do domínio:**  
  - "Ativo" (não "equipamento", "item", "bem")  
  - "Manutenção" (não "service", "OS", "chamado")  
  - "Filial" (não "unidade", "local", "site")  
  - "Health check" (não "verificação de saúde", "monitoramento")  
  - "Baixa patrimonial" (não "descarte", "exclusão definitiva")

### 6.2 Padrões de Mensagens de Erro
**Estrutura obrigatória:** `[O que aconteceu] + [Como resolver] + [Ação sugerida (se houver)]`

| Tipo | Template | Exemplo |
| :--- | :--- | :--- |
| **Validação de campo (inline)** | `"{Campo}: {regra violada}. {Correção}."` | "Tag do ativo: obrigatório. Preencha com o código patrimonial." |
| **Erro de API (toast/error banner)** | `"{Ação} falhou: {motivo técnico resumido}. {Ação do usuário}."` | "Salvar ativo falhou: tag duplicada. Verifique o código e tente novamente." |
| **Erro de rede/servidor** | `"Não foi possível {ação}. Verifique sua conexão e tente novamente."` | "Não foi possível carregar a lista. Verifique sua conexão e tente novamente." |
| **Permissão negada** | `"Você não tem permissão para {ação}. Contate seu administrador."` | "Você não tem permissão para excluir ativos. Contate seu administrador." |

* **Nunca** expor stack traces, IDs de request, códigos HTTP brutos ao usuário final.
* **Sempre** oferecer botão "Tentar novamente" em toasts/banners de erro de rede.

### 6.3 Padrões de Call-to-Action (Botões e Links)
| Contexto | Padrão | Exemplos |
| :--- | :--- | :--- |
| **Botão Primário (ação principal)** | Verbo no imperativo, sem reticências, max 3 palavras | "Salvar", "Criar ativo", "Iniciar manutenção", "Aprovar", "Confirmar exclusão" |
| **Botão Secundário** | "Cancelar", "Voltar", "Limpar", "Fechar" | — |
| **Botão Destrutivo (em modal de confirmação)** | "Excluir", "Baixar patrimônio", "Revogar acesso", "Desativar" | — |
| **Link (navegação, não ação)** | Substantivo ou frase curta, sublinhado no hover | "Ver detalhes", "Histórico de manutenções", "Configurações avançadas" |
| **Ação em Toast (ex: "Desfazer")** | Verbo no imperativo, curto | "Desfazer", "Ver detalhes", "Tentar novamente" |

### 6.4 Formatação de Números, Datas, Moeda
| Tipo | Formato (pt-BR) | Exemplo | Componente/Token |
| :--- | :--- | :--- | :--- |
| **Inteiro (quantidade, contadores)** | `1.234` (separador de milhar = ponto) | `1.234 ativos` | `Intl.NumberFormat('pt-BR')` |
| **Decimal (valores, %)** | `1.234,56` (vírgula decimal) | `R$ 12.345,67` / `87,5%` | `Intl.NumberFormat('pt-BR', {style:'currency', currency:'BRL'})` |
| **Data curta (tabelas, timestamps)** | `DD/MM/AAAA` | `15/01/2025` | `aegis-date-picker format="DD/MM/YYYY"` |
| **Data longa (detalhes, auditoria)** | `DD 'de' MMMM 'de' YYYY` | `15 de janeiro de 2025` | `new Date().toLocaleDateString('pt-BR', {dateStyle:'long'})` |
| **Data/hora (logs, auditoria)** | `DD/MM/AAAA HH:mm` | `15/01/2025 14:30` | `toLocaleString('pt-BR', {dateStyle:'short', timeStyle:'short'})` |
| **Tempo relativo (toasts, badges recentes)** | "há X min/h/dias" | "há 5 min", "há 2 dias" | Função utilitária `formatRelativeTime()` |
| **Bytes / Armazenamento** | `GB` / `TB` com 1 decimal | `476,3 GB` / `2,1 TB` | `formatBytes(bytes)` — base 1024 |
| **Porcentagem (health checks)** | `XX%` (inteiro) ou `XX,X%` (1 decimal se < 10%) | `87%` / `5,2%` | `value.toFixed(value < 10 ? 1 : 0) + '%'` |

---

## 7. Iconografia

* **Biblioteca de ícones:** **Lucide** (SVG, MIT license, tree-shakable, ~400 ícones). Carregado via `<svg><use href="#icon-{name}"></use></svg>` a partir de sprite SVG injetado no `index.html` (build step: `vite-plugin-svg-sprite` ou similar).
* **Tamanhos padrão (tokens de espaçamento aplicados ao `width`/`height` do SVG):**

| Tamanho | Token | Valor (px) | Uso |
| :--- | :--- | :--- | :--- |
| `xs` | `--space-3` | `12px` | Badges, chips, `aegis-badge dot`, `aegis-dropdown-item` leading icon |
| `sm` | `--space-4` | `16px` | **Padrão**: botões (`aegis-button`), inputs (`leading-icon`), `aegis-sidebar` items, `aegis-tabs` |
| `md` | `--space-5` | `20px` | `aegis-header` actions, `aegis-modal` header icons, `aegis-stat-card` |
| `lg` | `--space-6` | `24px` | `aegis-empty-state` illustration, `aegis-login-form` logo icon |
| `xl` | `--space-10` | `40px` | `aegis-empty-state` large illustration (opcional) |

* **Regras de uso:**
  1. **Ícone sozinho SEMPRE requer label acessível:** `aria-label` no botão/link ou `aegis-tooltip` associado. Ex: `<aegis-button variant="ghost" aria-label="Filtrar"><svg><use href="#icon-filter"></use></svg></aegis-button>`
  2. **Ícone decorativo (redundante com texto):** `aria-hidden="true"` + `focusable="false"`. Ex: `<aegis-button><svg aria-hidden="true"><use href="#icon-plus"></use></svg> Novo Ativo</aegis-button>`
  3. **Cor do ícone:** Herda `currentColor` (segue cor do texto do pai). Em badges/indicadores, usa token semântico correspondente (`--health-ok`, `--badge-danger-text`, etc.).
  4. **Não usar** ícones como único indicador de estado (ex: cor vermelha + ícone "X" para erro — deve ter texto "Erro" ou label acessível).

* **Ícones mapeados para o domínio (lista inicial):**

| Domínio | Ícones Lucide (nome) |
| :--- | :--- |
| **Ativos** | `laptop`, `monitor`, `server`, `printer`, `phone`, `tablet`, `hard-drive`, `cpu`, `wifi`, `router` |
| **Manutenção** | `wrench`, `hammer`, `screwdriver`, `tool`, `rotate-cw`, `calendar-clock`, `check-circle`, `x-circle` |
| **Status / Health** | `check-circle-2` (ok), `alert-triangle` (warning), `alert-octagon` (critical), `help-circle` (unknown), `activity` (monitoring) |
| **Navegação / UI** | `chevron-left/right/up/down`, `menu`, `x`, `search`, `filter`, `columns`, `download`, `upload`, `refresh-cw`, `more-vertical`, `more-horizontal` |
| **Ações** | `plus`, `edit-2`, `trash-2`, `copy`, `share-2`, `eye`, `eye-off`, `flag`, `bell`, `archive`, `folder-open` |
| **Usuário / RBAC** | `user`, `users`, `user-plus`, `user-minus`, `shield`, `key`, `lock`, `unlock`, `id-card` |
| **Auditoria / Logs** | `history`, `clock`, `git-commit`, `file-text`, `database`, `server-cog` |

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Lista baseada nos user flows e componentes propostos. Requer validação com designer/PO para ícones de domínio específicos (ex: tipo de ativo "Impressora 3D", "Switch de rede").

---

## 8. Imagens & Mídia

### 8.1 Proporções Aceitas
| Contexto | Proporção | Token/Classe | Notas |
| :--- | :--- | :--- | :--- |
| **Avatar (usuário, filial)** | `1:1` (quadrado) | `aegis-avatar size="md"` → `40px` (`--space-10`) | `border-radius: var(--radius-full)` |
| **Thumbnail de ativo (foto, nota fiscal)** | `4:3` ou `16:9` | `aegis-card__media` com `aspect-ratio: 4/3` | `object-fit: cover` |
| **Ilustração Empty State** | `1:1` ou `4:3` | `aegis-empty-state size="lg"` → `200px` max-width | SVG vetorial preferido (scales sem perda) |
| **Logo / Branding (login, header)** | Livre (definido pela marca) | `aegis-header__logo`, `aegis-login-form__logo` | **Pendente:** arquivo de marca não encontrado no repositório |

### 8.2 Tratamento de Placeholder / Loading
* **Skeleton (preferido para listas/tabelas/cards):** `aegis-loading variant="pulse"` ou `skeleton` — usa `--color-bg-tertiary` + animation `pulse 1.5s ease-in-out infinite` (respeita `prefers-reduced-motion`).
* **Blur-up (para imagens grandes):** `<img loading="lazy" src="blur-placeholder.svg" data-src="real.jpg">` + JS `IntersectionObserver` troca `src` ao entrar no viewport.
* **Avatar fallback:** Iniciais do nome (2 letras, maiúsculas) em círculo com background determinístico por hash do nome (ex: `hsl(${hash(nome) * 360}, 60%, 50%)`) — **não** usar imagem genérica "user.png".

### 8.3 Empty States — Padrão Visual
**Estrutura obrigatória (componente `aegis-empty-state`):**
```html
<aegis-empty-state size="md" illustration="inbox">
  <h3 slot="title">Nenhum ativo encontrado</h3>
  <p slot="description">Não há ativos cadastrados nesta filial. Comece adicionando o primeiro.</p>
  <aegis-button variant="primary" slot="action" on-click="openCreateModal">
    <svg slot="icon" aria-hidden="true"><use href="#icon-plus"></use></svg>
    Cadastrar Ativo
  </aegis-button>
</aegis-empty-state>
```
* **Ilustrações nomeadas (SVG no sprite):** `inbox` (lista vazia), `search` (busca sem resultados), `shield-off` (sem permissão), `wifi-off` (offline), `database` (sem dados de health check), `file-text` (auditoria vazia).
* **Sempre** incluir CTA (ação primária) quando houver ação possível. Se não houver ação (ex: sem permissão), omitir `slot="action"`.

---

## 9. Padrões de Layout Responsivo

### 9.1 Estratégia: Mobile-First
* **Base (≤ `--bp-sm` 640px):** Single column, sidebar colapsada em bottom navigation (`aegis-sidebar` vira bottom tab bar com 4-5 itens principais), tabelas com `overflow-x: auto` + `position: sticky` na primeira coluna (tag/nome), formulários em coluna única, modais `size="full"` (bottom sheet).
* **Tablet (`--bp-md` 768px–`--bp-lg` 1024px):** Sidebar colapsável (ícones apenas), grid 2 colunas para dashboard (`aegis-stat-card`), tabelas com todas as colunas visíveis, modais `size="md"`/`lg`.
* **Desktop (`≥ --bp-lg` 1024px):** Sidebar expandida fixa, grid 3-4 colunas dashboard, tabelas densas, modais `size="md"`/`lg`/`xl`, `aegis-filter-bar` expandida horizontalmente.
* **Ultra-wide (`≥ --bp-2xl` 1536px):** Container `max-width: 1400px` centralizado, side-by-side master-detail (lista à esquerda, detalhe à direita), `aegis-data-table` com colunas extras (ações, metadados).

### 9.2 Comportamento de Colapso / Esconder em Telas Pequenas
| Elemento | `< --bp-sm` (Mobile) | `--bp-sm` a `--bp-md` | `≥ --bp-lg` (Desktop) |
| :--- | :--- | :--- | :--- |
| **Sidebar** | Bottom tab bar (4-5 itens) | Colapsada (ícones + tooltip) | Expandida (labels + ícones) |
| **Header actions** | Menu hambúrguer (`aegis-dropdown`) | Ícones + tooltip | Labels + ícones |
| **Tabela (`aegis-data-table`)** | Scroll horizontal, 1ª coluna sticky, ações em `aegis-dropdown` por linha | Scroll horizontal, colunas essenciais visíveis | Todas as colunas, ações inline |
| **Filter bar (`aegis-filter-bar`)** | Colapsada em `aegis-dropdown` "Filtros" | Expandida, 2-3 filtros visíveis + "Mais" | Totalmente expandida |
| **Stat cards (dashboard)** | 1 coluna, scroll vertical | 2 colunas | 4 colunas |
| **Modal** | `size="full"` (bottom sheet, `border-radius: var(--radius-xl) var(--radius-xl) 0 0`) | `size="lg"` | `size="md"`/`lg`/`xl` |
| **Breadcrumb** | Apenas item atual + "Voltar" (truncado) | Últimos 2 itens + ellipsis | Completo |
| **Pagination** | "Anterior/Próximo" apenas | Compacta (página atual ±1) | Completa (first, prev, 1…5, next, last) |

> **Breakpoints definidos em [design-tokens.md §5](design-tokens.md#5-breakpoints):** `--bp-sm: 640px`, `--bp-md: 768px`, `--bp-lg: 1024px`, `--bp-xl: 1280px`, `--bp-2xl: 1536px`.

---

## 10. Checklist de Revisão Visual

Antes de merge de qualquer PR que toque UI, validar:

- [ ] **Tokens only:** Zero valores hardcoded (hex, px, rem, ms, z-index) — apenas `var(--token-name)` ou tokens JS importados. Lint `no-hardcoded-*` passa no CI.
- [ ] **Componentes oficiais:** Todos os elementos UI usam componentes de [component-library.md](component-library.md) (`aegis-*`). Sem `<button class="btn-primary">`, `<table class="table">`, `<div class="modal">` customizados.
- [ ] **Contraste validado:** `npm run a11y:contrast` passa (WCAG AA 4.5:1 texto, 3:1 UI) em **light e dark mode**.
- [ ] **Estados interativos definidos:** `:hover`, `:focus-visible`, `:active`, `:disabled`, `loading`, `error`, `empty`, `skeleton` — todos com tokens correspondentes.
- [ ] **Responsivo testado:** Viewports `375px`, `768px`, `1024px`, `1280px`, `1536px` — sem overflow horizontal involuntário, touch targets ≥ `44px` (`--space-11` aproximado).
- [ ] **Acessibilidade:** `axe-core` zero violations; navegação por teclado funcional (Tab, Enter, Esc, Setas); `aria-live` em toasts/alertas; focus trap em modais/dropdowns.
- [ ] **Motion respeitado:** `@media (prefers-reduced-motion: reduce)` desativa animações (`--duration-*` → `0.01ms`).
- [ ] **Dark mode:** Todos os tokens de cor têm valor dark mode definido (Seção 2.3 design-tokens) e componente renderiza corretamente com `.dark` no `:root`.
- [ ] **Iconografia:** Ícones decorativos com `aria-hidden="true"`; ícones funcionais com label acessível ou tooltip.
- [ ] **UX Writing:** Textos seguem padrões da Seção 6 (tom, erros, CTAs, formatação pt-BR).
- [ ] **Documentação atualizada:** Se novo token ou componente, `design-tokens.md` e `component-library.md` atualizados no mesmo PR.

---

## 11. Referências

* **Design Tokens (fonte única da verdade):** [design-tokens.md](design-tokens.md) — cores, tipografia, espaçamento, sombras, radius, motion, z-index, breakpoints, tokens semânticos de componente.
* **Component Library (inventário + specs):** [component-library.md](component-library.md) — 29 componentes propostos, 6 especificados em detalhe (`aegis-button`, `aegis-data-table`, `aegis-modal`, `aegis-dropdown`, `aegis-toast`, `aegis-health-indicator`).
* **User Flows (requisitos de UI):** [user-flows.md](user-flows.md) — 6 fluxos (Ativos, Manutenção, Health Checks, RBAC, Relatórios, Auth) com estados de UI, métricas, edge cases.
* **Diagnóstico Determinístico:** 348 arquivos Java (backend), 15 arquivos JS (frontend/services), **zero componentes UI existentes**, stack verificada: JavaScript vanilla (ESM) + `@popperjs/core` ^2.11.8.
* **Ferramentas pendentes (ver design-tokens.md §10):** Vite + Style Dictionary (build tokens), Vitest + Testing Library (testes), ESLint/Stylelint + custom rules (lint tokens), Storybook ou catálogo simples (documentação viva).
* **Acessibilidade:** WCAG 2.1 AA, axe-core, teste manual NVDA/VoiceOver.
* **Próximos passos imediatos (do component-library.md §7):**
  1. Configurar `package.json` (`type: "module"`, scripts `dev`/`build`/`test` com Vite + Vitest)
  2. Criar `frontend/tokens.json` + pipeline de build → CSS custom properties + JS tokens
  3. Implementar core: `aegis-button`, `aegis-input`, `aegis-modal`, `aegis-toast` + `aegis-data-table` (crítico)
  4. Configurar lint `no-hardcoded-tokens` no CI
  5. Decisão de produto: Dark mode MVP vs Fase 2; Web Components vs módulos JS vs framework leve

---

## 12. Declaração de Limitações e Gaps Conhecidos

> **Este UI Style Guide NÃO reflete um design system implementado.** O codebase atual **não possui componentes UI, tokens CSS, ou build configurado**.  
>   
> Todo conteúdo acima é **derivado dos artefatos-fonte (design-tokens.md, component-library.md) que por sua vez são 100% inferidos** a partir do diagnóstico determinístico (zero artefatos de design encontrados) e dos requisitos do BRD.  
>   
> **Marcadores `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`** aparecem nas seções onde a decisão final depende de: identidade visual corporativa (cores, fonte, logo), escopo do MVP (dark mode, gráficos, rich text), arquitetura de frontend (Web Components vs módulos vs framework), e priorização de componentes.  
>   
> **Nenhuma implementação deve iniciar** sem sessão de alinhamento com Product Design / Frontend Lead / PO para validar, ajustar ou descartar esta proposta.

---

## 13. Histórico de Revisões

| Versão | Data | Autor | Mudanças |
| :--- | :--- | :--- | :--- |
| 1.0 | 15/01/2025 | IA Assistida (UI Style Guide Generator) | Criação inicial baseada em design-tokens.md v1.0 e component-library.md v1.0. Todas as seções derivadas de artefatos inferidos — **requer validação humana completa**. |