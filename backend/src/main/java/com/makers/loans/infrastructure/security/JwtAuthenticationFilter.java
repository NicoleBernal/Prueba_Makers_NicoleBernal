package com.makers.loans.infrastructure.security;

import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements WebFilter {
    private final JwtTokenProvider tokens;
    public JwtAuthenticationFilter(JwtTokenProvider tokens){this.tokens=tokens;}

    @Override public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String header=exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if(header==null || !header.startsWith("Bearer ")) return chain.filter(exchange);
        try {
            var claims=tokens.parse(header.substring(7));
            var auth=new UsernamePasswordAuthenticationToken(claims.email(),null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + claims.role())));
            return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
        } catch (RuntimeException invalid) {
            return chain.filter(exchange);
        }
    }
}
