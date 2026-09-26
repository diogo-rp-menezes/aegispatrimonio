# Design Tokens — Aegis1

> **Versão:** 0.1 · **Owner:** Design/Frontend Lead · **Status:** Draft — *Tokens não implementados no codebase atual*  
> **Fonte de implementação:** *Nenhuma encontrada no repositório (sem tailwind.config, tokens.json, CSS variables, styled-system theme ou arquivos de design system)*

---

## 1. Overview

Este documento **deveria** ser a fonte única da verdade para todos os valores visuais primitivos usados na interface do Aegis1. No entanto, a varredura determinística do workspace (**15 arquivos `.js`**, 0 arquivos CSS/SCSS, 0 arquivos de configuração de design system) **não encontrou nenhuma implementação de design tokens** no código atual.

> **⚠️ Estado real:** O projeto não possui design tokens definidos, nem variáveis CSS customizadas, nem configuração de tema. Qualquer valor de cor, espaçamento, tipografia ou sombra está atualmente *hardcoded* (ou ausente) nos componentes. Este documento serve como **especificação do que precisa ser criado**, não como reflexo do existente.

**Próximos passos obrigatórios antes de considerar este documento "implementado":**
1. Definir paleta de cores, escala tipográfica, espaçamento e raios com Design/Produto
2. Implementar como CSS Custom Properties (ex.: `:root { --color-primary: ... }`) ou JSON de tokens + build (Style Dictionary)
3. Remover todos os valores hardcoded dos componentes e substituir por referências aos tokens
4. Adicionar este arquivo (ou `tokens.json`) ao repositório e versionar

---

## 2. Color Tokens

### 2.1 Cores Base (Primitivas) — *[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]*
> **Premissa:** Paleta neutra profissional para sistema B2B de manutenção industrial/facilities; acessibilidade WCAG AA mínima.

| Token | Valor (Hex) | Valor (RGB) | Uso Pretendido |
| :--- | :--- | :--- | :--- |
| `--color-bg` | `#FAFAFA` | `250, 250, 250` | Fundo principal da aplicação |
| `--color-panel` | `#FFFFFF` | `255, 255, 255` | Fundo de cards, modais, painéis |
| `--color-border` | `#E0E0E0` | `224, 224, 224` | Bordas de inputs, cards, divisores |
| `--color-border-strong` | `#BDBDBD` | `189, 189, 189` | Bordas de foco, estados ativos |
| `--color-text-primary` | `#1A1A1A` | `26, 26, 26` | Títulos, texto principal |
| `--color-text-secondary` | `#4A4A4A` | `74, 74, 74` | Corpo de texto, labels |
| `--color-text-muted` | `#9E9E9E` | `158, 158, 158` | Placeholders, metadados, texto desabilitado |
| `--color-accent` | `#0066CC` | `0, 102, 204` | Ações primárias (botões, links), foco visível |
| `--color-accent-hover` | `#0052A3` | `0, 82, 163` | Hover de ações primárias |
| `--color-accent-light` | `#E6F0FA` | `230, 240, 250` | Background de badges/info relacionados à ação primária |

### 2.2 Cores Semânticas — *[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]*

| Token | Valor (Hex) | Mapeia para | Uso |
| :--- | :--- | :--- | :--- |
| `--color-success` | `#2E7D32` | Primitiva verde | Confirmações, status "Concluída", indicadores positivos |
| `--color-success-light` | `#E8F5E9` | `--color-success` @ 10% | Background de toast/snackbar de sucesso |
| `--color-warning` | `#F57F17` | Primitiva âmbar | Alertas, status "Em andamento", atenção necessária |
| `--color-warning-light` | `#FFF8E1` | `--color-warning` @ 10% | Background de avisos não bloqueantes |
| `--color-danger` | `#C62828` | Primitiva vermelha | Erros, ações destrutivas (cancelar, deletar), status "Cancelada" |
| `--color-danger-light` | `#FDEDEC` | `--color-danger` @ 10% | Background de erros inline, modais destrutivos |
| `--color-info` | `#0277BD` | Primitiva azul | Mensagens informativas, status "Aberta", ajuda contextual |
| `--color-info-light` | `#E1F5FE` | `--color-info` @ 10% | Background de tooltips, empty states informativos |

### 2.3 Modos (Light / Dark) — *[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]*
> **Estratégia de troca de tema:** Classe `.dark` no `<html>` (toggle manual persistido em `localStorage` + respeito a `prefers-color-scheme` como fallback inicial). **Não implementado.**

| Token | Light Mode | Dark Mode |
| :--- | :--- | :--- |
| `--color-bg` | `#FAFAFA` | `#121212` |
| `--color-panel` | `#FFFFFF` | `#1E1E1E` |
| `--color-border` | `#E0E0E0` | `#333333` |
| `--color-border-strong` | `#BDBDBD` | `#4A4A4A` |
| `--color-text-primary` | `#1A1A1A` | `#F5F5F5` |
| `--color-text-secondary` | `#4A4A4A` | `#CCCCCC` |
| `--color-text-muted` | `#9E9E9E` | `#888888` |
| `--color-accent` | `#0066CC` | `#66B3FF` |
| `--color-accent-hover` | `#0052A3` | `#99CCFF` |
| `--color-accent-light` | `#E6F0FA` | `#1A3A5C` |
| `--color-success` | `#2E7D32` | `#81C784` |
| `--color-success-light` | `#E8F5E9` | `#1B3D1C` |
| `--color-warning` | `#F57F17` | `#FFB74D` |
| `--color-warning-light` | `#FFF8E1` | `#3D2E00` |
| `--color-danger` | `#C62828` | `#EF5350` |
| `--color-danger-light` | `#FDEDEC` | `#3D1A1A` |
| `--color-info` | `#0277BD` | `#4FC3F7` |
| `--color-info-light` | `#E1F5FE` | `#0D2B3D` |

---

## 3. Typography

### 3.1 Font Families — *[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]*
> **Premissa:** Fonte de sistema (system-ui) para performance zero-dependency; sem fontes externas (Google Fonts, etc.) para evitar requests extras e manter bundle leve. **Não implementado.**

| Token | Fonte | Fallback Stack | Uso |
| :--- | :--- | :--- | :--- |
| `--font-sans` | `system-ui` | `-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif` | Todo texto da UI, corpo, botões, inputs |
| `--font-mono` | `ui-monospace` | `SFMono-Regular, "SF Mono", Menlo, Consolas, "Liberation Mono", monospace` | Códigos de ordem, IDs, valores técnicos, logs |

### 3.2 Type Scale — *[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]*
> **Escala:** Base 1rem = 16px; razão 1.25 (major third). Line heights otimizados para leitura em densidade de dados (tabelas, listas de ordens). **Não implementado.**

| Token | Tamanho (rem) | Tamanho (px) | Line Height | Peso | Uso |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `text-xs` | `0.75rem` | `12px` | `1.5` | `400` | Labels de inputs, metadados de linha (ex.: "Criado em"), badges |
| `text-sm` | `0.875rem` | `14px` | `1.5` | `400` | Texto secundário, descrições curtas, tooltips |
| `text-base` | `1rem` | `16px` | `1.6` | `400` | **Corpo principal** — tabelas, formulários, listas |
| `text-lg` | `1.125rem` | `18px` | `1.5` | `500` | Subtítulos de seção, cards de resumo |
| `text-xl` | `1.25rem` | `20px` | `1.4` | `600` | Títulos de página, headers de modais |
| `text-2xl` | `1.5rem` | `24px` | `1.3` | `600` | Título principal (hero), dashboards |
| `text-3xl` | `1.875rem` | `30px` | `1.2` | `700` | *Reservado* — uso futuro em landing/empty states grandes |

---

## 4. Spacing & Sizing

### 4.1 Escala de Espaçamento — *[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]*
> **Grid base:** 4px (0.25rem). Todos os tokens são múltiplos de 4px. **Não implementado.**

| Token | Valor (rem) | Valor (px) |
| :--- | :--- | :--- |
| `space-0` | `0` | `0` |
| `space-1` | `0.25rem` | `4px` |
| `space-2` | `0.5rem` | `8px` |
| `space-3` | `0.75rem` | `12px` |
| `space-4` | `1rem` | `16px` |
| `space-5` | `1.25rem` | `20px` |
| `space-6` | `1.5rem` | `24px` |
| `space-8` | `2rem` | `32px` |
| `space-10` | `2.5rem` | `40px` |
| `space-12` | `3rem` | `48px` |
| `space-16` | `4rem` | `64px` |

**Convenção de uso:**
- `space-1` / `space-2`: gaps internos de componentes (padding de botão, input)
- `space-3` / `space-4`: gaps entre elementos relacionados (label + input, itens de card)
- `space-6` / `space-8`: separação de seções, padding de containers
- `space-12`+: layout de página, margens de containers principais

### 4.2 Border Radius — *[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]*

| Token | Valor (rem) | Valor (px) | Uso |
| :--- | :--- | :--- | :--- |
| `radius-none` | `0` | `0` | Tabelas, elementos colados |
| `radius-sm` | `0.25rem` | `4px` | Inputs, selects, badges, botões pequenos |
| `radius-md` | `0.5rem` | `8px` | **Padrão** — cards, botões padrão, modais, dropdowns |
| `radius-lg` | `0.75rem` | `12px` | Painéis flutuantes, sidebars, containers principais |
| `radius-full` | `9999px` | `9999px` | Avatares, pills, indicadores de status circulares |

### 4.3 Shadows / Elevation — *[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]*
> **Nota:** Sombras suaves para UI densa de dados; evitar elevação excessiva que compita com hierarquia de informação.

| Token | Valor (box-shadow) | Uso |
| :--- | :--- | :--- |
| `shadow-none` | `none` | Elementos flush com fundo |
| `shadow-sm` | `0 1px 2px rgba(0,0,0,0.05)` | Hover de linhas de tabela, cards compactos |
| `shadow-md` | `0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -2px rgba(0,0,0,0.1)` | **Padrão** — cards, painéis laterais, dropdowns |
| `shadow-lg` | `0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)` | Modais, drawers, tooltips complexos |
| `shadow-focus` | `0 0 0 3px var(--color-accent-light)` | **Foco visível** (acessibilidade) — nunca remover |

---

## 5. Breakpoints — *[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]*
> **Abordagem:** Mobile-first. Breakpoints alinhados a containers de dados (tabelas de ordens precisam de ≥768px para colunas completas). **Não implementado.**

| Token | Largura Mínima | Dispositivo Alvo | Uso no Aegis1 |
| :--- | :--- | :--- | :--- |
| `sm` | `640px` | Mobile grande / phablet | Stack de formulários, cards empilhados |
| `md` | `768px` | Tablet portrait | Tabelas com scroll horizontal controlado, sidebar colapsável |
| `lg` | `1024px` | Tablet landscape / Desktop pequeno | Layout de duas colunas (lista + detalhe), sidebar fixa |
| `xl` | `1280px` | Desktop padrão | Dashboard com 3+ colunas, tabelas completas sem scroll |
| `2xl` | `1536px` | Desktop grande / Ultra-wide | Layout estendido, painéis lado a lado |

---

## 6. Motion Tokens — *[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]*
> **Princípio:** Movimento funcional (feedback de estado, transição de painéis), não decorativo. Respeitar `prefers-reduced-motion`. **Não implementado.**

| Token | Duração | Easing | Uso |
| :--- | :--- | :--- | :--- |
| `duration-instant` | `0ms` | — | Toggles booleanos (checkbox, switch), mudanças de estado imediatas |
| `duration-fast` | `150ms` | `cubic-bezier(0.4, 0, 0.2, 1)` | Hover de botões, focus ring, expansão de accordion, tooltip show/hide |
| `duration-base` | `250ms` | `cubic-bezier(0.4, 0, 0.2, 1)` | **Padrão** — transição de modais, drawers, tabs, troca de abas |
| `duration-slow` | `350ms` | `cubic-bezier(0.4, 0, 0.2, 1)` | Page transitions (se SPA), animação de entrada de listas (stagger) |
| `easing-standard` | — | `cubic-bezier(0.4, 0, 0.2, 1)` | Curva padrão Material/MUI — saída rápida, entrada suave |
| `easing-emphasized` | — | `cubic-bezier(0.4, 0, 0.2, 1)` | Mesmo que standard (alias semântico para clareza) |

---

## 7. Z-Index Scale — *[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]*

| Token | Valor | Uso |
| :--- | :--- | :--- |
| `z-base` | `0` | Conteúdo normal |
| `z-dropdown` | `100` | Dropdowns, menus de contexto, autocomplete |
| `z-sticky` | `200` | Headers fixos, sidebars |
| `z-modal-backdrop` | `400` | Overlay escuro atrás de modais |
| `z-modal` | `500` | Modais, drawers, dialogs |
| `z-toast` | `600` | Toasts, snackbars, notificações globais |
| `z-tooltip` | `700` | Tooltips, popovers (acima de tudo) |

---

## 8. Governança dos Tokens

| Aspecto | Definição |
| :--- | :--- |
| **Processo de adição/alteração** | 1. Issue no repositório com motivação (novo componente, ajuste de acessibilidade, rebrand)  <br>2. PR com: token(s) novo(s) + exemplos de uso em componente(s) + screenshot de antes/depois  <br>3. Aprovação obrigatória: **Design Lead** + **Frontend Lead**  <br>4. Merge → build gera `tokens.css` / `tokens.json` versionados |
| **Ferramenta de sincronização** | **Fase 1 (atual):** CSS Custom Properties em `src/styles/tokens.css` (zero dependências)  <br>**Fase 2 (futuro):** Style Dictionary → gera `tokens.json`, `tokens.css`, `tokens.ts` (para uso em JS se necessário) + Figma Tokens plugin para sync bidirecional |
| **Como evitar drift design ↔ código** | - Tokens **só existem no código** (source of truth)  <br>- Figma consome via plugin (não o inverso)  <br>- CI roda teste visual (Chromatic/Storybook) em PRs que tocam tokens  <br>- Documentação auto-gerada a partir dos tokens (script lê `tokens.css` e atualiza este .md) |
| **Versionamento** | SemVer no pacote de tokens (se extraído para pacote próprio) ou tag no monorepo. Breaking change = remoção/renomeação de token → major. Novo token / ajuste de valor → minor/patch. |

---

## 9. Checklist de Implementação (Definition of Done)

- [ ] Criar `src/styles/tokens.css` com todas as custom properties acima (light + dark via `[data-theme="dark"]` ou `.dark`)
- [ ] Importar `tokens.css` no entry point da aplicação (`index.html` ou `main.js`)
- [ ] Substituir **todos** valores hardcoded nos 15 arquivos `.js`/componentes por `var(--token-name)`
- [ ] Configurar ESLint/Stylelint para **proibir** cores hex/rgb, px fixos, `box-shadow` inline fora de tokens
- [ ] Adicionar script `npm run tokens:validate` que falha se houver valores não-tokenizados no código
- [ ] Documentar tokens no Storybook (ou similar) com controles interativos
- [ ] Validar contraste WCAG AA em todos os pares semânticos (texto/fundo, borda/fundo)
- [ ] Testar modo dark completo (todas as telas, estados de foco, modais, toasts)
- [ ] Obter sign-off de Design Lead + Frontend Lead + Acessibilidade

---

## 10. Referências

* **Arquivo de implementação alvo:** `frontend/src/styles/tokens.css` *(a criar)*
* **Figma/Design tool:** *[Link a ser preenchido pelo Design Lead — não existe no repositório]*
* **BRD (contexto de negócio):** `Business Requirements Document` — seções 1, 3, 4, 6 definem requisitos de UI (densidade de dados, mobile/responsivo, acessibilidade, estados de ordem)
* **Diagnóstico determinístico:** `server/analyze-pipeline.ts` — confirma ausência de CSS/design system no codebase atual

---

## 11. Revision History

| Versão | Data | Autor | Mudanças |
| :--- | :--- | :--- | :--- |
| 0.1 | 15/01/2025 | Pipeline (gerado) | Criação inicial baseada em diagnóstico real — **tokens não existem no código**; valores são propostas iniciais **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** |