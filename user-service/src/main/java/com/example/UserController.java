package com.example;

import com.example.Dto.UserDto;
import com.example.Dto.UserProfile;
import com.example.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/user-service")
public class UserController {

    @Autowired
    private UserService userService;

    // this method will create user and make entry in db and return id bcoz in service createUser() method we are returning id
    @PostMapping("/user")
    ResponseEntity<Long> createUser(@RequestBody UserDto userDto) throws ExecutionException, InterruptedException, JsonProcessingException {
        return ResponseEntity.ok(userService.createUser(userDto));
    }
    // so above function will return id so we will use that id to return profile from header for that particular id
    @GetMapping("/user")
    ResponseEntity<UserProfile> getuserProfile(@RequestHeader Long id) throws UserNotExistException {
        return ResponseEntity.ok(userService.getuserProfile(id));
    }
}
