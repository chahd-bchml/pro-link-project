package prolink.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import prolink.models.User;
import prolink.repositories.UserRepository;
import java.util.List;

@RestController
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/test-db")
    public List<User> getTestUsers() {
        return userRepository.findAll(); // This fetches everything from your XAMPP Users table!
    }
}