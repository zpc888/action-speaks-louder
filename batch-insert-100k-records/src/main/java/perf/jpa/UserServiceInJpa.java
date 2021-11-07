package perf.jpa;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import perf.mybatis.User;

import java.util.List;

@AllArgsConstructor
@Service
public class UserServiceInJpa {
    private final UserRepository userRepository;

    public void addUsers(List<JpaUser> users) {
        for (JpaUser user: users) {
            userRepository.save(user);
        }
    }

    public void deleteAllUsers() {
        userRepository.deleteAll();
    }
}
