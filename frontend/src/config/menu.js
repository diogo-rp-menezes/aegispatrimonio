// src/config/menu.js
// src/config/menu.js
export const menuItems = [
  { title: "Dashboard", path: "/dashboard", icon: "bi-speedometer2" },
  {
    title: "Equipamentos",
    path: "/ativos",
    icon: "bi-pc-display",
    children: [
      { title: "TI", path: "/ativos/ti" },
      { title: "Mobiliário", path: "/ativos/mobiliario" },
      { title: "Especiais", path: "/ativos/especiais" }
    ]
  },
  { title: "Comodatos", path: "/ativos/comodatos", icon: "bi-arrow-left-right" },
  { title: "Relatórios", path: "/relatorios", icon: "bi-file-earmark-text" },
  { title: "Configurações", path: "/configuracoes", icon: "bi-gear" }
];
