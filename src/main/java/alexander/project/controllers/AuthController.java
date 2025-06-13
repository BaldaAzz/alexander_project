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
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;

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
    @GetMapping("/register")
    public String register(Model model, HttpServletRequest request) {
        // Если пользователь уже авторизован, перенаправляем на главную
        if (authService.isAuthenticated()) {
            return "redirect:/";
        }
        
        model.addAttribute("user", new User());
        
        // Добавляем HttpServletRequest в модель
        model.addAttribute("request", request);
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "register";
    }
    
    /**
     * Обработка формы регистрации
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "redirect:/?error=registration";
        }

        try {
            userService.registerNewUser(user);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/?error=registration";
        }
    }
} 