package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashSet;

import static org.mockito.Mockito.*;

@Transactional
class UserContextServicePerformanceTest extends BaseIT {

    @Autowired
    private UserContextService userContextService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @SpyBean
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private FilialRepository filialRepository;

    private Usuario testUser;
    private Funcionario testFuncionario;

    @BeforeEach
    void setUp() {
        // Mock Request Context
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        // Setup dependencies
        Filial filial = new Filial();
        filial.setNome("Headquarters");
        filial.setCodigo("HQ001");
        filial.setCnpj("12.345.678/0001-90");
        filial.setEndereco("123 Main St");
        filial.setTipo(TipoFilial.MATRIZ);
        filialRepository.save(filial);

        Departamento dep = new Departamento();
        dep.setNome("IT");
        dep.setFilial(filial);
        departamentoRepository.save(dep);

        testFuncionario = new Funcionario();
        testFuncionario.setNome("John Doe");
        testFuncionario.setMatricula("12345");
        testFuncionario.setCargo("Developer");
        testFuncionario.setDepartamento(dep);
        testFuncionario.setFiliais(new HashSet<>());
        testFuncionario.getFiliais().add(filial);
        funcionarioRepository.save(testFuncionario);

        testUser = new Usuario();
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("password");
        testUser.setRole("ROLE_USER");
        testUser.setStatus(Status.ATIVO);
        testUser.setFuncionario(testFuncionario);
        usuarioRepository.save(testUser);

        when(currentUserProvider.getCurrentUsuario()).thenReturn(testUser);
    }

    @Test
    void testGetCurrentFuncionarioPerformance() {
        // First call
        userContextService.getCurrentFuncionario();

        // Second call
        userContextService.getCurrentFuncionario();

        // Expect findByIdWithFiliais to be called ONCE (after optimization)
        verify(funcionarioRepository, times(1)).findByIdWithFiliais(testFuncionario.getId());
    }
}
