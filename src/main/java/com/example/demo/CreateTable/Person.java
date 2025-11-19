package com.example.demo.CreateTable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Entity         // this class would corresspond to a table in our database
@Data           // this is to avoid the getters and setters
@Table(name="demotable")    // the table in which this stuff will be added

public class Person {

    @Id         // primary key, which in our case is the Long id on line 12
    @GeneratedValue(strategy=GenerationType.IDENTITY)   //auto-increment the ID as we add more and more Person
    private Long id;
    private String name;
    private int age;




}
