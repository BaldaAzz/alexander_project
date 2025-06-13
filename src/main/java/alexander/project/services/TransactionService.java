package alexander.project.services;

import alexander.project.models.Account;
import alexander.project.models.Transaction;
import alexander.project.models.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.time.LocalDate;
import java.util.Map;

import alexander.project.dto.TransactionDto;

public interface TransactionService {
    List<Transaction> getRecentTransactions();
    List<Transaction> getTransactionsByUser(Long userId);
    List<Transaction> getAllTransactions();
    List<Transaction> getTransactionsByAccount(Account account);
    BigDecimal getTotalBalance(Long userId);
    BigDecimal getMonthlyIncome(Long userId);
    BigDecimal getMonthlyExpenses(Long userId);
    BigDecimal getCurrentBalance();
    BigDecimal getTotalIncomes();
    BigDecimal getTotalExpenses();
    Transaction createTransaction(String description, BigDecimal amount, LocalDateTime date);
    Transaction createTransaction(String description, BigDecimal amount, LocalDateTime date, Long accountId);
    void deleteTransaction(Long id);
    List<Transaction> findByUser(User user);
    Transaction findById(Long id);
    Transaction save(Transaction transaction);
    void deleteById(Long id);
    List<Transaction> findByUserAndFilter(User user, String period, Long categoryId, Long accountId, String type);
    BigDecimal getTotalAmountByUserAndDateAfter(User user, LocalDateTime startDate);
    BigDecimal getTotalAmountByCategoryAndUserAndDateAfter(Long categoryId, User user, LocalDateTime startDate);
    Map<String, BigDecimal> getMonthlyIncomeVsExpense(User user);
    Transaction findByIdAndUser(Long id, User user);
    Transaction updateTransaction(Transaction updatedTransaction);

    TransactionDto convertToDto(Transaction transaction);
} 