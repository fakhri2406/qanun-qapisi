package com.qanunqapisi.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qanunqapisi.dto.response.error.ErrorResponse;
import com.qanunqapisi.repository.RevokedTokenRepository;
import com.qanunqapisi.util.ErrorMessages;
import com.qanunqapisi.util.TokenGenerator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProperties jwtProperties;
    private final UserDetailsService userDetailsService;
    private final RevokedTokenRepository revokedTokenRepository;
    private final TokenGenerator tokenGenerator;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String token = header.substring(7);

            if (revokedTokenRepository.findByToken(token).isPresent()) {
                writeErrorResponse(response, HttpStatus.UNAUTHORIZED, ErrorMessages.ACCESS_TOKEN_REVOKED);
                return;
            }

            Claims claims;
            try {
                claims = Jwts.parserBuilder()
                    .setSigningKey(tokenGenerator.getSigningKey())
                    .requireIssuer(jwtProperties.getIssuer())
                    .requireAudience(jwtProperties.getAudience())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            } catch (JwtException e) {
                SecurityContextHolder.clearContext();
                writeErrorResponse(response, HttpStatus.UNAUTHORIZED, ErrorMessages.INVALID_ACCESS_TOKEN);
                return;
            }

            String username = claims.getSubject();
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);
                UserDetails userDetails;
                if (roles != null) {
                    userDetails = new org.springframework.security.core.userdetails.User(
                        username,
                        "",
                        roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .toList()
                    );
                } else {
                    userDetails = userDetailsService.loadUserByUsername(username);
                }
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        SecurityContextHolder.clearContext();
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error(status.name())
            .message(message)
            .build();
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
