package com.optimagrowth.organization.utils;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class UserContextFilter implements Filter {

    @Autowired
    UserContext userContext;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        userContext.setCorrelationId(String.valueOf(((HttpServletRequest) request).getHeader(UserContext.CORRELATION_ID)));
        userContext.setAuthToken(String.valueOf(((HttpServletRequest) request).getHeader(UserContext.AUTH_TOKEN)));
        userContext.setOrganizationId(String.valueOf(((HttpServletRequest) request).getHeader(UserContext.ORGANIZATION_ID)));
        userContext.setUserId(String.valueOf(((HttpServletRequest) request).getHeader(UserContext.USER_ID)));

        log.debug("UserContextFilter Correlation id: {}", userContext.getCorrelationId());

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}