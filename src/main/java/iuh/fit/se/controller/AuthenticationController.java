package iuh.fit.se.controller;

import java.text.ParseException;

import jakarta.validation.Valid;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;

import iuh.fit.se.dto.request.*;
import iuh.fit.se.dto.response.ApiResponse;
import iuh.fit.se.dto.response.AuthenticationResponse;
import iuh.fit.se.dto.response.IntrospectResponse;
import iuh.fit.se.dto.response.UserResponse;
import iuh.fit.se.service.AuthenticationService;
import iuh.fit.se.service.OTPService;
import iuh.fit.se.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/authentication")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final OTPService otpService;
    private final UserService userService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    // Xác thực ID Token từ Google và trả về JWT
    @PostMapping("/login-email-password")
    public ApiResponse<AuthenticationResponse> loginWithEmailPassword(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse response = authenticationService.loginWithEmailPassword(request);
        return ApiResponse.<AuthenticationResponse>builder().result(response).build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        AuthenticationResponse response = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder().result(response).build();
    }

    // Xác thực JWT và trả về thông tin người dùng
    @GetMapping("/verify-jwt")
    public ApiResponse<UserResponse> verifyJwtToken(@RequestHeader("Authorization") String token) {
        UserResponse userResponse = authenticationService.verifyJwtToken(token.replace("Bearer ", ""));
        return ApiResponse.<UserResponse>builder().result(userResponse).build();
    }

//    @PreAuthorize("hasRole('ADMIN')")
//    @PreAuthorize("hasAuthority('DELETE_SELLER')")
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @PostMapping("/change-password")
    public ApiResponse<String> changePassword(@RequestBody ChangePasswordRequest request) {
        authenticationService.changePassword(request);
        return ApiResponse.<String>builder()
                .result("Password changed successfully")
                .build();
    }

    @PostMapping("/register")
    public ApiResponse<AuthenticationResponse> register(@RequestBody @Valid AuthenticationRegisterRequest request)
            throws JsonProcessingException {
        log.warn(request.toString());
        AuthenticationResponse response = authenticationService.register(request);
        return ApiResponse.<AuthenticationResponse>builder().result(response).build();
    }

    @PostMapping("/verifyOTP")
    public ApiResponse<AuthenticationResponse> verifyOTP(@RequestBody VerifyOTPRequest request)
            throws JsonProcessingException {
        AuthenticationResponse response = authenticationService.verifyEmail(request);
        return ApiResponse.<AuthenticationResponse>builder().result(response).build();
    }

    @GetMapping("/verifyFromEmail")
    public ApiResponse<AuthenticationResponse> verifyFromEmail(@RequestParam("email") String email)
            throws JsonProcessingException {
        try {
            log.warn("CON1");
            String otpKey = "otp:user:" + email;
            String otp = redisTemplate.opsForValue().get(otpKey);
            String pendingKey = "register:pending:" + email;
            String registerDataJson = redisTemplate.opsForValue().get(pendingKey);
            if (registerDataJson == null) {
                return ApiResponse.<AuthenticationResponse>builder()
                        .message("Dữ liệu đăng ký không tồn tại hoặc đã hết hạn.")
                        .build();
            }

            // Chuyển đổi JSON thành đối tượng AuthenticationRegisterRequest
            AuthenticationRegisterRequest registerRequest =
                    objectMapper.readValue(registerDataJson, AuthenticationRegisterRequest.class);
            // Tạo JSON request cho API verifyOTP
            String jsonBody = String.format("{\"email\":\"%s\",\"otp\":\"%s\"}", email, otp);

            // Thiết lập header và gọi API POST
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);
            log.warn("CON2");
            String verifyUrl = "http://localhost:8888/savorgo/api/authentication/verifyOTP";
            ResponseEntity<String> response =
                    restTemplate.exchange(verifyUrl, HttpMethod.POST, requestEntity, String.class);
            log.warn("CON3");
            // Xử lý kết quả
            if (response.getStatusCode().is2xxSuccessful()) {
                // Xóa dữ liệu tạm thời sau khi xác thực thành công
                redisTemplate.delete(otpKey);
                redisTemplate.delete(pendingKey);
                return ApiResponse.<AuthenticationResponse>builder()
                        .message("Đăng ký thành công.")
                        .build();
            } else {
                return ApiResponse.<AuthenticationResponse>builder()
                        .message("Đăng ký thất bại.")
                        .build();
            }
        } catch (Exception e) {
            return ApiResponse.<AuthenticationResponse>builder()
                    .message("Có lỗi xảy ra")
                    .build();
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/assign-role")
    public ApiResponse<String> assignRole(@RequestBody AssignRoleRequest request) throws JsonProcessingException {
        authenticationService.assignRoleToUser(request);
        return ApiResponse.<String>builder()
                .code(200)
                .result("Assign role successfully")
                .build();
    }

    @PreAuthorize("hasAuthority('UPDATE_SELLER')")
    @PostMapping("/revoke-role")
    public ApiResponse<String> revokeRole(@RequestBody RevokeRoleRequest request) throws JsonProcessingException {
        authenticationService.revokeRoleFromUser(request);
        return ApiResponse.<String>builder()
                .code(200)
                .result("Revoke role successfully")
                .build();
    }

    /**
     * Endpoint để gửi OTP reset mật khẩu
     * Người dùng nhập email, hệ thống gửi OTP qua email
     */
    @PostMapping("/forgot-password")
    public ApiResponse<AuthenticationResponse> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.getEmail());
        AuthenticationResponse response = authenticationService.forgotPassword(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(response)
                .build();
    }

    /**
     * Endpoint để reset mật khẩu với OTP
     * Người dùng nhập email, OTP và mật khẩu mới
     */
    @PostMapping("/reset-password")
    public ApiResponse<AuthenticationResponse> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request) {
        log.info("Reset password request for email: {}", request.getEmail());
        AuthenticationResponse response = authenticationService.resetPassword(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(response)
                .build();
    }
}
