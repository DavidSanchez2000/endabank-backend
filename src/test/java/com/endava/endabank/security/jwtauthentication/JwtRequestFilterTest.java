package com.endava.endabank.security.jwtauthentication;

import com.endava.endabank.constants.Strings;
import com.endava.endabank.exceptions.customexceptions.BadDataException;
import com.endava.endabank.model.User;
import com.endava.endabank.security.utils.JwtManage;
import com.endava.endabank.service.impl.UserServiceImpl;
import com.endava.endabank.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class JwtRequestFilterTest {

    @Mock
    private UserServiceImpl userService;
    @InjectMocks
    private JwtRequestFilter jwtRequestFilter;

    @Test
    void testDoFilterJWTInternalShouldFailWhenAuthorizationNull() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        JwtRequestFilter jwtRequestFilter1 = Mockito.spy(jwtRequestFilter);
        verify(jwtRequestFilter1, never()).getAuthenticationToken(any(), any(), any());
        jwtRequestFilter1.doFilterInternal(request, response, filterChain);
        verify(filterChain, Mockito.times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterJWTInternalShouldFailWhenAuthorizationNotBearer() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        JwtRequestFilter jwtRequestFilter1 = Mockito.spy(jwtRequestFilter);
        when(request.getHeader(Strings.AUTHORIZATION_BODY)).thenReturn("random");
        jwtRequestFilter1.doFilterInternal(request, response, filterChain);
        verify(jwtRequestFilter1, never()).getAuthenticationToken(any(), any(), any());
        verify(filterChain, Mockito.times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterJWTInternalShouldSuccessWhenDataCorrect() throws ServletException, IOException {
        User user = TestUtils.getUserNotAdmin();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        JwtRequestFilter jwtRequestFilter1 = Mockito.spy(jwtRequestFilter);
        when(request.getHeader(Strings.AUTHORIZATION_BODY)).thenReturn(Strings.BEARER_TEST);
        doReturn(new UsernamePasswordAuthenticationToken(user, "")).when(jwtRequestFilter1)
                .getAuthenticationToken(Strings.BEARER_TEST,
                        Strings.SECRET_JWT, request);
        jwtRequestFilter1.doFilterInternal(request, response, filterChain);
        verify(filterChain, Mockito.times(1)).doFilter(request, response);
        User user1 = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals(user.getId(), user1.getId());
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterJWTInternalShouldFailWhenNotValidToken() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        JwtRequestFilter jwtRequestFilter1 = Mockito.spy(jwtRequestFilter);
        when(request.getHeader(Strings.AUTHORIZATION_BODY)).thenReturn(Strings.BEARER_TEST);
        Mockito.doThrow(new AccessDeniedException("")).when(jwtRequestFilter1)
                .getAuthenticationToken(any(), any(), any());
        assertThrows(BadDataException.class, () ->
                jwtRequestFilter1.doFilterInternal(request, response, filterChain));
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testGetAuthenticationTokenShouldSuccessWhenDataCorrect() {
        User user = TestUtils.getUserNotAdmin();
        String token = JwtManage.generateToken(user.getId(), user.getEmail(), TestUtils.SECRET_DUMMY);
        ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
        when(userService.getUsernamePasswordToken(any(Integer.class))).
                thenReturn(new UsernamePasswordAuthenticationToken(user, null));
        HttpServletRequest request = mock(HttpServletRequest.class);
        jwtRequestFilter.getAuthenticationToken(Strings.BEARER + token, TestUtils.SECRET_DUMMY, request);
        verify(userService).getUsernamePasswordToken(idCaptor.capture());
        assertEquals(user.getId(), idCaptor.getValue());
    }
}