# Glossário & Ubiquitous Language — Aegis1

> Documento gerado automaticamente pelo pipeline determinístico de análise de código
> (server/glossary-pipeline.ts). Termos extraídos via AST + schema real (server/db.ts) + rotas
> reais (server/express-app.ts); definições geradas por IA a partir de uso real no código —
> revise as linhas marcadas com ⚠️.

## 1. Termos de Domínio
| Termo | Definição | Contexto/Bounded Context | Sinônimos observados |
| :--- | :--- | :--- | :--- |
| aprovar | Ação de autorizar oficialmente a execução ou conclusão de uma solicitação de manutenção, confirmando que ela atende aos critérios necessários. | frontend | autorizar, validar, homologar |
| atualizar | Ação de modificar os dados cadastrais de uma entidade existente para refletir seu estado mais recente ou corrigir informações. | frontend | editar, modificar, alterar |
| authInterceptor | Mecanismo responsável por gerenciar a autenticação nas requisições ao backend, anexando credenciais automaticamente e tratando renovações de acesso quando necessário. | frontend | Interceptador de Autenticação, Gerenciador de Token, Middleware de Segurança |
| buscarPorId | Operação de recuperação de uma entidade específica a partir de seu identificador único. | frontend | consultarPorId, obterPorId, recuperarPorId, findById |
| cancelar | Ação de interromper ou anular uma solicitação de manutenção antes de sua conclusão, revertendo seu estado para que não seja mais processada. | frontend | anular, abortar, desfazer |
| concluir | Ação de finalizar uma ordem de manutenção, indicando que todos os serviços foram executados e a solicitação está encerrada. | frontend | finalizar, encerrar, fechar |
| criar | Ação de registrar uma nova entidade (como departamento, filial, fornecedor ou funcionário) no sistema, tornando-a disponível para uso nas operações do negócio. | frontend | cadastrar, registrar, incluir, adicionar |
| custoTotalPorAtivo | Representa o valor financeiro total gasto com manutenções para um ativo específico. | frontend | custo total do ativo, gasto total por equipamento, despesa acumulada de manutenção |
| deletar | Ação de remover permanentemente um registro de entidade (como departamento, filial, fornecedor ou funcionário) da base de dados do sistema. | frontend | excluir, remover, apagar |
| handleApiError | Processa e padroniza falhas de comunicação com serviços externos para garantir tratamento consistente. | frontend | tratarErroApi, gerenciarFalhaComunicacao, processarErroRequisicao |
| handleResponse | Processa o retorno das chamadas de rede, garantindo que os dados recebidos estejam no formato esperado para uso nas telas. | frontend | tratarResposta, processarRetorno |
| iniciar | Ação de dar início a um processo de manutenção, alterando seu estado para 'em andamento' e registrando o responsável. | frontend | começar, dar início, abrir, executar |
| listar | Ação de recuperar e exibir uma coleção completa de registros de uma determinada entidade, como departamentos, filiais, fornecedores ou funcionários. | frontend | consultar, buscar todos, relacionar, obter lista |
| request | Representa a ação de solicitar dados ou operações ao sistema backend, encapsulando a comunicação entre a interface do usuário e os serviços de negócio. | frontend | chamada de serviço, requisição de API, solicitação de dados |
