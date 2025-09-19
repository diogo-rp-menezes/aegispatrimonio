<template>
  <header class="top-bar">
    <div class="logo">
      <i class="bi bi-shield-check"></i>
      <transition name="fade-slide">
        <span v-if="!collapsed">Aegis Patrimônio</span>
      </transition>
    </div>
    <div class="search-bar">
      <input type="text" placeholder="Pesquisar..." class="search-input" />
      <i class="bi bi-search search-icon"></i>
    </div>
    <div class="actions">
      <button class="btn btn-primary btn-modern">Ação</button>
    </div>
  </header>
</template>

<script setup>
import { defineProps } from "vue";

const props = defineProps({
  collapsed: Boolean
});
</script>
