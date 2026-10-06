package edu.uclm.reper.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ImportRequestSizeFilter extends OncePerRequestFilter {

    private final long maxRequestBytes;

    public ImportRequestSizeFilter(@Value("${app.import.max-request-bytes:33554432}") long maxRequestBytes) {
        this.maxRequestBytes = maxRequestBytes;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        return !("POST".equalsIgnoreCase(method) && uri != null && uri.contains("/projects/import"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long contentLength = request.getContentLengthLong();
        if (contentLength < 0) {
            String clHeader = request.getHeader("Content-Length");
            if (clHeader != null) {
                try {
                    contentLength = Long.parseLong(clHeader);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (contentLength < 0 || contentLength > maxRequestBytes) {
            response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"errorCode\":\"TOTAL_TOO_LARGE\",\"error\":\"Request is too large\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
