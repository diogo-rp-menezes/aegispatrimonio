<template>
  <section class="card-modern" aria-labelledby="form-ativo-title">
    <div class="card-header-modern">
      <h5 id="form-ativo-title" class="card-title-modern">
        <i class="bi bi-box-seam me-2"></i>
        {{ isEdit ? 'Editar Ativo' : 'Novo Ativo' }}
      </h5>
    </div>

    <div class="card-body">
      <form @submit.prevent="save">
        <!-- Nome -->
        <div class="mb-3">
          <label for="nome" class="form-label">Nome *</label>
          <input
            id="nome"
            v-model="form.nome"
            type="text"
            class="form-control"
            required
            aria-required="true"
          />
        </div>

        <!-- Tipo -->
        <div class="mb-3">
          <label for="tipo" class="form-label">Tipo do Ativo *</label>
          <select
            id="tipo"
            v-model="form.tipoAtivoId"
            class="form-select"
            required
            aria-required="true"
          >
            <option value="">Selecione...</option>
            <option v-for="t in tipos" :key="t.id" :value="t.id">{{ t.nome }}</option>
          </select>
        </div>

        <!-- Número Patrimônio -->
        <div class="mb-3">
          <label for="numeroPatrimonio" class="form-label">Número Patrimônio</label>
          <input
            id="numeroPatrimonio"
            v-model="form.numeroPatrimonio"
            type="text"
            class="form-control"
          />
        </div>

        <!-- Localização -->
        <div class="mb-3">
          <label for="localizacao" class="form-label">Localização</label>
          <select id="localizacao" v-model="form.localizacaoId" class="form-select">
            <option value="">Selecione...</option>
            <option v-for="l in localizacoes" :key="l.id" :value="l.id">{{ l.nome }}</option>
          </select>
        </div>

        <!-- Status -->
        <div class="mb-3">
          <label for="status" class="form-label">Status</label>
          <select id="status" v-model="form.status" class="form-select">
            <option value="ATIVO">Ativo</option>
            <option value="INATIVO">Inativo</option>
            <option value="BAIXADO">Baixado</option>
          </select>
        </div>

        <!-- Valor -->
        <div class="mb-3">
          <label for="valor" class="form-label">Valor de Aquisição *</label>
          <input
            id="valor"
            v-model="form.valorAquisicao"
            type="number"
            step="0.01"
            class="form-control"
            required
            aria-required="true"
          />
        </div>

        <!-- Data -->
        <div class="mb-3">
          <label for="dataAquisicao" class="form-label">Data de Aquisição</label>
          <input
            id="dataAquisicao"
            v-model="form.dataAquisicao"
            type="date"
            class="form-control"
          />
        </div>

        <!-- Ações -->
        <div class="d-flex justify-content-end gap-2 mt-4">
          <button type="button" class="btn btn-outline-secondary" @click="cancelar">
            Cancelar
          </button>
          <button type="submit" class="btn btn-primary-modern" :disabled="saving">
            <span v-if="saving">Salvando...</span>
            <span v-else>Salvar</span>
          </button>
        </div>
      </form>
    </div>
  </section>
</template>

<script setup>
import { ref, reactive, onMounted, computed, defineProps, defineEmits } from 'vue';
import axios from 'axios';

// Defina props e emits
const props = defineProps({
  ativoId: {
    type: String,
    default: null
  }
});

const emit = defineEmits(['saved', 'cancel']);

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api';

const isEdit = computed(() => !!props.ativoId);

const form = reactive({
  nome: '',
  tipoAtivoId: '',
  numeroPatrimonio: '',
  localizacaoId: '',
  status: 'ATIVO',
  valorAquisicao: '',
  dataAquisicao: '',
});

const tipos = ref([]);
const localizacoes = ref([]);
const saving = ref(false);

onMounted(async () => {
  if (isEdit.value) {
    const { data } = await axios.get(`${API_BASE}/ativos/${props.ativoId}`);
    Object.assign(form, data, {
      tipoAtivoId: data.tipoAtivoId || '',
      localizacaoId: data.localizacaoId || '',
      valorAquisicao: data.valorAquisicao || '',
      dataAquisicao: data.dataAquisicao || '',
    });
  }

  // Carregar listas auxiliares
  try {
    const [tiposResp, locsResp] = await Promise.all([
      axios.get(`${API_BASE}/tipos-ativos`).catch(() => ({ data: [] })),
      axios.get(`${API_BASE}/localizacoes`).catch(() => ({ data: [] })),
    ]);
    tipos.value = tiposResp.data;
    localizacoes.value = locsResp.data;
  } catch (e) {
    console.warn('Não foi possível carregar listas auxiliares', e);
  }
});

async function save() {
  saving.value = true;
  try {
    if (isEdit.value) {
      await axios.put(`${API_BASE}/ativos/${props.ativoId}`, form);
    } else {
      await axios.post(`${API_BASE}/ativos`, form);
    }
    emit('saved');
  } finally {
    saving.value = false;
  }
}

function cancelar() {
  emit('cancel');
}
</script>