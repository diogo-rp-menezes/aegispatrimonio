<script setup>
import { ref, onMounted } from "vue";
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api/v1';

const stats = ref([
  { title: "Total de Ativos", value: "...", icon: "bi-box-seam", color: "primary" },
  { title: "Em Manutenção", value: "...", icon: "bi-tools", color: "warning" },
  { title: "Valor Total", value: "...", icon: "bi-cash-stack", color: "success" },
  { title: "Locais", value: "...", icon: "bi-building", color: "info" },
]);

const quickActions = ref([
  { title: "Cadastrar Ativo", icon: "bi-plus-lg", color: "primary", action: () => router.push('/ativos/novo') },
  { title: "Ver Ativos", icon: "bi-list-ul", color: "secondary", action: () => router.push('/ativos') },
  // { title: "Relatórios", icon: "bi-file-earmark-text", color: "info", action: () => {} }, // Future
]);

const tableData = ref([]);
const loading = ref(true);

const currencyFormatter = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
function formatCurrency(v) {
  if (v == null) return 'R$ 0,00';
  const n = typeof v === 'string' ? Number(v) : v;
  return isNaN(n) ? v : currencyFormatter.format(n);
}

function getStatusBadge(status) {
  const statusMap = {
    'ATIVO': 'success',
    'INATIVO': 'warning',
    'BAIXADO': 'danger',
    'EM_MANUTENCAO': 'warning text-dark'
  };
  return statusMap[status] || 'secondary';
}

async function fetchStats() {
  try {
    const { data } = await axios.get(`${API_BASE}/dashboard/stats`);
    stats.value = [
      { title: "Total de Ativos", value: data.totalAtivos, icon: "bi-box-seam", color: "primary" },
      { title: "Em Manutenção", value: data.ativosEmManutencao, icon: "bi-tools", color: "warning" },
      { title: "Valor Total", value: formatCurrency(data.valorTotal), icon: "bi-cash-stack", color: "success" },
      { title: "Locais", value: data.totalLocalizacoes, icon: "bi-building", color: "info" },
    ];
  } catch (error) {
    console.error("Erro ao carregar estatísticas:", error);
  }
}

async function fetchRecentAssets() {
  try {
    const { data } = await axios.get(`${API_BASE}/ativos?size=5&sort=id,desc`);
    // API returns Page<AtivoDTO>
    tableData.value = data.content.map(item => ({
      id: item.id,
      nome: item.nome,
      categoria: item.tipoAtivoNome || 'N/A',
      status: item.status
    }));
  } catch (error) {
    console.error("Erro ao carregar ativos recentes:", error);
  }
}

onMounted(async () => {
  loading.value = true;
  await Promise.all([fetchStats(), fetchRecentAssets()]);
  loading.value = false;
});
</script>

<template>
  <div class="dashboard">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="fw-bold">Dashboard</h2>
      <!--
      <button class="btn btn-outline-primary">
        <i class="bi bi-gear"></i> Configurações
      </button>
      -->
    </div>

    <!-- Cards de estatísticas -->
    <div class="row g-3 mb-4">
      <div v-for="stat in stats" :key="stat.title" class="col-md-3">
        <div class="card h-100 border-0 shadow-sm">
          <div class="card-body d-flex align-items-center">
            <div
              :class="`bg-${stat.color} bg-opacity-10 text-${stat.color} rounded-circle d-flex align-items-center justify-content-center me-3`"
              style="width: 55px; height: 55px;"
            >
              <i :class="`bi ${stat.icon} fs-4`"></i>
            </div>
            <div>
              <h6 class="mb-0 text-muted">{{ stat.title }}</h6>
              <h4 class="fw-bold mb-0">{{ stat.value }}</h4>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick actions -->
    <div class="row g-3 mb-4">
      <div v-for="action in quickActions" :key="action.title" class="col-md-4">
        <div class="card text-center p-4 border-0 shadow-sm action-card" @click="action.action" style="cursor: pointer;">
          <i :class="`bi ${action.icon} text-${action.color} fs-1 mb-2`"></i>
          <h6 class="fw-bold">{{ action.title }}</h6>
          <!-- <button class="btn btn-sm btn-outline-primary mt-2">Acessar</button> -->
        </div>
      </div>
    </div>

    <!-- Tabela resumo -->
    <div class="card border-0 shadow-sm">
      <div class="card-body">
        <h5 class="fw-bold mb-3">Últimos Ativos Cadastrados</h5>
        <div class="table-responsive">
          <table class="table table-hover align-middle">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nome</th>
                <th>Categoria</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="tableData.length === 0">
                <td colspan="4" class="text-center py-3 text-muted">Nenhum ativo recente.</td>
              </tr>
              <tr v-for="item in tableData" :key="item.id">
                <td>{{ item.id }}</td>
                <td>{{ item.nome }}</td>
                <td>{{ item.categoria }}</td>
                <td>
                  <span :class="`badge bg-${getStatusBadge(item.status)}`">
                    {{ item.status }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard h2 {
  font-size: 1.75rem;
}
.action-card {
  transition: transform 0.2s, box-shadow 0.2s;
}
.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 .5rem 1rem rgba(0,0,0,.15)!important;
}
</style>
