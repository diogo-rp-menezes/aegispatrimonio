# Wireframes & Screen Specifications — Aegis1

> **Versão:** 2.0 · **Owner:** UX/Product Design · **Status:** Implemented
> **Base:** Component Library v2.0 + UI Style Guide v2.0 + User Flows v2.0 + Design Tokens v2.0
> **Ferramenta:** Figma (source of truth) + Storybook (component docs) + Mermaid (flow diagrams)
> **Status:** Wireframes implementados como componentes Vue 3 + Bootstrap 5 + PWA

---

## 1. Overview

Os wireframes do Aegis1 são **implementados diretamente como componentes Vue 3** (Component Library v2.0) e documentados no **Storybook** como fonte viva. Não existem arquivos estáticos de wireframe — a especificação visual vive no código e no Figma (source of truth para design).

**Princípios:**
- **Mobile First** — Wireframes desenhados para 375px primeiro, escalados para desktop
- **Component-Driven** — Wireframes = composição de componentes da Library v2.0
- **Responsive by Default** — Breakpoints: xs (<576), sm (≥576), md (≥768), lg (≥992), xl (≥1200), xxl (≥1400)
- **Acessibilidade First** — WCAG 2.1 AA em todos os estados (focus, loading, error, empty)

---

## 2. Screen Inventory (Por Fluxo de Usuário)

### 2.1 Autenticação & Onboarding

| Tela | Rota | Componentes Principais | Responsivo |
| :--- | :--- | :--- | :--- |
| **Login** | `/login` | `Card` (form), `FormField`, `Input` (email, password), `Button` (primary), `Link` (esqueci senha), `Alert` (erros) | Mobile: Full width card; Desktop: Card centralizado 400px |
| **Primeiro Acesso / Troca Senha** | `/primeiro-acesso` | `Card` (form), `FormField`, `Input` (senha atual, nova, confirmar), `Button`, `StrengthMeter` | Igual Login |
| **MFA / 2FA** | `/mfa` | `Card`, `Input` (código 6 dígitos, `inputmode="numeric"`), `Button`, `Link` (reenvio) | Igual Login |

### 2.2 Dashboard Principal

| Tela | Rota | Componentes Principais | Responsivo |
| :--- | :--- | :--- | :--- |
| **Dashboard Principal** | `/dashboard` | `PageContainer`, `KPICard` (4-6), `Chart` (line/bar), `AlertFeed` (WebSocket), `DataTable` (ordens recentes), `Sidebar` (filtros mobile) | Mobile: Stack cards (1 col), Chart full width, Drawer filtros; Desktop: Grid 2-3 cols, Sidebar fixa |
| **Dashboard Preditiva** | `/dashboard/preditiva` | `PageContainer`, `KPICard` (4), `PrevisaoTable` (DataTable + expand), `AlertCard` (críticos), `Chart` (probabilidade), `Drawer` (filtros) | Mobile: Stack, Drawer filtros; Desktop: Grid, Sidebar fixa |

### 2.3 Cadastros Mestres (CRUD)

| Tela | Rota | Componentes Principais | Responsivo |
| :--- | :--- | :--- | :--- |
| **Lista Entidades** (Filial, Depto, Fornecedor, Funcionário, TipoAtivo) | `/cadastros/:entity` | `PageContainer`, `EntityTable` (DataTable + Toolbar), `EntityModal` (FormField, Input, Select, Button), `Breadcrumb`, `Toast` | Mobile: Toolbar stacked, Modal fullscreen; Desktop: Toolbar horizontal, Modal md/lg |
| **Detalhe Entidade** | `/cadastros/:entity/:id` | `PageContainer`, `EntityDetail` (Tabs: Geral, Auditoria), `EntityForm` (readonly), `Button` (Editar, Excluir), `Breadcrumb` | Mobile: Tabs scrollable, Modal fullscreen; Desktop: Tabs horizontal, Modal lg |

### 2.4 Ativos & Hardware

| Tela | Rota | Componentes Principais | Responsivo |
| :--- | :--- | :--- | :--- |
| **Lista Ativos** | `/ativos` | `PageContainer`, `AssetTable` (DataTable + Toolbar: busca fuzzy, filtros), `AssetModal` (AssetForm), `Breadcrumb`, `QRCodeDisplay` (ação linha) | Mobile: Toolbar stacked, Modal fullscreen, QR modal; Desktop: Toolbar horizontal, Modal lg |
| **Criar/Editar Ativo** | `/ativos/novo`, `/ativos/:id/editar` | `AssetForm` (FormField, Input, Select, DatePicker, HardwareSection dinâmico), `Button` (Salvar, Cancelar), `QRCodeDisplay` (após criar), `PdfViewer` (Termo) | Mobile: Form stacked, HardwareSection accordion, Modal fullscreen; Desktop: Grid 2 cols, Modal xl |
| **Detalhe Ativo** | `/ativos/:id` | `PageContainer`, `AssetDetail` (Tabs: Geral, Hardware, Ordens, Auditoria, QR), `AssetTransferModal`, `AssetBaixaModal`, `QRCodeDisplay`, `PdfViewer`, `HealthScoreIndicator` | Mobile: Tabs scrollable, QR modal, Transfer/Baixa modals fullscreen; Desktop: Tabs horizontal, Modals lg |
| **Hardware Section** | (Componente em AssetForm/Detail) | `HardwareSection` (Accordion por tipo: CPU, Memória, Discos, Rede), `DiscosTable`, `MemoriaTable`, `RedeTable`, `HealthCheckForm` (ação disco) | Mobile: Accordion stacked; Desktop: Grid 2 cols |
| **Health Check (PWA Mobile)** | `/ativos/:id/health-check` | `QRScanner` (PWA), `HealthCheckForm` (Discos: SMART inputs), `Button` (Salvar), `Toast` (offline indicator), `OfflineBanner` | Mobile: Fullscreen camera, Offline banner, Background sync; Desktop: Modal centered 600px |
| **QR Code Público** | `/public/ativo/:tag` | `QRCodeDisplay` (read-only), `AssetSummary` (Card), `Button` (Ver no sistema - se logado) | Mobile: Full width card; Desktop: Card centralizado 500px |

### 2.5 Ordens de Manutenção

| Tela | Rota | Componentes Principais | Responsivo |
| :--- | :--- | :--- | :--- |
| **Lista Ordens** | `/ordens` | `PageContainer`, `OrderTable` (DataTable + Toolbar avançada: busca fuzzy, filtros estado/prioridade/tipo/filial/técnico/ativo/data), `OrderModal` (OrderForm), `Breadcrumb` | Mobile: Toolbar stacked, Modal fullscreen; Desktop: Toolbar horizontal, Modal lg |
| **Criar Ordem** | `/ordens/novo` | `OrderForm` (FormField, GlobalSearch para ativo, Select técnico/fornecedor, DatePicker, InputNumber custos), `Button` (Salvar, Cancelar), `Breadcrumb` | Mobile: Form stacked, Modal fullscreen; Desktop: Grid 2 cols, Modal xl |
| **Detalhe Ordem** | `/ordens/:id` | `PageContainer`, `OrderDetail` (Tabs: Geral, Timeline, Evidências, Custos, Auditoria), `OrderActions` (Botões contextuais: Iniciar, Aprovar, Concluir, Cancelar, Reatribuir), `EvidenceModal`, `CostForm`, `ReassignModal`, `Breadcrumb` | Mobile: Tabs scrollable, Action buttons fixed bottom, Modals fullscreen; Desktop: Tabs horizontal, Action buttons header, Modals lg |
| **Aprovar Ordem** | (Modal em Detalhe) | `EvidenceModal` (FormField: Checklist, FileUpload foto, SignaturePad assinatura), `Button` (Aprovar, Rejeitar), `Toast` | Mobile: Modal fullscreen, Camera capture; Desktop: Modal lg |
| **Concluir Ordem** | (Modal em Detalhe) | `CostForm` (FormField: InputNumber mão de obra, MaterialTable materiais, TerceirosTable), `Button` (Concluir), `Toast` | Mobile: Modal fullscreen; Desktop: Modal lg |
| **Dashboard Ordens** | `/dashboard/ordens` | `PageContainer`, `KPICard` (6), `Chart` (tendência 30d, prioridade, filial, tipo), `AlertFeed` (WebSocket: ordens criadas/iniciadas/aprovadas/concluídas/canceladas, SLA breach), `DataTable` (ordens vencendo SLA) | Mobile: Stack KPIs, Chart full width, AlertFeed stacked; Desktop: Grid KPIs, Chart + Table side-by-side |

### 2.6 Manutenção Preventiva

| Tela | Rota | Componentes Principais | Responsivo |
| :--- | :--- | :--- | :--- |
| **Lista Planos** | `/preventivas` | `PageContainer`, `PreventivaPlanTable` (DataTable), `PreventivaPlanModal` (PreventivaPlanForm), `Breadcrumb` | Mobile: Modal fullscreen; Desktop: Modal lg |
| **Criar/Editar Plano** | `/preventivas/novo`, `/preventivas/:id/editar` | `PreventivaPlanForm` (FormField: Cron expression builder, Select ativos/tipo, DatePicker preferencial, Select técnico), `Button` | Mobile: Form stacked, Modal fullscreen; Desktop: Grid 2 cols, Modal lg |
| **Calendário Preventiva** | `/preventivas/calendario` | `PageContainer`, `PreventivaCalendar` (FullCalendar), `Drawer` (detalhe plano), `Button` (Gerar agora) | Mobile: Calendar stacked, Drawer bottom sheet; Desktop: Calendar + Sidebar |
| **Relatório Aderência** | `/relatorios/preventiva-aderencia` | `PageContainer`, `AderenciaReport` (DataTable + filtros), `Chart` (aderência temporal), `Button` (Export PDF/CSV) | Mobile: Stacked; Desktop: Chart + Table side-by-side |

### 2.7 Manutenção Preditiva

| Tela | Rota | Componentes Principais | Responsivo |
| :--- | :--- | :--- | :--- |
| **Dashboard Preditiva** | `/dashboard/preditiva` | `PageContainer`, `KPICard` (4), `PrevisaoTable` (DataTable expandível: probabilidade, IC 95%, ação "Agendar Substituição"), `AlertCard` (críticos), `Chart` (probabilidade temporal), `Drawer` (filtros) | Mobile: Stack KPIs, Table stacked, Drawer bottom sheet; Desktop: Grid KPIs, Table + Chart side-by-side |
| **Health Check (PWA)** | `/ativos/:id/health-check` | `QRScanner` (PWA), `HealthCheckForm` (Discos: SMART inputs), `Button` (Salvar), `Toast` (offline), `OfflineBanner` | Mobile: Fullscreen camera, Offline banner, Background sync; Desktop: Modal centered 600px |
| **Agendar Substituição** | (Ação em PrevisaoTable) | `PreventivaPlanModal` (pre-filled: ativo, disco, data prevista - 2 dias), `Button` | Mobile: Modal fullscreen; Desktop: Modal md |

### 2.8 Busca Global

| Tela | Rota | Componentes Principais | Responsivo |
| :--- | :--- | :--- | :--- |
| **Busca Global (Header)** | Header (todas telas) | `GlobalSearch` (Input + Debounce 300ms + Dropdown agrupado por tipo + Highlight + Ações rápidas), `SearchResults` (Dropdown agrupado: Ativos, Ordens, Funcionários, Fornecedores), `SearchConfig` (Admin) | Mobile: Full width dropdown, touch-friendly; Desktop: Dropdown 400px, keyboard navigation |

### 2.9 Segurança & Admin (Aegis Shield)

| Tela | Rota | Componentes Principais | Responsivo |
| :--- | :--- | :--- | :--- |
| **Matriz RBAC** | `/admin/roles` | `PageContainer`, `RoleMatrix` (Grid visual: Roles × Permissions × Contexto Global/Filial), `Button` (Salvar), `Toast` (validação consistência) | Mobile: Table stacked (cards por role), Modal fullscreen; Desktop: Grid visual, Modal lg |
| **Gerenciar Usuários** | `/admin/usuarios` | `PageContainer`, `UserManagement` (DataTable + Toolbar), `UserModal` (UserForm: provisionamento, roles, filial, status, reset senha), `Breadcrumb` | Mobile: Modal fullscreen; Desktop: Modal lg |
| **Seletor Filial (Admin Global)** | Header (Admin Global) | `TenantSelector` (Select filtrável, avatar filial), `Toast` (troca contexto) | Mobile: Dropdown full width; Desktop: Dropdown 300px |
| **Auditoria** | `/auditoria` | `PageContainer`, `AuditTimeline` (DataTable expandível: diff visual antes/depois, filtros entidade/id/usuário/período/ação), `Button` (Export PDF/CSV assinado) | Mobile: Table stacked, Expand modal; Desktop: Table expandível, Export buttons |
| **LGPD / Privacidade** | `/privacidade` | `PrivacyPanel` (Button: Solicitar Exclusão, Exportar Dados), `DataExport` (JSON download), `ConsentManager` (Toggles por finalidade) | Mobile: Stacked; Desktop: Side-by-side panels |

### 2.10 Relatórios & QR/PDF

| Tela | Rota | Componentes Principais | Responsivo |
| :--- | :--- | :--- | :--- |
| **Termo Responsabilidade** | `/relatorios/termo-responsabilidade` | `TermoResponsabilidade` (PdfViewer + assinatura placeholder), `Button` (Gerar, Download, Imprimir, Email), `QRCodeDisplay` | Mobile: PdfViewer fullscreen, QR modal; Desktop: PdfViewer modal, QR modal |
| **Etiquetas QR Code Lote** | `/relatorios/etiquetas-lote` | `QRCodeBatchGenerator` (Filtros: filial/tipo/status/localização, Formato PDF A4/ZIP SVG), `Button` (Gerar, Download), `Toast` | Mobile: Form stacked, Download modal; Desktop: Form side-by-side, Download direct |
| **Relatórios Compliance** | `/relatorios/compliance` | `ComplianceReport` (Select tipo: NR-10/12, LGPD, ISO 27001; DateRangePicker, Filtros), `Button` (Gerar PDF assinado), `Toast` | Mobile: Form stacked; Desktop: Form side-by-side |

### 2.11 Perfil & Configurações

| Tela | Rota | Componentes Principais | Responsivo |
| :--- | :--- | :--- | :--- |
| **Meu Perfil** | `/perfil` | `PageContainer`, `ProfileForm` (FormField: avatar, nome, email, telefone, senha), `Button` (Salvar), `Tabs` (Perfil, Sessões, LGPD, Preferências) | Mobile: Tabs scrollable; Desktop: Tabs horizontal |
| **Sessões Ativas** | `/perfil/sessoes` | `SessionTable` (DataTable: device, IP, location, last activity, current), `Button` (Revogar), `Toast` | Mobile: Table stacked; Desktop: Table |
| **Preferências** | `/perfil/preferencias` | `PreferenceForm` (Switch: tema escuro, notificações push, email, idioma), `Button` (Salvar) | Mobile: Stacked; Desktop: Side-by-side |

---

## 3. Component States (Todos os Componentes)

| Estado | Visual Spec | Implementação |
| :--- | :--- | :--- |
| **Default** | Tokens padrão (`--color-bg-secondary`, `--color-border-light`, `--color-text-primary`) | Classes base |
| **Hover** | `--color-bg-tertiary` bg, `--color-border-medium` border, `--shadow-xs` shadow | `:hover:not(:disabled):not(.loading)` |
| **Focus Visible** | `outline: none`, `box-shadow: var(--shadow-focus)` (3px `--color-primary-bg`) | `:focus-visible` |
| **Active/Pressed** | `transform: scale(0.98)`, `--shadow-sm` | `:active:not(:disabled)` |
| **Loading** | Spinner (`--duration-fast` spin), `opacity: 0.8`, `cursor: wait` | `.loading` class / `loading` prop |
| **Disabled** | `opacity: 0.5`, `cursor: not-allowed`, `--color-bg-tertiary` bg, `--color-text-tertiary` text | `:disabled` / `disabled` prop |
| **Error** | `--color-border-error` border, `0 0 0 3px var(--color-danger-bg)` shadow, `--color-danger-text` error text | `.error` class / `error` prop |
| **Success** | `--color-success-bg` bg, `--color-success-border` border, `--color-success-text` text | `.success` class / `success` prop |
| **Empty** | `EmptyState` component (icon, title, description, action) | `EmptyState` component |
| **Skeleton** | `Skeleton` component (variants: text, circular, rectangular, table-row, card) | `Skeleton` component |
| **Selected (Table/Select)** | `--color-primary-bg` bg, `3px solid --color-primary` left border | `.selected` class / `selected` prop |
| **Expanded (Accordion/Table)** | `max-height: scrollHeight`, `opacity: 1`, icon `rotate(180deg)` | `.expanded` class / `expanded` prop |
| **Offline (PWA)** | `OfflineBanner` (warning bg, sync icon), `pendingCount` badge | `.offline` class / `isOffline` state |

---

## 4. Responsive Breakpoints (Wireframe Specs)

| Breakpoint | Container | Sidebar | Modal | DataTable | Toolbar | Form |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **xs (< 576px)** | 100% fluid | Drawer (hamburger) | Fullscreen (`size: full`) | Horizontal scroll + sticky 1st col | Stacked (search → filters → actions) | Single column, full width inputs |
| **sm (≥ 576px)** | 540px | Drawer | Fullscreen | Horizontal scroll + sticky 1st col | Stacked | Single column |
| **md (≥ 768px)** | 720px | Collapsible (280px) | `size: md` (600px) | Horizontal scroll + sticky 1st col | Horizontal (search | filters | actions) | 2-col grid (label + input) |
| **lg (≥ 992px)** | 960px | Fixed (280px) | `size: lg` (800px) | Full width, all cols visible | Horizontal | 2-3 col grid |
| **xl (≥ 1200px)** | 1140px | Fixed (280px) | `size: lg` | Full width | Horizontal | 2-3 col grid |
| **xxl (≥ 1400px)** | 1320px | Fixed (320px) | `size: lg` | Full width | Horizontal | 2-3 col grid |

---

## 5. Navigation & Information Architecture

### 3.1 Global Navigation (Sidebar)

```
🏠 Dashboard
  📊 Visão Geral
  📈 Preditiva
  📋 Ordens
  🔧 Ativos
  🛡️ Preventiva
  🔍 Busca
  ⚙️ Admin (se permission)
    👥 Usuários
    🛡️ Roles & Permissões
    🏢 Filiais
    📋 Auditoria
    🔒 LGPD
  📄 Relatórios
    📄 Termo Responsabilidade
    🏷️ Etiquetas QR
    📋 Compliance
  👤 Perfil
```

### 3.2 Header Global

```
[Logo Aegis1] [GlobalSearch (flex:1)] [TenantSelector (Admin Global)] [Notifications (Bell)] [UserMenu (Avatar + Dropdown)]
```

### 3.3 Breadcrumb Patterns

| Contexto | Breadcrumb |
| :--- | :--- |
| **Lista Entidade** | `Home > Cadastros > Fornecedores` |
| **Detalhe Entidade** | `Home > Cadastros > Fornecedores > Fornecedor ABC` |
| **Criar/Editar** | `Home > Cadastros > Fornecedores > Novo` / `Editar` |
| **Detalhe Ativo** | `Home > Ativos > NB-001` |
| **Detalhe Ordem** | `Home > Ordens > OS-2025-00123` |
| **Dashboard** | `Home > Dashboard` / `Home > Dashboard > Preditiva` |

---

## 6. Rastreabilidade Wireframes ↔ Artefatos

| Wireframe/Tela | Component Library | UI Style Guide | Design Tokens | User Flows | Storybook | Figma |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Login/Auth** | `Card`, `FormField`, `Input`, `Button` | `ui-style-guide.md#forms` | `tokens.scss` (form tokens) | Flow 8 (Auth) | `Card.stories`, `Button.stories` | `Auth Flow` |
| **Dashboard** | `KPICard`, `Chart`, `DataTable`, `AlertFeed` | `ui-style-guide.md#dashboard` | `tokens.scss` (chart colors) | Flow 11 | `KPICard.stories`, `Chart.stories` | `Dashboard` |
| **Cadastros** | `EntityTable`, `EntityModal`, `EntityForm` | `ui-style-guide.md#tables`, `#forms` | `tokens.scss` (table, form) | Flow 2 | `EntityTable.stories` | `Cadastros` |
| **Ativos** | `AssetTable`, `AssetForm`, `AssetDetail`, `HardwareSection` | `ui-style-guide.md#assets` | `tokens.scss` (asset tokens) | Flow 3 | `AssetForm.stories` | `Ativos` |
| **Ordens** | `OrderTable`, `OrderForm`, `OrderDetail`, `OrderActions` | `ui-style-guide.md#orders` | `tokens.scss` (order tokens) | Flow 1 | `OrderForm.stories` | `Ordens` |
| **Preventiva** | `PreventivaPlanTable`, `PreventivaCalendar` | `ui-style-guide.md#preventiva` | `tokens.scss` (preventiva tokens) | Flow 5 | `PreventivaPlanForm.stories` | `Preventiva` |
| **Preditiva** | `PreditivaDashboard`, `HealthCheckForm`, `QRScanner` | `ui-style-guide.md#preditiva` | `tokens.scss` (preditiva tokens) | Flow 6 | `PreditivaDashboard.stories` | `Preditiva` |
| **Busca Global** | `GlobalSearch`, `SearchResults` | `ui-style-guide.md#search` | `tokens.scss` (search tokens) | Flow 7 | `GlobalSearch.stories` | `Busca` |
| **Admin/RBAC** | `RoleMatrix`, `UserManagement`, `AuditTimeline` | `ui-style-guide.md#admin` | `tokens.scss` (admin tokens) | Flow 8 | `RoleMatrix.stories` | `Admin` |
| **Relatórios** | `TermoResponsabilidade`, `QRCodeBatchGenerator` | `ui-style-guide.md#reports` | `tokens.scss` (report tokens) | Flow 9 | `TermoResponsabilidade.stories` | `Relatórios` |
| **PWA Mobile** | `QRScanner`, `HealthCheckForm`, `OfflineBanner` | `ui-style-guide.md#pwa` | `tokens.scss` (pwa tokens) | Flow 4 | `QRScanner.stories` | `PWA Mobile` |

---

*Documento regenerado completamente com wireframes implementados como componentes Vue 3 + Bootstrap 5 + PWA. Substitui versão anterior inexistente. Fonte de verdade: Figma (design) + Storybook (component docs) + Código (implementação).*