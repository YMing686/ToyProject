package org.demo.auth.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private static final String KEYSTORE_FILE = "jwtkeystore.jks";  // Keystore file path
  private static final String KEY_ALIAS = "jwtkey";               // Alias for RSA key pair in the keystore
  private static final String KEYSTORE_PASSWORD = "password";     // Keystore password
  private static final String KEY_PASSWORD = "password";          // Private key password (same as keypass during generation)
  private static final Long EXPIRATION_TIME = 86400000L;          // Token expiration time (1 day)

  private KeyStore keyStore;

  // Load keystore
  public JwtTokenProvider() throws Exception {
    keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    try (var inputStream = getClass().getClassLoader().getResourceAsStream(KEYSTORE_FILE)) {
      keyStore.load(inputStream, KEYSTORE_PASSWORD.toCharArray());
    }
  }

  // Get private key for signing JWT
  private PrivateKey getPrivateKey() throws Exception {
    return (PrivateKey) keyStore.getKey(KEY_ALIAS, KEY_PASSWORD.toCharArray());
  }

  // Get public key for verifying JWT
  private PublicKey getPublicKey() throws Exception {
    return keyStore.getCertificate(KEY_ALIAS).getPublicKey();
  }

  // Generate JWT with RSA signature (RS256)
  public String generateToken(String username) throws Exception {
    Map<String, Object> claims = new HashMap<>();
    claims.put("username", username);
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(username)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
        .signWith(SignatureAlgorithm.RS256, getPrivateKey())  // Use private key from keystore
        .compact();
  }

  // Extract username from JWT
  public String extractUsername(String token) throws Exception {
    return Jwts.parser()
        .setSigningKey(getPublicKey())  // Use public key from keystore
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  // Validate the token (check if it's expired)
  public boolean validateToken(String token) throws Exception {
    return !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) throws Exception {
    return Jwts.parser()
        .setSigningKey(getPublicKey())  // Use public key from keystore
        .parseClaimsJws(token)
        .getBody()
        .getExpiration()
        .before(new Date());
  }
}