import { request } from './api';

const BASE_URL = '/filiais';

export const filialService = {
  // Listar filiais
  async listar() {
    return request(BASE_URL);
  },

  // Buscar filial por ID
  async buscarPorId(id) {
    return request(`${BASE_URL}/${id}`);
  },

  // Criar nova filial
  async criar(data) {
    return request(BASE_URL, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  // Atualizar filial
  async atualizar(id, data) {
    return request(`${BASE_URL}/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  },

  // Deletar filial
  async deletar(id) {
    return request(`${BASE_URL}/${id}`, {
      method: 'DELETE',
    });
  }
};
