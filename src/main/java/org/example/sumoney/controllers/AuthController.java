package org.example.sumoney.controllers;

import org.example.sumoney.dto.requests.ChangePasswordRequest;
import org.example.sumoney.dto.requests.LoginRequest;
import org.example.sumoney.dto.requests.RefreshRequest;
import org.example.sumoney.dto.requests.RegisterRequest;
import org.example.sumoney.dto.response.AuthResponse;
import org.example.sumoney.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.logout(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordRequest request,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(authService.changePassword(request, userId));
    }

}
