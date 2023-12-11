package com.smart.dao;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.smart.entities.User;

import jakarta.transaction.Transactional;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("select u from User u where u.email=:email ")
    public User getUserByUserName(@Param("email") String email);

    @Modifying
    @Transactional
    @Query("delete from Contact c where c.Id =:cid")
    public void deleteContactById(@Param("cid") Integer cid);
}
