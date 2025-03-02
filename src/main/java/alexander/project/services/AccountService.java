package alexander.project.services;

import alexander.project.models.Account;
import alexander.project.models.User;
import alexander.project.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AuthService authService;

    /**
     * Получение текущего пользователя
     */
    private User getCurrentUser() {
        return authService.getCurrentUser();
    }

    /**
     * Получение всех счетов пользователя
     */
    public List<Account> getAllAccounts() {
        return accountRepository.findByUser(getCurrentUser());
    }

    /**
     * Получение счета по ID
     */
    public Account getAccountById(Long id) {
        return accountRepository.findByIdAndUser(id, getCurrentUser());
    }

    /**
     * Создание нового счета
     */
    public Account createAccount(String name) {
        Account account = new Account();
        account.setName(name);
        account.setBalance(BigDecimal.ZERO);
        account.setCurrency("RUB");
        account.setUser(getCurrentUser());
        
        return accountRepository.save(account);
    }

    /**
     * Обновление баланса счета
     */
    @Transactional
    public void updateAccountBalance(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }

    /**
     * Удаление счета
     */
    public void deleteAccount(Long id) {
        Account account = getAccountById(id);
        if (account != null) {
            accountRepository.delete(account);
        }
    }
    
    /**
     * Получение или создание основного счета пользователя
     * Если у пользователя нет счетов, создает основной счет
     */
    @Transactional
    public Account getDefaultAccount() {
        User user = getCurrentUser();
        Account account = accountRepository.findFirstByUser(user);
        
        if (account == null) {
            account = new Account();
            account.setName("Основной счет");
            account.setBalance(BigDecimal.ZERO);
            account.setCurrency("RUB");
            account.setUser(user);
            account = accountRepository.save(account);
        }
        
        return account;
    }
} 