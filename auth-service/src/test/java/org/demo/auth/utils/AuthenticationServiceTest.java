package org.demo.auth.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.Optional;
import org.demo.auth.model.DTO.LoginRequest;
import org.demo.auth.model.DTO.RegisterRequest;
import org.demo.auth.model.DTO.RegisterResponse;
import org.demo.auth.model.entity.Account;
import org.demo.auth.repository.AccountRepository;
import org.demo.auth.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthenticationServiceTest {

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private PasswordEncoder passwordEncoder;  // Mock the PasswordEncoder

  @InjectMocks
  private AuthenticationService authenticationService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testRegister_Success() throws Exception {
    // Given
    RegisterRequest request = new RegisterRequest();
    request.setUsername("testuser");
    request.setFirstname("Test");
    request.setLastname("User");
    request.setPassword(Base64.getEncoder().encodeToString("password".getBytes()));

    when(accountRepository.findByUsername("testuser")).thenReturn(Optional.empty());
    when(passwordEncoder.encode(any(String.class))).thenReturn("encoded-password");
    when(accountRepository.save(any(Account.class))).thenReturn(
        Account.builder()
            .accountId(1)
            .username("testuser")
            .password("encoded-password")
            .permission((short) 0)
            .build()
    );
    when(jwtTokenProvider.generateToken(1, "testuser", (short) 0)).thenReturn("jwt-token");

    // When
    RegisterResponse response = authenticationService.register(request);

    // Then
    assertNotNull(response);
    assertEquals("jwt-token", response.getToken());
    verify(accountRepository, times(1)).save(any(Account.class));
  }

  @Test
  public void testRegister_UserAlreadyExists() {
    // Given
    RegisterRequest request = new RegisterRequest();
    request.setUsername("existinguser");
    request.setPassword(Base64.getEncoder().encodeToString("password".getBytes()));

    Account existingAccount = new Account();
    when(accountRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingAccount));

    // When & Then
    Exception exception = assertThrows(RuntimeException.class, () -> {
      authenticationService.register(request);
    });

    assertEquals("User already exist", exception.getMessage());
    verify(accountRepository, never()).save(any(Account.class));
  }

  @Test
  public void testLogin_Success() throws Exception {
    // Given
    LoginRequest request = new LoginRequest();
    request.setUsername("testuser");
    request.setPassword(Base64.getEncoder().encodeToString("password".getBytes()));

    Account account = Account.builder()
        .accountId(1)
        .username("testuser")
        .password("encoded-password")
        .permission((short) 0)
        .build();

    when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
    when(passwordEncoder.matches(any(CharSequence.class), any(String.class))).thenReturn(
        true);  // Simulate password match
    when(jwtTokenProvider.generateToken(1, "testuser", (short) 0)).thenReturn("jwt-token");

    // When
    RegisterResponse response = authenticationService.login(request);

    // Then
    assertNotNull(response);
    assertEquals("jwt-token", response.getToken());
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  public void testLogin_InvalidPassword() {
    // Given
    LoginRequest request = new LoginRequest();
    request.setUsername("testuser");
    request.setPassword(Base64.getEncoder().encodeToString("wrongpassword".getBytes()));

    Account account = Account.builder()
        .username("testuser")
        .password("encoded-password")
        .build();

    when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
    when(passwordEncoder.matches(any(CharSequence.class), any(String.class))).thenReturn(
        false);  // Simulate password mismatch

    // When & Then
    Exception exception = assertThrows(RuntimeException.class, () -> {
      authenticationService.login(request);
    });

    assertEquals("Invalid password", exception.getMessage());
    verify(authenticationManager, never()).authenticate(
        any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  public void testLogin_UserNotFound() {
    // Given
    LoginRequest request = new LoginRequest();
    request.setUsername("nonexistentuser");
    request.setPassword(Base64.getEncoder().encodeToString("password".getBytes()));

    when(accountRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

    // When & Then
    Exception exception = assertThrows(RuntimeException.class, () -> {
      authenticationService.login(request);
    });

    assertEquals("User not found", exception.getMessage());
    verify(authenticationManager, never()).authenticate(
        any(UsernamePasswordAuthenticationToken.class));
  }
}