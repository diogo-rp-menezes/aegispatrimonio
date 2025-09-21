// src/services/departamentos.js
import { handleResponse } from './api';

const BASE_URL = '/api/departamentos';

export const departamentoService = {
  // Listar departamentos
  async listar(filtros = {}) {
    const params = new URLSearchParams();
    
    if (filtros.nome) params.append('nome', filtros.nome);
    if (filtros.page !== undefined) params.append('page', filtros.page);
    if (filtros.size !== undefined) params.append('size', filtros.size);
    
    const response = await fetch(`${BASE_URL}?${params}`);
    return handleResponse(response);
  },

  // Buscar departamento por ID
  async buscarPorId(id) {
    const response = await fetch(`${BASE_URL}/${id}`);
    return handleResponse(response);
  },

  // Criar novo departamento
  async criar(departamentoData) {
    const response = await fetch(BASE_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(departamentoData),
    });
    return handleResponse(response);
  },

  // Atualizar departamento
  async atualizar(id, departamentoData) {
    const response = await fetch(`${BASE_URL}/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(departamentoData),
    });
    return handleResponse(response);
  },

  // Deletar departamento
  async deletar(id) {
    const response = await fetch(`${BASE_URL}/${id}`, {
      method: 'DELETE',
    });
    return handleResponse(response);
  }
};