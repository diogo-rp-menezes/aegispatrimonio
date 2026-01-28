package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.Permission;
import br.com.aegispatrimonio.model.Role;
import br.com.aegispatrimonio.repository.PermissionRepository;
import br.com.aegispatrimonio.repository.RoleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
public class RbacPerformanceTest extends BaseIT {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RbacManagementService rbacManagementService;

    @PersistenceContext
    private EntityManager entityManager;

    private Long roleId;

    @BeforeEach
    public void setup() {
        permissionRepository.deleteAll();
        roleRepository.deleteAll();

        Permission p1 = new Permission();
        p1.setResource("RES1");
        p1.setAction("ACT1");
        permissionRepository.save(p1);

        Permission p2 = new Permission();
        p2.setResource("RES2");
        p2.setAction("ACT2");
        permissionRepository.save(p2);

        Role role = new Role();
        role.setName("TEST_ROLE");
        role.setDescription("Test Role");
        role.setPermissions(new HashSet<>(Set.of(p1, p2)));
        role = roleRepository.save(role);
        roleId = role.getId();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Transactional
    public void testFetchBehavior() {
        Session session = entityManager.unwrap(Session.class);
        Statistics stats = session.getSessionFactory().getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        // 1. Fetch Role.
        System.out.println("Fetching Role...");
        Role fetchedRole = roleRepository.findById(roleId).orElseThrow();

        long queryCountAfterFind = stats.getPrepareStatementCount();
        System.out.println("Queries after findById: " + queryCountAfterFind);

        // 2. Access permissions
        System.out.println("Accessing Permissions...");
        int permCount = fetchedRole.getPermissions().size();

        long queryCountAfterAccess = stats.getPrepareStatementCount();
        System.out.println("Queries after accessing permissions: " + queryCountAfterAccess);
    }

    @Test
    public void testListarRolesPerformance() {
        // Create another role to make N > 1
        Role role2 = new Role();
        role2.setName("TEST_ROLE_2");
        role2.setDescription("Test Role 2");
        roleRepository.save(role2);

        entityManager.flush();
        entityManager.clear();

        Session session = entityManager.unwrap(Session.class);
        Statistics stats = session.getSessionFactory().getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        System.out.println("Listing Roles...");
        rbacManagementService.listarRoles();

        long queryCount = stats.getPrepareStatementCount();
        System.out.println("Queries during listarRoles: " + queryCount);
        assertEquals(1, queryCount, "listarRoles should only execute 1 query (N+1 problem check)");
    }
}
