package prolink.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prolink.models.User;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // Spring Boot automatically writes the SQL for this!
    Optional<User> findByEmail(String email);
}