// src/components/patrimony/AtivoDetails.vue
<template>
  <div class="ativo-details">
    <div class="modal-header">
      <h3>Detalhes do Ativo</h3>
      <button @click="$emit('close')" class="close-btn">×</button>
    </div>
    
    <div class="modal-body">
      <p>Detalhes do ativo em construção...</p>
      <p>Em breve teremos todas as informações!</p>
      
      <div v-if="ativo" class="preview-info">
        <p><strong>Nome:</strong> {{ ativo.nome }}</p>
        <p><strong>Patrimônio:</strong> {{ ativo.numeroPatrimonio }}</p>
      </div>
    </div>

    <div class="modal-footer">
      <button @click="$emit('close')" class="btn-primary">Fechar</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { AtivoResponse } from '@/types/ativo';

const props = defineProps<{
  ativo: AtivoResponse | null;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
}>();
</script>

<style scoped>
.ativo-details {
  background: white;
  border-radius: 8px;
  padding: 0;
  max-width: 600px;
  margin: 0 auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
  min-height: 200px;
  color: #666;
}

.preview-info {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #2563eb;
}

.preview-info p {
  margin: 5px 0;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding: 20px;
  border-top: 1px solid #eee;
}

.btn-primary {
  background: #2563eb;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.btn-primary:hover {
  background: #1d4ed8;
}
</style>