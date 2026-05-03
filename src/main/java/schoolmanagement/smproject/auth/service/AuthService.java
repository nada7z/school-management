package schoolmanagement.smproject.auth.service;

import schoolmanagement.smproject.auth.entity.User;
import schoolmanagement.smproject.auth.repository.AuthRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class AuthService {
    
    private final AuthRepository authRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Constructor injection (Spring best practice)
    public AuthService(AuthRepository authRepository) {
        this.authRepository = authRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Validates login credentials
     * @return Optional<User> if login succeeds, empty if invalid
     */
    public Optional<User> login(String username, String rawPassword) {
        Optional<User> user = authRepository.findByUsername(username);
        
        if (user.isPresent() && passwordEncoder.matches(rawPassword, user.get().getPasswordHash())) {
            return user; // ✅ Login successful
        }
        return Optional.empty(); // ❌ Invalid username or password
    }

    /**
     * Creates a new user (Admin only)
     */
    @Transactional
    public User registerUser(String username, String rawPassword, String email, User.UserRole role) {
        if (authRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (authRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Hash password BEFORE saving
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User newUser = new User(username, encodedPassword, email, role);
        return authRepository.save(newUser);
    }

    /**
     * Changes password with old password verification
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = authRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        authRepository.save(user);
    }

    /**
     * Check if user has required role
     */
    public boolean hasRole(User user, User.UserRole requiredRole) {
        return user != null && user.getRole() == requiredRole;
    }
}