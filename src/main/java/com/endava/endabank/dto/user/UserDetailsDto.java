package com.endava.endabank.dto.user;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Data
public class UserDetailsDto {
    private String email;
    private String phoneNumber;
    private String firstName;
    private boolean isApproved;
    private Collection<String> authorities;
}
