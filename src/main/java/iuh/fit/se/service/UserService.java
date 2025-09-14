package iuh.fit.se.service;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import iuh.fit.se.dto.request.UserCreationRequest;
import iuh.fit.se.dto.request.UserUpdateRequest;
import iuh.fit.se.dto.response.UserResponse;

public interface UserService {
    //        UserResponse findById(String id);

    UserResponse getMyInfo();
    //
    UserResponse createUser(UserCreationRequest request) throws JsonProcessingException;

    UserResponse updateUser(String id, UserUpdateRequest request);
    //
    //        void deleteUser(String id);
    //    //
    UserResponse findByEmail(String email);

    UserResponse findByUserId(String userId);

    List<UserResponse> findUsers();

    List<UserResponse> findByRole(String role);

    //        void deleteUsers(List<String> ids);

    //        List<UserResponse> searchUsers(String searchQuery);
}
