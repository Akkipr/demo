package com.example.demo.Friends;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity         // this class would corresspond to a table in our database
@Data           // this is to avoid the getters and setters
@Table(name="friendships")    // the table in which this stuff will be added
public class Friend {

    @Id         
    @GeneratedValue(strategy=GenerationType.IDENTITY)   //auto-increment the ID as we add more and more users
    private Long id;
    private Long user1_id;
    private Long user2_id;
    private String status;
    private Long requester_id;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

}

