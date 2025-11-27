package com.example.demo.Friends;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Login.User;
import com.example.demo.Login.UserRepo;

import jakarta.servlet.http.HttpSession;

@RestController
public class Requests {

    @Autowired
    private FriendRepo friendRepo;

    @Autowired
    private UserRepo userRepo;

    @PostMapping("/sendrequest")
    public String sendrequest(@RequestParam String email2_id, HttpSession session) {
        Long requesterId = (Long) session.getAttribute("userId");

        if (requesterId == null) {
            return "Not logged in"; //this should not happen but ehhh
        }

        User user2 = userRepo.findByEmail(email2_id).orElse(null);
        if (user2 == null) {
            return "User does not exist";
        }

        long receiverId = user2.getId();
        // check if there was a recent rejection
        Optional<Friend> recentReject = friendRepo.wasRecentlyRejected(requesterId, receiverId);
        
        if (recentReject.isPresent()) {
            return "You must wait 5 minutes before sending another request";
        }

        // check if a friendship already exists
        Optional<Friend> existing = friendRepo.findRelationship(requesterId, receiverId);

        if (existing.isPresent()) {
            if (existing.get().getStatus().equals("pending")) {
                return "Request already pending";
            }
            if (existing.get().getStatus().equals("accepted")) {
                return "You are already friends";
            }

        }

        //otherwise insert new row
        friendRepo.sendFriendRequest(requesterId, receiverId, requesterId);
        return "Friend request sent";
    }


    @PostMapping("/acceptrequest")
    public String accept(@RequestParam long senderId, HttpSession session) {
        long me = (Long) session.getAttribute("userId");

        friendRepo.acceptRequest(senderId, me);

        return "accepted";
    }

    @PostMapping("/rejectrequest")
    public String reject(@RequestParam long senderId, HttpSession session) {
        long me = (Long) session.getAttribute("userId");

        friendRepo.rejectRequest(senderId, me);

        return "rejected";
    }


    @PostMapping("/unfriend")
    public String unfriend(@RequestParam String email2, HttpSession session) {
        long requesterId = (Long) session.getAttribute("userId");

        User u2 = userRepo.findByEmail(email2).orElse(null);

        if (u2 == null) {
            return "Users not found";
        }

        friendRepo.unfriend(requesterId, u2.getId());
        return "Friend removed";
    }

    @GetMapping("/incoming")
    public List<IncomingRequests> incoming(HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return List.of();
        }

        return friendRepo.findIncomingRequests(userId);
    }

    @GetMapping("/outgoing")
    public String outgoing(HttpSession session) {
        
        String friendsStuff = "";

        long requesterId = (Long) session.getAttribute("userId");

        if (requesterId == 0) {
            return "No Friends Found";
        }

        List<Friend> friends = friendRepo.findOutgoingRequests(requesterId);
        
        for (Friend f : friends) {
            if (f.getUser1_id() == requesterId) {
                User user4 = userRepo.findById(f.getUser2_id()).orElse(null);
                friendsStuff += "Outgoing Request to: " + user4.getName() + "\n";
            } else {
                User user4 = userRepo.findById(f.getUser1_id()).orElse(null);
                friendsStuff += "Outgoing Request to: " + user4.getName() + "\n";
            }
        }
        
        return friendsStuff;

    }


}




