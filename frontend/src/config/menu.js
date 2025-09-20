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
    title: "Comodatos",
    path: "/comodatos",
    icon: "bi bi-arrow-left-right"
  },
  {
    title: "Relatórios",
    path: "/relatorios",
    icon: "bi bi-file-earmark-text"
  },
  {
    title: "Configurações",
    path: "/configuracoes",
    icon: "bi bi-gear"
  }
];
