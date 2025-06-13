package alexander.project.controllers;

import alexander.project.models.Category;
import alexander.project.models.User;
import alexander.project.models.enums.TransactionType;
import alexander.project.services.CategoryService;
import alexander.project.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService;

    @GetMapping
    public String categoriesPage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("categories", categoryService.findByUser(user));
        model.addAttribute("archivedCategories", categoryService.findByUserAndArchived(user, true));
        model.addAttribute("groups", categoryService.getAllGroups(user));
        model.addAttribute("user", user);
        return "categories";
    }

    @PostMapping("/add")
    public String addCategory(@AuthenticationPrincipal User user,
                            @RequestParam String name,
                            @RequestParam String icon,
                            @RequestParam String color,
                            @RequestParam String description,
                            @RequestParam TransactionType type,
                            @RequestParam(required = false) String groupName,
                            @RequestParam(required = false) BigDecimal budgetLimit,
                            @RequestParam(required = false) Integer notificationThreshold,
                            RedirectAttributes redirectAttributes) {
        try {
            Category category = new Category();
            category.setName(name);
            category.setIcon(icon);
            category.setColor(color);
            category.setDescription(description);
            category.setType(type);
            category.setUser(user);
            category.setGroupName(groupName);
            category.setBudgetLimit(budgetLimit);
            category.setNotificationThreshold(notificationThreshold);
            
            categoryService.save(category);
            redirectAttributes.addFlashAttribute("success", "Категория успешно добавлена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении категории: " + e.getMessage());
        }
        return "redirect:/categories";
    }

    @PostMapping("/edit/{id}")
    public String editCategory(@PathVariable Long id,
                             @RequestParam String name,
                             @RequestParam String icon,
                             @RequestParam String color,
                             @RequestParam String description,
                             @RequestParam TransactionType type,
                             @RequestParam(required = false) String groupName,
                             @RequestParam(required = false) BigDecimal budgetLimit,
                             @RequestParam(required = false) Integer notificationThreshold,
                             RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.findById(id);
            if (category != null) {
                category.setName(name);
                category.setIcon(icon);
                category.setColor(color);
                category.setDescription(description);
                category.setType(type);
                category.setGroupName(groupName);
                category.setBudgetLimit(budgetLimit);
                category.setNotificationThreshold(notificationThreshold);
                
                categoryService.save(category);
                redirectAttributes.addFlashAttribute("success", "Категория успешно обновлена");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении категории: " + e.getMessage());
        }
        return "redirect:/categories";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Категория успешно удалена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении категории: " + e.getMessage());
        }
        return "redirect:/categories";
    }

    @PostMapping("/archive/{id}")
    public String archiveCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.archiveCategory(id);
            redirectAttributes.addFlashAttribute("success", "Категория успешно архивирована");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при архивации категории: " + e.getMessage());
        }
        return "redirect:/categories";
    }

    @PostMapping("/unarchive/{id}")
    public String unarchiveCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.unarchiveCategory(id);
            redirectAttributes.addFlashAttribute("success", "Категория успешно разархивирована");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при разархивации категории: " + e.getMessage());
        }
        return "redirect:/categories";
    }

    @PostMapping("/sort/{id}")
    @ResponseBody
    public ResponseEntity<?> updateSortOrder(@PathVariable Long id, @RequestParam Integer newOrder) {
        try {
            categoryService.updateSortOrder(id, newOrder);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/trend/{id}")
    @ResponseBody
    public ResponseEntity<Map<LocalDate, BigDecimal>> getCategoryTrend(
            @PathVariable Long id,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        try {
            Map<LocalDate, BigDecimal> trend = categoryService.getCategoryTrend(id, startDate, endDate);
            return ResponseEntity.ok(trend);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/comparison/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, BigDecimal>> getCategoryComparison(
            @PathVariable Long id,
            @RequestParam LocalDate period1Start,
            @RequestParam LocalDate period1End,
            @RequestParam LocalDate period2Start,
            @RequestParam LocalDate period2End) {
        try {
            Map<String, BigDecimal> comparison = categoryService.getCategoryComparison(
                id, period1Start, period1End, period2Start, period2End);
            return ResponseEntity.ok(comparison);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/group/{groupName}")
    public String getCategoriesByGroup(@PathVariable String groupName,
                                     @AuthenticationPrincipal User user,
                                     Model model) {
        model.addAttribute("categories", categoryService.findByUserAndGroup(user, groupName));
        model.addAttribute("groupName", groupName);
        model.addAttribute("user", user);
        return "categories";
    }
} 