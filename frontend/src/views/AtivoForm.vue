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

        <!-- Fornecedor -->
        <div class="mb-3">
          <label for="fornecedor" class="form-label">Fornecedor *</label>
          <select
            id="fornecedor"
            v-model="form.fornecedorId"
            class="form-select"
            required
            aria-required="true"
          >
            <option value="">Selecione...</option>
            <option v-for="f in fornecedores" :key="f.id" :value="f.id">{{ f.nome }}</option>
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

        <!-- Detalhes de Hardware -->
        <div class="card mt-3 mb-3">
          <div class="card-header d-flex justify-content-between align-items-center" @click="showHardware = !showHardware" style="cursor: pointer;">
            <h6 class="mb-0">Detalhes de Hardware (Opcional)</h6>
            <i class="bi" :class="showHardware ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
          </div>
          <div v-show="showHardware" class="card-body">
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Nome do Computador</label>
                  <input v-model="form.detalheHardware.computerName" type="text" class="form-control">
                </div>
                <div class="col-md-6 mb-3">
                  <label class="form-label">Domínio</label>
                  <input v-model="form.detalheHardware.domain" type="text" class="form-control">
                </div>
                <div class="col-md-4 mb-3">
                  <label class="form-label">SO Nome</label>
                  <input v-model="form.detalheHardware.osName" type="text" class="form-control">
                </div>
                <div class="col-md-4 mb-3">
                  <label class="form-label">SO Versão</label>
                  <input v-model="form.detalheHardware.osVersion" type="text" class="form-control">
                </div>
                <div class="col-md-4 mb-3">
                  <label class="form-label">SO Arquitetura</label>
                  <input v-model="form.detalheHardware.osArchitecture" type="text" class="form-control">
                </div>
                <div class="col-md-12 mb-3">
                  <label class="form-label">Modelo CPU</label>
                  <input v-model="form.detalheHardware.cpuModel" type="text" class="form-control">
                </div>
                <div class="col-md-6 mb-3">
                  <label class="form-label">Cores</label>
                  <input v-model="form.detalheHardware.cpuCores" type="number" class="form-control">
                </div>
                <div class="col-md-6 mb-3">
                  <label class="form-label">Threads</label>
                  <input v-model="form.detalheHardware.cpuThreads" type="number" class="form-control">
                </div>
                <div class="col-md-4 mb-3">
                  <label class="form-label">Fabricante Placa-Mãe</label>
                  <input v-model="form.detalheHardware.motherboardManufacturer" type="text" class="form-control">
                </div>
                <div class="col-md-4 mb-3">
                  <label class="form-label">Modelo Placa-Mãe</label>
                  <input v-model="form.detalheHardware.motherboardModel" type="text" class="form-control">
                </div>
                <div class="col-md-4 mb-3">
                  <label class="form-label">Serial Placa-Mãe</label>
                  <input v-model="form.detalheHardware.motherboardSerialNumber" type="text" class="form-control">
                </div>
              </div>
          </div>
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
import { request } from '../services/api';

// Defina props e emits
const props = defineProps({
  ativoId: {
    type: String,
    default: null
  }
});

const emit = defineEmits(['saved', 'cancel']);

const isEdit = computed(() => !!props.ativoId);

const form = reactive({
  filialId: '',
  nome: '',
  tipoAtivoId: '',
  fornecedorId: '',
  numeroPatrimonio: '',
  localizacaoId: '',
  status: 'ATIVO',
  valorAquisicao: '',
  dataAquisicao: '',
  detalheHardware: {
    computerName: '',
    domain: '',
    osName: '',
    osVersion: '',
    osArchitecture: '',
    motherboardManufacturer: '',
    motherboardModel: '',
    motherboardSerialNumber: '',
    cpuModel: '',
    cpuCores: null,
    cpuThreads: null
  }
});

const tipos = ref([]);
const localizacoes = ref([]);
const fornecedores = ref([]);
const saving = ref(false);
const showHardware = ref(false);

onMounted(async () => {
  if (isEdit.value) {
    const data = await request(`/ativos/${props.ativoId}`);
    Object.assign(form, data, {
      tipoAtivoId: data.tipoAtivoId || '',
      fornecedorId: data.fornecedorId || '',
      localizacaoId: data.localizacaoId || '',
      valorAquisicao: data.valorAquisicao || '',
      dataAquisicao: data.dataAquisicao || '',
      filialId: data.filialId || ''
    });
    if (data.detalheHardware) {
      Object.assign(form.detalheHardware, data.detalheHardware);
    } else if (!form.detalheHardware) {
       form.detalheHardware = {
        computerName: '',
        domain: '',
        osName: '',
        osVersion: '',
        osArchitecture: '',
        motherboardManufacturer: '',
        motherboardModel: '',
        motherboardSerialNumber: '',
        cpuModel: '',
        cpuCores: null,
        cpuThreads: null
       };
    }
  } else {
    // Inject filialId from localStorage
    const fid = localStorage.getItem('currentFilial');
    if (fid) {
      form.filialId = Number(fid);
    }
  }

  // Carregar listas auxiliares
  try {
    const [tiposData, locsData, fornsData] = await Promise.all([
      request('/tipos-ativos').catch(() => []),
      request('/localizacoes').catch(() => []),
      request('/fornecedores').catch(() => [])
    ]);
    tipos.value = tiposData;
    localizacoes.value = locsData;
    fornecedores.value = fornsData;
  } catch (e) {
    console.warn('Não foi possível carregar listas auxiliares', e);
  }
});

async function save() {
  saving.value = true;
  try {
    if (isEdit.value) {
      await request(`/ativos/${props.ativoId}`, { method: 'PUT', body: form });
    } else {
      await request('/ativos', { method: 'POST', body: form });
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
