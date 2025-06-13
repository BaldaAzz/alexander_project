package alexander.project.controllers;

import alexander.project.models.Account;
import alexander.project.models.Transaction;
import alexander.project.services.AccountService;
import alexander.project.services.TransactionService;
import alexander.project.services.AuthService;
import alexander.project.services.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import alexander.project.models.User;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import alexander.project.dto.TransactionDto;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

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
            return "redirect:/login";
        }
        return null;
    }

    /**
     * Отображение дашборда
     */
    @GetMapping
    public String dashboard(Model model, HttpServletRequest request, @AuthenticationPrincipal User user) {
        // Проверяем аутентификацию
        if (!authService.isAuthenticated()) {
            return "redirect:/login";
        }

        // Получаем данные транзакций
        List<Transaction> transactions = transactionService.getRecentTransactions();
        BigDecimal incomes = transactionService.getTotalIncomes();
        BigDecimal expenses = transactionService.getTotalExpenses();

        // Получаем данные счетов
        List<Account> accounts = accountService.getAllAccounts();
        BigDecimal totalBalance = accountService.getTotalBalanceByUser(user);
        
        // Получаем категории пользователя
        List<alexander.project.models.Category> categories = categoryService.findByUser(user);

        // Добавляем данные в модель
        model.addAttribute("transactions", transactions);
        model.addAttribute("incomes", incomes);
        model.addAttribute("expenses", expenses);
        model.addAttribute("accounts", accounts);
        model.addAttribute("categories", categories);
        model.addAttribute("user", user);
        model.addAttribute("totalBalance", totalBalance);

        // Добавляем HttpServletRequest в модель
        model.addAttribute("request", request);
        model.addAttribute("currentPath", request.getRequestURI());

        return "dashboard";
    }

    /**
     * Получение данных для ежемесячного отчета
     */
    @GetMapping("/monthly-report")
    @ResponseBody
    public List<TransactionDto> getMonthlyReport(@AuthenticationPrincipal User user) {
        // Получаем список сущностей Transaction за текущий месяц
        // Изменяем фильтр на null для получения всех транзакций по умолчанию
        List<Transaction> transactions = transactionService.findByUserAndFilter(user, null, null, null, null);
        // Преобразуем сущности в DTO и возвращаем список DTO
        return transactions.stream()
                .map(transactionService::convertToDto)
                .collect(Collectors.toList());
    }
} 