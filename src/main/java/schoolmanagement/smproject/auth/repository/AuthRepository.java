package schoolmanagement.smproject.auth.repository;

import schoolmanagement.smproject.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<User, Long> {
    
    // Find user by login username
    Optional<User> findByUsername(String username);
    
    // Find user by email (for password reset / validation)
    Optional<User> findByEmail(String email);
    
    // Check uniqueness before registration
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}