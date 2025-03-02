package alexander.project.repositories;

import alexander.project.models.Account;
import alexander.project.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    // Найти счета пользователя
    List<Account> findByUser(User user);
    
    // Найти счет пользователя по имени
    Account findByUserAndName(User user, String name);
    
    // Найти счет пользователя по ID
    Account findByIdAndUser(Long id, User user);
    
    // Найти основной счет пользователя (по умолчанию)
    // Метод находит первый счет, что для новых пользователей будет основной счет
    Account findFirstByUser(User user);
} 