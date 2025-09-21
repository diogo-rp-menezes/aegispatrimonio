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
    
    <div class="user-profile">
      <div class="user-avatar">JS</div>
      <div class="user-info">
        <h6>John Smith</h6>
        <small>Administrador</small>
      </div>
    </div>
  </header>
</template>

<script setup>
import { defineProps, defineEmits } from "vue";

defineProps({
  isMobile: Boolean
});

defineEmits(['toggle-sidebar']);
</script>