<template>
  <div class="card-modern">
    <div class="card-header-modern">
      <h5 class="card-title-modern">
        <i class="bi bi-file-earmark-plus me-2"></i>
        Nova Solicitação de Manutenção
      </h5>
    </div>
    <div class="card-body-modern">
      <form @submit.prevent="save">
        <div class="row g-3">

          <!-- Ativo -->
          <div class="col-md-6">
            <label class="form-label">Ativo ID *</label>
            <input
              v-model="form.ativoId"
              type="number"
              class="form-control"
              required
              placeholder="ID do Ativo"
            >
            <div class="form-text">Insira o ID do ativo (Ex: 1)</div>
          </div>

          <!-- Solicitante -->
          <div class="col-md-6">
            <label class="form-label">Solicitante ID *</label>
            <input
              v-model="form.solicitanteId"
              type="number"
              class="form-control"
              required
              placeholder="ID do Funcionário Solicitante"
            >
            <div class="form-text">Insira o ID do funcionário (Ex: 1)</div>
          </div>

          <!-- Tipo -->
          <div class="col-md-6">
            <label class="form-label">Tipo de Manutenção *</label>
            <select v-model="form.tipo" class="form-select" required>
              <option value="CORRETIVA">Corretiva</option>
              <option value="PREVENTIVA">Preventiva</option>
              <option value="PREDITIVA">Preditiva</option>
              <option value="AJUSTE">Ajuste</option>
              <option value="CALIBRACAO">Calibração</option>
              <option value="LIMPEZA">Limpeza</option>
              <option value="OUTROS">Outros</option>
            </select>
          </div>

          <!-- Fornecedor -->
          <div class="col-md-6">
            <label class="form-label">Fornecedor ID (Opcional)</label>
            <input
              v-model="form.fornecedorId"
              type="number"
              class="form-control"
              placeholder="ID do Fornecedor"
            >
          </div>

          <!-- Descrição do Problema -->
          <div class="col-12">
            <label class="form-label">Descrição do Problema *</label>
            <textarea
              v-model="form.descricaoProblema"
              class="form-control"
              rows="3"
              required
            ></textarea>
          </div>

          <!-- Custo Estimado -->
          <div class="col-md-6">
            <label class="form-label">Custo Estimado (R$)</label>
            <input
              v-model="form.custoEstimado"
              type="number"
              step="0.01"
              class="form-control"
            >
          </div>

          <!-- Data Prevista -->
          <div class="col-md-6">
            <label class="form-label">Data Prevista Conclusão</label>
            <input
              v-model="form.dataPrevistaConclusao"
              type="date"
              class="form-control"
            >
          </div>

          <!-- Observações -->
          <div class="col-12">
            <label class="form-label">Observações</label>
            <textarea
              v-model="form.observacoes"
              class="form-control"
              rows="2"
            ></textarea>
          </div>

        </div>

        <div class="d-flex justify-content-end gap-2 mt-4">
          <button type="button" class="btn btn-outline-secondary" @click="$emit('cancel')">
            Cancelar
          </button>
          <button type="submit" class="btn btn-primary-modern" :disabled="saving">
            <span v-if="saving" class="spinner-border spinner-border-sm me-2" role="status"></span>
            Salvar Solicitação
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { manutencaoService } from '../services/manutencaoService';

const emit = defineEmits(['saved', 'cancel']);

const saving = ref(false);
const form = reactive({
  ativoId: '',
  solicitanteId: '',
  fornecedorId: '',
  tipo: 'CORRETIVA',
  descricaoProblema: '',
  custoEstimado: '',
  dataPrevistaConclusao: '',
  observacoes: ''
});

async function save() {
  saving.value = true;
  try {
    const payload = { ...form };
    if (!payload.fornecedorId) delete payload.fornecedorId;

    await manutencaoService.criar(payload);
    emit('saved');
  } catch (e) {
    alert('Erro ao salvar: ' + e.message);
  } finally {
    saving.value = false;
  }
}
</script>
