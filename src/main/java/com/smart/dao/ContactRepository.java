package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

    // Pagination
    @Query("from Contact as c where c.user.id=:userId")
    // Current page-page
    // Contact per page-5
    public Page<Contact> findConatactByUser(@Param("userId") int UserId, Pageable pageable);

    public List<Contact> findByNameContainingAndUser(String nameKeyword, User user);
}
// findByNamecontainingAndUser