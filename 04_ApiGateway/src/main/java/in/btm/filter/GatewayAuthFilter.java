package in.btm.filter;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.btm.dto.ApiResponse;
import in.btm.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@Order(-1)
@RequiredArgsConstructor
public class GatewayAuthFilter implements GlobalFilter {

	private static final Logger log = LoggerFactory.getLogger(GatewayAuthFilter.class);

	private final JwtUtil jwtUtil;

	private final ObjectMapper objectMapper;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		String path = exchange.getRequest().getURI().getPath();

		String method = exchange.getRequest().getMethod().name();

		log.info("Incoming Request : {} {}", method, path);

		AntPathMatcher matcher = new AntPathMatcher();

		List<String> publicUrls = List.of("/auth/**", "/api/users", "/users/*/activate");

		// Allow OPTIONS
		if ("OPTIONS".equalsIgnoreCase(method)) {
			return chain.filter(exchange);
		}

		// Public Endpoints
		if (publicUrls.stream().anyMatch(pattern -> matcher.match(pattern, path))) {

			return chain.filter(exchange);
		}

		String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {

			return errorResponse(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
		}

		String token = authHeader.substring(7);

		try {

			if (!jwtUtil.validateToken(token)) {

				return errorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid JWT token");
			}

			Integer userId = jwtUtil.extractUserId(token);

			String email = jwtUtil.extractEmail(token);

			String role = jwtUtil.extractRole(token);

			log.info("Authenticated User -> userId={}, email={}, role={}", userId, email, role);

			ServerHttpRequest request = exchange.getRequest().mutate().header("X-User-Id", String.valueOf(userId))
					.header("X-User-Email", email).header("X-User-Role", role).build();

			return chain.filter(exchange.mutate().request(request).build());

		} catch (ExpiredJwtException ex) {

			return errorResponse(exchange, HttpStatus.UNAUTHORIZED, "JWT token expired");

		} catch (SecurityException ex) {

			return errorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid JWT signature");

		} catch (MalformedJwtException ex) {

			return errorResponse(exchange, HttpStatus.UNAUTHORIZED, "Malformed JWT token");

		} catch (UnsupportedJwtException ex) {

			return errorResponse(exchange, HttpStatus.UNAUTHORIZED, "Unsupported JWT token");

		} catch (Exception ex) {

			log.error("JWT validation failed", ex);

			return errorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid JWT token");
		}
	}

	private Mono<Void> errorResponse(ServerWebExchange exchange, HttpStatus status, String message) {

		ServerHttpResponse response = exchange.getResponse();

		response.setStatusCode(status);

		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

		try {

			ApiResponse<Object> apiResponse = ApiResponse.builder().success(false).message(message)
					.error(status.getReasonPhrase()).status(status.value())
					.path(exchange.getRequest().getPath().value()).timestamp(LocalDateTime.now()).build();

			byte[] body = objectMapper.writeValueAsBytes(apiResponse);

			DataBuffer buffer = response.bufferFactory().wrap(body);

			return response.writeWith(Mono.just(buffer));

		} catch (Exception ex) {

			log.error("Failed to write error response", ex);

			return response.setComplete();
		}
	}
}