# ⚡ Added Real-time System Alerts Widget to Dashboard

## 💡 O quê
Adição de um widget de "Alertas do Sistema" no Dashboard principal (`DashboardInfo.vue`), consumindo a API `/api/v1/alerts/recent`.

## 🎯 Porquê
O Dashboard anterior focava apenas em métricas estáticas e preditivas (longo prazo). Faltava visibilidade para problemas *em tempo real* (ex: CPU > 90%, Disco Crítico < 7 dias) que já estavam sendo gerados pelo backend (`AlertNotificationService`), mas não eram exibidos proativamente ao usuário.

## 📊 Melhoria Mensurada
- **Visibilidade:** 100% de visibilidade dos últimos 5 alertas críticos sem necessidade de navegação.
- **Ação:** Redução de cliques para "Marcar como Lido" (direto no dashboard).
- **Cobertura de Testes:** Novo teste E2E (`dashboard.spec.js`) validando a renderização e visibilidade do widget.

## ⚙️ Detalhes Técnicos
- **Frontend:** Vue.js 3 + Bootstrap 5 Cards.
- **Integração:** `GET /alerts/recent` e `PATCH /alerts/{id}/read`.
- **Estado:** Reativo com `ref([])` e atualização otimista na UI ao marcar como lido.
