<script setup>
import { useRouter } from 'vue-router';
import QrScanner from '../components/QrScanner.vue';

const router = useRouter();

function handleResult(decodedText) {
  console.log(`Scan result: ${decodedText}`);

  try {
    // Expected format: http://.../ativos/123 or just /ativos/123 or just 123
    // We try to find "/ativos/(\d+)"
    const match = decodedText.match(/\/ativos\/(\d+)/);

    if (match && match[1]) {
      const id = match[1];
      router.push(`/ativos/${id}`);
    } else if (!isNaN(decodedText) && decodedText.trim() !== '') {
       // Maybe just the ID?
       router.push(`/ativos/${decodedText}`);
    } else {
      alert(`Código QR não reconhecido para um ativo: ${decodedText}`);
      location.reload();
    }
  } catch (e) {
    console.error(e);
    alert('Erro ao processar código QR');
    location.reload();
  }
}
</script>

<template>
  <div class="container mt-4">
    <div class="row justify-content-center">
      <div class="col-md-8 text-center">
        <h2 class="mb-4">Leitor de QR Code</h2>
        <p class="text-muted mb-4">Aponte a câmera para o código QR do ativo.</p>

        <div class="card shadow-sm border-0">
          <div class="card-body p-3">
             <QrScanner @result="handleResult" />
          </div>
        </div>

        <button class="btn btn-secondary mt-4" @click="router.back()">
          <i class="bi bi-arrow-left"></i> Voltar
        </button>
      </div>
    </div>
  </div>
</template>
