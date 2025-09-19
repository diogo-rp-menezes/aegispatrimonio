<!-- src/views/Dashboard.vue -->
<script setup>
import { ref, onMounted } from "vue";
import { listarAtivos } from "../services/api";
import { useRouter } from "vue-router";

const ativos = ref([]);
const loading = ref(false);
const error = ref(null);

const router = useRouter();

async function carregarAtivos() {
  loading.value = true;
  error.value = null;
  try {
    const data = await listarAtivos({ page: 0, size: 20, sort: "id,asc" });
    // Se sua API retorna objeto paginado (content, totalPages, etc.)
    ativos.value = data.content ? data.content : data;
  } catch (err) {
    console.error(err);
    error.value = "Erro ao carregar ativos.";
  } finally {
    loading.value = false;
  }
}

function abrirDetalhe(id) {
  router.push(`/detalhe/${id}`);
}

onMounted(() => {
  carregarAtivos();
});
</script>

<template>
  <div>
    <h2 class="mb-4">Dashboard - Ativos</h2>

    <div v-if="loading" class="alert alert-info">Carregando ativos...</div>
    <div v-if="error" class="alert alert-danger">{{ error }}</div>

    <table v-if="!loading && ativos.length > 0" class="table table-striped table-hover">
      <thead>
        <tr>
          <th>ID</th>
          <th>Nome</th>
          <th>Número Patrimônio</th>
          <th>Ações</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="ativo in ativos" :key="ativo.id">
          <td>{{ ativo.id }}</td>
          <td>{{ ativo.nome }}</td>
          <td>{{ ativo.numeroPatrimonio }}</td>
          <td>
            <button
              class="btn btn-sm btn-primary"
              @click="abrirDetalhe(ativo.id)"
            >
              <i class="bi bi-search"></i> Detalhes
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="!loading && ativos.length === 0" class="alert alert-warning">
      Nenhum ativo encontrado.
    </div>
  </div>
</template>
