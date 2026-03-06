package com.wedcrm.service;

import com.wedcrm.entity.User;
import com.wedcrm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public User save(User user){
        return repository.save(user);
    }

    public List<User> findAll(){
        return repository.findAll();
    }
}
