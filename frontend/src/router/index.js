import { createRouter, createWebHistory } from "vue-router";
import DashboardInfo from "../views/DashboardInfo.vue";
import LoginView from "../views/LoginView.vue";
import MainLayout from "../components/MainLayout.vue";

const routes = [
  { path: "/login", component: LoginView },
  {
    path: "/",
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      { path: "", redirect: "dashboard" },
      { path: "dashboard", component: DashboardInfo },
      { path: "ativos", component: () => import("../views/AtivosView.vue") },
      { path: "ativos/novo", component: () => import("../views/AtivoForm.vue") },
      { path: "ativos/:id/editar", component: () => import("../views/AtivoForm.vue"), props: true },
      { path: "ativos/:id", component: () => import("../views/DetailView.vue"), props: true },

      // Fornecedores
      { path: "fornecedores", component: () => import("../views/fornecedores/FornecedorList.vue") },
      { path: "fornecedores/novo", component: () => import("../views/fornecedores/FornecedorForm.vue") },
      { path: "fornecedores/:id/editar", component: () => import("../views/fornecedores/FornecedorForm.vue"), props: true },
    ]
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const isAuthenticated = !!localStorage.getItem('authToken');
  // Check if any matched route requires auth
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);

  if (requiresAuth && !isAuthenticated) {
    next('/login');
  } else {
    next();
  }
});

export default router;
