package iuh.fit.se.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import iuh.fit.se.dto.request.RoleRequest;
import iuh.fit.se.dto.response.RoleResponse;
import iuh.fit.se.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toRoleResponse(Role role);

    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);
}
