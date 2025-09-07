// src/components/patrimony/AtivoForm.vue
<template>
  <div class="ativo-form">
    <div class="modal-header">
      <h3>{{ editingAtivo ? 'Editar' : 'Novo' }} Ativo</h3>
      <button @click="$emit('close')" class="close-btn">×</button>
    </div>
    
    <div class="modal-body">
      <p>Formulário de ativo em construção...</p>
      <p>Em breve teremos todos os campos!</p>
    </div>

    <div class="modal-footer">
      <button @click="$emit('close')" class="btn-secondary">Cancelar</button>
      <button @click="handleSave" class="btn-primary">Salvar</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import type { AtivoResponse } from '@/types/ativo';

const props = defineProps<{
  ativo?: AtivoResponse | null;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'saved'): void;
}>();

const editingAtivo = ref(props.ativo);

const handleSave = () => {
  console.log('Salvando ativo...');
  emit('saved');
  emit('close');
};
</script>

<style scoped>
.ativo-form {
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
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #666;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
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

.btn-secondary {
  background: #6b7280;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.btn-primary:hover {
  background: #1d4ed8;
}

.btn-secondary:hover {
  background: #4b5563;
}
</style>