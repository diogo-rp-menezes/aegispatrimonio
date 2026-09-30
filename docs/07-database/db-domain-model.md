# Database Domain & Entity Model — aegis_patrimonio

> **Versão:** 1.0 · **Owner:** backend-team · **Status:** Draft
> Modelo conceitual — a IA pode inferir este modelo a partir do domínio, mas ele nunca deve ser convertido diretamente em SQL sem passar pelo contrato físico em [[db-schema-spec]].

## 1. Domain
```yaml
domain:
  id: "aegis-patrimonio"
  name: "Gestão de Patrimônio Organizacional"
  description: "Domínio de negócio para cadastro, rastreamento, manutenção, alertas, depreciação e auditoria de bens patrimoniais (ativos) de uma organização, incluindo sua localização, responsáveis, fornecedores, classificação e histórico de saúde de hardware."
```

## 2. Entities
```yaml
entities:
  - id: "ent-ativo"
    name: "Ativo"
    description: "Bem ou item de valor no patrimônio da organização, rastreável para gestão, depreciação e manutenção. Entidade central do domínio."
    table: "ativo"
    classification: "core"
    lifecycle:
      creation: "Cadastro via createAtivo (fluxo administrativo)"
      archival: "Baixa/descarte do ativo — partição por ano em tabela de histórico"
      deletion: "Não permitido eliminação física; apenas baixa lógica (status = BAIXADO) com retenção legal"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "tag_patrimonio"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "tag_patrimonio"
        type: "String"
        required: true
      - name: "nome"
        type: "String"
        required: true
      - name: "descricao"
        type: "Text"
        required: false
      - name: "valor_aquisicao"
        type: "Money"
        required: true
      - name: "data_aquisicao"
        type: "LocalDate"
        required: true
      - name: "vida_util_anos"
        type: "Integer"
        required: true
      - name: "status"
        type: "Enum[ATIVO, EM_MANUTENCAO, BAIXADO, EM_ESTOQUE]"
        required: true
      - name: "numero_serie"
        type: "String"
        required: false
      - name: "modelo"
        type: "String"
        required: false
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
    relationships:
      - target: "TipoAtivo"
        cardinality: "N:1"
        semantic: "Classificação do ativo (ex: notebook, servidor, móvel) que define regras de depreciação"
      - target: "Localizacao"
        cardinality: "N:1"
        semantic: "Local físico ou lógico onde o ativo está alocado"
      - target: "Departamento"
        cardinality: "N:1"
        semantic: "Departamento responsável pelo ativo"
      - target: "Filial"
        cardinality: "N:1"
        semantic: "Filial/unidade de negócio à qual o ativo pertence"
      - target: "Fornecedor"
        cardinality: "N:1"
        semantic: "Fornecedor de quem o ativo foi adquirido"
      - target: "Funcionario"
        cardinality: "N:1"
        semantic: "Responsável/custodiante do ativo (colaborador)"
      - target: "AtivoDetalheHardware"
        cardinality: "1:1"
        semantic: "Detalhes técnicos de hardware (CPU, memória, disco, rede) para ativos de TI"
      - target: "Manutencao"
        cardinality: "1:N"
        semantic: "Histórico de ordens de manutenção do ativo"
      - target: "Alerta"
        cardinality: "1:N"
        semantic: "Alertas gerados para o ativo (uso de disco, manutenção vencida, etc.)"
      - name: "Auditoria"
        cardinality: "1:N"
        semantic: "Trilha de auditoria de alterações no ativo"
    invariants:
      - "tag_patrimonio deve ser único por organização"
      - "valor_aquisicao > 0"
      - "vida_util_anos > 0"
      - "data_aquisicao <= data_atual"
      - "Se status = BAIXADO, não pode haver manutenções em aberto"
      - "Ativo de TI deve ter AtivoDetalheHardware correspondente"
    data_sensitivity: "confidential"

  - id: "ent-tipo-ativo"
    name: "TipoAtivo"
    description: "Classificação de ativos que define características e regras de depreciação (ex: notebook, servidor, impressora, móvel, veículo)."
    table: "tipo_ativo"
    classification: "reference"
    lifecycle:
      creation: "Cadastro via createTipoAtivo (administrativo)"
      archival: "Inativação (soft delete) quando não houver ativos vinculados"
      deletion: "Não permitido se houver ativos referenciando"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "codigo"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "codigo"
        type: "String"
        required: true
      - name: "nome"
        type: "String"
        required: true
      - name: "descricao"
        type: "Text"
        required: false
      - name: "taxa_depreciacao_anual"
        type: "Percentage"
        required: true
      - name: "vida_util_padrao_anos"
        type: "Integer"
        required: true
      - name: "ativo"
        type: "Boolean"
        required: true
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
    relationships:
      - target: "Ativo"
        cardinality: "1:N"
        semantic: "Ativos classificados sob este tipo"
    invariants:
      - "codigo deve ser único"
      - "taxa_depreciacao_anual entre 0 e 100"
      - "vida_util_padrao_anos > 0"
    data_sensitivity: "internal"

  - id: "ent-localizacao"
    name: "Localizacao"
    description: "Local físico ou lógico onde ativos patrimoniais podem ser alocados ou monitorados (ex: sala, andar, prédio, data center, estoque)."
    table: "localizacao"
    classification: "reference"
    lifecycle:
      creation: "Cadastro via createLocalizacao"
      archival: "Inativação quando não houver ativos alocados"
      deletion: "Não permitido se houver ativos referenciando"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "codigo"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "codigo"
        type: "String"
        required: true
      - name: "nome"
        type: "String"
        required: true
      - name: "descricao"
        type: "Text"
        required: false
      - name: "tipo"
        type: "Enum[SALA, ANDAR, PREDIO, DATA_CENTER, ESTOQUE, EXTERNO]"
        required: true
      - name: "endereco_completo"
        type: "Text"
        required: false
      - name: "ativo"
        type: "Boolean"
        required: true
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
    relationships:
      - target: "Ativo"
        cardinality: "1:N"
        semantic: "Ativos alocados nesta localização"
      - target: "Filial"
        cardinality: "N:1"
        semantic: "Filial à qual esta localização pertence"
    invariants:
      - "codigo deve ser único por filial"
      - "nome não pode ser vazio"
    data_sensitivity: "internal"

  - id: "ent-departamento"
    name: "Departamento"
    description: "Unidade organizacional interna (setor, divisão) para gestão de ativos e alertas."
    table: "departamento"
    classification: "reference"
    lifecycle:
      creation: "Cadastro via createDepartamento"
      archival: "Inativação quando não houver ativos ou funcionários vinculados"
      deletion: "Não permitido se houver referências ativas"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "codigo"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "codigo"
        type: "String"
        required: true
      - name: "nome"
        type: "String"
        required: true
      - name: "descricao"
        type: "Text"
        required: false
      - name: "centro_custo"
        type: "String"
        required: false
      - name: "ativo"
        type: "Boolean"
        required: true
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
    relationships:
      - target: "Ativo"
        cardinality: "1:N"
        semantic: "Ativos sob responsabilidade deste departamento"
      - target: "Funcionario"
        cardinality: "1:N"
        semantic: "Funcionários lotados neste departamento"
      - target: "Filial"
        cardinality: "N:1"
        semantic: "Filial à qual o departamento pertence"
    invariants:
      - "codigo deve ser único por filial"
      - "nome não pode ser vazio"
    data_sensitivity: "internal"

  - id: "ent-filial"
    name: "Filial"
    description: "Unidade de negócio ou estabelecimento da organização (matriz, filiais, centros de distribuição)."
    table: "filial"
    classification: "reference"
    lifecycle:
      creation: "Cadastro via createFilial"
      archival: "Encerramento da unidade (inativação)"
      deletion: "Não permitido — retenção legal/histórica"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "cnpj"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "cnpj"
        type: "CNPJ"
        required: true
      - name: "razao_social"
        type: "String"
        required: true
      - name: "nome_fantasia"
        type: "String"
        required: false
      - name: "endereco"
        type: "Text"
        required: true
      - name: "telefone"
        type: "Phone"
        required: false
      - name: "email"
        type: "Email"
        required: false
      - name: "ativa"
        type: "Boolean"
        required: true
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
    relationships:
      - target: "Ativo"
        cardinality: "1:N"
        semantic: "Ativos pertencentes a esta filial"
      - target: "Departamento"
        cardinality: "1:N"
        semantic: "Departamentos desta filial"
      - target: "Localizacao"
        cardinality: "1:N"
        semantic: "Localizações desta filial"
      - target: "Funcionario"
        cardinality: "1:N"
        semantic: "Funcionários lotados nesta filial"
    invariants:
      - "cnpj deve ser válido e único"
      - "razao_social não pode ser vazio"
    data_sensitivity: "confidential"

  - id: "ent-fornecedor"
    name: "Fornecedor"
    description: "Entidade externa (pessoa jurídica) de quem a organização adquire ativos ou serviços de manutenção."
    table: "fornecedor"
    classification: "reference"
    lifecycle:
      creation: "Cadastro via createFornecedor"
      archival: "Inativação quando não houver contratos/ativos vigentes"
      deletion: "Não permitido se houver histórico de aquisições"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "cnpj"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "cnpj"
        type: "CNPJ"
        required: true
      - name: "razao_social"
        type: "String"
        required: true
      - name: "nome_fantasia"
        type: "String"
        required: false
      - name: "endereco"
        type: "Text"
        required: false
      - name: "telefone"
        type: "Phone"
        required: false
      - name: "email"
        type: "Email"
        required: false
      - name: "contato_comercial"
        type: "String"
        required: false
      - name: "ativo"
        type: "Boolean"
        required: true
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
    relationships:
      - target: "Ativo"
        cardinality: "1:N"
        semantic: "Ativos adquiridos deste fornecedor"
      - target: "Manutencao"
        cardinality: "1:N"
        semantic: "Ordens de manutenção executadas por este fornecedor"
    invariants:
      - "cnpj deve ser válido e único"
      - "razao_social não pode ser vazio"
    data_sensitivity: "confidential"

  - id: "ent-funcionario"
    name: "Funcionario"
    description: "Colaborador da organização, responsável por ativos (custodiante) ou solicitante de manutenções."
    table: "funcionario"
    classification: "core"
    lifecycle:
      creation: "Cadastro via createFuncionario (pode vir junto com createUsuario)"
      archival: "Desligamento (inativação) — mantém histórico de responsabilidade"
      deletion: "Não permitido — retenção legal trabalhista"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "matricula"
        type: "business"
      - name: "cpf"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "matricula"
        type: "String"
        required: true
      - name: "cpf"
        type: "CPF"
        required: true
      - name: "nome_completo"
        type: "String"
        required: true
      - name: "email_corporativo"
        type: "Email"
        required: true
      - name: "telefone"
        type: "Phone"
        required: false
      - name: "cargo"
        type: "String"
        required: true
      - name: "data_admissao"
        type: "LocalDate"
        required: true
      - name: "data_demissao"
        type: "LocalDate"
        required: false
      - name: "ativo"
        type: "Boolean"
        required: true
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
    relationships:
      - target: "Ativo"
        cardinality: "1:N"
        semantic: "Ativos sob custódia/responsabilidade deste funcionário"
      - target: "Manutencao"
        cardinality: "1:N"
        semantic: "Manutenções solicitadas ou aprovadas por este funcionário"
      - target: "Departamento"
        cardinality: "N:1"
        semantic: "Departamento de lotação"
      - target: "Filial"
        cardinality: "N:1"
        semantic: "Filial de lotação"
      - target: "Usuario"
        cardinality: "1:1"
        semantic: "Credencial de acesso ao sistema (se houver)"
    invariants:
      - "matricula deve ser única"
      - "cpf deve ser válido e único"
      - "email_corporativo deve ser único"
      - "data_demissao, se preenchida, > data_admissao"
      - "Se ativo = false, data_demissao deve estar preenchida"
    data_sensitivity: "restricted"

  - id: "ent-usuario"
    name: "Usuario"
    description: "Credencial de acesso ao sistema (autenticação/autorização), vinculada a um funcionário. Perfis: ADMIN, USER."
    table: "usuario"
    classification: "core"
    lifecycle:
      creation: "Provisionamento via createUsuario ou createFuncionarioAndUsuario"
      archival: "Desativação ao desligar funcionário"
      deletion: "Não permitido — auditoria de acessos"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "username"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "username"
        type: "String"
        required: true
      - name: "password_hash"
        type: "PasswordHash"
        required: true
      - name: "role"
        type: "Enum[ADMIN, USER]"
        required: true
      - name: "ativo"
        type: "Boolean"
        required: true
      - name: "ultimo_login"
        type: "Instant"
        required: false
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
    relationships:
      - target: "Funcionario"
        cardinality: "1:1"
        semantic: "Funcionário dono desta credencial"
    invariants:
      - "username deve ser único"
      - "password_hash nunca nulo"
      - "role deve ser ADMIN ou USER"
      - "Se funcionário inativo, usuário deve ser inativo"
    data_sensitivity: "restricted"

  - id: "ent-manutencao"
    name: "Manutencao"
    description: "Ordem de manutenção (preventiva ou corretiva) de um ativo, com fluxo de aprovação, execução e conclusão."
    table: "manutencao"
    classification: "transactional"
    lifecycle:
      creation: "Solicitação via iniciar (criação de OS)"
      archival: "Conclusão ou cancelamento — partição por ano"
      deletion: "Não permitido — histórico obrigatório"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "numero_os"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "numero_os"
        type: "String"
        required: true
      - name: "ativo_id"
        type: "UUID"
        required: true
      - name: "tipo"
        type: "Enum[PREVENTIVA, CORRETIVA]"
        required: true
      - name: "status"
        type: "Enum[SOLICITADA, AGUARDANDO_APROVACAO, APROVADA, EM_ANDAMENTO, CONCLUIDA, CANCELADA]"
        required: true
      - name: "descricao_problema"
        type: "Text"
        required: true
      - name: "data_solicitacao"
        type: "Instant"
        required: true
      - name: "data_aprovacao"
        type: "Instant"
        required: false
      - name: "data_inicio"
        type: "Instant"
        required: false
      - name: "data_conclusao"
        type: "Instant"
        required: false
      - name: "custo_estimado"
        type: "Money"
        required: false
      - name: "custo_real"
        type: "Money"
        required: false
      - name: "observacoes"
        type: "Text"
        required: false
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
    relationships:
      - target: "Ativo"
        cardinality: "N:1"
        semantic: "Ativo objeto da manutenção"
      - target: "Fornecedor"
        cardinality: "N:1"
        semantic: "Fornecedor executor (se terceirizado)"
      - target: "Funcionario"
        cardinality: "N:1"
        semantic: "Solicitante da manutenção"
      - target: "Funcionario"
        cardinality: "N:1"
        semantic: "Aprovador da manutenção"
      - target: "Funcionario"
        cardinality: "N:1"
        semantic: "Responsável técnico/executor"
    invariants:
      - "numero_os deve ser único"
      - "Se status = APROVADA, data_aprovacao não nula"
      - "Se status = EM_ANDAMENTO, data_inicio não nula"
      - "Se status = CONCLUIDA, data_conclusao não nula"
      - "Transições de status válidas: SOLICITADA → AGUARDANDO_APROVACAO → APROVADA → EM_ANDAMENTO → CONCLUIDA; qualquer → CANCELADA"
      - "custo_real >= 0 se preenchido"
    data_sensitivity: "internal"

  - id: "ent-alerta"
    name: "Alerta"
    description: "Notificação/aviso gerado pelo sistema para situações de risco ou atenção (disco cheio, manutenção vencida, garantia expirando, etc.)."
    table: "alerta"
    classification: "transactional"
    lifecycle:
      creation: "Geração automática (jobs) ou manual"
      archival: "Marcação como lido + arquivamento após 90 dias"
      deletion: "Purge após 2 anos (política de retenção)"
    identifiers:
      - name: "id"
        type: "technical"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "ativo_id"
        type: "UUID"
        required: false
      - name: "tipo"
        type: "Enum[DISCO_CRITICO, MANUTENCAO_VENCIDA, GARANTIA_EXPIRANDO, DEPRECIACAO_TOTAL, HARDWARE_DEGRADADO]"
        required: true
      - name: "severidade"
        type: "Enum[BAIXA, MEDIA, ALTA, CRITICA]"
        required: true
      - name: "titulo"
        type: "String"
        required: true
      - name: "mensagem"
        type: "Text"
        required: true
      - name: "lido"
        type: "Boolean"
        required: true
      - name: "data_leitura"
        type: "Instant"
        required: false
      - name: "created_at"
        type: "Instant"
        required: true
    relationships:
      - target: "Ativo"
        cardinality: "N:1"
        semantic: "Ativo relacionado ao alerta (pode ser nulo para alertas globais)"
      - target: "Usuario"
        cardinality: "N:1"
        semantic: "Usuário destinatário/notificado (via markAsRead)"
    invariants:
      - "Se lido = true, data_leitura não nula"
      - "Severidade CRITICA exige ativo_id não nulo"
    data_sensitivity: "internal"

  - id: "ent-ativo-detalhe-hardware"
    name: "AtivoDetalheHardware"
    description: "Detalhes técnicos de hardware para ativos de TI (CPU, memória, discos, adaptadores de rede), usado para health checks e monitoramento preditivo."
    table: "ativo_detalhe_hardware"
    classification: "supporting"
    lifecycle:
      creation: "Cadastro junto com ativo de TI ou atualização via inventário/agente"
      archival: "Removido quando ativo é baixado"
      deletion: "Cascata com ativo (1:1)"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "ativo_id"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "ativo_id"
        type: "UUID"
        required: true
      - name: "processador_modelo"
        type: "String"
        required: false
      - name: "processador_nucleos"
        type: "Integer"
        required: false
      - name: "memoria_total_gb"
        type: "Integer"
        required: false
      - name: "sistema_operacional"
        type: "String"
        required: false
      - name: "ultimo_inventario"
        type: "Instant"
        required: false
      - name: "created_at"
        type: "Instant"
        required: true
      - name: "updated_at"
        type: "Instant"
        required: true
    relationships:
      - target: "Ativo"
        cardinality: "1:1"
        semantic: "Ativo pai (apenas ativos de TI)"
      - target: "Memoria"
        cardinality: "1:N"
        semantic: "Pentes de memória instalados"
      - target: "Disco"
        cardinality: "1:N"
        semantic: "Discos/volumes instalados"
      - target: "AdaptadorRede"
        cardinality: "1:N"
        semantic: "Interfaces de rede"
    invariants:
      - "ativo_id deve referenciar ativo com tipo_ativo de categoria TI"
      - "memoria_total_gb > 0 se preenchido"
    data_sensitivity: "internal"

  - id: "ent-memoria"
    name: "Memoria"
    description: "Pente de memória RAM instalado no ativo (capacidade, tipo, velocidade, fabricante)."
    table: "memoria"
    classification: "supporting"
    lifecycle:
      creation: "Inventário de hardware"
      archival: "Remoção física do pente"
      deletion: "Cascata com AtivoDetalheHardware"
    identifiers:
      - name: "id"
        type: "technical"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "ativo_detalhe_hardware_id"
        type: "UUID"
        required: true
      - name: "capacidade_gb"
        type: "Integer"
        required: true
      - name: "tipo"
        type: "Enum[DDR3, DDR4, DDR5, LPDDR4, LPDDR5]"
        required: false
      - name: "velocidade_mhz"
        type: "Integer"
        required: false
      - name: "fabricante"
        type: "String"
        required: false
      - name: "slot"
        type: "String"
        required: false
    relationships:
      - target: "AtivoDetalheHardware"
        cardinality: "N:1"
        semantic: "Detalhe de hardware pai"
    invariants:
      - "capacidade_gb > 0"
    data_sensitivity: "internal"

  - id: "ent-disco"
    name: "Disco"
    description: "Disco/volume de armazenamento do ativo (tipo, capacidade, uso, saúde SMART)."
    table: "disco"
    classification: "supporting"
    lifecycle:
      creation: "Inventário de hardware"
      archival: "Remoção física do disco"
      deletion: "Cascata com AtivoDetalheHardware"
    identifiers:
      - name: "id"
        type: "technical"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "ativo_detalhe_hardware_id"
        type: "UUID"
        required: true
      - name: "tipo"
        type: "Enum[HDD, SSD, NVME, EMMC]"
        required: true
      - name: "capacidade_gb"
        type: "Integer"
        required: true
      - name: "usado_gb"
        type: "Integer"
        required: false
      - name: "modelo"
        type: "String"
        required: false
      - name: "serial_number"
        type: "String"
        required: false
      - name: "saude_smart"
        type: "Enum[BOM, ATENCAO, CRITICO, DESCONHECIDO]"
        required: false
      - name: "mount_point"
        type: "String"
        required: false
    relationships:
      - target: "AtivoDetalheHardware"
        cardinality: "N:1"
        semantic: "Detalhe de hardware pai"
      - target: "HealthCheckDisco"
        cardinality: "1:N"
        semantic: "Histórico de saúde do disco"
    invariants:
      - "capacidade_gb > 0"
      - "usado_gb <= capacidade_gb se preenchido"
    data_sensitivity: "internal"

  - id: "ent-adaptador-rede"
    name: "AdaptadorRede"
    description: "Interface de rede do ativo (MAC, IP, tipo, velocidade)."
    table: "adaptador_rede"
    classification: "supporting"
    lifecycle:
      creation: "Inventário de hardware"
      archival: "Remoção física da interface"
      deletion: "Cascata com AtivoDetalheHardware"
    identifiers:
      - name: "id"
        type: "technical"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "ativo_detalhe_hardware_id"
        type: "UUID"
        required: true
      - name: "mac_address"
        type: "MACAddress"
        required: true
      - name: "ip_address"
        type: "IPAddress"
        required: false
      - name: "tipo"
        type: "Enum[ETHERNET, WIFI, BLUETOOTH, VIRTUAL]"
        required: true
      - name: "velocidade_mbps"
        type: "Integer"
        required: false
      - name: "nome_interface"
        type: "String"
        required: false
    relationships:
      - target: "AtivoDetalheHardware"
        cardinality: "N:1"
        semantic: "Detalhe de hardware pai"
    invariants:
      - "mac_address deve ser único e formato válido"
    data_sensitivity: "internal"

  - id: "ent-health-check-disco"
    name: "HealthCheckDisco"
    description: "Registro periódico de indicadores de saúde do disco (espaço livre, SMART, temperatura) para monitoramento e auditoria operacional."
    table: "health_check_disco"
    classification: "audit"
    lifecycle:
      creation: "Coleta automática via agente/job (updateHealthCheck)"
      archival: "Partição mensal; retenção 13 meses"
      deletion: "Purge após retenção legal"
    identifiers:
      - name: "id"
        type: "technical"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "disco_id"
        type: "UUID"
        required: true
      - name: "espaco_livre_gb"
        type: "Integer"
        required: true
      - name: "espaco_total_gb"
        type: "Integer"
        required: true
      - name: "temperatura_celsius"
        type: "Integer"
        required: false
      - name: "smart_status"
        type: "String"
        required: false
      - name: "coletado_em"
        type: "Instant"
        required: true
    relationships:
      - target: "Disco"
        cardinality: "N:1"
        semantic: "Disco monitorado"
    invariants:
      - "espaco_livre_gb >= 0"
      - "espaco_total_gb > 0"
      - "espaco_livre_gb <= espaco_total_gb"
    data_sensitivity: "internal"

  - id: "ent-auditoria"
    name: "Auditoria"
    description: "Trilha de auditoria imutável de alterações em entidades sensíveis (ativo, funcionário, usuário, manutenção) — quem, quando, o quê, antes/depois."
    table: "auditoria"
    classification: "audit"
    lifecycle:
      creation: "Automática via triggers/callbacks (preUpdate, onUpdate) em operações de escrita"
      archival: "Partição por ano; cold storage após 2 anos"
      deletion: "Não permitido — retenção legal mínima 5 anos (LGPD/compliance)"
    identifiers:
      - name: "id"
        type: "technical"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "entidade"
        type: "String"
        required: true
      - name: "entidade_id"
        type: "UUID"
        required: true
      - name: "acao"
        type: "Enum[CREATE, UPDATE, DELETE, STATUS_CHANGE]"
        required: true
      - name: "usuario_id"
        type: "UUID"
        required: true
      - name: "valores_anteriores"
        type: "JSONB"
        required: false
      - name: "valores_novos"
        type: "JSONB"
        required: false
      - name: "ip_origem"
        type: "IPAddress"
        required: false
      - name: "created_at"
        type: "Instant"
        required: true
    relationships:
      - target: "Usuario"
        cardinality: "N:1"
        semantic: "Usuário que realizou a ação"
    invariants:
      - "Registros são imutáveis (insert-only)"
      - "created_at = now() no momento da inserção"
    data_sensitivity: "restricted"

  - id: "ent-permissao"
    name: "Permissao"
    description: "Autorização granular para que um papel execute uma ação sobre um recurso (RBAC)."
    table: "permissao"
    classification: "reference"
    lifecycle:
      creation: "Cadastro via createPermission (setup inicial)"
      archival: "Inativação se não usada por nenhum papel"
      deletion: "Não recomendado — referência em logs de auditoria"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "recurso_acao"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "recurso"
        type: "String"
        required: true
      - name: "acao"
        type: "String"
        required: true
      - name: "descricao"
        type: "Text"
        required: false
    relationships:
      - target: "Papel"
        cardinality: "N:M"
        semantic: "Papéis que possuem esta permissão"
    invariants:
      - "Par (recurso, acao) deve ser único"
    data_sensitivity: "internal"

  - id: "ent-papel"
    name: "Papel"
    description: "Papel de acesso (role) para configuração de autorização granular (ex: GESTOR_PATRIMONIO, TECNICO_MANUTENCAO, AUDITOR)."
    table: "papel"
    classification: "reference"
    lifecycle:
      creation: "Cadastro via createRole"
      archival: "Inativação se não atribuído a nenhum usuário"
      deletion: "Não permitido se referenciado em auditoria"
    identifiers:
      - name: "id"
        type: "technical"
      - name: "nome"
        type: "business"
    attributes:
      - name: "id"
        type: "UUID"
        required: true
      - name: "nome"
        type: "String"
        required: true
      - name: "descricao"
        type: "Text"
        required: false
    relationships:
      - target: "Permissao"
        cardinality: "N:M"
        semantic: "Permissões concedidas a este papel"
      - target: "Usuario"
        cardinality: "N:M"
        semantic: "Usuários que possuem este papel (além do role base ADMIN/USER)"
    invariants:
      - "nome deve ser único"
    data_sensitivity: "internal"
```

## 3. Evidence & Assumptions
```yaml
assumptions:
  - id: "ASM-001"
    claim:
      object: "ativo.tag_patrimonio"
      interpretation: "Identificador de negócio único (etiqueta de patrimônio) usado para rastreamento físico"
    confidence: 0.95
    evidence:
      - source: "db-context#L45 (createAtivo glossary term)"
      - source: "glossario#L45 (createAtivo: 'Ação de cadastrar um novo bem ou item de valor no patrimônio')"
    status: "validated"

  - id: "ASM-002"
    claim:
      object: "ativo.tipo_ativo_id → tipo_ativo"
      interpretation: "Todo ativo pertence a um tipo que define regras de depreciação"
    confidence: 0.95
    evidence:
      - source: "glossario#L78 (createTipoAtivo: 'Processo de cadastro de uma nova classificação de ativos... definindo suas características e regras de depreciação')"
      - source: "db-context#L12 (Domain: 'depreciação')"
    status: "validated"

  - id: "ASM-003"
    claim:
      object: "ativo.localizacao_id → localizacao"
      interpretation: "Ativos são alocados em localizações físicas/lógicas cadastradas"
    confidence: 0.95
    evidence:
      - source: "glossario#L84 (createLocalizacao: 'Ação de cadastrar um novo local físico ou lógico onde ativos patrimoniais podem ser alocados')"
    status: "validated"

  - id: "ASM-004"
    claim:
      object: "ativo.departamento_id → departamento"
      interpretation: "Departamento responsável pelo ativo (centro de custo/custódia)"
    confidence: 0.9
    evidence:
      - source: "glossario#L54 (createDepartamento: 'cadastro de um novo departamento na estrutura organizacional... para gestão de ativos e alertas')"
    status: "validated"

  - id: "ASM-005"
    claim:
      object: "ativo.filial_id → filial"
      interpretation: "Filial/unidade de negócio à qual o ativo pertence"
    confidence: 0.9
    evidence:
      - source: "glossario#L60 (createFilial: 'Ação de cadastrar uma nova filial ou unidade de negócio no sistema patrimonial')"
    status: "validated"

  - id: "ASM-006"
    claim:
      object: "ativo.fornecedor_id → fornecedor"
      interpretation: "Fornecedor de aquisição do ativo"
    confidence: 0.9
    evidence:
      - source: "glossario#L66 (createFornecedor: 'Processo de cadastro e estabelecimento de um novo fornecedor no sistema de gestão patrimonial')"
    status: "validated"

  - id: "ASM-007"
    claim:
      object: "ativo.funcionario_id → funcionario"
      interpretation: "Funcionário responsável/custodiante do ativo"
    confidence: 0.9
    evidence:
      - source: "glossario#L72 (createFuncionario: 'Registro de um novo colaborador no sistema para fins de validação de funcionalidades')"
      - source: "db-context#L12 (Domain: 'responsáveis')"
    status: "validated"

  - id: "ASM-008"
    claim:
      object: "ativo_detalhe_hardware (1:1 com ativo)"
      interpretation: "Apenas ativos de TI (notebook, servidor) têm detalhes de hardware"
    confidence: 0.85
    evidence:
      - source: "glossario#L108 (deleteByAtivoDetalheHardwareId: 'remoção em lote de componentes de hardware... vinculados a um identificador específico de detalhe de ativo')"
      - source: "glossario#L114 (findByAtivoDetalheHardwareId: 'busca que retorna componentes de hardware... vinculados a um detalhe de hardware específico de um ativo')"
      - source: "glossario#L156 (getHealthHistory: 'histórico de indicadores de saúde do ativo, como espaço em disco')"
      - source: "glossario#L210 (updateHealthCheck: 'Atualiza o status de saúde de um ativo com base em dados de hardware e disco')"
    status: "requires_validation"

  - id: "ASM-009"
    claim:
      object: "manutencao.status fluxo"
      interpretation: "Fluxo de estados: SOLICITADA → AGUARDANDO_APROVACAO → APROVADA → EM_ANDAMENTO → CONCLUIDA; qualquer → CANCELADA"
    confidence: 0.9
    evidence:
      - source: "glossario#L12 (aprovar: 'Ação de autorizar formalmente a execução ou conclusão de uma solicitação de manutenção')"
      - source: "glossario#L24 (cancelar: 'Ação de interromper e invalidar uma solicitação de manutenção')"
      - source: "glossario#L30 (concluir: 'Ação de finalizar uma ordem de manutenção')"
      - source: "glossario#L36 (iniciar: 'Ação de disparar o começo de um processo de manutenção')"
    status: "validated"

  - id: "ASM-010"
    claim:
      object: "alerta.tipo enum"
      interpretation: "Tipos de alerta baseados em glossário: disco crítico, manutenção vencida, garantia, depreciação, hardware degradado"
    confidence: 0.8
    evidence:
      - source: "glossario#L102 (listarAlertas: 'recuperação e apresentação do conjunto de alertas registrados para acompanhamento de situações de risco ou atenção patrimonial')"
      - source: "glossario#L150 (getRecentAlerts: 'recuperar a lista das notificações ou avisos mais recentes... situações que exigem atenção imediata')"
      - source: "glossario#L156 (getHealthHistory: 'histórico de indicadores de saúde do ativo, como espaço em disco')"
      - source: "db-context#L12 (Domain: 'alertas de uso/manutenção')"
    status: "requires_validation"

  - id: "ASM-011"
    claim:
      object: "usuario.role ∈ {ADMIN, USER}"
      interpretation: "Dois perfis base; permissões granulares via tabela papel/permissao"
    confidence: 0.9
    evidence:
      - source: "glossario#L18 (atualizar_comAdmin_deveRetornarOk: 'usuário administrador consegue atualizar registros')"
      - source: "glossario#L24 (atualizar_comUser_deveRetornarForbidden: 'usuários com perfil padrão (USER) não possuem permissão')"
      - source: "glossario#L192 (isAdmin: 'Verifica se o usuário autenticado possui o papel de administrador')"
      - source: "glossario#L54 (createPermission: 'Concede autorização para que um papel execute uma ação')"
      - source: "glossario#L60 (createRole: 'Cria um papel de acesso no sistema')"
    status: "validated"

  - id: "ASM-012"
    claim:
      object: "funcionario.usuario (1:1)"
      interpretation: "Nem todo funcionário tem usuário; apenas os que acessam o sistema"
    confidence: 0.85
    evidence:
      - source: "glossario#L78 (createFuncionarioAndUsuario: 'Processo de criação conjunta de um funcionário e seu usuário de acesso')"
      - source: "db-context#L48 (Non-Responsibilities: 'Dados de autenticação/autorização... o modelo Usuario existe no código... mas o manifesto não detalha se a tabela de usuários vive neste banco')"
    status: "requires_validation"

  - id: "ASM-013"
    claim:
      object: "auditoria (tabela imutável)"
      interpretation: "Trilha de auditoria via callbacks preUpdate/onUpdate nas entidades JPA"
    confidence: 0.9
    evidence:
      - source: "glossario#L198 (onUpdate: 'Ações automáticas executadas... antes de salvar alterações... atualização de data de modificação ou validações')"
      - source: "glossario#L204 (preUpdate: 'Método executado automaticamente antes da atualização... para manter dados de auditoria como data de modificação')"
      - source: "db-context#L12 (Domain: 'auditoria')"
      - source: "db-context#L68 (Compliance: 'recomenda-se audit log nativo (p. ex. pgaudit)')"
    status: "validated"

  - id: "ASM-014"
    claim:
      object: "health_check_disco (partição temporal)"
      interpretation: "Tabela de alta volumetria, particionada por tempo para performance"
    confidence: 0.75
    evidence:
      - source: "glossario#L156 (getHealthHistory: 'Recupera o histórico de indicadores de saúde do ativo... para fins de monitoramento e auditoria operacional')"
      - source: "db-context#L72 (Lifecycle: 'sugerir política de partitioning por ano para tabelas de auditoria/histórico')"
    status: "requires_validation"

  - id: "ASM-015"
    claim:
      object: "memoria, disco, adaptador_rede (componentes de hardware)"
      interpretation: "Entidades filhas de AtivoDetalheHardware para inventário granular"
    confidence: 0.9
    evidence:
      - source: "glossario#L108 (deleteByAtivoDetalheHardwareId: 'remoção em lote de componentes de hardware (memórias, discos, adaptadores de rede)')"
      - source: "glossario#L114 (findByAtivoDetalheHardwareId: 'retorna componentes de hardware (adaptadores de rede, discos, memórias)')"
    status: "validated"

  - id: "ASM-016"
    claim:
      object: "Banco de dados: PostgreSQL 15+"
      interpretation: "Tecnologia de banco inferida do contexto (db-context menciona PostgreSQL 15+)"
    confidence: 0.8
    evidence:
      - source: "db-context#L72 (Lifecycle: 'instância PostgreSQL 15+ provisionada por IaC')"
    status: "requires_validation"

  - id: "ASM-017"
    claim:
      object: "Migrações: Flyway/Liquibase em src/main/resources/db/migration"
      interpretation: "Ferramenta de migração versionada"
    confidence: 0.75
    evidence:
      - source: "db-context#L54 (Consumers: 'Ferramentas de migração (Flyway/Liquibase — inferido)... Local: src/main/resources/db/migration')"
    status: "requires_validation"

  - id: "ASM-018"
    claim:
      object: "Seeding: RealisticDataSeeder apenas em dev/test"
      interpretation: "Dados sintéticos para ambientes não produtivos"
    confidence: 0.95
    evidence:
      - source: "db-context#L54 (Consumers: 'RealisticDataSeeder... Popula dados de teste/desenvolvimento em ambiente development e test')"
      - source: "diagnostico: 'src/main/java/br/com/aegispatrimonio/config/seeder/RealisticDataSeeder.java:34 — Método run com complexidade ciclomática alta (15)'"
    status: "validated"

  - id: "ASM-019"
    claim:
      object: "LGPD compliance"
      interpretation: "Dados de funcionários (CPF, email) e usuários exigem proteção LGPD"
    confidence: 0.85
    evidence:
      - source: "db-context#L68 (Compliance: 'LGPD... provável, pois o projeto é brasileiro... lida com dados de responsáveis por ativos (pessoas físicas)')"
    status: "requires_validation"

  - id: "ASM-020"
    claim:
      object: "SLA produção ≥ 99,9%"
      interpretation: "Disponibilidade alvo inferida para gestão patrimonial crítica"
    confidence: 0.6
    evidence:
      - source: "db-context#L62 (Availability: 'SLA de produção: não explicitado... Recomenda-se ≥ 99,9%')"
    status: "requires_validation"
```

**Regra de threshold:**
| Confiança | Ação |
| :--- | :--- |
| ≥ 0.95 | Pode ser incorporado automaticamente |
| 0.80 – 0.94 | Pode ser proposto, requer revisão leve |
| 0.60 – 0.79 | Requer revisão humana explícita |
| < 0.60 | Não deve virar decisão de schema |