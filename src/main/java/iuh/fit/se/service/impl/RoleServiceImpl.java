package iuh.fit.se.service.impl;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;

import iuh.fit.se.dto.request.RoleRequest;
import iuh.fit.se.dto.response.RoleResponse;
import iuh.fit.se.entity.Role;
import iuh.fit.se.mapper.RoleMapper;
import iuh.fit.se.repository.PermissionRepository;
import iuh.fit.se.repository.RoleRepository;
import iuh.fit.se.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleServiceImpl implements RoleService {
    RoleRepository roleRepository;
    RoleMapper roleMapper;
    PermissionRepository permissionRepository;

    @Override
    public RoleResponse create(RoleRequest request) {
        Role role = roleMapper.toRole(request);
        var permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));
        return roleMapper.toRoleResponse(roleRepository.save(role));
    }

    @Override
    public List<RoleResponse> getAll() {
        var roles = roleRepository.findAll();
        return roles.stream().map(roleMapper::toRoleResponse).toList();
    }

    @Override
    public void delete(String roleName) {
        roleRepository.deleteById(roleName);
    }
}
