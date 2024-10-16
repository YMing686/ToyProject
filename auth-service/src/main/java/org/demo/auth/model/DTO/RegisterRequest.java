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
public class RegisterRequest {

  @NonNull
  private String firstname;

  @NonNull
  private String lastname;

  @NonNull
  private String username;

  @NonNull
  private String password;
}
