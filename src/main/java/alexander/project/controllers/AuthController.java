package alexander.project.controllers;

import alexander.project.models.User;
import alexander.project.services.UserService;
import alexander.project.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * Контроллер для отображения страницы логина
     */
    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request) {
        // Если пользователь уже авторизован, перенаправляем на главную
        if (authService.isAuthenticated()) {
            return "redirect:/";
        }
        
        // Добавляем HttpServletRequest в модель
        model.addAttribute("request", request);
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "login";
    }
    
    /**
     * Контроллер для отображения страницы регистрации
     */
    @GetMapping("/registration")
    public String registration(Model model, HttpServletRequest request) {
        // Если пользователь уже авторизован, перенаправляем на главную
        if (authService.isAuthenticated()) {
            return "redirect:/";
        }
        
        model.addAttribute("user", new User());
        
        // Добавляем HttpServletRequest в модель
        model.addAttribute("request", request);
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "registration";
    }
    
    /**
     * Обработка формы регистрации
     */
    @PostMapping("/registration")
    public String createUser(User user, RedirectAttributes redirectAttributes) {
        if (!userService.createUser(user)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пользователь с таким email уже существует");
            return "redirect:/registration";
        }
        
        redirectAttributes.addFlashAttribute("successMessage", "Пользователь успешно зарегистрирован");
        return "redirect:/login";
    }
} 