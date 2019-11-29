package com.example.reservationApi.account;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

public class AccountFilter extends FilterSecurityInterceptor {
    private final AccountService accountService;

    public AccountFilter(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getServletPath();

        if (path.matches("/api/account/.*") && isPutMethod(httpRequest)) {
            Authentication authentication = getAuthentication(httpRequest);
            if (!isAdmin(authentication) && !accountIsEditedByItself(httpRequest, authentication)) {
                ((HttpServletResponse) response).sendError(403);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private Authentication getAuthentication(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        SecurityContextImpl sci = (SecurityContextImpl) session.getAttribute("SPRING_SECURITY_CONTEXT");
        return sci.getAuthentication();
    }

    private boolean accountIsEditedByItself(HttpServletRequest httpRequest, Authentication authentication) {
        String username = String.valueOf(authentication.getPrincipal());
        Account account = accountService.findByLogin(username);

        String path = httpRequest.getServletPath();
        String stringId = path.substring(path.lastIndexOf("/") + 1);
        UUID id = UUID.fromString(stringId);

        boolean b = id.compareTo(account.getId()) == 0;
        return b;
    }

    private boolean isAdmin(Authentication authentication) {
        boolean role_admin = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_ADMIN"));
        return role_admin;
    }

    private boolean isPutMethod(HttpServletRequest httpRequest) {
        String method = httpRequest.getMethod();
        return method.equals("PUT");
    }
}
