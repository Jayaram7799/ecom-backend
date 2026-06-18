package in.btm.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private long expiration;

	private SecretKey getSigningKey() {

		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	// ==========================
	// Generate Token
	// ==========================

	public String generateToken(String email, String role) {

		Date now = new Date();

		Date expiryDate = new Date(now.getTime() + expiration);

		return Jwts.builder().subject(email).claim("role", role).issuedAt(now).expiration(expiryDate)
				.signWith(getSigningKey()).compact();
	}

	// ==========================
	// Extract Claims
	// ==========================

	private Claims extractAllClaims(String token) {

		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
	}

	// ==========================
	// Extract Email
	// ==========================

	public String extractEmail(String token) {

		return extractAllClaims(token).getSubject();
	}

	// ==========================
	// Extract Role
	// ==========================

	public String extractRole(String token) {

		return extractAllClaims(token).get("role", String.class);
	}

	// ==========================
	// Extract Expiration
	// ==========================

	public Date extractExpiration(String token) {

		return extractAllClaims(token).getExpiration();
	}

	// ==========================
	// Check Expired
	// ==========================

	public boolean isTokenExpired(String token) {

		return extractExpiration(token).before(new Date());
	}

	// ==========================
	// Validate Token
	// ==========================

	public boolean validateToken(String token) {

		if (token == null || token.isBlank()) {

			return false;
		}

		try {

			Claims claims = extractAllClaims(token);

			return claims.getExpiration().after(new Date());

		} catch (ExpiredJwtException e) {

			log.warn("JWT expired");

		} catch (JwtException e) {

			log.warn("Invalid JWT: {}", e.getMessage());

		} catch (Exception e) {

			log.error("JWT validation error", e);
		}

		return false;
	}
}