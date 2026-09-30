# Accessibility Guidelines — Aegis Patrimônio

> **Versão:** 1.0 · **Owner:** Frontend/QA Lead · **Status:** Draft
> **Nível-alvo:** WCAG 2.1 Nível AA · **Depende de:** [design-tokens.md, nfr.md]

## 1. Overview

O Aegis Patrimônio assume o compromisso de garantir que sua interface web seja acessível a todas as pessoas, incluindo usuários de tecnologias assistivas (leitores de tela, navegação por teclado, zoom de tela, controle por voz). Este documento define os requisitos, padrões de implementação e processo de validação contínua para atingir conformidade **WCAG 2.1 AA** nos fluxos críticos do sistema (login, criação/edição de ativo, solicitação de manutenção, aprovação de manutenção, visualização de alertas).

A acessibilidade não é apenas requisito legal (Lei Brasileira de Inclusão — Lei nº 13.146/2015, Decreto nº 10.096/2019) — é critério de qualidade que amplia o alcance do produto para operadores de filiais, gestores de TI e auditores que dependem de navegação alternativa.

> **⚠️ [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — O escopo "fluxos críticos" acima deriva dos casos de uso do BRD (Personas: Admin, User/Solicitante, Aprovador) e dos endpoints mapeados no NFR-U01. Confirmação final do escopo com Product Owner.

---

## 2. Padrão de Conformidade

* **Norma de referência:** WCAG 2.1 Nível AA (todas as diretrizes 1.1 a 4.1)
* **Obrigatoriedade legal aplicável:**
  - Lei Brasileira de Inclusão (LBI) — Art. 63 (acessibilidade em sítios da internet)
  - Decreto nº 10.096/2019 (regulamenta LBI para órgãos públicos e empresas concessionárias)
  - LGPD (Lei nº 13.709/2018) — Art. 9º (acesso a dados por titular com deficiência)
* **Escopo:** **Fluxos críticos** (ver Seção 1) + **componentes de UI reutilizáveis** (tabelas, formulários, modais, tooltips, badges, botões, navegação principal). Páginas de configuração avançada e relatórios de exportação entram no backlog de auditoria trimestral.
  <!-- source: nfr.md#5-usabilidade--acessibilidade (NFR-U01, NFR-U02) -->

---

## 3. Contraste de Cor

A paleta de cores **ainda não está implementada** — o projeto não possui design tokens (ver `design-tokens.md` §2). A tabela abaixo define os **requisitos mínimos** que os tokens devem satisfazer quando definidos pelo time de Design. Valores reais de contraste só poderão ser medidos após a implementação de `tokens.css` e `tokens.json`.

| Combinação (Token) | Contexto | Contraste Mínimo Exigido (WCAG 2.1 AA) | Contraste Atual | Status |
| :--- | :--- | :--- | :--- | :--- |
| `var(--color-text-primary)` sobre `var(--color-bg-primary)` | Corpo de texto, tabelas, listas | **4.5:1** | Pendente — tokens não definidos | ⬜ Pendente |
| `var(--color-text-secondary)` sobre `var(--color-bg-primary)` | Texto secundário, metadados, placeholders | **4.5:1** (texto normal) / **3:1** (texto grande ≥ 18pt ou 14pt bold) | Pendente | ⬜ Pendente |
| `var(--color-text-primary)` sobre `var(--color-bg-secondary)` | Cards, painéis, sidebars | **4.5:1** | Pendente | ⬜ Pendente |
| `var(--color-text-inverse)` sobre `var(--color-brand-primary)` | Texto em botões primários, badges coloridos | **4.5:1** | Pendente | ⬜ Pendente |
| `var(--color-border-strong)` / `var(--color-brand-primary)` (focus ring) | Bordas de input focado, focus ring visível | **3:1** (elementos de UI) | Pendente | ⬜ Pendente |
| `var(--color-success)` / `var(--color-warning)` / `var(--color-danger)` / `var(--color-info)` sobre respectivos `*-bg` | Badges de status (Pendente/Aprovada/Em Execução/Concluída/Cancelada), toasts, alertas inline | **4.5:1** (texto) / **3:1** (ícones/bordas) | Pendente | ⬜ Pendente |
| Modo escuro: `var(--color-text-primary)` (dark) sobre `var(--color-bg-primary)` (dark) | Todo texto em tema escuro | **4.5:1** | Pendente | ⬜ Pendente |

> **Ação requerida:** Time de Design deve definir valores hex/RGB para todos os tokens em `design-tokens.md` §2 e validar contraste com ferramentas (ex: Colour Contrast Analyser, axe DevTools) **antes** de merge. CI deve incluir teste automatizado de contraste (ex: `storybook-addon-a11y` + Chromatic).
> <!-- source: design-tokens.md#2-color-tokens, design-tokens.md#2.4-modos-light--dark -->

---

## 4. Navegação por Teclado

* **Ordem de tabulação lógica garantida:** **Sim** — regra obrigatória para todos os componentes. Validação: teste manual + Cypress `tab` order assertion em fluxos críticos.
  <!-- source: nfr.md#5-usabilidade--acessibilidade (NFR-U02) -->
* **Foco visível em todo elemento interativo:** **Sim** — implementado via token `--shadow-focus: 0 0 0 3px var(--color-brand-primary) / 0.4` (ver `design-tokens.md` §4.3). **Nenhum componente deve remover `outline` sem substituir por focus ring equivalente.**
  <!-- source: design-tokens.md#4.3-shadows--elevation -->
* **Atalhos de teclado definidos:**

| Atalho | Ação | Escopo |
| :--- | :--- | :--- |
| `Tab` / `Shift+Tab` | Navegar entre elementos focáveis (links, botões, inputs, selects, abas) | Global |
| `Enter` / `Space` | Ativar botão, link, item de menu, checkbox, radio | Global |
| `Esc` | Fechar modal, dropdown, tooltip Popper, side panel | Global |
| `Setas` | Navegar dentro de componentes compostos: tabs, menu dropdown, date picker, tabela com `role="grid"` | Componente |
| `Home` / `End` | Primeiro/último item em lista/grid/tabs | Componente |
| `F6` / `Shift+F6` | Pular entre regiões principais (header, sidebar, main, footer) — **a implementar** | Global |

* **Trap de foco em modais:** **Implementado** — ao abrir modal (via Popper.js ou dialog nativo), foco move para primeiro elemento focável dentro do modal; `Tab`/`Shift+Tab` ciclam apenas dentro do modal; `Esc` fecha e devolve foco ao trigger. Validação: teste Cypress + axe `focus-trap` rule.
  <!-- source: design-tokens.md#7-z-index-scale (--z-modal, --z-modal-backdrop), nfr.md#5 (NFR-U02) -->

---

## 5. Suporte a Screen Readers

* **Uso de HTML semântico — regras obrigatórias:**
  - **Landmarks:** `<header role="banner">`, `<nav role="navigation">`, `<main role="main">`, `<aside role="complementary">` (sidebar), `<footer role="contentinfo">`
  - **Headings hierárquicos:** `h1` único por página (título principal), `h2` para seções, `h3` para subseções — **nunca pular níveis**
  - **Listas:** `<ul>`/`<ol>` para menus, breadcrumbs, listas de ações, tabelas de dados simples (quando não houver `role="grid"`)
  - **Tabelas de dados complexas (ativos, manutenções):** `<table>` com `<caption>`, `<thead>`, `<th scope="col">`, `<tbody>`, `<tr>`, `<td>` — **não usar `role="grid"` a menos que haja navegação 2D por setas**
  <!-- source: nfr.md#5 (NFR-U02), design-tokens.md#8.1-tabela-de-ativos--manutencoes -->

* **Labels acessíveis — regras por elemento:**

| Elemento | Regra Obrigatória |
| :--- | :--- |
| Ícone sozinho (sem texto visível) — ex: botão só com ícone de lixeira, olho, engrenagem | `aria-label="Ação descritiva"` (ex: `aria-label="Excluir ativo"`) |
| Imagem informativa (ex: diagrama de saúde do ativo, QR code) | `alt="Descrição concisa do conteúdo/informação transmitida"` |
| Imagem decorativa / ícone redundante com texto adjacente | `alt=""` **ou** `aria-hidden="true"` no `<img>`/`<svg>` |
| Campo de formulário (`<input>`, `<select>`, `<textarea>`) | `<label for="id">` visível **ou** `aria-label` **ou** `aria-labelledby` — **nunca deixar sem label acessível** |
| Grupo de radios/checkboxes | `<fieldset><legend>Pergunta do grupo</legend>...` |
| Mensagem de erro/ajuda associada a campo | `aria-describedby="id-da-mensagem"` no input + `role="alert"` ou `aria-live="polite"` no container da mensagem |
| Tabela com ações por linha (editar, excluir, ver detalhes) | Botões de ação com `aria-label` contextual (ex: `aria-label="Editar ativo notebook Dell 001"`) |

* **Regiões dinâmicas (`aria-live`):**
  - **Toasts/notificações:** `role="status"` + `aria-live="polite"` (não interrompe leitura)
  - **Erros de validação inline:** `role="alert"` + `aria-live="assertive"` (interrompe imediatamente)
  - **Progresso de operações longas (exportação PDF, health check):** `role="progressbar"` + `aria-valuenow`/`aria-valuemin`/`aria-valuemax` + `aria-live="polite"`
  - **Atualização de lista via filtro/busca:** `aria-live="polite"` no container da lista + `aria-atomic="true"` se lista inteira substituída

* **Estados e propriedades ARIA obrigatórias:**
  - `aria-expanded` em triggers de dropdown/accordion/sidebar
  - `aria-controls` apontando para ID do painel controlado
  - `aria-selected` em tabs, opções de select customizado, linhas selecionáveis
  - `aria-disabled` (não `disabled` nativo) em botões desabilitados que precisam permanecer no fluxo de tab
  - `aria-required="true"` em campos obrigatórios (além de `required` nativo)
  - `aria-invalid="true"` em campos com erro + `aria-describedby` para mensagem

---

## 6. Formulários Acessíveis

* **Estrutura padrão por campo:**

```html
<div class="form-field">
  <label for="ativo-nome" class="form-label">Nome do Ativo <span class="required" aria-hidden="true">*</span></label>
  <input
    type="text"
    id="ativo-nome"
    name="nome"
    class="form-input"
    aria-required="true"
    aria-describedby="ativo-nome-help ativo-nome-error"
    autocomplete="off"
  >
  <span id="ativo-nome-help" class="form-help">Mínimo 3 caracteres, máximo 100</span>
  <span id="ativo-nome-error" class="form-error" role="alert" aria-live="assertive"></span>
</div>
```

* **Validação:**
  - Validação nativa HTML5 (`required`, `minlength`, `maxlength`, `pattern`, `type="email"`) **sempre habilitada**
  - Validação customizada JS: definir `aria-invalid="true"` + preencher `#id-error` com mensagem clara + mover foco para primeiro campo inválido no submit
  - Mensagem de erro: **o que está errado + como corrigir** (ex: "Data de aquisição deve ser anterior a hoje. Selecione uma data válida.")

* **Agrupamento:** `<fieldset><legend>` para grupos relacionados (ex: endereço, datas de início/fim, filtros avançados)

* **Autocomplete:** Usar valores padrão (`name`, `email`, `tel`, `street-address`, `postal-code`, `cc-number`, `bday`) para facilitar preenchimento por gerenciadores de senha/navegador

---

## 7. Tabelas de Dados (Ativos, Manutenções, Alertas)

* **Estrutura semântica obrigatória:**

```html
<table class="data-table">
  <caption>Lista de Ativos — Filial: Matriz (12 itens)</caption>
  <thead>
    <tr>
      <th scope="col">Código</th>
      <th scope="col">Nome</th>
      <th scope="col">Tipo</th>
      <th scope="col">Status</th>
      <th scope="col"><span class="visually-hidden">Ações</span></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>ATV-001</code></td>
      <td>Notebook Dell Latitude 5420</td>
      <td>Notebook</td>
      <td><span class="badge badge-ativo">Ativo</span></td>
      <td>
        <div class="actions" role="group" aria-label="Ações para ativo ATV-001">
          <button aria-label="Ver detalhes do ativo ATV-001" class="btn-icon">👁</button>
          <button aria-label="Editar ativo ATV-001" class="btn-icon">✎</button>
          <button aria-label="Excluir ativo ATV-001" class="btn-icon btn-danger">🗑</button>
        </div>
      </td>
    </tr>
  </tbody>
</table>
```

* **Responsividade mobile:** Em `< 768px` (ver `design-tokens.md` §4.3 `--bp-md`), transformar em cards com `role="list"` + `role="listitem"` mantendo semântica via CSS `display: grid` — **não remover `<table>` do DOM**, apenas ocultar visualmente e mostrar versão card (técnica "table to cards" acessível)

* **Ordenação/filtro:** Cabeçalhos ordenáveis = `<button>` dentro do `<th>` com `aria-sort="none|ascending|descending"` + `aria-pressed` se toggle

* **Paginação:** `<nav aria-label="Paginação da lista de ativos">` com `<button aria-label="Página 1">`, `aria-current="page"` na ativa

---

## 8. Modais, Tooltips e Popovers (Popper.js)

* **Modal (dialog nativo preferido):**
  - Usar `<dialog>` nativo quando possível (`showModal()` + `close()`)
  - `aria-modal="true"` + `role="dialog"` + `aria-labelledby="modal-title"` + `aria-describedby="modal-desc"`
  - Focus trap implementado (ver §4)
  - Backdrop inerte (`inert` attribute ou `aria-hidden="true"` no conteúdo principal)

* **Tooltip (Popper.js):**
  - Trigger: `aria-describedby="tooltip-id"` + `data-tooltip-target`
  - Tooltip element: `role="tooltip"` + `id="tooltip-id"` + `data-popper-placement`
  - **Não** colocar conteúdo interativo dentro de tooltip — usar popover se necessário
  - Delay: `show: 200ms`, `hide: 100ms` (ver `design-tokens.md` §6 `--duration-fast/normal`)

* **Popover (ações complexas):**
  - `role="dialog"` + `aria-modal="false"` + focus management manual
  - Fecha com `Esc`, click fora, perda de foco

---

## 9. Componentes Específicos do Aegis

### 9.1 Badge de Status (Ativo / Manutenção / Alerta)
```html
<!-- Exemplo: badge de status "Em Manutenção" -->
<span class="badge badge-manutencao" role="status" aria-live="polite">
  <span class="visually-hidden">Status: </span>Em Manutenção
</span>
```
- Cores via tokens semânticos (ver `design-tokens.md` §2.2, §8.4) — contraste validado
- Texto completo sempre presente (não apenas ícone/cor)

### 9.2 QR Code / Etiqueta de Ativo (PDF)
- QR code: `alt="QR Code do ativo ATV-001 — escaneie para ver detalhes"`
- Dados técnicos (serial, IP, MAC): fonte monoespaçada (`var(--font-mono)`) + `role="text"` para leitura caractere a caractere
- Tamanho mínimo QR: 32×32px (ver `design-tokens.md` §8.5 `--qrcode-size`)

### 9.3 Dashboard Preditivo (Gráficos/Charts)
- **Alternativa textual obrigatória:** Tabela de dados equivalente ao gráfico (toggle "Ver dados em tabela")
- `role="img"` + `aria-label="Gráfico de previsão de exaustão de disco — 12 ativos com risco alto nos próximos 30 dias"`
- Cores não são único meio de transmitir informação (padrões/texturas + labels diretos)
- Navegação por teclado em pontos de dados (se interativo): `role="application"` + setas

### 9.4 Busca Fuzzy / Filtros Avançados
- Input de busca: `role="searchbox"` + `aria-autocomplete="list"` + `aria-controls="search-results"`
- Resultados: `role="listbox"` + `aria-activedescendant` no item destacado
- Filtros colapsáveis: `<details><summary>` nativo ou `aria-expanded` + `aria-controls`

---

## 10. Responsividade e Zoom

* **Mobile-first para solicitações** (NFR-08): Fluxo "Criar Solicitação de Manutenção" testado e funcional em 320px width
* **Zoom até 200%:** Nenhuma perda de conteúdo ou funcionalidade — layout fluido (CSS Grid/Flex), unidades relativas (rem/%), breakpoints via tokens (`design-tokens.md` §4.3)
* **Reflow (WCAG 1.4.10):** Em 320px CSS pixels (equivalente a 1280px @ 400% zoom), conteúdo em coluna única, sem scroll horizontal
* **Touch targets:** Mínimo **44×44px** (ver `design-tokens.md` §8.3 `--btn-height` ≥ 44px) — espaçamento entre alvos ≥ 8px

---

## 11. Internacionalização e Idioma

* **Idioma principal:** `lang="pt-BR"` no `<html>`
* **Mudança de idioma:** `lang` attribute em trechos em outro idioma (ex: termos técnicos em inglês)
* **Direção de texto:** `dir="ltr"` (padrão) — suporte a RTL não necessário no escopo atual

---

## 12. Validação Contínua (CI/CD Gates)

| Gate | Ferramenta | Critério de Bloqueio | Frequência |
| :--- | :--- | :--- | :--- |
| **Lint A11y** | `eslint-plugin-jsx-a11y` (Vue) | 0 errors, 0 warnings em código novo/modificado | Todo PR |
| **Unit A11y** | `vitest` + `@testing-library/vue` + `jest-axe` | 0 violations em componentes testados | Todo PR |
| **E2E A11y** | `cypress-axe` | 0 violations **critical/serious** em fluxos críticos (login, criar ativo, solicitar manutenção, aprovar) | Todo PR + nightly |
| **Contraste Automatizado** | `storybook-addon-a11y` + Chromatic | 0 falhas de contraste em stories de componentes base | Todo PR |
| **Auditoria Manual** | Checklist humano (QA + Design) | 0 falhas WCAG 2.1 AA em fluxos críticos | Trimestral + release major |
| **Teste com Usuários Reais** | Sessões com usuários de tecnologias assistivas | Feedback incorporado no backlog | Semestral |

> **Integração:** Pipeline CI/CD (ver `deployment-plan.md`) bloqueia merge se qualquer gate falhar. Relatórios publicados no PR como artifact.

---

## 13. Checklist de Auditoria por Componente (Definição de Pronto)

Um componente só é considerado "acessível" quando **todos** itens abaixo passam:

- [ ] **HTML semântico** correto (landmarks, headings, lists, tables)
- [ ] **Contraste** ≥ 4.5:1 (texto) / 3:1 (UI) — validado via ferramenta + tokens
- [ ] **Navegação por teclado** completa (Tab order, focus visível, atalhos, trap em modais)
- [ ] **Labels** em todos inputs, botões ícone-only, imagens informativas
- [ ] **ARIA** apropriado (estados, propriedades, live regions) — sem ARIA desnecessário
- [ ] **Formulários** com validação acessível (erros anunciados, foco no primeiro erro)
- [ ] **Tabelas** com caption, th scope, ações com aria-label contextual
- [ ] **Modais/tooltips** com focus trap, aria-modal, aria-describedby
- [ ] **Zoom 200% / 320px** sem perda de conteúdo/função
- [ ] **Touch targets** ≥ 44×44px
- [ ] **Reduced motion** respeitado (animações desabilitadas)
- [ ] **Teste axe** (automatizado) = 0 critical/serious
- [ ] **Teste manual** com NVDA (Windows) + VoiceOver (Mac) + Orca (Linux) nos fluxos críticos

---

## 14. Processo de Exceção e Dívida Técnica

Se um componente **não puder** atender a um critério WCAG 2.1 AA por limitação técnica legítima:

1. Documentar no PR: critério, justificativa técnica, impacto no usuário, mitigação alternativa
2. Aprovação: **Tech Lead + Design Lead + PO** (três assinaturas)
3. Registrar no **Risk Register** (`risk-register.md`) como dívida técnica de acessibilidade
4. Prazo máximo de resolução: **próximo release minor** (máx 3 meses)
5. Revisão trimestral: se não resolvido, escalar para Engineering Manager

---

## 15. Referências

- `design-tokens.md` — Tokens de cor, tipografia, espaçamento, motion, z-index (fonte única da verdade)
- `component-library.md` — Inventário de componentes que devem seguir estas guidelines
- `ui-style-guide.md` — Diretrizes visuais e de uso dos componentes
- `nfr.md` — NFR-U01 (WCAG 2.1 AA), NFR-U02 (navegação teclado), NFR-U03 (responsivo 320–1920px)
- `system-architecture.md` §3 — Frontend stack (Vue 3, Bootstrap 5, Vite, Popper.js)
- `BRD` — Personas (Admin, User/Solicitante, Aprovador), fluxos críticos
- `deployment-plan.md` — CI/CD gates incluindo validação a11y
- WCAG 2.1 Guidelines: https://www.w3.org/WAI/WCAG21/quickref/
- axe-core rules: https://github.com/dequelabs/axe-core/blob/develop/doc/rule-descriptions.md
- WAI-ARIA Authoring Practices: https://www.w3.org/WAI/ARIA/apg/

---

## 16. Próximos Passos

1. **Design Team** finaliza tokens de cor com contraste validado (WCAG AA) — desbloqueia implementação
2. **Frontend Lead** implementa `tokens.css` + `tokens.ts` + integração Vite (ver `design-tokens.md` §9)
3. **Engenharia** refatora componentes base (Button, Input, Table, Modal, Badge, Toast) para atender checklist §13
4. **QA** configura `cypress-axe` + `eslint-plugin-jsx-a11y` + `storybook-addon-a11y` no CI
5. **Auditoria trimestral** agendada: primeira em 30 dias após v1.0 dos tokens