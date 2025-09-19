<!-- src/views/DetailView.vue -->
<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { buscarAtivoPorId } from "../services/api";

const route = useRoute();
const router = useRouter();

const ativo = ref(null);
const loading = ref(false);
const error = ref(null);

async function carregarAtivo() {
  loading.value = true;
  error.value = null;
  try {
    ativo.value = await buscarAtivoPorId(route.params.id);
  } catch (err) {
    console.error(err);
    error.value = "Erro ao carregar ativo.";
  } finally {
    loading.value = false;
  }
}

function voltar() {
  router.push("/dashboard");
}

onMounted(() => {
  carregarAtivo();
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

    <div v-if="ativo" class="card shadow-sm">
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
          <strong>Valor:</strong> R$ {{ ativo.valor || "0,00" }}
        </p>
      </div>
    </div>

    <div v-if="!loading && !ativo" class="alert alert-warning">
      Nenhum ativo encontrado.
    </div>
  </div>
</template>
