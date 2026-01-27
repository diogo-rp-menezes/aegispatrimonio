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

      // Funcionários
      { path: "funcionarios", component: () => import("../views/pessoas/PessoasList.vue") },
      { path: "funcionarios/novo", component: () => import("../views/pessoas/PessoaForm.vue") },
      { path: "funcionarios/:id/editar", component: () => import("../views/pessoas/PessoaForm.vue"), props: true },
      { path: "funcionarios/:id", component: () => import("../views/pessoas/PessoaForm.vue"), props: true },

      // Manutenções
      { path: "manutencoes", component: () => import("../views/ManutencoesView.vue") },

      // System Health (Admin Only)
      {
        path: "system-health",
        component: () => import("../views/SystemHealthView.vue"),
        meta: { roles: ['ROLE_ADMIN'] }
      },

      // Admin - RBAC
      {
        path: "admin/roles",
        component: () => import("../views/admin/RoleList.vue"),
        meta: { roles: ['ROLE_ADMIN'] }
      },
      {
        path: "admin/roles/novo",
        component: () => import("../views/admin/RoleForm.vue"),
        meta: { roles: ['ROLE_ADMIN'] }
      },
      {
        path: "admin/roles/:id/editar",
        component: () => import("../views/admin/RoleForm.vue"),
        meta: { roles: ['ROLE_ADMIN'] }
      },
      {
        path: "admin/permissions",
        component: () => import("../views/admin/PermissionList.vue"),
        meta: { roles: ['ROLE_ADMIN'] }
      },
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
    return;
  }

  // Check Role
  const roles = to.meta.roles;
  if (roles && roles.length > 0) {
    const userRolesStr = localStorage.getItem('userRoles');
    const userRoles = userRolesStr ? JSON.parse(userRolesStr) : [];
    const hasRole = roles.some(role => userRoles.includes(role));

    if (!hasRole) {
      // Redirect to dashboard if unauthorized
      next('/dashboard');
      return;
    }
  }

  next();
});

export default router;
