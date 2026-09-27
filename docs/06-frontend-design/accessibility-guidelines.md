# Accessibility Guidelines — Aegis Patrimônio

> **Versão:** 1.0 · **Owner:** Frontend/QA Lead · **Status:** Draft  
> **Nível-alvo:** WCAG 2.1 Nível AA · **Depende de:** [design-tokens.md](design-tokens.md), [nfr.md](nfr.md)

---

## 1. Overview

O **Aegis Patrimônio** é um sistema enterprise de gestão de ativos de TI (inventário, manutenção, health checks, alertas, auditoria, relatórios de custo) utilizado por equipes de infraestrutura, help desk, gestores de ativos e auditores. A acessibilidade não é opcional: garante que operadores de campo (muitas vezes em ambientes com iluminação precária, usando teclado ou leitores de tela) e gestores com deficiência visual/motora possam executar fluxos críticos — cadastrar ativo, solicitar manutenção, aprovar baixa, responder a alerta de disco crítico — sem barreiras.

**Compromisso:** Todos os fluxos *in-scope* do BRD (dashboard, CRUD de ativos, manutenção, health checks, alertas, relatórios, administração de usuários/RBAC) devem atender **WCAG 2.1 Nível AA**. Fluxos *out-of-scope* (app mobile nativo, integração AD/LDAP, assinatura digital) seguem o mesmo padrão quando implementados no futuro.

**Estado atual:** O diagnóstico determinístico (348 arquivos Java, 15 arquivos JS) **não revelou nenhuma implementação de acessibilidade** — sem `aria-*`, sem `role`, sem focus management, sem testes automatizados (axe/Lighthouse), sem documentação de contraste. O frontend usa JavaScript vanilla + `@popperjs/core` (apenas para posicionamento de tooltips/dropdowns). Os **Design Tokens** ([design-tokens.md](design-tokens.md)) são **propostas iniciais [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** e ainda não existem no código. Este documento define o *contrato* que a implementação deve cumprir.

---

## 2. Padrão de Conformidade

| Item | Detalhe |
| :--- | :--- |
| **Norma de referência** | WCAG 2.1 Nível AA (todas as 50 diretrizes de A + AA) |
| **Obrigatoriedade legal aplicável** | Lei Brasileira de Inclusão (Lei nº 13.146/2015, Art. 63), Decreto nº 10.098/2019 (acessibilidade em sites governamentais e de interesse público), LGPD (Art. 7º, §1º — consentimento acessível) |
| **Escopo** | **Toda a aplicação web** (15 arquivos JS + HTML/CSS associados). Fluxos críticos prioritários: Login, Dashboard, Listagem/Detalhe/Criação de Ativo, Solicitação/Aprovação/Conclusão de Manutenção, Health Check & Alertas, Relatórios (custo total por ativo), Administração de Usuários & RBAC. |
| **Critério de aceite** | Zero violações WCAG 2.1 AA em auditoria automatizada (axe-core) + zero barreiras em teste manual com NVDA/VoiceOver nos fluxos críticos. |

---

## 3. Contraste de Cor

As combinações abaixo referenciam **tokens semânticos propostos em [design-tokens.md](design-tokens.md) (Seção 2.2 e 8)**. **Todos os valores são hipóteses [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — a validação real só ocorre quando os tokens forem implementados como CSS custom properties e auditados com ferramenta de contraste (ex: `axe-core` + `color-contrast-checker`).

| Combinação (Token FG / Token BG) | Contexto | Contraste Mínimo Exigido (WCAG AA) | Contraste Estimado (Light) | Contraste Estimado (Dark) | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `var(--color-text-primary)` / `var(--color-bg-primary)` | Corpo de texto principal, tabelas, cards | 4.5:1 | 15.3:1 (#212529 on #FFFFFF) | 15.3:1 (#F9FAFB on #111827) | ✅ Projetado p/ AA |
| `var(--color-text-secondary)` / `var(--color-bg-primary)` | Labels, metadados, texto de ajuda | 4.5:1 | 7.0:1 (#495057 on #FFFFFF) | 7.0:1 (#E5E7EB on #111827) | ✅ Projetado p/ AA |
| `var(--color-text-muted)` / `var(--color-bg-primary)` | Placeholders, timestamps, texto desabilitado | 4.5:1 | **4.5:1** (#6C757D on #FFFFFF) — **limite** | **4.5:1** (#9CA3AF on #111827) — **limite** | ⚠️ **Requer validação** — se falhar, escurecer `--color-text-muted` |
| `var(--color-text-primary)` / `var(--color-bg-secondary)` | Texto dentro de cards, sidebars | 4.5:1 | 12.6:1 | 12.6:1 | ✅ Projetado p/ AA |
| `var(--color-accent-primary)` / `var(--color-btn-primary-text)` | Botão primário (texto branco sobre azul) | 4.5:1 | 5.9:1 (#FFFFFF on #2563EB) | 4.5:1 (#FFFFFF on #3B82F6) | ✅ Projetado p/ AA |
| `var(--color-btn-secondary-bg)` / `var(--color-text-primary)` | Botão secundário (texto escuro sobre cinza claro) | 4.5:1 | 7.0:1 | 7.0:1 | ✅ Projetado p/ AA |
| `var(--color-danger)` / `var(--color-btn-primary-text)` | Botão destrutivo (texto branco sobre vermelho) | 4.5:1 | 5.2:1 | 4.5:1 (#FFFFFF on #EF4444) | ✅ Projetado p/ AA |
| `var(--color-border-light)` / `var(--color-bg-primary)` | Bordas de inputs, cards, divisores de tabela | 3:1 (UI components) | 3.0:1 (#DEE2E6 on #FFFFFF) — **limite** | 3.0:1 (#4B5563 on #111827) — **limite** | ⚠️ **Requer validação** — se falhar, escurecer `--color-border-light` |
| `var(--color-focus-ring)` (`var(--color-accent-light)`) / `var(--color-bg-primary)` | Focus ring visível (3px solid) | 3:1 (adjacent colors) | 3.0:1 (#DBEAFE on #FFFFFF) — **limite** | 3.0:1 (#1E3A5F on #111827) — **limite** | ⚠️ **Requer validação** — testar com `--shadow-focus` |
| `var(--color-success)` / `var(--color-badge-success-bg)` | Badge "Concluído/Saudável" (texto verde on verde claro) | 4.5:1 | 5.5:1 (#059669 on #D1FAE5) | 4.5:1 (#10B981 on #064E3B est.) | ✅ Projetado p/ AA |
| `var(--color-warning)` / `var(--color-badge-warning-bg)` | Badge "Pendente/Disco >85%" (texto âmbar on âmbar claro) | 4.5:1 | **4.5:1** (#D97706 on #FEF3C7) — **limite** | **4.5:1** (#F59E0B on #78350F est.) — **limite** | ⚠️ **Requer validação** |
| `var(--color-danger)` / `var(--color-badge-danger-bg)` | Badge "Crítico/Falha" (texto vermelho on vermelho claro) | 4.5:1 | 5.2:1 (#DC2626 on #FEE2E2) | 4.5:1 (#EF4444 on #7F1D1D est.) | ✅ Projetado p/ AA |
| `var(--color-info)` / `var(--color-badge-info-bg)` | Badge "Em andamento" (texto ciano on ciano claro) | 4.5:1 | 4.8:1 (#0891B2 on #CFFAFE) | 4.5:1 (#06B6D4 on #164E63 est.) | ✅ Projetado p/ AA |

> **Ação obrigatória antes de merge:** Rodar `npm run a11y:contrast` (script a criar) que extrai tokens do `tokens.json` buildado e valida todas as combinações acima contra WCAG AA (texto normal 4.5:1, texto grande 3:1, UI 3:1). Falha no pipeline se qualquer combinação estiver abaixo.

---

## 4. Navegação por Teclado

| Requisito | Especificação | Validação |
| :--- | :--- | :--- |
| **Ordem de tabulação lógica** | Ordem DOM = ordem visual. Em layouts complexos (dashboard com grid, master-detail), usar `tabindex="0"` apenas onde necessário e evitar `tabindex > 0`. | Teste manual: Tab/Shift+Tab em cada tela; auditoria axe `tabindex` rule. |
| **Foco visível em todo elemento interativo** | **Nunca** `outline: none` sem substituição. Usar `var(--shadow-focus)` ([design-tokens.md#4.3](design-tokens.md#43-shadows--elevation)): `box-shadow: 0 0 0 3px var(--color-accent-light); outline: none;`. Aplicar a: `<button>`, `<a>`, `<input>`, `<select>`, `<textarea>`, `<summary>`, `[role="button"]`, `[tabindex="0"]`, itens de menu Popper.js. | Teste automatizado: `axe-core` rule `focus-visible`; teste manual: Tab em toda a app. |
| **Skip link** | Primeiro elemento focável na página: `<a href="#main-content" class="skip-link">Pular para conteúdo principal</a>` — visível apenas no foco (`position: absolute; top: -100%;` → `top: var(--space-4)` no `:focus`). | Presente em `index.html` / layout base. |
| **Atalhos de teclado globais** | | |
| `Esc` | Fechar modal, dropdown, tooltip, side panel, toast | Global — implementado via event listener em `document` |
| `Tab` / `Shift+Tab` | Navegar entre elementos focáveis | Nativo — não quebrar |
| `Enter` / `Space` | Ativar botão, link, checkbox, radio, accordion summary | Nativo — não quebrar |
| `Setas` | Navegar dentro de componentes compostos: menu (Popper.js), tabs, tree view, date picker, autocomplete | Implementado em cada componente (ver Seção 5) |
| `Home` / `End` | Primeiro/último item em listas longas (tabelas, menus) | Opcional — recomendado para tabelas > 50 linhas |
| `F6` / `Shift+F6` | Pular entre regiões principais: header (nav), sidebar, main, footer | Implementado via `tabindex="0"` em landmarks + JS |
| **Focus trap em modais/drawers** | Ao abrir: salvar elemento anteriormente focado, mover foco para primeiro elemento focável do modal (ou `role="dialog"` com `aria-modal="true"`), prender Tab/Shift+Tab dentro do modal, restaurar foco ao fechar. Usar `focus-trap` lib (1.5KB) ou implementação própria ≤ 30 linhas. | Teste: abrir modal → Tab não sai do modal → Esc fecha → foco retorna ao trigger. |
| **Focus management em navegação SPA** | Se houver navegação client-side (não detectada no diagnóstico), ao mudar rota: mover foco para `<main>` ou heading `h1` da nova view, anunciar mudança via `aria-live="polite"`. | Não aplicável hoje (MPA provável) — revisar se SPA for adotado. |

---

## 5. Suporte a Screen Readers

### 5.1 HTML Semântico & Landmarks

| Regra | Implementação Obrigatória |
| :--- | :--- |
| **Landmarks** | Toda página: `<header role="banner">`, `<nav role="navigation" aria-label="Navegação principal">`, `<main role="main" id="main-content">`, `<aside role="complementary" aria-label="Painel lateral">` (se houver), `<footer role="contentinfo">`. |
| **Headings hierárquicos** | Exatamente **um** `<h1>` por página (título da view). Subseções: `<h2>` → `<h3>` → `<h4>` sem pular níveis. Em cards de dashboard: `<section aria-labelledby="card-title">` + `<h2 id="card-title">`. |
| **Listas** | Menus: `<ul><li><a>...</a></li></ul>`. Tabelas de dados: `<table><thead><tr><th scope="col">...</th></tr></thead><tbody>...`. Não usar `<div>` para listas navegáveis. |
| **Botões vs Links** | `<button>` para ações (salvar, excluir, abrir modal). `<a href="...">` para navegação (ir para detalhe do ativo, paginação). Nunca `<a href="#">` com `onclick`. |
| **Formulários** | Todo `<input>`, `<select>`, `<textarea>` tem `<label for="id">` visível **ou** `aria-label`/`aria-labelledby` se design sem label visível (ex: busca com ícone de lupa). `required` + `aria-required="true"` para campos obrigatórios. |
| **Tabelas complexas** | `<th scope="col">` para cabeçalhos de coluna, `<th scope="row">` para cabeçalhos de linha (ex: nome do ativo na primeira coluna). `caption` visível ou `aria-label` descrevendo a tabela. |
| **Popper.js tooltips/dropdowns** | Tooltip: `role="tooltip"`, `id` único, trigger com `aria-describedby="tooltip-id"`. Dropdown/menu: `role="menu"`, itens `role="menuitem"`, trigger `aria-haspopup="true" aria-expanded="false/true" aria-controls="menu-id"`. Setas ↑↓ navegam, Enter/Space seleciona, Esc fecha. |

### 5.2 Labels Acessíveis — Regras por Elemento

| Elemento | Regra Obrigatória | Exemplo no Aegis |
| :--- | :--- | :--- |
| **Ícone sozinho (sem texto visível)** | `aria-label="Ação descritiva"` no `<button>` ou `<a>`. Ex: botão "Excluir ativo" só com ícone de lixeira → `aria-label="Excluir ativo {{nome}}"` | `button[aria-label="Excluir ativo Servidor-01"]` |
| **Imagem informativa** | `alt="Descrição concisa do conteúdo/função"`. Gráficos de health check: `alt="Gráfico de uso de disco: 87% — acima do limite 85%"` | `<img src="disk-chart.png" alt="Uso de disco: 87% — crítico">` |
| **Imagem decorativa** | `alt=""` **ou** `role="presentation"` **ou** `aria-hidden="true"`. Ícones decorativos dentro de botão com texto: `aria-hidden="true"` no `<svg>`. | `<svg aria-hidden="true">...</svg> <span>Salvar</span>` |
| **Campo de formulário** | `<label for="input-id">Label visível</label> <input id="input-id" ...>`. Se label invisível por design: `<input aria-label="Label acessível" ...>`. | `<label for="asset-tag">Tag do ativo</label> <input id="asset-tag" required>` |
| **Mensagem de erro** | `<div id="error-msg" role="alert" aria-live="assertive">...</div>` + input com `aria-describedby="error-msg" aria-invalid="true"`. | Ver Seção 6. |
| **Região dinâmica (toast, contador, resultado de busca)** | Container com `aria-live="polite"` (não interrompe) ou `assertive` (crítico). `aria-atomic="true"` se toda a região deve ser lida. | `<div id="toast-container" aria-live="polite" aria-atomic="true"></div>` |

### 5.3 Regiões Dinâmicas (`aria-live`) — Onde Usar

| Região | `aria-live` | `aria-atomic` | Trigger |
| :--- | :--- | :--- | :--- |
| **Toast/Notificação** | `polite` | `true` | Sucesso/erro ao salvar, alerta de health check crítico |
| **Contador de alertas no header** | `polite` | `true` | Atualização via polling/WebSocket (job `AlertNotificationService.checkResourceUsageAlerts`) |
| **Resultados de busca/filtro de tabela** | `polite` | `false` | Digitação no input de busca (debounced 300ms) |
| **Validação inline de formulário** | `assertive` | `true` | Blur/submit com erro — anunciado imediatamente |
| **Progresso de operação longa** (ex: importação CSV, geração de relatório) | `polite` | `true` | Atualização de % concluído |

---

## 6. Formulários Acessíveis

| Aspecto | Padrão Obrigatório | Referência |
| :--- | :--- | :--- |
| **Associação label/input** | Sempre `<label for="id">` + `<input id="id">`. Se layout impede label visível: `aria-label` no input + `visually-hidden` label para SEO/acessibilidade. | [design-tokens.md#8](design-tokens.md#8-component-level-tokens-semanticos) — tokens `--input-*` para estilo consistente |
| **Campos obrigatórios** | `required` + `aria-required="true"` + indicador visual (asterisco) + texto "Obrigatório" no label ou `aria-describedby` apontando para texto oculto. | |
| **Mensagens de erro** | Container `role="alert" aria-live="assertive"` associado via `aria-describedby` no input. Input com `aria-invalid="true"`. Estilo: borda `var(--color-danger)`, ícone + texto. Limpar `aria-invalid` e remover `aria-describedby` ao corrigir. | NFR-SEC03 (validação de entrada) |
| **Mensagens de sucesso/ajuda** | `aria-live="polite"` + `aria-describedby` no input (opcional). Não usar `role="alert"`. | |
| **Validação: inline vs submit** | **Inline (on blur/change):** para feedback imediato (formato de email, tag duplicada via API). **No submit:** para regras de negócio complexas (dependência entre campos, verificação de permissão). Ambos acessíveis: inline usa `aria-live="polite"`, submit usa `role="alert"`. | |
| **Autocomplete / Combobox** | Padrão ARIA 1.2 `combobox`: input `role="combobox" aria-autocomplete="list" aria-controls="listbox-id" aria-expanded="true/false"`, listbox `role="listbox" id="listbox-id"`, options `role="option" aria-selected="true/false"`. Setas navegam, Enter seleciona, Esc fecha. | Usado em: seleção de ativo pai, responsável, categoria |
| **Date picker** | Input `type="text"` + `aria-describedby` para formato + botão trigger `aria-haspopup="dialog"`. Popup `role="dialog" aria-modal="true"` com grid de dias `role="grid"`, navegação por setas. | Manutenção: data agendada, data conclusão |
| **Upload de arquivo** | `<input type="file" aria-label="Anexar comprovante de manutenção">` + área de drop `role="region" aria-label="Área de arrastar arquivos"` + feedback `aria-live="polite"`. | Comprovantes de manutenção, fotos de ativo |

---

## 7. Motion & Preferências do Usuário

| Preferência | Resposta da Aplicação | Implementação |
| :--- | :--- | :--- |
| **`prefers-reduced-motion: reduce`** | **Todas** as animações/transições (`var(--duration-*)` em [design-tokens.md#6](design-tokens.md#6-motion-tokens)) tornam-se `0.01ms` (quase instantâneo) mantendo estado final. Focus ring, hover, dropdown slide, modal fade, accordion, skeleton → content, tooltip show/hide — todos respeitam. | CSS: `@media (prefers-reduced-motion: reduce) { *, *::before, *::after { animation-duration: 0.01ms !important; transition-duration: 0.01ms !important; } }` + JS: `window.matchMedia('(prefers-reduced-motion: reduce)').matches` para pular animações programáticas (ex: scroll suave). |
| **`prefers-color-scheme: dark`** | Aplicar tema escuro automaticamente (tokens `--color-*` dark mode em [design-tokens.md#2.3](design-tokens.md#23-modos-light--dark)). Toggle manual no header persiste em `localStorage` e sobrescreve preferência do sistema. | CSS custom properties no `:root` e `:root.dark` + JS toggle. **Decisão pendente [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**: Dark mode é MVP ou Fase 2? (BRD não especifica). |
| **`prefers-contrast: more`** | Aumentar contraste: bordas mais espessas (2px), focus ring 4px, textos `--color-text-muted` → `--color-text-secondary`. Implementar via `@media (prefers-contrast: more)` sobrescrevendo tokens. | Opcional AAA — avaliar custo/benefício. |
| **`forced-colors: active`** (Windows High Contrast) | Garantir que bordas, focus ring, estados de botão usem `currentColor` ou `CanvasText`/`Highlight` system colors. Remover backgrounds decorativos que somem no modo forçado. Testar no Windows High Contrast Mode. | Obrigatório para enterprise. |

---

## 8. Processo de Validação

| Etapa | Ferramenta / Método | Frequência | Responsável | Critério de Passe |
| :--- | :--- | :--- | :--- | :--- |
| **Lint de acessibilidade no PR** | `eslint-plugin-jsx-a11y` (se React) — **não aplicável** (JS vanilla). Substituir por: `axe-core` CLI no HTML renderido + `html-validate` com regras a11y. | **Todo PR** que toca frontend (JS/HTML/CSS) | Autor do PR + CI | Zero violações `critical` + `serious` (axe). `moderate` permitidos com justificativa. |
| **Teste automatizado em CI** | `playwright` + `@axe-core/playwright` rodando suíte de fluxos críticos (login, criar ativo, solicitar manutenção, dashboard alertas). | **Todo PR** + **nightly** | QA / CI Pipeline | 0 violações WCAG 2.1 AA nos fluxos cobertos. |
| **Auditoria automatizada completa** | `axe-core` CLI ou `Lighthouse CI` em build de staging (todas as rotas). | **Semanal** + **pré-release** | QA Lead | Relatório sem regressões; novas violações = blocker. |
| **Teste manual com screen reader** | **NVDA** (Windows) + **VoiceOver** (macOS/iOS) + **JAWS** (se licença). Roteiro: 10 fluxos críticos (ver Seção 2). | **Trimestral** + **após refatoração major** | QA + 1 dev | Zero barreiras bloqueantes; issues documentadas no backlog com severidade. |
| **Teste 100% navegação por teclado** | Desconectar mouse; percorrer todos os fluxos críticos só com Tab/Shift+Tab/Enter/Esc/Setas. Verificar: ordem lógica, focus visível, focus trap, skip link, atalhos. | **Todo release candidate** | QA + 1 dev | Zero armadilhas de foco; todos os componentes interativos alcançáveis e operáveis. |
| **Validação de contraste** | Script custom (`npm run a11y:contrast`) lendo `tokens.json` buildado + `color-contrast-checker` (WCAG AA). | **Todo PR** que altera `tokens.json` + **semanal** | CI / Frontend Lead | 100% combinações ≥ 4.5:1 (texto) / 3:1 (UI/large text). |
| **Revisão de código com checklist a11y** | Checklist no PR template: `[ ] Semantic HTML`, `[ ] Labels/aria-label`, `[ ] Focus management`, `[ ] aria-live regions`, `[ ] Contraste tokens`, `[ ] Reduced motion`, `[ ] Testado com teclado`. | **Todo PR** frontend | Reviewer | Checklist aprovado = merge permitido. |

---

## 9. Débitos de Acessibilidade Conhecidos

> **Base:** Diagnóstico determinístico (zero a11y implementado) + NFR-U01/U02/U03/U04 não validados + Design Tokens propostos mas não implementados.

| Item | Severidade | Componente/Tela Afetada | Descrição | Prazo de Correção | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Ausência total de atributos ARIA** | Crítica | Toda a aplicação (15 arquivos JS + HTML) | Sem `role`, `aria-*`, `aria-live`, `aria-describedby`, `aria-expanded`, `aria-controls` em nenhum componente. | Sprint 1 (fundação) | 🔴 Aberto |
| **Focus management inexistente** | Crítica | Modais, dropdowns (Popper.js), sidebars, toasts | Sem focus trap, sem restauração de foco, sem skip link, foco não gerenciado em navegação. | Sprint 1 | 🔴 Aberto |
| **Contraste não validado** | Alta | Todos os tokens semânticos ([design-tokens.md#3](design-tokens.md#3-contraste-de-cor)) | 6 combinações no limite (3:1 ou 4.5:1 exato) — podem falhar em dark mode ou monitores calibrados. | Sprint 1 (após tokens implementados) | 🟡 Pendente validação |
| **Navegação por teclado quebrada em componentes Popper.js** | Alta | Dropdowns de ação (linha de tabela), tooltips, seletores customizados | Popper.js posiciona mas não gerencia teclado (setas, Esc, Enter, Tab). | Sprint 2 | 🔴 Aberto |
| **Formulários sem labels/aria-describedby** | Alta | Criação/edição de ativo, manutenção, usuário, filtros | Inputs sem `<label for>`, erros não anunciados, required não exposto a SR. | Sprint 2 | 🔴 Aberto |
| **Tabelas de dados sem semântica completa** | Média | Listagem de ativos, manutenções, health checks, auditoria | Falta `scope="col/row"`, `caption`, `aria-label`, navegação por setas opcional. | Sprint 3 | 🟡 Planejado |
| **Reduced motion não respeitado** | Média | Todas as transições (hover, focus, modal, dropdown, toast, skeleton) | Tokens de motion definidos ([design-tokens.md#6](design-tokens.md#6-motion-tokens)) mas sem `@media (prefers-reduced-motion)` no CSS. | Sprint 2 | 🟡 Planejado |
| **Dark mode não implementado** | Média | Toda a aplicação | Tokens dark mode propostos ([design-tokens.md#2.3](design-tokens.md#23-modos-light--dark)) mas sem CSS/JS toggle. **Decisão de produto pendente [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**. | Fase 2 (se aprovado) | ⚪ Decisão pendente |
| **Testes automatizados a11y ausentes no CI** | Alta | Pipeline de build/deploy | Sem `axe-core`, `playwright`, `lighthouse-ci` configurados. | Sprint 1 (infra) | 🔴 Aberto |
| **Documentação de atalhos de teclado ausente** | Baixa | Help/Onboarding | Usuários não sabem atalhos (Esc, F6, setas em menus). | Sprint 3 | 🟢 Backlog |

---

## 10. Referências

* [WCAG 2.1 Quick Reference](https://www.w3.org/WAI/WCAG21/quickref/) — checklist oficial
* [WAI-ARIA Authoring Practices 1.2 (APG)](https://www.w3.org/WAI/ARIA/apg/) — padrões de componentes (combobox, dialog, menu, table, tabs, tooltip)
* [axe-core Rules](https://github.com/dequelabs/axe-core/blob/develop/doc/rule-descriptions.md) — regras automatizadas
* [Design Tokens — Aegis Patrimônio](design-tokens.md) — fonte única de cores, espaçamento, tipografia, motion, z-index (Seções 2, 4, 6, 7, 8)
* [Non-Functional Requirements — Aegis Patrimônio](nfr.md) — NFR-U01 a U04, NFR-M04, NFR-M05 (Seções 5, 7)
* [BRD — Aegis Patrimônio](brd.md) — Requisitos de negócio, fluxos críticos, guardrails (latência, custo, compliance)
* [Diagnóstico Determinístico do Codebase](diagnostico.md) — 348 arquivos Java, 15 JS, complexidade ciclomática, console.*, stubs
* [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/) — validação manual de pares de cor
* [NVDA Screen Reader](https://www.nvaccess.org/download/) — teste manual Windows
* [VoiceOver Getting Started](https://support.apple.com/guide/voiceover/welcome/mac) — teste manual macOS/iOS

---

## 11. Declaração de Limitações e Próximos Passos

> **Este documento define o *contrato* de acessibilidade para o Aegis Patrimônio.**  
>   
> **Nenhuma das regras acima está implementada hoje.** O codebase (diagnóstico determinístico) não contém atributos ARIA, focus management, skip links, testes a11y, ou validação de contraste. Os **Design Tokens** são propostas [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] e precisam ser implementados, buildados e auditados antes que o contraste real possa ser medido.  
>   
> **Próximos passos obrigatórios (Sprint 0/1):**
> 1. **Validar tokens** com Design/PO — aprovar cores, tipografia, motion, dark mode (MVP vs Fase 2).
> 2. **Configurar tooling**: Vite + Style Dictionary (build tokens) + `axe-core` + `playwright` + `html-validate` no CI.
> 3. **Implementar fundação a11y**: CSS custom properties (tokens), skip link, focus ring global (`var(--shadow-focus)`), `@media (prefers-reduced-motion)`, `@media (prefers-color-scheme)`.
> 4. **Componente por componente**: Botão → Input → Select → Modal → Dropdown (Popper.js) → Tabela → Toast → Formulário completo — cada um com PR dedicado, checklist a11y, teste manual NVDA/VoiceOver.
> 5. **Auditoria baseline**: Rodar suite completa em staging, documentar gaps, priorizar correções no backlog.

---

## 12. Histórico de Revisões

| Versão | Data | Autor | Mudanças |
| :--- | :--- | :--- | :--- |
| 1.0 | 15/01/2025 | IA Assistida (Accessibility Guidelines Generator) | Criação inicial baseada em diagnóstico determinístico (zero a11y), Design Tokens v1.0 (proposto), NFR v1.0 (WCAG 2.1 AA target), BRD v1.0. Todas as seções referenciam artefatos-fonte; combinações de contraste mapeiam tokens propostos; débitos listados exaustivamente. **Marcadores [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] mantidos onde decisões de produto/design são necessárias.** |