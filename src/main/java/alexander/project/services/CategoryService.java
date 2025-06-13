package alexander.project.services;

import alexander.project.models.Category;
import alexander.project.models.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CategoryService {
    List<Category> findByUser(User user);
    List<Category> findByUserAndArchived(User user, boolean archived);
    Category findById(Long id);
    Category save(Category category);
    void deleteById(Long id);
    void archiveCategory(Long id);
    void unarchiveCategory(Long id);
    List<Category> findByUserAndGroup(User user, String groupName);
    Map<LocalDate, BigDecimal> getCategoryTrend(Long categoryId, LocalDate startDate, LocalDate endDate);
    Map<String, BigDecimal> getCategoryComparison(Long categoryId, LocalDate period1Start, LocalDate period1End, LocalDate period2Start, LocalDate period2End);
    List<String> getAllGroups(User user);
    void updateSortOrder(Long categoryId, Integer newOrder);
    boolean isBudgetLimitExceeded(Long categoryId);
} 