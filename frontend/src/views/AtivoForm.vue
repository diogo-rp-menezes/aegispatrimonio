<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { buscarAtivoPorId, criarAtivo, atualizarAtivo } from "../services/api";

const route = useRoute();
const router = useRouter();

const ativo = ref({
  nome: "",
  numeroPatrimonio: "",
  descricao: "",
  dataAquisicao: "",
  valor: "",
});

const loading = ref(false);
const error = ref(null);
const sucesso = ref(null);

const isEdicao = ref(false);

onMounted(async () => {
  if (route.params.id) {
    isEdicao.value = true;
    loading.value = true;
    try {
      const data = await buscarAtivoPorId(route.params.id);
      ativo.value = {
        nome: data.nome || "",
        numeroPatrimonio: data.numeroPatrimonio || "",
        descricao: data.descricao || "",
        dataAquisicao: data.dataAquisicao || "",
        valor: data.valor || "",
      };
    } catch (err) {
      console.error(err);
      error.value = "Erro ao carregar ativo.";
    } finally {
      loading.value = false;
    }
  }
});

async function salvar() {
  loading.value = true;
  error.value = null;
  sucesso.value = null;
  try {
    if (isEdicao.value) {
      await atualizarAtivo(route.params.id, ativo.value);
      sucesso.value = "Ativo atualizado com sucesso!";
    } else {
      await criarAtivo(ativo.value);
      sucesso.value = "Ativo criado com sucesso!";
    }
    setTimeout(() => router.push("/dashboard"), 1500);
  } catch (err) {
    console.error(err);
    error.value = "Erro ao salvar ativo.";
  } finally {
    loading.value = false;
  }
}

function cancelar() {
  router.push("/dashboard");
}
</script>

<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2>{{ isEdicao ? "Editar Ativo" : "Novo Ativo" }}</h2>
      <button class="btn btn-secondary" @click="cancelar">
        <i class="bi bi-arrow-left"></i> Voltar
      </button>
    </div>

    <div v-if="loading" class="alert alert-info">Carregando...</div>
    <div v-if="error" class="alert alert-danger">{{ error }}</div>
    <div v-if="sucesso" class="alert alert-success">{{ sucesso }}</div>

    <form @submit.prevent="salvar" class="card shadow-sm p-4">
      <div class="mb-3">
        <label class="form-label">Nome</label>
        <input v-model="ativo.nome" type="text" class="form-control" required />
      </div>

      <div class="mb-3">
        <label class="form-label">Número de Patrimônio</label>
        <input
          v-model="ativo.numeroPatrimonio"
          type="text"
          class="form-control"
          required
        />
      </div>

      <div class="mb-3">
        <label class="form-label">Descrição</label>
        <textarea v-model="ativo.descricao" class="form-control"></textarea>
      </div>

      <div class="mb-3">
        <label class="form-label">Data de Aquisição</label>
        <input
          v-model="ativo.dataAquisicao"
          type="date"
          class="form-control"
        />
      </div>

      <div class="mb-3">
        <label class="form-label">Valor</label>
        <input v-model="ativo.valor" type="number" step="0.01" class="form-control" />
      </div>

      <button type="submit" class="btn btn-primary me-2">
        <i class="bi bi-save"></i> Salvar
      </button>
      <button type="button" class="btn btn-secondary" @click="cancelar">
        Cancelar
      </button>
    </form>
  </div>
</template>
