// src/services/ativo-service.ts
import { apiClient } from './api-client';
import { 
  AtivoRequest, 
  AtivoResponse, 
  PageResponse, 
  Pageable,
  StatusAtivo 
} from '@/types/ativo';

export class AtivoService {
  private baseUrl = '/ativos';

  async criar(ativo: AtivoRequest): Promise<AtivoResponse> {
    return apiClient.post<AtivoResponse>(this.baseUrl, ativo);
  }

  async buscarPorId(id: number): Promise<AtivoResponse> {
    return apiClient.get<AtivoResponse>(`${this.baseUrl}/${id}`);
  }

  async buscarPorNumeroPatrimonio(numeroPatrimonio: string): Promise<AtivoResponse> {
    return apiClient.get<AtivoResponse>(`${this.baseUrl}/patrimonio/${numeroPatrimonio}`);
  }

  async listarTodos(pageable: Pageable): Promise<PageResponse<AtivoResponse>> {
    const params = {
      page: pageable.page,
      size: pageable.size,
      sort: pageable.sort
    };
    return apiClient.get<PageResponse<AtivoResponse>>(this.baseUrl, { params });
  }

  async listarPorTipo(tipoAtivoId: number): Promise<AtivoResponse[]> {
    return apiClient.get<AtivoResponse[]>(`${this.baseUrl}/tipo/${tipoAtivoId}`);
  }

  async listarPorLocalizacao(localizacaoId: number): Promise<AtivoResponse[]> {
    return apiClient.get<AtivoResponse[]>(`${this.baseUrl}/localizacao/${localizacaoId}`);
  }

  async listarPorStatus(status: StatusAtivo): Promise<AtivoResponse[]> {
    return apiClient.get<AtivoResponse[]>(`${this.baseUrl}/status/${status}`);
  }

  async buscarPorFaixaDeValor(valorMin: number, valorMax: number): Promise<AtivoResponse[]> {
    return apiClient.get<AtivoResponse[]>(`${this.baseUrl}/valor`, {
      params: { valorMin, valorMax }
    });
  }

  async verificarNumeroPatrimonio(numeroPatrimonio: string): Promise<boolean> {
    return apiClient.get<boolean>(`${this.baseUrl}/verificar-patrimonio/${numeroPatrimonio}`);
  }

  async atualizar(id: number, ativo: AtivoRequest): Promise<AtivoResponse> {
    return apiClient.put<AtivoResponse>(`${this.baseUrl}/${id}`, ativo);
  }

  async deletar(id: number): Promise<void> {
    await apiClient.delete<void>(`${this.baseUrl}/${id}`);
  }
}

export const ativoService = new AtivoService();