package iuh.fit.se.service.impl;

import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import iuh.fit.se.entity.Role;
import iuh.fit.se.enums.UserRoleEnum;
import iuh.fit.se.repository.RoleRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;

import iuh.fit.se.dto.request.*;
import iuh.fit.se.dto.response.AuthenticationResponse;
import iuh.fit.se.dto.response.IntrospectResponse;
import iuh.fit.se.dto.response.UserResponse;
import iuh.fit.se.entity.InvalidatedToken;
import iuh.fit.se.entity.User;
import iuh.fit.se.exception.AppException;
import iuh.fit.se.exception.ErrorCode;
import iuh.fit.se.repository.InvalidatedTokenRepository;
import iuh.fit.se.repository.UserRepository;
import iuh.fit.se.service.AuthenticationService;
import iuh.fit.se.service.OTPService;
import iuh.fit.se.service.UserService;
import iuh.fit.se.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final OTPService otpService;
    private final RedisTemplate<String, String> redisTemplate;
    private final RoleRepository roleRepository;

    @Override
    public AuthenticationResponse loginWithEmailPassword(AuthenticationRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        // Tìm user trong database
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // Kiểm tra mật khẩu
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Tạo JWT token
        String jwtToken = jwtUtil.generateToken(user);

        return AuthenticationResponse.builder()
                .jwtToken(jwtToken)
                .message("Login successful")
                .build();
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean valid = true;
        try {
            verifyToken(token, false);
        } catch (AppException e) {
            valid = false;
        }
        return IntrospectResponse.builder().valid(valid).build();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(jwtUtil.getSecretKey());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expityTime = !isRefresh
                ? signedJWT.getJWTClaimsSet().getExpirationTime()
                : new Date(signedJWT
                        .getJWTClaimsSet()
                        .getIssueTime()
                        .toInstant()
                        .plus(jwtUtil.getREFRESH_DURATION(), ChronoUnit.SECONDS)
                        .toEpochMilli());
        var verified = signedJWT.verify(verifier);
        if (!verified && expityTime.after(new Date())) throw new AppException(ErrorCode.UNAUTHENTICATED);
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        return signedJWT;
    }

    @Override
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);
            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();
            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();
            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException e) {
            log.info("Token already expired");
        }
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signToken = verifyToken(request.getToken(), true);
        var jit = signToken.getJWTClaimsSet().getJWTID();
        var expiryTime = signToken.getJWTClaimsSet().getExpirationTime();
        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();
        invalidatedTokenRepository.save(invalidatedToken);

        var email = signToken.getJWTClaimsSet().getSubject();

        var user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        var jwtToken = jwtUtil.generateToken(user);
        return AuthenticationResponse.builder()
                .jwtToken(jwtToken)
                .message("Refresh token successful")
                .build();
    }
    // Xác thực JWT và trả về thông tin người dùng
    @Override
    public UserResponse verifyJwtToken(String jwtToken) {
        String email = jwtUtil.extractEmail(jwtToken);

        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return objectMapper.convertValue(user, UserResponse.class);
    }

    @Override
    public void changePassword( ChangePasswordRequest request) {
        // Lấy email từ token JWT
        String id = SecurityContextHolder.getContext().getAuthentication().getName();
        // Tìm người dùng trong database
//        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User user = userRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_INCORRECT);
        }
        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public AuthenticationResponse register(AuthenticationRegisterRequest request) throws JsonProcessingException {
        // Check if the user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXITS);
        }
        String userData = objectMapper.writeValueAsString(request);
        // Store in Redis with a TTL of 5 minutes
        String redisKey = "register:pending:" + request.getEmail();
        redisTemplate.opsForValue().set(redisKey, userData, 2, TimeUnit.MINUTES);
        otpService.createAndSaveOTP(request.getEmail());
        return AuthenticationResponse.builder().message("Sended OTP success").build();
    }

    @Override
    public AuthenticationResponse verifyEmail(VerifyOTPRequest request) throws JsonProcessingException {
        boolean flag = otpService.verifyOTP(request);
        if (!flag) {
            throw new AppException(ErrorCode.INCORRECT_OTP);
        }
        String redisKey = "register:pending:" + request.getEmail();
        String userData = redisTemplate.opsForValue().get(redisKey);
        // Deserialize the user data
        AuthenticationRegisterRequest registerRequest;
        try {
            registerRequest = objectMapper.readValue(userData, AuthenticationRegisterRequest.class);
            log.warn("Verify Email: User data: {}", registerRequest.toString());
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        userService.createUser(UserCreationRequest.builder()
                .email(registerRequest.getEmail())
                .password(registerRequest.getPassword())
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .address(registerRequest.getAddress())
                .build());
        return AuthenticationResponse.builder()
                .message("Success")
                .build();
    }

    @Override
    public void assignRoleToUser(AssignRoleRequest request) throws JsonProcessingException {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Role sellerRole = roleRepository.findByName(UserRoleEnum.SELLER.name());

        Set<Role> roles = new HashSet<>(user.getRoles());
        if(!roles.contains(sellerRole)){
            roles.add(sellerRole);
            user.setRoles(roles);
            userRepository.save(user);
        }
    }

}
