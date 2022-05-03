package com.endava.endabank.service.impl;

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
import com.endava.endabank.exceptions.customexceptions.ResourceNotFoundException;
import com.endava.endabank.exceptions.customexceptions.ServiceUnavailableException;
import com.endava.endabank.exceptions.customexceptions.UniqueConstraintViolationException;
import com.endava.endabank.model.ForgotUserPasswordToken;
import com.endava.endabank.model.IdentifierType;
import com.endava.endabank.model.Permission;
import com.endava.endabank.model.Role;
import com.endava.endabank.model.User;
import com.endava.endabank.configuration.MailProperties;
import com.endava.endabank.security.utils.JwtManage;
import com.endava.endabank.service.ForgotUserPasswordTokenService;
import com.endava.endabank.service.IdentifierTypeService;
import com.endava.endabank.service.RoleService;
import com.endava.endabank.service.UserService;
import com.endava.endabank.utils.MailService;
import com.endava.endabank.utils.user.UserValidations;
import com.google.common.annotations.VisibleForTesting;
import com.sendgrid.Response;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private UserDao userDao;
    private ModelMapper modelMapper;
    private IdentifierTypeService identifierTypeService;
    private RoleService roleService;
    private PasswordEncoder passwordEncoder;
    private ForgotUserPasswordTokenService forgotUserPasswordTokenService;
    private MailService mailService;
    private MailProperties mailProperties;


    @Override
    @Transactional(readOnly = true)
    public List<UserToApproveAccountDto> usersToApprove() {
        List<User> users = userDao.findAll();
        return users.stream().map(this::mapToUserToApproveDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Integer id) {
        return userDao.findById(id).
                orElseThrow(() -> new UsernameNotFoundException(Strings.USER_NOT_FOUND));
    }

    @Override
    @Transactional
    public User save(UserRegisterDto userDto) {
        User user = modelMapper.map(userDto, User.class);
        Optional<User> userExist = userDao.findByEmail(user.getEmail());
        if (userExist.isPresent()) {
            throw new UniqueConstraintViolationException(Strings.CONSTRAINT_EMAIL_VIOLATED);
        }
        userExist = userDao.findByIdentifier(user.getIdentifier());
        if (userExist.isPresent()) {
            throw new UniqueConstraintViolationException(Strings.CONSTRAINT_IDENTIFIER_VIOLATED);
        }
        Role role = roleService.findById(Permissions.ROLE_USER);
        IdentifierType identifierType = identifierTypeService.findById(userDto.getTypeIdentifierId());
        user.setRole(role);
        user.setIdentifierType(identifierType);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userDao.save(user);
        return user;
    }


    @Override
    @Transactional(readOnly = true)
    public UsernamePasswordAuthenticationToken getUsernamePasswordToken(Integer userId) {
        User user = this.findById(userId);
        Role role = user.getRole();
        Set<Permission> permissions = role.getPermissions();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role.getName()));
        permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.getName())));
        UserPrincipalSecurity userPrincipalSecurity = modelMapper.map(user, UserPrincipalSecurity.class);
        return
                new UsernamePasswordAuthenticationToken(userPrincipalSecurity, null, authorities);
    }

    @Override
    @Transactional(readOnly = true)
    public UserToApproveAccountDto updateUserAccountApprove(Integer id, boolean value) {
        User user = this.findById(id);
        user.setIsApproved(value);
        userDao.save(user);
        return this.mapToUserToApproveDto(user);
    }

    @Override
    public Map<String, Object> generateResetPassword(String email) {
        User userDb = userDao.findByEmail(email).
                orElseThrow(() -> new UsernameNotFoundException(Strings.USER_NOT_FOUND));
        BiFunction<User, String, String> callback = (user, token) -> {
            ForgotUserPasswordToken forgotUserPasswordToken = new ForgotUserPasswordToken(user, token);
            forgotUserPasswordTokenService.save(forgotUserPasswordToken);
            return Routes.RESET_PASSWORD_FRONTEND_ROUTE + token;
        };
        return this.sendEmailToUser(mailProperties.getTemplatePassword(), userDb,
                Strings.EMAIL_AS_RESET_PASSWORD, callback);
    }

    @Override
    public Map<String, Object> generateEmailVerification(User userDb, String email) {
        if (userDb == null) {
            userDb = userDao.findByEmail(email).
                    orElseThrow(() -> new UsernameNotFoundException(Strings.USER_NOT_FOUND));
        }
        if (Boolean.TRUE.equals(userDb.getIsEmailVerified())) {
            throw new BadDataException(Strings.EMAIL_ALREADY_VERIFIED);
        }
        BiFunction<User, String, String> callback = (user, token) -> {
            user.setTokenEmailVerification(token);
            userDao.save(user);
            String emailDb = "&email=" + user.getEmail();
            return Routes.EMAIL_VALIDATION_FRONTEND_ROUTE + token + emailDb;
        };
        return this.sendEmailToUser(mailProperties.getTemplateVerify(), userDb,
                Strings.EMAIL_AS_VERIFY_EMAIL, callback);
    }

    @Override
    @Transactional
    public Map<String, String> updateForgotPassword(UpdatePasswordDto updatePasswordDto) throws AccessDeniedException {
        Map<String, String> map = new HashMap<>();
        UserValidations.comparePasswords(updatePasswordDto.getPassword(), updatePasswordDto.getRePassword());
        int userId = UserValidations.validateUserForgotPasswordToken(
                forgotUserPasswordTokenService, updatePasswordDto.getToken(), Strings.SECRET_JWT);
        User user = userDao.findById(userId).
                orElseThrow(() -> new UsernameNotFoundException(Strings.USER_NOT_FOUND));
        user.setPassword(passwordEncoder.encode(updatePasswordDto.getPassword()));
        userDao.save(user);
        map.put(Strings.MESSAGE_RESPONSE, Strings.PASSWORD_UPDATED);
        return map;
    }

    @Override
    public Map<String, String> updatePassword(UserPrincipalSecurity userPrincipalSecurity, UpdatePasswordDto updatePasswordDto) throws AccessDeniedException {
        Map<String, String> map = new HashMap<>();
        UserValidations.comparePasswords(updatePasswordDto.getPassword(), updatePasswordDto.getRePassword());
        User user = userDao.findById(userPrincipalSecurity.getId()).
                orElseThrow(() -> new UsernameNotFoundException(Strings.USER_NOT_FOUND));
        UserValidations.validateOldPassword(passwordEncoder, user, updatePasswordDto.getOldPassword());
        user.setPassword(passwordEncoder.encode(updatePasswordDto.getPassword()));
        userDao.save(user);
        map.put(Strings.MESSAGE_RESPONSE, Strings.PASSWORD_UPDATED);
        return map;
    }

    @Override
    public UserDetailsDto getUserDetails(UserPrincipalSecurity user, Collection<GrantedAuthority> authorities) {
        UserDetailsDto userDetailsDto = mapToUserDetailsDto(user);
        userDetailsDto.setAuthorities(authorities);
        return userDetailsDto;
    }

    @Override
    public Map<String, Object> verifyEmail(String token) {
        int userId = JwtManage.verifyToken("Bearer " + token, Strings.SECRET_JWT);
        User user = userDao.findById(userId).
                orElseThrow(() -> new UsernameNotFoundException(Strings.USER_NOT_FOUND));
        user.setIsEmailVerified(true);
        userDao.save(user);
        Map<String, Object> map = new HashMap<>();
        map.put(Strings.MESSAGE_RESPONSE, Strings.EMAIL_VERIFIED);
        return map;
    }

    @Override
    @Transactional
    public Map<String, Object> saveAndSendVerifyEmail(UserRegisterDto userRegisterDto) {
        User userDb = this.save(userRegisterDto);
        Map<String, Object> map = this.generateEmailVerification(userDb, userDb.getEmail());
        map.put(Strings.USER_BODY, modelMapper.map(userDb, UserRegisterGetDto.class));
        map.put(Strings.MESSAGE_RESPONSE, Strings.EMAIL_FOR_VERIFICATION_SENT);
        return map;
    }

    @VisibleForTesting
    UserToApproveAccountDto mapToUserToApproveDto(User user) {
        return modelMapper.map(user, UserToApproveAccountDto.class);
    }

    @VisibleForTesting
    UserDetailsDto mapToUserDetailsDto(UserPrincipalSecurity user) {
        return modelMapper.map(user, UserDetailsDto.class);
    }

    @VisibleForTesting
    Map<String, Object> sendEmailToUser(String templateId, User user, String asName,
                                                BiFunction<User, String, String> callback) {
        Map<String, Object> map = new HashMap<>();
        try {
            String token = JwtManage.generateToken(user.getId(), user.getEmail(), Strings.SECRET_JWT);
            String link = callback.apply(user, token);
            Response response = mailService.sendEmail(user.getEmail(), user.getFirstName(), link, templateId, asName);
            map.put(Strings.STATUS_CODE_RESPONSE, HttpStatus.valueOf(response.getStatusCode()));
        } catch (Exception e) {
            throw new ServiceUnavailableException(e.getMessage());
        }
        map.put(Strings.MESSAGE_RESPONSE, Strings.MAIL_SENT);
        return map;
    }

}
