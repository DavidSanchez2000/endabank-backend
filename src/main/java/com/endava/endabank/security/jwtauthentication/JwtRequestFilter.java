package com.endava.endabank.security.jwtauthentication;

import com.endava.endabank.constants.Strings;
import com.endava.endabank.exceptions.customexceptions.BadDataException;
import com.endava.endabank.security.utils.JwtManage;
import com.endava.endabank.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@AllArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith(Strings.BEARER)) {
            try {
                UsernamePasswordAuthenticationToken authenticationToken =
                        getAuthenticationToken(authorization, Strings.SECRET_JWT, request);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                throw new BadDataException(Strings.JWT_ERROR_TOKEN_VERIFICATION);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    protected UsernamePasswordAuthenticationToken getAuthenticationToken(
            String authorization, String secret, HttpServletRequest request) {
        Integer userId = JwtManage.verifyToken(authorization, secret);
        UsernamePasswordAuthenticationToken authenticationToken = userService.getUsernamePasswordToken(userId);
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authenticationToken;
    }

}
