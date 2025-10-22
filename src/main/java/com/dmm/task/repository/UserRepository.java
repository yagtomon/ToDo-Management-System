package com.dmm.task.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.dmm.task.data.entity.Users; 

@Repository
public interface UserRepository extends CrudRepository<Users, String> { // ★ Long を String に修正 ★
    Optional<Users> findByLoginId(String loginId);
}