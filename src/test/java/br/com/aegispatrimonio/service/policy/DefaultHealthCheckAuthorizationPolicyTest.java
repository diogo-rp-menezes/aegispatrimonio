package br.com.aegispatrimonio.service.policy;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Funcionario;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.FuncionarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultHealthCheckAuthorizationPolicyTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @InjectMocks
    private DefaultHealthCheckAuthorizationPolicy policy;

    private Usuario adminUser;
    private Usuario regularUserFilialA;
    private Usuario regularUserFilialB;
    private Usuario userWithoutFuncionario;
    private Usuario userWithNonExistentFuncionario;
    private Usuario userWithFuncionarioNoFiliais;
    private Ativo ativoFilialA;
    private Ativo ativoFilialB;
    private Filial filialA;
    private Filial filialB;
    private Funcionario funcionarioFilialA;
    private Funcionario funcionarioFilialB;

    @BeforeEach
    void setUp() {
        filialA = new Filial();
        filialA.setId(1L);
        filialA.setNome("Filial A");

        filialB = new Filial();
        filialB.setId(2L);
        filialB.setNome("Filial B");

        ativoFilialA = new Ativo();
        ativoFilialA.setId(100L);
        ativoFilialA.setFilial(filialA);

        ativoFilialB = new Ativo();
        ativoFilialB.setId(101L);
        ativoFilialB.setFilial(filialB);

        // Admin User
        adminUser = new Usuario();
        adminUser.setRole("ROLE_ADMIN");
        adminUser.setFuncionario(new Funcionario()); // Pode ter um funcionário, mas a role ADMIN ignora

        // Regular User for Filial A
        funcionarioFilialA = new Funcionario();
        funcionarioFilialA.setId(1L);
        funcionarioFilialA.setFiliais(Set.of(filialA));
        regularUserFilialA = new Usuario();
        regularUserFilialA.setRole("ROLE_USER");
        regularUserFilialA.setFuncionario(funcionarioFilialA);

        // Regular User for Filial B
        funcionarioFilialB = new Funcionario();
        funcionarioFilialB.setId(2L);
        funcionarioFilialB.setFiliais(Set.of(filialB));
        regularUserFilialB = new Usuario();
        regularUserFilialB.setRole("ROLE_USER");
        regularUserFilialB.setFuncionario(funcionarioFilialB);

        // User without Funcionario
        userWithoutFuncionario = new Usuario();
        userWithoutFuncionario.setRole("ROLE_USER");
        userWithoutFuncionario.setFuncionario(null);

        // User with non-existent Funcionario
        Funcionario nonExistentFunc = new Funcionario();
        nonExistentFunc.setId(99L);
        userWithNonExistentFuncionario = new Usuario();
        userWithNonExistentFuncionario.setRole("ROLE_USER");
        userWithNonExistentFuncionario.setFuncionario(nonExistentFunc);

        // User with Funcionario but no associated Filiais
        Funcionario funcNoFiliais = new Funcionario();
        funcNoFiliais.setId(3L);
        funcNoFiliais.setFiliais(Collections.emptySet());
        userWithFuncionarioNoFiliais = new Usuario();
        userWithFuncionarioNoFiliais.setRole("ROLE_USER");
        userWithFuncionarioNoFiliais.setFuncionario(funcNoFiliais);
    }

    @Test
    @DisplayName("ADMIN user should be able to update any asset")
    void assertCanUpdate_adminUser_shouldNotThrowException() {
        assertDoesNotThrow(() -> policy.assertCanUpdate(adminUser, ativoFilialA));
        assertDoesNotThrow(() -> policy.assertCanUpdate(adminUser, ativoFilialB));
    }

    @Test
    @DisplayName("Regular user with access to asset's filial should be able to update")
    void assertCanUpdate_regularUserWithAccess_shouldNotThrowException() {
        when(funcionarioRepository.findById(funcionarioFilialA.getId())).thenReturn(Optional.of(funcionarioFilialA));
        assertDoesNotThrow(() -> policy.assertCanUpdate(regularUserFilialA, ativoFilialA));
    }

    @Test
    @DisplayName("Regular user without access to asset's filial should throw AccessDeniedException")
    void assertCanUpdate_regularUserWithoutAccess_shouldThrowAccessDeniedException() {
        when(funcionarioRepository.findById(funcionarioFilialA.getId())).thenReturn(Optional.of(funcionarioFilialA));
        assertThrows(AccessDeniedException.class, () -> policy.assertCanUpdate(regularUserFilialA, ativoFilialB));
    }

    @Test
    @DisplayName("User without an associated Funcionario should throw AccessDeniedException")
    void assertCanUpdate_userWithoutFuncionario_shouldThrowAccessDeniedException() {
        assertThrows(AccessDeniedException.class, () -> policy.assertCanUpdate(userWithoutFuncionario, ativoFilialA));
    }

    @Test
    @DisplayName("User with a non-existent Funcionario should throw AccessDeniedException")
    void assertCanUpdate_userWithNonExistentFuncionario_shouldThrowAccessDeniedException() {
        when(funcionarioRepository.findById(userWithNonExistentFuncionario.getFuncionario().getId())).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () -> policy.assertCanUpdate(userWithNonExistentFuncionario, ativoFilialA));
    }

    @Test
    @DisplayName("User with Funcionario but no associated filiais should throw AccessDeniedException")
    void assertCanUpdate_userWithFuncionarioNoFiliais_shouldThrowAccessDeniedException() {
        when(funcionarioRepository.findById(userWithFuncionarioNoFiliais.getFuncionario().getId())).thenReturn(Optional.of(userWithFuncionarioNoFiliais.getFuncionario()));
        assertThrows(AccessDeniedException.class, () -> policy.assertCanUpdate(userWithFuncionarioNoFiliais, ativoFilialA));
    }
}
