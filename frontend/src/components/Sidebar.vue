<template>
  <aside :class="['sidebar', { collapsed }]">
    <!-- Header -->
    <div class="sidebar-header">
      <div class="sidebar-logo" :class="{ centered: collapsed }">
        <i class="bi bi-shield-check"></i>
        <transition name="fade-slide">
          <span v-if="!collapsed">Aegis</span>
        </transition>
      </div>
      <transition name="fade-slide">
        <div class="sidebar-subtitle" v-if="!collapsed">Patrimônio</div>
      </transition>
      <button class="btn btn-sm btn-light mt-2 toggle-btn" @click="toggleCollapse">
        <i :class="collapsed ? 'bi-chevron-right' : 'bi-chevron-left'"></i>
      </button>
    </div>

    <!-- Menu -->
    <div class="sidebar-nav">
      <div v-for="item in menuItems" :key="item.path" class="nav-item">
        <router-link
          :to="item.path"
          class="nav-link"
          :class="{ active: isActive(item.path) }"
        >
          <i :class="['nav-icon', item.icon]"></i>
          <transition name="fade-slide">
            <span v-if="!collapsed">{{ item.title }}</span>
          </transition>
          <transition name="fade-slide">
            <span v-if="!collapsed && item.badge" class="badge">{{ item.badge }}</span>
          </transition>
        </router-link>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, defineEmits } from "vue";
import { useRoute } from "vue-router";
import { menuItems } from "../config/menu.js";
import '../assets/styles/sidebar.css';

const route = useRoute();
const collapsed = ref(false);
const emit = defineEmits(['toggle']);

const toggleCollapse = () => {
  collapsed.value = !collapsed.value;
  emit('toggle', collapsed.value);
};

const isActive = (path) =>
  route.path === path || route.path.startsWith(path + "/");
</script>
