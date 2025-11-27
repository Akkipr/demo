package com.example.demo.Friends;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface FriendRepo extends JpaRepository<Friend, Long> {


    // check if a friendship already exists
    @Query(value = "SELECT * FROM public.friendships WHERE (user1_id = ?1 AND user2_id = ?2) OR (user1_id = ?2 AND user2_id = ?1)", nativeQuery = true)
    Optional<Friend> findRelationship(long user1_id, long user2_id);

    // send a friend request
    @Modifying          // let postgres know that we are modifying the database
    @Transactional      // wrap the operation in a single transaction to ensure there isn't anything loose
    @Query(value = "INSERT INTO public.friendships (user1_id, user2_id, status, requester_id, created_at) VALUES (?1, ?2, 'pending', ?3, NOW())", nativeQuery = true)
    void sendFriendRequest(long user1_id, long user2_id, long requesterId);

    // accept request
    @Modifying
    @Transactional
    @Query(value = "UPDATE public.friendships SET status = 'accepted', updated_at = NOW() WHERE (user1_id = ?1 AND user2_id = ?2) OR (user1_id = ?2 AND user2_id = ?1)", nativeQuery = true)
    void acceptRequest(long user1_id, long user2_id);

    // reject request
    @Modifying
    @Transactional
    @Query(value = "UPDATE public.friendships SET status = 'rejected', updated_at = NOW() WHERE user1_id = ?1 AND user2_id = ?2", nativeQuery = true)
    void rejectRequest(long user1_id, long user2_id);

    // delete friend = set to rejected
    @Modifying
    @Transactional
    @Query(value = "UPDATE public.friendships SET status = 'rejected', updated_at = NOW() WHERE (user1_id = ?1 AND user2_id = ?2) OR (user1_id = ?2 AND user2_id = ?1)", nativeQuery = true)
    void unfriend(long user1_id, long user2_id);

    // get all friends of a user
    @Query(value = "SELECT * FROM friendships WHERE (user1_id = ?1 OR user2_id = ?1) AND status = 'accepted'", nativeQuery = true)
    List<Friend> findFriends(long userId);

    // incoming requests (others sent to this user)
    @Query(value = "SELECT f.id AS id, f.requester_id AS requesterId, u.name AS requesterName FROM friendships f JOIN users u ON f.requester_id = u.id WHERE f.status = 'pending' AND (f.user1_id = ?1 OR f.user2_id = ?1) AND f.requester_id <> ?1", nativeQuery = true)
    List<IncomingRequests> findIncomingRequests(long userId);
    
    // outgoing requests (this user sent to others)
    @Query(value = "SELECT * FROM public.friendships WHERE requester_id = ?1 AND status = 'pending'", nativeQuery = true)
    List<Friend> findOutgoingRequests(long userId);

    // check if a request was rejected in the last 5 mins
    @Query(value = "SELECT * FROM public.friendships WHERE user1_id = ?1 AND user2_id = ?2 AND status = 'rejected' AND updated_at >= NOW() - INTERVAL '5 minutes'", nativeQuery = true)
    Optional<Friend> wasRecentlyRejected(long user1_id, long user2_id);

}
