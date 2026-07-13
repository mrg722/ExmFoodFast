package com.foodfast.reparto_servicio.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class AuthorizationRelayFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            String auth = null;
            if (request instanceof HttpServletRequest) {
                auth = ((HttpServletRequest) request).getHeader("Authorization");
            }
            RequestHeaderContext.setAuthorization(auth);
            chain.doFilter(request, response);
        } finally {
            RequestHeaderContext.clear();
        }
    }
}
