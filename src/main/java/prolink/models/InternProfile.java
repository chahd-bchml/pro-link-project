package prolink.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "intern_profiles")
@Data
public class InternProfile {

    @Id
    @Column(name = "user_id")
    private Integer userId; // Primary Key and Foreign Key to Users table

    @Column(name = "university_id", nullable = false)
    private String universityId;

    @Column(name = "mentor_id")
    private Integer mentorId; // The ID of the assigned Mentor from the Users table

    @Column(name = "valid_until")
    private LocalDate validUntil;

    // One-to-One relationship back to the core User
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
}