package com.wedcrm.controller;

import com.wedcrm.entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService service;

    @PostMapping
    public Customer create(@RequestBody Customer customer){
        return service.save(customer);
    }

    @GetMapping
    public List<Customer> list(){
        return service.findAll();
    }
}
