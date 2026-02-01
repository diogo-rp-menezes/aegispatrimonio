package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.*;
import br.com.aegispatrimonio.mapper.RbacMapper;
import br.com.aegispatrimonio.model.Group;
import br.com.aegispatrimonio.model.Permission;
import br.com.aegispatrimonio.model.Role;
import br.com.aegispatrimonio.repository.GroupRepository;
import br.com.aegispatrimonio.repository.PermissionRepository;
import br.com.aegispatrimonio.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RbacManagementService {

    private final RoleRepository roleRepository;
    private final GroupRepository groupRepository;
    private final PermissionRepository permissionRepository;
    private final RbacMapper rbacMapper;

    public RbacManagementService(RoleRepository roleRepository, GroupRepository groupRepository, PermissionRepository permissionRepository, RbacMapper rbacMapper) {
        this.roleRepository = roleRepository;
        this.groupRepository = groupRepository;
        this.permissionRepository = permissionRepository;
        this.rbacMapper = rbacMapper;
    }

    // --- Roles ---

    @Transactional(readOnly = true)
    public List<RoleDTO> listarRoles() {
        return roleRepository.findAllWithPermissions().stream()
                .map(rbacMapper::toRoleDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoleDTO buscarRolePorId(Long id) {
        return roleRepository.findById(id)
                .map(rbacMapper::toRoleDTO)
                .orElseThrow(() -> new EntityNotFoundException("Role não encontrada com ID: " + id));
    }

    @Transactional
    public RoleDTO criarRole(RoleCreateDTO dto) {
        if (roleRepository.findByName(dto.name()).isPresent()) {
            throw new IllegalArgumentException("Já existe uma role com o nome informado: " + dto.name());
        }

        Role role = new Role();
        role.setName(dto.name());
        role.setDescription(dto.description());

        if (dto.permissionIds() != null && !dto.permissionIds().isEmpty()) {
            List<Permission> permissions = permissionRepository.findAllById(dto.permissionIds());
            if (permissions.size() != dto.permissionIds().size()) {
                 throw new IllegalArgumentException("Algumas permissões informadas não existem.");
            }
            role.setPermissions(new HashSet<>(permissions));
        }

        return rbacMapper.toRoleDTO(roleRepository.save(role));
    }

    @Transactional
    public RoleDTO atualizarRole(Long id, RoleUpdateDTO dto) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role não encontrada com ID: " + id));

        Optional<Role> existingRole = roleRepository.findByName(dto.name());
        if (existingRole.isPresent() && !existingRole.get().getId().equals(id)) {
             throw new IllegalArgumentException("Já existe uma role com o nome informado: " + dto.name());
        }

        role.setName(dto.name());
        role.setDescription(dto.description());

        if (dto.permissionIds() != null) {
            List<Permission> permissions = permissionRepository.findAllById(dto.permissionIds());
             if (permissions.size() != dto.permissionIds().size()) {
                 throw new IllegalArgumentException("Algumas permissões informadas não existem.");
            }
            role.setPermissions(new HashSet<>(permissions));
        }

        return rbacMapper.toRoleDTO(roleRepository.save(role));
    }

    @Transactional
    public void deletarRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new EntityNotFoundException("Role não encontrada com ID: " + id);
        }
        roleRepository.deleteById(id);
    }

    // --- Permissions ---

    @Transactional(readOnly = true)
    public List<PermissionDTO> listarPermissoes() {
        return permissionRepository.findAll().stream()
                .map(rbacMapper::toPermissionDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PermissionDTO criarPermissao(PermissionCreateDTO dto) {
        Permission permission = new Permission();
        permission.setResource(dto.resource());
        permission.setAction(dto.action());
        permission.setDescription(dto.description());
        permission.setContextKey(dto.contextKey());

        return rbacMapper.toPermissionDTO(permissionRepository.save(permission));
    }

    @Transactional
    public void deletarPermissao(Long id) {
         if (!permissionRepository.existsById(id)) {
            throw new EntityNotFoundException("Permissão não encontrada com ID: " + id);
        }
        permissionRepository.deleteById(id);
    }

    // --- Groups ---

    @Transactional(readOnly = true)
    public List<GroupDTO> listarGrupos() {
        return groupRepository.findAll().stream()
                .map(rbacMapper::toGroupDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GroupDTO buscarGrupoPorId(Long id) {
        return groupRepository.findById(id)
                .map(rbacMapper::toGroupDTO)
                .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado com ID: " + id));
    }

    @Transactional
    public GroupDTO criarGrupo(GroupCreateDTO dto) {
        if (groupRepository.findByName(dto.name()).isPresent()) {
            throw new IllegalArgumentException("Já existe um grupo com o nome informado: " + dto.name());
        }

        Group group = new Group();
        group.setName(dto.name());
        group.setDescription(dto.description());

        if (dto.permissionIds() != null && !dto.permissionIds().isEmpty()) {
            List<Permission> permissions = permissionRepository.findAllById(dto.permissionIds());
            if (permissions.size() != dto.permissionIds().size()) {
                 throw new IllegalArgumentException("Algumas permissões informadas não existem.");
            }
            group.setPermissions(new HashSet<>(permissions));
        }

        return rbacMapper.toGroupDTO(groupRepository.save(group));
    }

    @Transactional
    public GroupDTO atualizarGrupo(Long id, GroupUpdateDTO dto) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado com ID: " + id));

        Optional<Group> existingGroup = groupRepository.findByName(dto.name());
        if (existingGroup.isPresent() && !existingGroup.get().getId().equals(id)) {
             throw new IllegalArgumentException("Já existe um grupo com o nome informado: " + dto.name());
        }

        group.setName(dto.name());
        group.setDescription(dto.description());

        if (dto.permissionIds() != null) {
            List<Permission> permissions = permissionRepository.findAllById(dto.permissionIds());
             if (permissions.size() != dto.permissionIds().size()) {
                 throw new IllegalArgumentException("Algumas permissões informadas não existem.");
            }
            group.setPermissions(new HashSet<>(permissions));
        }

        return rbacMapper.toGroupDTO(groupRepository.save(group));
    }

    @Transactional
    public void deletarGrupo(Long id) {
        if (!groupRepository.existsById(id)) {
            throw new EntityNotFoundException("Grupo não encontrado com ID: " + id);
        }
        groupRepository.deleteById(id);
    }
}
