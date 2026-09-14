package assembly.general.api.dto;

import java.util.List;

public class ActiveReservationsResponse {
    private List<ActiveReservationItem> reservations;
    private int totalActive;

    public ActiveReservationsResponse(List<ActiveReservationItem> reservations, int totalActive) {
        this.reservations = reservations;
        this.totalActive = totalActive;
    }

    public List<ActiveReservationItem> getReservations() { return reservations; }
    public int getTotalActive() { return totalActive; }
}