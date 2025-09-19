<template>
  <div class="app-container">
    <Sidebar @toggle="onSidebarToggle" />
    <div class="main-content" :style="{ marginLeft: mainMargin + 'px' }">
      <TopBar :collapsed="sidebarCollapsed" />
      <main class="p-4">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import Sidebar from "./components/Sidebar.vue";
import TopBar from "./components/TopBar.vue";

const mainMargin = ref(280);
const sidebarCollapsed = ref(false);

const onSidebarToggle = (collapsed) => {
  sidebarCollapsed.value = collapsed;
  mainMargin.value = collapsed ? 70 : 280;
};
</script>
