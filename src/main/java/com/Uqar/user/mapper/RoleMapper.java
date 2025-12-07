package com.Uqar.user.mapper;

import com.Uqar.user.dto.RoleResponseDTO;
import com.Uqar.user.entity.Role;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RoleMapper {

    private final PermissionMapper permissionMapper;

    public RoleMapper(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    public RoleResponseDTO toResponse(Role role) {
        if (role == null) {
            return null;
        }

        return RoleResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .isActive(role.isActive())
                .isSystem(role.isSystem())
                .isSystemGenerated(role.isSystemGenerated())
                .permissions(role.getPermissions() != null ? 
                    role.getPermissions().stream()
                        .map(permissionMapper::toResponse)
                        .collect(Collectors.toSet()) : null)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .createdBy(role.getCreatedBy())
                .updatedBy(role.getLastModifiedBy())
                .build();
    }
} 