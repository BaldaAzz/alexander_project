package alexander.project.controllers;

import alexander.project.models.Account;
import alexander.project.models.Transaction;
import alexander.project.services.AccountService;
import alexander.project.services.TransactionService;
import alexander.project.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {
    
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final AuthService authService;
    
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
    public String transactions(Model model, HttpServletRequest request) {
        // Проверяем аутентификацию
        String redirect = checkAuth();
        if (redirect != null) {
            return redirect;
        }
        
        List<Transaction> transactions = transactionService.getAllTransactions();
        List<Account> accounts = accountService.getAllAccounts();
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("accounts", accounts);
        
        // Добавляем HttpServletRequest в модель
        model.addAttribute("request", request);
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "transactions";
    }
    
    /**
     * Создание новой транзакции
     */
    @PostMapping("/add")
    public String addTransaction(
            @RequestParam("description") String description,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("isExpense") boolean isExpense,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "accountId", required = false) Long accountId,
            RedirectAttributes redirectAttributes) {
        
        // Проверяем аутентификацию
        String redirect = checkAuth();
        if (redirect != null) {
            return redirect;
        }
        
        // Если это расход, делаем сумму отрицательной
        if (isExpense) {
            amount = amount.negate();
        }
        
        // Создаем транзакцию с указанной датой и временем 12:00
        LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.NOON);
        
        Transaction transaction;
        if (accountId != null) {
            // Создаем транзакцию для указанного счета
            transaction = transactionService.createTransaction(description, amount, dateTime, accountId);
        } else {
            // Используем основной счет
            transaction = transactionService.createTransaction(description, amount, dateTime);
        }
        
        // Добавляем сообщение об успешном добавлении
        String successMessage = "Транзакция успешно добавлена: " + 
                               (transaction.isIncome() ? "доход" : "расход") + 
                               " на сумму " + transaction.getAbsoluteAmount() + " руб.";
        redirectAttributes.addFlashAttribute("successMessage", successMessage);
        
        return "redirect:/";
    }
    
    /**
     * Удаление транзакции
     */
    @PostMapping("/delete/{id}")
    public String deleteTransaction(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Проверяем аутентификацию
        String redirect = checkAuth();
        if (redirect != null) {
            return redirect;
        }
        
        transactionService.deleteTransaction(id);
        redirectAttributes.addFlashAttribute("successMessage", "Транзакция успешно удалена!");
        return "redirect:/";
    }
} 