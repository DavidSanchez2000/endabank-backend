package com.endava.endabank.service.impl;

import com.endava.endabank.dao.IdentifierTypeDao;
import com.endava.endabank.exceptions.customexceptions.ResourceNotFoundException;
import com.endava.endabank.model.IdentifierType;
import com.endava.endabank.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentifierTypeServiceImplTest {

    @Mock
    private IdentifierTypeDao identifierTypeDao;

    @InjectMocks
    private IdentifierTypeServiceImpl identifierTypeService;

    @Test
    void testFindByIdShouldFailWhenIdentifierTypeNotFound() {
        IdentifierType identifierType = TestUtils.identifierTypeCC();
        Integer id = identifierType.getId();
        when(identifierTypeDao.findById(identifierType.getId())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> identifierTypeService.findById(id));
    }

    @Test
    void testFindByIdShouldSuccessWhenDataCorrect() {
        IdentifierType identifierType = TestUtils.identifierTypeCC();
        Integer id = identifierType.getId();
        when(identifierTypeDao.findById(identifierType.getId())).thenReturn(Optional.of(identifierType));
        IdentifierType identifierType1 = identifierTypeService.findById(id);
        assertEquals(identifierType, identifierType1);
    }


}