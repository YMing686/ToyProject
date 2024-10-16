package org.demo.auth.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private static final String PRIVATE_KEY = "secret";
  private static final String PUBLIC_KEY = "secret";
  private static final long EXPIRATION_TIME = 86400000L;

  public String generateToken(String username) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("username", username);
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(username)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
        // Sign with SECRET KEY use RSA with SHA-256
        .signWith(SignatureAlgorithm.RS256, PRIVATE_KEY)
        .compact();
  }

  public String extractUsername(String token) {
    return Jwts.parser().setSigningKey(PUBLIC_KEY).parseClaimsJws(token).getBody().getSubject();
  }

  public boolean validateToken(String token) {
    return !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return Jwts.parser().setSigningKey(PUBLIC_KEY).parseClaimsJws(token).getBody().getExpiration().before(new Date());
  }

}
