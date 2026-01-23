<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { fornecedorService } from "../../services";

const router = useRouter();
const fornecedores = ref([]);
const loading = ref(false);
const error = ref(null);

async function carregarFornecedores() {
  loading.value = true;
  error.value = null;
  try {
    const data = await fornecedorService.listar();
    fornecedores.value = data || [];
  } catch (err) {
    console.error('Erro ao carregar fornecedores:', err);
    error.value = err.message || "Erro ao carregar fornecedores";
  } finally {
    loading.value = false;
  }
}

function novoFornecedor() {
  router.push("/fornecedores/novo");
}

function editarFornecedor(id) {
  router.push(`/fornecedores/${id}/editar`);
}

async function excluirFornecedor(id) {
  if (confirm("Tem certeza que deseja excluir este fornecedor?")) {
    try {
      await fornecedorService.deletar(id);
      await carregarFornecedores();
    } catch (err) {
      console.error('Erro ao excluir fornecedor:', err);
      alert(err.message || "Erro ao excluir fornecedor");
    }
  }
}

function getStatusBadge(status) {
  return status === 'ATIVO' ? 'bg-success' : 'bg-danger';
}

onMounted(() => {
  carregarFornecedores();
});
</script>

<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2>Gestão de Fornecedores</h2>
      <button class="btn btn-primary" @click="novoFornecedor">
        <i class="bi bi-plus-lg"></i> Novo Fornecedor
      </button>
    </div>

    <div v-if="loading" class="alert alert-info">
      <i class="bi bi-arrow-repeat spin"></i> Carregando fornecedores...
    </div>

    <div v-if="error" class="alert alert-danger">
      <i class="bi bi-exclamation-triangle"></i> {{ error }}
    </div>

    <div class="card shadow-sm">
      <div class="card-body">
        <div class="table-responsive">
          <table class="table table-hover align-middle">
            <thead class="table-light">
              <tr>
                <th>Nome</th>
                <th>CNPJ</th>
                <th>Contato</th>
                <th>Email</th>
                <th>Telefone</th>
                <th>Status</th>
                <th class="text-end">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="fornecedor in fornecedores" :key="fornecedor.id">
                <td class="fw-bold">{{ fornecedor.nome }}</td>
                <td>{{ fornecedor.cnpj }}</td>
                <td>{{ fornecedor.nomeContatoPrincipal || '-' }}</td>
                <td>{{ fornecedor.emailPrincipal || '-' }}</td>
                <td>{{ fornecedor.telefonePrincipal || '-' }}</td>
                <td>
                  <span class="badge" :class="getStatusBadge(fornecedor.status)">
                    {{ fornecedor.status }}
                  </span>
                </td>
                <td class="text-end">
                  <div class="btn-group">
                    <button class="btn btn-sm btn-outline-primary" @click="editarFornecedor(fornecedor.id)" title="Editar">
                      <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" @click="excluirFornecedor(fornecedor.id)" title="Excluir">
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="!loading && fornecedores.length === 0" class="text-center py-5 text-muted">
          <i class="bi bi-inbox display-1"></i>
          <p class="mt-3">Nenhum fornecedor cadastrado.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
