<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { request } from '../../services/api';

const router = useRouter();
const route = useRoute();
const isEdit = computed(() => !!route.params.id);

const form = ref({
    name: '',
    description: '',
    permissionIds: []
});

const allPermissions = ref([]);
const loading = ref(true);
const saving = ref(false);
const error = ref(null);

const groupedPermissions = computed(() => {
    const groups = {};
    allPermissions.value.forEach(p => {
        if (!groups[p.resource]) groups[p.resource] = [];
        groups[p.resource].push(p);
    });
    return groups;
});

const fetchPermissions = async () => {
    try {
        allPermissions.value = await request('/permissions');
    } catch (e) {
        error.value = "Erro ao carregar permissões.";
        console.error(e);
    }
};

const fetchRole = async () => {
    if (!isEdit.value) return;
    try {
        const role = await request(`/roles/${route.params.id}`);
        form.value.name = role.name;
        form.value.description = role.description;
        form.value.permissionIds = role.permissions.map(p => p.id);
    } catch (e) {
        error.value = "Erro ao carregar perfil.";
        console.error(e);
    }
};

const submit = async () => {
    saving.value = true;
    try {
        const payload = {
            name: form.value.name,
            description: form.value.description,
            permissionIds: form.value.permissionIds
        };

        if (isEdit.value) {
            await request(`/roles/${route.params.id}`, {
                method: 'PUT',
                body: payload
            });
        } else {
            await request('/roles', {
                method: 'POST',
                body: payload
            });
        }
        router.push('/admin/roles');
    } catch (e) {
        error.value = "Erro ao salvar perfil.";
        console.error(e);
        saving.value = false;
    }
};

onMounted(async () => {
    loading.value = true;
    await fetchPermissions();
    await fetchRole();
    loading.value = false;
});
</script>

<template>
    <div class="container-fluid">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2>{{ isEdit ? 'Editar Perfil' : 'Novo Perfil' }}</h2>
            <button class="btn btn-outline-secondary" @click="router.back()">
                <i class="bi bi-arrow-left me-2"></i>Voltar
            </button>
        </div>

        <div v-if="loading" class="text-center">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Carregando...</span>
            </div>
        </div>

        <div v-else>
            <div v-if="error" class="alert alert-danger mb-4">{{ error }}</div>

            <form @submit.prevent="submit" class="row g-4">
                <div class="col-md-8">
                    <div class="card shadow-sm mb-4">
                        <div class="card-body">
                            <h5 class="card-title mb-3">Dados Básicos</h5>
                            <div class="mb-3">
                                <label for="name" class="form-label">Nome</label>
                                <input type="text" class="form-control" id="name" v-model="form.name" required placeholder="Ex: ROLE_FINANCEIRO">
                            </div>
                            <div class="mb-3">
                                <label for="description" class="form-label">Descrição</label>
                                <textarea class="form-control" id="description" v-model="form.description" rows="3"></textarea>
                            </div>
                        </div>
                    </div>

                    <div class="card shadow-sm">
                        <div class="card-body">
                            <h5 class="card-title mb-3">Permissões</h5>
                            <div v-for="(perms, resource) in groupedPermissions" :key="resource" class="mb-4">
                                <h6 class="text-primary fw-bold border-bottom pb-2">{{ resource }}</h6>
                                <div class="row row-cols-1 row-cols-md-2 g-2">
                                    <div class="col" v-for="perm in perms" :key="perm.id">
                                        <div class="form-check">
                                            <input class="form-check-input" type="checkbox" :value="perm.id" :id="'perm'+perm.id" v-model="form.permissionIds">
                                            <label class="form-check-label" :for="'perm'+perm.id">
                                                {{ perm.action }} <small class="text-muted">({{ perm.description }})</small>
                                            </label>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="card shadow-sm sticky-top" style="top: 20px;">
                        <div class="card-body">
                            <button type="submit" class="btn btn-primary w-100 mb-2" :disabled="saving">
                                <span v-if="saving" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                                {{ saving ? 'Salvando...' : 'Salvar Perfil' }}
                            </button>
                             <button type="button" class="btn btn-light w-100" @click="router.back()">Cancelar</button>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    </div>
</template>
