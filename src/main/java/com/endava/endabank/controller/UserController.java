package com.endava.endabank.controller;

import com.endava.endabank.constants.Permissions;
import com.endava.endabank.constants.Routes;
import com.endava.endabank.dto.user.UpdatePasswordDto;
import com.endava.endabank.dto.user.UserDetailsDto;
import com.endava.endabank.dto.user.UserPrincipalSecurity;
import com.endava.endabank.dto.user.UserRegisterDto;
import com.endava.endabank.dto.user.UserToApproveAccountDto;
import com.endava.endabank.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(Routes.API_ROUTE + Routes.USERS_ROUTE)
@AllArgsConstructor
public class UserController {
    private UserService userService;

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody UserRegisterDto user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveAndSendVerifyEmail(user)).getBody();
    }

    @GetMapping()
    @PreAuthorize(Permissions.AUTHORITY_ACCOUNT_VALIDATE)
    public ResponseEntity<List<UserToApproveAccountDto>> getUsersToApprove() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.usersToApprove());
    }

    @PutMapping(Routes.APPROVE_ACCOUNT_ROUTE)
    @PreAuthorize(Permissions.AUTHORITY_ACCOUNT_VALIDATE)
    public ResponseEntity<UserToApproveAccountDto> updateUserIsApproved(@PathVariable Integer id, @RequestBody Map<String, Boolean> map) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserAccountApprove(id, map.get("value")));
    }

    @GetMapping(Routes.RESET_PASSWORD_ROUTE + "/{email}")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable String email) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.generateResetPassword(email));
    }

    @PutMapping(Routes.RESET_PASSWORD_ROUTE)
    public ResponseEntity<Map<String, String>> updateForgotPassword(@RequestBody UpdatePasswordDto updatePasswordDto) throws AccessDeniedException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateForgotPassword(updatePasswordDto));
    }

    @PutMapping(Routes.UPDATE_PASSWORD)
    public ResponseEntity<Map<String, String>> updatePassword(Principal principal, @RequestBody UpdatePasswordDto updatePasswordDto) throws AccessDeniedException {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) principal;
        UserPrincipalSecurity user = (UserPrincipalSecurity) usernamePasswordAuthenticationToken.getPrincipal();
        return ResponseEntity.status(HttpStatus.OK).body(userService.updatePassword(user, updatePasswordDto));
    }

    @GetMapping(Routes.RESOURCE_DETAILS)
    public UserDetailsDto getDetails(Principal principal) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) principal;
        Collection<GrantedAuthority> authorities = usernamePasswordAuthenticationToken.getAuthorities();
        UserPrincipalSecurity user = (UserPrincipalSecurity) usernamePasswordAuthenticationToken.getPrincipal();
        return userService.getUserDetails(user, authorities);
    }

    @GetMapping(Routes.EMAIL_VALIDATION_ROUTE + "/{email}")
    public Map<String, Object> getDetailsById(@PathVariable String email) {
        return userService.generateEmailVerification(null, email);
    }

    @PostMapping(Routes.EMAIL_VALIDATION_ROUTE + "/{email}")
    public Map<String, Object> validateEmail(@PathVariable String email) {
        return userService.verifyEmail(email);
    }
}
