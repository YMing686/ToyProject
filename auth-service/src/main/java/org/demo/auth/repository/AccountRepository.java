package org.demo.auth.repository;

import java.util.Optional;
import org.demo.auth.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
  Optional<Account> findByUsername(String username);
}
