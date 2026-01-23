import { createRouter, createWebHistory } from "vue-router";
import DashboardInfo from "../views/DashboardInfo.vue";
import LoginView from "../views/LoginView.vue";

const routes = [
  { path: "/login", component: LoginView },
  { path: "/", redirect: "/dashboard" },
  { path: "/dashboard", component: DashboardInfo, meta: { requiresAuth: true } },
  { path: "/ativos", component: () => import("../views/AtivosView.vue"), meta: { requiresAuth: true } },
  { path: "/ativos/novo", component: () => import("../views/AtivoForm.vue"), meta: { requiresAuth: true } },
  { path: "/ativos/:id/editar", component: () => import("../views/AtivoForm.vue"), props: true, meta: { requiresAuth: true } },
  { path: "/ativos/:id", component: () => import("../views/DetailView.vue"), props: true, meta: { requiresAuth: true } },

  // Fornecedores
  { path: "/fornecedores", component: () => import("../views/fornecedores/FornecedorList.vue"), meta: { requiresAuth: true } },
  { path: "/fornecedores/novo", component: () => import("../views/fornecedores/FornecedorForm.vue"), meta: { requiresAuth: true } },
  { path: "/fornecedores/:id/editar", component: () => import("../views/fornecedores/FornecedorForm.vue"), props: true, meta: { requiresAuth: true } },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const isAuthenticated = !!localStorage.getItem('authToken');
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login');
  } else {
    next();
  }
});

export default router;
