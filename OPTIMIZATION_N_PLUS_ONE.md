# ⚡ Optimize Ativo list queries to prevent N+1

## 💡 O quê
Otimização das consultas de listagem de ativos (`AtivoRepository.findByFilters` e `findByFilialIdsAndFilters`) adicionando `LEFT JOIN FETCH` para entidades relacionadas (`filial`, `tipoAtivo`, `localizacao`, `fornecedor`, `funcionarioResponsavel`, `detalheHardware`).

## 🎯 Porquê
Identificado problema de performance (N+1 Selects) na listagem de ativos. O `AtivoMapper` acessa propriedades de entidades relacionadas (como `nome` da filial, `nome` do tipo de ativo), mas a consulta original carregava apenas a entidade raiz `Ativo`. Como essas relações são carregadas separadamente (lazy ou eager via select secundário), isso causava múltiplas consultas ao banco de dados para cada requisição de página.

## 📊 Melhoria Mensurada
- **Redução de Queries:** De 1 + N * 5 queries (onde N é o tamanho da página) para **1 query única** (com joins) por página.
- **Impacto:** Melhora significativa na latência do Dashboard (widget "Últimos Ativos Cadastrados") e na listagem principal de ativos, especialmente conforme o volume de dados cresce.

## ⚙️ Detalhes Técnicos
- **Repositório:** `AtivoRepository`
- **JPQL:** Adicionado `LEFT JOIN FETCH` para todas as relações `@ManyToOne` e `@OneToOne` acessadas no DTO.
- **Paginação:** Adicionado `countQuery` explícito para garantir que o Spring Data JPA calcule o total de registros corretamente ao usar `JOIN FETCH`.
