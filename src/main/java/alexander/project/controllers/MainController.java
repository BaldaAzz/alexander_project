package alexander.project.controllers;

import alexander.project.models.User;
import alexander.project.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final UserService userService;

    @GetMapping("/")
    public String showMainPage(Model model) {
        return "index";
    }

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        return "login";
    }

    @GetMapping("/registration")
    public String showRegisterPage(Model model) {
        return "registration";
    }

    @PostMapping("/registration")
    public String createUser(User user, Model model) {
        if(!userService.createUser(user)) {
            model.addAttribute("errorMessage", "Пользователь с таким email уже существует!");

            return "auth/registration";
        }

        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String showDashboardPage(Model model) {
        return "dashboard";
    }
}
