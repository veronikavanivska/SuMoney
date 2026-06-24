package org.example.sumoney.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.example.sumoney.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Bean
    public JwtEncoder jwtEncoder() {
        SecretKey key = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA512"
        );

        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    public JwtDecoder jwtDecoder(UserRepository userRepository) {
        SecretKey key = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA512"
        );

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();

        OAuth2TokenValidator<Jwt> defaults = JwtValidators.createDefault();

        OAuth2TokenValidator<Jwt> subjectValidator = jwt -> {
            if (jwt.getSubject() == null || jwt.getSubject().isBlank()) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error(
                                "invalid_token",
                                "missing subject",
                                null
                        )
                );
            }

            return OAuth2TokenValidatorResult.success();
        };

        OAuth2TokenValidator<Jwt> tokenVersionValidator = jwt -> {
            Long userId;

            try {
                userId = Long.valueOf(jwt.getSubject());
            } catch (NumberFormatException e) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error(
                                "invalid_token",
                                "invalid subject",
                                null
                        )
                );
            }

            Long tokenVersion = jwt.getClaim("token_version");

            if (tokenVersion == null) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error(
                                "invalid_token",
                                "missing token version",
                                null
                        )
                );
            }

            return userRepository.findById(userId)
                    .filter(user -> Objects.equals(user.getTokenVersion(), tokenVersion))
                    .map(user -> OAuth2TokenValidatorResult.success())
                    .orElseGet(() -> OAuth2TokenValidatorResult.failure(
                            new OAuth2Error(
                                    "invalid_token",
                                    "invalid token version",
                                    null
                            )
                    ));
        };

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        defaults,
                        subjectValidator,
                        tokenVersionValidator
                )
        );

        return decoder;
    }
}