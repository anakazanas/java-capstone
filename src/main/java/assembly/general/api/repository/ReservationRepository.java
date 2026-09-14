package assembly.general.api.repository;

import assembly.general.api.entity.Reservation;
import assembly.general.api.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    long countByUser_IdAndStatusIn(UUID userId, List<ReservationStatus> statuses);

    long countByUser_IdAndStatus(UUID userId, ReservationStatus status);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.book JOIN FETCH r.user WHERE r.user.id = :userId AND r.status IN :statuses")
    List<Reservation> findActiveByUserId(@Param("userId") UUID userId, @Param("statuses") List<ReservationStatus> statuses);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.book JOIN FETCH r.user WHERE r.user.id = :userId ORDER BY COALESCE(r.returnedAt, r.reservedAt) DESC")
    Page<Reservation> findHistoryByUserId(@Param("userId") UUID userId, Pageable pageable);
}
