<template>
  <div class="app-container">
    <!-- Sidebar -->
    <Sidebar @toggle="onSidebarToggle" ref="sidebar" />

    <!-- Conteúdo principal -->
    <div :class="['main-content']" :style="{ marginLeft: sidebarWidth + 'px' }">
      <!-- TopBar -->
      <TopBar :collapsed="sidebarCollapsed" />

      <!-- Área principal onde o router renderiza as views -->
      <main class="p-4">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import Sidebar from "./Sidebar.vue";
import TopBar from "./TopBar.vue";

const sidebarCollapsed = ref(false);
const sidebarWidth = ref(280); // largura inicial expandida

const onSidebarToggle = (collapsed) => {
  sidebarCollapsed.value = collapsed;
  sidebarWidth.value = collapsed ? 70 : 280;
};
</script>

<style src="../assets/styles/mainlayout.css"></style>
