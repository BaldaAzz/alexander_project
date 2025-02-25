package alexander.project.services;

import alexander.project.models.User;
import alexander.project.models.enums.Role;
import alexander.project.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean createUser(User user) {

        if(isUserExists(user)) return false;
        saveUser(user);

        return true;
    }

    private boolean isUserExists(User user) {
        return userRepository.findByEmail(user.getEmail()) != null;
    }

    private void saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.getRoles().add(Role.USER);
        userRepository.save(user);
    }

}
