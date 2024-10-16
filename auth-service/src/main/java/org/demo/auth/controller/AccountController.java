package org.demo.auth.controller;

import lombok.RequiredArgsConstructor;
import org.demo.auth.model.DTO.ErrorResponse;
import org.demo.auth.model.DTO.LoginRequest;
import org.demo.auth.model.DTO.RegisterRequest;
import org.demo.auth.model.DTO.RegisterResponse;
import org.demo.auth.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {
  private final AuthenticationService authenticationService;

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
    // Register a new account
    try {
      RegisterResponse response = this.authenticationService.register(request);
      return ResponseEntity.ok(response);
    } catch (Exception ex) {
      return ResponseEntity.internalServerError().body(new ErrorResponse(ex.getMessage()));
    }
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    // Login an account
    try {
      RegisterResponse response = this.authenticationService.login(request);
      return ResponseEntity.ok(response);
    } catch (Exception ex) {
      ex.printStackTrace();
      return ResponseEntity.internalServerError().body(new ErrorResponse(ex.getLocalizedMessage()));
    }
  }
}
