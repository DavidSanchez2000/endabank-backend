package com.endava.endabank.service;

import com.endava.endabank.model.ForgotUserPasswordToken;

public interface ForgotUserPasswordTokenService {
    ForgotUserPasswordToken findByUserId(Integer id);

    void save(ForgotUserPasswordToken forgotUserPasswordToken);
}
