package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.FornecedorCreateDTO;
import br.com.aegispatrimonio.dto.FornecedorUpdateDTO;
import br.com.aegispatrimonio.model.Fornecedor;
import br.com.aegispatrimonio.model.StatusFornecedor;
import br.com.aegispatrimonio.repository.FornecedorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FornecedorControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private EntityManager entityManager;

    private Fornecedor fornecedorAtivo;

    @BeforeEach
    void setUp() {
        Fornecedor novoFornecedor = new Fornecedor();
        novoFornecedor.setNome("Fornecedor Existente");
        novoFornecedor.setCnpj("41238799000136");
        novoFornecedor.setStatus(StatusFornecedor.ATIVO);
        fornecedorAtivo = fornecedorRepository.save(novoFornecedor);
    }

    @Test
    @DisplayName("POST /fornecedores - Deve criar um fornecedor com sucesso para usuário ADMIN")
    @WithMockUser(roles = "ADMIN")
    void criar_comAdmin_deveRetornarCreated() throws Exception {
        FornecedorCreateDTO createDTO = new FornecedorCreateDTO("Novo Fornecedor", "08969361000152", "Endereço", "Contato", "contato@novo.com", "987654321", "Obs Nova");

        mockMvc.perform(post("/fornecedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Novo Fornecedor")))
                .andExpect(jsonPath("$.cnpj", is("08969361000152")));
    }

    @Test
    @DisplayName("DELETE /fornecedores/{id} - Deve deletar (soft delete) o fornecedor com sucesso para usuário ADMIN")
    @WithMockUser(roles = "ADMIN")
    void deletar_comAdmin_deveMoverParaInativo() throws Exception {
        mockMvc.perform(delete("/fornecedores/{id}", fornecedorAtivo.getId()))
                .andExpect(status().isNoContent());

        String statusAposDelete = (String) entityManager.createNativeQuery(
                "SELECT status FROM fornecedores WHERE id = :id")
                .setParameter("id", fornecedorAtivo.getId())
                .getSingleResult();

        assertEquals("INATIVO", statusAposDelete);
    }

    @Test
    @DisplayName("POST /fornecedores - Deve retornar Forbidden para usuário USER")
    @WithMockUser(roles = "USER")
    void criar_comUser_deveRetornarForbidden() throws Exception {
        FornecedorCreateDTO createDTO = new FornecedorCreateDTO("Novo Fornecedor User", "15302930000177", "Endereço", "Contato", "contato@novo.com", "987654321", "Obs Nova");

        mockMvc.perform(post("/fornecedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /fornecedores - Deve retornar Bad Request para CNPJ duplicado")
    @WithMockUser(roles = "ADMIN")
    void criar_comCnpjDuplicado_deveRetornarBadRequest() throws Exception {
        FornecedorCreateDTO createDTO = new FornecedorCreateDTO("Novo Fornecedor Duplicado", fornecedorAtivo.getCnpj(), "Endereço Duplicado", null, null, null, null);

        mockMvc.perform(post("/fornecedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /fornecedores/{id} - Deve encontrar o fornecedor pelo ID")
    @WithMockUser(roles = "USER")
    void buscarPorId_deveRetornarFornecedor() throws Exception {
        mockMvc.perform(get("/fornecedores/{id}", fornecedorAtivo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(fornecedorAtivo.getId().intValue())))
                .andExpect(jsonPath("$.nome", is(fornecedorAtivo.getNome())));
    }

    @Test
    @DisplayName("GET /fornecedores/{id} - Deve retornar Not Found para ID inexistente")
    @WithMockUser(roles = "USER")
    void buscarPorId_comIdInexistente_deveRetornarNotFound() throws Exception {
        mockMvc.perform(get("/fornecedores/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /fornecedores/{id} - Deve atualizar o fornecedor com sucesso para usuário ADMIN")
    @WithMockUser(roles = "ADMIN")
    void atualizar_comAdmin_deveRetornarOk() throws Exception {
        FornecedorUpdateDTO updateDTO = new FornecedorUpdateDTO("Fornecedor Atualizado", "28803082000121", "Endereço Novo", "Contato Update", "contato@update.com", "112233445", "Obs Update", StatusFornecedor.INATIVO);

        mockMvc.perform(put("/fornecedores/{id}", fornecedorAtivo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Fornecedor Atualizado")))
                .andExpect(jsonPath("$.status", is("INATIVO")));
    }

    @Test
    @DisplayName("DELETE /fornecedores/{id} - Deve retornar Forbidden para usuário USER")
    @WithMockUser(roles = "USER")
    void deletar_comUser_deveRetornarForbidden() throws Exception {
        mockMvc.perform(delete("/fornecedores/{id}", fornecedorAtivo.getId()))
                .andExpect(status().isForbidden());
    }
}
