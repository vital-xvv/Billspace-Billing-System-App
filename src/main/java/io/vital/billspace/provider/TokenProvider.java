package io.vital.billspace.provider;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import io.micrometer.common.util.StringUtils;
import io.vital.billspace.model.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;

@Component
public class TokenProvider {
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 1_800_000;
    private static final String AUTHORITIES = "authorities";
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 432_000_000;
    @Value("${jwt.secret}")
    private String secret;
    @Value("${application.title}")
    private String appName;
    private static final String DESCRIPTION = "Billing System for Small Businesses";

    public String createAccessToken(UserPrincipal userPrincipal){
        String[] claims = getClaimsFromUser(userPrincipal);
        return JWT.create().withIssuer(appName).withAudience(DESCRIPTION).withIssuedAt(Instant.now())
                .withSubject(userPrincipal.getUsername()).withArrayClaim(AUTHORITIES, claims)
                .withExpiresAt(new Date(currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(secret));
    }

    public String createRefreshToken(UserPrincipal userPrincipal){
        return JWT.create().withIssuer(appName).withAudience(DESCRIPTION).withIssuedAt(Instant.now())
                .withSubject(userPrincipal.getUsername())
                .withExpiresAt(new Date(currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(secret));
    }

    private String[] getClaimsFromUser(UserPrincipal userPrincipal) {
        return userPrincipal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toArray(String[]::new);
    }

    public List<GrantedAuthority> getAuthorities(String token){
        String[] claims = getClaimsFromToken(token);
        return Arrays
                .stream(claims)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private String[] getClaimsFromToken(String token) {
        JWTVerifier verifier = getJWTVerifier();
        return verifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
    }

    private JWTVerifier getJWTVerifier() {
        JWTVerifier verifier;
        try{
            Algorithm algorithm = Algorithm.HMAC512(secret);
            verifier = JWT.require(algorithm).withIssuer(appName).build();
        }catch (JWTVerificationException ex){
            throw new JWTVerificationException("Token can not be verified.");
        }
        return verifier;
    }

    public Authentication getAuthentication(String email, List<GrantedAuthority> authorities,
                                            HttpServletRequest request){
        UsernamePasswordAuthenticationToken userPasswordAuthToken =
                new UsernamePasswordAuthenticationToken(email, null, authorities);
        userPasswordAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return userPasswordAuthToken;
    }

    public boolean isTokenValid(String email, String token){
        JWTVerifier verifier = getJWTVerifier();
        return StringUtils.isNotEmpty(email) && !isTokenExpired(verifier, token);
    }

    private boolean isTokenExpired(JWTVerifier verifier, String token) {
        Date expiration = verifier.verify(token).getExpiresAt();
        return expiration.before(new Date());
    }

    public String getSubject(String token, HttpServletRequest request){
        JWTVerifier verifier = getJWTVerifier();
        try{
            return verifier.verify(token).getSubject();
        }catch (TokenExpiredException ex){
            request.setAttribute("expiredMessage", ex.getMessage());
            throw ex;
        }catch (InvalidClaimException ex){
            request.setAttribute("invalidClaim", ex.getMessage());
            throw ex;
        }catch (Exception ex){
            throw ex;
        }
    }
}
