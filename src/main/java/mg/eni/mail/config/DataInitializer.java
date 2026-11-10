
package mg.eni.mail.config;

import mg.eni.mail.entity.Role;
import mg.eni.mail.entity.User;
import mg.eni.mail.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createInitialAdmin(
            UserRepository users,
            PasswordEncoder encoder,
            @Value("${app.admin.email:}") String email,
            @Value("${app.admin.password:}") String password) {

        return args -> {
            if (email != null && !email.isBlank()
                    && password != null && !password.isBlank()
                    && !users.existsByEmailIgnoreCase(email)) {

                User admin = new User();
                admin.setFirstName("Administrateur");
                admin.setLastName("ENI");
                admin.setEmail(email.trim().toLowerCase());
                admin.setPassword(encoder.encode(password));
                admin.setRole(Role.ADMIN);
                admin.setActive(true);

                users.save(admin);
            }
        };
    }
}
