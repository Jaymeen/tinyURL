package org.infinitesolutions.tinyurl.common.security;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private String jwkUrl = "https://cognito-idp.ap-south-1.amazonaws.com/ap-south-1_oyi9gS83e";

    private final JwkProvider jwkProvider;

    public JwtAuthenticationFilter() {
        this.jwkProvider = new UrlJwkProvider(jwkUrl);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return HttpMethod.GET.matches(method) && path.matches("^/[a-zA-Z0-9]+$");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authorization token is missing or invalid");
            return;
        }

        String token = authHeader.substring(7);

        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            Jwk jwk = jwkProvider.get(decodedJWT.getKeyId());
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            algorithm.verify(decodedJWT);

            String username = decodedJWT.getSubject();
            List<GrantedAuthority> authorities = List.of();

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        catch (TokenExpiredException e) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token has Expired");
            return;
        }
        catch (JWTVerificationException | JwkException e) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token Verification Failed");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse httpServletResponse, int status, String message) throws IOException {
        httpServletResponse.setStatus(status);
        httpServletResponse.setContentType("application/json");
        httpServletResponse.getWriter().write("{\"error\": \"" + message + "\"}");
        httpServletResponse.getWriter().flush();
    }
}
