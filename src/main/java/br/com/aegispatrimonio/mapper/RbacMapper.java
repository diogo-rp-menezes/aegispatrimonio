package br.com.aegispatrimonio.mapper;

import br.com.aegispatrimonio.dto.PermissionDTO;
import br.com.aegispatrimonio.dto.RoleDTO;
import br.com.aegispatrimonio.model.Permission;
import br.com.aegispatrimonio.model.Role;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RbacMapper {

    public PermissionDTO toPermissionDTO(Permission permission) {
        if (permission == null) {
            return null;
        }
        return new PermissionDTO(
            permission.getId(),
            permission.getResource(),
            permission.getAction(),
            permission.getDescription(),
            permission.getContextKey()
        );
    }

    public RoleDTO toRoleDTO(Role role) {
        if (role == null) {
            return null;
        }
        Set<PermissionDTO> permissionDTOs = null;
        if (role.getPermissions() != null) {
            permissionDTOs = role.getPermissions().stream()
                .map(this::toPermissionDTO)
                .collect(Collectors.toSet());
        }

        return new RoleDTO(
            role.getId(),
            role.getName(),
            role.getDescription(),
            permissionDTOs
        );
    }
}
