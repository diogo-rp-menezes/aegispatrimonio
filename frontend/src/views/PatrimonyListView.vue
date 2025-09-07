// src/views/PatrimonyListView.vue
<template>
  <MainLayout>
    <div class="patrimony-list-view">
      <div class="page-header">
        <h1 class="page-title">
          <i class="bi bi-list-check me-2"></i>
          Lista de Ativos Patrimoniais
        </h1>
        <p class="page-subtitle">Gerencie todos os ativos da instituição</p>
      </div>

      <!-- Substitui o coming-soon pelo componente real -->
      <AtivoList />
    </div>
  </MainLayout>
</template>

<script setup lang="ts">
import MainLayout from '@/components/layout/MainLayout.vue';
import AtivoList from '@/components/patrimony/AtivoList.vue';
</script>

<style scoped>
.patrimony-list-view {
  padding: 20px;
}

.page-header {
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  color: var(--aegis-primary);
  margin-bottom: 0.5rem;
}

.page-subtitle {
  color: #6c757d;
  font-size: 1.1rem;
}
</style>