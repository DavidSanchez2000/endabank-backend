package com.endava.endabank.service.impl;

import com.endava.endabank.constants.Strings;
import com.endava.endabank.dao.RoleDao;
import com.endava.endabank.exceptions.customexceptions.ResourceNotFoundException;
import com.endava.endabank.model.Role;
import com.endava.endabank.service.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {
    private RoleDao roleDao;

    @Override
    @Transactional(readOnly = true)
    public Role findById(Integer id) {
        return roleDao.findById(id).
                orElseThrow(() -> new ResourceNotFoundException(Strings.ROLE_NOT_FOUND));
    }
}
