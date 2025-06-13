package alexander.project.controllers;

import alexander.project.models.Account;
import alexander.project.models.Category;
import alexander.project.models.Transaction;
import alexander.project.models.User;
import alexander.project.models.enums.TransactionType;
import alexander.project.services.AccountService;
import alexander.project.services.CategoryService;
import alexander.project.services.TransactionService;
import alexander.project.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final AuthService authService;
    private final CategoryService categoryService;
    
    @Autowired
    private MessageSource messageSource;
    
    /**
     * Проверка аутентификации
     */
    private String checkAuth() {
        if (!authService.isAuthenticated()) {
            return "redirect:/";
        }
        return null;
    }
    
    /**
     * Отображение списка транзакций
     */
    @GetMapping
    public String transactionsPage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("transactions", transactionService.findByUser(user));
        model.addAttribute("accounts", accountService.findByUser(user));
        model.addAttribute("categories", categoryService.findByUser(user));
        model.addAttribute("user", user);
        return "transactions";
    }
    
    /**
     * Создание новой транзакции
     */
    @PostMapping("/add")
    public String addTransaction(@AuthenticationPrincipal User user,
                               @RequestParam(required = true) String isExpense,
                               @RequestParam BigDecimal amount,
                               @RequestParam Long categoryId,
                               @RequestParam Long accountId,
                               @RequestParam(required = false) String description,
                               @RequestParam(required = false, defaultValue = "false") boolean isRecurring,
                               @RequestParam(required = false) String recurrenceType,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {
        logger.info("Received date for transaction: {}", date);
        try {
            Transaction transaction = new Transaction();
            boolean isExpenseBool = Boolean.parseBoolean(isExpense);
            transaction.setType(isExpenseBool ? TransactionType.EXPENSE : TransactionType.INCOME);
            transaction.setAmount(isExpenseBool ? amount.negate() : amount);
            
            Category category = categoryService.findById(categoryId);
            if (category == null) {
                throw new IllegalArgumentException(messageSource.getMessage("transaction.category.notfound", null, locale));
            }
            transaction.setCategoryName(category.getName());
            transaction.setCategoryIcon(category.getIcon());
            transaction.setCategoryColor(category.getColor());
            
            Account account = accountService.findById(accountId);
            if (account == null) {
                throw new IllegalArgumentException(messageSource.getMessage("account.notfound", null, locale));
            }
            transaction.setAccount(account);
            transaction.setDescription(description);
            transaction.setUser(user);
            transaction.setDate(date != null ? date : LocalDateTime.now());
            
            if (isRecurring) {
                transaction.setRecurring(true);
                transaction.setRecurrenceType(recurrenceType);
            }
            
            transactionService.save(transaction);
            accountService.updateAccountBalance(account, transaction.getAmount());
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("transaction.add.success", null, locale));
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при добавлении транзакции: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            logger.error("Неизвестная ошибка при добавлении транзакции", e);
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("transaction.add.error", null, locale) + ": " + e.getMessage());
        }
        return "redirect:/transactions";
    }
    
    /**
     * Получение транзакции по ID для редактирования
     */
    @GetMapping("/{id}")
    @ResponseBody
    public Transaction getTransactionById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return transactionService.findByIdAndUser(id, user);
    }
    
    /**
     * Удаление транзакции
     */
    @PostMapping("/delete/{id}")
    public String deleteTransaction(@PathVariable Long id, RedirectAttributes redirectAttributes, Locale locale) {
        try {
            transactionService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("transaction.delete.success", null, locale));
        } catch (SecurityException e) {
            logger.error("Ошибка безопасности при удалении транзакции: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        catch (Exception e) {
            logger.error("Ошибка при удалении транзакции", e);
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("transaction.delete.error", null, locale) + ": " + e.getMessage());
        }
        return "redirect:/transactions";
    }
    
    /**
     * Обновление существующей транзакции
     */
    @PostMapping("/edit")
    public String updateTransaction(@AuthenticationPrincipal User user,
                                  @RequestParam Long id,
                                  @RequestParam String isExpense,
                                  @RequestParam BigDecimal amount,
                                  @RequestParam Long categoryId,
                                  @RequestParam Long accountId,
                                  @RequestParam(required = false) String description,
                                  @RequestParam(required = false, defaultValue = "false") boolean isRecurring,
                                  @RequestParam(required = false) String recurrenceType,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
                                  RedirectAttributes redirectAttributes,
                                  Locale locale) {
         try {
             Transaction existingTransaction = transactionService.findByIdAndUser(id, user);

             boolean isExpenseBool = Boolean.parseBoolean(isExpense);
             BigDecimal finalAmount = isExpenseBool ? amount.negate() : amount;

             // Создаем временный объект Transaction для передачи обновленных данных в сервис
             Transaction updatedTransaction = new Transaction();
             updatedTransaction.setId(id);
             updatedTransaction.setDescription(description);
             updatedTransaction.setAmount(finalAmount);
             updatedTransaction.setDate(date != null ? date : LocalDateTime.now());

             // Получаем объекты Category и Account для установки в updatedTransaction
             Category category = categoryService.findById(categoryId);
             if (category == null) {
                 throw new IllegalArgumentException(messageSource.getMessage("transaction.category.notfound", null, locale));
             }
             updatedTransaction.setCategory(category);

             Account account = accountService.findById(accountId);
             if (account == null) {
                 throw new IllegalArgumentException(messageSource.getMessage("account.notfound", null, locale));
             }
             updatedTransaction.setAccount(account);

             // Устанавливаем данные по повторяющейся транзакции
             updatedTransaction.setRecurring(isRecurring);
             updatedTransaction.setRecurrenceType(isRecurring ? recurrenceType : null);

             transactionService.updateTransaction(updatedTransaction);
             redirectAttributes.addFlashAttribute("success", messageSource.getMessage("transaction.edit.success", null, locale));

         }  catch (SecurityException e) {
            logger.error("Ошибка безопасности при редактировании транзакции: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IllegalArgumentException e) {
             logger.error("Ошибка при редактировании транзакции: {}", e.getMessage());
             redirectAttributes.addFlashAttribute("error", e.getMessage());
         } catch (Exception e) {
             logger.error("Неизвестная ошибка при редактировании транзакции", e);
             redirectAttributes.addFlashAttribute("error", messageSource.getMessage("transaction.edit.error", null, locale) + ": " + e.getMessage());
         }
         return "redirect:/transactions";
    }
} 