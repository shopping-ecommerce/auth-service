package iuh.fit.se.service;

import java.text.ParseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.JOSEException;

import iuh.fit.se.dto.request.*;
import iuh.fit.se.dto.response.AuthenticationResponse;
import iuh.fit.se.dto.response.IntrospectResponse;
import iuh.fit.se.dto.response.UserResponse;
import org.springframework.transaction.annotation.Transactional;

public interface AuthenticationService {

    IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException;

    void logout(LogoutRequest request) throws ParseException, JOSEException;

    AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException;

    UserResponse verifyJwtToken(String jwtToken);

    AuthenticationResponse loginWithEmailPassword(AuthenticationRequest request);

    void changePassword(ChangePasswordRequest request);

    AuthenticationResponse register(AuthenticationRegisterRequest request) throws JsonProcessingException;

    AuthenticationResponse verifyEmail(VerifyOTPRequest request) throws JsonProcessingException;

    void assignRoleToUser(AssignRoleRequest request) throws JsonProcessingException;

    @Transactional
    void revokeRoleFromUser(RevokeRoleRequest request) throws JsonProcessingException;

    /**
     * Bước 1: Gửi OTP về email để reset mật khẩu
     * @param request chứa email người dùng
     * @return AuthenticationResponse thông báo đã gửi OTP
     */
    AuthenticationResponse sendOTPForgotPassword(ForgotPasswordRequest request);

    /**
     * Bước 2: Xác thực OTP
     * @param request chứa email và OTP
     * @return AuthenticationResponse thông báo OTP hợp lệ
     */
    AuthenticationResponse verifyOTPForgotPassword(VerifyOTPForgotPasswordRequest request);

    /**
     * Bước 3: Đặt mật khẩu mới
     * @param request chứa email, mật khẩu mới và xác nhận mật khẩu
     * @return AuthenticationResponse thông báo đổi mật khẩu thành công
     */
    AuthenticationResponse resetPasswordWithNewPassword(ResetPasswordRequest request) throws JsonProcessingException;
}
