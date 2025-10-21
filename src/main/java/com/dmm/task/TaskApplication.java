package com.dmm.task;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dmm.task.entity.User;
import com.dmm.task.repository.UserRepository;

@SpringBootApplication
public class TaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskApplication.class, args);
    }

    /**
     * アプリケーション起動時に実行され、初期ユーザーを登録する
     */
    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // --- 登録するユーザー情報 ---

            // 1. 一般ユーザー (user01 / password01)
            if (userRepository.findByLoginId("user01").isEmpty()) {
                User user1 = new User();
                user1.setLoginId("user01");
                // ★パスワードは必ずPasswordEncoderでハッシュ化して登録する！
                user1.setPassword(passwordEncoder.encode("password01")); 
                userRepository.save(user1);
                System.out.println("★ユーザー [user01] を登録しました。");
            }

            // 2. 管理者ユーザー (admin / password)
            if (userRepository.findByLoginId("admin").isEmpty()) {
                User adminUser = new User();
                adminUser.setLoginId("admin");
                // ★パスワードは必ずPasswordEncoderでハッシュ化して登録する！
                adminUser.setPassword(passwordEncoder.encode("password"));
                userRepository.save(adminUser);
                System.out.println("★管理者 [admin] を登録しました。");
            }
        };
    }
}