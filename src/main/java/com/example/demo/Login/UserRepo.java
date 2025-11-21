package com.example.demo.Login;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepo extends JpaRepository<User, Long> {

    // try to a user by email
    @Query(value = "SELECT * FROM public.users WHERE email = ?1", nativeQuery = true)
    Optional<User> findByEmail(String email);       // optional because it may or may not find a user with that email

    // insert a new user (register)
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO public.users(name, email, password) VALUES(?1, ?2, ?3)", nativeQuery = true)
    void registerUser(String name, String email, String password);

    // validate login
    @Query(value = "SELECT * FROM public.users WHERE email = ?1 AND password = ?2", nativeQuery = true)
    Optional<User> loginUser(String email, String password);
}
