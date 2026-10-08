
package in.btm.entity;

import java.time.LocalDateTime;

import in.btm.enums.TokenType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "verification_tokens", indexes = { @Index(name = "idx_verification_token", columnList = "token"),
		@Index(name = "idx_verification_email", columnList = "email") })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 255)
	private String email;

	@Column(nullable = false, unique = true, length = 255)
	private String token;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private TokenType type;

	@Column(nullable = false)
	private LocalDateTime expiresAt;

	@Column(nullable = false)
	@Builder.Default
	private boolean used = false;

	@Column(nullable = false)
	@Builder.Default
	private int attempts = 0;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}
}
