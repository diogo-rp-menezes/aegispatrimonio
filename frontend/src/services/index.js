// src/services/index.js

// Exportações de API
export { handleResponse, handleApiError, fetchConfig, authInterceptor } from './api';

// Exportações de serviços
export { pessoaService } from './pessoas';
export { departamentoService } from './departamentos';
export { fornecedorService } from './fornecedorService';

// Você pode adicionar outros serviços aqui no futuro
// export { ativoService } from './ativos';
// export { comodatoService } from './comodatos';