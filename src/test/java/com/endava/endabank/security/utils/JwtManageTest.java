package com.endava.endabank.security.utils;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.endava.endabank.exceptions.customexceptions.BadDataException;
import com.endava.endabank.model.User;
import com.endava.endabank.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ActiveProfiles("test")
class JwtManageTest {

    private final String secret_dummy = TestUtils.SECRET_DUMMY;

    @Test
    void generateValidTokenTest() throws BadDataException {
        User user = TestUtils.getUserAdmin();
        String token = JwtManage.generateToken(user.getId(),
                user.getEmail(), secret_dummy);
        assertNotNull(token);
        assertNotEquals("", token);
    }

    @Test
    void generateInvalidTokenTest() {
        assertThrows(BadDataException.class, () ->
                JwtManage.generateToken(null, "a@a.com", secret_dummy));
        assertThrows(BadDataException.class, () ->
                JwtManage.generateToken(1, null, secret_dummy));
        assertThrows(BadDataException.class, () ->
                JwtManage.generateToken(1, "", secret_dummy));
        assertThrows(BadDataException.class, () ->
                JwtManage.generateToken(1, "a@a.com", ""));
        assertThrows(BadDataException.class, () ->
                JwtManage.generateToken(1, "a@a.com", null));
    }

    @Test
    void verifyValidToken() throws BadDataException {
        User user = TestUtils.getUserAdmin();
        String token = JwtManage.generateToken(user.getId(), user.getEmail(), secret_dummy);
        int idUser = JwtManage.verifyToken("Bearer " + token, secret_dummy);
        assertEquals(idUser, user.getId());
    }

    @Test
    void verifyInvalidToken() throws BadDataException {
        User user = TestUtils.getUserAdmin();
        String token = JwtManage.generateToken(user.getId(), user.getEmail(), secret_dummy);
        assertThrows(JWTVerificationException.class, () ->
                JwtManage.verifyToken("Bearer " + token + "asd", secret_dummy)
        );
        assertThrows(JWTVerificationException.class, () ->
                JwtManage.verifyToken("Bearer ", secret_dummy)
        );
        assertThrows(BadDataException.class, () ->
                JwtManage.verifyToken("", secret_dummy)
        );
        assertThrows(BadDataException.class, () ->
                JwtManage.verifyToken(null, secret_dummy)
        );
        assertThrows(BadDataException.class, () ->
                JwtManage.verifyToken("Bearer ", null)
        );
        assertThrows(BadDataException.class, () ->
                JwtManage.verifyToken("Bearer ", "")
        );
    }
}