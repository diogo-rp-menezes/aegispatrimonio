# ⚡ Implementação de Filtro "Indeterminado" no Dashboard

## 💡 O quê
Implementação da capacidade de filtrar ativos com status de saúde "Indeterminado" (sem predição calculada) diretamente a partir do widget de Manutenção Preditiva do Dashboard.

## 🎯 Porquê
O Dashboard exibia a contagem de ativos "Indeterminados", mas o clique no card não aplicava nenhum filtro, redirecionando para a lista completa de ativos. Isso dificultava a identificação de ativos que não estão enviando dados de telemetria ou para os quais a predição ainda não foi calculada.

## 📊 Melhoria Mensurada
- **Experiência do Usuário (UX):** Drill-down funcional para todos os estados de saúde (Crítico, Alerta, Saudável, Indeterminado).
- **Visibilidade:** Permite aos gestores identificar rapidamente ativos não monitorados ou com falhas na coleta de métricas.
- **Cobertura de Testes:** Novos testes unitários no backend garantem que a lógica de filtragem por existência de predição (`IS NULL` vs `IS NOT NULL`) funciona corretamente.
