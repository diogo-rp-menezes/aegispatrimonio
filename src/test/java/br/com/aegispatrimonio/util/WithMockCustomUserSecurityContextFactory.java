package br.com.aegispatrimonio.util;

import br.com.aegispatrimonio.model.Departamento;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Funcionario;
import br.com.aegispatrimonio.model.Status;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.FuncionarioRepository;
import br.com.aegispatrimonio.repository.UsuarioRepository;
import br.com.aegispatrimonio.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private FuncionarioRepository funcionarioRepository;
    @Autowired
    private FilialRepository filialRepository;
    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // Tenta encontrar o usuário no banco de dados de teste pelo EMAIL (username da anotação)
        Usuario usuario = usuarioRepository.findByEmail(annotation.username() + "@example.com").orElseGet(() -> {
            // Se não encontrar, cria e persiste um novo usuário com suas dependências
            Filial filial = new Filial();
            filial.setNome("Filial Teste " + UUID.randomUUID().toString().substring(0, 4));
            filial.setCodigo("FT" + UUID.randomUUID().toString().substring(0, 2));
            filial.setCnpj("000000000001" + UUID.randomUUID().toString().substring(0, 4));
            filial.setStatus(Status.ATIVO);
            filial = filialRepository.save(filial);

            Departamento departamento = new Departamento();
            departamento.setNome("Departamento Teste " + UUID.randomUUID().toString().substring(0, 4));
            departamento.setFilial(filial);
            departamento.setStatus(Status.ATIVO);
            departamento = departamentoRepository.save(departamento);

            Funcionario funcionario = new Funcionario();
            funcionario.setNome(annotation.username());
            funcionario.setMatricula(annotation.username().replaceAll("\\s+", "") + "-" + UUID.randomUUID().toString().substring(0, 4));
            funcionario.setCargo("Cargo Teste");
            funcionario.setStatus(Status.ATIVO);
            funcionario.setFiliais(Set.of(filial));
            funcionario.setDepartamento(departamento);
            funcionario = funcionarioRepository.save(funcionario);

            Usuario newUsuario = new Usuario();
            newUsuario.setUsername(annotation.username()); // O campo username da entidade Usuario não existe, mas é usado para o mock
            newUsuario.setEmail(annotation.username() + "@example.com");
            newUsuario.setPassword("password");
            newUsuario.setRole(annotation.roles()[0]);
            newUsuario.setStatus(Status.ATIVO);
            newUsuario.setFuncionario(funcionario);
            return usuarioRepository.save(newUsuario);
        });

        // Coletar as authorities (roles + permissions) da anotação
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        Stream.of(annotation.roles()).map(r -> new SimpleGrantedAuthority("ROLE_" + r)).forEach(grantedAuthorities::add);
        Stream.of(annotation.authorities()).map(SimpleGrantedAuthority::new).forEach(grantedAuthorities::add);

        // Criar o CustomUserDetails usando o Usuario real/persisted
        CustomUserDetails principal = new CustomUserDetails(usuario, grantedAuthorities);

        // Criar o token de autenticação com o principal correto
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}
