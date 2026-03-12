package com.wedcrm.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class Address {

    private String street;

    private String number;

    private String complement;

    private String neighborhood;

    private String city;

    private String state;

    private String zipCode;

    private String country = "Brasil";

}
