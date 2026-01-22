Project Guidelines - Aegis Patrimônio
🎯 Visão Geral do Projeto
O Aegis Patrimônio é uma aplicação Spring Boot para gestão de ativos patrimoniais, com arquitetura em camadas (Controller, Service, Repository, Mapper) e foco em segurança, observabilidade e qualidade de código.

🏗️ Estrutura do Projeto
text
src/
├── main/java/br/com/aegispatrimonio/
│   ├── controller/     # REST endpoints com @PreAuthorize
│   ├── service/        # Lógica de negócio e interfaces (I*Service)
│   ├── repository/     # Repositórios JPA
│   ├── mapper/         # Mapeamento DTO-Entity com MapStruct
│   ├── dto/           # Data Transfer Objects (records)
│   ├── model/         # Entidades JPA
│   ├── config/        # Configurações Spring
│   ├── security/      # Configurações de segurança
│   └── exception/     # Tratamento global de erros
└── test/java/br/com/aegispatrimonio/
├── controller/    # Testes de integração com MockMvc
├── service/       # Testes unitários com mocks
└── repository/    # Testes de persistência
🔧 Pré-requisitos de Execução
SEMPRE execute mvn clean verify antes de submeter qualquer solução

GARANTA que todos os testes passam (status GREEN)

VALIDE a cobertura de código ≥80% para camada de serviço

CONFIRME que não há regressões nos testes existentes

🧪 Estratégia de Testes
Testes Unitários (JUnit 5 + Mockito):

Foco em serviços, policies, mappers e componentes isolados

Mock todas as dependências externas

Execute com: mvn test

Testes de Integração (SpringBootTest + MockMvc):

Valide fluxos completos de endpoints

Use @SpringBootTest para testes de integração

Execute com: mvn verify (inclui Failsafe)

Cobertura de Código (JaCoCo):

Meta: ≥80% para serviços e componentes críticos

Relatório gerado em: target/site/jacoco/html/index.html

📝 Padrões de Código Obrigatórios
Convenções de Nomenclatura
Interfaces: Prefixo I (ex: IAtivoService)

Implementações: Sufixo Impl ou Default (ex: AtivoServiceImpl)

DTOs: Sufixo DTO (ex: AtivoDTO, AtivoCreateDTO)

Records: Para DTOs imutáveis e query parameters

Princípios Arquiteturais
java
// Controllers devem ser FINOS
@RestController
public class AtivoController {
private final IAtivoService ativoService; // Injeção por interface

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // RBAC obrigatório
    public List<AtivoDTO> listarTodos(@Valid AtivoQueryParams params) {
        return ativoService.listarTodos(pageable, params.filialId(), params.tipoAtivoId(), params.status());
    }
}

// Services devem seguir SRP
@Service
public class AtivoServiceImpl implements IAtivoService {
@Transactional(readOnly = true) // Transações explícitas
public List<AtivoDTO> listarTodos(Pageable pageable, Long filialId, Long tipoAtivoId, StatusAtivo status) {
// Lógica de negócio aqui
}
}
Segurança e Validação
SEMPRE use @PreAuthorize em endpoints sensíveis

SEMPRE valide DTOs com @Valid + Bean Validation

SEMPRE use logging estruturado (SLF4J) em vez de System.out.println

NUNCA exponha dados sensíveis em logs ou respostas de erro

🔄 Fluxo de Desenvolvimento
Analise os requisitos com base na documentação (ARQUITETURA_PLAN.md, TEST_PLAN.md)

Implemente seguindo os padrões do DEVELOPMENT_MODELS.md

Crie testes conforme TEST_CHECK_STRATEGY.md

Execute mvn clean verify para validar

Corrija quaisquer falhas antes de submeter

📊 Métricas de Qualidade
Cobertura de testes: ≥80%

Complexidade ciclomática: ≤10 por função

Vulnerabilidades de segurança: 0 críticas/altas

Todos os testes: GREEN

Build success rate: 100%

🚨 Cenários Críticos para Validar
RBAC (Admin vs User permissions)

Validação de unicidade (número de patrimônio)

Consistência filial-localização-responsável

Health check com collections (discos, memórias, adaptadores)

Tratamento de erros centralizado (ApplicationControllerAdvice)

📚 Documentação Referência
ARQUITETURA_PLAN.md - Visão arquitetural e estado atual

TEST_PLAN.md - Casos de teste e cenários

DEVELOPMENT_MODELS.md - Padrões de implementação

REFACTORIZATION_PROPOSAL.md - Histórico de refatorações

rules.md - Princípios fundamentais e SOLID