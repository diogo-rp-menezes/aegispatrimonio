package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.request.MovimentacaoRequestDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import br.com.aegispatrimonio.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager; // Importar EntityManager
import jakarta.persistence.PersistenceContext; // Importar PersistenceContext
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager; // Importar PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional; // Importar Transactional
import org.springframework.transaction.TransactionDefinition; // Importar TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition; // Importar DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionTemplate; // Importar TransactionTemplate

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet; // Importar HashSet
import java.util.Set;
import java.util.UUID; // Importar UUID

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional // Adicionar @Transactional para que o EntityManager funcione corretamente
class MovimentacaoControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired private MovimentacaoRepository movimentacaoRepository;
    @Autowired private AtivoRepository ativoRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private LocalizacaoRepository localizacaoRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private TipoAtivoRepository tipoAtivoRepository;
    @Autowired private FornecedorRepository fornecedorRepository;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PersistenceContext // Injetar EntityManager
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager; // Injetar PlatformTransactionManager

    private String adminToken;
    private Ativo ativo;
    private Funcionario userOrigem;
    private Funcionario userDestino;
    private Localizacao localOrigem;
    private Localizacao localDestino;

    @BeforeEach
    void setUp() {
        // A limpeza de auto-incremento e tabelas de junção é feita em uma transação separada no resetAutoIncrement().
        // As chamadas deleteAll() são removidas para evitar interferência e redundância com TRUNCATE TABLE.

        // Resetar auto-incremento para tabelas relevantes e limpar a tabela de junção em uma transação separada
        resetAutoIncrement();
        entityManager.clear(); // Limpar o contexto de persistência após as operações nativas

        Filial filial = createFilial("Matriz", "MTRZ", "00.000.000/0001-00");
        filial = filialRepository.findById(filial.getId()).orElseThrow(); // Re-fetch para garantir que a entidade esteja fresca no contexto
        Departamento depto = createDepartamento("TI", filial.getId()); // Passar ID da filial
        this.userOrigem = createFuncionarioAndUsuario("User Origem", "origem@aegis.com", "ROLE_USER", depto, Set.of(filial.getId()));
        this.userDestino = createFuncionarioAndUsuario("User Destino", "destino@aegis.com", "ROLE_USER", depto, Set.of(filial.getId()));
        Funcionario adminFunc = createFuncionarioAndUsuario("Admin", "admin@aegis.com", "ROLE_ADMIN", depto, Set.of(filial.getId()));
        this.adminToken = jwtService.generateToken(new CustomUserDetails(adminFunc.getUsuario()));

        this.localOrigem = createLocalizacao("Sala 101", filial.getId()); // Passar ID da filial
        this.localDestino = createLocalizacao("Sala 102", filial.getId()); // Passar ID da filial

        TipoAtivo tipoAtivo = createTipoAtivo("Notebook");
        Fornecedor fornecedor = createFornecedor("Dell", "11.111.111/0001-11");
        this.ativo = createAtivo("Notebook-01", "PAT-001", filial.getId(), tipoAtivo, fornecedor, userOrigem, localOrigem); // Passar ID da filial
    }

    @Test
    @DisplayName("Deve criar e efetivar uma movimentação com sucesso")
    void cicloDeVidaMovimentacao_deveFuncionarCorretamente() throws Exception {
        MovimentacaoRequestDTO request = new MovimentacaoRequestDTO(ativo.getId(), localOrigem.getId(), localDestino.getId(),
                userOrigem.getId(), userDestino.getId(), LocalDate.now(), "Movimentação de teste", "Obs");

        String responseString = mockMvc.perform(post("/api/v1/movimentacoes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("PENDENTE")))
                .andReturn().getResponse().getContentAsString();

        Long movimentacaoId = objectMapper.readTree(responseString).get("id").asLong();

        mockMvc.perform(post("/api/v1/movimentacoes/efetivar/{id}", movimentacaoId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("EFETIVADA")));

        Ativo ativoAtualizado = ativoRepository.findById(ativo.getId()).orElseThrow();
        assertEquals(localDestino.getId(), ativoAtualizado.getLocalizacao().getId());
        assertEquals(userDestino.getId(), ativoAtualizado.getFuncionarioResponsavel().getId());
    }

    // --- Helper Methods ---

    private Filial createFilial(String nome, String codigo, String cnpj) {
        Filial f = new Filial();
        f.setNome(nome); f.setCodigo(codigo); f.setCnpj(cnpj); f.setTipo(TipoFilial.MATRIZ); f.setStatus(Status.ATIVO);
        Filial savedFilial = filialRepository.save(f);
        return savedFilial;
    }

    private Departamento createDepartamento(String nome, Long filialId) { // Alterado para Long filialId
        Departamento d = new Departamento();
        d.setNome(nome);
        d.setFilial(filialRepository.findById(filialId).orElseThrow(() -> new RuntimeException("Filial not found with ID: " + filialId))); // Buscar Filial
        return departamentoRepository.save(d);
    }

    private Funcionario createFuncionarioAndUsuario(String nome, String email, String role, Departamento depto, Set<Long> filialIds) { // Alterado para Set<Long>
        Funcionario func = new Funcionario();
        func.setId(null); // Forçar a entidade a ser tratada como nova para garantir a geração de um novo ID
        func.setNome(nome);
        func.setMatricula(nome.replaceAll("\\s+", "") + "-" + UUID.randomUUID().toString().substring(0, 8)); // Gerar matrícula única
        func.setCargo("Analista");
        func.setDepartamento(depto);

        // Buscar Filiais frescas do banco de dados para garantir que estejam no contexto de persistência atual
        Set<Filial> freshFiliais = new HashSet<>();
        for (Long filialId : filialIds) { // Iterar sobre IDs
            freshFiliais.add(filialRepository.findById(filialId).orElseThrow(() -> new RuntimeException("Filial not found with ID: " + filialId)));
        }
        func.setFiliais(freshFiliais);

        func.setStatus(Status.ATIVO);
        Usuario user = new Usuario();
        user.setEmail(email); user.setPassword(passwordEncoder.encode("password")); user.setRole(role);
        user.setStatus(Status.ATIVO); user.setFuncionario(func); func.setUsuario(user);
        return funcionarioRepository.save(func);
    }

    private Localizacao createLocalizacao(String nome, Long filialId) { // Alterado para Long filialId
        Localizacao loc = new Localizacao();
        loc.setNome(nome);
        loc.setFilial(filialRepository.findById(filialId).orElseThrow(() -> new RuntimeException("Filial not found with ID: " + filialId))); // Buscar Filial
        loc.setStatus(Status.ATIVO);
        return localizacaoRepository.save(loc);
    }

    private TipoAtivo createTipoAtivo(String nome) {
        TipoAtivo tipo = new TipoAtivo();
        tipo.setNome(nome); tipo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO); tipo.setStatus(Status.ATIVO);
        return tipoAtivoRepository.save(tipo);
    }

    private Fornecedor createFornecedor(String nome, String cnpj) {
        Fornecedor f = new Fornecedor();
        f.setNome(nome);
        f.setCnpj(cnpj);
        f.setStatus(StatusFornecedor.ATIVO);
        return fornecedorRepository.save(f);
    }

    private Ativo createAtivo(String nome, String patrimonio, Long filialId, TipoAtivo tipo, Fornecedor fornecedor, Funcionario responsavel, Localizacao local) { // Alterado para Long filialId
        Ativo a = new Ativo();
        a.setNome(nome);
        a.setNumeroPatrimonio(patrimonio);
        a.setFilial(filialRepository.findById(filialId).orElseThrow(() -> new RuntimeException("Filial not found with ID: " + filialId))); // Buscar Filial
        a.setTipoAtivo(tipo);
        a.setFornecedor(fornecedor);
        a.setFuncionarioResponsavel(responsavel);
        a.setLocalizacao(local);
        a.setDataAquisicao(LocalDate.now());
        a.setValorAquisicao(new BigDecimal("3000.00"));
        a.setStatus(StatusAtivo.ATIVO);
        a.setDataRegistro(LocalDate.now());
        return ativoRepository.save(a);
    }

    // Novo método para resetar os contadores de auto-incremento e limpar tabelas de junção
    private void resetAutoIncrement() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); // Sempre iniciar uma nova transação
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager, def);

        transactionTemplate.executeWithoutResult(status -> {
            // Desativar verificações de chave estrangeira temporariamente
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

            // TRUNCATE TABLE e resetar AUTO_INCREMENT explicitamente para entidades principais
            entityManager.createNativeQuery("TRUNCATE TABLE movimentacoes").executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE movimentacoes AUTO_INCREMENT = 1").executeUpdate();

            entityManager.createNativeQuery("TRUNCATE TABLE ativos").executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE ativos AUTO_INCREMENT = 1").executeUpdate();

            entityManager.createNativeQuery("TRUNCATE TABLE usuarios").executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE usuarios AUTO_INCREMENT = 1").executeUpdate();

            entityManager.createNativeQuery("TRUNCATE TABLE funcionarios").executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE funcionarios AUTO_INCREMENT = 1").executeUpdate();

            entityManager.createNativeQuery("TRUNCATE TABLE localizacoes").executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE localizacoes AUTO_INCREMENT = 1").executeUpdate();

            entityManager.createNativeQuery("TRUNCATE TABLE departamentos").executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE departamentos AUTO_INCREMENT = 1").executeUpdate();

            entityManager.createNativeQuery("TRUNCATE TABLE filiais").executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE filiais AUTO_INCREMENT = 1").executeUpdate();

            entityManager.createNativeQuery("TRUNCATE TABLE tipos_ativo").executeUpdate(); // Corrigido o nome da tabela
            entityManager.createNativeQuery("ALTER TABLE tipos_ativo AUTO_INCREMENT = 1").executeUpdate(); // Corrigido o nome da tabela

            entityManager.createNativeQuery("TRUNCATE TABLE fornecedores").executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE fornecedores AUTO_INCREMENT = 1").executeUpdate();

            // TRUNCATE TABLE para tabelas de junção (não possuem AUTO_INCREMENT, mas é bom garantir a limpeza)
            entityManager.createNativeQuery("TRUNCATE TABLE funcionario_filial").executeUpdate();

            // Limpar o contexto de persistência imediatamente após a truncagem (para o EM desta transação)
            entityManager.clear(); 

            // Reativar verificações de chave estrangeira
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
        });
    }
}
