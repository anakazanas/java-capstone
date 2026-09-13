package assembly.general.api.service;

import assembly.general.api.dto.*;
import assembly.general.api.entity.MembershipStatus;
import assembly.general.api.entity.Role;
import assembly.general.api.entity.User;
import assembly.general.api.exception.EmailAlreadyExistsException;
import assembly.general.api.exception.InvalidCredentialsException;
import assembly.general.api.repository.UserRepository;
import assembly.general.api.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(Role.PATRON);
        user.setMembershipStatus(MembershipStatus.ACTIVE);

        User saved = userRepository.save(user);

        return new RegisterResponse(
                saved.getId(), saved.getEmail(), saved.getFirstName(), saved.getLastName(),
                saved.getRole(), saved.getMembershipStatus(), saved.getCreatedAt(),
                "Registration successful"
        );
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        UserSummary summary = new UserSummary(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole()
        );

        return new LoginResponse(token, "Bearer", jwtService.getExpirationSeconds(), summary);
    }
}