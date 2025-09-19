<!-- src/views/DetailView.vue -->
<script setup>
import { useRoute } from "vue-router";

const route = useRoute();
</script>

<template>
  <div>
    <h2>Detail View (placeholder)</h2>
    <p>ID: {{ route.params.id }}</p>
  </div>
</template>