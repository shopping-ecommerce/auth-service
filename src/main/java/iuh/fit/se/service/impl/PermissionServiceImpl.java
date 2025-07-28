package iuh.fit.se.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import iuh.fit.se.dto.request.PermissionRequest;
import iuh.fit.se.dto.response.PermissionResponse;
import iuh.fit.se.entity.Permission;
import iuh.fit.se.mapper.PermissionMapper;
import iuh.fit.se.repository.PermissionRepository;
import iuh.fit.se.service.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionServiceImpl implements PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    @Override
    public PermissionResponse createPermission(PermissionRequest request) {
        Permission permission = permissionRepository.save(permissionMapper.toPermission(request));
        return permissionMapper.toPermissionResponse(permission);
    }

    @Override
    public List<PermissionResponse> getAllPermissions() {
        var permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    @Override
    public void deletePermission(String permissionName) {
        permissionRepository.deleteById(permissionName);
    }
}
