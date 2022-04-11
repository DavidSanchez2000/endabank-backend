package com.endava.endabank.utils;

import com.endava.endabank.dto.user.UserRegisterDto;
import com.endava.endabank.dto.user.UserRegisterGetDto;
import com.endava.endabank.model.IdentifierType;
import com.endava.endabank.model.Role;
import com.endava.endabank.model.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.HashSet;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestUtils {
    public static User getUserAdmin() {
        Role adminRole = TestUtils.adminRole();
        IdentifierType cc = TestUtils.identifierTypeCC();
        return User.builder().
                id(1).
                email("admin@endava.com").
                phoneNumber("3212312321").
                identifier("1001000000").
                firstName("Endava ").
                lastName("Admin").
                isApproved(true).
                role(adminRole).
                identifierType(cc).
                bankAccounts(new ArrayList<>()).build();
    }

    public static User getUserNotAdmin() {
        Role userRole = TestUtils.userRole();
        IdentifierType cc = TestUtils.identifierTypeCC();
        return User.builder().
                id(1).
                email("user@endava.com").
                phoneNumber("3210000000").
                identifier("1001000000").
                firstName("Endava").
                lastName("User").
                password("Aa123456").
                isApproved(true).
                role(userRole).
                identifierType(cc).
                bankAccounts(new ArrayList<>()).build();
    }

    public static Role adminRole() {
        return new Role(1, "ROLE_ADMIN", new ArrayList<>(), new HashSet<>());
    }

    public static Role userRole() {
        return new Role(2, "ROLE_USER", new ArrayList<>(), new HashSet<>());
    }

    public static IdentifierType identifierTypeCC() {
        return new IdentifierType(1, "CC", new ArrayList<>());
    }

    public static UserRegisterDto getUserRegisterDto() {
        return UserRegisterDto.builder().
                email("user@endava.com").
                phoneNumber("3210000000").
                identifier("1001000000").
                firstName("Endava").
                lastName("User").
                password("Aa123456*").
                typeIdentifierId(1).build();
    }

    public static UserRegisterGetDto userRegisterGetDto() {
        return new ModelMapper().
                        map(getUserNotAdmin(), UserRegisterGetDto.class);
    }
}