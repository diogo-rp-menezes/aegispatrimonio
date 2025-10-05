// src/services/api.js

// Função para tratar respostas HTTP
export const handleResponse = async (response) => {
  if (!response.ok) {
    const error = await response.text();
    throw new Error(error || 'Erro na requisição');
  }
  
  // Se a resposta for 204 (No Content), retorna null
  if (response.status === 204) {
    return null;
  }
  
  return response.json();
};

// Função para lidar com erros de API
export const handleApiError = (error) => {
  console.error('Erro na API:', error);
  throw error;
};

// Configurações padrão para fetch
export const fetchConfig = {
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  headers: {
    'Content-Type': 'application/json',
  },
};

// Interceptor para adicionar token de autenticação (se necessário)
export const authInterceptor = (config) => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers = {
      ...config.headers,
      'Authorization': `Bearer ${token}`
    };
  }
  return config;
};