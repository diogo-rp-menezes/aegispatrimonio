<!-- src/views/DetailView.vue -->
<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { request } from "../services/api";

const route = useRoute();
const router = useRouter();

const ativo = ref(null);
const loading = ref(false);
const error = ref(null);

const historico = ref([]);
const loadingHistorico = ref(false);

async function carregarAtivo() {
  loading.value = true;
  error.value = null;
  try {
    ativo.value = await request(`/ativos/${route.params.id}`);
  } catch (err) {
    console.error(err);
    error.value = "Erro ao carregar ativo.";
  } finally {
    loading.value = false;
  }
}

async function carregarHistorico() {
  loadingHistorico.value = true;
  try {
    const data = await request(`/audit/ativos/${route.params.id}`);
    historico.value = data || [];
  } catch (err) {
    console.error("Erro ao carregar histórico", err);
  } finally {
    loadingHistorico.value = false;
  }
}

function voltar() {
  router.push("/ativos");
}

function editar() {
  if (ativo.value && ativo.value.id) {
    router.push(`/ativos/${ativo.value.id}/editar`);
  }
}

function formatDate(dateStr) {
  if (!dateStr) return "-";
  return new Date(dateStr).toLocaleString("pt-BR");
}

function formatCurrency(val) {
  if (val == null) return "-";
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  }).format(val);
}

function getDaysRemaining(dateStr) {
  if (!dateStr) return null;
  const target = new Date(dateStr);
  if (isNaN(target.getTime())) return null;
  const today = new Date();
  const diffTime = target - today;
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
}

function getPredictionClass(dateStr) {
  const days = getDaysRemaining(dateStr);
  if (days === null) return "alert-secondary";
  if (days < 7) return "alert-danger";
  if (days < 30) return "alert-warning";
  return "alert-success";
}

function getPredictionText(dateStr) {
  const days = getDaysRemaining(dateStr);
  if (days === null) return "";
  if (days < 0) return `Esgotamento estimado ocorreu há ${Math.abs(days)} dias.`;
  return `Esgotamento de disco estimado em ${days} dias (${formatDate(dateStr)}).`;
}

function translateType(type) {
  const map = {
    ADD: "Criação",
    MOD: "Edição",
    DEL: "Exclusão",
  };
  return map[type] || type;
}

function getBadgeClass(type) {
  const map = {
    ADD: "badge bg-success",
    MOD: "badge bg-warning text-dark",
    DEL: "badge bg-danger",
  };
  return map[type] || "badge bg-secondary";
}

onMounted(() => {
  carregarAtivo();
  carregarHistorico();
});
</script>

<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2>Detalhes do Ativo</h2>
      <div>
        <button class="btn btn-primary me-2" @click="editar" v-if="ativo">
          <i class="bi bi-pencil"></i> Editar
        </button>
        <button class="btn btn-secondary" @click="voltar">
          <i class="bi bi-arrow-left"></i> Voltar
        </button>
      </div>
    </div>

    <div v-if="loading" class="alert alert-info">Carregando ativo...</div>
    <div v-if="error" class="alert alert-danger">{{ error }}</div>

    <div v-if="ativo" class="card shadow-sm mb-4">
      <div class="card-body">
        <h5 class="card-title">{{ ativo.nome }}</h5>
        <h6 class="card-subtitle mb-2 text-muted">
          Patrimônio Nº {{ ativo.numeroPatrimonio }}
        </h6>
        <p class="card-text">
          <strong>ID:</strong> {{ ativo.id }} <br />
          <strong>Descrição:</strong> {{ ativo.descricao || "Não informado" }} <br />
          <strong>Data de Aquisição:</strong>
          {{ ativo.dataAquisicao || "Não informado" }} <br />
          <strong>Valor de Aquisição:</strong>
          {{ formatCurrency(ativo.valorAquisicao) }}
        </p>
      </div>
    </div>

    <!-- Predictive Analysis Alert -->
    <div v-if="ativo && ativo.previsaoEsgotamentoDisco" :class="['alert', 'd-flex', 'align-items-center', 'mb-4', getPredictionClass(ativo.previsaoEsgotamentoDisco)]" role="alert">
      <i class="bi bi-graph-down-arrow fs-2 me-3"></i>
      <div>
        <h5 class="alert-heading fw-bold mb-1">Análise Preditiva</h5>
        <p class="mb-0">{{ getPredictionText(ativo.previsaoEsgotamentoDisco) }}</p>
      </div>
    </div>

    <!-- Hardware Details -->
    <div v-if="ativo && ativo.detalheHardware" class="card shadow-sm mb-4">
      <div class="card-header bg-light">
        <h5 class="mb-0"><i class="bi bi-cpu me-2"></i>Detalhes de Hardware</h5>
      </div>
      <div class="card-body">
        <div class="row">
          <div class="col-md-6 mb-2">
            <strong>Hostname:</strong> {{ ativo.detalheHardware.computerName || '-' }}
          </div>
          <div class="col-md-6 mb-2">
            <strong>Sistema Operacional:</strong> {{ ativo.detalheHardware.osName }} {{ ativo.detalheHardware.osVersion }}
          </div>
          <div class="col-md-6 mb-2">
            <strong>Processador:</strong> {{ ativo.detalheHardware.cpuModel }} ({{ ativo.detalheHardware.cpuCores }} cores)
          </div>
           <div class="col-md-6 mb-2">
            <strong>Placa Mãe:</strong> {{ ativo.detalheHardware.motherboardManufacturer }} {{ ativo.detalheHardware.motherboardModel }}
          </div>
        </div>
      </div>
    </div>

    <!-- Technical Attributes (Adaptive Taxonomy) -->
    <div v-if="ativo && ativo.atributos && Object.keys(ativo.atributos).length > 0" class="card shadow-sm mb-4">
      <div class="card-header bg-light">
        <h5 class="mb-0"><i class="bi bi-tags me-2"></i>Especificações Técnicas</h5>
      </div>
      <div class="card-body">
        <ul class="list-group list-group-flush">
          <li v-for="(val, key) in ativo.atributos" :key="key" class="list-group-item d-flex justify-content-between align-items-center">
            <span class="fw-bold">{{ key }}</span>
            <span>{{ val }}</span>
          </li>
        </ul>
      </div>
    </div>

    <!-- Histórico de Auditoria -->
    <div v-if="ativo" class="card shadow-sm">
      <div class="card-header bg-light">
        <h5 class="mb-0">
          <i class="bi bi-clock-history me-2"></i>Histórico de Alterações
        </h5>
      </div>
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover mb-0 align-middle">
            <thead class="table-light">
              <tr>
                <th>Data/Hora</th>
                <th>Usuário</th>
                <th>Ação</th>
                <th>Snapshot (Valor)</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loadingHistorico">
                <td colspan="4" class="text-center py-4">
                  <div class="spinner-border spinner-border-sm me-2"></div>
                  Carregando histórico...
                </td>
              </tr>
              <tr v-else-if="historico.length === 0">
                <td colspan="4" class="text-center py-4 text-muted">
                  Nenhum registro de auditoria encontrado.
                </td>
              </tr>
              <tr v-for="item in historico" :key="item.revision.id">
                <td>
                  {{ formatDate(item.revision.revisionDate) }}
                </td>
                <td>
                  {{ item.revision.username || "Sistema" }}
                </td>
                <td>
                  <span :class="getBadgeClass(item.revision.revisionType)">
                    {{ translateType(item.revision.revisionType) }}
                  </span>
                </td>
                <td>
                  <small class="text-muted">
                    {{ formatCurrency(item.entity.valorAquisicao) }}
                  </small>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div v-if="!loading && !ativo && !error" class="alert alert-warning">
      Nenhum ativo encontrado.
    </div>
  </div>
</template>

<style scoped>
.badge {
  font-weight: 500;
}
</style>
