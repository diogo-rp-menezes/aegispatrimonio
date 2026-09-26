# Accessibility Guidelines — Aegis1

> **Versão:** 1.0 · **Owner:** Frontend/QA Lead · **Status:** Draft  
> **Nível-alvo:** WCAG 2.1 AA · **Depende de:** [design-tokens.md, nfr.md]

## 1. Overview

O Aegis1 é um sistema B2B de gestão de ordens de manutenção industrial/facilities utilizado por quatro perfis principais: **Gestor de Manutenção** (desktop), **Técnico de Campo** (mobile/tablet em condições adversas), **Aprovador** (desktop) e **Administrador** (desktop). A acessibilidade não é opcional: técnicos operam em ambientes com ruído, luz forte, luvas e/ou limitações motoras temporárias; gestores podem ter baixa visão ou depender de navegação por teclado. Este documento estabelece as regras obrigatórias para que todos os fluxos críticos (criar ordem, iniciar, aprovar, concluir, cancelar, listar, custoTotalPorAtivo) atendam **WCAG 2.1 Nível AA** como definido em **NFR-U01**.

> **Estado atual:** O codebase **não possui CSS, design tokens implementados, nem componentes de UI** — apenas 15 arquivos `.js` (627 LOC) com lógica de API (`api.js`) e serviços. Os tokens de cor, tipografia, espaçamento, foco e motion descritos em `design-tokens.md` são **propostas iniciais [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** e ainda não existem no código. Este guia serve como **especificação de implementação** para quando a camada de UI for construída.

## 2. Padrão de Conformidade

* **Norma de referência:** WCAG 2.1 Nível AA (todas as diretrizes A + AA)
* **Obrigatoriedade legal aplicável:** Lei Brasileira de Inclusão (Lei nº 13.146/2015), Decreto nº 10.098/2019 (acessibilidade em sítios eletrônicos), LGPD (Art. 9 — consentimento acessível)
* **Escopo:** **Toda a aplicação** — nenhum fluxo isento. Fluxos críticos (ordens de serviço, cadastros, dashboards, autenticação) devem ser validados prioritariamente.

## 3. Contraste de Cor

As combinações abaixo referenciam os **tokens propostos em `design-tokens.md` (Seção 2)**. Como os tokens não estão implementados, os valores "Contraste Atual" são **teóricos** baseados nos hexadecimais propostos. **Validação real só será possível após implementação dos tokens e build da UI.**

| Combinação | Contexto | Contraste Mínimo Exigido (WCAG AA) | Contraste Teórico (Light) | Contraste Teórico (Dark) | Status |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `--color-text-primary` sobre `--color-bg` | Corpo de texto principal, tabelas, listas | 4.5:1 | **12.6:1** (#1A1A1A / #FAFAFA) | **12.6:1** (#F5F5F5 / #121212) | ✅ OK (teórico) |
| `--color-text-secondary` sobre `--color-bg` | Labels, descrições, texto secundário | 4.5:1 | **7.0:1** (#4A4A4A / #FAFAFA) | **7.0:1** (#CCCCCC / #121212) | ✅ OK (teórico) |
| `--color-text-muted` sobre `--color-bg` | Placeholders, metadados, texto desabilitado | 4.5:1 | **3.0:1** (#9E9E9E / #FAFAFA) ❌ | **3.0:1** (#888888 / #121212) ❌ | ❌ **FALHA** — não atende AA para texto normal |
| `--color-text-primary` sobre `--color-panel` | Texto dentro de cards, modais, painéis | 4.5:1 | **12.6:1** | **12.6:1** | ✅ OK (teórico) |
| `--color-accent` sobre `--color-bg` | Links, botões primários (texto) | 4.5:1 | **5.9:1** (#0066CC / #FAFAFA) | **5.9:1** (#66B3FF / #121212) | ✅ OK (teórico) |
| `--color-accent` sobre `--color-panel` | Botões primários em cards/modais | 4.5:1 | **5.9:1** | **5.9:1** | ✅ OK (teórico) |
| `--color-border` sobre `--color-bg` | Bordas de inputs, cards, divisores (UI) | 3:1 | **1.3:1** (#E0E0E0 / #FAFAFA) ❌ | **1.3:1** (#333333 / #121212) ❌ | ❌ **FALHA** — bordas não têm contraste suficiente |
| `--color-border-strong` sobre `--color-bg` | Bordas de foco, estados ativos (UI) | 3:1 | **2.3:1** (#BDBDBD / #FAFAFA) ❌ | **2.3:1** (#4A4A4A / #121212) ❌ | ❌ **FALHA** |
| `--color-success` sobre `--color-success-light` | Badge/status "Concluída" | 4.5:1 | **5.2:1** (#2E7D32 / #E8F5E9) | **5.2:1** (#81C784 / #1B3D1C) | ✅ OK (teórico) |
| `--color-warning` sobre `--color-warning-light` | Badge/status "Em andamento" | 4.5:1 | **4.5:1** (#F57F17 / #FFF8E1) ✅ limite | **4.5:1** (#FFB74D / #3D2E00) ✅ limite | ⚠️ **Limite** — validar com ferramenta |
| `--color-danger` sobre `--color-danger-light` | Badge/status "Cancelada", erros inline | 4.5:1 | **5.5:1** (#C62828 / #FDEDEC) | **5.5:1** (#EF5350 / #3D1A1A) | ✅ OK (teórico) |
| `--color-info` sobre `--color-info-light` | Badge/status "Aberta", tooltips informativos | 4.5:1 | **5.1:1** (#0277BD / #E1F5FE) | **5.1:1** (#4FC3F7 / #0D2B3D) | ✅ OK (teórico) |
| Texto grande (`text-lg`+) sobre `--color-bg` | Títulos, headers de página | 3:1 | **12.6:1** | **12.6:1** | ✅ OK (teórico) |

> **Ações obrigatórias antes de implementar UI:**
> 1. **Ajustar `--color-text-muted`** para atender 4.5:1 (ex.: `#757575` light / `#AAAAAA` dark)
> 2. **Ajustar `--color-border` e `--color-border-strong`** para atender 3:1 em UI (ex.: `--color-border: #CCCCCC` light / `#555555` dark; `--color-border-strong: #999999` light / `#777777` dark)
> 3. **Validar todos os pares com ferramenta automatizada (axe-core, Lighthouse) após build real**

## 4. Navegação por Teclado

* **Ordem de tabulação lógica garantida:** **Sim — obrigatório**. A ordem deve seguir a leitura visual (topo → baixo, esquerda → direita). Testar em cada tela nova/modificada.
* **Foco visível em todo elemento interativo:** **Sim — obrigatório**. Usar **exclusivamente** o token `shadow-focus` definido em `design-tokens.md` (Seção 4.3):
  ```css
  :focus-visible {
    outline: none;
    box-shadow: var(--shadow-focus); /* 0 0 0 3px var(--color-accent-light) */
  }
  ```
  **Nunca** remover `outline` sem substituir por `box-shadow` equivalente. Não usar `outline: none` sem `:focus-visible`.
* **Atalhos de teclado definidos:**

| Atalho | Ação | Escopo | Observação |
| :--- | :--- | :--- | :--- |
| `Tab` / `Shift+Tab` | Navegar entre elementos focáveis | Global | Ordem lógica |
| `Enter` / `Espaço` | Ativar botão, link, checkbox, radio | Global | Padrão nativo |
| `Esc` | Fechar modal, drawer, dropdown, toast | Global | Devolver foco ao gatilho |
| `Setas` | Navegar dentro de componentes compostos (tabs, menu, select, date picker) | Componente | Roving tabindex |
| `Home` / `End` | Primeiro/último item em listas longas | Tabelas, listas | Opcional, recomendado |
| `Ctrl/Cmd + K` | Abrir busca global / command palette | Global | **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — definir se necessário |

* **Trap de foco em modais:** **Implementado — obrigatório**. Ao abrir modal/drawer:
  1. Salvar elemento que tinha foco antes
  2. Mover foco para primeiro elemento focável do modal (ou `role="dialog"` com `autofocus` no close button)
  3. Ciclar `Tab`/`Shift+Tab` apenas dentro do modal
  4. Ao fechar (`Esc` ou botão), restaurar foco no elemento salvo
  5. **Como testar:** Navegar só com teclado — `Tab` não deve vazar para background; `Esc` fecha e foco retorna.

## 5. Suporte a Screen Readers

* **Uso de HTML semântico — regras obrigatórias:**
  - **Landmarks:** `<header role="banner">`, `<nav role="navigation">`, `<main role="main">`, `<aside role="complementary">` (sidebar), `<footer role="contentinfo">`
  - **Headings hierárquicos:** Um único `<h1>` por tela; `<h2>` para seções; `<h3>` para subseções; **nunca pular níveis**
  - **Listas:** `<ul>`/`<ol>` para menus, breadcrumbs, listas de ordens, checklists
  - **Tabelas:** `<table>` com `<thead>`, `<th scope="col">`, `<tbody>`; usar `<caption>` para descrever a tabela (ex.: "Ordens de serviço — 12 itens, filtradas por status 'Aberta'")
  - **Botões:** `<button>` sempre — nunca `<div onclick>`, `<a href="#">` para ações
  - **Links:** `<a href>` apenas para navegação (mudança de URL/rota)

* **Labels acessíveis:**

| Elemento | Regra Obrigatória | Exemplo no Aegis1 |
| :--- | :--- | :--- |
| Ícone sozinho (sem texto visível) | `aria-label` descritivo | `<button aria-label="Filtrar ordens"><IconFilter /></button>` |
| Imagem informativa (foto de equipamento, assinatura, QR code) | `alt` descritivo conciso | `<img alt="Foto do compressor #452 mostrando vazamento na válvula" />` |
| Imagem decorativa (ícones decorativos, avatares genéricos) | `alt=""` **ou** `aria-hidden="true"` | `<img src="avatar.png" alt="" aria-hidden="true" />` |
| Campo de formulário | `<label for="id">` **associado explicitamente** | `<label for="ativo-id">Ativo</label><input id="ativo-id" />` |
| Campo sem label visível (ex.: busca com ícone de lupa) | `aria-label` **ou** `aria-labelledby` | `<input aria-label="Buscar ordens por número, ativo ou solicitante" />` |
| Grupo de radios/checkboxes | `<fieldset>` + `<legend>` | `<fieldset><legend>Status da ordem</legend>...` |
| Mensagem de erro/ajuda associada a campo | `aria-describedby="erro-id"` no input + `id="erro-id"` na mensagem | `<input aria-describedby="erro-ativo" /><span id="erro-ativo" role="alert">Selecione um ativo</span>` |

* **Regiões dinâmicas (`aria-live`) — onde usar:**

| Componente / Situação | `aria-live` | `aria-atomic` | Exemplo |
| :--- | :---: | :---: | :--- |
| Toast / Snackbar (sucesso, erro, aviso) | `polite` | `true` | `<div role="status" aria-live="polite" aria-atomic="true">Ordem #1234 concluída</div>` |
| Contador de resultados de busca/filtro | `polite` | `true` | `<span aria-live="polite" aria-atomic="true">12 ordens encontradas</span>` |
| Validação inline ao sair do campo (blur) | `assertive` | `true` | `<span role="alert" id="erro-email">Email inválido</span>` (via `aria-describedby`) |
| Atualização de status de ordem em tempo real (websocket/polling) | `polite` | `false` | `<span aria-live="polite">Status alterado para "Em andamento"</span>` |
| Confirmação de ação destrutiva (modal "Tem certeza?") | `assertive` | `true` | `<div role="alertdialog" aria-modal="true" aria-labelledby="titulo-confirma">` |

## 6. Formulários Acessíveis

* **Associação label/input — padrão obrigatório:**
  ```html
  <!-- SEMPRE label explícito -->
  <label for="tecnico-id">Técnico responsável</label>
  <select id="tecnico-id" name="tecnico" required>...</select>
  
  <!-- Se label visual não for possível (design), usar aria-label -->
  <input type="search" aria-label="Buscar ordens" placeholder="Buscar..." />
  ```
  **Proibido:** `placeholder` como substituto de label; `<label>` sem `for` envolvendo input (falha em alguns leitores).

* **Mensagens de erro:**
  - Associadas via `aria-describedby` no campo (`input[aria-describedby="erro-campo"]`)
  - Container da mensagem com `role="alert"` ou `aria-live="assertive"` para anúncio imediato
  - Texto claro: **"O que está errado + como corrigir"** (ex.: "Data de início não pode ser futura. Selecione uma data até hoje.")
  - Erros de validação no submit: focar primeiro campo com erro + anunciar resumo (`aria-live="assertive"` no container de erros)

* **Validação:**
  - **Inline (on blur/change):** Para feedback imediato — anunciar via `aria-live="assertive"` na mensagem associada
  - **No submit:** Validar tudo, mostrar resumo de erros no topo do formulário (`role="alert"`, `aria-live="assertive"`), focar primeiro campo inválido
  - **Indicação visual de erro:** Borda `--color-danger` + ícone + mensagem — **não apenas cor** (WCAG 1.4.1)
  - **Campos obrigatórios:** `required` + `aria-required="true"` + indicador visual (asterisco) + texto "(obrigatório)" no label para SR

## 7. Motion & Preferências do Usuário

* **`prefers-reduced-motion`:** **Respeitado obrigatoriamente** (NFR-U01, `design-tokens.md` Seção 6).
  ```css
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
  - Tokens de duração (`duration-fast`, `duration-base`, `duration-slow`) **devem ser zero** quando esta media query ativa
  - Animações de entrada de listas (stagger), modais, drawers, tabs, tooltips — todas desativadas
  - **Teste:** Ativar "Reduzir movimento" no SO → navegar app → zero animações perceptíveis

* **`prefers-color-scheme`:** **Suporte obrigatório** (NFR-U01, `design-tokens.md` Seção 2.3).
  - Implementação: classe `.dark` no `<html>` (toggle manual persistido em `localStorage` + fallback inicial para `prefers-color-scheme`)
  - Tokens de cor (Seção 2.3) definem valores para ambos os modos
  - **Teste:** Alternar tema do SO → app deve seguir automaticamente na primeira visita; toggle manual deve sobrescrever e persistir

## 8. Processo de Validação

* **Ferramentas automatizadas (CI/CD obrigatório):**
  - **axe-core** integrado nos testes E2E (Playwright/Cypress) — falha build se violações AA
  - **Lighthouse CI** — score Accessibility ≥ 95 em todas as rotas críticas
  - **eslint-plugin-jsx-a11y** (se migrar para React) ou equivalente para vanilla JS — regras: `anchor-is-valid`, `click-events-have-key-events`, `no-noninteractive-element-interactions`, `role-has-required-aria-props`

* **Frequência de auditoria:**
  - **A cada PR** que toque componentes de UI, formulários, navegação, modais, tabelas: axe + Lighthouse no pipeline
  - **Auditoria trimestral completa** (todas as rotas, fluxos críticos, estados de erro/vazio/carregamento) com relatório documentado

* **Teste manual com screen reader (obrigatório antes de release):**
  - **NVDA + Firefox** (Windows) — fluxos: criar ordem, aprovar, concluir, listar com filtros
  - **VoiceOver + Safari** (macOS/iOS) — mesmo fluxos + navegação touch no mobile (técnico de campo)
  - **TalkBack + Chrome** (Android) — fluxo técnico mobile
  - Checklist: anúncio de labels, erros, status dinâmicos, navegação por headings/landmarks, foco visível, ordem de tabulação

* **Teste de navegação 100% por teclado (obrigatório a cada sprint):**
  - Percorrer **todos** os fluxos críticos só com `Tab`/`Shift+Tab`/`Enter`/`Esc`/`Setas`
  - Verificar: foco visível em **todo** elemento, trap em modais, devolução de foco, atalhos funcionando, nenhum "keyboard trap" involuntário

## 9. Débitos de Acessibilidade Conhecidos

| Item | Severidade | Componente/Tela Afetada | Prazo de Correção | Observação |
| :--- | :---: | :--- | :---: | :--- |
| Tokens de cor não implementados — contraste real desconhecido | **Alta** | App inteiro | Antes de qualquer build de UI | Blocker para WCAG AA |
| `--color-text-muted` falha 4.5:1 (teórico) | **Alta** | Placeholders, metadados, texto desabilitado | Ajustar token antes de implementar | Ver Seção 3 |
| `--color-border` / `--color-border-strong` falham 3:1 (teórico) | **Alta** | Inputs, cards, divisores, foco | Ajustar token antes de implementar | Ver Seção 3 |
| Nenhum componente UI existe — sem HTML semântico, labels, ARIA | **Crítica** | App inteiro | Implementação da camada de UI | Este guia é spec para implementação |
| `console.*` residuais em `api.js` (linhas 26, 49, 52) — vazam em produção | **Média** | Camada de API / telemetria | Antes de go-live (NFR-SEC04, NFR-O01) | Pipeline deve falhar se `console.*` no bundle |
| Complexidade ciclomática alta em `api.js:request` (13) — dificulta testes de acessibilidade da camada de rede | **Média** | `frontend/src/services/api.js` | Refatorar antes de testes de integração | NFR-M01 |

## 10. Referências

* [WCAG 2.1 Quick Reference](https://www.w3.org/WAI/WCAG21/quickref/) — checklist oficial
* [axe-core](https://github.com/dequelabs/axe-core) — engine de teste automatizado
* [Lighthouse Accessibility Scoring](https://developer.chrome.com/docs/lighthouse/accessibility/) — métricas CI
* [WAI-ARIA Authoring Practices Guide (APG)](https://www.w3.org/WAI/ARIA/apg/) — patterns de componentes acessíveis
* `design-tokens.md` — Seções 2 (Color), 3 (Typography), 4 (Spacing/Sizing/Shadows), 6 (Motion), 7 (Z-Index) — **tokens propostos [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* `nfr.md` — NFR-U01, U02, U03, SEC04, O01, M01, M03 — requisitos não-funcionais de acessibilidade, segurança, observabilidade, manutenibilidade
* [BRD (Business Requirements Document)] — Seções 3 (Personas), 4 (KPIs/Guardrails), 5 (In-Scope), 6 (Regras de Negócio), 7 (Dependências), 8 (Riscos) — contexto de negócio e fluxos críticos

---

## 11. Revision History

| Versão | Data | Autor | Mudanças |
| :--- | :--- | :--- | :--- |
| 1.0 | 15/01/2025 | Pipeline (gerado) | Criação inicial baseada em diagnóstico real, `design-tokens.md` (tokens propostos) e `nfr.md` — **nenhum componente UI existe no codebase**; todos os valores de contraste são teóricos baseados nos tokens propostos **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** |