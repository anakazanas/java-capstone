package assembly.general.api.service;

import assembly.general.api.dto.ProfileResponse;
import assembly.general.api.entity.ReservationStatus;
import assembly.general.api.entity.User;
import assembly.general.api.repository.ReservationRepository;
import assembly.general.api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    public UserService(UserRepository userRepository, ReservationRepository reservationRepository) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
    }

    public ProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + userId));

        long activeReservations = reservationRepository.countByUser_IdAndStatusIn(
                userId, List.of(ReservationStatus.RESERVED, ReservationStatus.CHECKED_OUT));
        long borrowingHistory = reservationRepository.countByUser_IdAndStatus(
                userId, ReservationStatus.RETURNED);

        return new ProfileResponse(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getPhoneNumber(),
                user.getRole(), user.getMembershipStatus(), user.getCreatedAt(),
                activeReservations, borrowingHistory
        );
    }
}