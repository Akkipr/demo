package com.example.demo.Login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
public class RegisterUser {

    @Autowired
    private UserRepo userRepo;

    @GetMapping("/register")
    public String register(@RequestParam String name, @RequestParam String email, @RequestParam String password, HttpSession session) {

        // might have to do this in SQL way later
        if (name == null || name.isEmpty() ||
            email == null || email.isEmpty() ||
            password == null || password.isEmpty()) {
            return "All fields are required";
        }   
        // see if email exists
        if (userRepo.findByEmail(email).isPresent()) {
            return "Email already registered";
        }

        // register user
        userRepo.registerUser(name, email, password);
        User user = userRepo.loginUser(email, password).orElse(null);
        session.setAttribute("userId", user.getId());
        session.setAttribute("email", user.getEmail());
        return "Registration successful";
    }

}
