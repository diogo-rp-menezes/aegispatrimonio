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
  - **Toasts/notificações** (sucesso, erro, aviso): `role="status" aria-live="polite"` (não interrompe leitura)  
  - **Alertas de saúde (disco/CPU/memória)** do `AlertNotificationService`: `role="alert" aria-live="assertive"` (interrompe — crítico)  
  - **Resultados de busca/filtro em tabela de ativos**: `aria-live="polite"` no container de resultados + `aria-atomic="true"`  
  - **Contadores de badge** (ex: "3 alertas não lidos"): `aria-live="polite"` no elemento do contador  
  <!-- source: nfr.md#5 (NFR-U02), nfr.md#6-observabilidade (NFR-O04), design-tokens.md#8.4-badges--status -->

---

## 6. Formulários Acessíveis

* **Associação label/input — padrão obrigatório:**  
  ```html
  <label for="ativo-nome">Nome do Ativo</label>
  <input type="text" id="ativo-nome" name="nome" required aria-describedby="ativo-nome-erro ativo-nome-ajuda">
  <span id="ativo-nome-ajuda" class="help-text">Máx. 100 caracteres</span>
  <span id="ativo-nome-erro" class="error-message" role="alert" aria-live="polite"></span>
  ```
  - **Proibido:** `placeholder` como único label, `title` attribute como substituto de label, inputs sem `id` único na página.  
  <!-- source: design-tokens.md#8.2-formularios -->

* **Mensagens de erro:**  
  - Exibidas **inline** abaixo do campo (não apenas toast global)  
  - Associadas via `aria-describedby` no input  
  - Anunciadas ao screen reader via `role="alert"` ou `aria-live="assertive"` no container da mensagem  
  - Estilo visual: borda `var(--input-border-error)` + ícone + texto — **não depender apenas de cor**  
  <!-- source: design-tokens.md#8.2-formularios (--input-border-error), nfr.md#5 (NFR-U02) -->

* **Validação:**  
  - **Inline (on blur/change):** Para feedback imediato — mensagem de erro aparece, `aria-invalid="true"` no input, foco permanece no campo  
  - **No submit:** Se houver erros, foco move para **primeiro campo inválido**, scroll suave, anúncio de resumo de erros (`role="alert" aria-live="assertive"`)  
  - **Sucesso:** Toast `role="status" aria-live="polite"` + limpeza de `aria-invalid`  

---

## 7. Motion & Preferências do Usuário

* **`prefers-reduced-motion: reduce`** — **Suporte obrigatório** (ver `design-tokens.md` §6). Todas as animações/transições definidas nos tokens (`--duration-*`) devem ser **instantâneas (0ms)** quando esta preferência está ativa. Implementação:  
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
  - Tooltips Popper: show/hide instantâneo  
  - Modais/sidebars: sem animação de slide/fade  
  - Tabelas: expansão de linha imediata  
  <!-- source: design-tokens.md#6-motion-tokens -->

* **`prefers-color-scheme`** — **Suporte obrigatório** (ver `design-tokens.md` §2.4). Default do sistema deve ser respeitado na primeira visita; toggle manual persiste em `localStorage` e sobrescreve preferência do sistema. Classe `.theme-dark` no `<html>` controla modo.  
  <!-- source: design-tokens.md#2.4-modos-light--dark -->

* **`prefers-contrast: more`** — **Suporte recomendado** (WCAG 2.1 AA não exige, mas melhora usabilidade). Aumentar espessura de bordas, focus rings e contraste de cores semânticas quando ativo.  
  > **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Não explicitado no BRD/NFR, mas alinhado a boas práticas. Decisão com Design Lead.

---

## 8. Processo de Validação

| Etapa | Ferramenta / Método | Frequência | Responsável | Critério de Aceite |
| :--- | :--- | :--- | :--- | :--- |
| **Lint/Unitário (PR)** | `eslint-plugin-jsx-a11y` (JS), `axe-core` em testes de componente (Cypress component testing) | **Todo PR** que toque frontend | Autor do PR + Reviewer | Zero violações `error`; `warning` apenas com justificativa documentada |
| **E2E Automatizado (CI)** | Cypress + `cypress-axe` nos fluxos críticos (login, criar ativo, solicitar manutenção, aprovar, listar alertas) | **Todo merge em `main`** + nightly | QA Lead | 0 violações WCAG 2.1 AA (axe `wcag2aa` tag) nos fluxos cobertos |
| **Auditoria Completa (Trimestral)** | axe DevTools (browser extension) + Lighthouse Accessibility + teste manual com NVDA (Windows) + VoiceOver (macOS/iOS) | **Trimestral** (ou após release major) | QA Lead + Designer | Relatório com itens, severidade, prazo de correção — publicado no Confluence/GitHub Wiki |
| **Teste Manual 100% Teclado** | Navegação completa sem mouse nos fluxos críticos | **A cada release** (smoke) + **Trimestral** (completo) | QA Analyst | Todos os elementos interativos alcançáveis, foco visível, ordem lógica, trap em modais funcionando |
| **Teste com Screen Reader** | NVDA (Chrome/Edge) + VoiceOver (Safari) — fluxos críticos + componentes novos | **Trimestral** + **Todo componente novo complexo** (ex: wizard, data grid) | QA Analyst + Designer | Leitura coerente, labels anunciados, regiões live funcionando, sem "traps" de leitura |

> **Métrica de guarda (guardrail):** Taxa de violações axe `wcag2aa` em fluxos críticos **= 0** em `main` — falha no pipeline se > 0.  
> <!-- source: nfr.md#5 (NFR-U01), nfr.md#6 (NFR-O01, NFR-O03) -->

---

## 9. Débitos de Acessibilidade Conhecidos

| Item | Severidade | Componente/Tela Afetada | Prazo de Correção | Observação |
| :--- | :--- | :--- | :--- | :--- |
| Design tokens não implementados — contraste não verificável | **Alta** | Toda a aplicação | Sprint 1 (conforme `design-tokens.md` §11) | Bloqueia validação automatizada de contraste |
| `console.error`/`console.debug` residuais em `frontend/src/services/api.js:44,107` | Média | Camada de API frontend | Sprint 1 (conforme NFR-SEC05) | Polui console; pode vazar info sensível em leitores de tela que anunciam console |
| Complexidade ciclomática alta em `frontend/src/services/api.js:request` (13) | Média | Service de API (interceptor, error handling, retry) | Sprint 2 (conforme NFR-M02) | Dificulta manutenção de lógica de acessibilidade (ex: headers de trace, retry com anúncio) |
| Stub vazio `Usuario.setUsername` (`src/main/java/.../model/Usuario.java:86`) | Baixa | Backend — modelo de usuário | Sprint 1 (conforme NFR-M04) | Não afeta frontend diretamente, mas impede validação de perfil acessível |
| Ausência de `role="alert"` / `aria-live` em toasts atuais | **Alta** | Sistema de notificações (toasts) | Sprint 1 | Crítico para NFR-U02 (alertas de saúde) |
| Ausência de `fieldset`/`legend` em grupos de radio/checkbox (ex: filtros de ativo) | Média | Formulários de filtro, criação de ativo | Sprint 2 | Necessário para screen readers anunciarem contexto do grupo |
| Ordem de headings inconsistente em páginas de dashboard/listagem | Média | Dashboard, listagem de ativos, detalhes de manutenção | Sprint 2 | Auditoria trimestral deve mapear todas as páginas |

> **Nota:** Itens marcados com severidade **Alta** bloqueiam conformidade WCAG 2.1 AA nos fluxos críticos. Devem ser priorizados no backlog de acessibilidade.  
> <!-- source: Diagnóstico determinístico (console.*, complexidade, stub), nfr.md#5 (NFR-U01, NFR-U02), nfr.md#7 (NFR-M02, NFR-M04), design-tokens.md#11 -->

---

## 10. Referências

* **WCAG 2.1 Quick Reference:** https://www.w3.org/WAI/WCAG21/quickref/  
* **axe-core rules (WCAG 2.1 AA):** https://github.com/dequelabs/axe-core/blob/develop/doc/rule-descriptions.md  
* **Design Tokens (este projeto):** `design-tokens.md` — fonte única de verdade para cores, tipografia, espaçamento, focus ring, motion, z-index  
* **NFR (este projeto):** `nfr.md` — requisitos NFR-U01 a NFR-U04, NFR-M02, NFR-SEC05  
* **BRD (fonte de requisitos de negócio):** `Business Requirements Document` — seções 3 (Personas), 4 (Objetivos O2, O5), 5 (Escopo), 6 (Regras BR-01 a BR-10)  
* **Diagnóstico de código:** `frontend/src/services/api.js` (Popper.js usage, console residuals, complexidade), `src/main/java/.../service/AlertNotificationService.java` (alertas de saúde)  
* **Ferramentas de validação recomendadas:**  
  - axe DevTools (browser extension)  
  - Lighthouse Accessibility (Chrome DevTools)  
  - Colour Contrast Analyser (TPGi)  
  - NVDA (Windows) — https://www.nvaccess.org/download/  
  - VoiceOver (macOS/iOS) — nativo  
  - Cypress + cypress-axe — https://github.com/avanslaars/cypress-axe  
  - Storybook + @storybook/addon-a11y — para validação de componentes isolados  

---

> **Fim do documento.** Este guia deve ser revisado a cada release major ou quando houver alteração nos design tokens. Todas as seções com status **⬜ Pendente** ou **Prazo de Correção** definido requerem ação rastreável no backlog do time.