package com.endava.endabank.service;

import com.endava.endabank.dto.user.UpdatePasswordDto;
import com.endava.endabank.dto.user.UserDetailsDto;
import com.endava.endabank.dto.user.UserPrincipalSecurity;
import com.endava.endabank.dto.user.UserRegisterDto;
import com.endava.endabank.dto.user.UserToApproveAccountDto;
import com.endava.endabank.model.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface UserService {
    List<UserToApproveAccountDto> usersToApprove();

    User findById(Integer id);

    User save(UserRegisterDto user);

    UsernamePasswordAuthenticationToken getUsernamePasswordToken(Integer userId);

    UserToApproveAccountDto updateUserAccountApprove(Integer id, boolean value);

    Map<String, Object> generateResetPassword(String email);

    Map<String, Object> generateEmailVerification(User user, String email);

    Map<String, String> updateForgotPassword(UpdatePasswordDto updatePasswordDto) throws AccessDeniedException;

    Map<String, String> updatePassword(UserPrincipalSecurity user, UpdatePasswordDto updatePasswordDto) throws AccessDeniedException;

    UserDetailsDto getUserDetails(UserPrincipalSecurity user, Collection<GrantedAuthority> authorities);

    Map<String, Object> verifyEmail(String email);

    Map<String, Object> saveAndSendVerifyEmail(UserRegisterDto userRegisterDto);
}
