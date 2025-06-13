package alexander.project.controllers;

import alexander.project.models.User;
import alexander.project.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String settingsPage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        return "settings";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal User currentUser,
                              @RequestParam String firstName,
                              @RequestParam String lastName,
                              @RequestParam String email,
                              @RequestParam(required = false) String phone,
                              RedirectAttributes redirectAttributes) {
        try {
            User updatedUser = userService.updateProfile(currentUser.getId(), firstName, lastName, email, phone);
            redirectAttributes.addFlashAttribute("success", "Профиль успешно обновлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении профиля: " + e.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal User user,
                               @RequestParam String currentPassword,
                               @RequestParam String newPassword,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("Пароли не совпадают");
            }
            userService.changePassword(user.getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Пароль успешно изменен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при изменении пароля: " + e.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/notifications")
    public String updateNotifications(@AuthenticationPrincipal User user,
                                    @RequestParam(required = false) boolean emailNotifications,
                                    @RequestParam(required = false) boolean transactionNotifications,
                                    @RequestParam(required = false) boolean balanceNotifications,
                                    RedirectAttributes redirectAttributes) {
        try {
            userService.updateNotificationSettings(user.getId(), emailNotifications, transactionNotifications, balanceNotifications);
            redirectAttributes.addFlashAttribute("success", "Настройки уведомлений обновлены");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении настроек уведомлений: " + e.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/currency")
    public String updateCurrency(@AuthenticationPrincipal User user,
                               @RequestParam String currency,
                               RedirectAttributes redirectAttributes) {
        try {
            userService.updateCurrency(user.getId(), currency);
            redirectAttributes.addFlashAttribute("success", "Валюта успешно обновлена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении валюты: " + e.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/delete-account")
    public String deleteAccount(@AuthenticationPrincipal User user,
                              @RequestParam String password,
                              RedirectAttributes redirectAttributes) {
        try {
            userService.deleteAccount(user.getId(), password);
            return "redirect:/logout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении аккаунта: " + e.getMessage());
            return "redirect:/settings";
        }
    }
} 