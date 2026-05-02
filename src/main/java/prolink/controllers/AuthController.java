// This allows your phone to talk to the Java code
package prolink.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import prolink.repositories.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prolink.models.User;

import java.util.Map;

@RestController // REQUIRED: Tells Spring this is a web controller
@RequestMapping("/api/auth") // REQUIRED: Matches your Flutter baseUrl
@CrossOrigin(origins = "*") // REQUIRED: Allows your phone to talk to your laptop
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> payload) {
        // 1. Extract data from the Flutter Request
        String email = (String) payload.get("email");
        String password = (String) payload.get("password");
        String fullName = (String) payload.get("fullName");
        String role = (String) payload.get("role"); // 'INTERN' or 'MENTOR'
        String department = (String) payload.get("department");

        // 2. Create and Save the main User
        User user = new User();
        user.setEmail(email);
        user.setPassword(password); // Note: In real apps, we hash this!
        user.setFullName(fullName);
        user.setRole(role);
        user.setDepartment(department);
        user.setValidated(false); // Admin must approve later

        User savedUser = userRepository.save(user);

        // 3. If they are an Intern, save to Intern_Profiles table too
        if (role.equals("INTERN")) {
            String universityId = (String) payload.get("universityId");

            // Manual SQL insert or use an InternProfileRepository
            jdbcTemplate.update(
                    "INSERT INTO Intern_Profiles (user_id, university_id) VALUES (?, ?)",
                    savedUser.getId(), universityId
            );
        }

        return ResponseEntity.ok("Registration Successful");
    }
    //This method will look for the email, check the password, and return the user details.
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        // 1. Find user by email
        return userRepository.findByEmail(email)
                .map(user -> {
                    // 2. Check Password
                    if (!user.getPassword().equals(password)) {
                        return ResponseEntity.status(401).body("Invalid Password");
                    }

                    // 3. Check Validation Status (The Fix)
                    if (!user.isValidated()) {
                        return ResponseEntity.status(403).body("Account not yet validated by Admin");
                    }

                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.status(404).body("User not found"));
    }
}
