<template>
  <aside :class="['sidebar', { collapsed }]">
    <div class="sidebar-header">
      <div class="sidebar-logo">
        <i class="bi bi-shield-check"></i>
        <transition name="fade-slide">
          <span v-if="!collapsed">Aegis</span>
        </transition>
      </div>
      <transition name="fade-slide">
        <div class="sidebar-subtitle" v-if="!collapsed">Patrimônio</div>
      </transition>
      <button class="btn btn-sm btn-light mt-2" @click="toggleCollapse">
        <i :class="collapsed ? 'bi-chevron-right' : 'bi-chevron-left'"></i>
      </button>
    </div>

    <div class="sidebar-nav">
      <div v-for="item in menuItems" :key="item.path" class="nav-item">
        <router-link
          :to="item.path"
          class="nav-link"
          :class="{ active: isActive(item.path) }"
          @click.prevent="toggleSubmenu(item)"
        >
          <i :class="['nav-icon', item.icon]"></i>
          <transition name="fade-slide">
            <span v-if="!collapsed">{{ item.title }}</span>
          </transition>
          <i
            v-if="item.children && !collapsed"
            :class="[
              'bi',
              openSubmenus.includes(item.path) ? 'bi-chevron-down' : 'bi-chevron-right'
            ]"
            style="margin-left:auto"
          ></i>
        </router-link>

        <!-- Submenu -->
        <transition name="fade-slide">
          <div
            v-if="item.children && openSubmenus.includes(item.path) && !collapsed"
            class="submenu"
          >
            <router-link
              v-for="child in item.children"
              :key="child.path"
              :to="child.path"
              class="nav-link submenu-link"
              :class="{ active: isActive(child.path) }"
            >
              <span>{{ child.title }}</span>
            </router-link>
          </div>
        </transition>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref } from "vue";
import { useRoute } from "vue-router";
import { menuItems } from "../config/menu.js";
import '../assets/styles/sidebar.css'; // CSS atualizado

const route = useRoute();
const collapsed = ref(false);
const openSubmenus = ref([]);

const toggleCollapse = () => collapsed.value = !collapsed.value;

const isActive = (path) => {
  return route.path === path || route.path.startsWith(path + "/");
};

const toggleSubmenu = (item) => {
  if (!item.children) return;
  const index = openSubmenus.value.indexOf(item.path);
  if (index > -1) {
    openSubmenus.value.splice(index, 1);
  } else {
    openSubmenus.value.push(item.path);
  }
};
</script>
