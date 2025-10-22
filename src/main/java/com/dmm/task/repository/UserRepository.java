package com.dmm.task.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

// ❌ 修正前: import com.dmm.task.entity.User;
import com.dmm.task.entity.Users; // 👈 1. Usersに修正

@Repository
// ❌ 修正前: public interface UserRepository extends JpaRepository<User, Long> {
public interface UserRepository extends CrudRepository<Users, Long> {
    Optional<Users> findByLoginId(String loginId); // ★findByLoginIdが正確であること★
}