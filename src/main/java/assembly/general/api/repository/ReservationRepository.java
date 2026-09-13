package assembly.general.api.repository;

import assembly.general.api.entity.Reservation;
import assembly.general.api.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    long countByUser_IdAndStatusIn(UUID userId, List<ReservationStatus> statuses);
    long countByUser_IdAndStatus(UUID userId, ReservationStatus status);
}