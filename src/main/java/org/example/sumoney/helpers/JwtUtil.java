package org.example.sumoney.helpers;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;


import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;


@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.duration}")
    private int jwtExpiration;

    public String generateToken(Long userId, Long tokenVersion) {

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userId.toString())
                .expiresAt(Instant.now().plus(jwtExpiration, ChronoUnit.SECONDS))
                .issuedAt(Instant.now())
                .id(UUID.randomUUID().toString())
                .claim("token_version", tokenVersion)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS512).build();

        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        JWKSource<SecurityContext> jwkSource = new ImmutableSecret<>(key);

        JwtEncoder encoder = new NimbusJwtEncoder(jwkSource);

        Jwt jwt = encoder.encode(JwtEncoderParameters.from(jwsHeader, claims));

        return jwt.getTokenValue();
    }
}

