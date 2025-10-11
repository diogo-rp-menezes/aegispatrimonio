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
