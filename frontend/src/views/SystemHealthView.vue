<template>
  <div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="fw-bold">Saúde do Sistema</h2>
      <button class="btn btn-primary" @click="fetchData" :disabled="loading">
        <i class="bi bi-arrow-clockwise" :class="{ 'spin': loading }"></i> Atualizar
      </button>
    </div>

    <div v-if="error" class="alert alert-danger">{{ error }}</div>

    <div v-if="loading && !stats" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Carregando...</span>
      </div>
    </div>

    <div v-else-if="stats" class="row g-4">
      <!-- Host Info -->
      <div class="col-12">
        <div class="card border-0 shadow-sm">
          <div class="card-body">
            <h5 class="card-title fw-bold"><i class="bi bi-server me-2"></i>Informações do Host</h5>
            <p class="mb-0"><strong>Hostname:</strong> {{ stats.host }}</p>
            <p class="mb-0 text-muted small">Última atualização: {{ formatDate(stats.createdAt) }}</p>
          </div>
        </div>
      </div>

      <!-- Metrics Cards -->
      <div class="col-md-4">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body text-center">
            <h5 class="card-title text-muted">Uso de CPU</h5>
            <h2 class="display-4 fw-bold" :class="getCpuColor(stats.cpuUsage)">
              {{ formatPercent(stats.cpuUsage) }}
            </h2>
            <div class="progress mt-3" style="height: 10px;">
              <div class="progress-bar" :class="getCpuBg(stats.cpuUsage)" role="progressbar"
                   :style="{ width: `${stats.cpuUsage * 100}%` }"></div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body text-center">
            <h5 class="card-title text-muted">Memória Livre</h5>
            <h2 class="display-4 fw-bold" :class="getMemColor(stats.memFreePercent)">
              {{ formatPercent(stats.memFreePercent) }}
            </h2>
            <div class="progress mt-3" style="height: 10px;">
              <div class="progress-bar" :class="getMemBg(stats.memFreePercent)" role="progressbar"
                   :style="{ width: `${stats.memFreePercent * 100}%` }"></div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <h5 class="card-title text-muted text-center mb-3">Discos</h5>
            <ul class="list-group list-group-flush">
                <li v-for="(disk, index) in parseList(stats.disks)" :key="index" class="list-group-item px-0">
                    <small>{{ disk }}</small>
                </li>
            </ul>
          </div>
        </div>
      </div>

       <div class="col-12">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <h5 class="card-title text-muted text-center mb-3">Rede</h5>
            <ul class="list-group list-group-flush">
                <li v-for="(net, index) in parseList(stats.nets)" :key="index" class="list-group-item px-0">
                    <small>{{ net }}</small>
                </li>
            </ul>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { request } from '../services/api';

const stats = ref(null);
const loading = ref(false);
const error = ref(null);

const fetchData = async () => {
  loading.value = true;
  error.value = null;
  try {
    const data = await request('/health-check/system/last');
    stats.value = data;
  } catch (err) {
    console.error(err);
    error.value = 'Erro ao carregar dados do sistema. Verifique se você tem permissão.';
  } finally {
    loading.value = false;
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleString('pt-BR');
};

const formatPercent = (val) => {
  if (val == null) return '-';
  return (val * 100).toFixed(1) + '%';
};

const parseList = (str) => {
    if (!str) return [];
    return str.split(';').filter(s => s.trim().length > 0);
}

// Colors helpers
const getCpuColor = (val) => val > 0.8 ? 'text-danger' : (val > 0.5 ? 'text-warning' : 'text-success');
const getCpuBg = (val) => val > 0.8 ? 'bg-danger' : (val > 0.5 ? 'bg-warning' : 'bg-success');

const getMemColor = (val) => val < 0.1 ? 'text-danger' : (val < 0.2 ? 'text-warning' : 'text-success');
const getMemBg = (val) => val < 0.1 ? 'bg-danger' : (val < 0.2 ? 'bg-warning' : 'bg-success');

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.spin {
  animation: spin 1s linear infinite;
}
@keyframes spin { 100% { transform: rotate(360deg); } }
</style>
