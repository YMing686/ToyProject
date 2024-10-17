package org.demo.apigateway.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.KeyStore;
import java.security.PublicKey;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements WebFilter {

  private static final String KEYSTORE_FILE = "jwtkeystore.jks";
  private static final String KEY_ALIAS = "jwtkey";
  private static final String KEYSTORE_PASSWORD = "password";
  private final KeyStore keyStore;

  public JwtAuthenticationFilter() throws Exception {
    keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    try (var inputStream = getClass().getClassLoader().getResourceAsStream(KEYSTORE_FILE)) {
      keyStore.load(inputStream, KEYSTORE_PASSWORD.toCharArray());
    }
  }

  private PublicKey getPublicKey() throws Exception {
    return keyStore.getCertificate(KEY_ALIAS).getPublicKey();
  }


  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer")) {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }

    String token = authHeader.substring(7);

    try {
      Claims claims = Jwts.parser()
          .setSigningKey(getPublicKey())
          .parseClaimsJws(token)
          .getBody();
      String userId = claims.get("id", String.class);
      String roles = claims.get("role", String.class);

      ServerWebExchange mutatedExchange = exchange.mutate()
          .request(r -> r
              .header("userId", userId)
              .header("roles", roles))
          .build();
      return chain.filter(mutatedExchange);
    } catch (Exception e) {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }
  }
}
