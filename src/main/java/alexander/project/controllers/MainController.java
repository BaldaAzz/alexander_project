package alexander.project.controllers;

import alexander.project.models.Transaction;
import alexander.project.models.Account;
import alexander.project.repositories.UserRepository;
import alexander.project.services.TransactionService;
import alexander.project.services.AccountService;
import alexander.project.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final AuthService authService;

    @GetMapping("/")
    public String main(Model model, HttpServletRequest request) {
        // Проверяем, авторизован ли пользователь
        if (!authService.isAuthenticated()) {
            return "index"; // Направляем на index вместо редиректа на логин
        }
        
        // Получаем данные транзакций
        List<Transaction> transactions = transactionService.getRecentTransactions();
        BigDecimal balance = transactionService.getCurrentBalance();
        BigDecimal incomes = transactionService.getTotalIncomes();
        BigDecimal expenses = transactionService.getTotalExpenses();
        
        // Получаем данные счетов
        List<Account> accounts = accountService.getAllAccounts();
        
        // Добавляем данные в модель
        model.addAttribute("transactions", transactions);
        model.addAttribute("balance", balance);
        model.addAttribute("incomes", incomes);
        model.addAttribute("expenses", expenses);
        model.addAttribute("accounts", accounts);
        
        // Добавляем HttpServletRequest в модель
        model.addAttribute("request", request);
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "dashboard";
    }
}
