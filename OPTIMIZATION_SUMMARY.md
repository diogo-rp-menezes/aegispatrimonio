# ⚡ Otimização de Performance no Dashboard: Manutenção Preditiva

## 💡 O quê
Adição de um índice de banco de dados (`idx_ativos_previsao_esgotamento`) na coluna `previsao_esgotamento_disco` da tabela `ativos`.

## 🎯 Porquê
O Dashboard realiza múltiplas queries de range e ordenação nesta coluna para calcular:
- Ativos Críticos (< 7 dias)
- Alertas (7-30 dias)
- Ativos Saudáveis (> 30 dias)
- Ativos em Risco (Top 5 ordenado)
- Tendência de Falhas (Próximas 8 semanas)

Sem o índice, cada carregamento do dashboard forçava um *Full Table Scan* na tabela de ativos. Conforme o volume de dados cresce, isso degradaria significativamente a performance e a experiência do usuário.

## 📊 Melhoria Esperada
- **Antes:** Complexidade O(N) para cada métrica preditiva.
- **Depois:** Complexidade O(log N) para buscas de range e ordenação.
- **Impacto:** Redução drástica na latência do Dashboard e menor carga na CPU do banco de dados.

## ⚙️ Detalhes Técnicos
- **Migração:** Flyway V11 (`V11__add_index_predictive_maintenance.sql`)
- **Validação:** Testes de integração (`DashboardControllerIT`) verificaram que a funcionalidade permanece intacta.
