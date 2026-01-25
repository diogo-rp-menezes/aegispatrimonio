package br.com.aegispatrimonio.integration;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.service.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
class AlertControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private AtivoRepository ativoRepository;

    @Autowired
    private FilialRepository filialRepository;

    @Autowired
    private TipoAtivoRepository tipoAtivoRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    private Ativo ativo;
    private Alerta alerta;
    private Usuario adminUser;

    @BeforeEach
    void setUp() {
        // Setup Admin User for Mocking
        adminUser = new Usuario();
        adminUser.setRole("ROLE_ADMIN");

        // Dependencies
        Filial filial = new Filial();
        filial.setNome("Filial Teste");
        filial.setCodigo("F001");
        filial.setCnpj("123");
        filial = filialRepository.save(filial);

        TipoAtivo tipo = new TipoAtivo();
        tipo.setNome("Laptop");
        tipo.setIcone("laptop.png");
        tipo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);
        tipo = tipoAtivoRepository.save(tipo);

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome("Dell");
        fornecedor.setCnpj("123");
        fornecedor = fornecedorRepository.save(fornecedor);

        // Ativo
        ativo = new Ativo();
        ativo.setNome("Laptop 01");
        ativo.setNumeroPatrimonio("PAT-001");
        ativo.setFilial(filial);
        ativo.setTipoAtivo(tipo);
        ativo.setFornecedor(fornecedor);
        ativo.setDataAquisicao(LocalDate.now());
        ativo.setValorAquisicao(java.math.BigDecimal.valueOf(1000));
        ativo.setDataRegistro(LocalDate.now());
        ativo = ativoRepository.save(ativo);

        // Alerta
        alerta = new Alerta();
        alerta.setAtivo(ativo);
        alerta.setTipo(TipoAlerta.CRITICO);
        alerta.setTitulo("CPU High");
        alerta.setMensagem("CPU > 90%");
        alerta = alertaRepository.save(alerta);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldListRecentAlertsAsAdmin() throws Exception {
        when(currentUserProvider.getCurrentUsuario()).thenReturn(adminUser);

        mockMvc.perform(get("/api/v1/alerts/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("CPU High"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldMarkAlertAsReadAsAdmin() throws Exception {
        when(currentUserProvider.getCurrentUsuario()).thenReturn(adminUser);

        mockMvc.perform(patch("/api/v1/alerts/" + alerta.getId() + "/read"))
                .andExpect(status().isNoContent());

        assert(alertaRepository.findById(alerta.getId()).get().isLido());
    }
}
