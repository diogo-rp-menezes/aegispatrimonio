<template>
  <div class="d-flex">
    <Sidebar />
    <div class="flex-grow-1">
      <TopBar />
      <main class="p-4">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import Sidebar from "./components/Sidebar.vue";
import TopBar from "./components/TopBar.vue";
</script>
