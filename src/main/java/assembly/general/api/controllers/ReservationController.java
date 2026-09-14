package assembly.general.api.controllers;

import assembly.general.api.dto.*;
import assembly.general.api.security.CustomUserDetails;
import assembly.general.api.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ReserveRequest request) {
        ReservationResponse response = reservationService.reserve(principal.getUser().getId(), request.getBookId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ActiveReservationsResponse> getActive(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(reservationService.getActiveReservations(principal.getUser().getId()));
    }

    @PostMapping("/{reservationId}/checkout")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<CheckoutResponse> checkout(
            @PathVariable UUID reservationId,
            @RequestBody(required = false) CheckoutRequest request) {
        String notes = request != null ? request.getNotes() : null;
        return ResponseEntity.ok(reservationService.checkout(reservationId, notes));
    }

    @PostMapping("/{reservationId}/return")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<ReturnResponse> returnBook(
            @PathVariable UUID reservationId,
            @Valid @RequestBody ReturnRequest request) {
        return ResponseEntity.ok(reservationService.returnBook(reservationId, request.getCondition(), request.getNotes()));
    }

    @GetMapping("/history")
    public ResponseEntity<PageResponse<HistoryItem>> history(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reservationService.getHistory(principal.getUser().getId(), page, size));
    }
}