---
apply: always
---

1. PRINCÍPIOS FUNDAMENTAIS DE ORIENTAÇÃO A OBJETOS

```
SEMPRE aplique estes princípios OO:
- ENCAPSULAMENTO: Atributos privados com getters/setters quando necessário
- HERANÇA: Use composição sobre herança quando possível
- POLIMORFISMO: Interfaces e classes abstratas para comportamentos comuns
- ABSTRAÇÃO: Ocultar detalhes de implementação complexos
```

2. PRINCÍPIOS SOLID - IMPLEMENTAÇÃO OBRIGATÓRIA

```typescript
// S - Single Responsibility Principle
// Cada classe deve ter uma única responsabilidade

// O - Open/Closed Principle
// Aberto para extensão, fechado para modificação

// L - Liskov Substitution Principle
// Classes derivadas devem ser substituíveis por suas bases

// I - Interface Segregation Principle
// Múltiplas interfaces específicas são melhores que uma geral

// D - Dependency Inversion Principle
// Dependa de abstrações, não de implementações concretas
```

3. SEGURANÇA DA APLICAÇÃO

```security
CRITICAL SECURITY RULES:
- VALIDAÇÃO: Sempre valide inputs do usuário (sanitize)
- AUTENTICAÇÃO: Implemente auth com tokens JWT seguros
- AUTORIZAÇÃO: RBAC (Role-Based Access Control)
- SQL INJECTION: Use prepared statements/ORM
- XSS: Escape output, use Content Security Policy
- CSRF: Implemente tokens anti-CSRF
- SENHAS: Hash com bcrypt/scrypt (NUNCA plain text)
- CORS: Configure corretamente para APIs
- RATE LIMITING: Proteção contra brute force
- LOGGING: Log de atividades de segurança
```

4. BOAS PRÁTICAS E CÓDIGO LIMPO

```clean-code
CLEAN CODE PRINCIPLES:
- NOMES SIGNIFICATIVOS: Variáveis/funções com nomes descritivos
- FUNÇÕES PEQUENAS: Máximo 20 linhas por função
- DRY (Don't Repeat Yourself): Elimine duplicação
- KISS (Keep It Simple): Simplicidade acima de complexidade
- YAGNI (You Ain't Gonna Need It): Não adicione funcionalidades desnecessárias
- COMMENTS: Comente o "porquê", não o "como"
- CONSISTÊNCIA: Padrões de código consistentes
```

5. PADRÕES DE PROJETO RECOMENDADOS

```design-patterns
PREFIRA estes padrões:
- Repository Pattern (para acesso a dados)
- Factory Pattern (para criação de objetos complexos)
- Strategy Pattern (para algoritimos intercambiáveis)
- Observer Pattern (para eventos e notificações)
- Dependency Injection (para injeção de dependências)
- Builder Pattern (para objetos complexos)
```

6. TRATAMENTO DE ERROS E EXCEÇÕES

```error-handling
ERROR HANDLING RULES:
- USE EXCEPTIONS: Para erros recuperáveis
- LOG ERRORS: Log completo com contexto
- USER-FRIENDLY: Mensagens amigáveis para usuários
- GRACEFUL DEGRADATION: Aplicação não deve quebrar completamente
- VALIDAÇÃO: Fail-fast com validação early
```

7. PERFORMANCE E OTIMIZAÇÃO

```performance
PERFORMANCE GUIDELINES:
- LAZY LOADING: Carregue dados sob demanda
- CACHING: Implemente cache estratégico
- DATABASE: Otimize queries, use índices
- MEMORY: Gerencie recursos adequadamente
- ASYNC: Operações I/O assíncronas quando possível
```

8. TESTES E QUALIDADE

```testing
TESTING REQUIREMENTS:
- TDD: Test-Driven Development quando aplicável
- UNIT TESTS: Cobertura mínima de 80%
- INTEGRATION TESTS: Teste de componentes integrados
- MOCKS: Use mocking para dependências externas
- TEST ISOLATION: Testes independentes e repetíveis
```

9. EXEMPLO DE IMPLEMENTAÇÃO

```typescript
// EXEMPLO DE CLASSE SEGURA E SOLID
interface UserRepository {
  findById(id: string): Promise<User | null>;
  save(user: User): Promise<void>;
}

class UserService {
  constructor(
    private userRepository: UserRepository,
    private passwordHasher: PasswordHasher,
    private validator: InputValidator
  ) {}

  async createUser(userData: CreateUserDto): Promise<User> {
    // Validação
    this.validator.validateUser(userData);

    // Hash da senha
    const hashedPassword = await this.passwordHasher.hash(userData.password);

    // Criação do usuário
    const user = User.create({
      ...userData,
      password: hashedPassword
    });

    // Persistência
    await this.userRepository.save(user);

    return user;
  }
}
```

10. REGRAS DE IMPLEMENTAÇÃO ESPECÍFICAS

```
IMPLEMENTATION RULES:
- SEMPRE use TypeScript/type system
- EVITE any/unknown sem validação adequada
- PREFIRA const/readonly sobre let/var
- USE async/await sobre callbacks
- IMPLEMENTE interfaces explícitas
- DOCUMENTE APIs com OpenAPI/Swagger
- USE environment variables para configurações
- IMPLEMENTE health checks
- CONFIGURE monitoring e alertas
```
br/com/aegispatrimonio/dto/FuncionarioCreateDTO.class
br/com/aegispatrimonio/controller/AuthController.class
br/com/aegispatrimonio/mapper/FuncionarioMapper.class
br/com/aegispatrimonio/dto/healthcheck/MemoriaDTO.class
br/com/aegispatrimonio/model/TipoFilial.class
br/com/aegispatrimonio/controller/LocalizacaoController.class
br/com/aegispatrimonio/exception/RestExceptionHandler.class
br/com/aegispatrimonio/dto/response/MovimentacaoResponseDTO.class
br/com/aegispatrimonio/model/MetodoDepreciacao.class
br/com/aegispatrimonio/repository/DiscoRepository.class
br/com/aegispatrimonio/controller/FuncionarioController.class
br/com/aegispatrimonio/model/Status.class
br/com/aegispatrimonio/security/TenantFilter.class
br/com/aegispatrimonio/model/TipoManutencao.class
br/com/aegispatrimonio/repository/TipoAtivoSpecification.class
br/com/aegispatrimonio/controller/TipoAtivoController.class
br/com/aegispatrimonio/model/Fornecedor.class
br/com/aegispatrimonio/dto/DepartamentoUpdateDTO.class
br/com/aegispatrimonio/controller/AtivoController.class
br/com/aegispatrimonio/controller/MovimentacaoController.class
br/com/aegispatrimonio/context/TenantContext.class
br/com/aegispatrimonio/exception/NotFoundException.class
br/com/aegispatrimonio/dto/FornecedorDTO.class
br/com/aegispatrimonio/exception/DuplicateException.class
br/com/aegispatrimonio/controller/FornecedorController.class
br/com/aegispatrimonio/dto/request/FornecedorRequestDTO.class
br/com/aegispatrimonio/exception/ApplicationControllerAdvice.class
br/com/aegispatrimonio/service/FornecedorService.class
br/com/aegispatrimonio/repository/AtivoRepository.class
br/com/aegispatrimonio/dto/FornecedorUpdateDTO.class
br/com/aegispatrimonio/repository/TipoAtivoRepository.class
br/com/aegispatrimonio/dto/request/ManutencaoInicioDTO.class
br/com/aegispatrimonio/repository/FilialSpecification.class
br/com/aegispatrimonio/dto/FilialDTO.class
br/com/aegispatrimonio/model/Manutencao.class
br/com/aegispatrimonio/dto/LocalizacaoCreateDTO.class
br/com/aegispatrimonio/exception/ResourceNotFoundException.class
br/com/aegispatrimonio/AegispatrimonioApplication.class
br/com/aegispatrimonio/config/CorsConfig.class
br/com/aegispatrimonio/service/AtivoService.class
br/com/aegispatrimonio/dto/healthcheck/AdaptadorRedeDTO.class
br/com/aegispatrimonio/model/Memoria.class
br/com/aegispatrimonio/repository/AtivoDetalheHardwareRepository.class
br/com/aegispatrimonio/dto/TipoAtivoDTO.class
br/com/aegispatrimonio/repository/LocalizacaoRepository.class
br/com/aegispatrimonio/mapper/DepartamentoMapper.class
br/com/aegispatrimonio/model/Usuario.class
br/com/aegispatrimonio/dto/healthcheck/HealthCheckDTO.class
br/com/aegispatrimonio/service/TipoAtivoService.class
br/com/aegispatrimonio/config/SecurityConfig.class
br/com/aegispatrimonio/dto/AtivoDTO.class
br/com/aegispatrimonio/repository/DepartamentoRepository.class
br/com/aegispatrimonio/dto/request/DepartamentoRequestDTO.class
br/com/aegispatrimonio/model/CategoriaContabil.class
br/com/aegispatrimonio/security/SecurityUtil.class
br/com/aegispatrimonio/service/HealthCheckService.class
br/com/aegispatrimonio/dto/LoginResponseDTO.class
br/com/aegispatrimonio/security/JwtService.class
br/com/aegispatrimonio/dto/response/ProjecaoDepreciacaoDTO.class
br/com/aegispatrimonio/model/StatusAtivo.class
br/com/aegispatrimonio/repository/ManutencaoSpecification.class
br/com/aegispatrimonio/dto/LoginRequestDTO.class
br/com/aegispatrimonio/dto/FilialCreateDTO.class
br/com/aegispatrimonio/dto/FuncionarioDTO.class
br/com/aegispatrimonio/dto/LocalizacaoDTO.class
br/com/aegispatrimonio/dto/request/LocalizacaoRequestDTO.class
br/com/aegispatrimonio/exception/SecurityExceptionHandler.class
br/com/aegispatrimonio/mapper/AtivoMapper.class
br/com/aegispatrimonio/service/DepartamentoService.class
br/com/aegispatrimonio/config/JwtSecretValidator.class
br/com/aegispatrimonio/model/Localizacao.class
br/com/aegispatrimonio/model/StatusManutencao.class
br/com/aegispatrimonio/dto/TipoAtivoCreateDTO.class
br/com/aegispatrimonio/model/Disco.class
br/com/aegispatrimonio/dto/FilialUpdateDTO.class
br/com/aegispatrimonio/dto/request/ManutencaoConclusaoDTO.class
br/com/aegispatrimonio/dto/DepartamentoDTO.class
br/com/aegispatrimonio/dto/AtivoCreateDTO.class
br/com/aegispatrimonio/config/DevConfig.class
br/com/aegispatrimonio/config/SwaggerConfig.class
br/com/aegispatrimonio/dto/healthcheck/DiscoDTO.class
br/com/aegispatrimonio/dto/response/TipoAtivoResponseDTO.class
br/com/aegispatrimonio/dto/response/FilialResponseDTO.class
br/com/aegispatrimonio/dto/request/AtivoRequestDTO.class
br/com/aegispatrimonio/security/CustomUserDetailsService.class
br/com/aegispatrimonio/dto/response/LocalizacaoResponseDTO.class
br/com/aegispatrimonio/model/Ativo.class
br/com/aegispatrimonio/dto/response/ManutencaoResponseDTO.class
br/com/aegispatrimonio/mapper/LocalizacaoMapper.class
br/com/aegispatrimonio/controller/DepartamentoController.class
br/com/aegispatrimonio/mapper/HealthCheckMapper.class
br/com/aegispatrimonio/model/TipoAtivo.class
br/com/aegispatrimonio/repository/UsuarioRepository.class
br/com/aegispatrimonio/dto/response/AtivoResponseDTO.class
br/com/aegispatrimonio/model/Departamento.class
br/com/aegispatrimonio/model/Filial.class
br/com/aegispatrimonio/dto/response/PessoaResponseDTO.class
br/com/aegispatrimonio/dto/request/ManutencaoRequestDTO.class
br/com/aegispatrimonio/model/AtivoDetalheHardware.class
br/com/aegispatrimonio/security/JwtAuthFilter.class
br/com/aegispatrimonio/controller/ManutencaoController.class
br/com/aegispatrimonio/controller/FilialController.class
br/com/aegispatrimonio/dto/FornecedorCreateDTO.class
br/com/aegispatrimonio/dto/LocalizacaoUpdateDTO.class
br/com/aegispatrimonio/config/CorsConfig$1.class
br/com/aegispatrimonio/dto/DepartamentoCreateDTO.class
br/com/aegispatrimonio/mapper/TipoAtivoMapper.class
br/com/aegispatrimonio/security/CustomUserDetails.class
br/com/aegispatrimonio/service/ManutencaoService.class
br/com/aegispatrimonio/service/DepreciacaoService.class
br/com/aegispatrimonio/repository/FuncionarioRepository.class
br/com/aegispatrimonio/service/FilialService.class
br/com/aegispatrimonio/repository/MovimentacaoRepository.class
br/com/aegispatrimonio/dto/request/MovimentacaoRequestDTO.class
br/com/aegispatrimonio/dto/request/FilialRequestDTO.class
br/com/aegispatrimonio/mapper/FilialMapper.class
br/com/aegispatrimonio/mapper/FornecedorMapper.class
br/com/aegispatrimonio/dto/response/FornecedorResponseDTO.class
br/com/aegispatrimonio/controller/DepreciacaoController.class
br/com/aegispatrimonio/service/LocalizacaoService.class
br/com/aegispatrimonio/repository/AtivoSpecification.class
br/com/aegispatrimonio/dto/AtivoUpdateDTO.class
br/com/aegispatrimonio/model/StatusMovimentacao.class
br/com/aegispatrimonio/model/StatusFornecedor.class
br/com/aegispatrimonio/model/Movimentacao.class
br/com/aegispatrimonio/dto/FuncionarioUpdateDTO.class
br/com/aegispatrimonio/service/FuncionarioService.class
br/com/aegispatrimonio/dto/request/ManutencaoCancelDTO.class
br/com/aegispatrimonio/service/MovimentacaoService.class
br/com/aegispatrimonio/repository/MemoriaRepository.class
br/com/aegispatrimonio/repository/AdaptadorRedeRepository.class
br/com/aegispatrimonio/dto/request/TipoAtivoRequestDTO.class
br/com/aegispatrimonio/repository/FornecedorRepository.class
br/com/aegispatrimonio/exception/GlobalExceptionHandler.class
br/com/aegispatrimonio/exception/ResourceConflictException.class
br/com/aegispatrimonio/repository/FilialRepository.class
br/com/aegispatrimonio/model/Funcionario.class
br/com/aegispatrimonio/repository/ManutencaoRepository.class
br/com/aegispatrimonio/model/AdaptadorRede.class
br/com/aegispatrimonio/dto/response/DepartamentoResponseDTO.class
