package com.wedcrm.controller;

import com.wedcrm.entity.User;
import com.wedcrm.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService service;

    @PostMapping
    public User create(@RequestBody User user){
        return service.save(user);
    }

    @GetMapping
    public List<User> list(){
        return service.findAll();
    }
}
