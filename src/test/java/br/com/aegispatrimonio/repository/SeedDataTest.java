package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = {"/cleanup.sql", "/db/migration/V4__Insert_Seed_Data.sql"})
public class SeedDataTest extends BaseIT {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoAtivoRepository tipoAtivoRepository;

    @Test
    void shouldLoadSeedData() {
        // Verify Admin User
        Optional<Usuario> admin = usuarioRepository.findWithDetailsByEmail("admin@aegis.com");
        assertThat(admin).isPresent();
        assertThat(admin.get().getRole()).isEqualTo("ADMIN");
        assertThat(admin.get().getFuncionario()).isNotNull();
        assertThat(admin.get().getFuncionario().getNome()).isEqualTo("Administrador do Sistema");

        // Verify Tipos de Ativo
        assertThat(tipoAtivoRepository.count()).isGreaterThanOrEqualTo(3);
        assertThat(tipoAtivoRepository.findByNome("Notebook")).isPresent();
    }
}
