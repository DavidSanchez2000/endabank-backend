package com.endava.endabank.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Strings {
    public static final String ROLE_NOT_FOUND = "The role was not found on the database.";
    public static final String IDENTIFIER_TYPE_NOT_FOUND = "The identifier type was not found on the database.";
    public static final String USER_NOT_FOUND = "The user was not found on the database.";
    public static final String MAIL_SENT = "Mail Sent";
    public static final String MAIL_FAIL = "There was an error sending the mail.";
    public static final String OLD_PASSWORD_NOT_MATCH = "The old password does not match.";
    public static final String PASSWORDS_DOES_NOT_MATCH = "The passwords do not match.";
    public static final String TOKEN_RESET_PASSWORD_INVALID = "The token is not valid for this user.";
    public static final String USER_TOKEN_NOT_FOUND = "The token for this user was not found on the database.";
    public static final String PASSWORD_UPDATED = "Password updated.";
    public static final String CONSTRAINT_IDENTIFIER_VIOLATED = "There is already a user registered with this identifier.";
    public static final String CONSTRAINT_EMAIL_VIOLATED = "There is already a user registered with this email.";
    public static final String SECRET_JWT = System.getenv("JWT_ENDABANK_SECRET");
    public static final String MESSAGE_RESPONSE = "message";
    public static final String STATUS_CODE_RESPONSE = "statusCode";
    public static final String BAD_DATA_FOR_TOKEN_GENERATION = "The data required for token generation not provided.";
    public static final String EMAIL_VERIFIED = "All set, the email was successfully verified.";
    public static final String EMAIL_NOT_VERIFIED = "The user email has not been verified yet.";
    public static final String EMAIL_FOR_VERIFICATION_SENT = "User registered, Check your email for the link for verification.";
    public static final String EMAIL_ALREADY_VERIFIED = "The email was already verified.";
    public static final String EMAIL_AS_VERIFY_EMAIL = "Email Verification";
    public static final String EMAIL_AS_RESET_PASSWORD = "Reset Password";
    public static final String BAD_DATA_FOR_TOKEN_VERIFICATION = "The data required for the verification is missing.";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String ROL = "rol";
    public static final String IS_APPROVED = "isApproved";
    public static  final String USER_BODY = "user";
}
