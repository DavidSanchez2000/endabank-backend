package com.endava.endabank.utils.user;

import com.endava.endabank.model.ForgotUserPasswordToken;
import com.endava.endabank.model.User;
import com.endava.endabank.security.utils.JwtManage;
import com.endava.endabank.service.ForgotUserPasswordTokenService;
import com.endava.endabank.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserValidationsTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ForgotUserPasswordTokenService forgotUserPasswordTokenService;

    @Test
    void validateUserForgotPasswordTokenShouldThrowException() {
        User user = TestUtils.getUserNotAdmin();
        String secret_dummy = "ZHVtbXkgdmFsdWUK";
        String token = JwtManage.generateToken(user.getId(), user.getEmail(), secret_dummy);
        when(forgotUserPasswordTokenService.findByUserId(user.getId())).thenReturn(TestUtils.getForgotUserPasswordToken(token+"asd"));
        assertThrows(AccessDeniedException.class, () -> UserValidations.
                validateUserForgotPasswordToken(forgotUserPasswordTokenService,token,secret_dummy));
    }
    @Test
    void validateUserForgotPasswordToken(){
        User user = TestUtils.getUserNotAdmin();
        String secret_dummy = "ZHVtbXkgdmFsdWUK";
        String token = JwtManage.generateToken(user.getId(), user.getEmail(), secret_dummy);
        when(forgotUserPasswordTokenService.findByUserId(user.getId())).thenReturn(TestUtils.getForgotUserPasswordToken(token));
        UserValidations.validateUserForgotPasswordToken(forgotUserPasswordTokenService,token,secret_dummy);
        ArgumentCaptor<ForgotUserPasswordToken> argumentCaptor = ArgumentCaptor.forClass(ForgotUserPasswordToken.class);
        verify(forgotUserPasswordTokenService).save(argumentCaptor.capture());
        assertNull(argumentCaptor.getValue().getToken());
    }

    @Test
    void validateOldPasswordIsCorrect(){
        User user = TestUtils.getUserNotAdmin();
        String oldPassword = "A1234567!aa";
        when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(false);
        assertThrows(AccessDeniedException.class,() -> UserValidations.validateOldPassword(passwordEncoder,user,oldPassword));
    }

    @Test
    void comparePasswordsDoNotMatch(){
        String password = "A1234567!a1";
        String rePassword = "A1234567!aa";
        assertThrows(AccessDeniedException.class,() -> UserValidations.comparePasswords(password,rePassword));
    }
}