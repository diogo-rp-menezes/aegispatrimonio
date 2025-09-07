// src/components/patrimony/AtivoList.vue
<template>
  <div class="ativo-list">
    <div class="header">
      <h2>Gestão de Ativos Patrimoniais</h2>
      <button @click="showForm = true" class="btn-primary">
        <i class="icon-plus"></i> Novo Ativo
      </button>
    </div>

    <!-- Filtros -->
    <div class="filters">
      <input
        v-model="filters.search"
        placeholder="Buscar por nome ou patrimônio..."
        @input="debouncedFetch"
      />
      <select v-model="filters.status" @change="fetchAtivos">
        <option value="">Todos os status</option>
        <option v-for="status in statusOptions" :key="status" :value="status">
          {{ formatStatus(status) }}
        </option>
      </select>
    </div>

    <!-- Loading -->
    <div v-if="store.loading" class="loading">Carregando...</div>

    <!-- Tabela -->
    <div v-else class="table-container">
      <table>
        <thead>
          <tr>
            <th>Patrimônio</th>
            <th>Nome</th>
            <th>Tipo</th>
            <th>Localização</th>
            <th>Status</th>
            <th>Valor</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="ativo in store.ativos" :key="ativo.id">
            <td>{{ ativo.numeroPatrimonio }}</td>
            <td>{{ ativo.nome }}</td>
            <td>{{ ativo.tipoAtivoNome }}</td>
            <td>{{ ativo.localizacaoNome }}</td>
            <td>
              <span :class="['status-badge', ativo.status.toLowerCase()]">
                {{ formatStatus(ativo.status) }}
              </span>
            </td>
            <td>R$ {{ formatCurrency(ativo.valorAquisicao) }}</td>
            <td>
              <button @click="editAtivo(ativo)" class="btn-icon">
                <i class="icon-edit"></i>
              </button>
              <button @click="viewAtivo(ativo)" class="btn-icon">
                <i class="icon-eye"></i>
              </button>
              <button @click="deleteAtivo(ativo)" class="btn-icon danger">
                <i class="icon-trash"></i>
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Paginação -->
      <div v-if="store.pagination" class="pagination">
        <button 
          :disabled="store.pagination.first" 
          @click="changePage(store.pagination.number - 1)"
        >
          Anterior
        </button>
        <span>Página {{ store.pagination.number + 1 }} de {{ store.pagination.totalPages }}</span>
        <button 
          :disabled="store.pagination.last" 
          @click="changePage(store.pagination.number + 1)"
        >
          Próxima
        </button>
      </div>
    </div>

    <!-- Modal de Formulário -->
    <AtivoForm 
      v-if="showForm" 
      :ativo="editingAtivo"
      @close="closeForm"
      @saved="onSaved"
    />

    <!-- Modal de Detalhes -->
    <AtivoDetails 
      v-if="selectedAtivo"
      :ativo="selectedAtivo"
      @close="selectedAtivo = null"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useAtivoStore } from '@/stores/ativo-store';
import { type AtivoResponse, StatusAtivo } from '@/types/ativo';
import AtivoForm from './AtivoForm.vue';
import AtivoDetails from './AtivoDetails.vue';
import { debounce } from '@/utils/debounce';

const store = useAtivoStore();
const showForm = ref(false);
const editingAtivo = ref<AtivoResponse | null>(null);
const selectedAtivo = ref<AtivoResponse | null>(null);

const filters = ref({
  search: '',
  status: ''
});

const statusOptions = Object.values(StatusAtivo);

const pagination = ref({
  page: 0,
  size: 10,
  sort: 'nome'
});

const fetchAtivos = async () => {
  await store.fetchTodos(pagination.value);
};

const debouncedFetch = debounce(fetchAtivos, 300);

const changePage = (page: number) => {
  pagination.value.page = page;
  fetchAtivos();
};

const editAtivo = (ativo: AtivoResponse) => {
  editingAtivo.value = ativo;
  showForm.value = true;
};

const viewAtivo = (ativo: AtivoResponse) => {
  selectedAtivo.value = ativo;
};

const deleteAtivo = async (ativo: AtivoResponse) => {
  if (confirm(`Tem certeza que deseja excluir o ativo "${ativo.nome}"?`)) {
    try {
      await store.deletarAtivo(ativo.id);
      await fetchAtivos();
    } catch (error) {
      console.error('Erro ao excluir ativo:', error);
    }
  }
};

const closeForm = () => {
  showForm.value = false;
  editingAtivo.value = null;
};

const onSaved = () => {
  closeForm();
  fetchAtivos();
};

const formatStatus = (status: StatusAtivo) => {
  const statusMap: Record<StatusAtivo, string> = {
    [StatusAtivo.ATIVO]: 'Ativo',
    [StatusAtivo.EM_MANUTENCAO]: 'Em Manutenção',
    [StatusAtivo.INATIVO]: 'Inativo',
    [StatusAtivo.BAIXADO]: 'Baixado'
  };
  return statusMap[status];
};

const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('pt-BR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(value);
};

onMounted(() => {
  fetchAtivos();
});

watch(filters, () => {
  debouncedFetch();
}, { deep: true });
</script>

<style scoped>
.ativo-list {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filters {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.filters input,
.filters select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.table-container {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  background: #f8f9fa;
  font-weight: 600;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.ativo {
  background: #e8f5e8;
  color: #2e7d32;
}

.status-badge.em_manutencao {
  background: #fff3e0;
  color: #f57c00;
}

.status-badge.inativo {
  background: #f5f5f5;
  color: #757575;
}

.status-badge.baixado {
  background: #ffebee;
  color: #d32f2f;
}

.btn-primary {
  background: #2563eb;
  color: white;
  padding: 10px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.btn-icon {
  background: none;
  border: none;
  padding: 6px;
  cursor: pointer;
  margin: 0 2px;
}

.btn-icon.danger {
  color: #dc2626;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8f9fa;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}
</style>