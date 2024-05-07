package io.vital.billspace.filter;

import io.vital.billspace.provider.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthorizationFilter extends OncePerRequestFilter {
    private static final String[] PUBLIC_ROUTES = {"/user/login", "/user/verify/code", "/user/register"};
    private final TokenProvider tokenProvider;
    public static final String TOKEN_KEY = "token";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String EMAIL_KEY = "email";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try{
            Map<String, String> values = getRequestValues(request);
            String token = getToken(request);
            if(tokenProvider.isTokenValid(values.get(EMAIL_KEY), token)){
                List<GrantedAuthority> authorities = tokenProvider.getAuthorities(values.get(TOKEN_KEY));
                Authentication authentication = tokenProvider.getAuthentication(values.get(EMAIL_KEY),
                        authorities, request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else { SecurityContextHolder.clearContext(); }
            filterChain.doFilter(request, response);
        }catch (Exception e){
            log.error(e.getMessage());
            //TODO
            //processError(request, response, e);
        }
    }

    private String getToken(HttpServletRequest request) {
        return ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(header -> header.startsWith(TOKEN_PREFIX))
                .map(token -> token.replace(TOKEN_PREFIX, "")).get();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getHeader(HttpHeaders.AUTHORIZATION) == null ||
                !request.getHeader(HttpHeaders.AUTHORIZATION).startsWith(TOKEN_PREFIX) ||
                request.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.name()) ||
                Arrays.asList(PUBLIC_ROUTES).contains(request.getRequestURI());
    }

    private Map<String, String> getRequestValues(HttpServletRequest request) {
        return Map.of(EMAIL_KEY, tokenProvider.getSubject(getToken(request), request), TOKEN_KEY, getToken(request));
    }


}
