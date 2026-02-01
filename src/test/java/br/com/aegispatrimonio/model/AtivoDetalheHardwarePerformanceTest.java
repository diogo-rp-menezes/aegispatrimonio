package br.com.aegispatrimonio.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.properties.hibernate.hbm2ddl.auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class AtivoDetalheHardwarePerformanceTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void testAtivoFetchingBehavior() {
        // 1. Setup Dependencies
        Filial filial = new Filial();
        filial.setNome("Filial Teste");
        filial.setCodigo("FIL01");
        filial.setCnpj("12.345.678/0001-90");
        filial.setTipo(TipoFilial.MATRIZ);
        filial.setStatus(Status.ATIVO);
        entityManager.persist(filial);

        TipoAtivo tipoAtivo = new TipoAtivo();
        tipoAtivo.setNome("Notebook");
        tipoAtivo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);
        tipoAtivo.setStatus(Status.ATIVO);
        entityManager.persist(tipoAtivo);

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome("Fornecedor Teste");
        fornecedor.setCnpj("98.765.432/0001-10");
        fornecedor.setStatus(StatusFornecedor.ATIVO);
        entityManager.persist(fornecedor);

        // 2. Setup Ativo
        Ativo ativo = new Ativo();
        ativo.setFilial(filial);
        ativo.setTipoAtivo(tipoAtivo);
        ativo.setFornecedor(fornecedor);
        ativo.setNome("Notebook Dell");
        ativo.setNumeroPatrimonio("PAT12345");
        ativo.setDataAquisicao(LocalDate.now());
        ativo.setValorAquisicao(new BigDecimal("5000.00"));
        ativo.setDataRegistro(LocalDate.now());
        ativo.setStatus(StatusAtivo.ATIVO);
        entityManager.persist(ativo);

        // 3. Setup AtivoDetalheHardware
        AtivoDetalheHardware detalhe = new AtivoDetalheHardware();
        detalhe.setAtivo(ativo); // Sets ID via MapsId
        detalhe.setComputerName("HOST-001");
        entityManager.persist(detalhe);

        entityManager.flush();
        entityManager.clear();

        // 4. Load AtivoDetalheHardware
        AtivoDetalheHardware loadedDetalhe = entityManager.find(AtivoDetalheHardware.class, ativo.getId());

        // 5. Verify Fetching Behavior
        PersistenceUnitUtil unitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        boolean isAtivoLoaded = unitUtil.isLoaded(loadedDetalhe, "ativo");

        // NEW BEHAVIOR: LAZY
        // This assertion confirms that it IS NOT loaded (proxy).
        assertFalse(isAtivoLoaded, "Ativo should be lazily loaded (not loaded initially)");
    }
}
