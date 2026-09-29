package com.ridelink.account.security;

import com.ridelink.account.domain.AccountStatus;
import com.ridelink.account.repository.AccountRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final AccountRepository repository;

    public JwtAuthenticationFilter(JwtService jwtService, AccountRepository repository) {
        this.jwtService = jwtService;
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtService.parse(header.substring(7));
                String id = Objects.requireNonNull(claims.getSubject(), "JWT subject is required");
                String role = Objects.requireNonNull(claims.get("role", String.class), "JWT role is required");
                repository.findById(id)
                        .filter(a -> a.getStatus() == AccountStatus.ACTIVE && a.getRole().name().equals(role))
                        .ifPresent(a -> SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(id, null,
                                        List.of(new SimpleGrantedAuthority("ROLE_" + role)))));
            } catch (JwtException | IllegalArgumentException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
