package alexander.project.controllers;

import alexander.project.models.Account;
import alexander.project.models.User;
import alexander.project.services.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public String accountsPage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("accounts", accountService.findByUser(user));
        model.addAttribute("user", user);
        return "accounts";
    }

    @PostMapping("/add")
    public String addAccount(@AuthenticationPrincipal User user,
                           @RequestParam String name,
                           @RequestParam BigDecimal balance,
                           @RequestParam String currency,
                           RedirectAttributes redirectAttributes) {
        try {
            Account account = new Account();
            account.setName(name);
            account.setBalance(balance);
            account.setCurrency(currency);
            account.setUser(user);
            
            accountService.save(account);
            redirectAttributes.addFlashAttribute("success", "Счет успешно создан");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при создании счета: " + e.getMessage());
        }
        return "redirect:/accounts";
    }

    @PostMapping("/edit/{id}")
    public String editAccount(@PathVariable Long id,
                            @RequestParam String name,
                            @RequestParam BigDecimal balance,
                            @RequestParam String currency,
                            RedirectAttributes redirectAttributes) {
        try {
            Account account = accountService.findById(id);
            if (account != null) {
                account.setName(name);
                account.setBalance(balance);
                account.setCurrency(currency);
                
                accountService.save(account);
                redirectAttributes.addFlashAttribute("success", "Счет успешно обновлен");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении счета: " + e.getMessage());
        }
        return "redirect:/accounts";
    }

    @PostMapping("/delete/{id}")
    public String deleteAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            accountService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Счет успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении счета: " + e.getMessage());
        }
        return "redirect:/accounts";
    }
} 