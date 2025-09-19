<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <div class="sidebar-logo">
        <i class="bi bi-shield-check"></i>
        Aegis
      </div>
      <div class="sidebar-subtitle">Patrimônio</div>
    </div>

    <div class="sidebar-nav">
      <div v-for="item in menuItems" :key="item.path" class="nav-item">
        <router-link 
          :to="item.path" 
          class="nav-link" 
          :class="{ active: isActive(item.path) }"
        >
          <i :class="['nav-icon', item.icon]"></i>
          {{ item.title }}
        </router-link>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { menuItems } from "../config/menu.js";
import { useRoute } from "vue-router";

const route = useRoute();

const isActive = (path) => {
  // Suporta rotas dinâmicas
  return route.path === path || route.path.startsWith(path + "/");
};
</script>

<style scoped>
.sidebar {
  width: 280px;
  background: linear-gradient(135deg, var(--aegis-primary) 0%, var(--aegis-secondary) 100%);
  color: white;
  padding: 0;
  position: fixed;
  height: 100vh;
  overflow-y: auto;
  z-index: 1000;
  box-shadow: 4px 0 20px rgba(0,0,0,0.1);
}

.sidebar-header {
  padding: 2rem 1.5rem;
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
}

.sidebar-logo {
  font-size: 1.5rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.sidebar-subtitle {
  font-size: 0.875rem;
  opacity: 0.8;
  margin-top: 0.5rem;
}

.sidebar-nav {
  padding: 1rem 0;
}

.nav-item {
  margin: 0.25rem 1rem;
}

.nav-link {
  color: rgba(255,255,255,0.8);
  padding: 0.875rem 1rem;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  text-decoration: none;
  transition: all 0.3s ease;
  font-weight: 500;
}

.nav-link:hover {
  background: rgba(255,255,255,0.1);
  color: white;
  transform: translateX(4px);
}

.nav-link.active {
  background: linear-gradient(135deg, var(--aegis-accent), #ff9800);
  color: white;
  box-shadow: 0 4px 12px rgba(245, 124, 0, 0.3);
}

.nav-icon {
  font-size: 1.25rem;
  width: 24px;
  text-align: center;
}
</style>
