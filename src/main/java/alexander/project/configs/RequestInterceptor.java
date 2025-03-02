package alexander.project.configs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class RequestInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        if (modelAndView != null) {
            // Добавляем объект HttpServletRequest в модель с именем "request"
            modelAndView.addObject("request", request);
            // Также добавляем отдельные атрибуты, которые могут быть полезны
            modelAndView.addObject("currentPath", request.getRequestURI());
        }
    }
} 