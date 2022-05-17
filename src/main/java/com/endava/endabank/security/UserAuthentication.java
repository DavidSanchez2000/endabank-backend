package com.endava.endabank.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Objects;


@Getter
@Setter
public class UserAuthentication extends User {
    private Integer id;
    private Boolean isApproved;
    private Boolean isEmailVerified;

    public UserAuthentication(String username, String password,
                              Collection<? extends GrantedAuthority> authorities,
                              Integer id, Boolean isApproved, Boolean isEmailVerified) {
        super(username, password, authorities);
        this.id = id;
        this.isApproved = isApproved != null && isApproved;
        this.isEmailVerified = isEmailVerified;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != UserAuthentication.class) return false;
        UserAuthentication userAuthenticationObj = (UserAuthentication) obj;
        return Objects.equals(this.id, userAuthenticationObj.getId()) &&
                Objects.equals(this.getUsername(), userAuthenticationObj.getUsername());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.getUsername());
    }

    public UserAuthentication(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    }
}
