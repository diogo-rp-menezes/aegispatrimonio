import { createRouter, createWebHistory } from "vue-router";
import DashboardInfo from "../views/DashboardInfo.vue";

const routes = [
  { path: "/", redirect: "/dashboard" },
  { path: "/dashboard", component: DashboardInfo },

  // Ativos
  { path: "/ativos", component: () => import("../views/AtivosView.vue") },
  { path: "/ativos/novo", component: () => import("../views/AtivoForm.vue") },
  { path: "/ativos/:id/editar", component: () => import("../views/AtivoForm.vue"), props: true },
  { path: "/ativos/:id", component: () => import("../views/AtivoDetail.vue"), props: true },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;