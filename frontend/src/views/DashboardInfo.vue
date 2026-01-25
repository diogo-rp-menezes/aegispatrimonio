<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from 'vue-router';
import { request } from '../services/api';
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
  ArcElement
} from 'chart.js';
import { Bar, Doughnut } from 'vue-chartjs';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, ArcElement);

const router = useRouter();

const stats = ref([
  { title: "Total de Ativos", value: "...", icon: "bi-box-seam", color: "primary" },
  { title: "Em Manutenção", value: "...", icon: "bi-tools", color: "warning" },
  { title: "Valor Total", value: "...", icon: "bi-cash-stack", color: "success" },
  { title: "Locais", value: "...", icon: "bi-building", color: "info" },
]);

const predictiveStats = ref([
  { title: "Críticos (< 7 dias)", value: "...", icon: "bi-exclamation-triangle-fill", color: "danger", description: "Risco iminente de falha de disco" },
  { title: "Em Alerta (30 dias)", value: "...", icon: "bi-exclamation-circle", color: "warning", description: "Requer atenção no próximo mês" },
  { title: "Saudáveis", value: "...", icon: "bi-check-circle", color: "success", description: "Sem previsão de falha próxima" },
]);

const quickActions = ref([
  { title: "Cadastrar Ativo", icon: "bi-plus-lg", color: "primary", action: () => router.push('/ativos/novo') },
  { title: "Ver Ativos", icon: "bi-list-ul", color: "secondary", action: () => router.push('/ativos') },
  // { title: "Relatórios", icon: "bi-file-earmark-text", color: "info", action: () => {} }, // Future
]);

const tableData = ref([]);
const riskyAssets = ref([]);
const loading = ref(true);

const statusChartData = ref({ labels: [], datasets: [] });
const typeChartData = ref({ labels: [], datasets: [] });
const predictiveChartData = ref({ labels: [], datasets: [] });
const trendChartData = ref({ labels: [], datasets: [] });
const chartOptions = { responsive: true, maintainAspectRatio: false };

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

function getStatusColor(status) {
  const colorMap = {
    'ATIVO': '#198754', // success
    'INATIVO': '#ffc107', // warning
    'BAIXADO': '#dc3545', // danger
    'EM_MANUTENCAO': '#fd7e14' // orange
  };
  return colorMap[status] || '#6c757d'; // secondary
}

function formatDate(dateStr) {
  if (!dateStr) return "-";
  return new Date(dateStr).toLocaleDateString("pt-BR");
}

function getDaysRemaining(dateStr) {
  if (!dateStr) return 999;
  const target = new Date(dateStr);
  const today = new Date();
  const diffTime = target - today;
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
}

async function fetchStats() {
  try {
    const data = await request('/dashboard/stats');
    stats.value = [
      { title: "Total de Ativos", value: data.totalAtivos, icon: "bi-box-seam", color: "primary" },
      { title: "Em Manutenção", value: data.ativosEmManutencao, icon: "bi-tools", color: "warning" },
      { title: "Valor Total", value: formatCurrency(data.valorTotal), icon: "bi-cash-stack", color: "success" },
      { title: "Locais", value: data.totalLocalizacoes, icon: "bi-building", color: "info" },
    ];

    predictiveStats.value = [
      { title: "Críticos (< 7 dias)", value: data.predicaoCritica, icon: "bi-exclamation-triangle-fill", color: "danger", description: "Risco iminente de falha de disco", filter: "CRITICO" },
      { title: "Em Alerta (30 dias)", value: data.predicaoAlerta, icon: "bi-exclamation-circle", color: "warning", description: "Requer atenção no próximo mês", filter: "ALERTA" },
      { title: "Saudáveis", value: data.predicaoSegura, icon: "bi-check-circle", color: "success", description: "Sem previsão de falha próxima", filter: "SAUDAVEL" },
      { title: "Indeterminado", value: data.predicaoIndeterminada || 0, icon: "bi-question-circle", color: "secondary", description: "Sem dados suficientes para previsão", filter: "" },
    ];

    predictiveChartData.value = {
      labels: ['Críticos', 'Em Alerta', 'Saudáveis', 'Indeterminado'],
      datasets: [{
        backgroundColor: ['#dc3545', '#ffc107', '#198754', '#6c757d'],
        data: [data.predicaoCritica, data.predicaoAlerta, data.predicaoSegura, data.predicaoIndeterminada || 0]
      }]
    };

    if (data.failureTrend) {
      trendChartData.value = {
        labels: data.failureTrend.map(item => item.label),
        datasets: [{
          label: 'Previsão de Falhas',
          backgroundColor: '#dc3545',
          data: data.failureTrend.map(item => item.value)
        }]
      };
    }

    // Chart Data Mapping
    if (data.ativosPorStatus) {
      statusChartData.value = {
        labels: data.ativosPorStatus.map(item => item.label),
        datasets: [{
          backgroundColor: data.ativosPorStatus.map(item => getStatusColor(item.label)),
          data: data.ativosPorStatus.map(item => item.value)
        }]
      };
    }

    if (data.ativosPorTipo) {
      typeChartData.value = {
        labels: data.ativosPorTipo.map(item => item.label),
        datasets: [{
          label: 'Quantidade',
          backgroundColor: '#0d6efd',
          data: data.ativosPorTipo.map(item => item.value)
        }]
      };
    }

    if (data.riskyAssets) {
      riskyAssets.value = data.riskyAssets;
    }

  } catch (error) {
    console.error("Erro ao carregar estatísticas:", error);
  }
}

async function fetchRecentAssets() {
  try {
    const data = await request('/ativos', { params: { size: 5, sort: 'id,desc' } });
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

function goToAssets(healthFilter) {
  if (healthFilter) {
    router.push({ path: '/ativos', query: { health: healthFilter } });
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

    <!-- Analytics Charts Section -->
    <div class="row g-3 mb-4">
      <div class="col-md-6">
        <div class="card h-100 border-0 shadow-sm">
          <div class="card-body">
            <h5 class="fw-bold mb-3">Ativos por Status</h5>
            <div style="height: 300px;">
              <Doughnut v-if="statusChartData.labels.length" :data="statusChartData" :options="chartOptions" />
              <div v-else class="d-flex justify-content-center align-items-center h-100 text-muted">
                Sem dados disponíveis
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-6">
        <div class="card h-100 border-0 shadow-sm">
          <div class="card-body">
            <h5 class="fw-bold mb-3">Ativos por Tipo</h5>
            <div style="height: 300px;">
              <Bar v-if="typeChartData.labels.length" :data="typeChartData" :options="chartOptions" />
               <div v-else class="d-flex justify-content-center align-items-center h-100 text-muted">
                Sem dados disponíveis
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Predictive Maintenance Section -->
    <h5 class="fw-bold mb-3"><i class="bi bi-cpu me-2"></i>Manutenção Preditiva (IA Híbrida)</h5>
    <div class="row g-3 mb-4">
      <div class="col-md-8">
        <div class="row g-3 mb-3">
          <div v-for="stat in predictiveStats" :key="stat.title" class="col-md-12">
            <div class="card h-100 border-0 shadow-sm predictive-card" @click="goToAssets(stat.filter)">
              <div class="card-body d-flex align-items-center">
                 <div
                  :class="`bg-${stat.color} text-white rounded-circle d-flex align-items-center justify-content-center me-3 shadow-sm`"
                  style="width: 50px; height: 50px;"
                >
                  <i :class="`bi ${stat.icon} fs-5`"></i>
                </div>
                <div>
                  <h6 class="mb-1 fw-bold text-dark">{{ stat.title }}</h6>
                  <h3 class="fw-bold mb-0">{{ stat.value }} <small class="text-muted fs-6 fw-normal">ativos</small></h3>
                  <small class="text-muted">{{ stat.description }}</small>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Trend Chart -->
        <div class="card border-0 shadow-sm">
          <div class="card-body">
            <h6 class="fw-bold mb-3">Tendência de Falhas (Próximas 8 Semanas)</h6>
            <div style="height: 250px;">
              <Bar v-if="trendChartData.labels.length" :data="trendChartData" :options="chartOptions" />
              <div v-else class="d-flex justify-content-center align-items-center h-100 text-muted">
                Sem dados de tendência
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card h-100 border-0 shadow-sm">
          <div class="card-body">
            <h6 class="fw-bold mb-3 text-center">Distribuição de Risco</h6>
            <div style="height: 200px;">
              <Doughnut v-if="predictiveChartData.labels.length" :data="predictiveChartData" :options="chartOptions" />
               <div v-else class="d-flex justify-content-center align-items-center h-100 text-muted">
                Sem dados
              </div>
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

    <!-- Tabela de Riscos (Prioridade) -->
    <div v-if="riskyAssets.length > 0" class="card border-0 shadow-sm mb-4 border-start border-danger border-5">
      <div class="card-body">
        <h5 class="fw-bold mb-3 text-danger"><i class="bi bi-exclamation-triangle-fill me-2"></i>Ativos em Risco Crítico (Top 5)</h5>
        <div class="table-responsive">
          <table class="table table-hover align-middle">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nome</th>
                <th>Tipo</th>
                <th>Previsão de Esgotamento</th>
                <th>Dias Restantes</th>
                <th>Ação</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="asset in riskyAssets" :key="asset.id">
                <td>{{ asset.id }}</td>
                <td>{{ asset.nome }}</td>
                <td>{{ asset.tipoAtivo }}</td>
                <td class="fw-bold text-danger">{{ formatDate(asset.previsaoEsgotamento) }}</td>
                <td>
                  <span :class="`badge bg-${getDaysRemaining(asset.previsaoEsgotamento) < 7 ? 'danger' : 'warning'}`">
                    {{ getDaysRemaining(asset.previsaoEsgotamento) }} dias
                  </span>
                </td>
                <td>
                  <button class="btn btn-sm btn-outline-primary" @click="router.push(`/ativos/${asset.id}`)">
                    Detalhes
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
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
.action-card, .predictive-card {
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
}
.action-card:hover, .predictive-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 .5rem 1rem rgba(0,0,0,.15)!important;
}
</style>
