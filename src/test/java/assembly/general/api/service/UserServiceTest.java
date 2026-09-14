package assembly.general.api.service;

import assembly.general.api.dto.ProfileResponse;
import assembly.general.api.entity.MembershipStatus;
import assembly.general.api.entity.ReservationStatus;
import assembly.general.api.entity.Role;
import assembly.general.api.entity.User;
import assembly.general.api.repository.ReservationRepository;
import assembly.general.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ReservationRepository reservationRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, reservationRepository);
    }

    @Test
    void getProfile_returnsCorrectStatsAndFields() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("jane.doe@example.com");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setPhoneNumber("+1-555-0123");
        user.setRole(Role.PATRON);
        user.setMembershipStatus(MembershipStatus.ACTIVE);
        user.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reservationRepository.countByUser_IdAndStatusIn(eq(userId), any())).thenReturn(2L);
        when(reservationRepository.countByUser_IdAndStatus(userId, ReservationStatus.RETURNED)).thenReturn(45L);

        ProfileResponse response = userService.getProfile(userId);

        assertThat(response.getEmail()).isEqualTo("jane.doe@example.com");
        assertThat(response.getActiveReservations()).isEqualTo(2);
        assertThat(response.getBorrowingHistory()).isEqualTo(45);
        assertThat(response.getMembershipStatus()).isEqualTo(MembershipStatus.ACTIVE);
    }
}