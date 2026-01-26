<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="text-center mb-4">Aegis Patrimônio</h2>
      <form @submit.prevent="handleLogin">
        <div class="mb-3">
          <label for="email" class="form-label">Email</label>
          <input
            type="email"
            class="form-control"
            id="email"
            v-model="email"
            required
            placeholder="seu@email.com"
          />
        </div>
        <div class="mb-3">
          <label for="password" class="form-label">Senha</label>
          <input
            type="password"
            class="form-control"
            id="password"
            v-model="password"
            required
            placeholder="********"
          />
        </div>

        <div v-if="error" class="alert alert-danger" role="alert">
          {{ error }}
        </div>

        <button type="submit" class="btn btn-primary w-100" :disabled="loading">
          {{ loading ? 'Entrando...' : 'Entrar' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { fetchConfig, handleResponse } from '../services/api';

const email = ref('');
const password = ref('');
const error = ref('');
const loading = ref(false);
const router = useRouter();

const handleLogin = async () => {
  loading.value = true;
  error.value = '';

  try {
    const response = await fetch(`${fetchConfig.baseURL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email.value, password: password.value })
    });

    const data = await handleResponse(response);

    // Save Token
    localStorage.setItem('authToken', data.token);

    // Save Roles
    if (data.roles && data.roles.length > 0) {
      localStorage.setItem('userRoles', JSON.stringify(data.roles));
    } else {
      localStorage.removeItem('userRoles');
    }

    // Save Filiais
    if (data.filiais && data.filiais.length > 0) {
        localStorage.setItem('allowedFiliais', JSON.stringify(data.filiais));
        // Always reset currentFilial to the first available one on login to avoid stale context from previous user
        localStorage.setItem('currentFilial', data.filiais[0].id);
    } else {
        localStorage.removeItem('allowedFiliais');
        localStorage.removeItem('currentFilial');
        console.warn("User has no allowed filiais.");
    }

    router.push('/dashboard');
  } catch (err) {
    console.error(err);
    error.value = 'Falha no login. Verifique suas credenciais.';
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f4f6f8;
}

.login-card {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}
</style>
