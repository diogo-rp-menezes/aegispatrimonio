<template>
  <div class="app-container">
    <Sidebar />
    <div class="main-content">
      <TopBar />
      <main class="content-area">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import Sidebar from './Sidebar.vue'
import TopBar from './TopBar.vue'
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
}

.main-content {
  margin-left: 280px;
  flex: 1;
  background: var(--aegis-light);
  min-height: 100vh;
}

.content-area {
  padding: 2rem;
}

@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
  }
}
</style>