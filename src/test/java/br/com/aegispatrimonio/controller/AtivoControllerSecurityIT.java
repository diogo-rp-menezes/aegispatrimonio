package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import br.com.aegispatrimonio.model.Status;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.security.CustomUserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AtivoControllerSecurityIT extends BaseIT {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/ativos deve retornar 403 para role GUEST (sem permissão READ)")
    @WithMockUser(username = "guest@example.com", roles = {"GUEST"})
    void listarTodos_shouldReturn403_forGuestRole() throws Exception {
        mockMvc.perform(get("/api/v1/ativos")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/ativos deve retornar 200 para role USER (permite READ)")
    void listarTodos_shouldReturn200_forUserRole() throws Exception {
        // Monta um CustomUserDetails compatível com o código de produção
        Usuario u = new Usuario();
        u.setId(1L);
        u.setEmail("user@example.com");
        u.setPassword("secret");
        u.setRole("ROLE_USER");
        u.setStatus(Status.ATIVO);
        // Monta um funcionário com uma filial associada para satisfazer a política do serviço
        br.com.aegispatrimonio.model.Funcionario func = new br.com.aegispatrimonio.model.Funcionario();
        func.setStatus(Status.ATIVO);
        java.util.Set<br.com.aegispatrimonio.model.Filial> filiais = new java.util.HashSet<>();
        br.com.aegispatrimonio.model.Filial filial = new br.com.aegispatrimonio.model.Filial();
        filial.setId(1L);
        filiais.add(filial);
        func.setFiliais(filiais);
        u.setFuncionario(func);
        CustomUserDetails cud = new CustomUserDetails(u);

        mockMvc.perform(get("/api/v1/ativos")
                        .with(user(cud))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/ativos/{id} deve retornar 403 para role GUEST (sem ADMIN/USER)")
    @WithMockUser(username = "guest@example.com", roles = {"GUEST"})
    void buscarPorId_shouldReturn403_forGuestRole() throws Exception {
        mockMvc.perform(get("/api/v1/ativos/{id}", 999)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/ativos/{id} deve permitir USER; se recurso não existir, retornar 404 (autorização passou)")
    void buscarPorId_shouldReturn404_forUserRole_whenNotFound() throws Exception {
        Usuario u = new Usuario();
        u.setId(2L);
        u.setEmail("user2@example.com");
        u.setPassword("secret");
        u.setRole("ROLE_USER");
        u.setStatus(Status.ATIVO);
        br.com.aegispatrimonio.model.Funcionario func = new br.com.aegispatrimonio.model.Funcionario();
        func.setStatus(Status.ATIVO);
        java.util.Set<br.com.aegispatrimonio.model.Filial> filiais = new java.util.HashSet<>();
        br.com.aegispatrimonio.model.Filial filial = new br.com.aegispatrimonio.model.Filial();
        filial.setId(1L);
        filiais.add(filial);
        func.setFiliais(filiais);
        u.setFuncionario(func);
        CustomUserDetails cud = new CustomUserDetails(u);

        // Esperamos 404 pois o recurso pode não existir no banco de teste; isso valida que a autorização permitiu passar
        mockMvc.perform(get("/api/v1/ativos/{id}", 123456)
                        .with(user(cud))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
