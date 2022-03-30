package com.endava.endabank.controllers;

import com.endava.endabank.dto.user.UserRegisterDto;
import com.endava.endabank.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserRegisterDto user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.save(user));
    }
    @GetMapping
    public ResponseEntity<?> get() {
        return ResponseEntity.status(HttpStatus.CREATED).body("hello world");
    }
}
