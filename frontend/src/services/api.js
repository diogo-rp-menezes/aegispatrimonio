// src/services/api.js

// Função para tratar respostas HTTP
export const handleResponse = async (response) => {
  if (response.status === 401 || response.status === 403) {
      // Opcional: Redirecionar para login ou limpar storage
      // localStorage.removeItem('authToken');
      // window.location.href = '/login';
  }

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

// Configurações padrão
export const fetchConfig = {
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
};

// Wrapper para fetch que injeta headers automaticamente
export const request = async (endpoint, options = {}) => {
    const token = localStorage.getItem('authToken');
    const filialId = localStorage.getItem('currentFilial');

    const headers = {
        ...options.headers,
    };

    if (!headers['Content-Type'] && !(options.body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
    }

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    if (filialId) {
        headers['X-Filial-ID'] = filialId;
    }

    let url = `${fetchConfig.baseURL}${endpoint}`;

    if (options.params) {
        const query = new URLSearchParams(options.params).toString();
        url += `?${query}`;
    }

    if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData) && !(options.body instanceof URLSearchParams)) {
        options.body = JSON.stringify(options.body);
    }

    const response = await fetch(url, {
        ...options,
        headers
    });

    return handleResponse(response);
};

// Interceptor legado (mantido para compatibilidade se usado em outro lugar, mas request() é preferido)
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
