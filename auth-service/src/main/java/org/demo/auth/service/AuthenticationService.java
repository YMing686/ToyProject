package org.demo.auth.service;

import jakarta.transaction.Transactional;
import java.util.Base64;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.demo.auth.model.DTO.LoginRequest;
import org.demo.auth.model.DTO.RegisterRequest;
import org.demo.auth.model.DTO.RegisterResponse;
import org.demo.auth.model.entity.Account;
import org.demo.auth.model.entity.Role;
import org.demo.auth.repository.AccountRepository;
import org.demo.auth.utils.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
  private final AccountRepository accountRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final AuthenticationManager authenticationManager;
  private final PasswordEncoder passwordEncoder;

  // TODO: Replace it with a more secure way to store passwords Like RSA
  private String getDecodedPassword(String password) {
    return new String(Base64.getDecoder().decode(password));
  }

  @Transactional
  public RegisterResponse register(RegisterRequest request) throws Exception {
    Optional<Account> existingAccount = this.accountRepository.findByUsername(
        request.getUsername());
    if (existingAccount.isPresent()) {
      logger.warn("Registration failed: User already exists with username: {}",
          request.getUsername());
      throw new RuntimeException("User already exist");
    }
    Account savedAccount = Account.builder()
        .firstname(request.getFirstname())
        .lastname(request.getLastname())
        .username(request.getUsername())
        .password(passwordEncoder.encode(getDecodedPassword(request.getPassword())))
        .permission(Role.getIdByRole("ROLE_USER"))
        .build();
    savedAccount = this.accountRepository.save(savedAccount);
    logger.info("Account successfully created for user: {}", request.getUsername());

    String jwtToken = this.jwtTokenProvider.generateToken(
        savedAccount.getAccountId(), savedAccount.getUsername(), savedAccount.getPermission());
    logger.debug("JWT token generated for user: {}", request.getUsername());

    return new RegisterResponse(jwtToken);
  }

  public RegisterResponse login(LoginRequest request) throws Exception {
    logger.info("Logging in user: {}", request.getUsername());

    // Decode password from request
    String decodedPassword = getDecodedPassword(request.getPassword());
    logger.info("Password for user {} has been decoded", request.getUsername());

    // Find the user
    Account account = this.accountRepository
        .findByUsername(request.getUsername())
        .orElseThrow(() -> {
          logger.warn("Login failed: User not found with username: {}", request.getUsername());
          return new RuntimeException("User not found");
        });

    // Check if the password matches
    boolean passwordMatches = passwordEncoder.matches(decodedPassword, account.getPassword());
    logger.info("Password match result for user {}: {}", request.getUsername(), passwordMatches);

    if (!passwordMatches) {
      logger.warn("Login failed: Invalid password for user: {}", request.getUsername());
      throw new RuntimeException("Invalid password");
    }

    // Authenticate the user
    logger.info("Authenticating user: {}", request.getUsername());
    this.authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), decodedPassword)
    );

    // Generate JWT token
    String jwtToken = this.jwtTokenProvider.generateToken(account.getAccountId(),
        account.getUsername(), account.getPermission());
    logger.info("JWT token generated for user: {}", request.getUsername());

    return new RegisterResponse(jwtToken);
  }
}
