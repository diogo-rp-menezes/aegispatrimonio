<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { pessoaService, departamentoService } from "../../services";

const router = useRouter();
const pessoas = ref([]);
const departamentos = ref([]);
const loading = ref(false);
const error = ref(null);

// Filtros e paginação
const filtros = ref({
  nome: "",
  departamentoId: null,
  page: 0,
  size: 10
});
const totalPages = ref(0);

// Carregar dados iniciais
async function carregarDadosIniciais() {
  try {
    await Promise.all([carregarPessoas(), carregarDepartamentos()]);
  } catch (err) {
    console.error('Erro ao carregar dados iniciais:', err);
    error.value = "Erro ao carregar dados";
  }
}

// Carregar pessoas
async function carregarPessoas() {
  loading.value = true;
  error.value = null;
  try {
    const data = await pessoaService.listar(filtros.value);
    pessoas.value = data.content || data;
    totalPages.value = data.totalPages || 1;
  } catch (err) {
    console.error('Erro ao carregar pessoas:', err);
    error.value = err.message || "Erro ao carregar pessoas";
  } finally {
    loading.value = false;
  }
}

// Carregar departamentos para o select
async function carregarDepartamentos() {
  try {
    const data = await departamentoService.listar({ size: 100 });
    departamentos.value = data.content || data;
  } catch (err) {
    console.error('Erro ao carregar departamentos:', err);
  }
}

// Navegação
function editarPessoa(id) {
  router.push(`/pessoas/${id}/editar`);
}

function verDetalhes(id) {
  router.push(`/pessoas/${id}`);
}

function novaPessoa() {
  router.push("/pessoas/novo");
}

// Excluir pessoa
async function excluirPessoa(id) {
  if (confirm("Tem certeza que deseja excluir esta pessoa?")) {
    try {
      await pessoaService.deletar(id);
      await carregarPessoas();
    } catch (err) {
      console.error('Erro ao excluir pessoa:', err);
      alert(err.message || "Erro ao excluir pessoa");
    }
  }
}

// Buscar e paginação
function buscar() {
  filtros.value.page = 0;
  carregarPessoas();
}

function mudarPagina(novaPagina) {
  if (novaPagina >= 0 && novaPagina < totalPages.value) {
    filtros.value.page = novaPagina;
    carregarPessoas();
  }
}

onMounted(() => {
  carregarDadosIniciais();
});
</script>

<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2>Gestão de Pessoas</h2>
      <button class="btn btn-success" @click="novaPessoa">
        <i class="bi bi-person-plus"></i> Nova Pessoa
      </button>
    </div>

    <!-- Filtros -->
    <div class="card mb-4">
      <div class="card-body">
        <h5 class="card-title">Filtros</h5>
        <div class="row g-3">
          <div class="col-md-5">
            <input
              v-model="filtros.nome"
              type="text"
              class="form-control"
              placeholder="Buscar por nome..."
              @keyup.enter="buscar"
            />
          </div>
          <div class="col-md-5">
            <select v-model="filtros.departamentoId" class="form-select">
              <option :value="null">Todos os departamentos</option>
              <option 
                v-for="depto in departamentos" 
                :key="depto.id" 
                :value="depto.id"
              >
                {{ depto.nome }}
              </option>
            </select>
          </div>
          <div class="col-md-2">
            <button class="btn btn-primary w-100" @click="buscar">
              <i class="bi bi-search"></i> Buscar
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="loading" class="alert alert-info">
      <i class="bi bi-arrow-repeat"></i> Carregando pessoas...
    </div>
    
    <div v-if="error" class="alert alert-danger">
      <i class="bi bi-exclamation-triangle"></i> {{ error }}
    </div>

    <!-- Tabela de Pessoas -->
    <div class="card">
      <div class="card-body">
        <div class="table-responsive">
          <table class="table table-striped table-hover">
            <thead>
              <tr>
                <th>Nome</th>
                <th>Email</th>
                <th>Departamento</th>
                <th>Cargo</th>
                <th class="text-center">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="pessoa in pessoas" :key="pessoa.id">
                <td>{{ pessoa.nome }}</td>
                <td>{{ pessoa.email }}</td>
                <td>{{ pessoa.departamento?.nome || 'N/A' }}</td>
                <td>{{ pessoa.cargo || 'N/A' }}</td>
                <td class="text-center">
                  <div class="btn-group" role="group">
                    <button class="btn btn-sm btn-info me-1" @click="verDetalhes(pessoa.id)" title="Ver detalhes">
                      <i class="bi bi-eye"></i>
                    </button>
                    <button class="btn btn-sm btn-warning me-1" @click="editarPessoa(pessoa.id)" title="Editar">
                      <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-danger" @click="excluirPessoa(pessoa.id)" title="Excluir">
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Mensagem de lista vazia -->
        <div v-if="!loading && pessoas.length === 0" class="text-center py-4 text-muted">
          <i class="bi bi-inbox"></i> Nenhuma pessoa encontrada
        </div>

        <!-- Paginação -->
        <nav v-if="totalPages > 1" class="mt-3">
          <ul class="pagination justify-content-center">
            <li class="page-item" :class="{ disabled: filtros.page === 0 }">
              <button class="page-link" @click="mudarPagina(filtros.page - 1)">
                <i class="bi bi-chevron-left"></i> Anterior
              </button>
            </li>
            
            <li 
              v-for="n in totalPages" 
              :key="n" 
              class="page-item" 
              :class="{ active: n - 1 === filtros.page }"
            >
              <button class="page-link" @click="mudarPagina(n - 1)">
                {{ n }}
              </button>
            </li>
            
            <li class="page-item" :class="{ disabled: filtros.page === totalPages - 1 }">
              <button class="page-link" @click="mudarPagina(filtros.page + 1)">
                Próxima <i class="bi bi-chevron-right"></i>
              </button>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </div>
</template>

<style scoped>
.table th {
  font-weight: 600;
  color: var(--aegis-primary);
  background-color: #f8f9fa;
}

.btn-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
}

.card-title {
  color: var(--aegis-primary);
  font-weight: 600;
}
</style>