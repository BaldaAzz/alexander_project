package alexander.project.services;

import alexander.project.models.Account;
import alexander.project.models.Transaction;
import alexander.project.models.User;
import alexander.project.models.enums.TransactionType;
import alexander.project.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AuthService authService;
    private final AccountService accountService;

    /**
     * Получение текущего пользователя
     */
    private User getCurrentUser() {
        return authService.getCurrentUser();
    }

    /**
     * Получение всех транзакций пользователя
     */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findByUserOrderByDateDesc(getCurrentUser());
    }

    /**
     * Получение последних 20 транзакций пользователя
     */
    public List<Transaction> getRecentTransactions() {
        return transactionRepository.findTop20ByUserOrderByDateDesc(getCurrentUser());
    }

    /**
     * Получение транзакций для конкретного счета
     */
    public List<Transaction> getTransactionsByAccount(Account account) {
        // Проверяем, что счет принадлежит текущему пользователю
        if (account.getUser().getId().equals(getCurrentUser().getId())) {
            return transactionRepository.findByAccountOrderByDateDesc(account);
        }
        return List.of(); // Пустой список, если счет не принадлежит пользователю
    }

    /**
     * Создание новой транзакции
     */
    @Transactional
    public Transaction createTransaction(String description, BigDecimal amount, LocalDateTime date) {
        // Получаем или создаем основной счет пользователя
        Account account = accountService.getDefaultAccount();
        
        // Создаем транзакцию
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setDate(date);
        transaction.setUser(getCurrentUser());
        transaction.setAccount(account);
        
        // Устанавливаем тип транзакции в зависимости от суммы
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            transaction.setType(TransactionType.INCOME);
        } else if (amount.compareTo(BigDecimal.ZERO) < 0) {
            transaction.setType(TransactionType.EXPENSE);
        } else {
            transaction.setType(TransactionType.OTHER);
        }
        
        // Обновляем баланс счета
        accountService.updateAccountBalance(account, amount);
        
        // Сохраняем транзакцию
        return transactionRepository.save(transaction);
    }
    
    /**
     * Создание новой транзакции для конкретного счета
     */
    @Transactional
    public Transaction createTransaction(String description, BigDecimal amount, LocalDateTime date, Long accountId) {
        // Получаем счет пользователя по ID
        Account account = accountService.getAccountById(accountId);
        if (account == null) {
            // Если счет не найден, используем основной счет
            account = accountService.getDefaultAccount();
        }
        
        // Создаем транзакцию
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setDate(date);
        transaction.setUser(getCurrentUser());
        transaction.setAccount(account);
        
        // Устанавливаем тип транзакции в зависимости от суммы
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            transaction.setType(TransactionType.INCOME);
        } else if (amount.compareTo(BigDecimal.ZERO) < 0) {
            transaction.setType(TransactionType.EXPENSE);
        } else {
            transaction.setType(TransactionType.OTHER);
        }
        
        // Обновляем баланс счета
        accountService.updateAccountBalance(account, amount);
        
        // Сохраняем транзакцию
        return transactionRepository.save(transaction);
    }

    /**
     * Удаление транзакции
     */
    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id).orElse(null);
        if (transaction != null && transaction.getUser().getId().equals(getCurrentUser().getId())) {
            // Обновляем баланс счета (вычитаем сумму транзакции, т.е. добавляем инвертированную сумму)
            accountService.updateAccountBalance(transaction.getAccount(), transaction.getAmount().negate());
            
            // Удаляем транзакцию
            transactionRepository.deleteById(id);
        }
    }

    /**
     * Получение текущего баланса пользователя
     */
    public BigDecimal getCurrentBalance() {
        BigDecimal balance = transactionRepository.calculateBalanceForUser(getCurrentUser());
        return balance != null ? balance : BigDecimal.ZERO;
    }

    /**
     * Получение суммы доходов пользователя
     */
    public BigDecimal getTotalIncomes() {
        BigDecimal incomes = transactionRepository.calculateIncomesForUser(getCurrentUser());
        return incomes != null ? incomes : BigDecimal.ZERO;
    }

    /**
     * Получение суммы расходов пользователя (возвращается положительное число)
     */
    public BigDecimal getTotalExpenses() {
        BigDecimal expenses = transactionRepository.calculateExpensesForUser(getCurrentUser());
        return expenses != null ? expenses.abs() : BigDecimal.ZERO;
    }

    /**
     * Получение баланса счета
     */
    public BigDecimal getAccountBalance(Account account) {
        BigDecimal balance = transactionRepository.calculateBalanceForAccount(account);
        return balance != null ? balance : BigDecimal.ZERO;
    }
    
    /**
     * Получение суммы доходов счета
     */
    public BigDecimal getAccountIncomes(Account account) {
        BigDecimal incomes = transactionRepository.calculateIncomesForAccount(account);
        return incomes != null ? incomes : BigDecimal.ZERO;
    }
    
    /**
     * Получение суммы расходов счета (возвращается положительное число)
     */
    public BigDecimal getAccountExpenses(Account account) {
        BigDecimal expenses = transactionRepository.calculateExpensesForAccount(account);
        return expenses != null ? expenses.abs() : BigDecimal.ZERO;
    }
} 