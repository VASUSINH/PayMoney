
package com.PayMoney.Service;

import com.PayMoney.Client.UserClient;
import com.PayMoney.DTO.UserAuthResponseDTO;
import com.PayMoney.Entity.AccountStatus;
import feign.FeignException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserClient userClient;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        if (token.isEmpty()) {
            SecurityContextHolder.clearContext();
            writeUnauthorized(response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String email = jwtService.extractEmail(token);

            if (email == null || email.isBlank()) {
                SecurityContextHolder.clearContext();
                writeUnauthorized(response);
                return;
            }

            UserAuthResponseDTO userEntity = findUser(email);

            if (userEntity == null
                    || !jwtService.isTokenValid(token, userEntity)) {
                SecurityContextHolder.clearContext();
                writeUnauthorized(response);
                return;
            }

            if (userEntity.getAccountStatus() != AccountStatus.ACTIVE) {
                SecurityContextHolder.clearContext();
                writeForbidden(response);
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userEntity,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole()))
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            writeUnauthorized(response);
            return;
        } catch (FeignException exception) {
            SecurityContextHolder.clearContext();
            writeServiceUnavailable(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    // user-service owns the users table, so the user is fetched from there.
    private UserAuthResponseDTO findUser(String email) {

        try {
            return userClient.getUserByEmail(email);
        } catch (FeignException.NotFound exception) {
            return null;
        }
    }

    private void writeUnauthorized(HttpServletResponse response)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"message\":\"Invalid or expired authentication token\"}"
        );
    }

    private void writeForbidden(HttpServletResponse response)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"message\":\"Account is deactivated. Reactivate your account to continue.\"}"
        );
    }

    private void writeServiceUnavailable(HttpServletResponse response)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"message\":\"User service is unavailable. Please try again later.\"}"
        );
    }
}
