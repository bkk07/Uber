package com.uber.gateway.filter;
import com.uber.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    public AuthFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    public static class Config {
        private String requiredRole;

        public String getRequiredRole() {
            return requiredRole;
        }

        public void setRequiredRole(String requiredRole) {
            this.requiredRole = requiredRole;
        }
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath(); // Use getPath() for consistent path handling

            logger.info("AuthFilter: Incoming request path: {}", path); // Log the exact path received

            // Check for public endpoints (order matters here, public paths first)
            if (path.startsWith("/auth") ||
                    path.equals("/api/drivers/register") ||
                    path.equals("/api/drivers/validate")) {
                logger.info("AuthFilter: Bypassing JWT validation for public path: {}", path); // Log if bypass occurs
                return chain.filter(exchange);
            }

            // If not a public path, proceed with JWT validation
            logger.warn("AuthFilter: Attempting JWT validation for path: {}", path); // Log if validation is attempted

            List<String> authHeaders = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
            if (authHeaders == null || authHeaders.isEmpty()) {
                logger.error("AuthFilter: Missing Authorization header for path: {}. Returning UNAUTHORIZED.", path);
                return this.onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = authHeaders.get(0);
            if (!authHeader.startsWith("Bearer ")) {
                logger.error("AuthFilter: Invalid Authorization header format for path: {}. Returning UNAUTHORIZED.", path);
                return this.onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                if (!jwtUtil.validateToken(token)) {
                    logger.error("AuthFilter: JWT Token is invalid or expired for path: {}. Returning UNAUTHORIZED.", path);
                    return this.onError(exchange, "JWT Token is invalid or expired", HttpStatus.UNAUTHORIZED);
                }

                String role = jwtUtil.extractRole(token);
                String userId = jwtUtil.extractUserId(token);

                // Role check
                if (config.getRequiredRole() != null && !config.getRequiredRole().equalsIgnoreCase(role)) {
                    logger.error("AuthFilter: Access Denied: Insufficient role '{}' for path: {}. Required: {}. Returning FORBIDDEN.", role, path, config.getRequiredRole());
                    return this.onError(exchange, "Access Denied: Insufficient role", HttpStatus.FORBIDDEN);
                }

                // Forward claims to downstream
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-Auth-User-Id", userId)
                        .header("X-Auth-User-Role", role)
                        .build();

                logger.info("AuthFilter: JWT validation successful for path: {}. Forwarding request.", path);
                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                logger.error("AuthFilter: Error during JWT processing for path: {}: {}", path, e.getMessage(), e);
                return this.onError(exchange, "Error processing JWT token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        // Optionally set a response body for more client-side detail
        // response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        // String responseBody = "{\"status\":" + httpStatus.value() + ", \"error\":\"" + err + "\"}";
        // DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes());
        logger.error("AuthFilter: Sending error response - Status: {}, Message: {}", httpStatus, err);
        return response.setComplete(); // .then(response.writeWith(Mono.just(buffer))); // Uncomment if adding body
    }
}