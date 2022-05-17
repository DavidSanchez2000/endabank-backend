package com.endava.endabank.service.impl;

import com.endava.endabank.constants.Strings;
import com.endava.endabank.dao.IdentifierTypeDao;
import com.endava.endabank.exceptions.customexceptions.ResourceNotFoundException;
import com.endava.endabank.model.IdentifierType;
import com.endava.endabank.service.IdentifierTypeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class IdentifierTypeServiceImpl implements IdentifierTypeService {
    private IdentifierTypeDao identifierTypeDao;

    @Override
    @Transactional(readOnly = true)
    public IdentifierType findById(Integer id) {
        return identifierTypeDao.findById(id).
                orElseThrow((() -> new ResourceNotFoundException(Strings.IDENTIFIER_TYPE_NOT_FOUND)));
    }
}
