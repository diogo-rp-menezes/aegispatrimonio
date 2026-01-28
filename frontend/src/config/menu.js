// src/config/menu.js
export const menuItems = [
  {
    title: "Dashboard",
    path: "/dashboard",
    icon: "bi bi-speedometer2"
  },
  {
    title: "Equipamentos",
    path: "/ativos",
    icon: "bi bi-pc-display",
    badge: 5,
    submenus: [
      { title: "TI", path: "/ativos/ti" },
      { title: "Mobiliário", path: "/ativos/mobiliario" },
      { title: "Especiais", path: "/ativos/especiais" }
    ]
  },
  {
    title: "Fornecedores",
    path: "/fornecedores",
    icon: "bi bi-people-fill"
  },
  {
    title: "Funcionários",
    path: "/funcionarios",
    icon: "bi bi-person-badge"
  },
  {
    title: "Comodatos",
    path: "/comodatos",
    icon: "bi bi-arrow-left-right"
  },
  {
    title: "Manutenções",
    path: "/manutencoes",
    icon: "bi bi-tools"
  },
  {
    title: "Relatórios",
    path: "/relatorios",
    icon: "bi bi-file-earmark-text"
  },
  {
    title: "Saúde do Sistema",
    path: "/system-health",
    icon: "bi bi-cpu",
    role: "ROLE_ADMIN"
  },
  {
    title: "Administração",
    path: "/admin",
    icon: "bi bi-shield-lock",
    role: "ROLE_ADMIN",
    submenus: [
      { title: "Perfis de Acesso", path: "/admin/roles" },
      { title: "Permissões", path: "/admin/permissions" }
    ]
  },
  {
    title: "Configurações",
    path: "/configuracoes",
    icon: "bi bi-gear"
  }
];
