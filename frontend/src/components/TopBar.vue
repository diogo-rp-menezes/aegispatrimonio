<template>
  <header class="top-bar">
    <!-- Botão menu mobile -->
    <button class="mobile-menu-btn" @click="$emit('toggle-sidebar')" v-if="isMobile">
      <i class="bi bi-list"></i>
    </button>
    
    <div class="search-bar">
      <i class="bi bi-search search-icon"></i>
      <input type="text" class="search-input" placeholder="Pesquisar patrimônio...">
    </div>

    <!-- Filial Selector -->
    <div class="filial-selector ms-3" v-if="allowedFiliais.length > 0">
        <select class="form-select form-select-sm" v-model="currentFilial" @change="onFilialChange">
            <option v-for="filial in allowedFiliais" :key="filial.id" :value="filial.id">
                {{ filial.nome }}
            </option>
        </select>
    </div>
    
    <div class="user-profile ms-3">
      <div class="user-avatar">JS</div>
      <div class="user-info">
        <h6>John Smith</h6>
        <small>Administrador</small>
      </div>
    </div>
  </header>
</template>

<script setup>
import { defineProps, defineEmits, ref, onMounted } from "vue";

defineProps({
  isMobile: Boolean
});

defineEmits(['toggle-sidebar']);

const allowedFiliais = ref([]);
const currentFilial = ref(null);

onMounted(() => {
    const filiaisStr = localStorage.getItem('allowedFiliais');
    if (filiaisStr) {
        try {
            allowedFiliais.value = JSON.parse(filiaisStr);
        } catch (e) {
            console.error("Error parsing allowedFiliais", e);
        }
    }

    const storedFilial = localStorage.getItem('currentFilial');
    if (storedFilial) {
        currentFilial.value = Number(storedFilial);
    } else if (allowedFiliais.value.length > 0) {
        currentFilial.value = allowedFiliais.value[0].id;
        localStorage.setItem('currentFilial', currentFilial.value);
    }
});

const onFilialChange = () => {
    if (currentFilial.value) {
        localStorage.setItem('currentFilial', currentFilial.value);
        // Reload to apply context switch globally
        window.location.reload();
    }
};
</script>

<style scoped>
.filial-selector {
    min-width: 150px;
}
</style>
