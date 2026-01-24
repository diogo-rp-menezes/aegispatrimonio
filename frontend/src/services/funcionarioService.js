// src/services/pessoas.js
import { request } from './api';

const BASE_URL = '/funcionarios';

export const funcionarioService = {
  // Listar pessoas com filtros
  async listar(filtros = {}) {
    const params = new URLSearchParams();
    
    // Note: Backend might not support all these filters yet in listarTodos(),
    // but we pass them just in case or for future implementation.
    // Currently FuncionarioController.listarTodos() takes no arguments.
    // Ideally backend should support filtering.
    
    // if (filtros.departamentoId) params.append('departamentoId', filtros.departamentoId);
    // if (filtros.nome) params.append('nome', filtros.nome);
    // if (filtros.page !== undefined) params.append('page', filtros.page);
    // if (filtros.size !== undefined) params.append('size', filtros.size);

    // For now, standard listing:
    return request(BASE_URL);
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
