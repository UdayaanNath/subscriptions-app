package org.demo.com.subscriptionsapp.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.demo.com.subscriptionsapp.api.dto.user.CreateUser;
import org.demo.com.subscriptionsapp.api.dto.user.SearchUserCriteria;
import org.demo.com.subscriptionsapp.api.dto.user.UpdateUser;
import org.demo.com.subscriptionsapp.api.dto.user.User;
import org.demo.com.subscriptionsapp.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUser createUser) {
        User user = userService.createUser(createUser);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUser updateUser) {
        User user = userService.updateUser(id, updateUser);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<User>> listUser(SearchUserCriteria searchUserCriteria) {
        Page<User> users = userService.listUser(searchUserCriteria);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.getUser(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
