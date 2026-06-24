package org.example.sumoney.services;

import org.example.sumoney.dto.requests.ChangePasswordRequest;
import org.example.sumoney.dto.requests.LoginRequest;
import org.example.sumoney.dto.requests.RefreshRequest;
import org.example.sumoney.dto.requests.RegisterRequest;
import org.example.sumoney.dto.response.AuthResponse;
import org.example.sumoney.entities.RefreshToken;
import org.example.sumoney.entities.User;
import org.example.sumoney.helpers.BCrypt;
import org.example.sumoney.helpers.CheckInput;
import org.example.sumoney.helpers.JwtUtil;
import org.example.sumoney.repositories.RefreshTokenRepository;
import org.example.sumoney.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

import static org.example.sumoney.helpers.RefreshTokenHelper.newOpaqueToken;
import static org.example.sumoney.helpers.RefreshTokenHelper.sha256Base64Url;

@Service
public class AuthService {

    private final BCrypt bCrypt;
    private final CheckInput checkInput;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${refresh.duration:30}")
    private long refreshDuration;

    public AuthService(
            BCrypt bCrypt,
            CheckInput checkInput,
            UserRepository userRepository,
            JwtUtil jwtUtil,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.bCrypt = bCrypt;
        this.checkInput = checkInput;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String register(RegisterRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Invalid input data");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Invalid input data");
        }

        if (!checkInput.isEmailValid(email)) {
            throw new IllegalArgumentException("Invalid input data");
        }

        if (!checkInput.isPasswordStrong(password)) {
            throw new IllegalArgumentException("Password does not meet requirements");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Unable to complete registration");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(bCrypt.hashPassword(password));
        user.setTokenVersion(0L);

        userRepository.save(user);

        return "Registered";
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Invalid input data");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Invalid input data");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!bCrypt.checkPassword(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String accessToken = jwtUtil.generateToken(user.getId(), user.getTokenVersion());

        String rawRefresh = newOpaqueToken(64);
        String hashedRefresh = sha256Base64Url(rawRefresh);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(hashedRefresh);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plus(Duration.ofDays(refreshDuration)));
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);

        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(rawRefresh);

        return response;
    }

    public AuthResponse refresh(RefreshRequest request) {
        String rawRefresh = request.getRefreshToken();

        if (rawRefresh == null || rawRefresh.isBlank()) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        String hashToken = sha256Base64Url(rawRefresh);

        RefreshToken currentToken = refreshTokenRepository
                .findActiveByHash(hashToken, Instant.now())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired refresh token"));

        User user = currentToken.getUser();

        currentToken.setRevoked(true);
        currentToken.setLastUsedAt(Instant.now());
        refreshTokenRepository.save(currentToken);

        String newRawRefresh = newOpaqueToken(64);
        String newHashRefresh = sha256Base64Url(newRawRefresh);

        RefreshToken nextToken = new RefreshToken();
        nextToken.setUser(user);
        nextToken.setTokenHash(newHashRefresh);
        nextToken.setExpiresAt(Instant.now().plus(Duration.ofDays(refreshDuration)));
        nextToken.setRevoked(false);

        refreshTokenRepository.save(nextToken);

        String newAccessToken = jwtUtil.generateToken(user.getId(), user.getTokenVersion());

        AuthResponse response = new AuthResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRawRefresh);

        return response;
    }

    public String logout(RefreshRequest request) {
        String rawRefresh = request.getRefreshToken();

        if (rawRefresh == null || rawRefresh.isBlank()) {
            return "Logged out";
        }

        String hashToken = sha256Base64Url(rawRefresh);

        refreshTokenRepository
                .findActiveByHash(hashToken, Instant.now())
                .ifPresent(token -> {
                    token.setRevoked(true);
                    token.setLastUsedAt(Instant.now());
                    refreshTokenRepository.save(token);
                });

        return "Logged out";
    }

    public String changePassword(ChangePasswordRequest request, Long userId) {
        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Operation could not be completed"));

        if (!bCrypt.checkPassword(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Unable to change password");
        }

        if (!checkInput.isPasswordStrong(newPassword)) {
            throw new IllegalArgumentException("Password does not meet requirements");
        }

        if (newPassword.equals(oldPassword)) {
            throw new IllegalArgumentException("Password does not meet requirements");
        }

        user.incrementTokenVersion();
        user.setPassword(bCrypt.hashPassword(newPassword));

        userRepository.save(user);

        refreshTokenRepository.revokeAllByUserId(user.getId(), Instant.now());

        return "Password changed";
    }
}