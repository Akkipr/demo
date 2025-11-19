package com.example.demo.CreateTable;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CreatePersonRepo extends JpaRepository<Person, Long> {     // connect the table to the postgres database, Person class with primary key being Long type
}
