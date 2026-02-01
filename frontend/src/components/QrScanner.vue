<script setup>
import { onMounted, onUnmounted, ref } from 'vue';
import { Html5QrcodeScanner } from 'html5-qrcode';

const emit = defineEmits(['result', 'error']);

const scanner = ref(null);

function onScanSuccess(decodedText, decodedResult) {
  emit('result', decodedText);
  if (scanner.value) {
    scanner.value.clear();
  }
}

function onScanFailure(error) {
  // handle scan failure, usually better to ignore and keep scanning.
  // emit('error', error);
  // console.warn(`Code scan error = ${error}`);
}

onMounted(() => {
  scanner.value = new Html5QrcodeScanner(
    "reader",
    { fps: 10, qrbox: { width: 250, height: 250 } },
    /* verbose= */ false
  );
  scanner.value.render(onScanSuccess, onScanFailure);
});

onUnmounted(() => {
  if (scanner.value) {
    scanner.value.clear().catch(error => {
        console.error("Failed to clear html5-qrcode scanner. ", error);
    });
  }
});
</script>

<template>
  <div class="qr-scanner-container">
    <div id="reader"></div>
  </div>
</template>

<style scoped>
.qr-scanner-container {
    width: 100%;
    max-width: 600px;
    margin: 0 auto;
}
</style>
