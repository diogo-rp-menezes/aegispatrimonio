// src/types/ativo.ts
export enum StatusAtivo {
  ATIVO = 'ATIVO',
  EM_MANUTENCAO = 'EM_MANUTENCAO', 
  INATIVO = 'INATIVO',
  BAIXADO = 'BAIXADO'
}

export enum MetodoDepreciacao {
  LINEAR = 'LINEAR',
  ACELERADA = 'ACELERADA'
}

export interface AtivoRequest {
  nome: string;
  tipoAtivoId: number;
  numeroPatrimonio: string;
  localizacaoId: number;
  status: StatusAtivo;
  dataAquisicao: string;
  fornecedorId: number;
  valorAquisicao: number;
  valorResidual: number;
  vidaUtilMeses?: number;
  metodoDepreciacao?: MetodoDepreciacao;
  dataInicioDepreciacao?: string;
  informacoesGarantia?: string;
  pessoaResponsavelId: number;
  observacoes?: string;
}

export interface AtivoResponse {
  id: number;
  nome: string;
  tipoAtivoId: number;
  tipoAtivoNome: string;
  numeroPatrimonio: string;
  localizacaoId: number;
  localizacaoNome: string;
  status: StatusAtivo;
  dataAquisicao: string;
  fornecedorId: number;
  fornecedorNome: string;
  valorAquisicao: number;
  valorResidual: number;
  vidaUtilMeses?: number;
  metodoDepreciacao?: MetodoDepreciacao;
  dataInicioDepreciacao?: string;
  taxaDepreciacaoMensal?: number;
  depreciacaoAcumulada?: number;
  valorContabilAtual?: number;
  informacoesGarantia?: string;
  pessoaResponsavelId: number;
  pessoaResponsavelNome: string;
  observacoes?: string;
  dataRegistro: string;
  criadoEm: string;
  atualizadoEm: string;
}

export interface Pageable {
  page: number;
  size: number;
  sort?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}