package org.demo.auth.model;

import lombok.Data;
import lombok.Getter;

@Getter
public enum Role {
  ADMIN((short) 0, "ROLE_ADMIN"),
  USER((short) 1, "ROLE_USER"),
  GUEST((short) 2, "ROLE_GUEST");

  private final short id;
  private final String name;

  Role(short id, String name) {
    this.id = id;
    this.name = name;
  }

  public static String getRoleById(short id) {
    for (Role role : Role.values()) {
      if (role.getId() == id) {
        return role.name;
      }
    }
    throw new IllegalArgumentException("Invalid role id: " + id);
  }
}
