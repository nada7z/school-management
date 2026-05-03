package schoolmanagement.smproject.common;

import schoolmanagement.smproject.auth.entity.User;
import schoolmanagement.smproject.auth.repository.AuthRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AuthRepository authRepository;

    public DataSeeder(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if "admin" user already exists
        if (authRepository.findByUsername("admin").isEmpty()) {
            System.out.println("🚀 Creating default admin user...");
            
            // Hash the password securely
            String rawPassword = "admin123";
            String hashedPassword = new BCryptPasswordEncoder().encode(rawPassword);
            
            // Create the user
            User admin = new User(
                "admin", 
                hashedPassword, 
                "admin@school.com", 
                User.UserRole.ADMIN
            );
            
            // Save to database
            authRepository.save(admin);
            
            System.out.println("✅ SUCCESS! Login with:");
            System.out.println("   Username: admin");
            System.out.println("   Password: admin123");
        } else {
            System.out.println("👋 Admin user already exists. Skipping creation.");
        }
    }
}