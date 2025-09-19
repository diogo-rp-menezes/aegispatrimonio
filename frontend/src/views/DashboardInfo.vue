<script setup>
import { ref } from "vue";

// 🔹 Placeholders por enquanto
const stats = ref([
  { title: "Total de Ativos", value: "152", icon: "bi-box-seam", color: "primary" },
  { title: "Em Manutenção", value: "8", icon: "bi-tools", color: "warning" },
  { title: "Depreciação Média", value: "12%", icon: "bi-graph-down", color: "danger" },
  { title: "Filiais", value: "4", icon: "bi-building", color: "success" },
]);

const quickActions = ref([
  { title: "Cadastrar Ativo", icon: "bi-plus-lg", color: "primary" },
  { title: "Gerar Relatório", icon: "bi-file-earmark-text", color: "secondary" },
  { title: "Movimentações", icon: "bi-arrow-left-right", color: "info" },
]);

const tableData = ref([
  { id: 1, nome: "Notebook Dell", categoria: "TI", status: "Ativo" },
  { id: 2, nome: "Projetor Epson", categoria: "Equipamento", status: "Manutenção" },
  { id: 3, nome: "Violão Yamaha", categoria: "Instrumento Musical", status: "Ativo" },
]);
</script>

<template>
  <div class="dashboard">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="fw-bold">Dashboard</h2>
      <button class="btn btn-outline-primary">
        <i class="bi bi-gear"></i> Configurações
      </button>
    </div>

    <!-- Cards de estatísticas -->
    <div class="row g-3 mb-4">
      <div v-for="stat in stats" :key="stat.title" class="col-md-3">
        <div class="card h-100">
          <div class="card-body d-flex align-items-center">
            <div
              :class="`bg-${stat.color} bg-opacity-10 text-${stat.color} rounded-circle d-flex align-items-center justify-content-center me-3`"
              style="width: 55px; height: 55px;"
            >
              <i :class="`bi ${stat.icon} fs-4`"></i>
            </div>
            <div>
              <h6 class="mb-0 text-muted">{{ stat.title }}</h6>
              <h4 class="fw-bold mb-0">{{ stat.value }}</h4>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick actions -->
    <div class="row g-3 mb-4">
      <div v-for="action in quickActions" :key="action.title" class="col-md-4">
        <div class="card text-center p-4">
          <i :class="`bi ${action.icon} text-${action.color} fs-1 mb-2`"></i>
          <h6 class="fw-bold">{{ action.title }}</h6>
          <button class="btn btn-sm btn-outline-primary mt-2">Acessar</button>
        </div>
      </div>
    </div>

    <!-- Tabela resumo -->
    <div class="card">
      <div class="card-body">
        <h5 class="fw-bold mb-3">Últimos Ativos Cadastrados</h5>
        <table class="table table-hover align-middle">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nome</th>
              <th>Categoria</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in tableData" :key="item.id">
              <td>{{ item.id }}</td>
              <td>{{ item.nome }}</td>
              <td>{{ item.categoria }}</td>
              <td>
                <span
                  :class="[
                    'badge',
                    item.status === 'Ativo' ? 'bg-success' :
                    item.status === 'Manutenção' ? 'bg-warning text-dark' :
                    'bg-secondary'
                  ]"
                >
                  {{ item.status }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard h2 {
  font-size: 1.75rem;
}
</style>
