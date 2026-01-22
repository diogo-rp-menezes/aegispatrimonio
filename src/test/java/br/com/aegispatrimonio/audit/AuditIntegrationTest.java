package br.com.aegispatrimonio.audit;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.context.TenantContext;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditIntegrationTest extends BaseIT {

    @Autowired
    private AtivoRepository ativoRepository;
    @Autowired
    private FilialRepository filialRepository;
    @Autowired
    private TipoAtivoRepository tipoAtivoRepository;
    @Autowired
    private FornecedorRepository fornecedorRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private TransactionTemplate transactionTemplate;

    private Filial filial;
    private TipoAtivo tipoAtivo;
    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        // Cleaning up context
        SecurityContextHolder.clearContext();
        TenantContext.clear();

        filial = new Filial();
        filial.setNome("Filial Teste Audit");
        filial.setCodigo("FILAUD");
        filial.setCnpj("99999999000199");
        filial.setTipo(TipoFilial.FILIAL);
        filial.setStatus(Status.ATIVO);
        filialRepository.save(filial);

        tipoAtivo = new TipoAtivo();
        tipoAtivo.setNome("Notebook Audit");
        tipoAtivo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);
        tipoAtivoRepository.save(tipoAtivo);

        fornecedor = new Fornecedor();
        fornecedor.setNome("Fornecedor Audit");
        fornecedor.setCnpj("88888888000188");
        fornecedor.setStatus(StatusFornecedor.ATIVO);
        fornecedorRepository.save(fornecedor);

        // Setup Security Context
        Usuario usuario = new Usuario();
        usuario.setEmail("auditor@aegis.com");
        usuario.setRole("ROLE_ADMIN");
        usuario.setStatus(Status.ATIVO);
        CustomUserDetails userDetails = new CustomUserDetails(usuario);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        // Setup Tenant Context
        TenantContext.setFilialId(filial.getId());
    }

    @Test
    void testAuditLogCreation() {
        Ativo ativo = new Ativo();
        ativo.setNome("Ativo Auditado");
        ativo.setNumeroPatrimonio("PAT-AUDIT-001");
        ativo.setFilial(filial);
        ativo.setTipoAtivo(tipoAtivo);
        ativo.setFornecedor(fornecedor);
        ativo.setDataAquisicao(LocalDate.now());
        ativo.setValorAquisicao(new BigDecimal("5000.00"));

        // Execute save in a transaction
        Long ativoId = transactionTemplate.execute(status -> {
             Ativo saved = ativoRepository.save(ativo);
             return saved.getId();
        });

        // Verify audit in a separate transaction (or same, but Envers writes at commit)
        transactionTemplate.execute(status -> {
            AuditReader auditReader = AuditReaderFactory.get(entityManager);
            List<Number> revisions = auditReader.getRevisions(Ativo.class, ativoId);
            assertThat(revisions).isNotEmpty();

            CustomRevisionEntity revision = auditReader.findRevision(CustomRevisionEntity.class, revisions.get(0));
            assertThat(revision.getUsername()).isEqualTo("auditor@aegis.com");
            assertThat(revision.getFilialId()).isEqualTo(filial.getId());
            return null;
        });
    }
}
