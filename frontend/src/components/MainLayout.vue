<template>
  <div class="app-container">
    <!-- Overlay para mobile -->
    <div v-if="isMobile && sidebarMobileOpen" 
         class="sidebar-overlay" 
         :class="{ active: sidebarMobileOpen }"
         @click="closeSidebarMobile"></div>
    
    <!-- Sidebar -->
    <Sidebar 
      @toggle="onSidebarToggle" 
      ref="sidebar" 
      :class="{ 'mobile-open': sidebarMobileOpen && isMobile }"
      :is-mobile="isMobile" />
    
    <!-- Conteúdo principal -->
    <div :class="['main-content', { 'collapsed': sidebarCollapsed }]" 
         :style="{ marginLeft: !isMobile ? sidebarWidth + 'px' : '0' }">
      
      <!-- TopBar -->
      <TopBar 
        :collapsed="sidebarCollapsed" 
        :is-mobile="isMobile"
        @toggle-sidebar="toggleSidebarMobile" />
      
      <!-- Área principal -->
      <main class="main-container">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import Sidebar from "./Sidebar.vue";
import TopBar from "./TopBar.vue";

const sidebarCollapsed = ref(false);
const sidebarMobileOpen = ref(false);
const sidebarWidth = ref(280);
const isMobile = ref(false);

// Verificar se é mobile
const checkMobile = () => {
  isMobile.value = window.innerWidth < 768;
  if (isMobile.value) {
    sidebarMobileOpen.value = false;
  }
};

// Toggle sidebar no mobile
const toggleSidebarMobile = () => {
  sidebarMobileOpen.value = !sidebarMobileOpen.value;
};

// Fechar sidebar no mobile
const closeSidebarMobile = () => {
  if (isMobile.value) {
    sidebarMobileOpen.value = false;
  }
};

// Toggle normal da sidebar
const onSidebarToggle = (collapsed) => {
  sidebarCollapsed.value = collapsed;
  sidebarWidth.value = collapsed ? 70 : 280;
};

// Event listeners
onMounted(() => {
  checkMobile();
  window.addEventListener('resize', checkMobile);
});

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile);
});
</script>