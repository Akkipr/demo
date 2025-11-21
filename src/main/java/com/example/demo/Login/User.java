package com.example.demo.Login;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Entity         // this class would corresspond to a table in our database
@Data           // this is to avoid the getters and setters
@Table(name="users")    // the table in which this stuff will be added
public class User {

    @Id         
    @GeneratedValue(strategy=GenerationType.IDENTITY)   //auto-increment the ID as we add more and more users
    private Long id;
    private String name;
    private String email;
    private String password;


}
