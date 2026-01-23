import { request } from './api';

const BASE_URL = '/fornecedores';

export const fornecedorService = {
  // Listar fornecedores
  async listar() {
    return request(BASE_URL);
  },

  // Buscar fornecedor por ID
  async buscarPorId(id) {
    return request(`${BASE_URL}/${id}`);
  },

  // Criar novo fornecedor
  async criar(fornecedorData) {
    return request(BASE_URL, {
      method: 'POST',
      body: JSON.stringify(fornecedorData),
    });
  },

  // Atualizar fornecedor
  async atualizar(id, fornecedorData) {
    return request(`${BASE_URL}/${id}`, {
      method: 'PUT',
      body: JSON.stringify(fornecedorData),
    });
  },

  // Deletar fornecedor
  async deletar(id) {
    return request(`${BASE_URL}/${id}`, {
      method: 'DELETE',
    });
  }
};
