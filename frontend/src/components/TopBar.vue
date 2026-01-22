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

    <!-- Quick Switcher Modal -->
    <div v-if="showSwitcher" class="switcher-overlay" @click.self="showSwitcher = false">
      <div class="switcher-modal">
        <div class="switcher-header">
           <i class="bi bi-diagram-3 text-primary"></i>
           <h5>Synaptic Switcher</h5>
        </div>
        <div class="switcher-body">
           <input
             ref="switcherInput"
             v-model="switcherSearch"
             class="form-control mb-3"
             placeholder="Buscar projeto/filial..."
             autofocus
           />
           <ul class="list-group">
             <li
               v-for="filial in filteredFiliais"
               :key="filial.id"
               class="list-group-item list-group-item-action d-flex justify-content-between align-items-center"
               @click="selectFilial(filial.id)"
               style="cursor: pointer;"
             >
               <span>{{ filial.nome }}</span>
               <i v-if="currentFilial === filial.id" class="bi bi-check-circle-fill text-success"></i>
             </li>
           </ul>
        </div>
        <div class="switcher-footer">
           <small class="text-muted">Pressione <kbd>Esc</kbd> para fechar</small>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { defineProps, defineEmits, ref, onMounted, computed, nextTick } from "vue";

defineProps({
  isMobile: Boolean
});

defineEmits(['toggle-sidebar']);

const allowedFiliais = ref([]);
const currentFilial = ref(null);
const showSwitcher = ref(false);
const switcherSearch = ref("");
const switcherInput = ref(null);

const filteredFiliais = computed(() => {
    if (!switcherSearch.value) return allowedFiliais.value;
    return allowedFiliais.value.filter(f =>
        f.nome.toLowerCase().includes(switcherSearch.value.toLowerCase())
    );
});

const selectFilial = (id) => {
    currentFilial.value = id;
    localStorage.setItem('currentFilial', id);
    showSwitcher.value = false;
    window.location.reload();
};

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

    // Synaptic Switcher Shortcut
    window.addEventListener('keydown', (e) => {
        if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
            e.preventDefault();
            showSwitcher.value = !showSwitcher.value;
            if (showSwitcher.value) {
                nextTick(() => {
                    switcherInput.value?.focus();
                });
            }
        }
        if (e.key === 'Escape' && showSwitcher.value) {
            showSwitcher.value = false;
        }
    });
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
.switcher-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.5);
    z-index: 9999;
    display: flex;
    justify-content: center;
    align-items: center;
    backdrop-filter: blur(2px);
}
.switcher-modal {
    background: white;
    width: 500px;
    max-width: 90%;
    border-radius: 12px;
    box-shadow: 0 10px 25px rgba(0,0,0,0.2);
    overflow: hidden;
    animation: slideDown 0.2s ease-out;
}
.switcher-header {
    padding: 15px 20px;
    background: #f8f9fa;
    border-bottom: 1px solid #eee;
    display: flex;
    align-items: center;
    gap: 10px;
}
.switcher-header h5 {
    margin: 0;
    font-weight: 600;
}
.switcher-body {
    padding: 20px;
}
.switcher-footer {
    padding: 10px 20px;
    background: #f8f9fa;
    border-top: 1px solid #eee;
    text-align: right;
}
@keyframes slideDown {
    from { transform: translateY(-20px); opacity: 0; }
    to { transform: translateY(0); opacity: 1; }
}
</style>
