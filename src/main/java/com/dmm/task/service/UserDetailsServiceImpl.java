package com.dmm.task.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service; // ★追加★

import com.dmm.task.data.entity.Users;
import com.dmm.task.repository.UserRepository;

@Service // ★有効化★
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepository.findByLoginId(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

List<GrantedAuthority> authorities = new ArrayList<>();
        
        // すべてのユーザーに最低限の権限 (ROLE_USER) を付与
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        // loginIdが "admin" の場合は、管理者権限 (ROLE_ADMIN) を追加
        if ("admin".equals(user.getLoginId())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return new User(
            user.getLoginId(),
            user.getPassword(),
            authorities
        );
    }
}