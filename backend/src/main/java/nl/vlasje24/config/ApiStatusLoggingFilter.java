package nl.vlasje24.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class ApiStatusLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        boolean failedWithException = false;
        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException exception) {
            failedWithException = true;
            log.error("API-verzoek afgebroken (methode={}, pad={}, oorzaak={})",
                    request.getMethod(), request.getRequestURI(),
                    exception.getClass().getSimpleName());
            throw exception;
        } finally {
            int status = response.getStatus();
            if (!failedWithException && status >= 500) {
                log.error("API-verzoek mislukt (methode={}, pad={}, status={})",
                        request.getMethod(), request.getRequestURI(), status);
            } else if (!failedWithException && status >= 400) {
                log.warn("API-verzoek afgewezen (methode={}, pad={}, status={})",
                        request.getMethod(), request.getRequestURI(), status);
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }
}
