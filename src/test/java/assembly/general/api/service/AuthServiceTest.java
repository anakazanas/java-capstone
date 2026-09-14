package assembly.general.api.service;

import assembly.general.api.dto.LoginRequest;
import assembly.general.api.dto.LoginResponse;
import assembly.general.api.dto.RegisterRequest;
import assembly.general.api.dto.RegisterResponse;
import assembly.general.api.entity.MembershipStatus;
import assembly.general.api.entity.Role;
import assembly.general.api.entity.User;
import assembly.general.api.exception.EmailAlreadyExistsException;
import assembly.general.api.exception.InvalidCredentialsException;
import assembly.general.api.repository.UserRepository;
import assembly.general.api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("jane.doe@example.com");
        registerRequest.setPassword("SecurePass123!");
        registerRequest.setFirstName("Jane");
        registerRequest.setLastName("Doe");
        registerRequest.setPhoneNumber("+1-555-0123");
    }

    @Test
    void register_savesNewPatronWithEncodedPassword() {
        when(userRepository.existsByEmail("jane.doe@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123!")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            u.setCreatedAt(Instant.now());
            return u;
        });

        RegisterResponse response = authService.register(registerRequest);

        assertThat(response.getEmail()).isEqualTo("jane.doe@example.com");
        assertThat(response.getRole()).isEqualTo(Role.PATRON);
        assertThat(response.getMembershipStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(response.getMessage()).isEqualTo("Registration successful");
    }

    @Test
    void register_throwsWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("jane.doe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void login_succeedsWithCorrectPassword() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("jane.doe@example.com");
        user.setPassword("hashed-password");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setRole(Role.PATRON);

        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SecurePass123!", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("fake-jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("jane.doe@example.com");
        loginRequest.setPassword("SecurePass123!");

        LoginResponse response = authService.login(loginRequest);

        assertThat(response.getAccessToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(86400L);
        assertThat(response.getUser().getEmail()).isEqualTo("jane.doe@example.com");
    }

    @Test
    void login_throwsOnWrongPassword() {
        User user = new User();
        user.setEmail("jane.doe@example.com");
        user.setPassword("hashed-password");

        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword!", "hashed-password")).thenReturn(false);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("jane.doe@example.com");
        loginRequest.setPassword("WrongPassword!");

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throwsWhenEmailNotFound() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nobody@example.com");
        loginRequest.setPassword("whatever");

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}