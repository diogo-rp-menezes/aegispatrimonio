<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { funcionarioService, departamentoService, filialService } from "../../services";

const route = useRoute();
const router = useRouter();

const pessoa = ref({
  nome: "",
  email: "",
  matricula: "",
  cargo: "",
  departamentoId: null,
  filiaisIds: [],
  role: "ROLE_USER",
  password: "" // Only for creation
});

const departamentos = ref([]);
const filiais = ref([]);
const loading = ref(false);
const error = ref(null);
const sucesso = ref(null);
const isEdicao = ref(false);

// Carregar dados iniciais
async function carregarDadosIniciais() {
  try {
    await Promise.all([carregarDepartamentos(), carregarFiliais()]);
    
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

// Carregar filiais
async function carregarFiliais() {
  try {
    const data = await filialService.listar();
    filiais.value = data.content || data;
  } catch (err) {
    console.error('Erro ao carregar filiais:', err);
  }
}

// Carregar pessoa para edição
async function carregarPessoa() {
  isEdicao.value = true;
  loading.value = true;
  
  try {
    const data = await funcionarioService.buscarPorId(route.params.id);
    pessoa.value = {
      nome: data.nome || "",
      email: data.email || "",
      matricula: data.matricula || "",
      cargo: data.cargo || "",
      departamentoId: data.departamentoId || null,
      filiaisIds: data.filiaisIds || [],
      role: data.role || "ROLE_USER",
      password: "" // Do not load password
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
      // Remove password from update if it's empty or not managed here (Update DTO typically doesn't include password unless intended to change)
      const payload = { ...pessoa.value };
      delete payload.password;

      await funcionarioService.atualizar(route.params.id, payload);
      sucesso.value = "Pessoa atualizada com sucesso!";
    } else {
      await funcionarioService.criar(pessoa.value);
      sucesso.value = "Pessoa criada com sucesso!";
    }
    
    setTimeout(() => router.push("/funcionarios"), 1500);
  } catch (err) {
    console.error('Erro ao salvar pessoa:', err);
    error.value = err.message || "Erro ao salvar pessoa";
  } finally {
    loading.value = false;
  }
}

function cancelar() {
  router.push("/funcionarios");
}

onMounted(() => {
  carregarDadosIniciais();
});
</script>

<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2>{{ isEdicao ? "Editar Funcionário" : "Novo Funcionário" }}</h2>
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
        <!-- Dados Pessoais -->
        <div class="col-12 mb-3">
            <h5 class="text-secondary border-bottom pb-2">Dados Pessoais</h5>
        </div>

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
          <label class="form-label">Email (Login) *</label>
          <input 
            v-model="pessoa.email" 
            type="email" 
            class="form-control" 
            required 
            :disabled="loading"
          />
        </div>

        <div v-if="!isEdicao" class="col-md-6 mb-3">
          <label class="form-label">Senha *</label>
          <input 
            v-model="pessoa.password"
            type="password"
            class="form-control" 
            required
            :disabled="loading"
          />
        </div>

        <div class="col-md-6 mb-3">
            <label class="form-label">Matrícula</label>
            <input
              v-model="pessoa.matricula"
              type="text"
              class="form-control"
              :disabled="loading"
            />
          </div>

        <!-- Dados Profissionais -->
        <div class="col-12 mb-3 mt-3">
            <h5 class="text-secondary border-bottom pb-2">Dados Profissionais</h5>
        </div>

        <div class="col-md-6 mb-3">
          <label class="form-label">Cargo *</label>
          <input 
            v-model="pessoa.cargo"
            type="text"
            class="form-control" 
            required
            :disabled="loading"
          />
        </div>

        <div class="col-md-6 mb-3">
          <label class="form-label">Departamento *</label>
          <select 
            v-model="pessoa.departamentoId" 
            class="form-select" 
            required
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

        <!-- Acesso e Permissões -->
        <div class="col-12 mb-3 mt-3">
            <h5 class="text-secondary border-bottom pb-2">Acesso e Permissões</h5>
        </div>

        <div class="col-md-6 mb-3">
            <label class="form-label">Perfil de Acesso *</label>
            <select
              v-model="pessoa.role"
              class="form-select"
              required
              :disabled="loading"
            >
              <option value="ROLE_USER">Usuário Comum</option>
              <option value="ROLE_ADMIN">Administrador</option>
            </select>
          </div>

          <div class="col-md-6 mb-3">
            <label class="form-label">Filiais Permitidas *</label>
            <select
              v-model="pessoa.filiaisIds"
              class="form-select"
              multiple
              required
              :disabled="loading"
              style="height: 120px;"
            >
              <option
                v-for="filial in filiais"
                :key="filial.id"
                :value="filial.id"
              >
                {{ filial.nome }}
              </option>
            </select>
            <small class="text-muted">Segure Ctrl (ou Cmd) para selecionar múltiplas.</small>
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
