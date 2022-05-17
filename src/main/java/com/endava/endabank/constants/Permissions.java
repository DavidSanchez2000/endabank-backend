package com.endava.endabank.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Permissions {
    public static final String AUTHORITY_ACCOUNT_VALIDATE = "hasAuthority('ACCOUNT:VALIDATE')";
    public static final Integer ROLE_USER = 2;
}
