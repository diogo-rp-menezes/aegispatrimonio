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
            <div class="input-group input-group-sm me-2" style="width: 220px;">
              <input
                v-model="q"
                @keyup.enter="fetchPage(0)"
                class="form-control form-control-sm"
                type="search"
                placeholder="Buscar por nome"
                aria-label="Buscar por nome"
              />
              <button class="btn btn-outline-secondary btn-sm" @click="fetchPage(0)" :aria-label="'Buscar ' + q">
                <i class="bi bi-search"></i>
              </button>
            </div>
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
                <td colspan="7" class="text-center py-4">Carregando...</td>
              </tr>

              <tr v-if="!loading && items.length === 0">
                <td colspan="7" class="text-center py-4">Nenhum ativo encontrado.</td>
              </tr>

              <tr v-for="item in items" :key="item.id">
                <td>{{ item.nome }}</td>
                <td>{{ item.tipoAtivoNome || '-' }}</td>
                <td>{{ item.numeroPatrimonio || '-' }}</td>
                <td>{{ item.localizacaoNome || '-' }}</td>
                <td>{{ formatCurrency(item.valorAquisicao) }}</td>
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
                    >
                      <i class="bi bi-pencil"></i> Editar
                    </button>
                    <button
                      class="btn btn-outline-secondary"
                      @click="goToDetail(item.id)"
                      :aria-label="`Ver detalhes de ${item.nome}`"
                    >
                      <i class="bi bi-eye"></i> Detalhes
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
          </div>

          <nav aria-label="Paginação de ativos">
            <ul class="pagination pagination-sm mb-0">
              <li :class="['page-item', { disabled: pageInfo.page === 0 }]">
                <button class="page-link" @click="prevPage" :disabled="pageInfo.page === 0">&laquo;</button>
              </li>
              <li
                v-for="p in displayedPageNumbers"
                :key="p"
                :class="['page-item', { active: p === pageInfo.page }]"
              >
                <button class="page-link" @click="fetchPage(p)">{{ p + 1 }}</button>
              </li>
              <li :class="['page-item', { disabled: pageInfo.page >= pageInfo.totalPages - 1 }]">
                <button class="page-link" @click="nextPage" :disabled="pageInfo.page >= pageInfo.totalPages - 1">&raquo;</button>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import AtivoForm from './AtivoForm.vue';

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api';
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
    if (q.value) params.nome = q.value;
    const { data } = await axios.get(`${API_BASE}/ativos`, { params });
    items.value = data.content || [];
    pageInfo.page = data.number;
    pageInfo.size = data.size;
    pageInfo.totalPages = data.totalPages;
    pageInfo.totalElements = data.totalElements;
    pageInfo.from = pageInfo.totalElements === 0 ? 0 : pageInfo.page * pageInfo.size + 1;
    pageInfo.to = Math.min(pageInfo.totalElements, (pageInfo.page + 1) * pageInfo.size);
  } finally {
    loading.value = false;
  }
}

function prevPage() { if (pageInfo.page > 0) fetchPage(pageInfo.page - 1); }
function nextPage() { if (pageInfo.page < pageInfo.totalPages - 1) fetchPage(pageInfo.page + 1); }

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

fetchPage(0);
</script>

<style scoped>
.btn-group {
  white-space: nowrap;
}
</style>