package com.wedcrm.service;

import com.wedcrm.entity.Customer;
import com.wedcrm.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository repository;

    public Customer save(Customer customer){
        return repository.save(customer);
    }

    public List<Customer> findAll(){
        return repository.findAll();
    }
}
