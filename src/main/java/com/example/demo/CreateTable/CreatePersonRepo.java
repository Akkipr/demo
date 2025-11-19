package com.example.demo.CreateTable;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;

public interface CreatePersonRepo extends JpaRepository<Person, Long> {     // connect the table to the postgres database, Person class with primary key being Long type

    @Query(value = "SELECT * FROM public.demotable WHERE age > ?1", nativeQuery = true)
    List<Person> findPeopleOlderThan(int age);


    @Modifying
    @Transactional
    @Query(value = "UPDATE public.demotable SET name = 'mynewname', age = 18 WHERE id = 1", nativeQuery = true)
    void updatePerson();
}
