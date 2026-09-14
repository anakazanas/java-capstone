package assembly.general.api.config;

import assembly.general.api.entity.MembershipStatus;
import assembly.general.api.entity.Role;
import assembly.general.api.entity.User;
import assembly.general.api.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class UserDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail("librarian@library.com")) {
            return;
        }

        User librarian = new User();
        librarian.setEmail("librarian@library.com");
        librarian.setPassword(passwordEncoder.encode("Librarian123!"));
        librarian.setFirstName("Lib");
        librarian.setLastName("Rarian");
        librarian.setPhoneNumber("+1-555-0000");
        librarian.setRole(Role.LIBRARIAN);
        librarian.setMembershipStatus(MembershipStatus.ACTIVE);

        userRepository.save(librarian);
    }
}