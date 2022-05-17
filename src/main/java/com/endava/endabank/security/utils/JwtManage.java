package com.endava.endabank.security.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.endava.endabank.constants.Strings;
import com.endava.endabank.exceptions.customexceptions.BadDataException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JwtManage {
    public static String generateToken(Integer id, String username, String secret) throws BadDataException {
        Map<String, Object> claims = new HashMap<>();
        if (id == null || username == null || "".equals(username)
                || secret == null || "".equals(secret)) {
            throw new BadDataException(Strings.BAD_DATA_FOR_TOKEN_GENERATION);
        }
        claims.put(Strings.USER_ID_BODY, id);
        return doGenerateToken(claims, username, secret);
    }

    private static String doGenerateToken(Map<String, Object> claims, String username, String secret) {
        Algorithm algorithm = Algorithm.HMAC512(secret);
        return JWT.create().
                withSubject(username).withIssuedAt(new Date(System.currentTimeMillis()))
                .withExpiresAt(new Date(System.currentTimeMillis() + 20 * 60 * 1000))
                .withPayload(claims)
                .sign(algorithm);
    }

    public static Integer verifyToken(String authorizationHeader, String secret) {
        if (authorizationHeader == null || "".equals(authorizationHeader)
                || secret == null || "".equals(secret)) {
            throw new BadDataException(Strings.BAD_DATA_FOR_TOKEN_VERIFICATION);
        }
        String token = authorizationHeader.substring(Strings.BEARER.length());
        Algorithm algorithm = Algorithm.HMAC512(secret);
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        Claim id = decodedJWT.getClaim(Strings.USER_ID_BODY);
        return id.asInt();
    }
}
