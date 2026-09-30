# Glossário & Ubiquitous Language — A4

> Documento gerado automaticamente pelo pipeline determinístico de análise de código
> (server/glossary-pipeline.ts). Termos extraídos via AST + schema real (server/db.ts) + rotas
> reais (server/express-app.ts); definições geradas por IA a partir de uso real no código —
> revise as linhas marcadas com ⚠️.

## 1. Termos de Domínio
| Termo | Definição | Contexto/Bounded Context | Sinônimos observados |
| :--- | :--- | :--- | :--- |
| aprovar | Ação de autorizar formalmente a execução ou conclusão de uma solicitação de manutenção, validando sua conformidade dentro do fluxo de aprovação. | frontend | autorizar, validar, homologar, confirmar |
| atualizar | Ação de modificar os dados cadastrais de uma entidade existente no sistema, como departamentos, filiais, fornecedores ou funcionários. | frontend | editar, modificar, alterar |
| atualizar_comAdmin_deveRetornarOk | Regra de negócio que garante que usuários com perfil administrativo podem atualizar registros de entidades do sistema recebendo confirmação de sucesso. | src | atualização por administrador, permissão de edição admin, atualização autorizada |
| atualizar_comUser_deveRetornarForbidden | Regra de negócio que impede usuários com perfil padrão de alterar cadastros, retornando erro de acesso negado. | src | Restrição de atualização para usuário comum, Bloqueio de edição por permissão insuficiente, Acesso negado na modificação de registros |
| authInterceptor | Componente responsável por interceptar requisições HTTP para injetar credenciais de autenticação e tratar respostas de acesso não autorizado automaticamente. | frontend | Interceptador de Autenticação, Middleware de Auth, Tratador de Token |
| buscarPorId | Ação de recuperar os dados completos de um registro específico a partir de seu identificador único. | frontend | consultarPorId, obterPorId, recuperarPorId, findById |
| buscarPorId_comIdInexistente_deveRetornarNotFound | O sistema deve responder com 'não encontrado' quando uma busca por identificador for realizada com um ID que não existe na base de dados. | src | busca de registro inexistente, validação de ID inválido, tratamento de recurso não encontrado, resposta 404 para ID inexistente |
| cancelar | Ação de anular uma solicitação de manutenção ou um fluxo de aprovação em andamento, tornando-a sem efeito e liberando eventuais recursos ou responsáveis vinculados. | frontend | anular, revogar, abortar, desfazer |
| clearSession | Encerra a sessão do usuário removendo credenciais e dados de autenticação do armazenamento local. | frontend | logout, encerrarSessao, limparSessao, deslogar |
| concluir | Finalizar uma ordem de manutenção ou fluxo de aprovação, marcando-a como encerrada após a execução dos serviços necessários. | frontend | finalizar, encerrar, completar, fechar |
| createAtivo | Processo de criação de um ativo de teste para suportar a execução de cenários de integração dos controladores. | src | criarAtivoTeste, setupAtivo, gerarAtivoParaTeste |
| createDepartamento | Ação de cadastrar um novo departamento na estrutura organizacional do patrimônio. | src | cadastrarDepartamento, registrarDepartamento, incluirDepartamento |
| createFilial | Método utilitário de teste responsável por criar e persistir uma entidade Filial (unidade organizacional) no banco de dados para suportar cenários de integração. | src | criarFilial, setupFilial, mockFilial |
| createFornecedor | Ação de cadastrar um novo fornecedor no sistema para viabilizar testes de integração dos controladores de ativos, alertas, depreciação e manutenção. | src | cadastrarFornecedor, registrarFornecedor, incluirFornecedor |
| createFuncionario | Criação de um registro de funcionário no sistema para associar a alertas, ativos ou relatórios. | src | cadastrarFuncionario, registrarFuncionario, adicionarFuncionario |
| createFuncionarioAndUsuario | Cria um funcionário vinculado a um usuário do sistema para viabilizar cenários de teste automatizados. | src | criarFuncionarioComUsuario, setupFuncionarioUsuario, registrarFuncionarioComAcesso |
| createLocalizacao | Ação de cadastrar um novo local físico ou lógico onde ativos patrimoniais podem ser alocados, permitindo o rastreamento e a gestão da posição dos bens. | src | cadastrarLocalizacao, registrarLocalizacao, incluirLocalFisico |
| createPermission | Processo de cadastrar uma nova permissão de acesso no sistema, definindo quais ações um papel pode executar sobre determinados recursos. | src | Criar Permissão, Cadastrar Permissão, Registro de Acesso |
| createRole | Processo de criação de um papel de acesso para definição de permissões e controle de segurança no sistema. | src | criarPapel, registrarPapel, cadastrarPapel |
| createTipoAtivo | Ação de cadastrar uma nova classificação de ativo no sistema para organizar e agrupar bens patrimoniais semelhantes. | src | cadastrarTipoAtivo, registrarTipoAtivo, novoTipoAtivo |
| createUserAndToken | Processo de configuração de um usuário de teste e geração de seu token de autenticação para validação de endpoints protegidos. | src | setupUsuarioComToken, criarUsuarioAutenticadoParaTeste, gerarCredenciaisDeTeste |
| createUsuario | Processo de cadastro de um novo usuário no sistema para fins de autenticação e autorização nos testes de integração. | src | cadastrarUsuario, registrarUsuario, incluirUsuario |
| criar | Ação de registrar uma nova entidade (departamento, filial, fornecedor ou funcionário) no sistema, tornando-a disponível para uso nas operações do negócio. | frontend | cadastrar, incluir, registrar, adicionar |
| criar_comAdmin_deveRetornarCreated | Regra de negócio que estabelece que usuários com perfil administrativo devem conseguir criar novos registros e receber confirmação de criação bem-sucedida. | src | Criação autorizada por administrador, Cadastro com permissão de admin, Inclusão com retorno de sucesso para admin |
| criar_comDadosInvalidos_deveRetornarBadRequest | Regra de validação que impede a criação de registros quando os dados fornecidos não atendem aos critérios obrigatórios ou de formato, retornando erro de requisição inválida. | src | Validação de entrada na criação, Rejeição de dados inválidos no cadastro, Teste de integridade de dados obrigatórios |
| criar_comUser_deveRetornarForbidden | Usuários com perfil padrão não possuem autorização para cadastrar novos registros desta entidade, recebendo acesso negado ao tentar. | src | criação restrita a administradores, cadastro proibido para usuário comum, acesso negado na inclusão |
| custoTotalPorAtivo | Representa o somatório de todos os custos de manutenção associados a um ativo específico em um determinado período. | frontend | custo total por ativo, total cost per asset, custo agregado por ativo |
| deletar | Ação de remover permanentemente um registro ou entidade do sistema, tornando-o indisponível para consultas e operações futuras. | frontend | excluir, remover, apagar, eliminar |
| deletar_comAdmin_deveRetornarNoContent | Um administrador pode excluir permanentemente um registro do sistema, recebendo confirmação de que a operação foi concluída com sucesso sem retorno de dados adicionais. | src | exclusão por administrador, remoção com permissão de admin, deleção autorizada por administrador |
| deletar_comUser_deveRetornarForbidden | Regra de negócio que impede usuários com perfil padrão (não administradores) de excluir registros, retornando erro de acesso negado. | src | Restrição de exclusão para usuário comum, Permissão de exclusão negada para role USER, Validação de autorização no delete |
| deleteByAtivoDetalheHardwareId | Processo de remoção automática de componentes de hardware (adaptadores de rede, discos, memórias) vinculados a um detalhe de ativo específico. | src | Exclusão de hardware por ID de detalhe de ativo, Limpeza de componentes de ativo, Remoção de itens de hardware associados |
| doFilte