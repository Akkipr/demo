package com.example.demo.Login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginUser {

    @Autowired
    private UserRepo userRepo;

    @GetMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password) {

        // might have to do this in SQL way later
        if (email == null || email.isEmpty() ||
            password == null || password.isEmpty()) {
            return "All fields are required";
        } 

        // create user object and attempt to login
        User user = userRepo.loginUser(email, password).orElse(null);

        if (user != null) {
            return "Login successful";
        }

        return "Invalid email or password";

    }

}
