package com.dmm.task;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

// ❌ 修正前: import com.dmm.task.entity.User;
import com.dmm.task.entity.Users; // 👈 修正: Usersエンティティをインポート
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
                // ❌ 修正前: User user1 = new User();
                Users user1 = new Users(); // 👈 修正: Usersを使用
                user1.setLoginId("user01");
                // ★パスワードは必ずPasswordEncoderでハッシュ化して登録する！
                user1.setPassword(passwordEncoder.encode("password01"));
                userRepository.save(user1);
                System.out.println("★ユーザー [user01] を登録しました。");
            }
            
            // 2. 一般ユーザー (user02 / password02) 👈 追加
            if (userRepository.findByLoginId("user02").isEmpty()) {
                Users user2 = new Users(); // 👈 修正: Usersを使用
                user2.setLoginId("user02");
                // ★パスワードは必ずPasswordEncoderでハッシュ化して登録する！
                user2.setPassword(passwordEncoder.encode("password02"));
                userRepository.save(user2);
                System.out.println("★ユーザー [user02] を登録しました。");
            }

            // 3. 管理者ユーザー (admin / password)
            if (userRepository.findByLoginId("admin").isEmpty()) {
                // ❌ 修正前: User adminUser = new User();
                Users adminUser = new Users(); // 👈 修正: Usersを使用
                adminUser.setLoginId("admin");
                // ★パスワードは必ずPasswordEncoderでハッシュ化して登録する！
                adminUser.setPassword(passwordEncoder.encode("password"));
                userRepository.save(adminUser);
                System.out.println("★管理者 [admin] を登録しました。");
            }
        };
    }
}