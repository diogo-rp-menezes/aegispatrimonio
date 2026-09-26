# User Flows & Interaction Diagrams — Aegis1 (Regenerado com AST Java)

> **Versão:** 2.0 · **Status:** Draft · **Owner:** UX / Product / Arquitetura
> **Base:** Use Cases v2.0 + Glossário + BRD v2.0 + NFR v2.0
> **Cobertura:** Frontend (Vue 3/PWA) + Backend (Java 21/Spring Boot) — fluxos completos extraídos via AST

---

## 1. User Personas & Actors (Atualizado com Domínio Completo)

| Ator | Papel | Objetivos Principais | Permissões (Aegis Shield) |
| :--- | :--- | :--- | :--- |
| **Admin Global** | Super-admin multi-tenant | Gerenciar filiais, roles/permissions, usuários globais, auditoria, configurações de segurança | `*_GLOBAL` (todas permissões, todas filiais); troca de contexto de filial |
| **Admin Cadastros (Filial)** | Mantém dados mestres da sua filial | CRUD departamentos, fornecedores, funcionários, tipos de ativo (scoped à filial) | `*_CRIAR`, `*_LER`, `*_ATUALIZAR`, `*_EXCLUIR` na sua filial |
| **Gestor de Manutenção** | Planejamento, KPIs, orçamento, compliance | Dashboards, custoTotalPorAtivo, aderência preventiva, preditiva, relatórios NR-10/12, LGPD | `ORDEM_LER_TODAS`, `RELATORIO_GERAR`, `ATIVO_LER`, `PREVENTIVA_GERENCIAR`, `PREDITIVA_LER` na sua filial |
| **Gestor de Patrimônio** | Ciclo de vida ativos, depreciação, TCO | Cadastro ativos com hardware, QR Code, Termo PDF, transferência, baixa, relatórios de custo | `ATIVO_CRIAR`, `ATIVO_LER`, `ATIVO_ATUALIZAR`, `ATIVO_EXCLUIR`, `HARDWARE_GERENCIAR`, `RELATORIO_GERAR` na sua filial |
| **Técnico de Campo** | Execução em campo (mobile/PWA) | Iniciar/concluir ordens, coletar health check (SMART), escanear QR Code, ver ordens alocadas | `ORDEM_INICIAR`, `ORDEM_CONCLUIR`, `ORDEM_LER_PROPRIAS`, `HEALTH_CHECK_COLETAR`, `QR_CODE_ESCANEAR` na sua filial |
| **Aprovador/Supervisor** | Validação de execução | Aprovar/rejeitar ordens com evidências, revisar checklists/fotos/assinatura | `ORDEM_APROVAR`, `ORDEM_LER_TODAS` na sua filial |
| **Auditor / Compliance** | Rastreabilidade, LGPD, ISO 27001 | Auditoria Envers completa, acessos negados, integridade dados, relatórios compliance | `AUDITORIA_LER`, `ACESSOS_NEGADOS_LER`, `INTEGRIDADE_VALIDAR` (global ou filial) |
| **DPO (Data Protection Officer)** | LGPD | Solicitações exclusão/portabilidade, DPIA, consentimento | `LGPD_GERENCIAR` (global) |

---

## 2. Core User Flows (Atualizados + Novos Fluxos Backend)

### Flow 1: Ciclo de Vida da Ordem de Manutenção (Corretiva) — **Atualizado com Estados Completos**

```mermaid
graph TD
    A[Início: Usuário autenticado JWT] --> B{Perfil / Ação}
    B -- Gestor: Criar Corretiva --> C[Tela Nova Ordem]
    C --> D[Preenche: ativo (busca fuzzy), descrição, prioridade, técnico, filial/dep/local (herdados), fornecedor opcional, custo estimado]
    D --> E{POST /api/v1/ordens via api.request}
    E -- 201 --> F[Toast sucesso + redirect detalhe]
    E -- 400/409/5xx --> G[handleApiError → toast erro específico]
    G --> D
    F --> H[Detalhe Ordem / Lista]
    B -- Técnico: Iniciar --> H
    H --> I{Estado da Ordem?}
    I -- ABERTA + técnico alocado (BR-05) --> J[Botão Iniciar habilitado]
    J --> K[Clica Iniciar]
    K --> L{PATCH /api/v1/ordens/{id}/iniciar}
    L -- 200 --> M[Badge: EM_ANDAMENTO + timestamp início + cronômetro UI]
    L -- 409 BR-05 --> N[Erro: 'Ordem não pode ser iniciada (BR-01)']
    N --> H
    M --> O{Perfil: Aprovador + permissão ORDEM_APROVAR na filial?}
    O -- Sim + canApprove:true --> P[Botão Aprovar habilitado]
    P --> Q[Modal: Evidência obrigatória (foto/checklist/assinatura digital)]
    Q --> R{PATCH /api/v1/ordens/{id}/aprovar}
    R -- 200 --> S[Badge: APROVADA + timestamp aprovador]
    R -- 400 evidência --> T[Erro validação evidência (BR-06)]
    T --> Q
    S --> U{Perfil: Técnico responsável?}
    U -- Sim --> V[Botão Concluir habilitado]
    V --> W[Form: Custos finais (mão de obra, material, terceiros) + observações]
    W --> X{PATCH /api/v1/ordens/{id}/concluir}
    X -- 200 --> Y[Badge: CONCLUIDA + custos travados + custoTotalPorAtivo atualizado]
    X -- 400 --> Z[Erro custos inválidos]
    Z --> W
    Y --> AA[Fim: Trigger preventiva check + notificações]
    I -- ABERTA sem técnico / outros estados --> AB[Ações bloqueadas / tooltip regra]
    I -- CONCLUIDA --> AC[Somente leitura + auditoria]
    I -- Qualquer exceto CONCLUIDA --> AD[Botão Cancelar disponível]
    AD --> AE[Modal confirmação + motivo obrigatório]
    AE --> AF{PATCH /api/v1/ordens/{id}/cancelar}
    AF -- 200 --> AG[Badge: CANCELADA + auditoria]
    AF -- 4xx --> AH[Erro]
    AH --> AE
```

**Estados de UI Cobertos:**
- **Loading:** Spinner em botões de ação + skeleton em detalhe + tabela com shimmer
- **Empty:** "Nenhuma ordem" + CTA "Criar primeira ordem" (Gestor)
- **Error:** Toast global via `handleApiError` (401→refresh+retry; 409→regra negócio; 5xx→erro servidor); inline em formulários
- **Success:** Toast por transição + badge atualizado otimisticamente
- **Disabled:** Botões por estado/permissão (Aegis Shield); campos custo só em APROVADA
- **Real-time:** WebSocket/SSE para atualização de badge sem refresh (ordem iniciada/aprovada/concluída por outro usuário)

**Métricas Instrumentadas:** Funil (criadas→iniciadas→aprovadas→concluídas); lead time (iniciar→aprovar); % canceladas; erros API por endpoint; SLA breach count.

---

### Flow 2: CRUD Entidades Mestres (Filial, Departamento, Fornecedor, Funcionário, TipoAtivo) — **Multi-tenancy Aware**

```mermaid
graph TD
    A[Menu Cadastros] --> B[Seletor de Filial (se Admin Global)]
    B --> C[Seleciona Entidade: Filial/Depto/Fornecedor/Funcionário/TipoAtivo]
    C --> D[Lista Paginada Server-side + Busca Fuzzy (Levenshtein)]
    D --> E{Ação}
    E -- Novo --> F[Modal Criar com validação client-side]
    F --> G{POST /api/v1/{entidade} (scoped por filial via header/contexto)}
    G -- 201 --> H[Toast + fecha modal + refresh lista]
    G -- 409 duplicado --> I[Erro inline: 'Registro já existe']
    I --> F
    E -- Editar --> J[GET /api/v1/{entidade}/{id} → Modal pré-preenchido]
    J --> K[PUT /api/v1/{entidade}/{id}]
    K -- 200 --> L[Toast + atualiza linha]
    K -- 409 --> M[Erro duplicado]
    E -- Detalhe --> N[GET /api/v1/{entidade}/{id} → Modal read-only + Auditoria tab]
    E -- Excluir --> O[Modal confirmação]
    O --> P{DELETE /api/v1/{entidade}/{id}}
    P -- 204 --> Q[Toast + remove linha]
    P -- 409 BR-04 --> R[Erro: 'Entidade possui vínculos (ativos/ordens), exclusão bloqueada']
```

**Diferenças por Entidade:**
- **Filial:** Apenas Admin Global; raiz da hierarquia; exclusão bloqueada se houver departamentos/ativos/ordens
- **Departamento:** Scoped à filial selecionada; Admin Filial ou Admin Global
- **Fornecedor:** Campos: categoria, SLA padrão, avaliação, certificações
- **Funcionário:** Vincula `Usuario` (provisiona login); define função manutenção (TECNICO/APROVADOR/SOLICITANTE)
- **TipoAtivo:** Define vida útil, valor residual %, requer hardware — usado em depreciação automática

---

### Flow 3: Cadastro Completo de Ativo com Hardware + QR Code + Termo PDF

```mermaid
graph TD
    A[Ativos → Novo Ativo] --> B[Seleciona TipoAtivo]
    B --> C{TipoAtivo.requerDetalheHardware?}
    C -- Sim --> D[Seção Hardware Expandida]
    C -- Não --> E[Formulário Básico]
    D --> D1[CPU: modelo, cores, frequência]
    D1 --> D2[Memória: total GB, tipo, frequência]
    D2 --> D3[Discos: +Adicionar → tipo SSD/HDD, capacidade, serial, SMART raw, temp, horas]
    D3 --> D4[Adaptadores Rede: +Adicionar → MAC, IP, velocidade, tipo WiFi/Ethernet]
    D4 --> E
    E --> F[Dados Comuns: tag único, serial, modelo, fabricante, data aquisição, valor, filial, depto, localização, responsável, fornecedor, NF]
    F --> G{POST /api/v1/ativos}
    G -- 201 --> H[Backend: calcula depreciação (BR-03) + gera QR Code (tag+URL+hash) + persiste Ativo + Hardware + Componentes]
    H --> I[Response: ativo completo + qrCodeUrl + termoPdfUrl]
    I --> J[UI: Toast sucesso + botões 'Ver Termo PDF' + 'Imprimir QR Code' + 'Copiar Link Público']
    J --> K[Termo PDF: abre nova aba → opção assinatura digital (placeholder v1: manual) → download]
    K --> L[QR Code: SVG/PNG → impressão etiqueta]
    G -- 400/409 --> M[Erros: tag duplicado, filial inválida, hardware obrigatório faltando]
    M --> F
```

**Estados UI:** Loading no submit (pode demorar 2s para PDF/QR); progress bar se lote; erro de validação hardware por campo.

---

### Flow 4: Gestão de Hardware do Ativo (Coleta em Campo via PWA + QR Code)

```mermaid
graph TD
    A[Técnico escaneia QR Code do ativo (câmera PWA)] --> B[PWA abre /public/ativo/{tag}?h={hash} → read-only + botão 'Coletar Health Check' se técnico autenticado]
    B --> C[Login rápido (biometria/pin) → JWT curto 15min]
    C --> D[Tela Health Check: Discos listados + campos SMART]
    D --> E[Preenche: reallocated_sectors, seek_error_rate, spin_retry_count, temperature, power_on_hours]
    E --> F{POST /api/v1/ativos/{id}/health-check}
    F -- 200 --> G[Backend: HealthCheckService.analisar() → score 0-100 + threshold check]
    G --> H{Score < threshold?}
    H -- Sim --> I[Alerta crítico + gera ordem preditiva automática (UC-27) + notificação]
    H -- Não --> J[Toast 'Health check salvo' + score exibido]
    I --> K[Dashboard preditivo atualizado tempo real]
    J --> K
    F -- 400/401/5xx --> L[Erro offline-first: salva local (IndexedDB) → sincroniza quando online]
```

**Offline-First (PWA):** Service Worker + IndexedDB para health checks offline; sync automático ao reconectar; indicador visual "pendente sincronização".

---

### Flow 5: Manutenção Preventiva — Agendamento + Geração Automática

```mermaid
graph TD
    A[Gestor → Preventiva → Novo Plano] --> B[Seleciona: Ativo(s) ou TipoAtivo + Filial]
    B --> C[Define: Frequência CRON, dia/hora, técnico padrão, descrição padrão]
    C --> D{POST /api/v1/preventivas}
    D -- 201 --> E[Plano criado + próxima execução calculada]
    E --> F[Scheduler (15min) verifica proximaExecucao <= now]
    F --> G{Para cada plano devido}
    G --> H[Cria SolicitacaoManutencao tipo PREVENTIVA + estado ABERTA + técnico padrão]
    H --> I[Atualiza proximaExecucao (próximo CRON)]
    I --> J[Notifica técnico (push/email) + WebSocket dashboard]
    J --> K[Auditoria: geração automática preventiva]
    K --> L[Gestor → Relatório Aderência: % concluídas no prazo / total geradas]
```

**Estados UI:** Lista de planos com status (ATIVO/PAUSADO); próxima execução visível; botão "Gerar Agora" (manual trigger); pausa/reativa.

---

### Flow 6: Manutenção Preditiva — Health Check + Regressão Linear + Ordem Automática

```mermaid
graph TD
    A[Batch Noturno 02:00 - ManutencaoPreditivaService] --> B[Para cada ativo com >= 3 health checks]
    B --> C[Regressão Linear Simples Mínimos Quadrados: reallocated_sectors vs power_on_hours]
    C --> D[Calcula: dias até threshold crítico + IC 95%]
    D --> E{Probabilidade > 80% E dataPrevista < 30 dias?}
    E -- Sim --> F[Cria PrevisaoFalha + Alerta + Ordem Preditiva Automática]
    E -- Não --> G[Atualiza PrevisaoFalha (monitoramento)]
    F --> H[Ordem: tipo PREDITIVA, prioridade ALTA/CRITICA, descrição com data/probabilidade]
    H --> I[Notificação: Gestor + Técnico responsável + Dashboard alerta]
    I --> J[Ação Gestor: 'Agendar Substituição' → cria preventiva programada + marca previsão TRATADA]
    G --> K[Dashboard Preditivo: cards (monitorados, alertas, ordens preditivas, previsões <30d) + tabela drill-down]
```

**Dashboard Preditivo (Tempo Real):**
- Cards: Ativos monitorados | Alertas ativos | Ordens preditivas abertas | Previsões < 30 dias
- Tabela: Ativo | Tag | Disco | Health Score | Probabilidade | Data Prevista | IC 95% | Ação (Ver Ordem / Agendar Substituição)
- Filtros: Filial, Probabilidade (slider), Horizonte (7/30/90 dias)
- WebSocket: novo alerta preditivo → toast + badge atualizado

---

### Flow 7: Busca Inteligente Global (Fuzzy Levenshtein + Filtros)

```mermaid
graph TD
    A[Header: Campo Busca Global (debounce 300ms)] --> B[Digita: 'notebok', 'dell 5520', 'joao silva']
    B --> C{GET /api/v1/busca?q=...&types=ativo,ordem,funcionario,fornecedor&filialId=1&page=0&size=10}
    C --> D[Backend: FuzzySearchService.buscar()]
    D --> E[Levenshtein distance <= 2 (config threshold 0.7) nos campos indexados]
    E --> F[Score fuzzy + boost por tipo (ativo>ordem>funcionario>fornecedor) + filtros exatos]
    F --> G[Resultados unificados paginados + highlight termo]
    G --> H[UI: Dropdown resultados agrupados por tipo + ícones + ações rápidas]
    H --> I[Clique resultado → navega para detalhe da entidade]
```

**Configuração Admin (UC-31):** Threshold similaridade (0.5-0.9), campos indexados por entidade, pesos de boost — `@RefreshScope` para reload sem restart.

---

### Flow 8: Segurança Aegis Shield — RBAC Granular + Multi-tenancy + Auditoria

#### 8.1 Login & Refresh Token
```mermaid
sequenceDiagram
    participant U as Usuário
    participant F as Frontend (Vue + Pinia)
    participant B as Backend (Spring Security)
    U->>F: Email + Senha
    F->>B: POST /api/v1/auth/login
    B->>F: 200 {accessToken (15min), refreshToken (HttpOnly cookie 7d)}
    F->>F: Armazena accessToken em memória (Pinia); refreshToken em cookie
    Note over F: authInterceptor anexa Authorization: Bearer <accessToken> em TODAS requests
    F->>B: GET /api/v1/ativos (com accessToken)
    B->>F: 200 dados
    Note over F,B: AccessToken expira (401)
    F->>B: POST /api/v1/auth/refresh (cookie automático)
    B->>F: 200 {novo accessToken}
    F->>F: Atualiza Pinia + retry request original (1x only)
    B->>F: 200 dados
    Note over F: Falha refresh → limpa Pinia/cookie → redirect /login
```

#### 8.2 Matriz de Permissões (Admin Global)
```mermaid
graph TD
    A[Admin → Roles & Permissions] --> B[Matriz Visual: Roles x Permissions x Contexto]
    B --> C[Roles: ADMIN(global), GESTOR(filial), TECNICO(proprio), USER(leitura), AUDITOR(leitura+auditoria)]
    C --> D[Permissions: ATIVO_*, ORDEM_*, RELATORIO_*, CONFIG_*, AUDITORIA_*, LGPD_*, HEALTH_CHECK_*, PREDITIVA_*]
    D --> E[Contexto: Global (ADMIN) ou Filial (demais)]
    E --> F[Edita célula → PUT /api/v1/admin/roles/{role}/permissions]
    F --> G[Validação: consistência (ex.: TECNICO não pode ter ORDEM_APROVAR)]
    G --> H[Auditoria: alteração de permissão registrada (Envers)]
```

#### 8.3 Multi-tenancy: Troca de Contexto (Admin Global)
```mermaid
graph TD
    A[Header: Seletor Filial (visível só ADMIN global)] --> B[Seleciona Filial X]
    B --> C[MultiTenancyFilter define tenantId=X no Hibernate Filter]
    C --> D[Todas queries subsequentes filtradas: WHERE filial_id = X]
    D --> E[UI reflete dados da Filial X]
    E --> F[Auditoria: troca de contexto registrada (usuario, filialAnterior, filialNova, timestamp)]
```

#### 8.4 Auditoria Envers (Consulta + Export)
```mermaid
graph TD
    A[Auditoria → Filtros: Entidade, ID, Usuario, Periodo, Acao] --> B[GET /api/v1/auditoria?entidade=Ativo&id=123&page=0&size=50]
    B --> C[Retorna: Revisao, Timestamp, Usuario, Acao (CREATE/UPDATE/DELETE), Diff campo-a-campo (antes/depois), IP, User-Agent]
    C --> D[UI: Timeline expansível + diff visual (verde/vermelho) + export PDF/CSV assinado]
    D --> E[Filtro avançado: 'Mostrar apenas alterações de custoTotalPorAtivo' / 'Apenas transferências de filial']
```

---

### Flow 9: Relatórios — Termo Responsabilidade PDF + Etiquetas QR Code Lote

#### 9.1 Termo de Responsabilidade (Alocação/Transferência/Baixa)
```mermaid
graph TD
    A[Ativo → Gerar Termo (UC-06, UC-08 transferência, UC-09 baixa)] --> B[Modal: Tipo (ALOCACAO/TRANSFERENCIA/BAIXA), observações]
    B --> C{POST /api/v1/relatorios/termo-responsabilidade}
    C --> D[Backend: PdfGenerator.gerarTermo() → template Thymeleaf/FlyingSaucer]
    D --> E[PDF com: Dados ativo + Responsável + Filial + Data + QR Code verificação + Hash integridade SHA-256]
    E --> F[Assinatura: Placeholder v1 (manual + testemunha) | Futuro: ICP-Brasil/gov.br A1]
    F --> G[Retorna stream PDF → Frontend: nova aba + download + opção 'Enviar por email']
    G --> H[Auditoria: geração termo registrada]
```

#### 9.2 Etiquetas QR Code em Lote (Inventário)
```mermaid
graph TD
    A[Relatórios → Etiquetas em Lote] --> B[Filtros: Filial, TipoAtivo, Status, Localização]
    B --> C{POST /api/v1/relatorios/etiquetas-lote}
    C --> D[QRCodeGenerator.gerarLote() → ZIP com SVGs OU PDF pronto A4 (24 etiquetas/folha)]
    D --> E[QR Code: https://aegis1.com/public/ativo/{tag}?h={hash} → página pública read-only]
    E --> F[Download ZIP/PDF → Impressão térmica/laser]
```

---

### Flow 10: LGPD — Direito ao Esquecimento + Portabilidade

```mermaid
graph TD
    A[Minha Conta → Privacidade] --> B{Botão: 'Solicitar Exclusão dos Meus Dados'}
    B --> C[POST /api/v1/usuarios/me/solicitar-exclusao]
    C --> D{Automatizado ou Workflow DPO?}
    D -- Automatizado v1 --> E[Anonimiza Usuario: nome→'USUARIO_ANON_{hash}', email→hash@anonymized.local]
    E --> F[Mantém Auditoria Envers com hash do usuário original (rastreabilidade legal)]
    F --> G[Revoga tokens + invalida sessões + status INATIVO]
    G --> H[Toast 'Seus dados foram anonimizados. Auditoria mantida para conformidade legal.']
    D -- Workflow DPO --> I[Cria tarefa DPO → notifica → DPO aprova → executa anonimização]
    A --> J[Botão: 'Exportar Meus Dados']
    J --> K[GET /api/v1/usuarios/me/exportar]
    K --> L[JSON completo: perfil, ordens, ativos responsáveis, health checks, auditoria, consentimentos]
    L --> M[Download .json + opção PDF]
```

---

### Flow 11: Dashboard Unificado (Ordens + Preditiva + Custos + Alertas)

```mermaid
graph TD
    A[Dashboard Principal] --> B[WebSocket conectado (SSE fallback)]
    B --> C[Cards KPI: Ordens Abertas | Em Andamento | Aguardando Aprovação | Vencendo SLA 24h | Concluídas Mês | Custo Total Mês]
    C --> D[Gráficos: Tendência 30d | Por Prioridade | Por Filial | Por Tipo Ativo]
    D --> E[Alertas Tempo Real: Ordem >24h sem aprovação | Ordem >SLA | Health Check Crítico | Previsão Falha <30d]
    E --> F[Clique Alerta → Navega para detalhe (Ordem / Ativo Preditivo)]
    F --> G[Drill-down: Clique card 'Custo Total Mês' → Relatório Custo por Ativo (UC-11)]
    G --> H[Drill-down: Clique 'Aderência Preventiva' → Relatório Preventiva (UC-24)]
```

**WebSocket Events:** `ordem.criada`, `ordem.iniciada`, `ordem.aprovada`, `ordem.concluida`, `ordem.cancelada`, `health.check.critico`, `preditiva.alerta`, `sla.breach`.

---

### Flow 12: Validação de Integridade & Compliance (Batch + Auditoria)

```mermaid
graph TD
    A[Batch Semanal (Domingo 03:00)] --> B[Calcula SHA-256 de tabelas críticas: ativo, ordem, usuario, auditoria]
    B --> C[Compara com baseline em tabela integridade_checksum]
    C --> D{Divergência?}
    D -- Sim --> E[Alerta CRÍTICO: Security + Admin Global + Auditor]
    E --> F[Cria Incidente Segurança + Auditoria assinada digitalmente]
    D -- Não --> G[Registra checksum atual + relatório OK]
    G --> H[Dashboard Compliance: 'Integridade OK - última verificação: {timestamp}']
```

---

## 3. Estados de UI Padronizados (Design System)

| Estado | Componente | Especificação |
| :--- | :--- | :--- |
| **Loading** | Botão, Tabela, Detalhe, Modal | Spinner 20px (botão), Skeleton (tabela/detalhe), Overlay semi-transparente (modal) |
| **Empty** | Lista, Tabela, Busca | Ilustração SVG + mensagem contextual + CTA primário (ex.: "Criar primeiro ativo") |
| **Error** | Toast global, Inline form, Modal | Toast: 4s auto-dismiss, ação "Retry" se recuperável; Inline: vermelho #DC2626, ícone alerta; Modal: erro crítico (5xx, auth) |
| **Success** | Toast, Badge otimista | Toast: verde #16A34A, 3s; Badge: transição de cor suave (ex.: amarelo→verde) |
| **Disabled** | Botão, Input, Select | Opacidade 0.5, cursor not-allowed, tooltip com razão (regra de negócio / permissão) |
| **Offline** | Banner topo, Ícone nuvem riscada | Banner fixo "Você está offline. Alterações salvas localmente." + contador pendentes |
| **Sync** | Indicador sincronização | Spinner pequeno + "Sincronizando {n} itens..." → "Sincronizado há {tempo}" |

---

## 4. Métricas de UX Instrumentadas (Por Fluxo)

| Fluxo | Métricas-Chave | Ferramenta |
| :--- | :--- | :--- |
| Ordem Corretiva | Funil conversão, Lead time (iniciar→aprovar), Taxa rejeição aprovação, Tempo conclusão | Mixpanel/Amplitude + Backend metrics |
| Cadastros Mestres | Tempo para criar, Taxa erro duplicado, Taxa exclusão bloqueada | Backend metrics |
| Ativo + Hardware | Tempo cadastro completo, % ativos com hardware completo, Taxa coleta health check campo | Backend + PWA analytics |
| Preventiva | Aderência (% prazo), Cobertura (ativos com plano), Tempo geração automática | Scheduler metrics + Relatórios |
| Preditiva | Precisão previsão (real vs previsto), Falsos positivos/negativos, Tempo até ação, Cobertura ativos monitorados | ML metrics + Business metrics |
| Busca Fuzzy | Taxa sucesso (clique resultado), Tempo resposta P95, % queries com typo, Zero results rate | Search analytics |
| Auth/Segurança | Taxa login sucesso, Tempo refresh token, Tentativas brute force, Acessos negados cross-tenant | Security metrics + SIEM |
| Relatórios | Tempo geração PDF/QR, Taxa download, Erros geração | Backend metrics |
| LGPD | Solicitações exclusão/exportação, Tempo atendimento, Conformidade prazo legal | DPO workflow metrics |

---

## 5. Acessibilidade (WCAG 2.1 AA) — Por Fluxo Crítico

| Fluxo | Requisitos AA | Implementação |
| :--- | :--- | :--- |
| Ordem (criar/iniciar/aprovar/concluir) | Focus order lógico, ARIA labels em badges/botões condicionais, Live region para toasts, Contraste 4.5:1 | Vue `focus-trap`, `aria-live="polite"` no toast container, `aria-pressed` em toggle buttons |
| Health Check (PWA mobile) | Touch target 48x48px, Orientação portrait/landscape, Redução de movimento, Legendas em gráficos | CSS `min-height: 48px`, `@media (prefers-reduced-motion)`, `aria-label` em charts |
| Busca Global | Anúncio de resultados (aria-live), Navegação por setas no dropdown, Escape para fechar | `role="combobox"`, `aria-controls`, `aria-expanded` |
| Auditoria/Relatórios | Tabelas acessíveis (scope=col/row), Export acessível, Zoom 200% sem perda | `<th scope="col">`, `tabindex="0"` em links export |
| Dashboard | Landmarks (main, aside, nav), Heading hierarchy (h1→h2→h3), Regiões live para alertas | `role="region" aria-label="Alertas críticos"` |

---

## 6. Rastreabilidade Flows ↔ Use Cases ↔ Artefatos

| Flow | Use Cases | API Spec | State Machine | Component Library | Testes E2E |
| :--- | :--- | :--- | :--- | :--- | :--- |
| Flow 1 (Ordem Corretiva) | UC-13 a UC-20 | `/api/v1/ordens` | `state-machines.md#solicitacaomanutencao` | `OrderCard`, `OrderDetail`, `OrderActions` | Cypress: criar→iniciar→aprovar→concluir |
| Flow 2 (Cadastros) | UC-01 a UC-05 | `/api/v1/filiais`, `/departamentos`, `/fornecedores`, `/funcionarios`, `/tipos-ativo` | — | `EntityTable`, `EntityForm`, `EntityModal` | Cypress: CRUD cada entidade |
| Flow 3 (Ativo + Hardware) | UC-06 a UC-12 | `/api/v1/ativos`, `/hardware`, `/relatorios/custo-total` | `state-machines.md#ativo` | `AtivoForm`, `HardwareSection`, `QRCodeDisplay`, `PdfViewer` | Cypress: cadastro hardware + QR + PDF |
| Flow 4 (Health Check PWA) | UC-25 | `/api/v1/ativos/{id}/health-check` | `state-machines.md#healthcheck` | `HealthCheckForm`, `QRScanner` | Cypress + PWA: offline→sync |
| Flow 5 (Preventiva) | UC-21 a UC-24 | `/api/v1/preventivas`, `/relatorios/preventiva-aderencia` | `state-machines.md#manutencaopreventiva` | `PreventivaPlanForm`, `PreventivaCalendar` | Cypress: criar plano → geração auto |
| Flow 6 (Preditiva) | UC-25 a UC-28 | `/api/v1/health-check`, `/dashboard/preditiva`, `/previsao-falha` | `state-machines.md#healthcheck` | `PreditivaDashboard`, `PrevisaoTable`, `AlertCard` | Unit: ManutencaoPreditivaServiceTest; Cypress: dashboard |
| Flow 7 (Busca Fuzzy) | UC-29 a UC-31 | `/api/v1/busca` | — | `GlobalSearch`, `SearchResults`, `SearchConfig` | Unit: FuzzySearchServiceTest, LevenshteinDistanceTest |
| Flow 8 (Aegis Shield) | UC-32 a UC-38 | `/api/v1/auth`, `/admin/roles`, `/admin/usuarios`, `/auditoria` | `state-machines.md#usuario` | `LoginForm`, `RoleMatrix`, `TenantSelector`, `AuditTimeline` | Integration: SecurityConfigIT, AegisShieldTest, MultiTenancyIT |
| Flow 9 (Relatórios QR/PDF) | UC-39 a UC-42 | `/api/v1/relatorios/termo`, `/qr-code`, `/etiquetas-lote`, `/compliance` | — | `PdfViewer`, `QRCodeGenerator`, `BatchExport` | Integration: RelatorioControllerIT, QRCodeGeneratorTest |
| Flow 10 (LGPD) | UC-37 | `/api/v1/usuarios/me/exclusao`, `/exportar` | — | `PrivacyPanel`, `DataExport` | Integration: UsuarioControllerIT (LGPD) |
| Flow 11 (Dashboard) | UC-19, UC-28 | `/api/v1/dashboard/ordens`, `/dashboard/preditiva` | — | `KPICard`, `ChartWidget`, `AlertFeed`, `WebSocketProvider` | Cypress: real-time updates |
| Flow 12 (Integridade) | UC-45 | `/api/v1/auditoria/integridade` | — | `IntegrityBadge` | Integration: IntegridadeChecksumIT |

---

*Documento regenerado com base em análise AST completa do backend Java (domain, service, controller, security, predictive, search, audit, report, scheduler) + frontend Vue/PWA. Substitui versão 1.0 que continha apenas visão frontend.*