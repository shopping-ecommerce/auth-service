package iuh.fit.se.service.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import iuh.fit.event.dto.NotificationEvent;
import iuh.fit.se.dto.request.UserCreationRequest;
import iuh.fit.se.dto.request.UserUpdateRequest;
import iuh.fit.se.dto.response.UserResponse;
import iuh.fit.se.entity.Role;
import iuh.fit.se.entity.User;
import iuh.fit.se.enums.UserRoleEnum;
import iuh.fit.se.exception.AppException;
import iuh.fit.se.exception.ErrorCode;
import iuh.fit.se.mapper.UserClientMapper;
import iuh.fit.se.mapper.UserMapper;
import iuh.fit.se.repository.RoleRepository;
import iuh.fit.se.repository.UserRepository;
import iuh.fit.se.repository.httpclient.UserClient;
import iuh.fit.se.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final UserClientMapper userClientMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private UserClient userClient;

    @Override
    public UserResponse getMyInfo() {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String id = SecurityContextHolder.getContext().getAuthentication().getName();
//        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    /**
     * Creates a new user based on the provided UserCreationRequest.
     *
     * @param request the UserCreationRequest containing user details
     * @return UserResponse the created user details
     */
    //    @Transactional
    @Override
    public UserResponse createUser(UserCreationRequest request) throws JsonProcessingException {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        var roles = new HashSet<Role>();
        roles.add(roleRepository.findByName("CUSTOMER"));
        user.setRoles(roles);
        user = userRepository.save(user);

        // Lưu qua user service
        var userClientRequest = userClientMapper.toUserClientRequest(request);
        userClientRequest.setAccountId(user.getId());
        ObjectMapper objectMapper = new ObjectMapper();
        log.info("UserClientRequest JSON: {}", objectMapper.writeValueAsString(userClientRequest));
        var x = userClient.createUser(userClientRequest);

        // publish message to kafka
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("EMAIL")
                .body(request.getFirstName() + " " + request.getLastName())
                .subject("Welcome to SavorGo")
                .recipient(request.getEmail())
                .build();
        kafkaTemplate.send("notification-delivery-success", notificationEvent);
        return userMapper.toUserResponse(user);
    }

    /**
     * Updates an existing user identified by the given id.
     *
     * @param id the id of the user to be updated
     * @param request the UserUpdateRequest containing updated user details
     * @return UserResponse the updated user details
     */
    @Override
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateUser(user, request);
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));
        user.setModifiedTime(LocalDateTime.now());

        return objectMapper.convertValue(userRepository.save(user), UserResponse.class);
    }

    /**
     * Deletes a user identified by the given id by marking it as deleted.
     *
     * @param id the id of the user to be deleted
     */
    //        @Override
    //        public void deleteUser(String id) {
    //            User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    //            user.setStatus(UserStatusEnum.DELETED);
    //            userRepository.save(user);
    //        }

    /**
     * Finds a user by their email address.
     *
     * @param email the email address of the user to be found
     * @return UserResponse the found user details
     */
    @Override
    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return objectMapper.convertValue(user, UserResponse.class);
    }

    /**
     * Retrieves all users.
     *
     * @return List<UserResponse> a list of all user details
     */
    @Override
    public List<UserResponse> findUsers() {
        List<User> users = userRepository.findAll();
        return objectMapper.convertValue(users, new TypeReference<List<UserResponse>>() {});
    }

    /**
     * Finds users by their role.
     *
     * @param role the role of the users to be found
     * @return List<UserResponse> a list of users with the specified role
     */
    @Override
    public List<UserResponse> findByRole(String role) {
        UserRoleEnum roleEnum = UserRoleEnum.valueOf(role.toUpperCase());
        List<User> users = userRepository.findByRoles(roleEnum);
        return objectMapper.convertValue(users, new TypeReference<List<UserResponse>>() {});
    }

    /**
     * Finds a user by their id.
     *
     * @param id the id of the user to be found
     * @return UserResponse the found user details
     */
            @Override
            public UserResponse findByUserId(String id) {
                Optional<User> user = userRepository.findById(id);
                return userMapper.toUserResponse(user.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
            }

    /**
     * Deletes multiple users identified by their ids by marking them as deleted.
     *
     * @param ids a list of user ids to be deleted
     */
    //        @Override
    //        public void deleteUsers(List<String> ids) {
    //            ids.forEach(id -> {
    //                User user = userRepository.findById(id).orElseThrow(() -> new
    // AppException(ErrorCode.USER_NOT_FOUND));
    //                user.setStatus(UserStatusEnum.DELETED);
    //                userRepository.save(user);
    //            });
    //        }

    /**
     * Searches for users based on a search query.
     *
     * @param searchQuery the query string to search for users
     * @return List<UserResponse> a list of users matching the search criteria
     */
    //        @Override
    //        public List<UserResponse> searchUsers(String searchQuery) {
    //            List<User> users = userRepository.findAll();
    //            String searchLower = searchQuery.toLowerCase();
    //            List<User> filteredUsers = users.stream()
    //                    .filter(user -> user.getId().contains(searchQuery)
    //                            || user.getEmail().contains(searchQuery)
    //                            || user.getFirstName().toLowerCase().contains(searchLower)
    //                            || user.getLastName().toLowerCase().contains(searchLower)
    //                            || user.getAddress().toLowerCase().contains(searchLower)
    //                            || (user.getFirstName().toLowerCase() + " "
    //                                            + user.getLastName().toLowerCase())
    //                                    .contains(searchLower)
    //                            || (user.getLastName().toLowerCase() + " "
    //                                            + user.getFirstName().toLowerCase())
    //                                    .contains(searchLower))
    //                    .collect(Collectors.toList());
    //            return objectMapper.convertValue(filteredUsers, new TypeReference<List<UserResponse>>() {});
    //        }
}
