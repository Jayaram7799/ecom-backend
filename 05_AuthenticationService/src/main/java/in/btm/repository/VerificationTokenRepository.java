package in.btm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import in.btm.entity.VerificationToken;
import in.btm.enums.TokenType;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

	
	Optional<VerificationToken> findByTokenAndType(String token, TokenType type);

	void deleteByEmailAndType(String email, TokenType type);
}