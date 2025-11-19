package com.example.demo.InsertDataToTable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.CreateTable.CreatePersonRepo;
import com.example.demo.CreateTable.Person;


@RestController     // define the path
public class InsertPerson {

    @Autowired      // avoid constructor 
    private CreatePersonRepo personRepo;

    @GetMapping("/insertPerson")
    public Person insertPerson() {
        // id is auto-incremented so dw about that
        Person person = new Person();
        person.setName("testguy1");
        person.setAge(18);
        return personRepo.save(person);     // save to the datavase!
    }
    


}
