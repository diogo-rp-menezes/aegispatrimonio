// src/services/pessoas.js
import { request } from './api';

const BASE_URL = '/funcionarios';

export const funcionarioService = {
  // Listar pessoas com filtros
  async listar(filtros = {}) {
    const params = new URLSearchParams();
    
    if (filtros.departamentoId) params.append('departamentoId', filtros.departamentoId);
    if (filtros.nome) params.append('nome', filtros.nome);
    if (filtros.page !== undefined) params.append('page', filtros.page);
    if (filtros.size !== undefined) params.append('size', filtros.size);

    const queryString = params.toString();
    const url = queryString ? `${BASE_URL}?${queryString}` : BASE_URL;

    return request(url);
  },

  // Buscar pessoa por ID
  async buscarPorId(id) {
    return request(`${BASE_URL}/${id}`);
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
  }
};
