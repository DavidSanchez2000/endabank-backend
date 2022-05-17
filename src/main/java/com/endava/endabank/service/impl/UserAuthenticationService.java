package com.endava.endabank.service.impl;

import com.endava.endabank.constants.Strings;
import com.endava.endabank.dao.UserDao;
import com.endava.endabank.exceptions.customexceptions.BadDataException;
import com.endava.endabank.model.Role;
import com.endava.endabank.model.User;
import com.endava.endabank.security.UserAuthentication;
import com.endava.endabank.security.utils.JwtManage;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@AllArgsConstructor
public class UserAuthenticationService implements UserDetailsService {
    private UserDao userDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDao.findByEmail(username).
                orElseThrow(() -> new UsernameNotFoundException(Strings.USER_NOT_FOUND));
        Role role = user.getRole();
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role.getName()));
        return new UserAuthentication(
                user.getEmail(), user.getPassword(),
                authorities, user.getId(), user.getIsApproved(), user.getIsEmailVerified());
    }

    public Map<String, Object> logInUser(Authentication authentication) {
        UserAuthentication userAuthentication = (UserAuthentication) authentication.getPrincipal();
        boolean data = userAuthentication.getIsEmailVerified() == null || !userAuthentication.getIsEmailVerified();
        if (data) {
            throw new AccessDeniedException(Strings.EMAIL_NOT_VERIFIED);
        }
        if(userAuthentication.getAuthorities().toArray().length == 0){
            throw new BadDataException(Strings.ROLE_REQUIRED);
        }
        String role = userAuthentication.getAuthorities().toArray()[0].toString();
        final String token = JwtManage.generateToken(userAuthentication.getId(),
                userAuthentication.getUsername(), Strings.SECRET_JWT);
        Map<String, Object> dataResponse = new HashMap<>();
        dataResponse.put(Strings.ACCESS_TOKEN, token);
        dataResponse.put(Strings.ROL, role);
        dataResponse.put(Strings.IS_APPROVED, userAuthentication.getIsApproved());
        return dataResponse;
    }

}
