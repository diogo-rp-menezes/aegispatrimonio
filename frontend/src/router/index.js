import { createRouter, createWebHistory } from "vue-router";
import DashboardInfo from "../views/DashboardInfo.vue";

const routes = [
  { path: "/", redirect: "/dashboard" },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
