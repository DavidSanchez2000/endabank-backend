package com.endava.endabank.constants;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Routes {
    public static final String API_ROUTE = "/api/v1";
    public static final String USERS_ROUTE = "/users";
    public static final String API_USERS_ROUTE = API_ROUTE + USERS_ROUTE;
    public static final String LOGIN_ROUTE = "/login";
    public static final String API_LOGIN_ROUTE = API_ROUTE + LOGIN_ROUTE;
    public static final String FRONTEND_ROUTE = "http://localhost:3000";
    public static final String RESET_PASSWORD_ROUTE = "/reset-password";
    public static final String APPROVE_ACCOUNT_ROUTE = "/approve/{id}";
    public static final String RESET_PASSWORD_FRONTEND_ROUTE = FRONTEND_ROUTE + "/reset-password/?token=";
    public static final String UPDATE_PASSWORD = "/change-password";
    public static final String SWAGGER_IU = "/swagger-ui.html/**";
    public static final String SWAGGER_JSON = "/v2/api-docs/**";
    public static final String CONFIGURATION = "/configuration/**";
    public static final String SWAGGER = "/swagger*/**";
    public static final String WEB_JARS = "/webjars/**";
    public static final String RESOURCE_DETAILS = "/details";
    public static final String EMAIL_VALIDATION_FRONTEND_ROUTE = FRONTEND_ROUTE + "/verify-email/?token=";
    public static final String EMAIL_VALIDATION_ROUTE =  "/verify-email";
    public static final String SECRET_JWT_DEFAULT = "secret";
}
