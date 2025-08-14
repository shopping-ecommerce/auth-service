package iuh.fit.se.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;

import iuh.fit.se.dto.request.UserCreationRequest;
import iuh.fit.se.dto.request.UserUpdateRequest;
import iuh.fit.se.dto.response.ApiResponse;
import iuh.fit.se.dto.response.UserResponse;
import iuh.fit.se.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users")
public class UserController {
    UserService userService;
    /**
     * Get all users.
     *
     * @return a list of all users
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.findUsers());
    }

    /**
     * Get users by role.
     *
     * @param role the role of users to search for
     * @return a list of users with the specified role
     */
    @GetMapping("/search/r/{role}")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable String role) {
        return ResponseEntity.ok(userService.findByRole(role));
    }

//    /**
//     * Get a user by email.
//     *
//     * @param email the email of the user
//     * @return the user information
//     */
//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/search/e/{email}")
//    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
//        var authentication = SecurityContextHolder.getContext().getAuthentication();
//        log.info("User: {}", authentication.getPrincipal());
//        log.info("Name: {}", authentication.getName());
//        log.info("Authorities: ");
//        authentication.getAuthorities().forEach(s -> log.info(s.getAuthority()));
//        return ResponseEntity.ok(userService.findByEmail(email));
//    }

    /**
     * Create a new user.
     *
     * @param request the user creation request containing user details
     * @return the created user
     */
    @PostMapping("/create")
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request)
            throws JsonProcessingException {
        log.warn("UserCreationRequest:", request.toString());
        //        return ResponseEntity.ok(userService.createUser(request));
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    /**
     * Update an existing user.
     *
     * @param id      the unique ID of the user to update
     * @param request the user update request containing updated user details
     * @return the updated user
     */
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String id, @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PreAuthorize("hasAuthority('VIEW_USER')")
    @GetMapping("/myinfo")
    public ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }
}
