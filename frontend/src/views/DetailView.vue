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
      <button class="btn btn-secondary" @click="voltar">
        <i class="bi bi-arrow-left"></i> Voltar
      </button>
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
