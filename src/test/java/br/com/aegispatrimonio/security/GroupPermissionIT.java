package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.service.IPermissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GroupPermissionIT extends BaseIT {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private IPermissionService permissionService;

    @Test
    @Transactional
    void userShouldInheritPermissionsFromGroup() {
        // 1. Create Permission
        Permission p = new Permission(null, "TEST_RESOURCE", "READ", "Test Desc", null);
        p = permissionRepository.save(p);

        // 2. Create Group with Permission
        Group g = new Group(null, "TEST_GROUP", "Desc", new HashSet<>());
        g.getPermissions().add(p);
        g = groupRepository.save(g);

        // 3. Create User with Group
        Usuario u = new Usuario();
        u.setEmail("groupuser@example.com");
        u.setPassword("pass");
        u.setRole("ROLE_USER");
        u.setStatus(Status.ATIVO);
        u.setGroups(new HashSet<>());
        u.getGroups().add(g);
        u = usuarioRepository.save(u);

        // 4. Authenticate
        CustomUserDetails userDetails = new CustomUserDetails(u);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 5. Verify
        boolean hasPerm = permissionService.hasPermission(auth, null, "TEST_RESOURCE", "READ", null);
        assertTrue(hasPerm, "User should have permission inherited from Group");
    }
}
