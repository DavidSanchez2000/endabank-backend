package com.endava.endabank.controller;

import com.endava.endabank.constants.Routes;
import com.endava.endabank.dto.user.AuthenticationDto;
import com.endava.endabank.exceptions.customexceptions.BadDataException;
import com.endava.endabank.service.impl.UserAuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping(Routes.API_ROUTE)
@AllArgsConstructor
public class AuthenticationController {
    private UserAuthenticationService userAuthenticationService;
    private AuthenticationManager authenticationManager;

    @PostMapping(value = Routes.LOGIN_ROUTE)
    public ResponseEntity<Map<String, Object>> createAuthenticationToken(@Valid @RequestBody AuthenticationDto authenticationDto){
        Authentication authentication = authenticate(authenticationDto.getEmail().toLowerCase(), authenticationDto.getPassword());
        return ResponseEntity.ok(userAuthenticationService.logInUser(authentication));
    }

    private Authentication authenticate(String email, String password) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }
}
