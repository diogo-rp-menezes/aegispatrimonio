<script setup>
import { ref, onMounted } from 'vue';
import { request } from '../../services/api';

const permissions = ref([]);
const loading = ref(true);
const error = ref(null);

const fetchPermissions = async () => {
    loading.value = true;
    try {
        permissions.value = await request('/permissions');
    } catch (e) {
        error.value = "Erro ao carregar permissões.";
        console.error(e);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    fetchPermissions();
});
</script>

<template>
    <div class="container-fluid">
        <h2 class="mb-4">Permissões do Sistema</h2>

        <div v-if="loading" class="text-center">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Carregando...</span>
            </div>
        </div>

        <div v-else-if="error" class="alert alert-danger">
            {{ error }}
        </div>

        <div v-else class="card shadow-sm">
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-hover align-middle">
                        <thead class="table-light">
                            <tr>
                                <th>ID</th>
                                <th>Recurso</th>
                                <th>Ação</th>
                                <th>Descrição</th>
                                <th>Contexto</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="perm in permissions" :key="perm.id">
                                <td>{{ perm.id }}</td>
                                <td><span class="badge bg-primary">{{ perm.resource }}</span></td>
                                <td><span class="badge bg-secondary">{{ perm.action }}</span></td>
                                <td>{{ perm.description }}</td>
                                <td><code>{{ perm.contextKey || '*' }}</code></td>
                            </tr>
                            <tr v-if="permissions.length === 0">
                                <td colspan="5" class="text-center text-muted">Nenhuma permissão encontrada.</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</template>
