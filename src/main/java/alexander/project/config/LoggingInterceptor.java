package alexander.project.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class LoggingInterceptor implements ClientHttpRequestInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, 
            ClientHttpRequestExecution execution) throws IOException {
        
        // Log request
        logger.info("Request: {} {}", request.getMethod(), request.getURI());
        if (body.length > 0) {
            logger.debug("Request body: {}", new String(body, StandardCharsets.UTF_8));
        }

        // Execute request
        ClientHttpResponse response = execution.execute(request, body);

        // Log response
        logger.info("Response: {} {}", response.getStatusCode(), response.getStatusText());
        
        // Log response body if needed
        if (logger.isDebugEnabled()) {
            String responseBody = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            logger.debug("Response body: {}", responseBody);
        }

        return response;
    }
} 