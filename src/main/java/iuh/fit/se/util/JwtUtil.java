package iuh.fit.se.util;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import iuh.fit.se.entity.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUtil {
    @Value("${jwt.signerKey}")
    private String secretKey;

    @Value("${jwt.valid-duration}")
    private long VALID_DURATION;

    @Value("${jwt.refreshable-duration}")
    private long REFRESH_DURATION;

    public String getSecretKey() {
        return secretKey;
    }

    public long getVALID_DURATION() {
        return VALID_DURATION;
    }

    public long getREFRESH_DURATION() {
        return REFRESH_DURATION;
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("Shopping")
                //                .audience(user.getFirstName())
//                .claim("scope", buildScopes(user))
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString());

        // Thêm claim "roles" (danh sách vai trò)
        List<String> roles = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(roles)) {
            claimsBuilder.claim("roles", roles);
        }

        // Thêm claim "scopes" (danh sách quyền)
        String scopes = buildScopes(user);
        if (!scopes.isEmpty()) {
            claimsBuilder.claim("scopes", scopes.split(" "));
        }
        JWTClaimsSet jwtClaimsSet = claimsBuilder.build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(secretKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create JWT token", e);
            throw new RuntimeException(e);
        }
    }

    private String buildScopes(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(s -> {
                stringJoiner.add("ROLE_" + s.getName());
                if (!CollectionUtils.isEmpty(s.getPermissions()))
                    s.getPermissions().forEach(p -> stringJoiner.add(p.getName()));
            });
        }
        return stringJoiner.toString();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    private Claims extractClaims(String token) {
        return Jwts.parser() // ✅ Sử dụng parserBuilder() thay vì parser()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
