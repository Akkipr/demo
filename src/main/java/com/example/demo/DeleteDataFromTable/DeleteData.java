package com.example.demo.DeleteDataFromTable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.CreateTable.CreatePersonRepo;


@RestController
public class DeleteData {

    @Autowired      // avoid constructor 
    private CreatePersonRepo personRepo;

    @GetMapping("/deletePerson")        // DeleteMapping does not seem to work, getting error 405
    public String deletePerson() {
        personRepo.deleteById((long) 1);
        return "Person Deleted";

    }

}
