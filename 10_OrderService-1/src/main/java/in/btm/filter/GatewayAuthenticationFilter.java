package in.btm.filter;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(GatewayAuthenticationFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String email = request.getHeader("X-User-Email");
		String role = request.getHeader("X-User-Role");

		log.info("X-User-Email : {}", email);
		log.info("X-User-Role  : {}", role);

		if (email != null && !email.isBlank() && role != null && !role.isBlank()
				&& SecurityContextHolder.getContext().getAuthentication() == null) {

			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null,
					List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));

			SecurityContextHolder.getContext().setAuthentication(authentication);

			log.info("Authentication created successfully");
			log.info("Authorities : {}", authentication.getAuthorities());
		}

		filterChain.doFilter(request, response);
	}
}