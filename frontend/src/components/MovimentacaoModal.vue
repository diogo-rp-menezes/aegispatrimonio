<script setup>
import { ref, defineProps, defineEmits, watch } from 'vue';
import { request } from '../services/api';

const props = defineProps({
  show: Boolean,
  ativo: Object
});

const emit = defineEmits(['close', 'success']);

const localizacoes = ref([]);
const funcionarios = ref([]);
const loading = ref(false);
const submitting = ref(false);
const error = ref(null);

const form = ref({
  localizacaoDestinoId: "",
  funcionarioDestinoId: "",
  dataMovimentacao: new Date().toISOString().split('T')[0],
  motivo: "",
  observacoes: ""
});

async function fetchData() {
  loading.value = true;
  try {
    const [locs, funcs] = await Promise.all([
      request('/localizacoes'),
      request('/funcionarios?size=100')
    ]);
    localizacoes.value = locs;
    funcionarios.value = funcs.content;
  } catch (err) {
    console.error("Erro ao carregar dados", err);
    error.value = "Erro ao carregar listas de seleção.";
  } finally {
    loading.value = false;
  }
}

watch(() => props.show, (newVal) => {
  if (newVal) {
    fetchData();
    // Reset form
    form.value = {
        localizacaoDestinoId: "",
        funcionarioDestinoId: "",
        dataMovimentacao: new Date().toISOString().split('T')[0],
        motivo: "",
        observacoes: ""
    };
  }
});

async function submit() {
  if (!form.value.localizacaoDestinoId || !form.value.funcionarioDestinoId || !form.value.motivo) {
      error.value = "Preencha todos os campos obrigatórios.";
      return;
  }

  submitting.value = true;
  error.value = null;

  const payload = {
    ativoId: props.ativo.id,
    localizacaoOrigemId: props.ativo.localizacaoId,
    funcionarioOrigemId: props.ativo.funcionarioResponsavelId,
    localizacaoDestinoId: form.value.localizacaoDestinoId,
    funcionarioDestinoId: form.value.funcionarioDestinoId,
    dataMovimentacao: form.value.dataMovimentacao,
    motivo: form.value.motivo,
    observacoes: form.value.observacoes
  };

  try {
    await request('/movimentacoes', {
      method: 'POST',
      body: payload
    });
    emit('success');
    emit('close');
  } catch (err) {
    console.error("Erro ao criar movimentação", err);
    // Try to extract message from problem detail
    let msg = "Erro ao criar movimentação.";
    if (err.message) {
         try {
             const json = JSON.parse(err.message);
             if (json.detail) msg = json.detail;
         } catch (e) {
             msg = err.message; // Fallback to raw text
         }
    }
    error.value = msg;
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <div v-if="show" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5)">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Movimentar Ativo</h5>
          <button type="button" class="btn-close" @click="$emit('close')"></button>
        </div>
        <div class="modal-body">
          <div v-if="error" class="alert alert-danger">{{ error }}</div>
          <div v-if="loading" class="text-center">
             <div class="spinner-border text-primary" role="status"></div>
          </div>
          <form v-else @submit.prevent="submit">
            <div class="mb-3">
              <label class="form-label">Nova Localização *</label>
              <select class="form-select" v-model="form.localizacaoDestinoId" required>
                <option value="">Selecione...</option>
                <option v-for="loc in localizacoes" :key="loc.id" :value="loc.id">
                  {{ loc.nome }} ({{ loc.filialNome || 'Filial não carregada' }})
                </option>
              </select>
            </div>

            <div class="mb-3">
              <label class="form-label">Novo Responsável *</label>
              <select class="form-select" v-model="form.funcionarioDestinoId" required>
                <option value="">Selecione...</option>
                <option v-for="func in funcionarios" :key="func.id" :value="func.id">
                  {{ func.nome }} ({{ func.departamentoNome || 'Dept. não carregado' }})
                </option>
              </select>
            </div>

            <div class="mb-3">
              <label class="form-label">Data da Movimentação *</label>
              <input type="date" class="form-control" v-model="form.dataMovimentacao" required />
            </div>

            <div class="mb-3">
              <label class="form-label">Motivo *</label>
              <input type="text" class="form-control" v-model="form.motivo" required placeholder="Ex: Mudança de sala, Troca de responsável" />
            </div>

            <div class="mb-3">
              <label class="form-label">Observações</label>
              <textarea class="form-control" v-model="form.observacoes" rows="3"></textarea>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="$emit('close')">Cancelar</button>
          <button type="button" class="btn btn-primary" @click="submit" :disabled="submitting || loading">
            <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
            Solicitar Movimentação
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
