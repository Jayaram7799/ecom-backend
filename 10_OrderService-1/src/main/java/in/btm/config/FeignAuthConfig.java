package in.btm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class FeignAuthConfig {

	@Bean
	RequestInterceptor requestInterceptor() {
		return template -> {

			ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

			if (attrs != null) {

				HttpServletRequest req = attrs.getRequest();

				template.header("X-User-Email", req.getHeader("X-User-Email"));

				template.header("X-User-Role", req.getHeader("X-User-Role"));
			}
		};
	}
}
