// src/services/api.js
import axios from "axios";

// Usa a variável de ambiente do Vite
// Configure no arquivo .env.local → VITE_API_BASE_URL=http://localhost:8080
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
});

// ========== Ativos ==========

// Lista de ativos (paginado e/ou com filtro por nome)
export async function listarAtivos({ nome = "", page = 0, size = 10, sort = "id,asc" } = {}) {
  const response = await api.get("/ativos", {
    params: { nome, page, size, sort },
  });
  return response.data;
}

// Buscar ativo pelo ID
export async function buscarAtivoPorId(id) {
  const response = await api.get(`/ativos/${id}`);
  return response.data;
}

// Buscar ativo por número de patrimônio
export async function buscarAtivoPorPatrimonio(numeroPatrimonio) {
  const response = await api.get(`/ativos/patrimonio/${numeroPatrimonio}`);
  return response.data;
}

// Criar ativo
export async function criarAtivo(ativo) {
  const response = await api.post("/ativos", ativo);
  return response.data;
}

// Atualizar ativo
export async function atualizarAtivo(id, ativo) {
  const response = await api.put(`/ativos/${id}`, ativo);
  return response.data;
}

// Deletar ativo
export async function deletarAtivo(id) {
  const response = await api.delete(`/ativos/${id}`);
  return response.data;
}

export default api;
 
