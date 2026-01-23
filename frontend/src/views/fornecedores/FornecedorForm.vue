<script setup>
import { ref, onMounted, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { fornecedorService } from "../../services";

const router = useRouter();
const route = useRoute();
const isEditMode = computed(() => !!route.params.id);

const form = ref({
  nome: "",
  cnpj: "",
  endereco: "",
  nomeContatoPrincipal: "",
  emailPrincipal: "",
  telefonePrincipal: "",
  observacoes: "",
  status: "ATIVO"
});

const loading = ref(false);
const error = ref(null);
const saving = ref(false);

async function carregarFornecedor(id) {
  loading.value = true;
  try {
    const data = await fornecedorService.buscarPorId(id);
    form.value = { ...data };
  } catch (err) {
    console.error('Erro ao carregar fornecedor:', err);
    error.value = "Erro ao carregar dados do fornecedor.";
  } finally {
    loading.value = false;
  }
}

async function salvar() {
  saving.value = true;
  error.value = null;

  try {
    if (isEditMode.value) {
      await fornecedorService.atualizar(route.params.id, form.value);
    } else {
      await fornecedorService.criar(form.value);
    }
    router.push("/fornecedores");
  } catch (err) {
    console.error('Erro ao salvar fornecedor:', err);
    error.value = err.message || "Erro ao salvar fornecedor.";
  } finally {
    saving.value = false;
  }
}

function cancelar() {
  router.back();
}

onMounted(() => {
  if (isEditMode.value) {
    carregarFornecedor(route.params.id);
  }
});
</script>

<template>
  <div class="container-fluid p-0">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2>{{ isEditMode ? 'Editar Fornecedor' : 'Novo Fornecedor' }}</h2>
    </div>

    <div v-if="error" class="alert alert-danger">
      {{ error }}
    </div>

    <div class="card shadow-sm">
      <div class="card-body">
        <form @submit.prevent="salvar">
          <div class="row g-3">
            <!-- Nome -->
            <div class="col-md-6">
              <label for="nome" class="form-label">Nome / Razão Social *</label>
              <input
                type="text"
                class="form-control"
                id="nome"
                v-model="form.nome"
                required
                maxlength="255"
              >
            </div>

            <!-- CNPJ -->
            <div class="col-md-6">
              <label for="cnpj" class="form-label">CNPJ *</label>
              <input
                type="text"
                class="form-control"
                id="cnpj"
                v-model="form.cnpj"
                required
                placeholder="00.000.000/0000-00"
              >
              <div class="form-text">Apenas números ou formato padrão.</div>
            </div>

            <!-- Endereço -->
            <div class="col-12">
              <label for="endereco" class="form-label">Endereço</label>
              <input
                type="text"
                class="form-control"
                id="endereco"
                v-model="form.endereco"
                maxlength="255"
              >
            </div>

            <!-- Contato Principal -->
            <div class="col-md-4">
              <label for="contato" class="form-label">Contato Principal</label>
              <input
                type="text"
                class="form-control"
                id="contato"
                v-model="form.nomeContatoPrincipal"
                maxlength="255"
              >
            </div>

            <!-- Email -->
            <div class="col-md-4">
              <label for="email" class="form-label">Email Principal</label>
              <input
                type="email"
                class="form-control"
                id="email"
                v-model="form.emailPrincipal"
                maxlength="255"
              >
            </div>

            <!-- Telefone -->
            <div class="col-md-4">
              <label for="telefone" class="form-label">Telefone Principal</label>
              <input
                type="text"
                class="form-control"
                id="telefone"
                v-model="form.telefonePrincipal"
                maxlength="50"
              >
            </div>

            <!-- Status (apenas edição) -->
            <div class="col-md-4" v-if="isEditMode">
              <label for="status" class="form-label">Status *</label>
              <select class="form-select" id="status" v-model="form.status" required>
                <option value="ATIVO">Ativo</option>
                <option value="INATIVO">Inativo</option>
              </select>
            </div>

            <!-- Observações -->
            <div class="col-12">
              <label for="obs" class="form-label">Observações</label>
              <textarea
                class="form-control"
                id="obs"
                v-model="form.observacoes"
                rows="3"
              ></textarea>
            </div>
          </div>

          <div class="d-flex justify-content-end gap-2 mt-4">
            <button type="button" class="btn btn-secondary" @click="cancelar" :disabled="saving">
              Cancelar
            </button>
            <button type="submit" class="btn btn-primary" :disabled="saving">
              <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
              {{ saving ? 'Salvando...' : 'Salvar' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
