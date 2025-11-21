package com.example.demo.GetOldPeople;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.CreateTable.CreatePersonRepo;
import com.example.demo.CreateTable.Person;

@RestController
public class GetOldPeople {

    private final CreatePersonRepo personRepository;

    //@Autowired
    public GetOldPeople(CreatePersonRepo personRepository) {
        this.personRepository = personRepository;
    }

    @GetMapping("/oldPeople")
    public List<Person> getOldPeople() {
        return personRepository.findPeopleOlderThan(50);
    }
}