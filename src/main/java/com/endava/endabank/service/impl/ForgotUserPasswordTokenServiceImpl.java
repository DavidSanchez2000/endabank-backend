package com.endava.endabank.service.impl;

import com.endava.endabank.constants.Strings;
import com.endava.endabank.dao.ForgotUserPasswordTokenDao;
import com.endava.endabank.model.ForgotUserPasswordToken;
import com.endava.endabank.service.ForgotUserPasswordTokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ForgotUserPasswordTokenServiceImpl implements ForgotUserPasswordTokenService {
    private ForgotUserPasswordTokenDao forgotUserPasswordTokenDao;

    @Override
    public ForgotUserPasswordToken findByUserId(Integer id) {
        return forgotUserPasswordTokenDao.findByUserId(id).
                orElseThrow(() -> new UsernameNotFoundException(Strings.USER_TOKEN_NOT_FOUND));
    }

    @Override
    public void save(ForgotUserPasswordToken forgotUserPasswordTokenNew) {
        Optional<ForgotUserPasswordToken> forgotUserPasswordTokenDbOpt =
                forgotUserPasswordTokenDao.findByUserId(forgotUserPasswordTokenNew.getUser().getId());
        if (forgotUserPasswordTokenDbOpt.isPresent()) {
            forgotUserPasswordTokenNew = forgotUserPasswordTokenDbOpt.get();
            forgotUserPasswordTokenNew.setToken(forgotUserPasswordTokenNew.getToken());
        }
        forgotUserPasswordTokenDao.save(forgotUserPasswordTokenNew);
    }
}
