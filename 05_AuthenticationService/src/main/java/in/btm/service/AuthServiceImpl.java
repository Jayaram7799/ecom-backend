package in.btm.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.btm.dto.ActivateAccountRequest;
import in.btm.dto.AuthResponse;
import in.btm.dto.ForgotPasswordRequest;
import in.btm.dto.LoginRequestDto;
import in.btm.dto.RegisterRequest;
import in.btm.dto.ResetPasswordRequest;
import in.btm.entity.AuthUser;
import in.btm.entity.VerificationToken;
import in.btm.enums.AccountStatus;
import in.btm.enums.TokenType;
import in.btm.enums.UserRole;
import in.btm.exceptions.AccountNotActivatedException;
import in.btm.exceptions.EmailAlreadyExistsException;
import in.btm.exceptions.EmailNotRegisteredException;
import in.btm.exceptions.InvalidResetTokenException;
import in.btm.exceptions.InvalidTemporaryPasswordException;
import in.btm.exceptions.PasswordMismatchException;
import in.btm.repository.AuthRepository;
import in.btm.repository.VerificationTokenRepository;
import in.btm.util.EmailUtil;
import in.btm.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

	private final AuthRepository authRepository;
	private final VerificationTokenRepository tokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final EmailUtil emailUtil;

	private static final SecureRandom RANDOM = new SecureRandom();

	private static final int TOKEN_EXPIRY_MINUTES = 15;

	// =========================================================
	// REGISTER
	// =========================================================

	@Override
	@Transactional
	public void register(RegisterRequest request) {

		String email = normalizeEmail(request.getEmail());

		if (authRepository.existsByEmail(email)) {
			throw new EmailAlreadyExistsException("Email already registered");
		}

		/*
		 * Generate 6-digit temporary password.
		 *
		 * Example: 583921
		 */
		String temporaryPassword = generateTemporaryPassword();

		/*
		 * IMPORTANT:
		 *
		 * Store the temporary password as BCrypt hash. Never store it as plain text.
		 */
		AuthUser user = AuthUser.builder().email(email).password(passwordEncoder.encode(temporaryPassword))
				.role(UserRole.USER).status(AccountStatus.INACTIVE).build();

		authRepository.save(user);

		/*
		 * Send temporary password to user's email.
		 */
		emailUtil.sendRegistrationEmail(email, temporaryPassword);

		log.info("Registration completed for email={}", email);
	}

	// =========================================================
	// ACTIVATE ACCOUNT
	// =========================================================

	@Override
	@Transactional
	public void activate(ActivateAccountRequest request) {

		String email = normalizeEmail(request.getEmail());

		AuthUser user = authRepository.findByEmail(email)
				.orElseThrow(() -> new EmailNotRegisteredException("Email not registered"));

		/*
		 * Check whether account is already active.
		 */
		if (AccountStatus.ACTIVE.equals(user.getStatus())) {
			throw new AccountNotActivatedException("Account already activated");
		}

		/*
		 * Verify the 6-digit temporary password.
		 *
		 * request.getTemporaryPassword() contains the password received by email.
		 *
		 * user.getPassword() contains the BCrypt hash.
		 */
		if (!passwordEncoder.matches(request.getTemporaryPassword(), user.getPassword())) {

			throw new InvalidTemporaryPasswordException("Invalid temporary password");
		}

		/*
		 * Validate new password.
		 */
		if (!request.getNewPassword().equals(request.getConfirmPassword())) {

			throw new PasswordMismatchException("Passwords do not match");
		}

		/*
		 * Encode the user's actual password.
		 */
		user.setPassword(passwordEncoder.encode(request.getNewPassword()));

		/*
		 * Activate account.
		 */
		user.setStatus(AccountStatus.ACTIVE);

		authRepository.save(user);

		log.info("Account activated successfully for email={}", email);
	}

	// =========================================================
	// LOGIN
	// =========================================================

	@Override
	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequestDto request) {

		String email = normalizeEmail(request.getEmail());

		AuthUser user = authRepository.findByEmail(email)
				.orElseThrow(() -> new EmailNotRegisteredException("Invalid email or password"));

		/*
		 * Account must be active.
		 */
		if (!AccountStatus.ACTIVE.equals(user.getStatus())) {

			throw new AccountNotActivatedException("Account not activated");
		}

		/*
		 * Verify password.
		 */
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {

			throw new InvalidTemporaryPasswordException("Invalid email or password");
		}

		/*
		 * Generate JWT.
		 */
		String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

		return AuthResponse.builder().accessToken(token).tokenType("Bearer").userId(user.getId()).email(user.getEmail())
				.role(user.getRole().name()).build();
	}

	// =========================================================
	// FORGOT PASSWORD
	// =========================================================

	
	@Override
	@Transactional
	public void forgotPassword(ForgotPasswordRequest request) {

		String email = normalizeEmail(request.getEmail());

		authRepository.findByEmail(email).orElseThrow(() -> new EmailNotRegisteredException("Email not registered"));

		/*
		 * Remove previous password-reset tokens.
		 */
		tokenRepository.deleteByEmailAndType(email, TokenType.PASSWORD_RESET);

		/*
		 * Generate reset token.
		 */
		String resetToken = generateToken();

		VerificationToken verificationToken = VerificationToken.builder().email(email).token(resetToken)
				.type(TokenType.PASSWORD_RESET).expiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES))
				.build();

		tokenRepository.save(verificationToken);

		/*
		 * Send reset token by email.
		 */
		emailUtil.sendResetPasswordEmail(email, resetToken);

		log.info("Password reset requested for email={}", email);
	}

	// =========================================================
	// RESET PASSWORD
	// =========================================================

	@Override
	@Transactional
	public void resetPassword(ResetPasswordRequest request) {

		VerificationToken token = tokenRepository.findByTokenAndType(request.getToken(), TokenType.PASSWORD_RESET)
				.orElseThrow(() -> new InvalidResetTokenException("Invalid reset token"));

		/*
		 * Check whether token was already used.
		 */
		if (token.isUsed()) {

			throw new InvalidResetTokenException("Reset token already used");
		}

		/*
		 * Check token expiry.
		 */
		if (token.getExpiresAt().isBefore(LocalDateTime.now())) {

			throw new InvalidResetTokenException("Reset token expired");
		}

		/*
		 * Validate passwords.
		 */
		if (!request.getNewPassword().equals(request.getConfirmPassword())) {

			throw new PasswordMismatchException("Passwords do not match");
		}

		/*
		 * Find user.
		 */
		AuthUser user = authRepository.findByEmail(token.getEmail())
				.orElseThrow(() -> new EmailNotRegisteredException("User not found"));

		/*
		 * Encode new password.
		 */
		user.setPassword(passwordEncoder.encode(request.getNewPassword()));

		/*
		 * Mark reset token as used.
		 */
		token.setUsed(true);

		authRepository.save(user);
		tokenRepository.save(token);

		log.info("Password reset completed for email={}", user.getEmail());
	}

	// =========================================================
	// EMAIL EXISTS
	// =========================================================

	@Override
	@Transactional(readOnly = true)
	public boolean isEmailExists(String email) {

		return authRepository.existsByEmail(normalizeEmail(email));
	}

	// =========================================================
	// HELPERS
	// =========================================================

	private String normalizeEmail(String email) {

		if (email == null || email.isBlank()) {

			throw new IllegalArgumentException("Email cannot be empty");
		}

		return email.trim().toLowerCase();
	}

	/**
	 * Generates a UUID token.
	 *
	 * Used ONLY for password reset.
	 */
	private String generateToken() {

		return UUID.randomUUID().toString();
	}

	/**
	 * Generates a 6-digit temporary password.
	 *
	 * Example: 102384 583921 927451
	 */
	private String generateTemporaryPassword() {

		return String.valueOf(100000 + RANDOM.nextInt(900000));
	}
}