package iuh.fit.se.mapper;

import org.mapstruct.Mapper;

import iuh.fit.se.dto.request.UserClientRequest;
import iuh.fit.se.dto.request.UserCreationRequest;

@Mapper(componentModel = "spring")
public interface UserClientMapper {
    // Ánh xạ các trường từ UserCreationRequest sang UserClientRequest
    //    @Mapping(source = "id", target = "accountId")
    UserClientRequest toUserClientRequest(UserCreationRequest request);
}
