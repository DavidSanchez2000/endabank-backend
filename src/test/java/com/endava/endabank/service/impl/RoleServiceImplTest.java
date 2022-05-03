package com.endava.endabank.service.impl;

import com.endava.endabank.dao.RoleDao;
import com.endava.endabank.exceptions.customexceptions.ResourceNotFoundException;
import com.endava.endabank.model.Role;
import com.endava.endabank.service.RoleService;
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
class RoleServiceImplTest {

    @Mock
    private RoleDao roleDao;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void findByIdShouldThrowAndException() {
        Role role = TestUtils.getUserNotAdmin().getRole();
        Integer id = role.getId();
        when(roleDao.findById(role.getId())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> roleService.findById(id));
    }

    @Test
    void findByIdShouldSuccess() {
        Role role = TestUtils.getUserNotAdmin().getRole();
        Integer id = role.getId();
        when(roleDao.findById(role.getId())).thenReturn(Optional.of(role));
        Role role1 = roleService.findById(id);
        assertEquals(role, role1);
    }

}