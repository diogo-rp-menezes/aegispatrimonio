import { request } from './api';

const BASE_URL = '/manutencoes';

export const manutencaoService = {
  // Listar manutenções com paginação e filtros
  async listar(filters = {}) {
    // Filtros aceitos: ativoId, status, tipo, solicitanteId, fornecedorId, datas...
    // E paginação: page, size, sort
    return request(BASE_URL, {
      method: 'GET',
      params: filters
    });
  },

  // Buscar por ID
  async buscarPorId(id) {
    return request(`${BASE_URL}/${id}`, {
      method: 'GET'
    });
  },

  // Criar nova solicitação
  async criar(dados) {
    return request(BASE_URL, {
      method: 'POST',
      body: dados
    });
  },

  // Aprovar manutenção
  async aprovar(id) {
    return request(`${BASE_URL}/aprovar/${id}`, {
      method: 'POST'
    });
  },

  // Iniciar manutenção
  async iniciar(id, dadosInicio) {
    return request(`${BASE_URL}/iniciar/${id}`, {
      method: 'POST',
      body: dadosInicio // { tecnicoId: ... }
    });
  },

  // Concluir manutenção
  async concluir(id, dadosConclusao) {
    return request(`${BASE_URL}/concluir/${id}`, {
      method: 'POST',
      body: dadosConclusao // { descricaoServico, custoReal, tempoExecucao }
    });
  },

  // Cancelar manutenção
  async cancelar(id, motivo) {
    return request(`${BASE_URL}/cancelar/${id}`, {
      method: 'POST',
      body: { motivo }
    });
  },

  // Deletar manutenção (apenas SOLICITADA)
  async deletar(id) {
    return request(`${BASE_URL}/${id}`, {
      method: 'DELETE'
    });
  },

  // Obter custo total por ativo
  async custoTotalPorAtivo(ativoId) {
    return request(`${BASE_URL}/custo-total`, {
      method: 'GET',
      params: { ativoId }
    });
  }
};
