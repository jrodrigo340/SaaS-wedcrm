package com.wedcrm.controller;

import com.wedcrm.dto.request.UserRequestDTO;
import com.wedcrm.dto.response.UserResponseDTO;
import com.wedcrm.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService service;

    @PostMapping
    public UserResponseDTO create(@RequestBody UserRequestDTO dto) {

        User user = mapper.toEntity(dto);
        User savedUser = service.save(user);

        return mapper.toDTO(savedUser);
    }

    @GetMapping
    public List<User> list(){
        return service.findAll();
    }
}
