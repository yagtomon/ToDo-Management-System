// src/main/java/com/dmm/task/service/AccountUserDetailsService.java
package com.dmm.task.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.dmm.task.data.entity.Users;
import com.dmm.task.data.repository.UsersRepository;

@Service
public class AccountUserDetailsService implements UserDetailsService {

    @Autowired
    private UsersRepository repository;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        if (userName == null || "".equals(userName)) {
            throw new UsernameNotFoundException("ユーザー名が空です");
        }
        
        // データベースからアカウント情報を取得
        Users user = repository.findById(userName)
                                .orElseThrow(() -> new UsernameNotFoundException(userName + "は見つかりません。"));

        // UserDetailsの実装クラスを生成して返す
        return new AccountUserDetails(user);
    }
}