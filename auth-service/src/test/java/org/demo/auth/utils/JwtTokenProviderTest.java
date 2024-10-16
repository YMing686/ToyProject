package org.demo.auth.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

  private JwtTokenProvider jwtTokenProvider;

  @BeforeEach
  void setUp() throws Exception {
    jwtTokenProvider = new JwtTokenProvider();
  }

  @Test
  void testGenerateToken() throws Exception {
    String username = "testuser";
    String token = jwtTokenProvider.generateToken(username);
    assertNotNull(token);
    System.out.println("Generated Token: " + token);
  }

  @Test
  void testExtractUsername() throws Exception {
    String username = "testuser";
    String token = jwtTokenProvider.generateToken(username);

    String extractedUsername = jwtTokenProvider.extractUsername(token);
    assertEquals(username, extractedUsername);
  }

  @Test
  void testValidateToken() throws Exception {
    String token = jwtTokenProvider.generateToken("testuser");

    boolean isValid = jwtTokenProvider.validateToken(token);
    assertTrue(isValid);
  }
  @Test
  void testInvalidToken() {
    String invalidToken = "invalid.token.here";

    JwtException exception = assertThrows(JwtException.class, () -> {
      jwtTokenProvider.extractUsername(invalidToken);
    });

    assertNotNull(exception);
  }
}