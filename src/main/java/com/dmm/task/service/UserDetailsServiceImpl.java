package com.dmm.task.service;

import java.util.Collections;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.dmm.task.repository.UserRepository;

// @Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.dmm.task.entity.User user = userRepository.findByLoginId(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // データベースから取得したユーザー情報 (Userエンティティ) を、
        // Spring Securityが利用できる UserDetails型 (ここではUserクラス) に変換して返す。
        // ※ここでは、権限 (Roles) は実装をシンプルにするため空にしています。
        // 管理者要件を満たすためには、UserエンティティにRoleを持たせ、ここに含める必要があります。
        return new User(
            user.getLoginId(),
            user.getPassword(), // BCryptでエンコードされたパスワード
            Collections.emptyList() // ユーザーの権限
        );
    }
}