@CrossOrigin(origins = "*") // This allows your phone to talk to the Java code
@RestController
@RequestMapping("/api/auth")
package prolink.controllers;

public class AuthController {
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
}
