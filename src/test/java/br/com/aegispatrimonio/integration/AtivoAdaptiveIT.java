package br.com.aegispatrimonio.integration;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.TipoAtivoCreateDTO;
import br.com.aegispatrimonio.dto.TipoAtivoDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static br.com.aegispatrimonio.model.StatusFornecedor.ATIVO;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
public class AtivoAdaptiveIT extends BaseIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private FilialRepository filialRepository;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PermissionRepository permissionRepository;

    private Usuario usuarioAdmin;
    private Filial filial;
    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        this.filial = createFilial("Filial Tech", "FL-TEC", "01000000000101");
        this.fornecedor = createFornecedor("Tech Corp", "11111111000111");
        this.usuarioAdmin = createUsuario("admin@tech.com", "ROLE_ADMIN");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateAtivoWithDynamicAttributes() throws Exception {
        mockLogin(usuarioAdmin);

        // 1. Create TipoAtivo with Schema
        String schema = "{\"type\": \"object\", \"properties\": {\"cor\": {\"type\": \"string\"}}}";
        TipoAtivoCreateDTO tipoDTO = new TipoAtivoCreateDTO("Cadeira Gamer", CategoriaContabil.IMOBILIZADO, schema);

        String tipoResponse = mockMvc.perform(post("/api/v1/tipos-ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tipoDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        TipoAtivoDTO tipoCriado = objectMapper.readValue(tipoResponse, TipoAtivoDTO.class);

        // 2. Create Ativo with Attributes
        Map<String, Object> atributos = new HashMap<>();
        atributos.put("cor", "Vermelho");
        atributos.put("peso", 15.5);
        atributos.put("rgb", true);

        AtivoCreateDTO ativoDTO = new AtivoCreateDTO(
                filial.getId(), "Cadeira XPTO", tipoCriado.id(), "PAT-0099", null,
                LocalDate.now(), fornecedor.getId(), new BigDecimal("1200.00"),
                null, "Obs", "Garantia Vitalicia", null, atributos
        );

        mockMvc.perform(post("/api/v1/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativoDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.atributos.cor", is("Vermelho")))
                .andExpect(jsonPath("$.atributos.peso", is(15.5)))
                .andExpect(jsonPath("$.atributos.rgb", is(true)));
    }

    private Filial createFilial(String nome, String codigo, String cnpj) {
        Filial filial = new Filial();
        filial.setNome(nome);
        filial.setCodigo(codigo);
        filial.setTipo(TipoFilial.FILIAL);
        filial.setCnpj(cnpj);
        filial.setStatus(Status.ATIVO);
        return filialRepository.save(filial);
    }

    private Fornecedor createFornecedor(String nome, String cnpj) {
        Fornecedor f = new Fornecedor();
        f.setNome(nome);
        f.setCnpj(cnpj);
        f.setStatus(ATIVO);
        return fornecedorRepository.save(f);
    }

    private Usuario createUsuario(String email, String roleName) {
        Usuario u = new Usuario();
        u.setEmail(email);
        u.setPassword("password");
        u.setRole(roleName);
        u.setStatus(Status.ATIVO);

        Role rbacRole = roleRepository.findByName(roleName).orElseGet(() -> {
            Role r = new Role();
            r.setName(roleName);
            return roleRepository.save(r);
        });

        // Grant CREATE permission for Admin explicitly if needed by Granular RBAC
        // Assuming Admin bypass or has all permissions. If not, we might need to add it.
        // For now, let's just assign the Role.
        // Ideally, PermissionService should allow ROLE_ADMIN to bypass.
        // If not, we need to add permissions.
        if ("ROLE_ADMIN".equals(roleName)) {
             // Ensure ADMIN has ATIVO:CREATE
             Permission pCreate = permissionRepository.findByResourceAndAction("ATIVO", "CREATE")
                .orElseGet(() -> permissionRepository.save(new Permission(null, "ATIVO", "CREATE", "Create Ativo", "filialId")));

             if (rbacRole.getPermissions() == null) {
                 rbacRole.setPermissions(new java.util.HashSet<>());
             }
             rbacRole.getPermissions().add(pCreate);
             rbacRole = roleRepository.save(rbacRole);
        }

        u.setRoles(java.util.Set.of(rbacRole));

        return usuarioRepository.save(u);
    }

    private void mockLogin(Usuario usuario) {
        CustomUserDetails principal = new CustomUserDetails(usuario);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
