<template>
  <section class="maintenance-view" aria-labelledby="maintenance-title">
    <!-- Header -->
    <div class="category-header">
      <i class="bi bi-tools category-icon" aria-hidden="true"></i>
      <div>
        <h3 id="maintenance-title">Manutenções</h3>
        <p class="text-muted">Gestão de manutenções preventivas, corretivas e preditivas</p>
      </div>
    </div>

    <!-- Botão Nova Solicitação -->
    <div class="d-flex justify-content-end mb-4">
      <button
        v-if="!showForm"
        class="btn btn-primary-modern"
        @click="openForm"
        aria-label="Nova Solicitação"
      >
        <i class="bi bi-plus-circle"></i> Nova Solicitação
      </button>
      <button
        v-else
        class="btn btn-outline-secondary"
        @click="closeForm"
        aria-label="Voltar para lista"
      >
        <i class="bi bi-arrow-left"></i> Voltar para Lista
      </button>
    </div>

    <!-- Formulário -->
    <div v-if="showForm" class="mb-5">
      <ManutencaoForm
        @saved="closeForm"
        @cancel="closeForm"
      />
    </div>

    <!-- Lista -->
    <div v-if="!showForm">
      <div class="card-modern" role="region" aria-label="Lista de Manutenções">
        <div class="card-header-modern d-flex align-items-center justify-content-between">
          <h5 class="card-title-modern">
            <i class="bi bi-list-task me-2" aria-hidden="true"></i>
            Solicitações e Ordens de Serviço
          </h5>

          <div class="d-flex align-items-center gap-2">
            <!-- Filtro de Status -->
            <select v-model="filterStatus" class="form-select form-select-sm" style="width: 200px;" @change="fetchPage(0)">
              <option value="">Todos os Status</option>
              <option value="SOLICITADA">Solicitada</option>
              <option value="APROVADA">Aprovada</option>
              <option value="EM_ANDAMENTO">Em Andamento</option>
              <option value="AGUARDANDO_PECAS">Aguardando Peças</option>
              <option value="CONCLUIDA">Concluída</option>
              <option value="CANCELADA">Cancelada</option>
              <option value="REPROVADA">Reprovada</option>
            </select>

            <button
              class="btn btn-outline-secondary btn-sm"
              @click="fetchPage(0)"
              title="Atualizar"
            >
              <i class="bi bi-arrow-repeat"></i>
            </button>
          </div>
        </div>

        <!-- Table -->
        <div class="table-responsive">
          <table class="table table-modern mb-0">
            <thead>
              <tr>
                <th scope="col">ID</th>
                <th scope="col">Ativo</th>
                <th scope="col">Tipo</th>
                <th scope="col">Data Solic.</th>
                <th scope="col">Solicitante</th>
                <th scope="col">Status</th>
                <th scope="col" class="text-end">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="7" class="text-center py-4">
                  <div class="spinner-border spinner-border-sm me-2" role="status"></div>
                  <span>Carregando...</span>
                </td>
              </tr>
              <tr v-else-if="items.length === 0">
                <td colspan="7" class="text-center py-4 text-muted">
                  Nenhuma manutenção encontrada.
                </td>
              </tr>
              <tr v-for="item in items" :key="item.id">
                <td>#{{ item.id }}</td>
                <td>
                  <strong>{{ item.ativoNome }}</strong>
                  <div class="small text-muted">{{ item.ativoNumeroPatrimonio }}</div>
                </td>
                <td>{{ item.tipo }}</td>
                <td>{{ formatDate(item.dataSolicitacao) }}</td>
                <td>{{ item.solicitanteNome }}</td>
                <td>
                  <span :class="`badge bg-${getStatusColor(item.status)}`">
                    {{ item.status }}
                  </span>
                </td>
                <td class="text-end">
                  <div class="btn-group btn-group-sm">
                    <!-- Ações dependendo do status -->
                    <button
                      v-if="item.status === 'SOLICITADA'"
                      class="btn btn-outline-success"
                      @click="aprovar(item)"
                      title="Aprovar"
                    >
                      <i class="bi bi-check-lg"></i>
                    </button>
                    <button
                      v-if="item.status === 'APROVADA'"
                      class="btn btn-outline-primary"
                      @click="iniciar(item)"
                      title="Iniciar"
                    >
                      <i class="bi bi-play-fill"></i>
                    </button>
                    <button
                      v-if="item.status === 'EM_ANDAMENTO'"
                      class="btn btn-outline-success"
                      @click="concluir(item)"
                      title="Concluir"
                    >
                      <i class="bi bi-check-circle-fill"></i>
                    </button>
                    <button
                      class="btn btn-outline-secondary"
                      @click="verDetalhes(item)"
                      title="Detalhes"
                    >
                      <i class="bi bi-eye"></i>
                    </button>
                    <button
                      v-if="['SOLICITADA', 'APROVADA'].includes(item.status)"
                      class="btn btn-outline-danger"
                      @click="cancelar(item)"
                      title="Cancelar"
                    >
                      <i class="bi bi-x-circle"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div class="card-footer-modern d-flex justify-content-between align-items-center mt-3">
          <div class="small text-muted">
            Mostrando {{ pageInfo.from }}–{{ pageInfo.to }} de {{ pageInfo.totalElements }} registros
          </div>
          <nav>
            <ul class="pagination pagination-sm mb-0">
              <li :class="['page-item', { disabled: pageInfo.page === 0 }]">
                <button class="page-link" @click="prevPage"><i class="bi bi-chevron-left"></i></button>
              </li>
              <li class="page-item disabled">
                <span class="page-link">{{ pageInfo.page + 1 }} / {{ pageInfo.totalPages || 1 }}</span>
              </li>
              <li :class="['page-item', { disabled: pageInfo.page >= pageInfo.totalPages - 1 }]">
                <button class="page-link" @click="nextPage"><i class="bi bi-chevron-right"></i></button>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { manutencaoService } from '../services/manutencaoService';
import ManutencaoForm from './ManutencaoForm.vue';

const items = ref([]);
const loading = ref(false);
const showForm = ref(false);
const filterStatus = ref('');
const editId = ref(null);

const pageInfo = reactive({
  page: 0,
  size: 10,
  totalPages: 0,
  totalElements: 0,
  from: 0,
  to: 0,
});

async function fetchPage(page = 0) {
  loading.value = true;
  try {
    const filters = {
      page,
      size: pageInfo.size,
      status: filterStatus.value || undefined
    };
    const data = await manutencaoService.listar(filters);
    items.value = data.content;
    pageInfo.page = data.number;
    pageInfo.totalPages = data.totalPages;
    pageInfo.totalElements = data.totalElements;
    pageInfo.from = pageInfo.totalElements === 0 ? 0 : pageInfo.page * pageInfo.size + 1;
    pageInfo.to = Math.min(pageInfo.totalElements, (pageInfo.page + 1) * pageInfo.size);
  } catch (error) {
    console.error("Erro ao listar manutenções:", error);
  } finally {
    loading.value = false;
  }
}

function openForm() {
  editId.value = null;
  showForm.value = true;
}

function closeForm() {
  showForm.value = false;
  editId.value = null;
  fetchPage(pageInfo.page);
}

function prevPage() {
  if (pageInfo.page > 0) fetchPage(pageInfo.page - 1);
}

function nextPage() {
  if (pageInfo.page < pageInfo.totalPages - 1) fetchPage(pageInfo.page + 1);
}

function getStatusColor(status) {
  const colors = {
    'SOLICITADA': 'info',
    'APROVADA': 'primary',
    'EM_ANDAMENTO': 'warning',
    'CONCLUIDA': 'success',
    'CANCELADA': 'secondary',
    'REPROVADA': 'danger',
    'AGUARDANDO_PECAS': 'dark'
  };
  return colors[status] || 'secondary';
}

function formatDate(dateString) {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleDateString('pt-BR');
}

// Ações Simplificadas para MVP (Idealmente abririam modais de confirmação/inputs)
async function aprovar(item) {
  if (!confirm(`Aprovar manutenção #${item.id}?`)) return;
  try {
    await manutencaoService.aprovar(item.id);
    fetchPage(pageInfo.page);
  } catch (e) {
    alert('Erro ao aprovar: ' + e.message);
  }
}

async function iniciar(item) {
  // Mock de input técnico ID para MVP (Hardcoded por enquanto ou prompt)
  const tecnicoId = prompt("ID do Técnico Responsável (deve ser da mesma filial):");
  if (!tecnicoId) return;

  try {
    await manutencaoService.iniciar(item.id, { tecnicoId: parseInt(tecnicoId) });
    fetchPage(pageInfo.page);
  } catch (e) {
    alert('Erro ao iniciar: ' + e.message);
  }
}

async function concluir(item) {
  const descricao = prompt("Descrição do Serviço:");
  if (!descricao) return;

  try {
    await manutencaoService.concluir(item.id, {
      descricaoServico: descricao,
      custoReal: 0, // Simplificação
      tempoExecucao: 60 // Simplificação
    });
    fetchPage(pageInfo.page);
  } catch (e) {
    alert('Erro ao concluir: ' + e.message);
  }
}

async function cancelar(item) {
  const motivo = prompt("Motivo do cancelamento:");
  if (!motivo) return;

  try {
    await manutencaoService.cancelar(item.id, motivo);
    fetchPage(pageInfo.page);
  } catch (e) {
    alert('Erro ao cancelar: ' + e.message);
  }
}

function verDetalhes(item) {
  alert(`Detalhes da Manutenção #${item.id}\nAtivo: ${item.ativoNome}\nProblema: ${item.descricaoProblema}`);
}

onMounted(() => {
  fetchPage(0);
});
</script>

<style scoped>
.category-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e9ecef;
}
.category-icon {
  font-size: 2rem;
  color: #0d6efd;
}
.table-modern th {
  font-weight: 600;
  text-transform: uppercase;
  font-size: 0.85rem;
  letter-spacing: 0.5px;
}
</style>
