package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.LoginRequestDTO;
import br.com.aegispatrimonio.model.Departamento;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Pessoa;
import br.com.aegispatrimonio.model.Status;
import br.com.aegispatrimonio.model.TipoFilial;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.PessoaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private FilialRepository filialRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Cria o ambiente de teste com um usuário administrador válido
        Filial filial = new Filial();
        filial.setNome("Matriz Teste");
        filial.setCodigo("MATRIZ");
        filial.setTipo(TipoFilial.MATRIZ);
        filial.setCnpj("00.000.000/0001-00");
        filial.setStatus(Status.ATIVO);
        filialRepository.save(filial);

        Departamento depto = new Departamento();
        depto.setNome("TI Teste");
        depto.setFilial(filial);
        departamentoRepository.save(depto);

        Pessoa admin = new Pessoa();
        admin.setNome("Admin Teste");
        admin.setEmail("admin@aegis.com");
        admin.setPassword(passwordEncoder.encode("password")); // Senha correta
        admin.setRole("ROLE_ADMIN");
        admin.setFilial(filial);
        admin.setDepartamento(depto);
        admin.setStatus(Status.ATIVO);
        admin.setCargo("Administrador");
        pessoaRepository.save(admin);
    }

    @Test
    void login_comCredenciaisValidas_deveRetornarTokenJwt() throws Exception {
        // Arrange: Prepara o corpo da requisição de login
        LoginRequestDTO loginRequest = new LoginRequestDTO("admin@aegis.com", "password");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk()) // Espera 200 OK
                .andExpect(jsonPath("$.token").exists()) // Espera que o campo 'token' exista
                .andExpect(jsonPath("$.token", not(emptyString()))); // Espera que o token não seja vazio
    }

    @Test
    void login_comSenhaInvalida_deveRetornarUnauthorized() throws Exception {
        // Arrange: Prepara o corpo da requisição com senha errada
        LoginRequestDTO loginRequest = new LoginRequestDTO("admin@aegis.com", "wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()); // Espera 401 Unauthorized
    }
}
