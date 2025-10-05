<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { pessoaService, departamentoService } from "../../services";

const route = useRoute();
const router = useRouter();

const pessoa = ref({
  nome: "",
  email: "",
  cargo: "",
  departamentoId: null,
  telefone: "",
  dataAdmissao: ""
});

const departamentos = ref([]);
const loading = ref(false);
const error = ref(null);
const sucesso = ref(null);
const isEdicao = ref(false);

// Carregar dados iniciais
async function carregarDadosIniciais() {
  try {
    await carregarDepartamentos();
    
    if (route.params.id) {
      await carregarPessoa();
    }
  } catch (err) {
    console.error('Erro ao carregar dados:', err);
    error.value = "Erro ao carregar dados do formulário";
  }
}

// Carregar departamentos
async function carregarDepartamentos() {
  try {
    const data = await departamentoService.listar({ size: 100 });
    departamentos.value = data.content || data;
  } catch (err) {
    console.error('Erro ao carregar departamentos:', err);
  }
}

// Carregar pessoa para edição
async function carregarPessoa() {
  isEdicao.value = true;
  loading.value = true;
  
  try {
    const data = await pessoaService.buscarPorId(route.params.id);
    pessoa.value = {
      nome: data.nome || "",
      email: data.email || "",
      cargo: data.cargo || "",
      departamentoId: data.departamento?.id || null,
      telefone: data.telefone || "",
      dataAdmissao: data.dataAdmissao || ""
    };
  } catch (err) {
    console.error('Erro ao carregar pessoa:', err);
    error.value = err.message || "Erro ao carregar pessoa";
  } finally {
    loading.value = false;
  }
}

// Salvar pessoa
async function salvar() {
  loading.value = true;
  error.value = null;
  sucesso.value = null;
  
  try {
    if (isEdicao.value) {
      await pessoaService.atualizar(route.params.id, pessoa.value);
      sucesso.value = "Pessoa atualizada com sucesso!";
    } else {
      await pessoaService.criar(pessoa.value);
      sucesso.value = "Pessoa criada com sucesso!";
    }
    
    setTimeout(() => router.push("/pessoas"), 1500);
  } catch (err) {
    console.error('Erro ao salvar pessoa:', err);
    error.value = err.message || "Erro ao salvar pessoa";
  } finally {
    loading.value = false;
  }
}

function cancelar() {
  router.push("/pessoas");
}

onMounted(() => {
  carregarDadosIniciais();
});
</script>

<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2>{{ isEdicao ? "Editar Pessoa" : "Nova Pessoa" }}</h2>
      <button class="btn btn-secondary" @click="cancelar">
        <i class="bi bi-arrow-left"></i> Voltar
      </button>
    </div>

    <div v-if="loading" class="alert alert-info">
      <i class="bi bi-arrow-repeat"></i> Carregando...
    </div>
    
    <div v-if="error" class="alert alert-danger">
      <i class="bi bi-exclamation-triangle"></i> {{ error }}
    </div>
    
    <div v-if="sucesso" class="alert alert-success">
      <i class="bi bi-check-circle"></i> {{ sucesso }}
    </div>

    <form @submit.prevent="salvar" class="card shadow-sm p-4">
      <div class="row">
        <div class="col-md-6 mb-3">
          <label class="form-label">Nome *</label>
          <input 
            v-model="pessoa.nome" 
            type="text" 
            class="form-control" 
            required 
            :disabled="loading"
          />
        </div>

        <div class="col-md-6 mb-3">
          <label class="form-label">Email *</label>
          <input 
            v-model="pessoa.email" 
            type="email" 
            class="form-control" 
            required 
            :disabled="loading"
          />
        </div>

        <div class="col-md-6 mb-3">
          <label class="form-label">Cargo</label>
          <input 
            v-model="pessoa.cargo" 
            type="text" 
            class="form-control" 
            :disabled="loading"
          />
        </div>

        <div class="col-md-6 mb-3">
          <label class="form-label">Telefone</label>
          <input 
            v-model="pessoa.telefone" 
            type="tel" 
            class="form-control" 
            :disabled="loading"
          />
        </div>

        <div class="col-md-6 mb-3">
          <label class="form-label">Departamento</label>
          <select 
            v-model="pessoa.departamentoId" 
            class="form-select" 
            :disabled="loading"
          >
            <option :value="null">Selecione um departamento</option>
            <option 
              v-for="depto in departamentos" 
              :key="depto.id" 
              :value="depto.id"
            >
              {{ depto.nome }}
            </option>
          </select>
        </div>

        <div class="col-md-6 mb-3">
          <label class="form-label">Data de Admissão</label>
          <input 
            v-model="pessoa.dataAdmissao" 
            type="date" 
            class="form-control" 
            :disabled="loading"
          />
        </div>
      </div>

      <div class="d-flex gap-2 mt-3">
        <button 
          type="submit" 
          class="btn btn-primary"
          :disabled="loading"
        >
          <i class="bi bi-save"></i> 
          {{ loading ? 'Salvando...' : 'Salvar' }}
        </button>
        
        <button 
          type="button" 
          class="btn btn-secondary" 
          @click="cancelar"
          :disabled="loading"
        >
          Cancelar
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.form-label {
  font-weight: 600;
  color: var(--aegis-dark);
}

.card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
</style>