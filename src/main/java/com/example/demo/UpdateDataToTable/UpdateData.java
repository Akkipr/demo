package com.example.demo.UpdateDataToTable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.CreateTable.CreatePersonRepo;

@RestController
public class UpdateData {

    @Autowired      // avoid constructor 
    private CreatePersonRepo personRepo;

    @GetMapping("/updatePerson")
    public void updatePerson() {
        // // id is auto-incremented so dw about that
        // Person person = personRepo.findById((long) 1).orElse(null);
        // if (person!=null) {
        //     person.setAge(18);
        //     person.setName("mynewname");
        //     personRepo.save(person);
        //     return person;

        // }else {
        //     return null;
        // }

        personRepo.updatePerson();
    }
}
