package alexander.project.configs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Configuration
public class ThymeleafConfig implements WebMvcConfigurer {

    /**
     * Настройка резолвера шаблонов Thymeleaf
     */
    @Bean
    public ITemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false); // отключение кэширования для разработки
        return resolver;
    }

    /**
     * Создаем собственный диалект для добавления web-объектов
     */
    @Bean
    public IDialect webObjectsDialect() {
        return new WebObjectsDialect();
    }

    /**
     * Настройка движка шаблонов Thymeleaf
     */
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setEnableSpringELCompiler(true);
        engine.setTemplateResolver(templateResolver());
        engine.addDialect(webObjectsDialect());
        return engine;
    }

    /**
     * Настройка ViewResolver для Thymeleaf
     */
    @Bean
    public ViewResolver viewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setCharacterEncoding("UTF-8");
        return resolver;
    }
    
    /**
     * Диалект для добавления объектов запроса в контекст Thymeleaf
     */
    public static class WebObjectsDialect implements IExpressionObjectDialect {
        
        @Override
        public String getName() {
            return "webObjects";
        }
        
        @Override
        public IExpressionObjectFactory getExpressionObjectFactory() {
            return new WebObjectsFactory();
        }
        
        public static class WebObjectsFactory implements IExpressionObjectFactory {
            
            @Override
            public Set<String> getAllExpressionObjectNames() {
                return Set.of("request", "session", "servletContext", "response");
            }
            
            @Override
            public Object buildObject(IExpressionContext context, String expressionObjectName) {
                ServletRequestAttributes requestAttributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                
                if (requestAttributes != null) {
                    HttpServletRequest request = requestAttributes.getRequest();
                    
                    switch (expressionObjectName) {
                        case "request":
                            return request;
                        case "session":
                            return request.getSession(false);
                        case "servletContext":
                            return request.getServletContext();
                        case "response":
                            return requestAttributes.getResponse();
                    }
                }
                
                return null;
            }
            
            @Override
            public boolean isCacheable(String expressionObjectName) {
                return false;
            }
        }
    }
} 