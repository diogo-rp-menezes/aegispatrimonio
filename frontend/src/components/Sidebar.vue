<template>
  <nav :class="['sidebar', { collapsed }]">
    <!-- Header -->
    <div class="sidebar-header">
      <div :class="['sidebar-logo', { centered: collapsed }]">
        <i class="bi bi-shield-check"></i>
        <span v-if="!collapsed">Aegis</span>
      </div>
      <div class="sidebar-subtitle" v-if="!collapsed">Patrimônio</div>
      <button class="toggle-btn" @click="toggleSidebar">
        <i :class="collapsed ? 'bi bi-arrow-right-square' : 'bi bi-arrow-left-square'"></i>
      </button>
    </div>

    <!-- Menu -->
    <div class="sidebar-nav">
      <div v-for="item in menuItems" :key="item.title" class="nav-item">
        <!-- Item com submenu -->
        <div v-if="item.submenus" class="nav-link"
             :class="{ 'submenu-open': isOpen(item) }"
             @click="!collapsed && toggleSubmenu(item)">
          <i :class="item.icon" class="nav-icon"></i>
          <span v-if="!collapsed">{{ item.title }}</span>
          <span v-if="item.badge && !collapsed" class="badge">{{ item.badge }}</span>
          <i v-if="!collapsed && item.submenus" class="bi bi-chevron-down submenu-toggle" :class="{ rotated: isOpen(item) }"></i>
        </div>

        <!-- Item sem submenu -->
        <router-link v-else
                     :to="item.path"
                     class="nav-link"
                     :class="{ active: isActive(item) }"
                     v-tooltip="collapsed ? item.title : null">
          <i :class="item.icon" class="nav-icon"></i>
          <span v-if="!collapsed">{{ item.title }}</span>
          <span v-if="item.badge && !collapsed" class="badge">{{ item.badge }}</span>
        </router-link>

        <!-- Submenu -->
        <transition name="slide-fade">
          <div v-if="item.submenus && isOpen(item) && !collapsed" class="submenu">
            <router-link v-for="sub in item.submenus" :key="sub.title"
                         :to="sub.path"
                         class="nav-subitem"
                         :class="{ active: isActive(sub) }">
              {{ sub.title }}
            </router-link>
          </div>
        </transition>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref } from "vue";
import { useRoute } from "vue-router";
import { menuItems } from "../config/menu.js";

const collapsed = ref(false);
const openSubmenus = ref([]);
const route = useRoute();

import { defineEmits } from "vue";
const emit = defineEmits(['toggle']);

const toggleSidebar = () => {
  collapsed.value = !collapsed.value;
  if (collapsed.value) openSubmenus.value = [];
  emit('toggle', collapsed.value);
};

const toggleSubmenu = (item) => {
  const index = openSubmenus.value.indexOf(item.title);
  if (index > -1) openSubmenus.value.splice(index, 1);
  else openSubmenus.value.push(item.title);
};

const isOpen = (item) => openSubmenus.value.includes(item.title);

const isActive = (item) => route.path === item.path || route.path.startsWith(item.path + "/");
</script>

<style src="../assets/styles/sidebar.css"></style>
