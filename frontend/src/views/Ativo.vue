<!-- src/views/Dashboard.vue -->
<script setup>
import { ref, onMounted } from "vue";
import { listarAtivos, deletarAtivo } from "../services/api";
import { useRouter } from "vue-router";

const ativos = ref([]);
const loading = ref(false);
const error = ref(null);

const page = ref(0);
const size = ref(10);
const totalPages = ref(0);

const nomeBusca = ref("");

const router = useRouter();

async function carregarAtivos() {
  loading.value = true;
  error.value = null;
  try {
    const data = await listarAtivos({
      nome: nomeBusca.value,
      page: page.value,
      size: size.value,
      sort: "id,asc",
    });

    if (data.content) {
      ativos.value = data.content;
      totalPages.value = data.totalPages;
    } else {
      ativos.value = data;
      totalPages.value = 1;
    }
  } catch (err) {
    console.error(err);
    error.value = "Erro ao carregar ativos.";
  } finally {
    loading.value = false;
  }
}

function abrirDetalhe(id) {
  router.push(`/ativos/${id}`);
}

function editarAtivo(id) {
  router.push(`/ativos/${id}/editar`);
}

function novoAtivo() {
  router.push("/ativos/novo");
}

async function excluirAtivo(id) {
  if (confirm("Tem certeza que deseja excluir este ativo?")) {
    try {
      await deletarAtivo(id);
      carregarAtivos(); // recarrega a tabela
    } catch (err) {
      console.error(err);
      alert("Erro ao excluir ativo.");
    }
  }
}

function mudarPagina(novaPagina) {
  if (novaPagina >= 0 && novaPagina < totalPages.value) {
    page.value = novaPagina;
    carregarAtivos();
  }
}

function buscar() {
  page.value = 0;
  carregarAtivos();
}

onMounted(() => {
  carregarAtivos();
});
</script>

<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2>Ativos</h2>
      <button class="btn btn-success" @click="novoAtivo">
        <i class="bi bi-plus-lg"></i> Novo Ativo
      </button>
    </div>

    <!-- 🔍 Busca -->
    <div class="input-group mb-3" style="max-width: 400px;">
      <input
        type="text"
        class="form-control"
        placeholder="Buscar por nome..."
        v-model="nomeBusca"
        @keyup.enter="buscar"
      />
      <button class="btn btn-primary" @click="buscar">
        <i class="bi bi-search"></i> Buscar
      </button>
    </div>

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
            <button class="btn btn-sm btn-primary me-2" @click="abrirDetalhe(ativo.id)">
              <i class="bi bi-search"></i>
            </button>
            <button class="btn btn-sm btn-warning me-2" @click="editarAtivo(ativo.id)">
              <i class="bi bi-pencil"></i>
            </button>
            <button class="btn btn-sm btn-danger" @click="excluirAtivo(ativo.id)">
              <i class="bi bi-trash"></i>
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="!loading && ativos.length === 0" class="alert alert-warning">
      Nenhum ativo encontrado.
    </div>

    <!-- Paginação -->
    <nav v-if="totalPages > 1" aria-label="Navegação de página" class="mt-3">
      <ul class="pagination">
        <li class="page-item" :class="{ disabled: page === 0 }">
          <button class="page-link" @click="mudarPagina(page - 1)">Anterior</button>
        </li>

        <li
          v-for="n in totalPages"
          :key="n"
          class="page-item"
          :class="{ active: n - 1 === page }"
        >
          <button class="page-link" @click="mudarPagina(n - 1)">
            {{ n }}
          </button>
        </li>

        <li class="page-item" :class="{ disabled: page === totalPages - 1 }">
          <button class="page-link" @click="mudarPagina(page + 1)">Próxima</button>
        </li>
      </ul>
    </nav>
  </div>
</template>
