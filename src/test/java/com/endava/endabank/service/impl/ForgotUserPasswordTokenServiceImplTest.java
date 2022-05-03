package com.endava.endabank.service.impl;

import com.endava.endabank.dao.ForgotUserPasswordTokenDao;
import com.endava.endabank.model.ForgotUserPasswordToken;
import com.endava.endabank.utils.TestUtils;
import org.checkerframework.checker.nullness.Opt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ForgotUserPasswordTokenServiceImplTest {
    @Mock
    private ForgotUserPasswordTokenDao forgotUserPasswordTokenDao;
    @InjectMocks
    private ForgotUserPasswordTokenServiceImpl forgotUserPasswordTokenService;


    @Test
    void findByUserIdShouldThrowException() {
        when(forgotUserPasswordTokenDao.findByUserId(1)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> forgotUserPasswordTokenService.findByUserId(1));
    }

    @Test
    void findByUserIdShouldReturnForgotPasswordToken() {
        when(forgotUserPasswordTokenDao.findByUserId(1)).thenReturn(Optional.of(TestUtils.getForgotUserPasswordToken("token")));
        assertEquals(TestUtils.getForgotUserPasswordToken("token"), forgotUserPasswordTokenService.findByUserId(1));
    }

    @Test
    void saveWhenTokenExistOnTheBd() {
        ForgotUserPasswordToken forgotUserPasswordToken =
                TestUtils.getForgotUserPasswordToken("new-token");
        ForgotUserPasswordToken forgotUserPasswordTokenDb =
                TestUtils.getForgotUserPasswordToken("token");
        when(forgotUserPasswordTokenDao.
                findByUserId(forgotUserPasswordToken.getUser().getId())).
                thenReturn(Optional.of(forgotUserPasswordTokenDb));
        forgotUserPasswordTokenService.save(forgotUserPasswordToken);
        ArgumentCaptor<ForgotUserPasswordToken> argumentCaptor = ArgumentCaptor.forClass(ForgotUserPasswordToken.class);
        verify(forgotUserPasswordTokenDao, times(1)).save(argumentCaptor.capture());
        assertEquals(forgotUserPasswordToken.getToken(), argumentCaptor.getValue().getToken());
        assertEquals(forgotUserPasswordToken.getUser(), argumentCaptor.getValue().getUser());
    }

    @Test
    void saveWhenTokenNotExistOnTheBd() {
        ForgotUserPasswordToken forgotUserPasswordToken =
                TestUtils.getForgotUserPasswordToken("new-token");
        when(forgotUserPasswordTokenDao.
                findByUserId(forgotUserPasswordToken.getUser().getId())).
                thenReturn(Optional.empty());
        forgotUserPasswordTokenService.save(forgotUserPasswordToken);
        ArgumentCaptor<ForgotUserPasswordToken> argumentCaptor = ArgumentCaptor.forClass(ForgotUserPasswordToken.class);
        verify(forgotUserPasswordTokenDao, times(1)).save(argumentCaptor.capture());
        assertEquals(forgotUserPasswordToken.getToken(), argumentCaptor.getValue().getToken());
        assertEquals(forgotUserPasswordToken.getUser(), argumentCaptor.getValue().getUser());
    }
}