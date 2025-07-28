package iuh.fit.se.service;

import java.util.List;

import iuh.fit.se.dto.request.RoleRequest;
import iuh.fit.se.dto.response.RoleResponse;

public interface RoleService {
    RoleResponse create(RoleRequest request);

    List<RoleResponse> getAll();

    void delete(String roleName);
}
