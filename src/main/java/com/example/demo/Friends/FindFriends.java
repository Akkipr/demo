package com.example.demo.Friends;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Login.User;
import com.example.demo.Login.UserRepo;

import jakarta.servlet.http.HttpSession;


@RestController

public class FindFriends {

    @Autowired
    private FriendRepo friendRepo;

    @Autowired
    private UserRepo userRepo;

    @GetMapping("/friends")
    public String getFriends(HttpSession session) {
        String friendsStuff = "";

        long requesterId = (Long) session.getAttribute("userId");

        if (requesterId == 0) {
            return "No Friends Found";
        }

        List<Friend> friends = friendRepo.findFriends(requesterId);
        
        for (Friend f : friends) {
            if (f.getUser1_id() == requesterId) {
                User user4 = userRepo.findById(f.getUser2_id()).orElse(null);
                friendsStuff += "Friend Name: " + user4.getName() + "\n";
            } else {
                User user4 = userRepo.findById(f.getUser1_id()).orElse(null);
                friendsStuff += "Friend Name: " + user4.getName() + "\n";
            }
        }
        
        return friendsStuff;
    }

}
