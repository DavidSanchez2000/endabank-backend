package com.endava.endabank.service.impl;

import com.endava.endabank.constants.Strings;
import com.endava.endabank.dao.UserDao;
import com.endava.endabank.model.User;
import com.endava.endabank.security.UserAuthentication;
import com.endava.endabank.security.utils.JwtManage;
import com.endava.endabank.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserAuthenticationServiceTest {
    @Mock
    private UserDao userDao;
    @InjectMocks
    private UserAuthenticationService userAuthenticationService;

    @Test
    void loadUserByUsernameShouldSuccesTest() {
        User user = TestUtils.getUserAdmin();
        when(userDao.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        UserAuthentication userAuthentication = (UserAuthentication) userAuthenticationService.loadUserByUsername(user.getEmail());
        assertEquals(user.getEmail(), userAuthentication.getUsername());
        assertEquals(user.getId(), userAuthentication.getId());
        assertEquals(user.getIsApproved(), userAuthentication.getIsApproved());
        assertEquals(user.getIsEmailVerified(), userAuthentication.getIsEmailVerified());
        assertEquals(user.getRole().getName(),
                userAuthentication.getAuthorities().toArray()[0].toString());
    }

    @Test
    void loadUserByUsernameShouldFailIfUserNotExist() {
        User user = TestUtils.getUserAdmin();
        String email = user.getEmail();
        when(userDao.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class,
                () -> userAuthenticationService.loadUserByUsername(email));
    }

    @Test
    void logInUserVerifiedShouldThrowAnException() {
        UserAuthentication userAuthentication = TestUtils.getUserAuthentication();
        userAuthentication.setIsEmailVerified(false);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userAuthentication, null);
        assertThrows(AccessDeniedException.class, () -> userAuthenticationService.logInUser(authentication));
    }

    @Test
    void logInUserNullVerifiedShouldThrowAnException() {
        UserAuthentication userAuthentication = TestUtils.getUserAuthentication();
        userAuthentication.setIsEmailVerified(null);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userAuthentication, null);
        assertThrows(AccessDeniedException.class, () -> userAuthenticationService.logInUser(authentication));
    }

    @Test
    void logInUserShouldSuccess() {
        try (MockedStatic<JwtManage> mockedJwtManage = Mockito.mockStatic(JwtManage.class)) {
            UserAuthentication userAuthentication = TestUtils.getUserAuthentication();
            Authentication authentication = new UsernamePasswordAuthenticationToken(userAuthentication, null);
            userAuthenticationService.logInUser(authentication);
             mockedJwtManage.when(()->
                     JwtManage.generateToken(userAuthentication.getId(),
                             userAuthentication.getUsername(), null)).thenReturn("token");
            Map<String, Object> map = userAuthenticationService.logInUser(authentication);
            assertEquals("token", map.get(Strings.ACCESS_TOKEN).toString());
            assertTrue((Boolean) map.get(Strings.IS_APPROVED));
            assertEquals(userAuthentication.getAuthorities().toArray()[0].toString(),
                    map.get(Strings.ROL).toString());
        }
    }
}