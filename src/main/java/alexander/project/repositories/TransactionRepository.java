package alexander.project.repositories;

import alexander.project.models.Account;
import alexander.project.models.Transaction;
import alexander.project.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Поиск всех транзакций пользователя, отсортированных по дате (сначала новые)
    List<Transaction> findByUserOrderByDateDesc(User user);
    
    // Поиск последних 20 транзакций пользователя
    List<Transaction> findTop20ByUserOrderByDateDesc(User user);
    
    // Поиск транзакций по счету
    List<Transaction> findByAccountOrderByDateDesc(Account account);
    
    // Поиск транзакций по счету и пользователю
    List<Transaction> findByAccountAndUserOrderByDateDesc(Account account, User user);
    
    // Вычисление текущего баланса пользователя
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = ?1")
    BigDecimal calculateBalanceForUser(User user);
    
    // Вычисление общей суммы доходов пользователя
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = ?1 AND t.amount > 0")
    BigDecimal calculateIncomesForUser(User user);
    
    // Вычисление общей суммы расходов пользователя (возвращается отрицательное число)
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = ?1 AND t.amount < 0")
    BigDecimal calculateExpensesForUser(User user);
    
    // Вычисление баланса для конкретного счета
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account = ?1")
    BigDecimal calculateBalanceForAccount(Account account);
    
    // Вычисление доходов для конкретного счета
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account = ?1 AND t.amount > 0")
    BigDecimal calculateIncomesForAccount(Account account);
    
    // Вычисление расходов для конкретного счета
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account = ?1 AND t.amount < 0")
    BigDecimal calculateExpensesForAccount(Account account);
} 