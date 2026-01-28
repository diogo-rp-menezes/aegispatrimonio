<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { request } from '../../services/api';

const router = useRouter();
const roles = ref([]);
const loading = ref(true);
const error = ref(null);

const fetchRoles = async () => {
    loading.value = true;
    try {
        roles.value = await request('/roles');
    } catch (e) {
        error.value = "Erro ao carregar perfis de acesso.";
        console.error(e);
    } finally {
        loading.value = false;
    }
};

const deleteRole = async (id) => {
    if (!confirm('Tem certeza que deseja excluir este perfil?')) return;
    try {
        await request(`/roles/${id}`, { method: 'DELETE' });
        roles.value = roles.value.filter(r => r.id !== id);
    } catch (e) {
        alert('Erro ao excluir perfil. Verifique se não está em uso.');
        console.error(e);
    }
};

onMounted(() => {
    fetchRoles();
});
</script>

<template>
    <div class="container-fluid">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2>Perfis de Acesso</h2>
            <button class="btn btn-primary" @click="router.push('/admin/roles/novo')">
                <i class="bi bi-plus-lg me-2"></i>Novo Perfil
            </button>
        </div>

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
                                <th>Nome</th>
                                <th>Descrição</th>
                                <th>Permissões</th>
                                <th class="text-end">Ações</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="role in roles" :key="role.id">
                                <td>{{ role.id }}</td>
                                <td class="fw-bold">{{ role.name }}</td>
                                <td>{{ role.description || '-' }}</td>
                                <td>
                                    <span class="badge bg-info text-dark">{{ role.permissions ? role.permissions.length : 0 }} permissões</span>
                                </td>
                                <td class="text-end">
                                    <button class="btn btn-sm btn-outline-primary me-2" @click="router.push(`/admin/roles/${role.id}/editar`)">
                                        <i class="bi bi-pencil"></i>
                                    </button>
                                    <button class="btn btn-sm btn-outline-danger" @click="deleteRole(role.id)">
                                        <i class="bi bi-trash"></i>
                                    </button>
                                </td>
                            </tr>
                            <tr v-if="roles.length === 0">
                                <td colspan="5" class="text-center text-muted">Nenhum perfil encontrado.</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</template>
