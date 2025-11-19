package com.example.demo.GetDataFromTable;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.CreateTable.CreatePersonRepo;
import com.example.demo.CreateTable.Person;


@RestController     // define the path
public class GetData {

    @Autowired      // avoid constructor 
    private CreatePersonRepo personRepo;

    @GetMapping("/getData")
    public List<Person> getData() {
        List<Person> persons = personRepo.findAll();   // fetch all the data from the database table
        return persons;
    }

}
