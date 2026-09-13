package assembly.general.api.dto;

import assembly.general.api.entity.MembershipStatus;
import assembly.general.api.entity.Role;

import java.time.Instant;
import java.util.UUID;

public class RegisterResponse {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private MembershipStatus membershipStatus;
    private Instant createdAt;
    private String message;

    public RegisterResponse(UUID userId, String email, String firstName, String lastName,
                            Role role, MembershipStatus membershipStatus, Instant createdAt, String message) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.membershipStatus = membershipStatus;
        this.createdAt = createdAt;
        this.message = message;
    }

    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Role getRole() { return role; }
    public MembershipStatus getMembershipStatus() { return membershipStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public String getMessage() { return message; }
}