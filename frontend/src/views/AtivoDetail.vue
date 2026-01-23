<template>
  <div class="detail-view" v-if="ativo">
    <button class="btn btn-outline-secondary mb-3" @click="voltar">
      <i class="bi bi-arrow-left"></i> Voltar
    </button>

    <div class="detail-header">
      <div class="row align-items-center">
        <div class="col-md-8">
          <h2><i class="bi bi-building"></i> {{ ativo.nome }}</h2>
          <h4>{{ ativo.tipoAtivoNome }}</h4>
          <div class="d-flex gap-2 mt-2">
            <span class="badge bg-light text-black">{{ ativo.numeroPatrimonio }}</span>
            <span class="badge bg-light text-black">{{ ativo.localizacaoNome }}</span>
            <span class="badge" :class="statusBadgeClass(ativo.status)">
              {{ statusText(ativo.status) }}
            </span>
          </div>
        </div>
        <div class="col-md-4 text-end">
          <button class="btn btn-accent-modern me-2">
            <i class="bi bi-pencil"></i> Editar
          </button>
          <button class="btn btn-primary-modern">
            <i class="bi bi-printer"></i> Imprimir
          </button>
        </div>
      </div>
    </div>

    <div class="row">
      <!-- Esquerda -->
      <div class="col-md-6">
        <div class="detail-section">
          <h5><i class="bi bi-info-circle"></i> Informações Básicas</h5>
          <div class="spec-list mt-3">
            <div class="spec-item"><span class="text-muted">Data de Aquisição:</span> <span>{{ ativo.dataAquisicao || 'N/A' }}</span></div>
            <div class="spec-item"><span class="text-muted">Fornecedor:</span> <span>{{ ativo.fornecedorNome || 'N/A' }}</span></div>
            <div class="spec-item"><span class="text-muted">Valor:</span> <span>{{ formatCurrency(ativo.valorAquisicao) }}</span></div>
            <div class="spec-item"><span class="text-muted">Garantia:</span> <span>{{ ativo.informacoesGarantia || 'N/A' }}</span></div>
            <div class="spec-item"><span class="text-muted">Responsável:</span> <span>{{ ativo.pessoaResponsavelNome || 'N/A' }}</span></div>
          </div>
        </div>

        <div class="detail-section">
          <h5><i class="bi bi-geo-alt"></i> Localização</h5>
          <div class="mt-3">
            <p><strong>Localização:</strong> {{ ativo.localizacaoNome || 'N/A' }}</p>
            <p><strong>Observações:</strong> {{ ativo.observacoes || 'Nenhuma' }}</p>
          </div>
        </div>
      </div>

      <!-- Direita -->
      <div class="col-md-6">
        <div class="detail-section">
          <h5><i class="bi bi-clock-history"></i> Histórico</h5>
          <div class="mt-3">
            <div class="alert alert-info">
              <i class="bi bi-info-circle"></i> Última atualização em {{ ativo.atualizadoEm }}
            </div>
            <ul class="list-group">
              <li class="list-group-item">
                <i class="bi bi-check-circle text-success me-2"></i>
                <strong>Cadastrado em:</strong> {{ ativo.dataRegistro }}
              </li>
              <li class="list-group-item">
                <i class="bi bi-tools text-warning me-2"></i>
                <strong>Valor Contábil Atual:</strong> {{ formatCurrency(ativo.valorContabilAtual) }}
              </li>
              <li class="list-group-item">
                <i class="bi bi-archive text-primary me-2"></i>
                <strong>Depreciação Acumulada:</strong> {{ formatCurrency(ativo.depreciacaoAcumulada) }}
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- Ações rápidas -->
    <div class="detail-section">
      <h5><i class="bi bi-lightning"></i> Ações Rápidas</h5>
      <div class="d-flex gap-2 mt-3">
        <button class="btn btn-outline-primary"><i class="bi bi-arrow-left-right"></i> Transferir</button>
        <button class="btn btn-outline-warning"><i class="bi bi-tools"></i> Registrar Manutenção</button>
        <button class="btn btn-outline-secondary"><i class="bi bi-file-earmark-text"></i> Gerar Relatório</button>
        <button class="btn btn-outline-danger" @click="deleteAtivo"><i class="bi bi-trash"></i> Baixar Item</button>
      </div>
    </div>
  </div>
  <div v-else class="text-center py-5">Carregando detalhes...</div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { request } from '../services/api';

const route = useRoute();
const router = useRouter();

const ativo = ref(null);

onMounted(async () => {
  try {
    const data = await request(`/ativos/${route.params.id}`);
    ativo.value = data;
  } catch (e) {
    console.error('Erro ao carregar ativo', e);
  }
});

function voltar() {
  router.push('/ativos');
}

async function deleteAtivo() {
  if (!confirm('Tem certeza que deseja baixar este ativo? Esta ação não pode ser desfeita.')) {
    return;
  }
  try {
    await request(`/ativos/${ativo.value.id}`, { method: 'DELETE' });
    router.push('/ativos');
  } catch (error) {
    console.error('Erro ao baixar ativo:', error);
    alert('Erro ao baixar ativo.');
  }
}

const currencyFormatter = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
function formatCurrency(v) {
  if (v == null) return 'R$ 0,00';
  const n = typeof v === 'string' ? Number(v) : v;
  return isNaN(n) ? v : currencyFormatter.format(n);
}
function statusText(status) {
  const map = { ATIVO: 'Ativo', INATIVO: 'Inativo', BAIXADO: 'Baixado' };
  return map[status] || status || 'Indefinido';
}
function statusBadgeClass(status) {
  if (status === 'ATIVO') return 'bg-success';
  if (status === 'INATIVO') return 'bg-secondary';
  if (status === 'BAIXADO') return 'bg-danger';
  return 'bg-light text-dark';
}
</script>
