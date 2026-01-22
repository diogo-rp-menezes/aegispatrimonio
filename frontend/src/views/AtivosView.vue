<template>
  <section class="patrimony-category" aria-labelledby="ativos-title">
    <!-- Header -->
    <div class="category-header">
      <i class="bi bi-building category-icon" aria-hidden="true"></i>
      <div>
        <h3 id="ativos-title">Ativos</h3>
        <p class="text-muted">Listagem de ativos patrimoniais</p>
      </div>
    </div>

    <!-- Botão Adicionar -->
    <div class="d-flex justify-content-end mb-4">
      <button
        v-if="!showForm"
        class="btn btn-primary-modern"
        @click="showForm = true"
        aria-label="Adicionar ativo"
      >
        <i class="bi bi-plus"></i> Adicionar Ativo
      </button>
      <button
        v-else
        class="btn btn-outline-secondary"
        @click="showForm = false"
        aria-label="Voltar para lista"
      >
        <i class="bi bi-arrow-left"></i> Voltar para Lista
      </button>
    </div>

    <!-- Formulário -->
    <div v-if="showForm" class="mb-5">
      <AtivoForm 
        :ativo-id="ativoEditId"
        @saved="onAtivoSaved"
        @cancel="onAtivoCancel"
      />
    </div>

    <!-- Lista (só mostra se não estiver no formulário) -->
    <div v-if="!showForm">
      <!-- Card -->
      <div class="card-modern" role="region" aria-label="Lista de Ativos">
        <div class="card-header-modern d-flex align-items-center justify-content-between">
          <h5 class="card-title-modern">
            <i class="bi bi-list-check me-2" aria-hidden="true"></i>
            Inventário de Ativos
          </h5>

          <div class="d-flex align-items-center gap-2">
            <!-- Busca com Debounce Automático -->
            <div class="input-group input-group-sm me-2" style="width: 280px;">
              <span class="input-group-text bg-light">
                <i class="bi bi-search"></i>
              </span>
              <input
                v-model="q"
                class="form-control form-control-sm"
                type="search"
                placeholder="Digite para buscar automaticamente..."
                aria-label="Buscar ativos por nome"
                @keyup.enter="fetchPage(0)"
              />
              <button 
                v-if="q" 
                class="btn btn-outline-danger btn-sm" 
                @click="limparBusca"
                title="Limpar busca"
                aria-label="Limpar busca"
              >
                <i class="bi bi-x-lg"></i>
              </button>
              <button 
                v-else
                class="btn btn-outline-secondary btn-sm" 
                @click="fetchPage(0)"
                title="Buscar"
                aria-label="Buscar"
              >
                <i class="bi bi-arrow-repeat"></i>
              </button>
            </div>
          </div>
        </div>

        <!-- Indicador de Busca -->
        <div v-if="q && !loading" class="card-body-modern border-bottom">
          <div class="alert alert-light py-2 mb-0">
            <small>
              <i class="bi bi-search me-1"></i>
              Buscando por: "<strong>{{ q }}</strong>"
              <span v-if="items.length > 0">- {{ items.length }} resultado(s) encontrado(s)</span>
            </small>
          </div>
        </div>

        <!-- Table -->
        <div class="table-responsive" role="table" aria-describedby="table-desc">
          <div id="table-desc" class="visually-hidden">Tabela com ativos patrimoniais</div>

          <table class="table table-modern mb-0" aria-live="polite">
            <thead>
              <tr>
                <th scope="col">Nome</th>
                <th scope="col">Tipo</th>
                <th scope="col">Patrimônio</th>
                <th scope="col">Localização</th>
                <th scope="col">Valor</th>
                <th scope="col">Status</th>
                <th scope="col" class="text-end">Ações</th>
              </tr>
            </thead>

            <tbody>
              <tr v-if="loading">
                <td colspan="7" class="text-center py-4">
                  <div class="d-flex align-items-center justify-content-center">
                    <div class="spinner-border spinner-border-sm me-2" role="status"></div>
                    <span>Buscando ativos...</span>
                  </div>
                </td>
              </tr>

              <tr v-if="!loading && items.length === 0 && q">
                <td colspan="7" class="text-center py-4">
                  <i class="bi bi-search display-6 text-muted d-block mb-2"></i>
                  <span class="text-muted">Nenhum ativo encontrado para "</span>
                  <strong>"{{ q }}"</strong>
                  <br>
                  <small class="text-muted">Tente ajustar os termos da busca.</small>
                </td>
              </tr>

              <tr v-if="!loading && items.length === 0 && !q">
                <td colspan="7" class="text-center py-4">
                  <i class="bi bi-inbox display-6 text-muted d-block mb-2"></i>
                  <span class="text-muted">Nenhum ativo cadastrado.</span>
                  <br>
                  <small class="text-muted">Clique em "Adicionar Ativo" para começar.</small>
                </td>
              </tr>

              <tr v-for="item in items" :key="item.id">
                <td>
                  <strong>{{ item.nome }}</strong>
                  <br>
                  <small class="text-muted">ID: {{ item.id }}</small>
                </td>
                <td>{{ item.tipoAtivoNome || '-' }}</td>
                <td>
                  <span class="badge bg-light text-dark border">
                    {{ item.numeroPatrimonio || 'N/A' }}
                  </span>
                </td>
                <td>{{ item.localizacaoNome || '-' }}</td>
                <td>
                  <strong :class="getValueClass(item.valorAquisicao)">
                    {{ formatCurrency(item.valorAquisicao) }}
                  </strong>
                </td>
                <td>
                  <span :class="`badge bg-${getStatusBadge(item.status)}`">
                    {{ getStatusText(item.status) }}
                  </span>
                </td>
                <td class="text-end">
                  <div class="btn-group btn-group-sm">
                    <button
                      class="btn btn-outline-primary"
                      @click="editAtivo(item.id)"
                      :aria-label="`Editar ${item.nome}`"
                      title="Editar"
                    >
                      <i class="bi bi-pencil"></i>
                    </button>
                    <button
                      class="btn btn-outline-success"
                      @click="goToDetail(item.id)"
                      :aria-label="`Ver detalhes de ${item.nome}`"
                      title="Detalhes"
                    >
                      <i class="bi bi-eye"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Footer / Pagination -->
        <div class="card-footer-modern d-flex justify-content-between align-items-center mt-3">
          <div class="small text-muted">
            Mostrando {{ pageInfo.from }}–{{ pageInfo.to }} de {{ pageInfo.totalElements }} registros
            <span v-if="q">- Filtrado por: "{{ q }}"</span>
          </div>

          <nav aria-label="Paginação de ativos">
            <ul class="pagination pagination-sm mb-0">
              <li :class="['page-item', { disabled: pageInfo.page === 0 }]">
                <button class="page-link" @click="prevPage" :disabled="pageInfo.page === 0">
                  <i class="bi bi-chevron-left"></i>
                </button>
              </li>
              <li
                v-for="p in displayedPageNumbers"
                :key="p"
                :class="['page-item', { active: p === pageInfo.page }]"
              >
                <button class="page-link" @click="fetchPage(p)">{{ p + 1 }}</button>
              </li>
              <li :class="['page-item', { disabled: pageInfo.page >= pageInfo.totalPages - 1 }]">
                <button class="page-link" @click="nextPage" :disabled="pageInfo.page >= pageInfo.totalPages - 1">
                  <i class="bi bi-chevron-right"></i>
                </button>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue';
import { useDebounceFn } from '@vueuse/core';
import { useRouter } from 'vue-router';
import { request } from '../services/api';
import AtivoForm from './AtivoForm.vue';

const PAGE_SIZE = 10;

const router = useRouter();

const q = ref('');
const items = ref([]);
const loading = ref(false);
const showForm = ref(false);
const ativoEditId = ref(null);

const pageInfo = reactive({
  page: 0,
  size: PAGE_SIZE,
  totalPages: 0,
  totalElements: 0,
  from: 0,
  to: 0,
});

// Debounce automático com VueUse (400ms de delay)
const debouncedFetch = useDebounceFn(() => {
  fetchPage(0);
}, 400);

// Watcher para busca automática
watch(q, (newValue, oldValue) => {
  if (newValue !== oldValue) {
    debouncedFetch();
  }
});

// Funções do formulário
function openAddForm() {
  ativoEditId.value = null;
  showForm.value = true;
}

function editAtivo(id) {
  ativoEditId.value = id;
  showForm.value = true;
}

function onAtivoSaved() {
  showForm.value = false;
  ativoEditId.value = null;
  fetchPage(0);
}

function onAtivoCancel() {
  showForm.value = false;
  ativoEditId.value = null;
}

// Funções auxiliares
function getStatusBadge(status) {
  const statusMap = {
    'ATIVO': 'success',
    'INATIVO': 'warning',
    'BAIXADO': 'danger'
  };
  return statusMap[status] || 'secondary';
}

function getStatusText(status) {
  const statusMap = {
    'ATIVO': 'Ativo',
    'INATIVO': 'Inativo',
    'BAIXADO': 'Baixado'
  };
  return statusMap[status] || status;
}

function getValueClass(value) {
  const numValue = typeof value === 'string' ? Number(value) : value;
  return numValue > 10000 ? 'text-success' : 'text-dark';
}

// Funções de paginação e busca
const currencyFormatter = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
function formatCurrency(v) {
  if (v == null) return 'R$ 0,00';
  const n = typeof v === 'string' ? Number(v) : v;
  return isNaN(n) ? v : currencyFormatter.format(n);
}

async function fetchPage(page = 0) {
  loading.value = true;
  try {
    const params = { page, size: pageInfo.size };
    if (q.value.trim()) params.nome = q.value.trim();
    
    const data = await request('/ativos', { params });
    items.value = data.content || [];
    pageInfo.page = data.number || 0;
    pageInfo.size = data.size || PAGE_SIZE;
    pageInfo.totalPages = data.totalPages || 0;
    pageInfo.totalElements = data.totalElements || 0;
    pageInfo.from = pageInfo.totalElements === 0 ? 0 : pageInfo.page * pageInfo.size + 1;
    pageInfo.to = Math.min(pageInfo.totalElements, (pageInfo.page + 1) * pageInfo.size);
  } catch (error) {
    console.error('Erro ao carregar ativos:', error);
    items.value = [];
    pageInfo.totalElements = 0;
    pageInfo.from = 0;
    pageInfo.to = 0;
  } finally {
    loading.value = false;
  }
}

function limparBusca() {
  q.value = '';
  fetchPage(0);
}

function prevPage() { 
  if (pageInfo.page > 0) fetchPage(pageInfo.page - 1); 
}

function nextPage() { 
  if (pageInfo.page < pageInfo.totalPages - 1) fetchPage(pageInfo.page + 1); 
}

const displayedPageNumbers = computed(() => {
  const total = pageInfo.totalPages || 1;
  const current = pageInfo.page || 0;
  const maxSlots = 5;
  let start = Math.max(0, current - Math.floor(maxSlots / 2));
  let end = Math.min(total - 1, start + maxSlots - 1);
  if (end - start < maxSlots - 1) start = Math.max(0, end - maxSlots + 1);
  return Array.from({ length: end - start + 1 }, (_, i) => start + i);
});

function goToDetail(id) {
  router.push(`/ativos/${id}`);
}

// Carregar inicialmente
onMounted(() => {
  fetchPage(0);
});
</script>

<style scoped>
.btn-group {
  white-space: nowrap;
}

.badge {
  font-size: 0.75em;
}

.spinner-border-sm {
  width: 1rem;
  height: 1rem;
}

.display-6 {
  font-size: 2.5rem;
}

.text-success {
  color: #198754 !important;
}

.bg-light {
  background-color: #f8f9fa !important;
}
</style>