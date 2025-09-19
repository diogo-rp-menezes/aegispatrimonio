import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";

// Bootstrap e ícones
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap";
import "bootstrap-icons/font/bootstrap-icons.css";

// CSS modularizado do projeto
import "./assets/styles/global.css";
import "./assets/styles/layout.css";
import "./assets/styles/cards.css";
import "./assets/styles/buttons.css";
import "./assets/styles/tables.css";
import "./assets/styles/badges.css";
import "./assets/styles/topbar.css";
import "./assets/styles/sidebar.css";

createApp(App).use(router).mount("#app");
