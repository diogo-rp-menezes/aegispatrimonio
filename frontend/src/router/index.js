import { createRouter, createWebHistory } from "vue-router";
import Dashboard from "../views/Dashboard.vue";
import DetailView from "../views/DetailView.vue";

const routes = [
  { path: "/", redirect: "/dashboard" },
  { path: "/dashboard", component: Dashboard },
  { path: "/detalhe/:id", component: DetailView, props: true },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
 
