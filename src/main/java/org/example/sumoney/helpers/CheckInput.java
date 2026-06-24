package org.example.sumoney.helpers;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class CheckInput {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>\\[\\]_/+=-]).{8,}$"
    );
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    public boolean isPasswordStrong(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
    public boolean isEmailValid(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}

