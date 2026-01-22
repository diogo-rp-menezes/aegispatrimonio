# Proposta de Refatoração Modular focada em Testabilidade (AtivoController e HealthCheck)

## Objetivos
- Melhorar a testabilidade e a clareza do código, reduzindo acoplamentos e responsabilidades sobrepostas.
- Introduzir pontos de extensão (interfaces) e micro-componentes coesos, facilitando testes de unidade e integração.
- Mitigar riscos observados no fluxo de Health Check (@MapsId + coleções) por meio de separação de responsabilidades.

## Diagnóstico (estado atual)
- AtivoController: fino e adequado (coordenação), porém sem tipagem dedicada para filtros de listagem (parâmetros soltos) e sem limites/validações explícitas de paginação no nível web.
- HealthCheckService: concentra responsabilidades heterogêneas em um único método:
  - Autorização (RBAC + vínculo de filial via usuário/funcionário).
  - Carregamento de agregado (Ativo + DetalheHardware).
  - Estratégia de atualização de escalares (mapper/JPQL).
  - Manutenção de coleções (discos/memórias/adaptadores).
  - Acesso estático ao SecurityContextHolder (difícil de mockar/variar).
  - Logs de debug com System.out (não padronizado; dificulta testes e polui output).
- Repositórios: coerentes, porém a Service conhece detalhes de consultas/updates específicos (updateScalars + deleteByAtivoDetalheHardwareId), misturando regras com acesso a dados.

## Linhas Mestras de Refatoração
1. Extrair métodos privados para melhorar legibilidade e pontos de teste em HealthCheckService.
2. Introduzir interfaces/portas para dependências externas à regra de negócio:
   - CurrentUserProvider (acesso ao usuário autenticado).
   - HealthCheckPersistencePort (operações de persistência do agregado/filhos específicas do caso de uso).
   - AuthorizationPolicy (regra de autorização por role/filial).
3. Dividir classe grande (HealthCheckService) em unidades coesas:
   - HealthCheckAuthorizationPolicy (autorização).
   - HealthCheckCollectionsManager (limpeza/salvamento das coleções).
   - HealthCheckUpdater (atualização de campos escalares do detalhe, via mapper/JPQL).
   - HealthCheckOrchestrator (ou o próprio Service) fica magro, coordenando componentes.
4. Para AtivoController, encapsular parâmetros de filtro em um objeto imutável (query object) e padronizar paginação/validação na camada web.

---

## Sugestões Concretas com Exemplos (antes → depois)

### 1) Isolar o acesso ao usuário logado (testável sem estáticos)

Antes (trecho de HealthCheckService):
```java
private Usuario getUsuarioLogado() {
    CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
            .getContext().getAuthentication().getPrincipal();
    return userDetails.getUsuario();
}
```

Depois (introduzindo CurrentUserProvider):
```java
// contrato simples e mockável
public interface CurrentUserProvider {
    Usuario getCurrentUsuario();
}

// implementação padrão (infra/security)
@Component
class SecurityContextCurrentUserProvider implements CurrentUserProvider {
    @Override
    public Usuario getCurrentUsuario() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return userDetails.getUsuario();
    }
}

// uso em HealthCheckService (injeção via construtor)
public class HealthCheckService {
    private final CurrentUserProvider currentUserProvider;
    public HealthCheckService(..., CurrentUserProvider currentUserProvider) {
        ...
        this.currentUserProvider = currentUserProvider;
    }
    private Usuario getUsuarioLogado() { return currentUserProvider.getCurrentUsuario(); }
}
```
Benefícios: remove dependência estática no service; facilita testes de unidade sem precisar mockar SecurityContextHolder estaticamente.

### 2) Separar Autorização da Orquestração

Antes (misto dentro de updateHealthCheck):
```java
if (!isAdmin(usuarioLogado)) {
    Funcionario func = usuarioLogado.getFuncionario();
    ... valida filial do ativo ...
}
```

Depois (AuthorizationPolicy dedicada):
```java
public interface HealthCheckAuthorizationPolicy {
    void assertCanUpdate(Usuario usuario, Ativo ativo);
}

@Component
class DefaultHealthCheckAuthorizationPolicy implements HealthCheckAuthorizationPolicy {
    private final FuncionarioRepository funcionarioRepository;
    public void assertCanUpdate(Usuario usuario, Ativo ativo) {
        if ("ROLE_ADMIN".equals(usuario.getRole())) return;
        Funcionario principal = usuario.getFuncionario();
        if (principal == null) throw new AccessDeniedException("Usuário não está associado a um funcionário.");
        Funcionario atual = funcionarioRepository.findById(principal.getId())
            .orElseThrow(() -> new AccessDeniedException("Funcionário associado ao usuário não foi encontrado no sistema."));
        boolean permitido = atual.getFiliais().stream().anyMatch(f -> f.getId().equals(ativo.getFilial().getId()));
        if (!permitido) throw new AccessDeniedException("Sem permissão para atualizar ativos desta filial.");
    }
}

// HealthCheckService (orquestra):
policy.assertCanUpdate(usuarioLogado, ativo);
```
Benefícios: autorização testável isoladamente; HealthCheckService torna-se mais simples.

### 3) Extrair atualização de escalares para um componente dedicado

Antes (mistura mapper/JPQL na Service):
```java
if (createdNow) {
   healthCheckMapper.updateEntityFromDto(detalhes, dto);
} else {
   detalheHardwareRepository.updateScalars(...);
}
```

Depois (HealthCheckUpdater):
```java
public interface HealthCheckUpdater {
    void updateScalars(Long ativoId, AtivoDetalheHardware detalhes, HealthCheckDTO dto, boolean createdNow);
}

@Component
class DefaultHealthCheckUpdater implements HealthCheckUpdater {
    private final HealthCheckMapper mapper;
    private final AtivoDetalheHardwareRepository repo;
    public void updateScalars(Long ativoId, AtivoDetalheHardware detalhes, HealthCheckDTO dto, boolean createdNow) {
        if (createdNow) {
            mapper.updateEntityFromDto(detalhes, dto); // dirty checking
        } else {
            repo.updateScalars(ativoId, dto.computerName(), dto.domain(), dto.osName(), dto.osVersion(),
                dto.osArchitecture(), dto.motherboardManufacturer(), dto.motherboardModel(),
                dto.motherboardSerialNumber(), dto.cpuModel(), dto.cpuCores(), dto.cpuThreads());
        }
    }
}
```
Benefícios: a estratégia de atualização pode ser testada e evoluída independentemente.

### 4) Extrair manutenção de coleções (clear + recreate)

Antes (Service chama delete em cada repositório e salva filhos):
```java
discoRepository.deleteByAtivoDetalheHardwareId(id);
memoriaRepository.deleteByAtivoDetalheHardwareId(id);
adaptadorRedeRepository.deleteByAtivoDetalheHardwareId(id);
// mapear e salvar novas listas
```

Depois (CollectionsManager dedicado):
```java
public interface HealthCheckCollectionsManager {
    void replaceCollections(AtivoDetalheHardware detalhes, HealthCheckDTO dto);
}

@Component
class DefaultHealthCheckCollectionsManager implements HealthCheckCollectionsManager {
    private final DiscoRepository discoRepo;
    private final MemoriaRepository memoriaRepo;
    private final AdaptadorRedeRepository adaptadorRepo;
    private final HealthCheckMapper mapper;

    @Override
    @Transactional
    public void replaceCollections(AtivoDetalheHardware detalhes, HealthCheckDTO dto) {
        Long id = detalhes.getId();
        discoRepo.deleteByAtivoDetalheHardwareId(id);
        memoriaRepo.deleteByAtivoDetalheHardwareId(id);
        adaptadorRepo.deleteByAtivoDetalheHardwareId(id);
        if (dto.discos() != null && !dto.discos().isEmpty()) {
            var discos = dto.discos().stream().map(mapper::toEntity).peek(d -> d.setAtivoDetalheHardware(detalhes)).toList();
            discoRepo.saveAll(discos);
        }
        if (dto.memorias() != null && !dto.memorias().isEmpty()) {
            var memorias = dto.memorias().stream().map(mapper::toEntity).peek(m -> m.setAtivoDetalheHardware(detalhes)).toList();
            memoriaRepo.saveAll(memorias);
        }
        if (dto.adaptadoresRede() != null && !dto.adaptadoresRede().isEmpty()) {
            var redes = dto.adaptadoresRede().stream().map(mapper::toEntity).peek(a -> a.setAtivoDetalheHardware(detalhes)).toList();
            adaptadorRepo.saveAll(redes);
        }
    }
}
```
Benefícios: lógica de coleções isolada e facilmente mockável em testes de unidade do serviço principal.

### 5) Orquestração enxuta em HealthCheckService (após divisão)

Antes (método longo e misto).

Depois (estrutura sugerida):
```java
@Service
public class HealthCheckService {
  private final CurrentUserProvider currentUser;
  private final HealthCheckAuthorizationPolicy policy;
  private final HealthCheckUpdater updater;
  private final HealthCheckCollectionsManager collections;
  private final AtivoRepository ativoRepository;
  private final AtivoDetalheHardwareRepository detalheRepo;

  @Transactional
  public void updateHealthCheck(Long ativoId, HealthCheckDTO dto) {
    Usuario usuario = currentUser.getCurrentUsuario();
    Ativo ativo = ativoRepository.findById(ativoId)
        .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com ID: " + ativoId));
    policy.assertCanUpdate(usuario, ativo);

    AtivoDetalheHardware detalhes = detalheRepo.findById(ativoId).orElse(null);
    boolean createdNow = (detalhes == null);
    if (createdNow) {
      detalhes = detalheRepo.saveAndFlush(createDetailsFor(ativo));
    }
    ensureMapsIdIntegrity(ativo, detalhes);
    updater.updateScalars(ativo.getId(), detalhes, dto, createdNow);
    collections.replaceCollections(detalhes, dto);
  }

  // métodos privados extraídos (testáveis via testes de unidade focados)
  private AtivoDetalheHardware createDetailsFor(Ativo ativo) { ... }
  private void ensureMapsIdIntegrity(Ativo a, AtivoDetalheHardware d) { ... }
}
```
Benefícios: cada responsabilidade fica testável de forma isolada; orquestração clara.

### 6) AtivoController: encapsular filtros e paginação

Antes:
```java
@GetMapping
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public List<AtivoDTO> listarTodos(Pageable pageable,
        @RequestParam(required = false) Long filialId,
        @RequestParam(required = false) Long tipoAtivoId,
        @RequestParam(required = false) StatusAtivo status) {
    return ativoService.listarTodos(pageable, filialId, tipoAtivoId, status);
}
```

Depois (Query Object + validação web):
```java
public record AtivoQueryParams(Long filialId, Long tipoAtivoId, StatusAtivo status) {}

@RestController
@RequestMapping("/api/v1/ativos")
@Validated
public class AtivoController {
  private final AtivoService ativoService; // ou IAtivoService (interface)

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  public List<AtivoDTO> listarTodos(@ParameterObject Pageable pageable,
                                    @Valid AtivoQueryParams params) {
    // opcional: impor limites de paginação
    Pageable bounded = PageRequest.of(Math.max(0, pageable.getPageNumber()),
                                      Math.min(100, pageable.getPageSize()),
                                      pageable.getSort());
    return ativoService.listarTodos(bounded, params.filialId(), params.tipoAtivoId(), params.status());
  }
}
```
Benefícios: assinatura do endpoint mais limpa e testável; fácil mockar/validar combinações de filtros; aplica limite de size consistente.

### 7) Introduzir contratos explícitos para Services

Antes: uso direto de implementação concreta (AtivoService, HealthCheckService) nas controllers.

Depois: expor interfaces para contratos estáveis:
```java
public interface IAtivoService {
  List<AtivoDTO> listarTodos(Pageable p, Long filialId, Long tipoAtivoId, StatusAtivo status);
  AtivoDTO buscarPorId(Long id);
  AtivoDTO criar(AtivoCreateDTO dto);
  AtivoDTO atualizar(Long id, AtivoUpdateDTO dto);
  void deletar(Long id);
}

public interface IHealthCheckService {
  void updateHealthCheck(Long ativoId, HealthCheckDTO dto);
}
```
Controllers passam a depender das interfaces, facilitando substituição por dublês em testes de slice web (@WebMvcTest) e fomentando hexagonalidade.

---

## Recomendações de Separação de Concerns
- Controllers: apenas coordenam, validam input (via Bean Validation) e traduzem status HTTP.
- Services/Use Cases: orquestram regras de negócio; sem dependências estáticas; @Transactional no nível orquestrador.
- Policies: encapsulam regras que podem variar (RBAC, vínculos por filial).
- Ports/Repositories: acesso a dados especializado; detalhes de update/JPQL encapsulados longe da orquestração.
- Mappers: mapeiam DTOs ↔ entidades; sem efeitos colaterais (não acessar repositórios).
- Logging: substituir System.out por SLF4J; logs passíveis de asserção em testes unitários quando necessário.

## Plano de Adoção Incremental (mínimo risco)
1. Introduzir CurrentUserProvider e migrar HealthCheckService para usar a interface (ajustar testes unitários para mock). 
2. Extrair HealthCheckAuthorizationPolicy e mover a lógica de autorização para ela.
3. Extrair HealthCheckUpdater e HealthCheckCollectionsManager e delegar no service.
4. Adicionar interfaces IAtivoService/IHealthCheckService e migrar AtivoController para depender delas (sem mudar assinatura externa).
5. Encapsular filtros da listagem de Ativos em AtivoQueryParams e impor limite de paginação.
6. Remover logs System.out e padronizar SLF4J.

Cada passo é pequeno, com testes orientando a mudança (AtivoControllerIT e HealthCheckServiceTest). A criação dos novos componentes pode começar sem alterar comportamento público; os testes existentes devem permanecer verdes a cada passo.

## Impacto em Testes
- HealthCheckServiceTest: deixará de precisar de MockedStatic<SecurityContextHolder>; usar mock de CurrentUserProvider.
- AtivoControllerIT: permanece funcional; Query Object não muda o contrato HTTP (parâmetros continuam sendo query params), mas simplifica validação e permite @Valid no objeto.
- Novos testes unitários: HealthCheckAuthorizationPolicyTest, DefaultHealthCheckUpdaterTest, DefaultHealthCheckCollectionsManagerTest, SecurityContextCurrentUserProviderTest.

## Conclusão
A refatoração propõe modularizar pontos críticos (principalmente em HealthCheckService), introduzindo interfaces e classes coesas, sem alterar contratos REST. O resultado é um código mais testável, com responsabilidades bem definidas e menor acoplamento a detalhes de infraestrutura, endereçando o problema observado e prevenindo regressões futuras.

---

## Status da Proposta: EXECUTADA E CONCLUÍDA
Todas as refatorações propostas neste documento foram implementadas com sucesso, resultando nas melhorias de testabilidade, clareza de código e separação de responsabilidades conforme planejado. Os objetivos foram atingidos.