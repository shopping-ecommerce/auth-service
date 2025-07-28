package iuh.fit.se.service;

import java.util.List;

import iuh.fit.se.dto.request.PermissionRequest;
import iuh.fit.se.dto.response.PermissionResponse;

public interface PermissionService {
    PermissionResponse createPermission(PermissionRequest request);

    List<PermissionResponse> getAllPermissions();

    void deletePermission(String permissionName);
}
