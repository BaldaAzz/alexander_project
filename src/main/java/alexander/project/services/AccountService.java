package alexander.project.services;

import alexander.project.models.Account;
import alexander.project.models.User;
import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    List<Account> findByUser(User user);
    Account findById(Long id);
    Account save(Account account);
    void deleteById(Long id);
    
    // Добавляем недостающие методы
    List<Account> getAllAccounts();
    Account getAccountById(Long id);
    Account getDefaultAccount();
    void updateAccountBalance(Account account, BigDecimal amount);
    BigDecimal getTotalBalanceByUser(User user);
} 