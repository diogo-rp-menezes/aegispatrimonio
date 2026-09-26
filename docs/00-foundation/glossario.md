# Glossário & Ubiquitous Language — Aegis1

> Documento gerado automaticamente pelo pipeline determinístico de análise de código
> (server/glossary-pipeline.ts). Termos extraídos via AST + schema real (server/db.ts) + rotas
> reais (server/express-app.ts); definições geradas por IA a partir de uso real no código —
> revise as linhas marcadas com ⚠️.

## 1. Termos de Domínio
| Termo | Definição | Contexto/Bounded Context | Sinônimos observados |
| :--- | :--- | :--- | :--- |
| aprovar | Ação de autorizar formalmente a execução ou conclusão de uma solicitação de manutenção, validando sua conformidade dentro do fluxo de trabalho. | frontend | autorizar, validar, homologar, confirmar |
| atualizar | Ação de modificar os dados cadastrais de uma entidade existente para refletir seu estado mais recente ou corrigir informações. | frontend | editar, modificar, alterar, atualizar registro |
| atualizar_comAdmin_deveRetornarOk | Regra de negócio que garante que usuários com perfil de administrador podem atualizar registros de ativos, filiais e fornecedores com sucesso. | src | atualização por administrador, permissão de atualização admin, teste de autorização admin |
| atualizar_comUser_deveRetornarForbidden | Regra de negócio que impede usuários com perfil padrão de alterar registros de departamentos, filiais ou funcionários, retornando erro de acesso negado. | src | atualização negada para usuário comum, acesso proibido na edição, validação de permissão de atualização |
| authInterceptor | Mecanismo que gerencia automaticamente as credenciais de acesso nas comunicações entre o sistema e o servidor, garantindo que as requisições estejam autenticadas. | frontend | Interceptador de Autenticação, Middleware de Segurança, Gerenciador de Tokens |
| buscarPorId | Operação de recuperação de uma entidade específica através de seu identificador único. | frontend | consultarPorId, obterPorId, recuperarPorId, findById |
| buscarPorId_comIdInexistente_deveRetornarNotFound | Regra de negócio que determina que o sistema deve informar a inexistência do registro quando uma busca é realizada com um identificador não cadastrado. | src | Validação de recurso inexistente, Tratamento de ID inválido na consulta, Retorno de não encontrado para ID desconhecido |
| cancelar | Ação de anular uma solicitação de manutenção ou um fluxo de aprovação em andamento, revertendo seu estado para cancelado. | frontend | anular, revogar, desfazer |
| concluir | Ação de finalizar uma ordem de manutenção ou uma etapa de fluxo de aprovação, alterando seu estado para encerrado ou aprovado. | frontend | finalizar, encerrar, completar, aprovar |
| createAtivo | Processo de criação de um ativo de teste para suportar a execução de cenários de integração dos controladores. | src | criarAtivoTeste, setupAtivo, instanciarAtivoParaTeste |
| createDepartamento | Função auxiliar de teste responsável por cadastrar um novo departamento no ambiente de integração para viabilizar a execução dos cenários de validação. | src | criarDepartamento, setupDepartamento, novoDepartamentoParaTeste |
| createFilial | Ação de cadastrar uma nova filial ou unidade de negócio no sistema, estabelecendo sua identidade e dados cadastrais básicos. | src | cadastrarFilial, registrarUnidade, criarUnidadeNegocio |
| createFornecedor | Criação de um novo fornecedor no sistema, registrando seus dados cadastrais para futuras aquisições e relacionamentos comerciais. | src | cadastrarFornecedor, registrarFornecedor, incluirFornecedor, novoFornecedor |
| createFuncionario | Processo de cadastro de um novo funcionário no sistema, incluindo seus dados pessoais e profissionais para vinculação a alertas, ativos e relatórios. | src | cadastrarFuncionario, registrarFuncionario, incluirFuncionario, adicionarFuncionario |
| createFuncionarioAndUsuario | Utilitário de teste que provisiona um colaborador com credenciais de acesso para validação de funcionalidades de integração. | src | criarFuncionarioEUsuario, provisionarFuncionarioComUsuario, setupFuncionarioUsuario |
| createLocalizacao | Ação de cadastrar um novo local físico ou lógico onde ativos patrimoniais podem ser alocados, permitindo sua rastreabilidade e gestão espacial. | src | Cadastrar Localização, Registrar Local, Incluir Local de Ativo |
| createPermission | Processo de estabelecer direitos de acesso e autorizações para recursos do sistema, definindo quais ações usuários ou perfis podem executar sobre determinadas entidades. | src | criarPermissao, estabelecerAcesso, definirAutorizacao, configurarPermissoes |
| createRole | Processo de criação de um novo papel ou perfil de acesso no sistema de segurança, definindo permissões e responsabilidades para usuários. | src | criar papel, criar perfil de acesso, cadastrar função, definir papel de usuário |
| createTipoAtivo | Processo de cadastro de uma nova classificação ou categoria de ativos no sistema patrimonial. | src | Cadastrar Tipo de Ativo, Incluir Classificação de Ativo, Criar Categoria de Ativo |
| createUserAndToken | Processo de teste que provisiona um usuário no sistema e gera seu token de autenticação para validação de cenários de integração. | src | provisionar usuário e token de teste, criar usuário autenticado para teste, gerar credenciais de acesso para integração |
| createUsuario | Processo de cadastro de um novo usuário no sistema, permitindo seu acesso e associação a patrimônios e alertas. | src | cadastrarUsuario, registrarUsuario, incluirUsuario |
| criar | Ação de registrar uma nova entidade (como departamento, filial, fornecedor ou funcionário) no sistema, tornando-a disponível para uso nas operações do negócio. | frontend | cadastrar, registrar, inserir, adicionar |
| criar_comAdmin_deveRetornarCreated | Regra que garante que usuários com perfil de administrador conseguem cadastrar novos registros e recebem confirmação de criação bem-sucedida. | src | Criar como admin retorna 201, Cadastro por administrador com sucesso, Teste de criação autorizada para admin |
| criar_comDadosInvalidos_deveRetornarBadRequest | O sistema deve rejeitar tentativas de cadastro com informações inválidas ou incompletas, respondendo com erro de requisição malformada. | src | Validação de dados de entrada no cadastro, Bloqueio de criação com dados inválidos, Retorno de erro 400 para payload inválido |
| criar_comUser_deveRetornarForbidden | Regra de negócio que impede usuários com perfil padrão de cadastrar novos registros, retornando erro de acesso negado. | src | Criação negada para usuário comum, Acesso proibido ao criar recurso, Validação de permissão de criação |
| custoTotalPorAtivo | Representa o somatório dos custos de manutenção agrupados por cada ativo do patrimônio. | frontend | custo total por ativo, gasto total por ativo, total de custos por ativo |
| deletar | Ação de remover permanentemente um registro de entidade (departamento, filial, fornecedor ou funcionário) da base de dados do sistema. | frontend | excluir, remover, apagar |
| deletar_comAdmin_deveRetornarNoContent | Um administrador pode excluir um recurso e o sistema confirma a exclusão sem retornar conteúdo na resposta. | src | Exclusão por administrador, Deleção com permissão de admin, Remoção autorizada por admin |
| deletar_comUser_deveRetornarForbidden | Regra de segurança que impede usuários com perfil padrão de excluir registros, retornando erro de acesso negado. | src | Restrição de exclusão para usuário comum, Bloqueio de delete para role user, Validação de permissão de exclusão |
| deleteByAtivoDetalheHardwareId | Processo de remoção em lote de componentes de hardware (adaptadores de rede, discos, memórias) vinculados a um detalhe de ativo específico. | src | Remover componentes por detalhe de ativo, Excluir itens de hardware por ID do ativo, Limpar hardware associado ao ativo |
| doFilterInternal | Método central que executa a lógica de filtro de autenticação e autorização nas requisições HTTP, validando tokens JWT e permissões de acesso. | src | Filtro de Autenticação, Validador de Token, Processador de Segurança |
| editar | Ação de modificar informações de uma entidade existente, como dados de ativos, filiais ou fornecedores. | frontend | modificar, alterar, atualizar |
| excluir | Ação de remover um registro do sistema permanentemente. | frontend | deletar, remover, apagar |
| filial | Unidade organizacional que representa uma divisão física ou lógica da empresa, contendo departamentos e localizações. | frontend | unidade, sucursal, estabelecimento |
| funcionario | Colaborador da organização, vinculado a departamentos e filiais, podendo ser responsável por ativos e solicitações. | frontend | colaborador, empregado, membro da equipe |
| fornecedor | Entidade externa que fornece produtos ou serviços para a organização, cadastrada para gestão de aquisições e contratos. | frontend | provedor, vendedor, parceiro comercial |
| gerarRelatorio | Ação de produzir um documento estruturado com dados do sistema, como termos de responsabilidade ou etiquetas de identificação. | frontend | emitir relatório, criar documento, exportar dados |
| getAll | Operação de recuperação de todos os registros de uma entidade, sem filtros de paginação ou busca. | frontend | listarTodos, obterTodos, buscarTodos |
| getById | Operação de busca de um registro específico pelo seu identificador único. | frontend | buscarPorId, obterPorId, consultarPorId |
| handleApiError | Processa e padroniza falhas de comunicação com serviços externos para garantir tratamento consistente. | frontend | tratarErroApi, gerenciarFalhaComunicacao, processarErroRequisicao |
| handleResponse | Processa o retorno das chamadas de rede, garantindo que os dados recebidos estejam no formato esperado para uso nas telas. | frontend | tratarResposta, processarRetorno |
| iniciar | Ação de dar início a um processo de manutenção, alterando seu estado para 'em andamento' e registrando o responsável. | frontend | começar, dar início, abrir, executar |
| listar | Ação de recuperar e exibir uma coleção completa de registros de uma determinada entidade, como departamentos, filiais, fornecedores ou funcionários. | frontend | consultar, buscar todos, relacionar, obter lista |
| listar_comFiltros_deveRetornarPaginado | Regra que define a listagem paginada com suporte a filtros de busca, ordenação e paginação para otimizar performance. | src | Listagem paginada com filtros, Busca com paginação, Filtros em listagem de recursos |
| login | Processo de autenticação do usuário no sistema, validando credenciais e gerando token de acesso. | frontend | autenticar, entrar, sign in |
| login_deveRetornarToken | Regra que garante que credenciais válidas geram um token JWT de acesso para sessões autenticadas. | src | Login retorna token, Autenticação bem-sucedida gera JWT, Geração de token de acesso |
| login_comCredenciaisInvalidas_deveRetornarUnauthorized | Regra de segurança que bloqueia tentativas de acesso com credenciais incorretas, retornando erro de não autorizado. | src | Bloqueio de login inválido, Falha de autenticação retorna 401, Credenciais incorretas negam acesso |
| manutencao | Processo de conservação ou reparo de ativos patrimoniais, podendo ser preventiva, corretiva ou preditiva. | frontend | manutenção, serviço técnico, intervenção |
| manutencaoPreventiva | Tipo de manutenção programada periodicamente para evitar falhas e prolongar a vida útil dos ativos. | frontend | preventiva, manutenção programada, manutenção periódica |
| manutencaoPreditiva | Tipo de manutenção baseada em análise de dados e monitoramento de condições para prever falhas antes que ocorram. | frontend | preditiva, manutenção baseada em condição, monitoramento preditivo |
| request | Representa a ação de solicitar dados ou operações ao sistema backend, encapsulando a comunicação entre a interface do usuário e os serviços de negócio. | frontend | chamada de serviço, requisição de API, solicitação de dados |
| salvar | Ação de persistir alterações ou novos registros no banco de dados. | frontend | gravar, persistir, armazenar |
| solicitacaoManutencao | Registro formal de uma necessidade de intervenção em um ativo, contendo detalhes do problema, prioridade e responsável. | frontend | ordem de serviço, chamado de manutenção, solicitação técnica |
| tipoAtivo | Classificação que categoriza ativos por natureza, como hardware, software, mobiliário, veículos, etc. | frontend | categoria de ativo, classificação de patrimônio, tipo de bem |
| usuario | Pessoa com credenciais de acesso ao sistema, associada a perfis de permissão e vinculada a funcionalidades específicas. | frontend | user, usuário do sistema, conta de acesso |

## 2. Termos Técnicos / de Implementação
| Termo | Definição | Contexto | Onde aparece |
| :--- | :--- | :--- | :--- |
| Ativo | Entidade principal do domínio patrimonial, representa um bem tangível ou intangível rastreado pelo sistema. | backend | src/main/java/.../domain/Ativo.java |
| AtivoDetalheHardware | Detalhamento de especificações de hardware de um ativo (CPU, memória, disco, rede). | backend | src/main/java/.../domain/AtivoDetalheHardware.java |
| AdaptadorRede | Componente de hardware representando uma interface de rede do ativo. | backend | src/main/java/.../domain/AdaptadorRede.java |
| Disco | Componente de hardware representando unidade de armazenamento do ativo. | backend | src/main/java/.../domain/Disco.java |
| Memoria | Componente de hardware representando memória RAM do ativo. | backend | src/main/java/.../domain/Memoria.java |
| Filial | Unidade organizacional de negócio, raiz da hierarquia de localização. | backend | src/main/java/.../domain/Filial.java |
| Departamento | Divisão organizacional dentro de uma filial. | backend | src/main/java/.../domain/Departamento.java |
| Localizacao | Local físico ou lógico onde ativos são alocados (sala, andar, rack, etc.). | backend | src/main/java/.../domain/Localizacao.java |
| Fornecedor | Entidade externa fornecedora de bens/serviços. | backend | src/main/java/.../domain/Fornecedor.java |
| Funcionario | Colaborador da organização, pode ser responsável por ativos. | backend | src/main/java/.../domain/Funcionario.java |
| TipoAtivo | Classificação/categoria de ativos (ex.: Notebook, Servidor, Impressora). | backend | src/main/java/.../domain/TipoAtivo.java |
| SolicitacaoManutencao | Ordem de serviço para manutenção de ativo. | backend | src/main/java/.../domain/SolicitacaoManutencao.java |
| ManutencaoPreventiva | Agendamento de manutenção preventiva recorrente. | backend | src/main/java/.../domain/ManutencaoPreventiva.java |
| Usuario | Conta de acesso ao sistema com roles/permissões. | backend | src/main/java/.../domain/Usuario.java |
| Role | Perfil de acesso (ex.: ADMIN, USER, GESTOR). | backend | src/main/java/.../domain/Role.java |
| Permission | Permissão granular para ações sobre recursos. | backend | src/main/java/.../domain/Permission.java |
| Auditoria | Registro imutável de alterações em entidades (Hibernate Envers). | backend | src/main/java/.../config/AuditoriaConfig.java |
| FuzzySearch | Busca aproximada usando algoritmo Levenshtein para tolerar erros de digitação. | backend | src/main/java/.../service/FuzzySearchService.java |
| LevenshteinDistance | Métrica de distância de edição entre strings, usada na busca fuzzy. | backend | src/main/java/.../util/LevenshteinDistance.java |
| ManutencaoPreditivaService | Serviço que aplica regressão linear (mínimos quadrados) para prever falhas de disco. | backend | src/main/java/.../service/ManutencaoPreditivaService.java |
| HealthCheck | Verificação de saúde do ativo baseada em métricas de hardware (SMART, temperatura, etc.). | backend | src/main/java/.../service/HealthCheckService.java |
| JwtTokenProvider | Componente que gera e valida tokens JWT para autenticação stateless. | backend | src/main/java/.../security/JwtTokenProvider.java |
| SecurityConfig | Configuração central de Spring Security (CORS, CSRF, filtros, rules). | backend | src/main/java/.../config/SecurityConfig.java |
| AegisShield | Modelo de autorização granular hierárquico e contextual com multi-tenancy por filial. | backend | src/main/java/.../security/AegisShield.java |
| MultiTenancyFilter | Filtro que isola dados por filial (tenant) no nível de query. | backend | src/main/java/.../security/MultiTenancyFilter.java |
| FlywayMigration | Scripts de migração de banco versionados (V1__init, V2__..., etc.). | backend | src/main/resources/db/migration/ |
| TestContainers | Infraestrutura de testes de integração com containers reais (MySQL, etc.). | backend | src/test/java/.../TestContainersConfig.java |
| QRCodeGenerator | Utilitário para geração de etiquetas QR Code para ativos. | backend | src/main/java/.../util/QRCodeGenerator.java |
| PdfGenerator | Geração de PDFs (Termos de Responsabilidade, relatórios). | backend | src/main/java/.../util/PdfGenerator.java |