package com.endava.endabank.service.impl;

import com.endava.endabank.configuration.MailProperties;
import com.endava.endabank.constants.Permissions;
import com.endava.endabank.constants.Routes;
import com.endava.endabank.constants.Strings;
import com.endava.endabank.dao.UserDao;
import com.endava.endabank.dto.user.UpdatePasswordDto;
import com.endava.endabank.dto.user.UserDetailsDto;
import com.endava.endabank.dto.user.UserPrincipalSecurity;
import com.endava.endabank.dto.user.UserRegisterDto;
import com.endava.endabank.dto.user.UserRegisterGetDto;
import com.endava.endabank.dto.user.UserToApproveAccountDto;
import com.endava.endabank.exceptions.customexceptions.BadDataException;
import com.endava.endabank.exceptions.customexceptions.ServiceUnavailableException;
import com.endava.endabank.exceptions.customexceptions.UniqueConstraintViolationException;
import com.endava.endabank.model.ForgotUserPasswordToken;
import com.endava.endabank.model.Permission;
import com.endava.endabank.model.Role;
import com.endava.endabank.model.User;
import com.endava.endabank.security.utils.JwtManage;
import com.endava.endabank.service.ForgotUserPasswordTokenService;
import com.endava.endabank.service.IdentifierTypeService;
import com.endava.endabank.service.RoleService;
import com.endava.endabank.utils.MailService;
import com.endava.endabank.utils.TestUtils;
import com.endava.endabank.utils.user.UserValidations;
import com.sendgrid.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private IdentifierTypeService identifierTypeService;

    @Mock
    private RoleService roleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MailService mailService;

    @Mock
    private ForgotUserPasswordTokenService forgotUserPasswordTokenService;

    private final MailProperties mailProperties = new MailProperties("test", "test", "test", "test");

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService =
                new UserServiceImpl(userDao, modelMapper, identifierTypeService, roleService,
                        passwordEncoder, forgotUserPasswordTokenService, mailService, mailProperties);
    }

    @Test
    void testGetUsernamePasswordTokenShouldSuccessWhenDataCorrect() {
        User user = TestUtils.getUserAdmin();
        UserPrincipalSecurity userPrincipalSecurity = TestUtils.getUserAdminPrincipalSecurity();
        UserServiceImpl userService1 = Mockito.spy(userService);
        doReturn(user).when(userService1).findById(1);
        when(modelMapper.map(TestUtils.getUserAdmin(), UserPrincipalSecurity.class)).thenReturn(userPrincipalSecurity);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = userService1.getUsernamePasswordToken(1);
        UserPrincipalSecurity userPrincipalSecurityRet = (UserPrincipalSecurity) usernamePasswordAuthenticationToken.getPrincipal();
        Collection<GrantedAuthority> grantedAuthorities = usernamePasswordAuthenticationToken.getAuthorities();
        assertEquals(userPrincipalSecurity.getEmail(), userPrincipalSecurityRet.getEmail());
        assertEquals(userPrincipalSecurity.getId(), userPrincipalSecurityRet.getId());
        assertEquals(userPrincipalSecurity.getFirstName(), userPrincipalSecurityRet.getFirstName());
        assertEquals(userPrincipalSecurity.getPhoneNumber(), userPrincipalSecurityRet.getPhoneNumber());
        assertEquals(userPrincipalSecurity.isApproved(), userPrincipalSecurityRet.isApproved());
        List<String> authorities = grantedAuthorities.stream().map(GrantedAuthority::getAuthority).toList();
        Role userRole = user.getRole();
        assertTrue(authorities.contains(userRole.getName()));
        userRole.getPermissions().stream().map(Permission::getName).forEach(permission -> {
            assertTrue(authorities.contains(permission));
        });
    }

    @Test
    void testFindByIdShouldSuccessWhenDataCorrect() {
        User userNotAdmin = TestUtils.getUserNotAdmin();
        when(userDao.findById(1)).thenReturn(Optional.of(userNotAdmin));
        User user = userService.findById(1);
        assertEquals(userNotAdmin.getEmail(), user.getEmail());
        assertEquals(userNotAdmin.getFirstName(), user.getFirstName());
        assertEquals(userNotAdmin.getLastName(), user.getLastName());
        assertEquals(userNotAdmin.getPhoneNumber(), user.getPhoneNumber());
        assertEquals(userNotAdmin.getId(), user.getId());
        assertEquals(userNotAdmin.getRole().getId(), user.getRole().getId());
        assertEquals(userNotAdmin.getRole().getName(), user.getRole().getName());
    }

    @Test
    void testFindByIdShouldFailWhenUsernameNotFound() {
        when(userDao.findById(1)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.findById(1));
    }

    @Test
    void testUpdateUserAccountApproveToTrueShouldSuccessWhenDataCorrect() {
        User userNotAdmin = TestUtils.getUserNotAdminNonApproved();
        UserServiceImpl userService1 = Mockito.spy(userService);
        doReturn(userNotAdmin).when(userService1).findById(1);
        when(userDao.save(TestUtils.getUserNotAdmin())).thenReturn(userNotAdmin);
        when(modelMapper.map(TestUtils.getUserNotAdmin(), UserToApproveAccountDto.class)).
                thenReturn(TestUtils.getUserApprovedAccountDto());
        UserToApproveAccountDto userToApproveAccountDto = userService1.updateUserAccountApprove(1, true);
        assertEquals(userNotAdmin.getEmail(), userToApproveAccountDto.getEmail());
        assertEquals(userNotAdmin.getId(), userToApproveAccountDto.getId());
        assertTrue(userToApproveAccountDto.isApproved());

    }

    @Test
    void testUpdateUserAccountApproveToFalseShouldSuccessWhenDataCorrect() {
        User userNotAdmin = TestUtils.getUserNotAdminNonApproved();
        UserServiceImpl userService1 = Mockito.spy(userService);
        doReturn(userNotAdmin).when(userService1).findById(1);
        when(userDao.save(TestUtils.getUserNotAdmin())).thenReturn(userNotAdmin);

        when(modelMapper.map(TestUtils.getUserNotAdmin(), UserToApproveAccountDto.class)).
                thenReturn(TestUtils.getUserNotAprrovedAccountDto());
        UserToApproveAccountDto userToApproveAccountDto = userService1.updateUserAccountApprove(1, false);
        assertEquals(userNotAdmin.getEmail(), userToApproveAccountDto.getEmail());
        assertEquals(userNotAdmin.getId(), userToApproveAccountDto.getId());
        assertEquals(userNotAdmin.getIsApproved(), userToApproveAccountDto.isApproved());
    }

    @Test
    void testGetUserDetailsShouldSuccessWhenDataCorrect() {
        UserServiceImpl userService1 = Mockito.spy(userService);
        UserPrincipalSecurity userPrincipalSecurity = TestUtils.getUserPrincipalSecurity();
        doReturn(TestUtils.userDetailsGetDto(userPrincipalSecurity)).
                when(userService1).mapToUserDetailsDto(userPrincipalSecurity);
        Role role = TestUtils.adminRole();
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role.getName()));
        role.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getName())));
        UserDetailsDto userDetailsGetDto = userService1.getUserDetails(userPrincipalSecurity, authorities);
        assertEquals(userPrincipalSecurity.getEmail(), userDetailsGetDto.getEmail());
        assertEquals(userPrincipalSecurity.isApproved(), userDetailsGetDto.isApproved());
        Collection<String> userDetailsAuthorities = userDetailsGetDto.getAuthorities();
        assertEquals(authorities.size(), userDetailsAuthorities.size());
        authorities.forEach(a -> assertTrue(userDetailsAuthorities.contains(a.getAuthority())));
    }

    @Test
    void testVerifyEmailShouldSuccessWhenDataCorrect() {
        User user = TestUtils.getUserNotAdmin();
        String token = JwtManage.generateToken(1, user.getEmail(), TestUtils.SECRET_DUMMY);
        try (MockedStatic<JwtManage> utilities = Mockito.mockStatic(JwtManage.class)) {
            when(userDao.findById(1)).thenReturn(Optional.of(user));
            utilities.when(() -> JwtManage.verifyToken(Strings.BEARER + token, Strings.SECRET_JWT)).thenReturn(1);
            Map<String, Object> map = userService.verifyEmail(token);
            assertEquals(Strings.EMAIL_VERIFIED, map.get(Strings.MESSAGE_RESPONSE));
        }
    }

    @Test
    void testSaveAndSendVerifyEmailTestShouldSuccessWhenDataCorrect() {
        User user = TestUtils.getUserNotAdmin();
        UserRegisterDto userRegisterDto = TestUtils.getUserRegisterDto();
        UserServiceImpl userService1 = Mockito.spy(userService);
        doReturn(user).when(userService1).save(userRegisterDto);
        Map<String, Object> map = new HashMap<>();
        map.put(Strings.STATUS_CODE_RESPONSE, HttpStatus.ACCEPTED.value());
        map.put(Strings.MESSAGE_RESPONSE, Strings.EMAIL_FOR_VERIFICATION_SENT);
        doReturn(map).when(userService1).generateEmailVerification(user, user.getEmail());
        UserRegisterGetDto userRegisterGetDto = TestUtils.getUserRegisterGetDto();
        when(modelMapper.map(user, UserRegisterGetDto.class)).thenReturn(userRegisterGetDto);
        Map<String, Object> mapRet = userService1.saveAndSendVerifyEmail(userRegisterDto);
        assertEquals(HttpStatus.ACCEPTED.value(), mapRet.get(Strings.STATUS_CODE_RESPONSE));
        assertEquals(Strings.EMAIL_FOR_VERIFICATION_SENT, mapRet.get(Strings.MESSAGE_RESPONSE));
        assertEquals(userRegisterGetDto, mapRet.get(Strings.USER_BODY));
    }

    @Test
    void testUsersToApproveShouldSuccessWhenDataCorrect() {
        when(userDao.findAll()).thenReturn(TestUtils.getUserList());
        UserServiceImpl userService1 = Mockito.spy(userService);
        doReturn(TestUtils.getUserApprovedAccountDto()).when(userService1).mapToUserToApproveDto(any());
        List<UserToApproveAccountDto> listUserToValidated = userService1.usersToApprove();
        assertTrue(listUserToValidated.size() > 0);
    }

    @Test
    void testGenerateResetPasswordShouldFailWhenUserNotOnDb() {
        User user = TestUtils.getUserNotAdmin();
        String email = user.getEmail();
        when(userDao.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, ()
                -> userService.generateResetPassword(email));
    }

    @Test
    void testGenerateResetPasswordShouldSuccessWhenDataCorrect() {
        User user = TestUtils.getUserNotAdmin();
        when(userDao.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        try (MockedStatic<JwtManage> jwtMock = Mockito.mockStatic(JwtManage.class)) {
            jwtMock.when(() ->
                            JwtManage.generateToken(user.getId(), user.getEmail(), Strings.SECRET_JWT)).
                    thenReturn("token");
            String link = Routes.RESET_PASSWORD_FRONTEND_ROUTE + "token";
            Response response = TestUtils.getSendGridResponse();
            when(
                    mailService.sendEmail(user.getEmail(), user.getFirstName(),
                            link, "test",
                            Strings.EMAIL_AS_RESET_PASSWORD)).thenReturn(response);
            Map<String, Object> mapRet = userService.generateResetPassword(user.getEmail());
            assertEquals(HttpStatus.ACCEPTED, mapRet.get(Strings.STATUS_CODE_RESPONSE));
            assertEquals(Strings.MAIL_SENT, mapRet.get(Strings.MESSAGE_RESPONSE));
            ArgumentCaptor<ForgotUserPasswordToken> argumentCaptor
                    = ArgumentCaptor.forClass(ForgotUserPasswordToken.class);
            verify(forgotUserPasswordTokenService,
                    times(1)).save(argumentCaptor.capture());
            ForgotUserPasswordToken forgotUserPasswordToken = argumentCaptor.getValue();
            assertEquals(user.getId(), forgotUserPasswordToken.getUser().getId());
            assertEquals(user.getEmail(), forgotUserPasswordToken.getUser().getEmail());
            assertEquals("token", forgotUserPasswordToken.getToken());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testSendEmailToUserShouldFailWhenServiceUnavailable() {
        User user = TestUtils.getUserNotAdmin();
        try (MockedStatic<JwtManage> jwtMock = Mockito.mockStatic(JwtManage.class)) {
            jwtMock.when(() ->
                            JwtManage.generateToken(user.getId(), user.getEmail(), null)).
                    thenReturn("token");
            BiFunction<User, String, String> callback = (u, t) -> "token";
            when(mailService.sendEmail(any(), any(), any(), any(), any()))
                    .thenReturn(null);
            assertThrows(ServiceUnavailableException.class,
                    () -> userService.sendEmailToUser("templateId", user, "test", callback));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testGenerateEmailVerificationShouldSuccessWhenDataCorrect() {
        User user = TestUtils.getUserNotAdmin();
        String email = user.getEmail();
        try (MockedStatic<JwtManage> jwtMock = Mockito.mockStatic(JwtManage.class)) {
            jwtMock.when(() ->
                            JwtManage.generateToken(user.getId(), user.getEmail(), Strings.SECRET_JWT)).
                    thenReturn("token");
            String emailDb = "&email=" + email;
            String link = Routes.EMAIL_VALIDATION_FRONTEND_ROUTE + "token" + emailDb;
            Response response = TestUtils.getSendGridResponse();
            when(
                    mailService.sendEmail(user.getEmail(), user.getFirstName(),
                            link, "test",
                            Strings.EMAIL_AS_VERIFY_EMAIL)).thenReturn(response);
            Map<String, Object> mapRet = userService.generateEmailVerification(user, email);
            assertEquals(HttpStatus.ACCEPTED, mapRet.get(Strings.STATUS_CODE_RESPONSE));
            assertEquals(Strings.MAIL_SENT, mapRet.get(Strings.MESSAGE_RESPONSE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testGenerateResetPasswordShouldFailWhenEmailNull() {
        assertThrows(UsernameNotFoundException.class,
                () -> userService.generateResetPassword(null));
    }

    @Test
    void testGenerateEmailVerificationShouldFailWhenUsernameNotFound() {
        assertThrows(UsernameNotFoundException.class,
                () -> userService.generateEmailVerification(null, null));
    }

    @Test
    void testGenerateEmailVerificationShouldFailWhenDataIncorrect() {
        User user = TestUtils.getUserNotAdmin();
        ;
        String email = user.getEmail();
        user.setIsEmailVerified(true);
        assertThrows(BadDataException.class, () -> userService.generateEmailVerification(user, email));
    }

    @Test
    void testMapToUserDetailsDtoShouldSuccessWhenDataCorrect() {
        UserPrincipalSecurity user = TestUtils.getUserPrincipalSecurity();
        assertEquals(modelMapper.map(user, UserDetailsDto.class), userService.mapToUserDetailsDto(user));
    }


    @Nested
    @DisplayName("User save test")
    class SaveUserTests {
        UserRegisterDto userRegisterDto;

        @BeforeEach
        void setUp() {
            userRegisterDto = TestUtils.getUserRegisterDto();
            when(modelMapper.map(userRegisterDto, User.class)).thenReturn(TestUtils.getUserNotAdmin());
        }

        @Test
        void testSaveShouldFailWhenUserEmailAlreadyExists() {
            when(userDao.findByEmail(userRegisterDto.getEmail())).thenReturn(Optional.of(TestUtils.getUserNotAdmin()));
            assertThrows(UniqueConstraintViolationException.class, () -> userService.save(userRegisterDto));
        }

        @Test
        void testSaveShouldFailWhenUserIdentifierAlreadyExists() {
            when(userDao.findByEmail(userRegisterDto.getEmail())).thenReturn(Optional.empty());
            when(userDao.findByIdentifier(userRegisterDto.getIdentifier())).thenReturn(Optional.of(TestUtils.getUserNotAdmin()));
            assertThrows(UniqueConstraintViolationException.class, () -> userService.save(userRegisterDto));
        }

        @Test
        void testSaveUserShouldSuccessWhenDataCorrect() {
            when(userDao.findByEmail(userRegisterDto.getEmail())).thenReturn(Optional.empty());
            when(userDao.findByIdentifier(userRegisterDto.getIdentifier())).thenReturn(Optional.empty());
            when(roleService.findById(Permissions.ROLE_USER)).
                    thenReturn(TestUtils.userRole());
            when(identifierTypeService.findById(userRegisterDto.
                    getTypeIdentifierId())).thenReturn(TestUtils.identifierTypeCC());
            User userDb = userService.save(userRegisterDto);
            assertEquals(userDb.getEmail(), userRegisterDto.getEmail());
            assertEquals(Permissions.ROLE_USER, userDb.getRole().getId());
            assertEquals(userDb.getIdentifier(), userRegisterDto.getIdentifier());
            assertEquals(userDb.getIdentifierType().getId(), userRegisterDto.getTypeIdentifierId());
        }
    }

    @Test
    void testUpdateForgotPasswordShouldSuccessWhenDataCorrect() {
        UpdatePasswordDto updatePasswordDto = TestUtils.getUpdatePasswordDto();
        User user = TestUtils.getUserNotAdmin();
        String secret_dummy = TestUtils.SECRET_DUMMY;
        String token = JwtManage.generateToken(user.getId(), user.getEmail(), secret_dummy);
        when(userDao.findById(user.getId())).thenReturn(Optional.of(TestUtils.getUserNotAdmin()));
        updatePasswordDto.setToken(token);
        try (MockedStatic<UserValidations> utilities = Mockito.mockStatic(UserValidations.class)) {
            utilities.when(() ->
                    UserValidations.validateUserForgotPasswordToken(
                            forgotUserPasswordTokenService, token,
                            Strings.SECRET_JWT)).thenReturn(1);
            Map<String, String> map = userService.updateForgotPassword(updatePasswordDto);
            assertEquals(Strings.PASSWORD_UPDATED, map.get(Strings.MESSAGE_RESPONSE));
        }
    }

    @Test
    void testUpdateForgotPasswordShouldFailWhenTokenIncorrect() {
        UpdatePasswordDto updatePasswordDto = TestUtils.getUpdatePasswordDto();
        User user = TestUtils.getUserNotAdmin();
        String secret_dummy = TestUtils.SECRET_DUMMY;
        String token = JwtManage.generateToken(user.getId(), user.getEmail(), secret_dummy);
        when(forgotUserPasswordTokenService.findByUserId(user.getId())).thenReturn(TestUtils.getForgotUserPasswordToken(token + "abc"));
        updatePasswordDto.setToken(token);
        try (MockedStatic<JwtManage> utilities = Mockito.mockStatic(JwtManage.class)) {
            utilities.when(() -> JwtManage.verifyToken(Strings.BEARER + token, Strings.SECRET_JWT)).thenReturn(1);
            assertThrows(AccessDeniedException.class, () -> userService.updateForgotPassword(updatePasswordDto));
        }
    }

    @Test
    void testUpdatePasswordShouldSuccessWhenDataCorrect() {
        UserPrincipalSecurity userPrincipalSecurity = TestUtils.getUserPrincipalSecurity();
        UpdatePasswordDto updatePasswordDto = TestUtils.getUpdatePasswordDto();
        when(userDao.findById(userPrincipalSecurity.getId())).thenReturn(Optional.of(TestUtils.getUserNotAdmin()));
        when(passwordEncoder.matches(updatePasswordDto.getOldPassword(), TestUtils.getUserNotAdmin().getPassword())).thenReturn(true);
        Map<String, String> map = userService.updatePassword(userPrincipalSecurity, updatePasswordDto);
        assertEquals(Strings.PASSWORD_UPDATED, map.get(Strings.MESSAGE_RESPONSE));
    }

    @Test
    void testUpdatePasswordShouldFailWhenUserNotCorrect() {
        UserPrincipalSecurity userPrincipalSecurity = TestUtils.getUserPrincipalSecurity();
        UpdatePasswordDto updatePasswordDto = TestUtils.getUpdatePasswordDto();
        when(userDao.findById(userPrincipalSecurity.getId())).thenReturn(Optional.of(TestUtils.getUserNotAdmin()));
        assertThrows(AccessDeniedException.class, () -> userService.updatePassword(userPrincipalSecurity, updatePasswordDto));
    }


}

