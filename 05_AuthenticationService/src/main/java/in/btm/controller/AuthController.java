package in.btm.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import in.btm.dto.ActivateAccountRequest;
import in.btm.dto.ApiResponse;
import in.btm.dto.AuthResponse;
import in.btm.dto.ForgotPasswordRequest;
import in.btm.dto.LoginRequestDto;
import in.btm.dto.RegisterRequest;
import in.btm.dto.ResetPasswordRequest;
import in.btm.exceptions.EmailNotRegisteredException;
import in.btm.exceptions.PasswordMismatchException;
import in.btm.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register User
     * Creates INACTIVE account and sends temporary password to email
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(
            @RequestBody @Valid RegisterRequest request,
            HttpServletRequest httpRequest) {

        authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.builder()
                        .success(true)
                        .message("Registration successful. Temporary password sent to email")
                        .status(HttpStatus.CREATED.value())
                        .path(httpRequest.getRequestURI())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Activate Account
     * Verify temporary password and set new password
     */
    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<?>> activate(
            @RequestBody @Valid ActivateAccountRequest request,
            HttpServletRequest httpRequest) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException(
                    "New password and confirm password do not match");
        }

        authService.activate(request);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Account activated successfully")
                        .status(HttpStatus.OK.value())
                        .path(httpRequest.getRequestURI())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Login
     * Only ACTIVE users can login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(
            @RequestBody @Valid LoginRequestDto request,
            HttpServletRequest httpRequest) {

        AuthResponse data = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Login successful")
                        .data(data)
                        .status(HttpStatus.OK.value())
                        .path(httpRequest.getRequestURI())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Forgot Password
     * Generate new temporary password and send email
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {

        authService.forgotPassword(request);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Temporary password sent to email")
                        .status(HttpStatus.OK.value())
                        .path(httpRequest.getRequestURI())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Check Email Exists
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<?>> checkEmail(
            @RequestParam String email,
            HttpServletRequest request) {

        if (!authService.isEmailExists(email)) {
            throw new EmailNotRegisteredException("Email not registered");
        }

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Email exists")
                        .data(Map.of("exists", true))
                        .status(HttpStatus.OK.value())
                        .path(request.getRequestURI())
                        .timestamp(LocalDateTime.now())
                        .build());
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Password reset successfully")
                        .status(HttpStatus.OK.value())
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}