// src/services/pessoas.js
import { handleResponse } from './api';

const BASE_URL = '/api/pessoas';

export const pessoaService = {
  // Listar pessoas com filtros
  async listar(filtros = {}) {
    const params = new URLSearchParams();
    
    if (filtros.departamentoId) params.append('departamentoId', filtros.departamentoId);
    if (filtros.nome) params.append('nome', filtros.nome);
    if (filtros.page !== undefined) params.append('page', filtros.page);
    if (filtros.size !== undefined) params.append('size', filtros.size);
    
    const response = await fetch(`${BASE_URL}?${params}`);
    return handleResponse(response);
  },

  // Buscar pessoa por ID
  async buscarPorId(id) {
    const response = await fetch(`${BASE_URL}/${id}`);
    return handleResponse(response);
  },

  // Buscar pessoa por email
  async buscarPorEmail(email) {
    const response = await fetch(`${BASE_URL}/email/${encodeURIComponent(email)}`);
    return handleResponse(response);
  },

  // Criar nova pessoa
  async criar(pessoaData) {
    const response = await fetch(BASE_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(pessoaData),
    });
    return handleResponse(response);
  },

  // Atualizar pessoa
  async atualizar(id, pessoaData) {
    const response = await fetch(`${BASE_URL}/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(pessoaData),
    });
    return handleResponse(response);
  },

  // Deletar pessoa
  async deletar(id) {
    const response = await fetch(`${BASE_URL}/${id}`, {
      method: 'DELETE',
    });
    return handleResponse(response);
  },

  // Buscar estatísticas de pessoas
  async getEstatisticas() {
    const response = await fetch(`${BASE_URL}/estatisticas`);
    return handleResponse(response);
  }
};