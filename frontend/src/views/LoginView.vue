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

      <div class="mt-4">
        <div class="d-flex align-items-center mb-3">
          <hr class="flex-grow-1" />
          <span class="mx-2 text-muted small">ou continue com</span>
          <hr class="flex-grow-1" />
        </div>
        <a :href="googleLoginUrl" class="btn btn-outline-danger w-100 mb-2">
          <i class="bi bi-google me-2"></i> Google
        </a>
        <a :href="githubLoginUrl" class="btn btn-outline-dark w-100">
          <i class="bi bi-github me-2"></i> GitHub
        </a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { fetchConfig, handleResponse, request } from '../services/api';

// Calculate Backend URL (remove /api/v1 suffix if present to get root)
const backendOrigin = fetchConfig.baseURL.includes('/api')
    ? fetchConfig.baseURL.split('/api')[0]
    : fetchConfig.baseURL; // If empty (proxy) or root, keeps as is.

// OAuth2 Endpoints (Standard Spring Security)
const googleLoginUrl = `${backendOrigin}/oauth2/authorization/google`;
const githubLoginUrl = `${backendOrigin}/oauth2/authorization/github`;

const email = ref('');
const password = ref('');
const error = ref('');
const loading = ref(false);
const router = useRouter();
const route = useRoute();

onMounted(async () => {
    // Check for token in URL (OAuth2 redirect)
    const token = route.query.token;
    if (token) {
        localStorage.setItem('authToken', token);
        loading.value = true;
        try {
            // Fetch user context
            const data = await request('/auth/me');
             // Save Roles
            if (data.roles && data.roles.length > 0) {
              localStorage.setItem('userRoles', JSON.stringify(data.roles));
            } else {
              localStorage.removeItem('userRoles');
            }

            // Save Filiais
            if (data.filiais && data.filiais.length > 0) {
                localStorage.setItem('allowedFiliais', JSON.stringify(data.filiais));
                localStorage.setItem('currentFilial', data.filiais[0].id);
            } else {
                localStorage.removeItem('allowedFiliais');
                localStorage.removeItem('currentFilial');
            }
            router.push('/dashboard');
        } catch (e) {
            console.error("Failed to fetch user context", e);
            error.value = "Falha na autenticação via provedor externo.";
            localStorage.removeItem('authToken');
        } finally {
            loading.value = false;
        }
    }
});

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
