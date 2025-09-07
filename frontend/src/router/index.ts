// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router';
import DashboardView from '@/views/DashboardView.vue';
import PatrimonyListView from '@/views/PatrimonyListView.vue';

const routes = [
  {
    path: '/',
    name: 'Dashboard',
    component: DashboardView
  },
  {
    path: '/patrimony',
    name: 'PatrimonyList',
    component: PatrimonyListView
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;