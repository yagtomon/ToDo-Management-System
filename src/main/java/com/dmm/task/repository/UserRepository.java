package com.dmm.task.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dmm.task.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // loginIdでユーザーを検索するメソッド
    Optional<User> findByLoginId(String loginId);
}