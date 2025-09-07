<template>
  <MainLayout>
    <div class="dashboard-view">
      <div class="page-header">
        <h1 class="page-title">
          <i class="bi bi-building me-2"></i>
          Gestão Patrimonial Aegis
        </h1>
        <p class="page-subtitle">Controle centralizado de todos os ativos da instituição</p>
      </div>

      <!-- Stats Overview -->
      <div class="stats-grid">
        <StatCard 
          title="Total de Itens"
          value="428"
          icon="bi-box-seam"
          trend="positive"
          trend-value="12% desde o último mês"
          color="secondary"
        />
        
        <StatCard 
          title="Localizações"
          value="18"
          icon="bi-pin-map"
          trend="positive"
          trend-value="2 novas unidades"
          color="success"
        />
        
        <StatCard 
          title="Em Manutenção"
          value="14"
          icon="bi-tools"
          trend="negative"
          trend-value="3% acima da média"
          color="accent"
        />
        
        <StatCard 
          title="Alertas"
          value="5"
          icon="bi-exclamation-triangle"
          trend="positive"
          trend-value="60% resolvidos"
          color="danger"
        />
      </div>
    </div>
  </MainLayout>
</template>

<script setup lang="ts">
import MainLayout from '@/components/layout/MainLayout.vue'
import StatCard from '@/components/dashboard/StatCard.vue'
</script>

<style scoped>
.dashboard-view {
  padding: 1rem;
}

.page-header {
  margin-bottom: 2rem;
}

.page-title {
  color: var(--aegis-primary);
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.page-subtitle {
  color: #6c757d;
  font-size: 1.1rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}
</style>