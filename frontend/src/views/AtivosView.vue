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

          <button
            class="btn btn-sm btn-primary-modern"
            @click="openAddModal"
            aria-haspopup="dialog"
            aria-controls="add-ativo-modal"
            aria-label="Adicionar ativo"
          >
            <i class="bi bi-plus"></i> Adicionar
          </button>
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
              <th scope="col" class="text-end">Ações</th>
            </tr>
          </thead>

          <tbody>
            <tr v-if="loading">
              <td colspan="6" class="text-center py-4">Carregando...</td>
            </tr>

            <tr v-if="!loading && items.length === 0">
              <td colspan="6" class="text-center py-4">Nenhum ativo encontrado.</td>
            </tr>

            <tr v-for="item in items" :key="item.id">
              <td>{{ item.nome }}</td>
              <td>{{ item.tipoAtivoNome || '-' }}</td>
              <td>{{ item.numeroPatrimonio || '-' }}</td>
              <td>{{ item.localizacaoNome || '-' }}</td>
              <td>{{ formatCurrency(item.valorAquisicao) }}</td>
              <td class="text-end">
                <button
                  class="btn btn-sm btn-outline-primary"
                  @click="goToDetail(item.id)"
                  :aria-label="`Ver detalhes de ${item.nome}`"
                >
                  <i class="bi bi-eye"></i> Detalhes
                </button>
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

    <!-- Modal de Adicionar -->
    <div v-if="showAdd" id="add-ativo-modal" class="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="modal-add-title">
      <div class="modal-container">
        <div class="modal-header">
          <h5 id="modal-add-title">Adicionar Ativo</h5>
          <button class="btn-close" @click="closeAddModal" aria-label="Fechar modal"></button>
        </div>
        <div class="modal-body">
          <p class="text-muted">Formulário de criação (placeholder). Substitua pelo seu formulário real.</p>
          <div class="mb-3">
            <label class="form-label">Nome</label>
            <input v-model="newItem.nome" class="form-control form-control-sm" type="text" />
          </div>
          <div class="d-flex gap-2 justify-content-end">
            <button class="btn btn-outline-secondary" @click="closeAddModal">Cancelar</button>
            <button class="btn btn-primary-modern" @click="createItem" :disabled="adding">
              <span v-if="adding">Salvando...</span>
              <span v-else>Salvar</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api';
const PAGE_SIZE = 10;

const router = useRouter();

const q = ref('');
const items = ref([]);
const loading = ref(false);

const pageInfo = reactive({
  page: 0,
  size: PAGE_SIZE,
  totalPages: 0,
  totalElements: 0,
  from: 0,
  to: 0,
});

const showAdd = ref(false);
const adding = ref(false);
const newItem = reactive({ nome: '' });

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

function openAddModal() { showAdd.value = true; newItem.nome = ''; }
function closeAddModal() { showAdd.value = false; }
async function createItem() {
  adding.value = true;
  try {
    await axios.post(`${API_BASE}/ativos`, { nome: newItem.nome });
    closeAddModal();
    await fetchPage(0);
  } finally {
    adding.value = false;
  }
}
function goToDetail(id) {
  router.push(`/ativos/${id}`);
}

fetchPage(0);
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(16, 24, 40, 0.6);
  z-index: 1050;
}
.modal-container {
  width: 640px;
  max-width: 94%;
  background: var(--aegis-surface, #fff);
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  overflow: hidden;
}
.modal-header { padding: 12px 16px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(0,0,0,0.06); }
.modal-body { padding: 16px; }
.btn-close { background: transparent; border: none; font-size: 1.1rem; }
</style>
