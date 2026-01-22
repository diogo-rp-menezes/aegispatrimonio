package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.Status;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AtivoControllerWriteSecurityIT extends BaseIT {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private CustomUserDetails buildUser(String role) {
        Usuario u = new Usuario();
        u.setId(role.equals("ROLE_ADMIN") ? 100L : 101L);
        u.setEmail(role.toLowerCase() + "@example.com");
        u.setPassword("secret");
        u.setRole(role);
        u.setStatus(Status.ATIVO);
        // Configura funcionário com uma filial para cumprir políticas em serviços quando necessário
        var func = new br.com.aegispatrimonio.model.Funcionario();
        func.setStatus(Status.ATIVO);
        func.setFiliais(new HashSet<>());
        var filial = new br.com.aegispatrimonio.model.Filial();
        filial.setId(1L);
        func.getFiliais().add(filial);
        u.setFuncionario(func);
        return new CustomUserDetails(u);
    }

    private String buildValidCreateJson() throws Exception {
        // Monta um JSON válido para AtivoCreateDTO; os IDs referenciados podem não existir no banco,
        // portanto esperamos 404 para ADMIN (autorização passa, domínio nega por inexistência).
        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("filialId", 1L);
        payload.put("nome", "Notebook Dell Latitude");
        payload.put("tipoAtivoId", 1L);
        payload.put("numeroPatrimonio", "PAT-TEST-0001");
        payload.put("localizacaoId", 1L);
        payload.put("dataAquisicao", LocalDate.now().toString());
        payload.put("fornecedorId", 1L);
        payload.put("valorAquisicao", new BigDecimal("3500.00"));
        payload.put("funcionarioResponsavelId", 1L);
        payload.put("observacoes", "Criado via teste de integração");
        payload.put("informacoesGarantia", "12 meses");
        return objectMapper.writeValueAsString(payload);
    }

    private String buildValidUpdateJson() throws Exception {
        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("filialId", 1L);
        payload.put("nome", "Notebook Dell Latitude (Atualizado)");
        payload.put("numeroPatrimonio", "PAT-TEST-0001");
        payload.put("tipoAtivoId", 1L);
        payload.put("localizacaoId", 1L);
        payload.put("status", "ATIVO");
        payload.put("dataAquisicao", java.time.LocalDate.now().toString());
        payload.put("fornecedorId", 1L);
        payload.put("valorAquisicao", new BigDecimal("3600.00"));
        payload.put("funcionarioResponsavelId", 1L);
        payload.put("observacoes", "Atualizado via teste");
        payload.put("informacoesGarantia", "24 meses");
        return objectMapper.writeValueAsString(payload);
    }

    @Test
    @DisplayName("POST /api/v1/ativos deve retornar 403 para USER e não-403 para ADMIN (autz)")
    void postAtivo_userForbidden_adminPassesAuthorization() throws Exception {
        String json = buildValidCreateJson();

        // USER deve ser 403 por @PreAuthorize (hasRole('ADMIN') && hasPermission('ATIVO','CREATE'))
        mockMvc.perform(post("/api/v1/ativos")
                        .with(user(buildUser("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

        // ADMIN: autorização deve permitir; como dados referenciam IDs possivelmente inexistentes, aceitamos 4xx diferente de 403
        mockMvc.perform(post("/api/v1/ativos")
                        .with(user(buildUser("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(result -> {
                    int sc = result.getResponse().getStatus();
                    if (sc == 403) {
                        throw new AssertionError("Esperava que ADMIN não fosse 403 em autorização, mas recebeu 403");
                    }
                });
    }

    @Test
    @DisplayName("PUT /api/v1/ativos/{id} deve retornar 403 para USER e não-403 para ADMIN (autz)")
    void putAtivo_userForbidden_adminPassesAuthorization() throws Exception {
        String json = buildValidUpdateJson();
        long inexistenteId = 999999L;

        mockMvc.perform(put("/api/v1/ativos/{id}", inexistenteId)
                        .with(user(buildUser("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/ativos/{id}", inexistenteId)
                        .with(user(buildUser("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(result -> {
                    int sc = result.getResponse().getStatus();
                    if (sc == 403) {
                        throw new AssertionError("Esperava que ADMIN não fosse 403 em autorização, mas recebeu 403");
                    }
                });
    }

    @Test
    @DisplayName("DELETE /api/v1/ativos/{id} deve retornar 403 para USER e não-403 para ADMIN (autz)")
    void deleteAtivo_userForbidden_adminPassesAuthorization() throws Exception {
        long inexistenteId = 888888L;

        mockMvc.perform(delete("/api/v1/ativos/{id}", inexistenteId)
                        .with(user(buildUser("ROLE_USER"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/ativos/{id}", inexistenteId)
                        .with(user(buildUser("ROLE_ADMIN"))))
                .andExpect(result -> {
                    int sc = result.getResponse().getStatus();
                    if (sc == 403) {
                        throw new AssertionError("Esperava que ADMIN não fosse 403 em autorização, mas recebeu 403");
                    }
                });
    }
}
