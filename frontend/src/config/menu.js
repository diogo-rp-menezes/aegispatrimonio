// src/config/menu.js
export const menuItems = [
  {
    title: "Dashboard",
    path: "/dashboard",
    icon: "bi-house"
  },
  {
    title: "Ativos",
    icon: "bi-box",
    children: [
      { title: "Listar Ativos", path: "/ativos", icon: "bi-list" },
      { title: "Novo Ativo", path: "/ativos/novo", icon: "bi-plus-circle" }
    ]
  }
  // Novos menus/submenus podem ser adicionados facilmente
];
