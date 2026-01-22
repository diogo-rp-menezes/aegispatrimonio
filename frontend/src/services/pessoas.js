// src/services/pessoas.js
import { request } from './api';

const BASE_URL = '/api/pessoas';

export const pessoaService = {
  // Listar pessoas com filtros
  async listar(filtros = {}) {
    const params = new URLSearchParams();
    
    if (filtros.departamentoId) params.append('departamentoId', filtros.departamentoId);
    if (filtros.nome) params.append('nome', filtros.nome);
    if (filtros.page !== undefined) params.append('page', filtros.page);
    if (filtros.size !== undefined) params.append('size', filtros.size);
    
    return request(`${BASE_URL}?${params}`);
  },

  // Buscar pessoa por ID
  async buscarPorId(id) {
    return request(`${BASE_URL}/${id}`);
  },

  // Buscar pessoa por email
  async buscarPorEmail(email) {
    return request(`${BASE_URL}/email/${encodeURIComponent(email)}`);
  },

  // Criar nova pessoa
  async criar(pessoaData) {
    return request(BASE_URL, {
      method: 'POST',
      body: JSON.stringify(pessoaData),
    });
  },

  // Atualizar pessoa
  async atualizar(id, pessoaData) {
    return request(`${BASE_URL}/${id}`, {
      method: 'PUT',
      body: JSON.stringify(pessoaData),
    });
  },

  // Deletar pessoa
  async deletar(id) {
    return request(`${BASE_URL}/${id}`, {
      method: 'DELETE',
    });
  },

  // Buscar estatísticas de pessoas
  async getEstatisticas() {
    return request(`${BASE_URL}/estatisticas`);
  }
};
