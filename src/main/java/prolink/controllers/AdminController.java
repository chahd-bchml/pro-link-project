package prolink.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import prolink.models.User;
import org.springframework.http.ResponseEntity;
import prolink.repositories.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin") // Matches your AdminService baseUrl
@CrossOrigin(origins = "*")
public class AdminController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    // 1. Get all users where is_validated is false
    @GetMapping("/pending")
    public List<User> getPendingUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.isValidated())
                .collect(Collectors.toList());
    }

    // 2. Get all Mentors (to fill the dropdown in the approval dialog)
    @GetMapping("/mentors")
    public List<User> getMentors() {
        return userRepository.findAll().stream()
                .filter(user -> "MENTOR".equals(user.getRole()))
                .collect(Collectors.toList());
    }

    // 3. Approve an intern and assign their department and mentor
    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approveIntern(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> updates) {

        return userRepository.findById(id).map(user -> {
            user.setValidated(true);
            user.setDepartment((String) updates.get("department"));
            userRepository.save(user);

            // Update the intern_profiles table with the mentor_id
            Integer mentorId = (Integer) updates.get("mentorId");
            if (mentorId != null){
                jdbcTemplate.update(
                        "UPDATE intern_profiles SET university_id = ?, mentor_id = ? WHERE user_id = ?",
                        mentorId, id
                );
            }

            return ResponseEntity.ok("Intern approved and assigned!");
        }).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/active")
    public List<User> getActiveUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.isValidated()) // Only show those already approved
                .collect(Collectors.toList());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody Map<String, Object> updates) {
        return userRepository.findById(id).map(user -> {
            if(updates.containsKey("fullName")) user.setFullName((String) updates.get("fullName"));
            if(updates.containsKey("email")) user.setEmail((String) updates.get("email")); // Added email
            if(updates.containsKey("role")) user.setRole((String) updates.get("role"));   // Added role
            if(updates.containsKey("department")) user.setDepartment((String) updates.get("department"));
            if(updates.containsKey("password")) user.setPassword((String) updates.get("password"));
            userRepository.save(user);
            return ResponseEntity.ok("User updated");
        }).orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/intern-profile/update/{id}")
    public ResponseEntity<?> updateInternProfile(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> updates) {

        String uniId = (String) updates.get("universityId");

        Integer mentorId = null;
        if (updates.get("mentorId") != null) {
            mentorId = Integer.valueOf(updates.get("mentorId").toString());
        }

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM intern_profiles WHERE user_id = ?",
                Integer.class,
                id
        );

        if (count != null && count > 0) {
            jdbcTemplate.update(
                    "UPDATE intern_profiles SET university_id = ?, mentor_id = ? WHERE user_id = ?",
                    uniId, mentorId, id
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO intern_profiles (user_id, university_id, mentor_id) VALUES (?, ?, ?)",
                    id, uniId, mentorId
            );
        }

        return ResponseEntity.ok("Intern profile saved");
    }
    @GetMapping("/intern-profile/{id}")
    public ResponseEntity<?> getInternProfile(@PathVariable Integer id) {
        try {
            Map<String, Object> profile = jdbcTemplate.queryForMap(
                    "SELECT user_id, university_id, mentor_id, valid_until FROM intern_profiles WHERE user_id = ?",
                    id
            );
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id); // Cascade will handle intern_profiles if configured
            return ResponseEntity.ok("User deleted");
        }
        return ResponseEntity.notFound().build();
    }

}
