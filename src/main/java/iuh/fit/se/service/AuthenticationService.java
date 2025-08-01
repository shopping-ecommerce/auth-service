package iuh.fit.se.service;

import java.text.ParseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.JOSEException;

import iuh.fit.se.dto.request.*;
import iuh.fit.se.dto.response.AuthenticationResponse;
import iuh.fit.se.dto.response.IntrospectResponse;
import iuh.fit.se.dto.response.UserResponse;

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
}
