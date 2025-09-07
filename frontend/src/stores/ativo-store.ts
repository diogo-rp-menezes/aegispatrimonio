// src/stores/ativo-store.ts
import { defineStore } from 'pinia';
import { ref } from 'vue';
import { ativoService } from '@/services/ativo-service';
import type { 
  AtivoRequest, 
  AtivoResponse, 
  PageResponse, 
  Pageable,
  StatusAtivo 
} from '@/types/ativo';

export const useAtivoStore = defineStore('ativo', () => {
  const ativos = ref<AtivoResponse[]>([]);
  const ativoSelecionado = ref<AtivoResponse | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);
  const pagination = ref<PageResponse<AtivoResponse> | null>(null);

  const clearError = () => {
    error.value = null;
  };

  const fetchTodos = async (pageable: Pageable) => {
    loading.value = true;
    clearError();
    try {
      pagination.value = await ativoService.listarTodos(pageable);
      ativos.value = pagination.value.content;
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Erro ao buscar ativos';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const fetchPorId = async (id: number) => {
    loading.value = true;
    clearError();
    try {
      ativoSelecionado.value = await ativoService.buscarPorId(id);
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Erro ao buscar ativo';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const criarAtivo = async (ativo: AtivoRequest) => {
    loading.value = true;
    clearError();
    try {
      const novoAtivo = await ativoService.criar(ativo);
      ativos.value.unshift(novoAtivo);
      return novoAtivo;
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Erro ao criar ativo';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const atualizarAtivo = async (id: number, ativo: AtivoRequest) => {
    loading.value = true;
    clearError();
    try {
      const ativoAtualizado = await ativoService.atualizar(id, ativo);
      const index = ativos.value.findIndex(a => a.id === id);
      if (index !== -1) {
        ativos.value[index] = ativoAtualizado;
      }
      if (ativoSelecionado.value?.id === id) {
        ativoSelecionado.value = ativoAtualizado;
      }
      return ativoAtualizado;
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Erro ao atualizar ativo';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const deletarAtivo = async (id: number) => {
    loading.value = true;
    clearError();
    try {
      await ativoService.deletar(id);
      ativos.value = ativos.value.filter(a => a.id !== id);
      if (ativoSelecionado.value?.id === id) {
        ativoSelecionado.value = null;
      }
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Erro ao deletar ativo';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const fetchPorStatus = async (status: StatusAtivo) => {
    loading.value = true;
    clearError();
    try {
      ativos.value = await ativoService.listarPorStatus(status);
      pagination.value = null;
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Erro ao buscar ativos por status';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const verificarPatrimonio = async (numeroPatrimonio: string): Promise<boolean> => {
    try {
      return await ativoService.verificarNumeroPatrimonio(numeroPatrimonio);
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Erro ao verificar patrimônio';
      throw err;
    }
  };

  return {
    ativos,
    ativoSelecionado,
    loading,
    error,
    pagination,
    clearError,
    fetchTodos,
    fetchPorId,
    criarAtivo,
    atualizarAtivo,
    deletarAtivo,
    fetchPorStatus,
    verificarPatrimonio
  };
});