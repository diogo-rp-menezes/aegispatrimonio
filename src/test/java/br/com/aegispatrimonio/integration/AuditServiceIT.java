package br.com.aegispatrimonio.integration;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.context.TenantContext;
import br.com.aegispatrimonio.dto.AtivoDTO;
import br.com.aegispatrimonio.dto.EntityRevisionDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import br.com.aegispatrimonio.service.AuditService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static br.com.aegispatrimonio.model.StatusFornecedor.ATIVO;
import static org.assertj.core.api.Assertions.assertThat;

public class AuditServiceIT extends BaseIT {

    @Autowired private AuditService auditService;
    @Autowired private AtivoRepository ativoRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private TipoAtivoRepository tipoAtivoRepository;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private TransactionTemplate transactionTemplate;

    private Usuario usuarioAdmin;
    private Filial filial;
    private TipoAtivo tipoAtivo;
    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        // Setup infrastructure
        this.filial = createFilial("Filial Audit", "FL-AUD", "00000000000202");
        this.fornecedor = createFornecedor("Audit Corp", "22222222000222");
        this.usuarioAdmin = createUsuario("audit@tech.com", "ROLE_ADMIN");
        this.tipoAtivo = createTipoAtivo("Laptop");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void shouldRetrieveAtivoHistoryWithTenancyIsolation() {
        // Execute in a transaction template to commit each step if needed,
        // but simple flush often works for Envers if correctly configured.
        // However, Envers usually writes at commit.
        // Let's use transactionTemplate to force commits for the setup/update phases
        // so the AuditReader (which reads from DB) can see them.

        Long ativoId = transactionTemplate.execute(status -> {
            mockLogin(usuarioAdmin);
            TenantContext.setFilialId(filial.getId());

            Ativo ativo = new Ativo();
            ativo.setNome("MacBook Pro");
            ativo.setNumeroPatrimonio("MBP-001");
            ativo.setFilial(filial);
            ativo.setTipoAtivo(tipoAtivo);
            ativo.setFornecedor(fornecedor);
            ativo.setValorAquisicao(new BigDecimal("10000.00"));
            ativo.setDataAquisicao(LocalDate.now());
            ativo.setDataRegistro(LocalDate.now());
            ativo.setStatus(StatusAtivo.ATIVO);

            ativo = ativoRepository.save(ativo);
            return ativo.getId();
        });

        // Update in a separate transaction (Revision 2)
        transactionTemplate.execute(status -> {
            mockLogin(usuarioAdmin);
            TenantContext.setFilialId(filial.getId());

            Ativo ativo = ativoRepository.findById(ativoId).orElseThrow();
            ativo.setValorAquisicao(new BigDecimal("12000.00")); // Price increase
            ativoRepository.save(ativo);
            return null;
        });

        // Now query history
        transactionTemplate.execute(status -> {
            mockLogin(usuarioAdmin);
            TenantContext.setFilialId(filial.getId());

            List<EntityRevisionDTO<AtivoDTO>> history = auditService.getAtivoHistory(ativoId);

            assertThat(history).hasSize(2);

            // First revision
            EntityRevisionDTO<AtivoDTO> rev1 = history.get(0);
            assertThat(rev1.entity().valorAquisicao()).isEqualByComparingTo("10000.00");
            assertThat(rev1.revision().revisionType()).isEqualTo("ADD");
            assertThat(rev1.revision().username()).isEqualTo("audit@tech.com");

            // Second revision
            EntityRevisionDTO<AtivoDTO> rev2 = history.get(1);
            assertThat(rev2.entity().valorAquisicao()).isEqualByComparingTo("12000.00");
            assertThat(rev2.revision().revisionType()).isEqualTo("MOD");

            return null;
        });

        // Verify Tenancy Isolation (Other Tenant)
        transactionTemplate.execute(status -> {
            // Simulate another tenant
            TenantContext.setFilialId(999L);

            List<EntityRevisionDTO<AtivoDTO>> history = auditService.getAtivoHistory(ativoId);

            assertThat(history).isEmpty();
            return null;
        });
    }

    // Helpers
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

    private Usuario createUsuario(String email, String role) {
        Usuario u = new Usuario();
        u.setEmail(email);
        u.setPassword("password");
        u.setRole(role);
        u.setStatus(Status.ATIVO);
        return usuarioRepository.save(u);
    }

    private TipoAtivo createTipoAtivo(String nome) {
        TipoAtivo t = new TipoAtivo();
        t.setNome(nome);
        t.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);
        t.setStatus(Status.ATIVO);
        return tipoAtivoRepository.save(t);
    }

    private void mockLogin(Usuario usuario) {
        CustomUserDetails principal = new CustomUserDetails(usuario);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
