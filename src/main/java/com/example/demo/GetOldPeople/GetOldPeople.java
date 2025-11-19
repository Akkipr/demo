package com.example.demo.GetOldPeople;

import com.example.demo.CreateTable.CreatePersonRepo;
import com.example.demo.CreateTable.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class GetOldPeople {

    private final CreatePersonRepo personRepository;

    @Autowired
    public GetOldPeople(CreatePersonRepo personRepository) {
        this.personRepository = personRepository;
    }

    @GetMapping("/oldPeople")
    public List<Person> getOldPeople() {
        return personRepository.findPeopleOlderThan(50);
    }
}