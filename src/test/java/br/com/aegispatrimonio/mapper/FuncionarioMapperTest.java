package br.com.aegispatrimonio.mapper;

import br.com.aegispatrimonio.dto.FuncionarioDTO;
import br.com.aegispatrimonio.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FuncionarioMapperTest {

    private FuncionarioMapper funcionarioMapper;

    @BeforeEach
    void setUp() {
        funcionarioMapper = new FuncionarioMapper();
    }

    @Test
    @DisplayName("Deve mapear Funcionario completo para FuncionarioDTO")
    void toDTO_deveMapearFuncionarioCompletoParaDTO() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setEmail("joana.dark@aegis.com");
        usuario.setRole("ROLE_ADMIN");

        Departamento depto = new Departamento();
        depto.setNome("Diretoria");

        Filial filial1 = new Filial();
        filial1.setId(1L); // <-- CORREÇÃO: Adicionado ID
        filial1.setNome("Matriz");
        
        Filial filial2 = new Filial();
        filial2.setId(2L); // <-- CORREÇÃO: Adicionado ID diferente
        filial2.setNome("Filial Rio");

        Funcionario funcionario = new Funcionario();
        funcionario.setId(1L);
        funcionario.setNome("Joana Dark");
        funcionario.setMatricula("JD-001");
        funcionario.setCargo("Diretora");
        funcionario.setStatus(Status.ATIVO);
        funcionario.setUsuario(usuario);
        funcionario.setDepartamento(depto);
        funcionario.setFiliais(Set.of(filial1, filial2));

        // Act
        FuncionarioDTO dto = funcionarioMapper.toDTO(funcionario);

        // Assert
        assertNotNull(dto);
        assertEquals(funcionario.getId(), dto.id());
        assertEquals(funcionario.getNome(), dto.nome());
        assertEquals(funcionario.getMatricula(), dto.matricula());
        assertEquals(funcionario.getCargo(), dto.cargo());
        assertEquals(usuario.getEmail(), dto.email());
        assertEquals(usuario.getRole(), dto.role());
        assertEquals(depto.getNome(), dto.departamento());
        assertTrue(dto.filiais().contains("Matriz"));
        assertTrue(dto.filiais().contains("Filial Rio"));
        assertEquals(funcionario.getStatus(), dto.status());
    }

    @Test
    @DisplayName("toDTO deve lidar com entidades relacionadas nulas")
    void toDTO_deveLidarComRelacionadosNulos() {
        // Arrange
        Funcionario funcionario = new Funcionario();
        funcionario.setId(2L);
        funcionario.setNome("Funcionário Fantasma");
        funcionario.setUsuario(null);
        funcionario.setDepartamento(null);
        funcionario.setFiliais(null);
        funcionario.setStatus(Status.INATIVO);

        // Act
        FuncionarioDTO dto = funcionarioMapper.toDTO(funcionario);

        // Assert
        assertNotNull(dto);
        assertNull(dto.email());
        assertNull(dto.role());
        assertNull(dto.departamento());
        assertEquals(Collections.emptySet(), dto.filiais());
        assertEquals(Status.INATIVO, dto.status());
    }

    @Test
    @DisplayName("toDTO deve retornar null quando Funcionario for nulo")
    void toDTO_deveRetornarNullParaFuncionarioNulo() {
        // Act
        FuncionarioDTO dto = funcionarioMapper.toDTO(null);

        // Assert
        assertNull(dto);
    }
}
