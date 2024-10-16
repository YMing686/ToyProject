package org.demo.auth.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
  @NonNull
  private String username;

  @NonNull
  private String password;
}
