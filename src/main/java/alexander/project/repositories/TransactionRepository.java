package alexander.project.repositories;

import alexander.project.models.Account;
import alexander.project.models.Category;
import alexander.project.models.Transaction;
import alexander.project.models.User;
import alexander.project.models.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    
    // Поиск всех транзакций пользователя, отсортированных по дате (сначала новые)
    List<Transaction> findByUserOrderByCreatedAtDesc(User user);
    
    // Поиск последних 20 транзакций пользователя
    List<Transaction> findTop20ByUserOrderByCreatedAtDesc(User user);
    
    // Поиск транзакций по счету
    List<Transaction> findByAccountOrderByCreatedAtDesc(Account account);
    
    // Поиск транзакций по счету и пользователю
    List<Transaction> findByAccountAndUserOrderByCreatedAtDesc(Account account, User user);
    
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

    List<Transaction> findTop10ByOrderByCreatedAtDesc();
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = ?1 AND t.user.id = ?2")
    BigDecimal sumAmountByTypeAndUserId(TransactionType type, Long userId);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = ?1 AND t.user.id = ?2 AND t.createdAt >= ?3")
    BigDecimal sumAmountByTypeAndUserIdAndCreatedAtAfter(TransactionType type, Long userId, LocalDateTime date);

    List<Transaction> findByUser(User user);

    // Добавлен новый метод для получения последних 10 транзакций пользователя
    List<Transaction> findTop10ByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.category = :category AND t.user = :user")
    int countByCategoryAndUser(@Param("category") Category category, @Param("user") User user);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.date >= :startDate")
    BigDecimal getTotalAmountByUserAndDateAfter(@Param("user") User user, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.category = :category AND t.user = :user AND t.date >= :startDate")
    BigDecimal getTotalAmountByCategoryAndUserAndDateAfter(
        @Param("category") Category category,
        @Param("user") User user,
        @Param("startDate") LocalDateTime startDate
    );

    List<Transaction> findByCategoryAndDateBetween(Category category, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.category = :category")
    int countByCategory(@Param("category") Category category);

    @Query("SELECT YEAR(t.date), MONTH(t.date), t.type, SUM(t.amount) " +
           "FROM Transaction t " +
           "WHERE t.user = :user AND t.date >= :startDate " +
           "GROUP BY YEAR(t.date), MONTH(t.date), t.type " +
           "ORDER BY YEAR(t.date) DESC, MONTH(t.date) DESC")
    List<Object[]> findMonthlyIncomeVsExpense(@Param("user") User user, @Param("startDate") LocalDateTime startDate);
} 