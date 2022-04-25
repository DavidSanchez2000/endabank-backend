package com.endava.endabank.service.impl;

import com.endava.endabank.constants.Permissions;
import com.endava.endabank.constants.Strings;
import com.endava.endabank.dao.ForgotUserPasswordTokenDao;
import com.endava.endabank.dao.IdentifierTypeDao;
import com.endava.endabank.dao.RoleDao;
import com.endava.endabank.dao.UserDao;
import com.endava.endabank.dto.user.UpdatePasswordDto;
import com.endava.endabank.dto.user.UserDetailsDto;
import com.endava.endabank.dto.user.UserPrincipalSecurity;
import com.endava.endabank.dto.user.UserRegisterDto;
import com.endava.endabank.dto.user.UserToApproveAccountDto;
import com.endava.endabank.exceptions.customexceptions.UniqueConstraintViolationException;
import com.endava.endabank.model.Permission;
import com.endava.endabank.model.Role;
import com.endava.endabank.model.User;
import com.endava.endabank.security.utils.JwtManage;
import com.endava.endabank.service.ForgotUserPasswordTokenService;
import com.endava.endabank.utils.TestUtils;
import com.endava.endabank.utils.user.UserValidations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private IdentifierTypeDao identifierTypeDao;

    @InjectMocks
    private IdentifierTypeServiceImpl identifierTypeService
            = new IdentifierTypeServiceImpl(identifierTypeDao);

    @Mock
    private RoleDao roleDao;

    @InjectMocks
    private RoleServiceImpl roleService = new RoleServiceImpl(roleDao);

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ForgotUserPasswordTokenDao forgotUserPasswordTokenDao;

    @InjectMocks
    private ForgotUserPasswordTokenServiceImpl forgotUserPasswordTokenServiceImpl
            = new ForgotUserPasswordTokenServiceImpl(forgotUserPasswordTokenDao);

    @InjectMocks
    private UserServiceImpl userService =
            new UserServiceImpl(userDao, modelMapper, identifierTypeService, roleService,
                    passwordEncoder, forgotUserPasswordTokenServiceImpl
            );

    @Mock
    private ForgotUserPasswordTokenService forgotUserPasswordTokenService;

    @Test
    void getUsernamePasswordToken() {
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
    void findById() {
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
    void findByIdShouldThrowExceptionWhenNotFound() {
        when(userDao.findById(1)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.findById(1));
    }

    @Test
    void updateUserAccountApproveToTrue() {
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
    void updateUserAccountApproveToFalse() {
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
    void getUserDetails() {
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
    void verifyEmail() {
        User user = TestUtils.getUserNotAdmin();
        String token = JwtManage.generateToken(1, user.getEmail(), TestUtils.SECRET_DUMMY);
        try (MockedStatic<JwtManage> utilities = Mockito.mockStatic(JwtManage.class)) {
            when(userDao.findById(1)).thenReturn(Optional.of(user));
            utilities.when(() -> JwtManage.verifyToken("Bearer " + token, null)).thenReturn(1);
            Map<String, Object> map = userService.verifyEmail(token);
            assertEquals(Strings.EMAIL_VERIFIED, map.get(Strings.MESSAGE_RESPONSE));
        }
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
        void saveShouldThrowExceptionWhenUserEmailAlreadyExists() {
            when(userDao.findByEmail(userRegisterDto.getEmail())).thenReturn(Optional.of(TestUtils.getUserNotAdmin()));
            assertThrows(UniqueConstraintViolationException.class, () -> userService.save(userRegisterDto));
        }

        @Test
        void saveShouldThrowExceptionWhenUserIdentifierAlreadyExists() {
            when(userDao.findByEmail(userRegisterDto.getEmail())).thenReturn(Optional.empty());
            when(userDao.findByIdentifier(userRegisterDto.getIdentifier())).thenReturn(Optional.of(TestUtils.getUserNotAdmin()));
            assertThrows(UniqueConstraintViolationException.class, () -> userService.save(userRegisterDto));
        }

        @Test
        void saveUserShouldSuccess() {
            when(userDao.findByEmail(userRegisterDto.getEmail())).thenReturn(Optional.empty());
            when(userDao.findByIdentifier(userRegisterDto.getIdentifier())).thenReturn(Optional.empty());
            when(roleService.findById(Permissions.ROLE_USER)).
                    thenReturn(Optional.of(TestUtils.userRole()));
            when(identifierTypeService.findById(userRegisterDto.
                    getTypeIdentifierId())).thenReturn(Optional.of(TestUtils.identifierTypeCC()));
            User userDb = userService.save(userRegisterDto);
            assertEquals(userDb.getEmail(), userRegisterDto.getEmail());
            assertEquals(Permissions.ROLE_USER, userDb.getRole().getId());
            assertEquals(userDb.getIdentifier(), userRegisterDto.getIdentifier());
            assertEquals(userDb.getIdentifierType().getId(), userRegisterDto.getTypeIdentifierId());
        }
    }

    @Test
    void updateForgotPasswordShouldSuccess() {
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
                            null)).thenReturn(1);
            Map<String, String> map = userService.updateForgotPassword(updatePasswordDto);
            assertEquals(Strings.PASSWORD_UPDATED, map.get(Strings.MESSAGE_RESPONSE));
        }
    }

    @Test
    void updateForgotPasswordShouldThrowException() {
        UpdatePasswordDto updatePasswordDto = TestUtils.getUpdatePasswordDto();
        User user = TestUtils.getUserNotAdmin();
        String secret_dummy = TestUtils.SECRET_DUMMY;
        String token = JwtManage.generateToken(user.getId(), user.getEmail(), secret_dummy);
        when(forgotUserPasswordTokenService.findByUserId(user.getId())).thenReturn(TestUtils.getForgotUserPasswordToken(token + "abc"));
        updatePasswordDto.setToken(token);
        try (MockedStatic<JwtManage> utilities = Mockito.mockStatic(JwtManage.class)) {
            utilities.when(() -> JwtManage.verifyToken("Bearer " + token, null)).thenReturn(1);
            assertThrows(AccessDeniedException.class, () -> userService.updateForgotPassword(updatePasswordDto));
        }
    }

    @Test
    void updatePasswordShouldSuccess() {
        UserPrincipalSecurity userPrincipalSecurity = TestUtils.getUserPrincipalSecurity();
        UpdatePasswordDto updatePasswordDto = TestUtils.getUpdatePasswordDto();
        when(userDao.findById(userPrincipalSecurity.getId())).thenReturn(Optional.of(TestUtils.getUserNotAdmin()));
        when(passwordEncoder.matches(updatePasswordDto.getOldPassword(), TestUtils.getUserNotAdmin().getPassword())).thenReturn(true);
        Map<String, String> map = userService.updatePassword(userPrincipalSecurity, updatePasswordDto);
        assertEquals(Strings.PASSWORD_UPDATED, map.get(Strings.MESSAGE_RESPONSE));
    }

    @Test
    void updatePasswordShouldThrowException() {
        UserPrincipalSecurity userPrincipalSecurity = TestUtils.getUserPrincipalSecurity();
        UpdatePasswordDto updatePasswordDto = TestUtils.getUpdatePasswordDto();
        when(userDao.findById(userPrincipalSecurity.getId())).thenReturn(Optional.of(TestUtils.getUserNotAdmin()));
        assertThrows(AccessDeniedException.class, () -> userService.updatePassword(userPrincipalSecurity, updatePasswordDto));
    }


}

