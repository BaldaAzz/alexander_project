package alexander.project.controllers;

import alexander.project.models.Account;
import alexander.project.models.Transaction;
import alexander.project.services.AccountService;
import alexander.project.services.TransactionService;
import alexander.project.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;
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
     * Отображение списка счетов
     */
    @GetMapping
    public String accounts(Model model, HttpServletRequest request) {
        // Проверяем аутентификацию
        String redirect = checkAuth();
        if (redirect != null) {
            return redirect;
        }
        
        List<Account> accounts = accountService.getAllAccounts();
        model.addAttribute("accounts", accounts);
        model.addAttribute("account", new Account()); // для формы создания нового счета
        
        // Добавляем HttpServletRequest в модель
        model.addAttribute("request", request);
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "accounts";
    }

    /**
     * Создание нового счета
     */
    @PostMapping("/create")
    public String createAccount(@RequestParam String name, RedirectAttributes redirectAttributes) {
        // Проверяем аутентификацию
        String redirect = checkAuth();
        if (redirect != null) {
            return redirect;
        }
        
        try {
            accountService.createAccount(name);
            redirectAttributes.addFlashAttribute("successMessage", "Счет успешно создан");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка создания счета: " + e.getMessage());
        }
        return "redirect:/accounts";
    }

    /**
     * Удаление счета
     */
    @PostMapping("/delete/{id}")
    public String deleteAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Проверяем аутентификацию
        String redirect = checkAuth();
        if (redirect != null) {
            return redirect;
        }
        
        try {
            accountService.deleteAccount(id);
            redirectAttributes.addFlashAttribute("successMessage", "Счет успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка удаления счета: " + e.getMessage());
        }
        return "redirect:/accounts";
    }

    /**
     * Получение деталей счета
     */
    @GetMapping("/{id}")
    public String accountDetails(@PathVariable Long id, Model model, HttpServletRequest request) {
        // Проверяем аутентификацию
        String redirect = checkAuth();
        if (redirect != null) {
            return redirect;
        }
        
        Account account = accountService.getAccountById(id);
        if (account == null) {
            return "redirect:/accounts";
        }
        
        // Получаем транзакции для этого счета
        List<Transaction> transactions = transactionService.getTransactionsByAccount(account);
        model.addAttribute("account", account);
        model.addAttribute("transactions", transactions);
        
        // Добавляем HttpServletRequest в модель
        model.addAttribute("request", request);
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "account-details";
    }
} 