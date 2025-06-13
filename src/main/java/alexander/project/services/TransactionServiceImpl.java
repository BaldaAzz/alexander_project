package alexander.project.services;

import alexander.project.models.Account;
import alexander.project.models.Category;
import alexander.project.models.Transaction;
import alexander.project.models.User;
import alexander.project.models.enums.TransactionType;
import alexander.project.repositories.TransactionRepository;
import alexander.project.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import jakarta.persistence.criteria.Predicate;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.HashMap;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import alexander.project.dto.TransactionDto;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AccountService accountService;

    @Autowired
    private MessageSource messageSource;

    @Override
    public List<Transaction> getRecentTransactions() {
        return transactionRepository.findTop10ByUserOrderByCreatedAtDesc(getCurrentUser());
    }

    @Override
    public List<Transaction> getTransactionsByUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return transactionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    public List<Transaction> getTransactionsByAccount(Account account) {
        return transactionRepository.findByAccountOrderByCreatedAtDesc(account);
    }

    @Override
    public BigDecimal getTotalBalance(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return transactionRepository.calculateBalanceForUser(user);
    }

    @Override
    public BigDecimal getMonthlyIncome(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return transactionRepository.calculateIncomesForUser(user);
    }

    @Override
    public BigDecimal getMonthlyExpenses(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return transactionRepository.calculateExpensesForUser(user);
    }

    @Override
    public BigDecimal getCurrentBalance() {
        return transactionRepository.calculateBalanceForUser(getCurrentUser());
    }

    @Override
    public BigDecimal getTotalIncomes() {
        return transactionRepository.calculateIncomesForUser(getCurrentUser());
    }

    @Override
    public BigDecimal getTotalExpenses() {
        return transactionRepository.calculateExpensesForUser(getCurrentUser());
    }

    @Override
    @Transactional
    public Transaction createTransaction(String description, BigDecimal amount, LocalDateTime date) {
        Account account = accountService.getDefaultAccount();
        return createTransaction(description, amount, date, account.getId());
    }

    @Override
    @Transactional
    public Transaction createTransaction(String description, BigDecimal amount, LocalDateTime date, Long accountId) {
        Account account = accountService.getAccountById(accountId);
        if (account == null) {
            account = accountService.getDefaultAccount();
        }

        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setDate(date);
        transaction.setUser(getCurrentUser());
        transaction.setAccount(account);

        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            transaction.setType(TransactionType.INCOME);
        } else {
            transaction.setType(TransactionType.EXPENSE);
        }

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(messageSource.getMessage("transaction.notfound", null, Locale.getDefault())));
        
        if (transaction.getUser().getId().equals(getCurrentUser().getId())) {
            accountService.updateAccountBalance(transaction.getAccount(), transaction.getAmount().negate());
            transactionRepository.deleteById(id);
        } else {
             throw new SecurityException(messageSource.getMessage("transaction.owner.error", null, Locale.getDefault()));
        }
    }

    @Override
    public void deleteById(Long id) {
        transactionRepository.deleteById(id);
    }

    @Override
    public List<Transaction> findByUser(User user) {
        return transactionRepository.findByUser(user);
    }

    @Override
    public Transaction findById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(messageSource.getMessage("transaction.notfound", null, Locale.getDefault())));
    }

    @Override
    @Transactional
    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Override
    public Transaction findByIdAndUser(Long id, User user) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(messageSource.getMessage("transaction.notfound", null, Locale.getDefault())));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new SecurityException(messageSource.getMessage("transaction.owner.error", null, Locale.getDefault()));
        }

        return transaction;
    }

    @Override
    public List<Transaction> findByUserAndFilter(User user, String period, Long categoryId, Long accountId, String type) {
        return transactionRepository.findAll((root, query, cb) -> {
            Predicate userPredicate = cb.equal(root.get("user"), user);
            Predicate finalPredicate = userPredicate;

            if (period != null && !period.isEmpty()) {
                LocalDateTime startDate = null;
                switch (period) {
                    case "today":
                        startDate = LocalDateTime.now().with(java.time.LocalTime.MIN);
                        break;
                    case "week":
                        startDate = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(java.time.LocalTime.MIN);
                        break;
                    case "month":
                        startDate = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).with(java.time.LocalTime.MIN);
                        break;
                    case "year":
                        startDate = LocalDateTime.now().with(TemporalAdjusters.firstDayOfYear()).with(java.time.LocalTime.MIN);
                        break;
                    // TODO: Handle custom period
                }
                if (startDate != null) {
                    finalPredicate = cb.and(finalPredicate, cb.greaterThanOrEqualTo(root.get("date"), startDate));
                }
            }

            if (categoryId != null) {
                finalPredicate = cb.and(finalPredicate, cb.equal(root.get("category").get("id"), categoryId));
            }

            if (accountId != null) {
                finalPredicate = cb.and(finalPredicate, cb.equal(root.get("account").get("id"), accountId));
            }

            if (type != null && !type.isEmpty()) {
                finalPredicate = cb.and(finalPredicate, cb.equal(root.get("type"), TransactionType.valueOf(type)));
            }

            query.orderBy(cb.desc(root.get("date"))); // Order by date descending
            return finalPredicate;
        });
    }

    @Override
    public BigDecimal getTotalAmountByUserAndDateAfter(User user, LocalDateTime startDate) {
         return transactionRepository.getTotalAmountByUserAndDateAfter(user, startDate);
    }

    @Override
    public BigDecimal getTotalAmountByCategoryAndUserAndDateAfter(Long categoryId, User user, LocalDateTime startDate) {
         Category category = new Category();
         category.setId(categoryId);
         return transactionRepository.getTotalAmountByCategoryAndUserAndDateAfter(category, user, startDate);
    }

    @Override
    public Map<String, BigDecimal> getMonthlyIncomeVsExpense(User user) {
        // Получаем транзакции за последний год
        LocalDateTime oneYearAgo = LocalDateTime.now().minus(1, ChronoUnit.YEARS).with(java.time.LocalTime.MIN);
        List<Object[]> monthlyData = transactionRepository.findMonthlyIncomeVsExpense(user, oneYearAgo);

        // Обрабатываем результаты для формирования карты
        Map<String, Map<TransactionType, BigDecimal>> monthlyAggregatedData = monthlyData.stream()
            .collect(Collectors.groupingBy(
                data -> data[0].toString() + "-" + String.format("%02d", data[1]), // Key: "YYYY-MM"
                Collectors.groupingBy(
                    data -> (TransactionType) data[2],
                    Collectors.reducing(BigDecimal.ZERO, data -> (BigDecimal) data[3], BigDecimal::add)
                )
            ));

        // Форматируем результат для графика (например, для Chart.js)
        Map<String, BigDecimal> monthlyIncome = new LinkedHashMap<>();
        Map<String, BigDecimal> monthlyExpense = new LinkedHashMap<>();

        // Заполняем все месяцы за последний год нулями для непрерывности на графике
        LocalDateTime currentMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).with(java.time.LocalTime.MIN);
        for (int i = 0; i < 12; i++) {
            String monthKey = currentMonth.minusMonths(i).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
            monthlyIncome.put(monthKey, BigDecimal.ZERO);
            monthlyExpense.put(monthKey, BigDecimal.ZERO);
        }

        monthlyAggregatedData.forEach((monthKey, typeMap) -> {
            monthlyIncome.computeIfPresent(monthKey, (k, v) -> v.add(typeMap.getOrDefault(TransactionType.INCOME, BigDecimal.ZERO)));
            monthlyExpense.computeIfPresent(monthKey, (k, v) -> v.add(typeMap.getOrDefault(TransactionType.EXPENSE, BigDecimal.ZERO).abs()));
        });

        // Возвращаем данные в формате, удобном для фронтенда (например, отдельные списки или карта)
        // Здесь возвращаем карту, где ключ - месяц, значение - Map с income/expense
         Map<String, BigDecimal> result = new LinkedHashMap<>();
         monthlyIncome.forEach((month, amount) -> result.put(month + "_INCOME", amount));
         monthlyExpense.forEach((month, amount) -> result.put(month + "_EXPENSE", amount));

         return result;
    }

    @Override
    @Transactional
    public Transaction updateTransaction(Transaction transaction) {
        Transaction existingTransaction = findById(transaction.getId());
        if (!existingTransaction.getUser().getId().equals(getCurrentUser().getId())) {
            throw new SecurityException(messageSource.getMessage("transaction.owner.error", null, Locale.getDefault()));
        }
        return transactionRepository.save(transaction);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        throw new RuntimeException("User not authenticated");
    }

    public TransactionDto convertToDto(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setType(transaction.getType());
        dto.setAmount(transaction.getAmount());
        dto.setDescription(transaction.getDescription());
        dto.setDate(transaction.getDate());
        // Явно получаем данные из связанных объектов
        if (transaction.getAccount() != null) {
            dto.setAccountName(transaction.getAccount().getName());
            dto.setAccountCurrency(transaction.getAccount().getCurrency());
        }
        if (transaction.getCategory() != null) {
             dto.setCategoryName(transaction.getCategory().getName());
             dto.setCategoryIcon(transaction.getCategory().getIcon());
             dto.setCategoryColor(transaction.getCategory().getColor());
        } else {
            // Если категория null, используем categoryName из самой транзакции, если есть
             dto.setCategoryName(transaction.getCategoryName());
             dto.setCategoryIcon(transaction.getCategoryIcon());
             dto.setCategoryColor(transaction.getCategoryColor());
        }
        return dto;
    }
}