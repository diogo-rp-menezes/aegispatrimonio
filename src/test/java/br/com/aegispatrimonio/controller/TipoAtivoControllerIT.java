package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.TipoAtivoCreateDTO;
import br.com.aegispatrimonio.dto.TipoAtivoDTO;
import br.com.aegispatrimonio.model.CategoriaContabil;
import br.com.aegispatrimonio.model.Pessoa;
import br.com.aegispatrimonio.repository.PessoaRepository;
import br.com.aegispatrimonio.security.CustomUserDetails;
import br.com.aegispatrimonio.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class TipoAtivoControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void setUp() {
        Pessoa admin = new Pessoa();
        admin.setEmail("admin@aegis.com");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole("ROLE_ADMIN");
        pessoaRepository.save(admin);
        adminToken = jwtService.generateToken(new CustomUserDetails(admin));
    }

    @Test
    @DisplayName("Deve listar todos os tipos de ativo e retornar status 200")
    void testListarTodos() throws Exception {
        mockMvc.perform(get("/tipos-ativo").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve buscar um tipo de ativo por ID e retornar status 200")
    void testBuscarPorId() throws Exception {
        // Primeiro, cria um tipo de ativo para garantir que ele exista
        TipoAtivoCreateDTO createDTO = new TipoAtivoCreateDTO("Notebook", CategoriaContabil.IMOBILIZADO);
        String jsonRequest = objectMapper.writeValueAsString(createDTO);

        String response = mockMvc.perform(post("/tipos-ativo")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        TipoAtivoDTO createdDTO = objectMapper.readValue(response, TipoAtivoDTO.class);
        Long id = createdDTO.id();

        // Agora, busca o tipo de ativo criado
        mockMvc.perform(get("/tipos-ativo/{id}", id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Notebook"));
    }
    
    @Test
    @DisplayName("Deve retornar 404 ao buscar um tipo de ativo com ID inexistente")
    void testBuscarPorIdInexistente() throws Exception {
        mockMvc.perform(get("/tipos-ativo/{id}", 999L).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve criar um novo tipo de ativo e retornar status 201")
    void testCriar() throws Exception {
        TipoAtivoCreateDTO createDTO = new TipoAtivoCreateDTO("Monitor", CategoriaContabil.IMOBILIZADO);
        String jsonRequest = objectMapper.writeValueAsString(createDTO);

        mockMvc.perform(post("/tipos-ativo")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Monitor"));
    }

    @Test
    @DisplayName("Deve atualizar um tipo de ativo existente e retornar status 200")
    void testAtualizar() throws Exception {
        // Cria um tipo de ativo
        TipoAtivoCreateDTO createDTO = new TipoAtivoCreateDTO("Cadeira", CategoriaContabil.IMOBILIZADO);
        String createJsonRequest = objectMapper.writeValueAsString(createDTO);

        String response = mockMvc.perform(post("/tipos-ativo")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJsonRequest))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        TipoAtivoDTO createdDTO = objectMapper.readValue(response, TipoAtivoDTO.class);
        Long id = createdDTO.id();

        // Atualiza o tipo de ativo
        TipoAtivoCreateDTO updateDTO = new TipoAtivoCreateDTO("Cadeira Gamer", CategoriaContabil.IMOBILIZADO);
        String updateJsonRequest = objectMapper.writeValueAsString(updateDTO);

        mockMvc.perform(put("/tipos-ativo/{id}", id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Cadeira Gamer"));
    }

    @Test
    @DisplayName("Deve deletar um tipo de ativo e retornar status 204")
    void testDeletar() throws Exception {
        // Cria um tipo de ativo para deletar
        TipoAtivoCreateDTO createDTO = new TipoAtivoCreateDTO("Mesa", CategoriaContabil.IMOBILIZADO);
        String createJsonRequest = objectMapper.writeValueAsString(createDTO);

        String response = mockMvc.perform(post("/tipos-ativo")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJsonRequest))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        TipoAtivoDTO createdDTO = objectMapper.readValue(response, TipoAtivoDTO.class);
        Long id = createdDTO.id();

        // Deleta o tipo de ativo
        mockMvc.perform(delete("/tipos-ativo/{id}", id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verifica se foi deletado
        mockMvc.perform(get("/tipos-ativo/{id}", id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
