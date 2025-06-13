package alexander.project.services;

import alexander.project.models.Account;
import alexander.project.models.User;
import alexander.project.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AuthService authService;
    private final CurrencyService currencyService;

    @Autowired
    private MessageSource messageSource;

    @Override
    public List<Account> findByUser(User user) {
        return accountRepository.findByUser(user);
    }

    @Override
    public Account findById(Long id) {
        return accountRepository.findById(id).orElse(null);
    }

    @Override
    public Account save(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public void deleteById(Long id) {
        accountRepository.deleteById(id);
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findByUser(getCurrentUser());
    }

    @Override
    public BigDecimal getTotalBalanceByUser(User user) {
        List<Account> accounts = accountRepository.findByUser(user);
        BigDecimal totalBalance = BigDecimal.ZERO;
        String targetCurrency = user.getCurrency(); // Основная валюта пользователя

        for (Account account : accounts) {
            if (account.getBalance() != null && account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
                if (account.getCurrency().equals(targetCurrency)) {
                    totalBalance = totalBalance.add(account.getBalance());
                } else {
                    // Конвертируем баланс счета в основную валюту пользователя
                    BigDecimal convertedAmount = currencyService.convertAmount(account.getBalance(), account.getCurrency(), targetCurrency);
                    totalBalance = totalBalance.add(convertedAmount);
                }
            }
        }

        return totalBalance.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public Account getAccountById(Long id) {
        return accountRepository.findByIdAndUser(id, getCurrentUser());
    }

    @Override
    public Account getDefaultAccount() {
        User user = getCurrentUser();
        Account account = accountRepository.findFirstByUser(user);
        
        if (account == null) {
            account = new Account();
            account.setName(messageSource.getMessage("account.default.name", null, Locale.getDefault()));
            account.setBalance(BigDecimal.ZERO);
            account.setCurrency("BYN");
            account.setUser(user);
            account = accountRepository.save(account);
        }
        
        return account;
    }

    @Override
    public void updateAccountBalance(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }

    private User getCurrentUser() {
        return authService.getCurrentUser();
    }
} 