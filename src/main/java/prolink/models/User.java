package prolink.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users") // Matches your XAMPP table name
@Data // This Lombok annotation creates all getters/setters automatically!
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Column(name = "full_name")
    private String fullName;

    private String role;

    @Column(name = "is_validated")
    private boolean isValidated;
}