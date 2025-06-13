package alexander.project.services;

import alexander.project.models.Category;
import alexander.project.models.User;
import alexander.project.repositories.CategoryRepository;
import alexander.project.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public List<Category> findByUser(User user) {
        List<Category> categories = categoryRepository.findByUser(user);
        updateCategoryStats(categories, user);
        return categories.stream()
                .filter(c -> !c.isArchived())
                .sorted(Comparator.comparing(Category::getSortOrder))
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findByUserAndArchived(User user, boolean archived) {
        return categoryRepository.findByUser(user).stream()
                .filter(c -> c.isArchived() == archived)
                .sorted(Comparator.comparing(Category::getSortOrder))
                .collect(Collectors.toList());
    }

    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Override
    public Category save(Category category) {
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        return categoryRepository.save(category);
    }

    @Override
    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public void archiveCategory(Long id) {
        Category category = findById(id);
        if (category != null) {
            category.setArchived(true);
            save(category);
        }
    }

    @Override
    public void unarchiveCategory(Long id) {
        Category category = findById(id);
        if (category != null) {
            category.setArchived(false);
            save(category);
        }
    }

    @Override
    public List<Category> findByUserAndGroup(User user, String groupName) {
        return categoryRepository.findByUser(user).stream()
                .filter(c -> !c.isArchived() && groupName.equals(c.getGroupName()))
                .sorted(Comparator.comparing(Category::getSortOrder))
                .collect(Collectors.toList());
    }

    @Override
    public Map<LocalDate, BigDecimal> getCategoryTrend(Long categoryId, LocalDate startDate, LocalDate endDate) {
        Category category = findById(categoryId);
        if (category == null) return Collections.emptyMap();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return transactionRepository.findByCategoryAndDateBetween(category, startDateTime, endDateTime)
                .stream()
                .collect(Collectors.groupingBy(
                    t -> t.getDate().toLocalDate(),
                    Collectors.reducing(
                        BigDecimal.ZERO,
                        t -> t.getAmount(),
                        BigDecimal::add
                    )
                ));
    }

    @Override
    public Map<String, BigDecimal> getCategoryComparison(Long categoryId, LocalDate period1Start, LocalDate period1End, 
                                                       LocalDate period2Start, LocalDate period2End) {
        Category category = findById(categoryId);
        if (category == null) return Collections.emptyMap();

        Map<String, BigDecimal> result = new HashMap<>();
        
        LocalDateTime period1StartDateTime = period1Start.atStartOfDay();
        LocalDateTime period1EndDateTime = period1End.atTime(LocalTime.MAX);
        LocalDateTime period2StartDateTime = period2Start.atStartOfDay();
        LocalDateTime period2EndDateTime = period2End.atTime(LocalTime.MAX);
        
        BigDecimal period1Total = transactionRepository.findByCategoryAndDateBetween(category, period1StartDateTime, period1EndDateTime)
                .stream()
                .map(t -> t.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal period2Total = transactionRepository.findByCategoryAndDateBetween(category, period2StartDateTime, period2EndDateTime)
                .stream()
                .map(t -> t.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        result.put("period1", period1Total);
        result.put("period2", period2Total);
        result.put("difference", period2Total.subtract(period1Total));
        
        return result;
    }

    @Override
    public List<String> getAllGroups(User user) {
        return categoryRepository.findByUser(user).stream()
                .map(Category::getGroupName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void updateSortOrder(Long categoryId, Integer newOrder) {
        Category category = findById(categoryId);
        if (category != null) {
            category.setSortOrder(newOrder);
            save(category);
        }
    }

    @Override
    public boolean isBudgetLimitExceeded(Long categoryId) {
        Category category = findById(categoryId);
        if (category == null || category.getBudgetLimit() == null) return false;

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        
        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.atTime(LocalTime.MAX);

        BigDecimal monthlyTotal = transactionRepository.findByCategoryAndDateBetween(category, startDateTime, endDateTime)
                .stream()
                .map(t -> t.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return monthlyTotal.compareTo(category.getBudgetLimit()) > 0;
    }

    private void updateCategoryStats(List<Category> categories, User user) {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        
        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.atTime(LocalTime.MAX);

        Map<Category, BigDecimal> categoryTotals = new HashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // Calculate totals for each category
        for (Category category : categories) {
            BigDecimal categoryTotal = transactionRepository.findByCategoryAndDateBetween(category, startDateTime, endDateTime)
                    .stream()
                    .map(t -> t.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            categoryTotals.put(category, categoryTotal);
            totalAmount = totalAmount.add(categoryTotal);
        }

        // Update category statistics
        for (Category category : categories) {
            BigDecimal categoryTotal = categoryTotals.get(category);
            category.setTransactionCount(transactionRepository.countByCategory(category));
            
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                double percentage = categoryTotal.divide(totalAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .doubleValue();
                category.setPercentage(percentage);
            } else {
                category.setPercentage(0.0);
            }
        }
    }
} 